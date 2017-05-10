package au.gov.dva.sopapi.sopref.data.updates;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

class AsyncUtils {


 public static <T> CompletableFuture<List<T>> sequence(List<CompletableFuture<T>> futures) {
  CompletableFuture<Void> allDoneFuture =
          CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
  return allDoneFuture.thenApply(v ->
          futures.stream().
                  map(future -> future.join()).
                  collect(Collectors.toList())
  );
 }

}
