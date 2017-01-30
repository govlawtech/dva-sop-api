package au.gov.dva.sopapi.sopref.data.updates;

import au.gov.dva.sopapi.interfaces.RegisterClient;
import au.gov.dva.sopapi.interfaces.model.InstrumentChange;
import au.gov.dva.sopapi.sopref.data.updates.types.NewCompilation;
import au.gov.dva.sopapi.sopref.data.updates.types.Replacement;
import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class LegRegChangeDetector {

    private RegisterClient registerClient;
    private static final Logger logger = LoggerFactory.getLogger(LegRegChangeDetector.class);

    public LegRegChangeDetector(RegisterClient registerClient) {
        this.registerClient = registerClient;
    }

    public ImmutableSet<InstrumentChange> detectNewCompilations(ImmutableSet<String> registerIds)
    {
        List<CompletableFuture<RedirectResult>> tasks = registerIds.stream()
                .map(s -> getRedirectResult(s))
                .collect(Collectors.toList());
        try {
            List<RedirectResult> results = AsyncUtils.sequence(tasks).get();
            List<NewCompilation> compilations = results.stream()
                    .filter(RedirectResult::isUpdatedCompilation)
                    .map(redirectResult -> new NewCompilation(redirectResult.getSource(),redirectResult.getTarget().get(), OffsetDateTime.now()))
                    .collect(Collectors.toList());
            return ImmutableSet.copyOf(compilations);

        } catch (InterruptedException e) {
            logger.error("Task to get all redirect targets for all Register IDs was interrupted.",e);
            return ImmutableSet.of();
        } catch (ExecutionException e) {
            logger.error("Task to get all redirect targets for all Register IDs threw execution exception .",e);
            return ImmutableSet.of();
        }
    }

    public ImmutableSet<InstrumentChange> detectReplacements(ImmutableSet<String> registerIds) {

        List<CompletableFuture<ReplacementResult>> batch = registerIds.stream()
                .map(s -> getReplacementResult(s))
                .collect(Collectors.toList());

        try {
            List<InstrumentChange> results = AsyncUtils.sequence(batch).get()
                    .stream()
                    .filter(r -> r.getNewRegisterId().isPresent())
                    .map(r -> new Replacement(r.getNewRegisterId().get(),OffsetDateTime.now(),r.getOriginalRegisterId()))
                    .collect(Collectors.toList());

            return ImmutableSet.copyOf(results);

        } catch (InterruptedException e) {
            logger.error("Task to get all replacing instruments was interrupted.",e);
            return ImmutableSet.of();
        } catch (ExecutionException e) {
            logger.error("task to get all replacing instruments threw Execution Exception.",e);
            return ImmutableSet.of();
        }
    }



    private CompletableFuture<ReplacementResult> getReplacementResult(String originalRegisterId)
    {
        return registerClient.getRepealingRegisterId(originalRegisterId)
                .thenApply(s -> new ReplacementResult(originalRegisterId,s));
    }

    private CompletableFuture<RedirectResult> getRedirectResult(String registerId)
    {
        return  registerClient.getRedirectTargetRegisterId(registerId)
                .handle((s, throwable) -> {
                    if (s != null)
                    {
                         return new RedirectResult(registerId,Optional.of(s));
                    }
                    else {
                        logger.error(String.format("Could not get redirect target for Register ID %s", s));
                        return new RedirectResult(registerId,Optional.empty());
                    }
                });
    }


    private static class ReplacementResult{
        private final String originalRegisterId;
        private final Optional<String> newRegisterId;

        public ReplacementResult(String originalRegisterId, Optional<String> newRegisterId) {
            this.originalRegisterId = originalRegisterId;
            this.newRegisterId = newRegisterId;
        }

        public Optional<String> getNewRegisterId() {
            return newRegisterId;
        }

        public String getOriginalRegisterId() {
            return originalRegisterId;
        }
    }


    private static class RedirectResult {
        private final String source;
        private final Optional<String> target;

        public RedirectResult(String source, Optional<String> target) {
            this.source = source;
            this.target = target;
        }

        public Optional<String> getTarget() {
            return target;
        }

        public String getSource() {
            return source;
        }

        public boolean isUpdatedCompilation()
        {
            if (!getTarget().isPresent())
                return false;

            else {
                return !target.get().contentEquals(source);
            }
        }
    }
}
