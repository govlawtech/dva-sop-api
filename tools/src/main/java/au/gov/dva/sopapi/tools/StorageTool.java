package au.gov.dva.sopapi.tools;

import au.gov.dva.sopapi.interfaces.RegisterClient;
import au.gov.dva.sopapi.interfaces.Repository;
import au.gov.dva.sopapi.interfaces.model.SoP;
import au.gov.dva.sopapi.sopref.data.Conversions;
import au.gov.dva.sopapi.sopref.data.FederalRegisterOfLegislationClient;
import au.gov.dva.sopapi.sopref.data.updates.AutoUpdate;
import au.gov.dva.sopapi.sopref.parsing.ServiceLocator;
import au.gov.dva.sopapi.sopref.parsing.traits.SoPFactory;
import com.mchange.v2.c3p0.util.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class StorageTool {

    private static Logger logger = LoggerFactory.getLogger("dvasopapi.initstorage");
    private final Repository repository;
    private final RegisterClient registerClient;

    public StorageTool(Repository repository, RegisterClient registerClient)
    {
        this.repository = repository;
        this.registerClient = registerClient;
    }

    public void SeedRuleConfig(byte[] rhCsv, byte[] bopCsv)
    {
        Seeds.seedRuleConfiguration(repository,rhCsv,bopCsv);
    }

    public void SeedStorageWithSops(List<String> initialRegisterIds) throws IOException {

            for (String registerId : initialRegisterIds)
            {
                Optional<byte[]> sopPdf = getPdfBytes(registerId);
                if (sopPdf.isPresent())
                {
                    try {

                        String rawText = Conversions.croppedPdfToPlaintext(sopPdf.get(), registerId);
                        String cleansedText = ServiceLocator.findTextCleanser(registerId).clense(rawText);
                        SoPFactory sopFactory = ServiceLocator.findSoPFactory(registerId);
                        SoP sop = sopFactory.create(registerId, cleansedText);
                        repository.addSop(sop);
                        System.out.println("Added to repository: " + registerId);
                    }
                    catch (Exception e)
                    {
                        System.out.println("Failed to extract SoP: " + registerId);
                    }
                }
                else {
                    System.out.println("Could not get SoP pdf for: " + registerId);
                }
            }
    }

    public void SeedStorageUsingUpdate(List<String> initialRegisterIds)
    {
        try {
            Seeds.queueNewSopChanges(repository, initialRegisterIds);
            Seeds.addServiceDeterminations(repository, registerClient);
            AutoUpdate.patchSoPChanges(repository);
            repository.purgeInstrumentChanges();
        } catch (Exception e) {
            logger.error("Exception occurred when attempting to seed initial data to Repository.", e);
        } catch (Error e) {
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

    private Optional<byte[]> getPdfBytes(String registerid)
    {

        Optional<byte[]> sopPdfFromCache = repository.getSopPdf(registerid);
        if (sopPdfFromCache.isPresent()) {
            return sopPdfFromCache;
        }
        else {
            try {
                byte[] fromFrl = registerClient.getAuthorisedInstrumentPdf(registerid).get(20, TimeUnit.SECONDS);
                return Optional.of(fromFrl);
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                e.printStackTrace();
                return Optional.empty();
            }
        }
    }
}
