package au.gov.dva.sopapi.sopref.data.updates;

import au.gov.dva.sopapi.interfaces.RegisterClient;
import au.gov.dva.sopapi.interfaces.model.InstrumentChange;
import au.gov.dva.sopapi.sopref.data.updates.types.NewCompilation;
import au.gov.dva.sopapi.sopref.data.updates.types.Replacement;
import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class LegRegChangeDetector {

    private RegisterClient registerClient;
    private static final Logger logger = LoggerFactory.getLogger("dvasopapi.frlchangedetector");

    public LegRegChangeDetector(RegisterClient registerClient) {
        this.registerClient = registerClient;
    }

    public ImmutableSet<InstrumentChange> detectNewCompilations(ImmutableSet<String> registerIds)
    {
        List<InstrumentChange> acc = new ArrayList<>();

        registerIds.forEach(s -> {
            CompletableFuture<RedirectResult> task = getRedirectResult(s);
            try {
                logger.trace(String.format("Checking for any new compilations for: %s...", s));
                RedirectResult result = task.get(10, TimeUnit.SECONDS);
                if (result.isUpdatedCompilation()) {
                    NewCompilation newCompilation = new NewCompilation(result.getSource(), result.getTarget().get(), OffsetDateTime.now());
                    logger.trace(String.format("...new compilation found: %s",newCompilation));
                    acc.add(newCompilation);
                }
                logger.trace("...none found.");

            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                logger.error(String.format("Unable to check for new compilation for instrument ID %s", s),e);
            }
        });

        return ImmutableSet.copyOf(acc);
    }

    public ImmutableSet<InstrumentChange> detectReplacements(ImmutableSet<String> registerIds) {

        List<InstrumentChange> acc = new ArrayList<>();

        registerIds.forEach(s -> {
            CompletableFuture<ReplacementResult> task = getReplacementResult(s);
            try {
                logger.trace(String.format("Checking for replacement instrument for: %s...", s));
                ReplacementResult result = task.get(10, TimeUnit.SECONDS);
                if (result.isReplaced()) {
                    Replacement replacement = new Replacement(result.getNewRegisterId().get(), OffsetDateTime.now(), result.getOriginalRegisterId());
                    logger.trace(String.format("...replacement found: %s",replacement));
                    acc.add(replacement);
                }
                logger.trace("...none found.");
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                logger.error(String.format("Unable to check for replacements for instrument ID %s", s),e);
            }
        });

        return ImmutableSet.copyOf(acc);
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
                        logger.error(String.format("Could not get redirect target for Register ID %s.",registerId),throwable);
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

        public boolean isReplaced(){
            return this.getNewRegisterId().isPresent() && !this.getOriginalRegisterId().contentEquals(this.getNewRegisterId().get());
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
