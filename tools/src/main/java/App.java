import au.gov.dva.sopapi.AppSettings;
import au.gov.dva.sopapi.interfaces.RegisterClient;
import au.gov.dva.sopapi.interfaces.Repository;
import au.gov.dva.sopapi.sopref.data.AzureStorageRepository;
import au.gov.dva.sopapi.sopref.data.FederalRegisterOfLegislationClient;
import au.gov.dva.sopapi.tools.StorageTool;

public class App {
    public static void main(String[] args) {
        // methods required:
        // init storage
        // update rule configuration from spreadsheet to repository

        RegisterClient registerClient = new FederalRegisterOfLegislationClient();
        Repository repository = new AzureStorageRepository(AppSettings.AzureStorage.getConnectionString());
        repository.purge();
        StorageTool storageTool = new StorageTool(repository,registerClient);
        storageTool.SeedRuleConfig();
        storageTool.SeedStorage();
        storageTool.Update();
    }
}
