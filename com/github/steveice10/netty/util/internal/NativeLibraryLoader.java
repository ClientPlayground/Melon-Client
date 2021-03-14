package com.github.steveice10.netty.util.internal;

import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class NativeLibraryLoader {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(NativeLibraryLoader.class);
  
  private static final String NATIVE_RESOURCE_HOME = "META-INF/native/";
  
  private static final File WORKDIR;
  
  static {
    String workdir = SystemPropertyUtil.get("com.github.steveice10.netty.native.workdir");
    if (workdir != null) {
      File f = new File(workdir);
      f.mkdirs();
      try {
        f = f.getAbsoluteFile();
      } catch (Exception exception) {}
      WORKDIR = f;
      logger.debug("-Dio.netty.native.workdir: " + WORKDIR);
    } else {
      WORKDIR = PlatformDependent.tmpdir();
      logger.debug("-Dio.netty.native.workdir: " + WORKDIR + " (io.netty.tmpdir)");
    } 
  }
  
  private static final boolean DELETE_NATIVE_LIB_AFTER_LOADING = SystemPropertyUtil.getBoolean("com.github.steveice10.netty.native.deleteLibAfterLoading", true);
  
  public static void loadFirstAvailable(ClassLoader loader, String... names) {
    List<Throwable> suppressed = new ArrayList<Throwable>();
    for (String name : names) {
      try {
        load(name, loader);
        return;
      } catch (Throwable t) {
        suppressed.add(t);
        logger.debug("Unable to load the library '{}', trying next name...", name, t);
      } 
    } 
    IllegalArgumentException iae = new IllegalArgumentException("Failed to load any of the given libraries: " + Arrays.toString((Object[])names));
    ThrowableUtil.addSuppressedAndClear(iae, suppressed);
    throw iae;
  }
  
  private static String calculatePackagePrefix() {
    String maybeShaded = NativeLibraryLoader.class.getName();
    String expected = "io!netty!util!internal!NativeLibraryLoader".replace('!', '.');
    if (!maybeShaded.endsWith(expected))
      throw new UnsatisfiedLinkError(String.format("Could not find prefix added to %s to get %s. When shading, only adding a package prefix is supported", new Object[] { expected, maybeShaded })); 
    return maybeShaded.substring(0, maybeShaded.length() - expected.length());
  }
  
  public static void load(String originalName, ClassLoader loader) {
    String name = calculatePackagePrefix().replace('.', '_') + originalName;
    List<Throwable> suppressed = new ArrayList<Throwable>();
    try {
      loadLibrary(loader, name, false);
      return;
    } catch (Throwable ex) {
      URL url;
      suppressed.add(ex);
      logger.debug("{} cannot be loaded from java.libary.path, now trying export to -Dio.netty.native.workdir: {}", new Object[] { name, WORKDIR, ex });
      String libname = System.mapLibraryName(name);
      String path = "META-INF/native/" + libname;
      InputStream in = null;
      OutputStream out = null;
      File tmpFile = null;
      if (loader == null) {
        url = ClassLoader.getSystemResource(path);
      } else {
        url = loader.getResource(path);
      } 
      try {
        if (url == null)
          if (PlatformDependent.isOsx()) {
            String fileName = path.endsWith(".jnilib") ? ("META-INF/native/lib" + name + ".dynlib") : ("META-INF/native/lib" + name + ".jnilib");
            if (loader == null) {
              url = ClassLoader.getSystemResource(fileName);
            } else {
              url = loader.getResource(fileName);
            } 
            if (url == null) {
              FileNotFoundException fnf = new FileNotFoundException(fileName);
              ThrowableUtil.addSuppressedAndClear(fnf, suppressed);
              throw fnf;
            } 
          } else {
            FileNotFoundException fnf = new FileNotFoundException(path);
            ThrowableUtil.addSuppressedAndClear(fnf, suppressed);
            throw fnf;
          }  
        int index = libname.lastIndexOf('.');
        String prefix = libname.substring(0, index);
        String suffix = libname.substring(index, libname.length());
        tmpFile = File.createTempFile(prefix, suffix, WORKDIR);
        in = url.openStream();
        out = new FileOutputStream(tmpFile);
        byte[] buffer = new byte[8192];
        int length;
        while ((length = in.read(buffer)) > 0)
          out.write(buffer, 0, length); 
        out.flush();
        closeQuietly(out);
        out = null;
        loadLibrary(loader, tmpFile.getPath(), true);
      } catch (UnsatisfiedLinkError e) {
        try {
          if (tmpFile != null && tmpFile.isFile() && tmpFile.canRead() && 
            !NoexecVolumeDetector.canExecuteExecutable(tmpFile))
            logger.info("{} exists but cannot be executed even when execute permissions set; check volume for \"noexec\" flag; use -Dio.netty.native.workdir=[path] to set native working directory separately.", tmpFile
                
                .getPath()); 
        } catch (Throwable t) {
          suppressed.add(t);
          logger.debug("Error checking if {} is on a file store mounted with noexec", tmpFile, t);
        } 
        ThrowableUtil.addSuppressedAndClear(e, suppressed);
        throw e;
      } catch (Exception e) {
        UnsatisfiedLinkError ule = new UnsatisfiedLinkError("could not load a native library: " + name);
        ule.initCause(e);
        ThrowableUtil.addSuppressedAndClear(ule, suppressed);
        throw ule;
      } finally {
        closeQuietly(in);
        closeQuietly(out);
        if (tmpFile != null && (!DELETE_NATIVE_LIB_AFTER_LOADING || !tmpFile.delete()))
          tmpFile.deleteOnExit(); 
      } 
      return;
    } 
  }
  
  private static void loadLibrary(ClassLoader loader, String name, boolean absolute) {
    Throwable suppressed = null;
    try {
      Class<?> newHelper = tryToLoadClass(loader, NativeLibraryUtil.class);
      loadLibraryByHelper(newHelper, name, absolute);
      logger.debug("Successfully loaded the library {}", name);
      return;
    } catch (UnsatisfiedLinkError e) {
      suppressed = e;
      logger.debug("Unable to load the library '{}', trying other loading mechanism.", name, e);
      NativeLibraryUtil.loadLibrary(name, absolute);
      logger.debug("Successfully loaded the library {}", name);
    } catch (Exception e) {
      suppressed = e;
      logger.debug("Unable to load the library '{}', trying other loading mechanism.", name, e);
      NativeLibraryUtil.loadLibrary(name, absolute);
      logger.debug("Successfully loaded the library {}", name);
    } catch (UnsatisfiedLinkError ule) {
      if (suppressed != null)
        ThrowableUtil.addSuppressed(ule, suppressed); 
      throw ule;
    } 
  }
  
  private static void loadLibraryByHelper(final Class<?> helper, final String name, final boolean absolute) throws UnsatisfiedLinkError {
    Object ret = AccessController.doPrivileged(new PrivilegedAction() {
          public Object run() {
            try {
              Method method = helper.getMethod("loadLibrary", new Class[] { String.class, boolean.class });
              method.setAccessible(true);
              return method.invoke((Object)null, new Object[] { this.val$name, Boolean.valueOf(this.val$absolute) });
            } catch (Exception e) {
              return e;
            } 
          }
        });
    if (ret instanceof Throwable) {
      Throwable t = (Throwable)ret;
      assert !(t instanceof UnsatisfiedLinkError) : t + " should be a wrapper throwable";
      Throwable cause = t.getCause();
      if (cause instanceof UnsatisfiedLinkError)
        throw (UnsatisfiedLinkError)cause; 
      UnsatisfiedLinkError ule = new UnsatisfiedLinkError(t.getMessage());
      ule.initCause(t);
      throw ule;
    } 
  }
  
  private static Class<?> tryToLoadClass(final ClassLoader loader, final Class<?> helper) throws ClassNotFoundException {
    try {
      return Class.forName(helper.getName(), false, loader);
    } catch (ClassNotFoundException e1) {
      if (loader == null)
        throw e1; 
      try {
        final byte[] classBinary = classToByteArray(helper);
        return AccessController.<Class<?>>doPrivileged(new PrivilegedAction<Class<?>>() {
              public Class<?> run() {
                try {
                  Method defineClass = ClassLoader.class.getDeclaredMethod("defineClass", new Class[] { String.class, byte[].class, int.class, int.class });
                  defineClass.setAccessible(true);
                  return (Class)defineClass.invoke(loader, new Object[] { this.val$helper.getName(), this.val$classBinary, Integer.valueOf(0), 
                        Integer.valueOf(this.val$classBinary.length) });
                } catch (Exception e) {
                  throw new IllegalStateException("Define class failed!", e);
                } 
              }
            });
      } catch (ClassNotFoundException e2) {
        ThrowableUtil.addSuppressed(e2, e1);
        throw e2;
      } catch (RuntimeException e2) {
        ThrowableUtil.addSuppressed(e2, e1);
        throw e2;
      } catch (Error e2) {
        ThrowableUtil.addSuppressed(e2, e1);
        throw e2;
      } 
    } 
  }
  
  private static byte[] classToByteArray(Class<?> clazz) throws ClassNotFoundException {
    String fileName = clazz.getName();
    int lastDot = fileName.lastIndexOf('.');
    if (lastDot > 0)
      fileName = fileName.substring(lastDot + 1); 
    URL classUrl = clazz.getResource(fileName + ".class");
    if (classUrl == null)
      throw new ClassNotFoundException(clazz.getName()); 
    byte[] buf = new byte[1024];
    ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
    InputStream in = null;
    try {
      in = classUrl.openStream();
      int r;
      while ((r = in.read(buf)) != -1)
        out.write(buf, 0, r); 
      return out.toByteArray();
    } catch (IOException ex) {
      throw new ClassNotFoundException(clazz.getName(), ex);
    } finally {
      closeQuietly(in);
      closeQuietly(out);
    } 
  }
  
  private static void closeQuietly(Closeable c) {
    if (c != null)
      try {
        c.close();
      } catch (IOException iOException) {} 
  }
  
  private static final class NoexecVolumeDetector {
    private static boolean canExecuteExecutable(File file) throws IOException {
      if (PlatformDependent.javaVersion() < 7)
        return true; 
      if (file.canExecute())
        return true; 
      Set<PosixFilePermission> existingFilePermissions = Files.getPosixFilePermissions(file.toPath(), new java.nio.file.LinkOption[0]);
      Set<PosixFilePermission> executePermissions = EnumSet.of(PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.GROUP_EXECUTE, PosixFilePermission.OTHERS_EXECUTE);
      if (existingFilePermissions.containsAll(executePermissions))
        return false; 
      Set<PosixFilePermission> newPermissions = EnumSet.copyOf(existingFilePermissions);
      newPermissions.addAll(executePermissions);
      Files.setPosixFilePermissions(file.toPath(), newPermissions);
      return file.canExecute();
    }
  }
}
