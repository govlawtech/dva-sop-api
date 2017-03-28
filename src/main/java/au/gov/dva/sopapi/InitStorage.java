package au.gov.dva.sopapi;

import au.gov.dva.sopapi.interfaces.Repository;
import au.gov.dva.sopapi.sopref.data.AzureStorageRepository;
import au.gov.dva.sopapi.sopref.data.FederalRegisterOfLegislationClient;
import au.gov.dva.sopapi.sopref.data.updates.AutoUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InitStorage {

    private static Logger logger = LoggerFactory.getLogger("dvasopapi.initstorage");

    public static void main(String[] args)
    {
        Repository repository = new AzureStorageRepository(AppSettings.AzureStorage.getConnectionString());
        seedStorageIfNecessary(repository);
        updateNow(repository);
    }


    private static void seedStorageIfNecessary(Repository repository) {

        try {

            Seeds.queueNewSopChanges(repository);
            Seeds.addServiceDeterminations(repository, new FederalRegisterOfLegislationClient());
            Seeds.seedRuleConfiguration(repository);
        }
        catch (Exception e) {
            logger.error("Exception occurred when attempting to seed initial data to Repository.", e);
        }

        catch (Error e)
        {
            logger.error("Error occurred when attempting to seed initial data to Repository.", e);
        }
    }

    private static void updateNow(Repository repository) {

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
