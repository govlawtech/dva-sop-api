package au.gov.dva.sopapi.sopref.data;

import au.gov.dva.sopapi.ConfigurationRuntimeException;
import au.gov.dva.sopapi.exceptions.RepositoryRuntimeException;
import au.gov.dva.sopapi.interfaces.CuratedTextRepository;
import au.gov.dva.sopapi.interfaces.Repository;
import au.gov.dva.sopapi.interfaces.RuleConfigurationRepository;
import au.gov.dva.sopapi.interfaces.model.InstrumentChange;
import au.gov.dva.sopapi.interfaces.model.InstrumentChangeBase;
import au.gov.dva.sopapi.interfaces.model.ServiceDetermination;
import au.gov.dva.sopapi.interfaces.model.SoP;
import au.gov.dva.sopapi.sopref.data.curatedText.CuratedTextRepositoryImpl;
import au.gov.dva.sopapi.sopref.data.servicedeterminations.StoredServiceDetermination;
import au.gov.dva.sopapi.sopref.data.sops.StoredSop;
import au.gov.dva.sopapi.sopsupport.ruleconfiguration.CsvRuleConfigurationRepository;
import au.gov.dva.sopapi.veaops.Facade;
import au.gov.dva.sopapi.veaops.VeaDetermination;
import au.gov.dva.sopapi.veaops.interfaces.VeaOperationalServiceRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.*;
import com.microsoft.azure.storage.table.CloudTableClient;
import net.didion.jwnl.data.Exc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class AzureStorageRepository implements Repository {

    private static Logger logger = LoggerFactory.getLogger(AzureStorageRepository.class);

    private String _storageConnectionString = null;
    private static final String SOP_CONTAINER_NAME = "sops";
    private static final String SERVICE_DETERMINATIONS_CONTAINER_NAME = "servicedeterminations";
    private static final String INSTRUMENT_CHANGES_CONTAINER_NAME = "instrumentchanges";
    private static final String FAILED_INSTRUMENT_CHANGES_CONTAINER_NAME = "failedinstrumentchanges";
    private static final String ARCHIVED_SOPS_CONTAINER_NAME = "archivedsops";
    private static final String ARCHIVED_SERVICE_DETERMINATIONS_CONTAINER_NAME = "archivedservicedeterminations";
    private static final String METADATA_CONTAINER_NAME = "metadata";
    private static final String LAST_SOPS_UPDATE_BLOB_NAME = "lastsopsupdate";
    private static final String RULE_CONFIG_CONTAINER_NAME = "ruleconfiguration";
    private static final String RH_RULE_CONFIG_CSV_NAME = "rh.csv";
    private static final String BOP_RULE_CONFIG_CSV_NAME = "bop.csv";
    private static final String SOP_PDFS_CONTAINER_NAME = "soppdfs";
    private static final String VEA_OPERATIONS_CONTAINER_NAME = "arkar";
    private static final String VEA_OPERATIONS_BLOB_NAME = "veaServiceReferenceData.xml";
    private static final String CURATED_TEXT_CONTAINER_NAME = "curatedtext";
    private static final String CURATED_TEXT_FACTOR_CSV_NAME = "hand-written-factor-text.csv";
    private static final String CURATED_TEXT_DEFINITIONS_CSV_NAME = "hand-written-definition-text.csv";


    private CloudStorageAccount _cloudStorageAccount = null;
    private CloudBlobClient _cloudBlobClient = null;
    private Optional<CuratedTextRepository> _curatedTextRepository;


    public AzureStorageRepository(String storageConnectionString) {
        try {
            _storageConnectionString = storageConnectionString;
            _cloudStorageAccount = CloudStorageAccount.parse(_storageConnectionString);
            _cloudBlobClient = _cloudStorageAccount.createCloudBlobClient();
            // to test connection
            Iterable<CloudBlobContainer> containers = _cloudBlobClient.listContainers();
            logger.info(String.format("Number of containers in Azure storage: %d.", Iterables.size(containers)));
            _curatedTextRepository = buildCuratedTextRepository();


        } catch (Exception e) {
            throw new RepositoryRuntimeException(e);
        }


    }


    private Optional<CuratedTextRepository> buildCuratedTextRepository() throws URISyntaxException, StorageException {

        try {
            Optional<CloudBlob> factorCsv = getBlobByName(CURATED_TEXT_CONTAINER_NAME, CURATED_TEXT_FACTOR_CSV_NAME);
            Optional<CloudBlob> definitionsCsv = getBlobByName(CURATED_TEXT_CONTAINER_NAME, CURATED_TEXT_DEFINITIONS_CSV_NAME);
            if (!factorCsv.isPresent() || !definitionsCsv.isPresent()) {
                return Optional.empty();
            }
            byte[] factorCsvUtf8 = getBlobBytes(factorCsv.get());
            byte[] defCsvUtf8 = getBlobBytes(definitionsCsv.get());
            CuratedTextRepository ctr = new CuratedTextRepositoryImpl(factorCsvUtf8, defCsvUtf8);
            return Optional.of(ctr);
        } catch (ConfigurationRuntimeException e) {
            throw new RepositoryRuntimeException(e);
        } catch (StorageException e) {
            throw new RepositoryRuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RepositoryRuntimeException(e);
        }

    }

    private CloudBlobContainer getOrCreatePrivateContainer(String containerName) throws URISyntaxException, StorageException {
        CloudBlobClient serviceClient = _cloudStorageAccount.createCloudBlobClient();
        CloudBlobContainer container = serviceClient.getContainerReference(containerName);
        BlobContainerPermissions blobContainerPermissions = new BlobContainerPermissions();
        blobContainerPermissions.setPublicAccess(BlobContainerPublicAccessType.OFF);
        if (!container.exists()) {
            container.create();
        }
        return container;
    }

    private CloudBlobContainer getOrCreateContainer(String containerName) throws URISyntaxException, StorageException {
        CloudBlobClient serviceClient = _cloudStorageAccount.createCloudBlobClient();
        CloudBlobContainer container = serviceClient.getContainerReference(containerName);
        if (!container.exists()) {
            container.create();
            BlobContainerPermissions containerPermissions = new BlobContainerPermissions();
            containerPermissions.setPublicAccess(BlobContainerPublicAccessType.CONTAINER);
            container.uploadPermissions(containerPermissions);
        }
        return container;
    }

    @Override
    public void addSop(SoP sop) {
        try {
            CloudBlobContainer container = getOrCreateContainer(SOP_CONTAINER_NAME);
            CloudBlockBlob blob = container.getBlockBlobReference(sop.getRegisterId());
            blob.getProperties().setContentType("application/json");
            blob.getProperties().setContentEncoding("UTF-8");
            JsonNode jsonNode = StoredSop.toJson(sop);
            blob.uploadText(Conversions.toString(jsonNode));
        } catch (RuntimeException e) {
            throw new RepositoryRuntimeException(e);
        } catch (Exception e) {
            throw new RepositoryRuntimeException(e);
        }
    }


    @Override
    public Optional<SoP> getSop(String registerId) {
        try {

            Optional<CloudBlob> cloudBlob = getBlobByName(SOP_CONTAINER_NAME, registerId);

            if (!cloudBlob.isPresent())
                return Optional.empty();

            else {
                return Optional.of(blobToSoP(cloudBlob.get()));
            }

        } catch (RuntimeException e) {
            throw new RepositoryRuntimeException(e);
        } catch (Exception e) {
            throw new RepositoryRuntimeException(e);
        }
    }

    @Override
    public Optional<byte[]> getSopPdf(String registerId) {
        try {
            String blobName = String.format("%s.pdf", registerId);
            Optional<CloudBlob> cloudBlob = getBlobByName(SOP_PDFS_CONTAINER_NAME, blobName);

            if (!cloudBlob.isPresent())
                return Optional.empty();
            else {
                return Optional.of(getBlobBytes(cloudBlob.get()));
            }

        } catch (RuntimeException e) {
            throw new RepositoryRuntimeException(e);
        } catch (Exception e) {
            throw new RepositoryRuntimeException(e);
        }
    }


    @Override
    public ImmutableSet<SoP> getAllSops() {
        try {
            CloudBlobContainer cloudBlobContainer = getOrCreateContainer(SOP_CONTAINER_NAME);

            Iterable<ListBlobItem> blobs = cloudBlobContainer.listBlobs();

            List<SoP> retrievedSops = new ArrayList<>();
            for (ListBlobItem blobItem : blobs) {
                if (blobItem instanceof CloudBlob) {
                    SoP sop = blobToSoP((CloudBlob) blobItem);
                    retrievedSops.add(sop);
                }
            }

            return ImmutableSet.copyOf(retrievedSops);
        } catch (RuntimeException e) {
            throw new RepositoryRuntimeException(e);
        } catch (Exception e) {
            throw new RepositoryRuntimeException(e);
        }
    }

    @Override
    public void archiveSoP(String registerId) {
        try {
            Optional<CloudBlob> cloudBlob = getBlobByName(SOP_CONTAINER_NAME, registerId);
            if (!cloudBlob.isPresent()) {
                throw new RepositoryRuntimeException(String.format("SoP with register ID does not exist: %s", registerId));
            }

            byte[] blobBytes = getBlobBytes(cloudBlob.get());

            String archivedSopBlobName = String.format("%s_%s", registerId, UUID.randomUUID().toString());
            saveBlob(ARCHIVED_SOPS_CONTAINER_NAME, archivedSopBlobName, blobBytes);

            deleteBlob(SOP_CONTAINER_NAME, registerId);

        } catch (URISyntaxException e) {
            throw new RepositoryRuntimeException(e);
        } catch (StorageException e) {
            throw new RepositoryRuntimeException(e);
        } catch (IOException e) {
            throw new RepositoryRuntimeException(e);
        }

    }


    private static ServiceDetermination blobToServiceDetermination(CloudBlob cloudBlob) {
        try {
            JsonNode jsonNode = getJsonNode(cloudBlob);
            ServiceDetermination serviceDetermination = StoredServiceDetermination.fromJson(jsonNode);
            return serviceDetermination;
        } catch (RuntimeException e) {
            throw new RepositoryRuntimeException(e);
        } catch (Exception e) {
            throw new RepositoryRuntimeException(e);
        }
    }

    private static SoP blobToSoP(CloudBlob cloudBlob) {
        try {
            JsonNode jsonNode = getJsonNode(cloudBlob);
            SoP sop = StoredSop.fromJson(jsonNode);
            return sop;
        } catch (RuntimeException e) {
            throw new RepositoryRuntimeException(e);
        } catch (Exception e) {
            throw new RepositoryRuntimeException(e);
        }
    }


    @Override
    public ImmutableSet<InstrumentChange> getInstrumentChanges() {

        return retrieveInstrumentChanges(INSTRUMENT_CHANGES_CONTAINER_NAME);
    }

    private ImmutableSet<InstrumentChange> retrieveInstrumentChanges(String containerName) {
        CloudBlobContainer cloudBlobContainer = null;
        try {

            cloudBlobContainer = getOrCreateContainer(containerName);
            ImmutableSet.Builder<InstrumentChange> builder = new ImmutableSet.Builder<>();
            for (ListBlobItem listBlobItem : cloudBlobContainer.listBlobs()) {
                if (listBlobItem instanceof CloudBlob) {
                    blobToInstrumentChangeStream((CloudBlob) listBlobItem).forEach(builder::add);
                }
            }


        } catch (URISyntaxException | StorageException | IOException e) {
            throw new RepositoryRuntimeException(e);
        }


        Stream<ListBlobItem> blobs = StreamSupport.stream(cloudBlobContainer.listBlobs().spliterator(), false);
        return blobs.flatMap(listBlobItem -> {
            try {
                return blobToInstrumentChangeStream((CloudBlob) listBlobItem);
            } catch (IOException e) {
                throw new RepositoryRuntimeException(e);
            } catch (StorageException e) {
                throw new RepositoryRuntimeException(e);
            }
        }).collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableSet::copyOf));
    }


    @Override
    public ImmutableSet<InstrumentChange> getRetryQueue() {
        return retrieveInstrumentChanges(FAILED_INSTRUMENT_CHANGES_CONTAINER_NAME);
    }

    @Override
    public void addToRetryQueue(InstrumentChange instrumentChange) {
        addInstrumentChangesToContainer(ImmutableSet.of(instrumentChange), FAILED_INSTRUMENT_CHANGES_CONTAINER_NAME);
    }

    private static Stream<InstrumentChange> blobToInstrumentChangeStream(CloudBlob cloudBlob) throws IOException, StorageException {
        JsonNode jsonNode = getJsonNode(cloudBlob);
        ImmutableList<JsonNode> jsonObjects = JsonUtils.getChildrenOfArrayNode(jsonNode);
        return jsonObjects.stream().map(n -> InstrumentChangeBase.fromJson(n));
    }

    @Override
    public void addInstrumentChanges(ImmutableSet<InstrumentChange> instrumentChanges) {
        addInstrumentChangesToContainer(instrumentChanges, INSTRUMENT_CHANGES_CONTAINER_NAME);
    }

    private void addInstrumentChangesToContainer(ImmutableSet<InstrumentChange> instrumentChanges, String containerName) {
        try {
            CloudBlobContainer container = getOrCreateContainer(containerName);
            String newBlobName = createBlobNameForInstrumentChangeBatch(instrumentChanges);
            CloudBlockBlob blob = container.getBlockBlobReference(newBlobName);
            ObjectMapper objectMapper = new ObjectMapper();
            ArrayNode root = objectMapper.createArrayNode();
            instrumentChanges.stream().forEach(ic -> root.add(ic.toJson()));
            blob.uploadText(Conversions.toString(root));
        } catch (Exception e) {
            throw new RepositoryRuntimeException(e);
        }
    }


    @Override
    public void purgeInstrumentChanges() {
        try {
            CloudBlobContainer instrumentChangesContainer = getOrCreateContainer(INSTRUMENT_CHANGES_CONTAINER_NAME);
            Iterable<ListBlobItem> blobs = instrumentChangesContainer.listBlobs();
            for (ListBlobItem blobItem : blobs) {
                if (blobItem instanceof CloudBlob) {
                    ((CloudBlob) blobItem).deleteIfExists();
                }
            }
        } catch (StorageException | URISyntaxException e) {
            throw new RepositoryRuntimeException(e);
        }
    }

    private static String createBlobNameForInstrumentChangeBatch(ImmutableSet<InstrumentChange> instrumentChanges) {
//        A blob name must conforming to the following naming rules:
//        A blob name can contain any combination of characters.
//            A blob name must be at least one character long and cannot be more than 1,024 characters long.
//            Blob names are case-sensitive.
//            Reserved URL characters must be properly escaped.
//        The number of path segments comprising the blob name cannot exceed 254. A path segment is the string between consecutive delimiter characters (e.g., the forward slash '/') that corresponds to the name of a virtual directory.

        int numberOfChanges = instrumentChanges.size();
        String timeForBlobName = OffsetDateTime.now().format(DateTimeFormatter.ISO_INSTANT).replace(':', '-');
        String uuid = UUID.randomUUID().toString();
        String blobName = String.format("%s_%d_changes_%s.json", timeForBlobName, numberOfChanges, uuid);
        return blobName;

    }

    @Override
    public void addServiceDetermination(ServiceDetermination serviceDetermination) {
        try {
            CloudBlobContainer container = getOrCreateContainer(SERVICE_DETERMINATIONS_CONTAINER_NAME);
            CloudBlockBlob blob = container.getBlockBlobReference(serviceDetermination.getRegisterId());
            JsonNode jsonNode = StoredServiceDetermination.toJson(serviceDetermination);
            blob.getProperties().setContentType("application/json");
            blob.getProperties().setContentEncoding("UTF-8");
            blob.uploadText(Conversions.toString(jsonNode));
        } catch (RuntimeException e) {
            throw new RepositoryRuntimeException(e);
        } catch (Exception e) {
            throw new RepositoryRuntimeException(e);
        }
    }

    @Override
    public void archiveServiceDetermination(String registerId) {
        try {
            Optional<CloudBlob> cloudBlob = getBlobByName(SERVICE_DETERMINATIONS_CONTAINER_NAME, registerId);
            if (!cloudBlob.isPresent()) {
                throw new RepositoryRuntimeException(String.format("Service Determination with register ID does not exist: %s", registerId));
            }

            byte[] blobBytes = getBlobBytes(cloudBlob.get());

            String archivedServiceDeterminationBlobName = String.format("%s_%s", registerId, UUID.randomUUID().toString());
            saveBlob(ARCHIVED_SERVICE_DETERMINATIONS_CONTAINER_NAME, archivedServiceDeterminationBlobName, blobBytes);
            deleteBlob(SERVICE_DETERMINATIONS_CONTAINER_NAME, registerId);

        } catch (URISyntaxException e) {
            throw new RepositoryRuntimeException(e);
        } catch (StorageException e) {
            throw new RepositoryRuntimeException(e);
        } catch (IOException e) {
            throw new RepositoryRuntimeException(e);
        }
    }

    @Override
    public ImmutableSet<ServiceDetermination> getServiceDeterminations() {

        try {
            CloudBlobContainer cloudBlobContainer = getOrCreateContainer(SERVICE_DETERMINATIONS_CONTAINER_NAME);

            Iterable<ListBlobItem> blobs = cloudBlobContainer.listBlobs();

            List<ServiceDetermination> retrievedServiceDeterminations = new ArrayList<>();
            for (ListBlobItem blobItem : blobs) {
                if (blobItem instanceof CloudBlob) {
                    ServiceDetermination serviceDetermination = blobToServiceDetermination((CloudBlob) blobItem);
                    retrievedServiceDeterminations.add(serviceDetermination);
                }
            }

            return ImmutableSet.copyOf(retrievedServiceDeterminations);
        } catch (RuntimeException e) {
            throw new RepositoryRuntimeException(e);
        } catch (Exception e) {
            throw new RepositoryRuntimeException(e);
        }
    }

    @Override
    public Optional<OffsetDateTime> getLastUpdated() {

        try {
            Optional<CloudBlob> updated = getBlobByName(METADATA_CONTAINER_NAME, LAST_SOPS_UPDATE_BLOB_NAME);
            if (!updated.isPresent()) {
                return Optional.empty();
            }
            String updateText = getBlobString(updated.get());
            OffsetDateTime offsetDateTime = OffsetDateTime.parse(updateText, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            return Optional.of(offsetDateTime);

        } catch (URISyntaxException e) {
            throw new RepositoryRuntimeException(e);
        } catch (StorageException e) {
            throw new RepositoryRuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RepositoryRuntimeException(e);
        } catch (DateTimeParseException e) {
            throw new RepositoryRuntimeException(e);
        }
    }

    @Override
    public void setLastUpdated(OffsetDateTime offsetDateTime) {
        try {
            CloudBlobContainer container = getOrCreateContainer(METADATA_CONTAINER_NAME);
            CloudBlockBlob blob = container.getBlockBlobReference(LAST_SOPS_UPDATE_BLOB_NAME);
            blob.getProperties().setContentType("text/plain");
            blob.getProperties().setContentEncoding("UTF-8");
            blob.uploadText(offsetDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        } catch (URISyntaxException e) {
            throw new RepositoryRuntimeException(e);
        } catch (StorageException e) {
            throw new RepositoryRuntimeException(e);
        } catch (IOException e) {
            throw new RepositoryRuntimeException(e);
        }


    }

    @Override
    public Optional<RuleConfigurationRepository> getRuleConfigurationRepository() {
        try {
            Optional<CloudBlob> rhCsv = getBlobByName(RULE_CONFIG_CONTAINER_NAME, RH_RULE_CONFIG_CSV_NAME);
            Optional<CloudBlob> bopCsv = getBlobByName(RULE_CONFIG_CONTAINER_NAME, BOP_RULE_CONFIG_CSV_NAME);
            if (!rhCsv.isPresent() || !bopCsv.isPresent()) {
                return Optional.empty();
            }
            byte[] rhCsvUtf8 = getBlobBytes(rhCsv.get());
            byte[] bopCsvUtf8 = getBlobBytes(bopCsv.get());
            RuleConfigurationRepository repository = new CsvRuleConfigurationRepository(rhCsvUtf8, bopCsvUtf8);
            return Optional.of(repository);
        } catch (ConfigurationRuntimeException e) {
            throw new RepositoryRuntimeException(e);
        } catch (StorageException e) {
            throw new RepositoryRuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RepositoryRuntimeException(e);
        }
    }

    @Override
    public void setRulesConfig(byte[] rhCsv, byte[] bopCsv) {
        try {
            getOrCreatePrivateContainer(RULE_CONFIG_CONTAINER_NAME);
            saveBlob(RULE_CONFIG_CONTAINER_NAME, RH_RULE_CONFIG_CSV_NAME, rhCsv);
            saveBlob(RULE_CONFIG_CONTAINER_NAME, BOP_RULE_CONFIG_CSV_NAME, bopCsv);
        } catch (URISyntaxException e) {
            throw new RepositoryRuntimeException(e);
        } catch (StorageException e) {
            throw new RepositoryRuntimeException(e);
        } catch (IOException e) {
            throw new RepositoryRuntimeException(e);
        }
    }

    public Optional<CuratedTextRepository> getCuratedTextRepository() {
        return _curatedTextRepository;
    }

    @Override
    public Optional<VeaOperationalServiceRepository> getVeaOperationalServiceRepository() {
        try {
            Optional<CloudBlob> b = getBlobByName(VEA_OPERATIONS_CONTAINER_NAME, VEA_OPERATIONS_BLOB_NAME);
            if (!b.isPresent()) {
                return Optional.empty();
            }

            byte[] xmlBytes = getBlobBytes(b.get());

            VeaOperationalServiceRepository repo = Facade.deserialiseRepository(xmlBytes);
            return Optional.of(repo);



        } catch (Exception e) {
            throw new RepositoryRuntimeException(e);
        }

    }


    private Optional<CloudBlob> getBlobByName(String containerName, String blobName) throws URISyntaxException, StorageException {
        CloudBlobContainer cloudBlobContainer = getOrCreateContainer(containerName);
        CloudBlob cloudBlob = null;
        for (ListBlobItem blobItem : cloudBlobContainer.listBlobs()) {
            // If the item is a blob, not a virtual directory.
            if ((blobItem instanceof CloudBlob) && ((CloudBlob) blobItem).getName().equalsIgnoreCase(blobName)) {
                CloudBlob blob = (CloudBlob) blobItem;
                cloudBlob = blob;
            }
        }

        if (cloudBlob == null)
            return Optional.empty();
        else return Optional.of(cloudBlob);
    }

    private void saveBlob(String containerName, String blobName, byte[] blobBytes) throws URISyntaxException, StorageException, IOException {
        CloudBlobContainer container = getOrCreateContainer(containerName);
        CloudBlockBlob blob = container.getBlockBlobReference(blobName);
        blob.uploadFromByteArray(blobBytes, 0, blobBytes.length);
    }

    private void deleteBlob(String containerName, String blobName) throws URISyntaxException, StorageException {
        CloudBlobContainer container = getOrCreateContainer(containerName);
        CloudBlockBlob blob = container.getBlockBlobReference(blobName);
        boolean success = blob.deleteIfExists();
        if (!success) {
            logger.trace(String.format("SoP not found, therefore not deleted: %s", blobName));
        }
    }

    private static JsonNode getJsonNode(CloudBlob cloudBlob) throws StorageException, IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        cloudBlob.download(outputStream);
        String jsonString = outputStream.toString(Charsets.UTF_8.name());
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(jsonString);
    }

    private static byte[] getBlobBytes(CloudBlob cloudBlob) throws StorageException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        cloudBlob.download(outputStream);
        return outputStream.toByteArray();
    }

    private static String getBlobString(CloudBlob cloudBlob) throws StorageException, UnsupportedEncodingException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        cloudBlob.download(outputStream);
        String s = outputStream.toString(Charsets.UTF_8.name());
        return s;
    }

    @Override
    public void purge() {
        for (CloudBlobContainer cloudBlobContainer : _cloudBlobClient.listContainers()) {
            try {
                if (cloudBlobContainer.getName() != SOP_PDFS_CONTAINER_NAME) {
                    cloudBlobContainer.deleteIfExists();
                }
            } catch (StorageException e) {
                throw new RepositoryRuntimeException(e);
            }
        }

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RepositoryRuntimeException(e);
        }
    }

}



