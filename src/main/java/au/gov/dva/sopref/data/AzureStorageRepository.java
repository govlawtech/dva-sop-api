package au.gov.dva.sopref.data;

import au.gov.dva.sopref.data.SoPs.StoredSop;
import au.gov.dva.sopref.exceptions.RepositoryError;
import au.gov.dva.sopref.interfaces.Repository;
import au.gov.dva.sopref.interfaces.model.InstrumentChange;
import au.gov.dva.sopref.interfaces.model.Operation;
import au.gov.dva.sopref.interfaces.model.SoP;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.*;

import java.io.ByteArrayOutputStream;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class AzureStorageRepository implements Repository {

    private String _storageConnectionString = null;
    private static final String SOP_CONTAINER_NAME = "sops";
    private static final String OPERATIONS_CONTAINER_NAME = "operations";
    private CloudStorageAccount _cloudStorageAccount = null;
    private CloudBlobClient _cloudBlobClient = null;


    public AzureStorageRepository(String storageConnectionString)
    {
        try {
            _storageConnectionString = storageConnectionString;
            _cloudStorageAccount = CloudStorageAccount.parse(_storageConnectionString);
            _cloudBlobClient = _cloudStorageAccount.createCloudBlobClient();
        }
        catch (Exception e)
        {
            throw new RepositoryError(e);
        }
    }

    private CloudBlobContainer getOrCreateContainer(String containerName) throws URISyntaxException, StorageException {
        CloudBlobClient serviceClient = _cloudStorageAccount.createCloudBlobClient();
        CloudBlobContainer container = serviceClient.getContainerReference(SOP_CONTAINER_NAME);

        if (!container.exists()) {
            container.create();
            BlobContainerPermissions containerPermissions = new BlobContainerPermissions();
            containerPermissions.setPublicAccess(BlobContainerPublicAccessType.CONTAINER);
            container.uploadPermissions(containerPermissions);
        }
        return container;
    }

    @Override
    public void saveSop(SoP sop) {
        try {
            CloudBlobContainer container = getOrCreateContainer(SOP_CONTAINER_NAME);
            CloudBlockBlob blob = container.getBlockBlobReference(sop.getRegisterId());
            JsonNode jsonNode = StoredSop.toJson(sop);
            blob.uploadText(Conversions.toString(jsonNode));
        }
        catch (RuntimeException e)
        {
            throw new RepositoryError(e);
        }
        catch (Exception e) {
            throw new RepositoryError(e);
        }
    }

    @Override
    public Optional<SoP> getSop(String registerId) {
        try {
            CloudBlobContainer cloudBlobContainer = getOrCreateContainer(SOP_CONTAINER_NAME);

            CloudBlob cloudBlob = null;
            for (ListBlobItem blobItem : cloudBlobContainer.listBlobs()) {
                // If the item is a blob, not a virtual directory.
                if (blobItem instanceof CloudBlob && ((CloudBlob) blobItem).getName().equalsIgnoreCase(registerId)) {
                    CloudBlob blob = (CloudBlob) blobItem;
                    cloudBlob = blob;
                }
            }

            if (cloudBlob == null)
                return Optional.empty();

            else {
                return Optional.of(blobToSoP(cloudBlob));
            }

        }
        catch (RuntimeException e)
        {
            throw new RepositoryError(e);
        }
        catch (Exception e) {
            throw new RepositoryError(e);
        }
    }

    @Override
    public Iterable<SoP> getAllSops() {
          try {
            CloudBlobContainer cloudBlobContainer = _cloudBlobClient.getContainerReference(SOP_CONTAINER_NAME);

            List<SoP> sops = StreamSupport.stream(cloudBlobContainer.listBlobs().spliterator(),false)
                    .filter(listBlobItem -> listBlobItem instanceof CloudBlob)
                    .map(listBlobItem -> blobToSoP((CloudBlob)listBlobItem))
                    .collect(Collectors.toList());

            return sops;

        }
        catch (RuntimeException e)
        {
            throw new RepositoryError(e);
        }
        catch (Exception e) {
            throw new RepositoryError(e);
        }
    }

    private static SoP blobToSoP(CloudBlob cloudBlob)  {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            cloudBlob.download(outputStream);
            String jsonString = outputStream.toString(Charsets.UTF_8.name());

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonString);

            SoP sop = StoredSop.fromJson(jsonNode);
            return sop;
        }
        catch (RuntimeException e)
        {
            throw new RepositoryError(e);
        }
        catch (Exception e) {
            throw new RepositoryError(e);
        }
    }

    @Override
    public Iterable<InstrumentChange> getInstrumentChanges() {
        return null;
    }

    @Override
    public void setOperations(Iterable<Operation> operations) {

    }

    @Override
    public Iterable<Operation> getOperations() {
        return null;
    }
}
