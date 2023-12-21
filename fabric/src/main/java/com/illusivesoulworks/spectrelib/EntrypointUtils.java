package com.illusivesoulworks.spectrelib;

import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;

public class EntrypointUtils {

  public static <T> void invokeEntrypoints(String key, Class<T> type, Consumer<? super T> invoker) {
    RuntimeException exception = null;
    Collection<EntrypointContainer<T>>
        entrypoints = FabricLoader.getInstance().getEntrypointContainers(key, type);

    SpectreConstants.LOG.debug("Iterating over entrypoint {}", key);

    for (EntrypointContainer<T> container : entrypoints) {
      try {
        invoker.accept(container.getEntrypoint());
      } catch (Throwable t) {
        exception = gatherExceptions(t, exception, exc -> new RuntimeException(String.format(
            "Could not execute entrypoint stage '%s' due to errors, provided by '%s'!", key,
            container.getProvider().getMetadata().getId()), exc));
      }
    }

    if (exception != null) {
      throw exception;
    }
  }

  public static <T extends Throwable> T gatherExceptions(Throwable exc, T prev,
                                                         Function<Throwable, T> mainExcFactory) {
    exc = unwrap(exc);

    if (prev == null) {
      return mainExcFactory.apply(exc);
    } else if (exc != prev) {

      for (Throwable t : prev.getSuppressed()) {

        if (exc.equals(t)) {
          return prev;
        }
      }
      prev.addSuppressed(exc);
    }
    return prev;
  }

  private static Throwable unwrap(Throwable exc) {

    if (exc instanceof UncheckedIOException || exc instanceof ExecutionException ||
        exc instanceof CompletionException) {
      Throwable ret = exc.getCause();

      if (ret != null) {
        return unwrap(ret);
      }
    }
    return exc;
  }
}
