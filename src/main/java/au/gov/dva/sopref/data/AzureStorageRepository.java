package au.gov.dva.sopref.data;

import au.gov.dva.AppSettings;
import au.gov.dva.sopref.exceptions.LegislationRegisterError;
import au.gov.dva.sopref.exceptions.RepositoryError;
import au.gov.dva.sopref.interfaces.Repository;
import au.gov.dva.sopref.interfaces.model.InstrumentChange;
import au.gov.dva.sopref.interfaces.model.Operation;
import au.gov.dva.sopref.interfaces.model.SoP;
import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.*;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;

public class AzureStorageRepository implements Repository {
    private static AzureStorageRepository ourInstance = new AzureStorageRepository();

    public static AzureStorageRepository getInstance() {
        return ourInstance;
    }

    private static final String storageConnectionString = AppSettings.AzureStorage.storageConnectionString();
    private static final String sopContainerName = "sops";
    private final CloudStorageAccount _cloudStorageAccount;
    private final CloudBlobClient _cloudBlobClient;

    private AzureStorageRepository() {
        try {
            _cloudStorageAccount = CloudStorageAccount.parse(storageConnectionString);
            _cloudBlobClient = _cloudStorageAccount.createCloudBlobClient();
        }
        catch (Exception e) {
            throw new RepositoryError(e);
        }
    }


    @Override
    public void saveSop(SoP sop) {
        try {
            CloudBlobClient serviceClient = _cloudStorageAccount.createCloudBlobClient();
            CloudBlobContainer container = null;
            assert(sopContainerName.matches("[a-z]+"));
            container = serviceClient.getContainerReference(sopContainerName);

            if (!container.exists()) {
                container.create();
                BlobContainerPermissions containerPermissions = new BlobContainerPermissions();
                containerPermissions.setPublicAccess(BlobContainerPublicAccessType.CONTAINER);
                container.uploadPermissions(containerPermissions);
            }

            CloudBlockBlob blob = container.getBlockBlobReference(sop.getRegisterId());
            JsonNode jsonNode = Conversions.toJson(sop);
            blob.uploadText(Conversions.toString(jsonNode));
        } catch (Exception e) {
            throw new RepositoryError(e);
        }
    }

    @Override
    public SoP getSop(String registerId) {
        return null;
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
