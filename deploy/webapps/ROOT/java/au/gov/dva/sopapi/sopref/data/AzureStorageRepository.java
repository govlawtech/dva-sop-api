package au.gov.dva.sopapi.sopref.data;

import au.gov.dva.sopapi.exceptions.RepositoryError;
import au.gov.dva.sopapi.interfaces.Repository;
import au.gov.dva.sopapi.interfaces.model.InstrumentChange;
import au.gov.dva.sopapi.interfaces.model.InstrumentChangeBase;
import au.gov.dva.sopapi.interfaces.model.ServiceDetermination;
import au.gov.dva.sopapi.interfaces.model.SoP;
import au.gov.dva.sopapi.sopref.data.servicedeterminations.StoredServiceDetermination;
import au.gov.dva.sopapi.sopref.data.sops.StoredSop;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.*;
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
    private static final String ARCHIVED_SOPS_CONTAINER_NAME = "archivedsops";
    private static final String ARCHIVED_SERVICE_DETERMINATIONS_CONTAINER_NAME = "archivedservicedeterminations";
    private static final String METADATA_CONTAINER_NAME = "metadata";
    private static final String LAST_SOPS_UPDATE_BLOB_NAME = "lastsopsupdate";

    private CloudStorageAccount _cloudStorageAccount = null;
    private CloudBlobClient _cloudBlobClient = null;


    public AzureStorageRepository(String storageConnectionString) {
        try {
            _storageConnectionString = storageConnectionString;
            _cloudStorageAccount = CloudStorageAccount.parse(_storageConnectionString);
            _cloudBlobClient = _cloudStorageAccount.createCloudBlobClient();
        } catch (Exception e) {
            throw new RepositoryError(e);
        }
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
            throw new RepositoryError(e);
        } catch (Exception e) {
            throw new RepositoryError(e);
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
            throw new RepositoryError(e);
        } catch (Exception e) {
            throw new RepositoryError(e);
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
            throw new RepositoryError(e);
        } catch (Exception e) {
            throw new RepositoryError(e);
        }
    }

    @Override
    public void archiveSoP(String registerId) {
        try {
            Optional<CloudBlob> cloudBlob = getBlobByName(SOP_CONTAINER_NAME, registerId);
            if (!cloudBlob.isPresent()) {
                throw new RepositoryError(String.format("SoP with register ID does not exist: %s", registerId));
            }

            byte[] blobBytes = getBlobBytes(cloudBlob.get());

            String archivedSopBlobName = String.format("%s_%s", registerId, UUID.randomUUID().toString());
            saveBlob(ARCHIVED_SOPS_CONTAINER_NAME, archivedSopBlobName, blobBytes);

            deleteBlob(SOP_CONTAINER_NAME, registerId);

        } catch (URISyntaxException e) {
            throw new RepositoryError(e);
        } catch (StorageException e) {
            throw new RepositoryError(e);
        } catch (IOException e) {
            throw new RepositoryError(e);
        }

    }


    private static ServiceDetermination blobToServiceDetermination(CloudBlob cloudBlob) {
        try {
            JsonNode jsonNode = getJsonNode(cloudBlob);
            ServiceDetermination serviceDetermination = StoredServiceDetermination.fromJson(jsonNode);
            return serviceDetermination;
        } catch (RuntimeException e) {
            throw new RepositoryError(e);
        } catch (Exception e) {
            throw new RepositoryError(e);
        }
    }


    private static SoP blobToSoP(CloudBlob cloudBlob) {
        try {
            JsonNode jsonNode = getJsonNode(cloudBlob);
            SoP sop = StoredSop.fromJson(jsonNode);
            return sop;
        } catch (RuntimeException e) {
            throw new RepositoryError(e);
        } catch (Exception e) {
            throw new RepositoryError(e);
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
    public ImmutableSet<InstrumentChange> getInstrumentChanges() {


        CloudBlobContainer cloudBlobContainer = null;
        try {

            cloudBlobContainer = getOrCreateContainer(INSTRUMENT_CHANGES_CONTAINER_NAME);
            ImmutableSet.Builder<InstrumentChange> builder = new ImmutableSet.Builder<>();
            for (ListBlobItem listBlobItem : cloudBlobContainer.listBlobs()) {
                if (listBlobItem instanceof CloudBlob) {
                    blobToInstrumentChangeStream((CloudBlob) listBlobItem).forEach(builder::add);
                }
            }


        } catch (URISyntaxException e) {
            throw new RepositoryError(e);
        } catch (StorageException e) {
            throw new RepositoryError(e);
        } catch (IOException e) {
            throw new RepositoryError(e);
        }


        Stream<ListBlobItem> blobs = StreamSupport.stream(cloudBlobContainer.listBlobs().spliterator(), false);
        return blobs.flatMap(listBlobItem -> {
            try {
                return blobToInstrumentChangeStream((CloudBlob) listBlobItem);
            } catch (IOException e) {
                throw new RepositoryError(e);
            } catch (StorageException e) {
                throw new RepositoryError(e);
            }
        }).collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableSet::copyOf));
    }

    private static Stream<InstrumentChange> blobToInstrumentChangeStream(CloudBlob cloudBlob) throws IOException, StorageException {
        JsonNode jsonNode = getJsonNode(cloudBlob);
        ImmutableList<JsonNode> jsonObjects = JsonUtils.getChildrenOfArrayNode(jsonNode);
        return jsonObjects.stream().map(n -> InstrumentChangeBase.fromJson(n));
    }

    @Override
    public void addInstrumentChanges(ImmutableSet<InstrumentChange> instrumentChanges) {
        try {
            CloudBlobContainer container = getOrCreateContainer(INSTRUMENT_CHANGES_CONTAINER_NAME);

            String newBlobName = createBlobNameForInstrumentChangeBatch(instrumentChanges);
            CloudBlockBlob blob = container.getBlockBlobReference(newBlobName);
            ObjectMapper objectMapper = new ObjectMapper();
            ArrayNode root = objectMapper.createArrayNode();
            instrumentChanges.stream().forEach(ic -> root.add(ic.toJson()));
            blob.uploadText(Conversions.toString(root));
        } catch (RuntimeException e) {
            throw new RepositoryError(e);
        } catch (Exception e) {
            throw new RepositoryError(e);
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
            throw new RepositoryError(e);
        } catch (Exception e) {
            throw new RepositoryError(e);
        }
    }

    @Override
    public void archiveServiceDetermination(String registerId) {
        try {
            Optional<CloudBlob> cloudBlob = getBlobByName(SERVICE_DETERMINATIONS_CONTAINER_NAME, registerId);
            if (!cloudBlob.isPresent()) {
                throw new RepositoryError(String.format("Service Determination with register ID does not exist: %s", registerId));
            }

            byte[] blobBytes = getBlobBytes(cloudBlob.get());

            String archivedServiceDeterminationBlobName = String.format("%s_%s", registerId, UUID.randomUUID().toString());
            saveBlob(ARCHIVED_SERVICE_DETERMINATIONS_CONTAINER_NAME, archivedServiceDeterminationBlobName, blobBytes);
            deleteBlob(SERVICE_DETERMINATIONS_CONTAINER_NAME, registerId);

        } catch (URISyntaxException e) {
            throw new RepositoryError(e);
        } catch (StorageException e) {
            throw new RepositoryError(e);
        } catch (IOException e) {
            throw new RepositoryError(e);
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
            throw new RepositoryError(e);
        } catch (Exception e) {
            throw new RepositoryError(e);
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
            throw new RepositoryError(e);
        } catch (StorageException e) {
            throw new RepositoryError(e);
        } catch (UnsupportedEncodingException e) {
            throw new RepositoryError(e);
        } catch (DateTimeParseException e) {
            throw new RepositoryError(e);
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
            throw new RepositoryError(e);
        } catch (StorageException e) {
            throw new RepositoryError(e);
        } catch (IOException e) {
            throw new RepositoryError(e);
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

    @Override
    public void purge() {
        for (CloudBlobContainer cloudBlobContainer : _cloudBlobClient.listContainers()) {
            try {
                cloudBlobContainer.deleteIfExists();
            } catch (StorageException e) {
                throw new RepositoryError(e);
            }
        }
    }

}



