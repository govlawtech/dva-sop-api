package au.gov.dva.sopapi.tools;

import au.gov.dva.sopapi.AppSettings;
import au.gov.dva.sopapi.interfaces.RegisterClient;
import au.gov.dva.sopapi.interfaces.Repository;
import au.gov.dva.sopapi.sopref.data.AzureStorageRepository;
import au.gov.dva.sopapi.sopref.data.FederalRegisterOfLegislationClient;
import au.gov.dva.sopapi.sopref.data.updates.AutoUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class StorageTool {

    private static Logger logger = LoggerFactory.getLogger("dvasopapi.initstorage");
    private final Repository repository;
    private final RegisterClient registerClient;

    public StorageTool(Repository repository, RegisterClient registerClient)
    {
        this.repository = repository;
        this.registerClient = registerClient;
    }

    public void SeedRuleConfig()
    {
        Seeds.seedRuleConfiguration(repository);
    }

    public void SeedStorage(List<String> initialRegisterIds) {

        try {
            Seeds.queueNewSopChanges(repository,initialRegisterIds);
            Seeds.addServiceDeterminations(repository, registerClient);
            AutoUpdate.patchSoPChanges(repository);
            repository.purgeInstrumentChanges();
        }
        catch (Exception e) {
            logger.error("Exception occurred when attempting to seed initial data to Repository.", e);
        }

        catch (Error e)
        {
            logger.error("Error occurred when attempting to seed initial data to Repository.", e);
        }
    }


    public void Update() {

        try {
            AutoUpdate.updateSopsChangeList(repository);
            AutoUpdate.patchSoPChanges(repository);
            AutoUpdate.updateServiceDeterminations(repository,new FederalRegisterOfLegislationClient());
        }
        catch (Exception e) {
            logger.error("Exception occurred when attempting immediate Repository update.", e);
        }

        catch (Error e)
        {
             logger.error("Error occurred when attempting immediate Repository update.", e);
        }
    }
}
