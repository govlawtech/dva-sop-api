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

public class Application implements spark.servlet.SparkApplication {

    private Repository _repository;
    private Cache _cache;

    private static Logger logger = LoggerFactory.getLogger(Application.class);

    public Application() {

    }

    @Override
    public void init()
    {
        _repository = new AzureStorageRepository(AppSettings.AzureStorage.getConnectionString());
        if (AppSettings.getEnvironment() == AppSettings.Environment.devtestlocal)
            _repository.purge();
        _cache = Cache.getInstance();
        seedStorageIfNecessary();
        autoUpdate();
        Routes.init(_cache);
    }

    private void seedStorageIfNecessary() {
        if (_repository.getAllSops().isEmpty() && _repository.getInstrumentChanges().isEmpty())
        {
            Seeds.queueNewSopChanges(_repository);
        }

        if (_repository.getServiceDeterminations().isEmpty())
        {
            Seeds.addServiceDeterminations(_repository,new FederalRegisterOfLegislationClient());
        }
    }

    private void autoUpdate(){

        try {
            updateNow();
            startScheduledUpdates();
        }
        catch (DvaSopApiError e)
        {
            logger.error("Error occurred during update.",e);
        }
    }



    private void startScheduledUpdates() {
        startScheduledPollingForSoPChanges(LocalTime.of(3,30));
        startScheduledLoadingOfSops(LocalTime.of(20,0));
        startScheduledPollingForServiceDeterminationChanges(LocalTime.of(0,0));
    }

    private void updateNow()
    {
        updateSops().run();
        AutoUpdate.patchSoPChanges(_repository);
        updateServiceDeterminations().run();
        _cache.refresh(_repository);
    }




    private Runnable updateSops() {
        return () -> {
            AutoUpdate.updateChangeList(
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
        startDailyExecutor(localTime,updateServiceDeterminations());
    }

    private void startScheduledPollingForSoPChanges(LocalTime runTime) {
       startDailyExecutor(runTime, updateSops());
    }


    private void startScheduledLoadingOfSops(LocalTime runTime) {
        startDailyExecutor(runTime,() -> {
            AutoUpdate.patchSoPChanges(_repository);
            _cache.refresh(_repository);
        });

    }

    private void startDailyExecutor(LocalTime runTime, Runnable runnable)
    {
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
