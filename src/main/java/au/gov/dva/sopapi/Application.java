package au.gov.dva.sopapi;

import au.gov.dva.sopapi.interfaces.Repository;
import au.gov.dva.sopapi.sopref.data.AzureStorageRepository;
import au.gov.dva.sopapi.sopref.data.FederalRegisterOfLegislationClient;
import au.gov.dva.sopapi.sopref.data.updates.AutoUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static spark.Spark.get;

public class Application implements spark.servlet.SparkApplication {

    private Repository _repository;
    private Cache _cache;

    private static Logger logger = LoggerFactory.getLogger("dvasopapi.rootapplicationlogger");

    public Application() {

    }

    @Override
    public void init() {

        _repository = new AzureStorageRepository(AppSettings.AzureStorage.getConnectionString());
        _cache = Cache.getInstance();
        _cache.refresh(_repository);

        autoUpdate();
        Routes.init(_cache);
        Routes.initStatus(_repository,_cache);
    }




    private void autoUpdate() {


        try {
            startScheduledUpdates();
        }
        catch (Exception e)
        {
            logger.error("Exception occurred when attempting to start scheduled Repository updates.", e);
        }
        catch (Error e)
        {
            logger.error("Error occurred when attempting to start scheduled Repository updates.", e);
        }

    }


    private void startScheduledUpdates() {
        startScheduledPollingForSoPChanges(LocalTime.of(3, 30));
        startScheduledLoadingOfSops(LocalTime.of(20, 0));
        startScheduledPollingForServiceDeterminationChanges(LocalTime.of(0, 0));
    }

    private void startScheduledPollingForServiceDeterminationChanges(LocalTime localTime) {
        startDailyExecutor(localTime, () -> {
          AutoUpdate.updateServiceDeterminations(_repository,new FederalRegisterOfLegislationClient());
          _cache.refresh(_repository);
        });
    }

    private void startScheduledPollingForSoPChanges(LocalTime runTime) {
        startDailyExecutor(runTime, () -> {
            AutoUpdate.updateSopsChangeList(_repository);
            _cache.refresh(_repository);
        });
    }

    private void startScheduledLoadingOfSops(LocalTime runTime) {
        startDailyExecutor(runTime, () -> {
            AutoUpdate.patchSoPChanges(_repository);
            _cache.refresh(_repository);
        });

    }

    private void startDailyExecutor(LocalTime runTime, Runnable runnable) {
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        OffsetDateTime nowCanberraTime = OffsetDateTime.now(ZoneId.of(DateTimeUtils.TZDB_REGION_CODE));
        OffsetDateTime scheduledTimeTodayCanberraTime = OffsetDateTime.from(
                ZonedDateTime.of(nowCanberraTime.toLocalDate(),
                        runTime,
                        ZoneId.of(DateTimeUtils.TZDB_REGION_CODE)));
        OffsetDateTime nextScheduledTime = scheduledTimeTodayCanberraTime.isAfter(nowCanberraTime) ? scheduledTimeTodayCanberraTime : scheduledTimeTodayCanberraTime.plusDays(1);
        long minutesToNextScheduledTime = Duration.between(nowCanberraTime, nextScheduledTime).toMinutes();
        scheduledExecutorService.scheduleAtFixedRate(runnable,
                minutesToNextScheduledTime,
                Duration.ofDays(1).toMinutes(),
                TimeUnit.MINUTES);

    }

}
