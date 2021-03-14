package net.minecraft.util;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import org.apache.logging.log4j.Logger;

public class Util {
  public static EnumOS getOSType() {
    String s = System.getProperty("os.name").toLowerCase();
    return s.contains("win") ? EnumOS.WINDOWS : (s.contains("mac") ? EnumOS.OSX : (s.contains("solaris") ? EnumOS.SOLARIS : (s.contains("sunos") ? EnumOS.SOLARIS : (s.contains("linux") ? EnumOS.LINUX : (s.contains("unix") ? EnumOS.LINUX : EnumOS.UNKNOWN)))));
  }
  
  public static <V> V runTask(FutureTask<V> task, Logger logger) {
    try {
      task.run();
      return task.get();
    } catch (ExecutionException executionexception) {
      logger.fatal("Error executing task", executionexception);
      if (executionexception.getCause() instanceof OutOfMemoryError) {
        OutOfMemoryError outofmemoryerror = (OutOfMemoryError)executionexception.getCause();
        throw outofmemoryerror;
      } 
    } catch (InterruptedException interruptedexception) {
      logger.fatal("Error executing task", interruptedexception);
    } 
    return null;
  }
  
  public enum EnumOS {
    LINUX, SOLARIS, WINDOWS, OSX, UNKNOWN;
  }
}
