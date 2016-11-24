package au.gov.dva.sopref.data;

import au.gov.dva.AppSettings;
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
import com.microsoft.azure.storage.blob.*;

import java.io.ByteArrayOutputStream;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Optional;

public class AzureStorageRepository implements Repository {

    private String _storageConnectionString = null;
    private static final String SOP_CONTAINER_NAME = "sops";
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

    @Override
    public void saveSop(SoP sop) {
        try {
            CloudBlobClient serviceClient = _cloudStorageAccount.createCloudBlobClient();
            CloudBlobContainer container = null;
            container = serviceClient.getContainerReference(SOP_CONTAINER_NAME);

            if (!container.exists()) {
                container.create();
                BlobContainerPermissions containerPermissions = new BlobContainerPermissions();
                containerPermissions.setPublicAccess(BlobContainerPublicAccessType.CONTAINER);
                container.uploadPermissions(containerPermissions);
            }

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
            CloudBlobContainer cloudBlobContainer = _cloudBlobClient.getContainerReference(SOP_CONTAINER_NAME);

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
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                cloudBlob.download(outputStream);
                String jsonString = outputStream.toString(Charsets.UTF_8.name());

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(jsonString);

                SoP sop = StoredSop.fromJson(jsonNode);
                return Optional.of(sop);
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
        return null;
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
