package me.kaimson.melonclient.smoothscrolling;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RunSixtyTimesEverySecImpl {
  public static final List<RunSixtyTimesEverySec> TICKS_LIST = Lists.newCopyOnWriteArrayList();
  
  private static final ScheduledExecutorService EXECUTOR_SERVICE;
  
  static {
    (EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor()).scheduleAtFixedRate(() -> {
          TICKS_LIST.removeIf(Objects::isNull);
          TICKS_LIST.iterator().forEachRemaining(Runnable::run);
        }0L, 16L, TimeUnit.MILLISECONDS);
  }
}
