package au.gov.dva.sopapi;

import au.gov.dva.sopapi.exceptions.DvaSopApiError;
import au.gov.dva.sopapi.interfaces.Repository;
import au.gov.dva.sopapi.sopref.data.AzureStorageRepository;
import au.gov.dva.sopapi.sopref.data.FederalRegisterOfLegislationClient;
import au.gov.dva.sopapi.sopref.data.updates.AutoUpdate;
import au.gov.dva.sopapi.sopref.data.updates.LegislationRegisterEmailClientImpl;
import au.gov.dva.sopapi.sopref.data.updates.changefactories.EmailSubscriptionInstrumentChangeFactory;
import au.gov.dva.sopapi.sopref.data.updates.changefactories.LegislationRegisterSiteChangeFactory;
import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

        Environment environment = AppSettings.getEnvironment();
        if (environment == Environment.devtestlocal)
        {
            _repository.purge();
            seedStorageIfNecessary();
            updateNow();
        }

        autoUpdate();
        Routes.init(_cache);
        Routes.initStatus(_repository,_cache);
    }

    private void seedStorageIfNecessary() {

        try {

            Seeds.queueNewSopChanges(_repository);
            Seeds.addServiceDeterminations(_repository, new FederalRegisterOfLegislationClient());
            Seeds.seedRuleConfiguration(_repository);
        }
        catch (Exception e) {
            logger.error("Exception occurred when attempting to seed initial data to Repository.", e);
        }

        catch (Error e)
        {
            logger.error("Error occurred when attempting to seed initial data to Repository.", e);
        }
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

    private void updateNow() {

        try {
            updateSopsChangeList().run();
            AutoUpdate.patchSoPChanges(_repository);
            updateServiceDeterminations().run();
            _cache.refresh(_repository);
        }
         catch (Exception e) {
            logger.error("Exception occurred when attempting immediate Repository update.", e);
        }

        catch (Error e)
        {
            logger.error("Error occurred when attempting immediate Repository update.", e);
        }
    }


    private Runnable updateSopsChangeList() {
        return () -> {
            AutoUpdate.patchChangeList(
                    _repository,
                    new EmailSubscriptionInstrumentChangeFactory(
                            new LegislationRegisterEmailClientImpl("noreply@legislation.gov.au"),
                            () -> _repository.getLastUpdated().orElse(OffsetDateTime.now().minusDays(1))),
                    new LegislationRegisterSiteChangeFactory(
                            new FederalRegisterOfLegislationClient(),
                            () -> _repository.getAllSops().stream().map(
                                    s -> s.getRegisterId())
                                    .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableSet::copyOf))));
            _cache.refresh(_repository);
        };
    }

    private Runnable updateServiceDeterminations() {

        return () -> {
            AutoUpdate.updateServiceDeterminations(_repository, new FederalRegisterOfLegislationClient());
            _cache.refresh(_repository);
        };
    }

    private void startScheduledPollingForServiceDeterminationChanges(LocalTime localTime) {
        startDailyExecutor(localTime, updateServiceDeterminations());
    }

    private void startScheduledPollingForSoPChanges(LocalTime runTime) {
        startDailyExecutor(runTime, updateSopsChangeList());
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
