package com.sun.jna;

import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Window;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Native implements Version {
  private static final Logger LOG = Logger.getLogger(Native.class.getName());
  
  public static final Charset DEFAULT_CHARSET = Charset.defaultCharset();
  
  public static final String DEFAULT_ENCODING = DEFAULT_CHARSET.name();
  
  public static final boolean DEBUG_LOAD = Boolean.getBoolean("jna.debug_load");
  
  public static final boolean DEBUG_JNA_LOAD = Boolean.getBoolean("jna.debug_load.jna");
  
  private static final Level DEBUG_JNA_LOAD_LEVEL = DEBUG_JNA_LOAD ? Level.INFO : Level.FINE;
  
  static String jnidispatchPath = null;
  
  private static final Map<Class<?>, Map<String, Object>> typeOptions = Collections.synchronizedMap(new WeakHashMap<Class<?>, Map<String, Object>>());
  
  private static final Map<Class<?>, Reference<?>> libraries = Collections.synchronizedMap(new WeakHashMap<Class<?>, Reference<?>>());
  
  private static final String _OPTION_ENCLOSING_LIBRARY = "enclosing-library";
  
  private static final Callback.UncaughtExceptionHandler DEFAULT_HANDLER = new Callback.UncaughtExceptionHandler() {
      public void uncaughtException(Callback c, Throwable e) {
        Native.LOG.log(Level.WARNING, "JNA: Callback " + c + " threw the following exception", e);
      }
    };
  
  private static Callback.UncaughtExceptionHandler callbackExceptionHandler = DEFAULT_HANDLER;
  
  static boolean isCompatibleVersion(String expectedVersion, String nativeVersion) {
    String[] expectedVersionParts = expectedVersion.split("\\.");
    String[] nativeVersionParts = nativeVersion.split("\\.");
    if (expectedVersionParts.length < 3 || nativeVersionParts.length < 3)
      return false; 
    int expectedMajor = Integer.parseInt(expectedVersionParts[0]);
    int nativeMajor = Integer.parseInt(nativeVersionParts[0]);
    int expectedMinor = Integer.parseInt(expectedVersionParts[1]);
    int nativeMinor = Integer.parseInt(nativeVersionParts[1]);
    if (expectedMajor != nativeMajor)
      return false; 
    if (expectedMinor > nativeMinor)
      return false; 
    return true;
  }
  
  static {
    loadNativeDispatchLibrary();
    if (!isCompatibleVersion("6.1.0", getNativeVersion())) {
      String LS = System.getProperty("line.separator");
      throw new Error(LS + LS + "There is an incompatible JNA native library installed on this system" + LS + "Expected: " + "6.1.0" + LS + "Found:    " + 
          
          getNativeVersion() + LS + ((jnidispatchPath != null) ? ("(at " + jnidispatchPath + ")") : 
          
          System.getProperty("java.library.path")) + "." + LS + "To resolve this issue you may do one of the following:" + LS + " - remove or uninstall the offending library" + LS + " - set the system property jna.nosys=true" + LS + " - set jna.boot.library.path to include the path to the version of the " + LS + "   jnidispatch library included with the JNA jar file you are using" + LS);
    } 
  }
  
  public static final int POINTER_SIZE = sizeof(0);
  
  public static final int LONG_SIZE = sizeof(1);
  
  public static final int WCHAR_SIZE = sizeof(2);
  
  public static final int SIZE_T_SIZE = sizeof(3);
  
  public static final int BOOL_SIZE = sizeof(4);
  
  public static final int LONG_DOUBLE_SIZE = sizeof(5);
  
  private static final int TYPE_VOIDP = 0;
  
  private static final int TYPE_LONG = 1;
  
  private static final int TYPE_WCHAR_T = 2;
  
  private static final int TYPE_SIZE_T = 3;
  
  private static final int TYPE_BOOL = 4;
  
  private static final int TYPE_LONG_DOUBLE = 5;
  
  static {
    initIDs();
    if (Boolean.getBoolean("jna.protected"))
      setProtected(true); 
  }
  
  static final int MAX_ALIGNMENT = (Platform.isSPARC() || Platform.isWindows() || (
    Platform.isLinux() && (Platform.isARM() || Platform.isPPC() || Platform.isMIPS())) || 
    Platform.isAIX() || (
    Platform.isAndroid() && !Platform.isIntel())) ? 8 : LONG_SIZE;
  
  static final int MAX_PADDING = (Platform.isMac() && Platform.isPPC()) ? 8 : MAX_ALIGNMENT;
  
  static {
    System.setProperty("jna.loaded", "true");
  }
  
  private static final Object finalizer = new Object() {
      protected void finalize() throws Throwable {
        Native.dispose();
        super.finalize();
      }
    };
  
  static final String JNA_TMPLIB_PREFIX = "jna";
  
  private static void dispose() {
    CallbackReference.disposeAll();
    Memory.disposeAll();
    NativeLibrary.disposeAll();
    unregisterAll();
    jnidispatchPath = null;
    System.setProperty("jna.loaded", "false");
  }
  
  static boolean deleteLibrary(File lib) {
    if (lib.delete())
      return true; 
    markTemporaryFile(lib);
    return false;
  }
  
  public static long getWindowID(Window w) throws HeadlessException {
    return AWT.getWindowID(w);
  }
  
  public static long getComponentID(Component c) throws HeadlessException {
    return AWT.getComponentID(c);
  }
  
  public static Pointer getWindowPointer(Window w) throws HeadlessException {
    return new Pointer(AWT.getWindowID(w));
  }
  
  public static Pointer getComponentPointer(Component c) throws HeadlessException {
    return new Pointer(AWT.getComponentID(c));
  }
  
  public static Pointer getDirectBufferPointer(Buffer b) {
    long peer = _getDirectBufferPointer(b);
    return (peer == 0L) ? null : new Pointer(peer);
  }
  
  private static Charset getCharset(String encoding) {
    Charset charset = null;
    if (encoding != null)
      try {
        charset = Charset.forName(encoding);
      } catch (IllegalCharsetNameException e) {
        LOG.log(Level.WARNING, "JNA Warning: Encoding ''{0}'' is unsupported ({1})", new Object[] { encoding, e
              .getMessage() });
      } catch (UnsupportedCharsetException e) {
        LOG.log(Level.WARNING, "JNA Warning: Encoding ''{0}'' is unsupported ({1})", new Object[] { encoding, e
              .getMessage() });
      }  
    if (charset == null) {
      LOG.log(Level.WARNING, "JNA Warning: Using fallback encoding {0}", DEFAULT_CHARSET);
      charset = DEFAULT_CHARSET;
    } 
    return charset;
  }
  
  public static String toString(byte[] buf) {
    return toString(buf, getDefaultStringEncoding());
  }
  
  public static String toString(byte[] buf, String encoding) {
    return toString(buf, getCharset(encoding));
  }
  
  public static String toString(byte[] buf, Charset charset) {
    int len = buf.length;
    for (int index = 0; index < len; index++) {
      if (buf[index] == 0) {
        len = index;
        break;
      } 
    } 
    if (len == 0)
      return ""; 
    return new String(buf, 0, len, charset);
  }
  
  public static String toString(char[] buf) {
    int len = buf.length;
    for (int index = 0; index < len; index++) {
      if (buf[index] == '\000') {
        len = index;
        break;
      } 
    } 
    if (len == 0)
      return ""; 
    return new String(buf, 0, len);
  }
  
  public static List<String> toStringList(char[] buf) {
    return toStringList(buf, 0, buf.length);
  }
  
  public static List<String> toStringList(char[] buf, int offset, int len) {
    List<String> list = new ArrayList<String>();
    int lastPos = offset;
    int maxPos = offset + len;
    for (int curPos = offset; curPos < maxPos; curPos++) {
      if (buf[curPos] == '\000') {
        if (lastPos == curPos)
          return list; 
        String value = new String(buf, lastPos, curPos - lastPos);
        list.add(value);
        lastPos = curPos + 1;
      } 
    } 
    if (lastPos < maxPos) {
      String value = new String(buf, lastPos, maxPos - lastPos);
      list.add(value);
    } 
    return list;
  }
  
  public static <T extends Library> T load(Class<T> interfaceClass) {
    return load((String)null, interfaceClass);
  }
  
  public static <T extends Library> T load(Class<T> interfaceClass, Map<String, ?> options) {
    return load(null, interfaceClass, options);
  }
  
  public static <T extends Library> T load(String name, Class<T> interfaceClass) {
    return load(name, interfaceClass, Collections.emptyMap());
  }
  
  public static <T extends Library> T load(String name, Class<T> interfaceClass, Map<String, ?> options) {
    if (!Library.class.isAssignableFrom(interfaceClass))
      throw new IllegalArgumentException("Interface (" + interfaceClass.getSimpleName() + ") of library=" + name + " does not extend " + Library.class
          .getSimpleName()); 
    Library.Handler handler = new Library.Handler(name, interfaceClass, options);
    ClassLoader loader = interfaceClass.getClassLoader();
    Object proxy = Proxy.newProxyInstance(loader, new Class[] { interfaceClass }, handler);
    cacheOptions(interfaceClass, options, proxy);
    return interfaceClass.cast(proxy);
  }
  
  @Deprecated
  public static <T> T loadLibrary(Class<T> interfaceClass) {
    return loadLibrary((String)null, interfaceClass);
  }
  
  @Deprecated
  public static <T> T loadLibrary(Class<T> interfaceClass, Map<String, ?> options) {
    return loadLibrary(null, interfaceClass, options);
  }
  
  @Deprecated
  public static <T> T loadLibrary(String name, Class<T> interfaceClass) {
    return loadLibrary(name, interfaceClass, Collections.emptyMap());
  }
  
  @Deprecated
  public static <T> T loadLibrary(String name, Class<T> interfaceClass, Map<String, ?> options) {
    if (!Library.class.isAssignableFrom(interfaceClass))
      throw new IllegalArgumentException("Interface (" + interfaceClass.getSimpleName() + ") of library=" + name + " does not extend " + Library.class
          .getSimpleName()); 
    Library.Handler handler = new Library.Handler(name, interfaceClass, options);
    ClassLoader loader = interfaceClass.getClassLoader();
    Object proxy = Proxy.newProxyInstance(loader, new Class[] { interfaceClass }, handler);
    cacheOptions(interfaceClass, options, proxy);
    return interfaceClass.cast(proxy);
  }
  
  private static void loadLibraryInstance(Class<?> cls) {
    if (cls != null && !libraries.containsKey(cls))
      try {
        Field[] fields = cls.getFields();
        for (int i = 0; i < fields.length; i++) {
          Field field = fields[i];
          if (field.getType() == cls && 
            Modifier.isStatic(field.getModifiers())) {
            libraries.put(cls, new WeakReference(field.get((Object)null)));
            break;
          } 
        } 
      } catch (Exception e) {
        throw new IllegalArgumentException("Could not access instance of " + cls + " (" + e + ")");
      }  
  }
  
  static Class<?> findEnclosingLibraryClass(Class<?> cls) {
    if (cls == null)
      return null; 
    Map<String, ?> libOptions = typeOptions.get(cls);
    if (libOptions != null) {
      Class<?> enclosingClass = (Class)libOptions.get("enclosing-library");
      if (enclosingClass != null)
        return enclosingClass; 
      return cls;
    } 
    if (Library.class.isAssignableFrom(cls))
      return cls; 
    if (Callback.class.isAssignableFrom(cls))
      cls = CallbackReference.findCallbackClass(cls); 
    Class<?> declaring = cls.getDeclaringClass();
    Class<?> fromDeclaring = findEnclosingLibraryClass(declaring);
    if (fromDeclaring != null)
      return fromDeclaring; 
    return findEnclosingLibraryClass(cls.getSuperclass());
  }
  
  public static Map<String, Object> getLibraryOptions(Class<?> type) {
    Map<String, Object> libraryOptions = typeOptions.get(type);
    if (libraryOptions != null)
      return libraryOptions; 
    Class<?> mappingClass = findEnclosingLibraryClass(type);
    if (mappingClass != null) {
      loadLibraryInstance(mappingClass);
    } else {
      mappingClass = type;
    } 
    libraryOptions = typeOptions.get(mappingClass);
    if (libraryOptions != null) {
      typeOptions.put(type, libraryOptions);
      return libraryOptions;
    } 
    try {
      Field field = mappingClass.getField("OPTIONS");
      field.setAccessible(true);
      libraryOptions = (Map<String, Object>)field.get((Object)null);
      if (libraryOptions == null)
        throw new IllegalStateException("Null options field"); 
    } catch (NoSuchFieldException e) {
      libraryOptions = Collections.emptyMap();
    } catch (Exception e) {
      throw new IllegalArgumentException("OPTIONS must be a public field of type java.util.Map (" + e + "): " + mappingClass);
    } 
    libraryOptions = new HashMap<String, Object>(libraryOptions);
    if (!libraryOptions.containsKey("type-mapper"))
      libraryOptions.put("type-mapper", lookupField(mappingClass, "TYPE_MAPPER", TypeMapper.class)); 
    if (!libraryOptions.containsKey("structure-alignment"))
      libraryOptions.put("structure-alignment", lookupField(mappingClass, "STRUCTURE_ALIGNMENT", Integer.class)); 
    if (!libraryOptions.containsKey("string-encoding"))
      libraryOptions.put("string-encoding", lookupField(mappingClass, "STRING_ENCODING", String.class)); 
    libraryOptions = cacheOptions(mappingClass, libraryOptions, null);
    if (type != mappingClass)
      typeOptions.put(type, libraryOptions); 
    return libraryOptions;
  }
  
  private static Object lookupField(Class<?> mappingClass, String fieldName, Class<?> resultClass) {
    try {
      Field field = mappingClass.getField(fieldName);
      field.setAccessible(true);
      return field.get((Object)null);
    } catch (NoSuchFieldException e) {
      return null;
    } catch (Exception e) {
      throw new IllegalArgumentException(fieldName + " must be a public field of type " + resultClass
          .getName() + " (" + e + "): " + mappingClass);
    } 
  }
  
  public static TypeMapper getTypeMapper(Class<?> cls) {
    Map<String, ?> options = getLibraryOptions(cls);
    return (TypeMapper)options.get("type-mapper");
  }
  
  public static String getStringEncoding(Class<?> cls) {
    Map<String, ?> options = getLibraryOptions(cls);
    String encoding = (String)options.get("string-encoding");
    return (encoding != null) ? encoding : getDefaultStringEncoding();
  }
  
  public static String getDefaultStringEncoding() {
    return System.getProperty("jna.encoding", DEFAULT_ENCODING);
  }
  
  public static int getStructureAlignment(Class<?> cls) {
    Integer alignment = (Integer)getLibraryOptions(cls).get("structure-alignment");
    return (alignment == null) ? 0 : alignment.intValue();
  }
  
  static byte[] getBytes(String s) {
    return getBytes(s, getDefaultStringEncoding());
  }
  
  static byte[] getBytes(String s, String encoding) {
    return getBytes(s, getCharset(encoding));
  }
  
  static byte[] getBytes(String s, Charset charset) {
    return s.getBytes(charset);
  }
  
  public static byte[] toByteArray(String s) {
    return toByteArray(s, getDefaultStringEncoding());
  }
  
  public static byte[] toByteArray(String s, String encoding) {
    return toByteArray(s, getCharset(encoding));
  }
  
  public static byte[] toByteArray(String s, Charset charset) {
    byte[] bytes = getBytes(s, charset);
    byte[] buf = new byte[bytes.length + 1];
    System.arraycopy(bytes, 0, buf, 0, bytes.length);
    return buf;
  }
  
  public static char[] toCharArray(String s) {
    char[] chars = s.toCharArray();
    char[] buf = new char[chars.length + 1];
    System.arraycopy(chars, 0, buf, 0, chars.length);
    return buf;
  }
  
  private static void loadNativeDispatchLibrary() {
    if (!Boolean.getBoolean("jna.nounpack"))
      try {
        removeTemporaryFiles();
      } catch (IOException e) {
        LOG.log(Level.WARNING, "JNA Warning: IOException removing temporary files", e);
      }  
    String libName = System.getProperty("jna.boot.library.name", "jnidispatch");
    String bootPath = System.getProperty("jna.boot.library.path");
    if (bootPath != null) {
      StringTokenizer dirs = new StringTokenizer(bootPath, File.pathSeparator);
      while (dirs.hasMoreTokens()) {
        String dir = dirs.nextToken();
        File file = new File(new File(dir), System.mapLibraryName(libName).replace(".dylib", ".jnilib"));
        String path = file.getAbsolutePath();
        LOG.log(DEBUG_JNA_LOAD_LEVEL, "Looking in {0}", path);
        if (file.exists())
          try {
            LOG.log(DEBUG_JNA_LOAD_LEVEL, "Trying {0}", path);
            System.setProperty("jnidispatch.path", path);
            System.load(path);
            jnidispatchPath = path;
            LOG.log(DEBUG_JNA_LOAD_LEVEL, "Found jnidispatch at {0}", path);
            return;
          } catch (UnsatisfiedLinkError unsatisfiedLinkError) {} 
        if (Platform.isMac()) {
          String orig, ext;
          if (path.endsWith("dylib")) {
            orig = "dylib";
            ext = "jnilib";
          } else {
            orig = "jnilib";
            ext = "dylib";
          } 
          path = path.substring(0, path.lastIndexOf(orig)) + ext;
          LOG.log(DEBUG_JNA_LOAD_LEVEL, "Looking in {0}", path);
          if ((new File(path)).exists())
            try {
              LOG.log(DEBUG_JNA_LOAD_LEVEL, "Trying {0}", path);
              System.setProperty("jnidispatch.path", path);
              System.load(path);
              jnidispatchPath = path;
              LOG.log(DEBUG_JNA_LOAD_LEVEL, "Found jnidispatch at {0}", path);
              return;
            } catch (UnsatisfiedLinkError ex) {
              LOG.log(Level.WARNING, "File found at " + path + " but not loadable: " + ex.getMessage(), ex);
            }  
        } 
      } 
    } 
    String jnaNosys = System.getProperty("jna.nosys", "true");
    if (!Boolean.parseBoolean(jnaNosys) || Platform.isAndroid())
      try {
        LOG.log(DEBUG_JNA_LOAD_LEVEL, "Trying (via loadLibrary) {0}", libName);
        System.loadLibrary(libName);
        LOG.log(DEBUG_JNA_LOAD_LEVEL, "Found jnidispatch on system path");
        return;
      } catch (UnsatisfiedLinkError unsatisfiedLinkError) {} 
    if (!Boolean.getBoolean("jna.noclasspath")) {
      loadNativeDispatchLibraryFromClasspath();
    } else {
      throw new UnsatisfiedLinkError("Unable to locate JNA native support library");
    } 
  }
  
  private static void loadNativeDispatchLibraryFromClasspath() {
    try {
      String mappedName = System.mapLibraryName("jnidispatch").replace(".dylib", ".jnilib");
      if (Platform.isAIX())
        mappedName = "libjnidispatch.a"; 
      String libName = "/com/sun/jna/" + Platform.RESOURCE_PREFIX + "/" + mappedName;
      File lib = extractFromResourcePath(libName, Native.class.getClassLoader());
      if (lib == null && 
        lib == null)
        throw new UnsatisfiedLinkError("Could not find JNA native support"); 
      LOG.log(DEBUG_JNA_LOAD_LEVEL, "Trying {0}", lib.getAbsolutePath());
      System.setProperty("jnidispatch.path", lib.getAbsolutePath());
      System.load(lib.getAbsolutePath());
      jnidispatchPath = lib.getAbsolutePath();
      LOG.log(DEBUG_JNA_LOAD_LEVEL, "Found jnidispatch at {0}", jnidispatchPath);
      if (isUnpacked(lib) && 
        !Boolean.getBoolean("jnidispatch.preserve"))
        deleteLibrary(lib); 
    } catch (IOException e) {
      throw new UnsatisfiedLinkError(e.getMessage());
    } 
  }
  
  static boolean isUnpacked(File file) {
    return file.getName().startsWith("jna");
  }
  
  public static File extractFromResourcePath(String name) throws IOException {
    return extractFromResourcePath(name, null);
  }
  
  public static File extractFromResourcePath(String name, ClassLoader loader) throws IOException {
    Level DEBUG = (DEBUG_LOAD || (DEBUG_JNA_LOAD && name.contains("jnidispatch"))) ? Level.INFO : Level.FINE;
    if (loader == null) {
      loader = Thread.currentThread().getContextClassLoader();
      if (loader == null)
        loader = Native.class.getClassLoader(); 
    } 
    LOG.log(DEBUG, "Looking in classpath from {0} for {1}", new Object[] { loader, name });
    String libname = name.startsWith("/") ? name : NativeLibrary.mapSharedLibraryName(name);
    String resourcePath = name.startsWith("/") ? name : (Platform.RESOURCE_PREFIX + "/" + libname);
    if (resourcePath.startsWith("/"))
      resourcePath = resourcePath.substring(1); 
    URL url = loader.getResource(resourcePath);
    if (url == null && resourcePath.startsWith(Platform.RESOURCE_PREFIX))
      url = loader.getResource(libname); 
    if (url == null) {
      String path = System.getProperty("java.class.path");
      if (loader instanceof URLClassLoader)
        path = Arrays.<URL>asList(((URLClassLoader)loader).getURLs()).toString(); 
      throw new IOException("Native library (" + resourcePath + ") not found in resource path (" + path + ")");
    } 
    LOG.log(DEBUG, "Found library resource at {0}", url);
    File lib = null;
    if (url.getProtocol().toLowerCase().equals("file")) {
      try {
        lib = new File(new URI(url.toString()));
      } catch (URISyntaxException e) {
        lib = new File(url.getPath());
      } 
      LOG.log(DEBUG, "Looking in {0}", lib.getAbsolutePath());
      if (!lib.exists())
        throw new IOException("File URL " + url + " could not be properly decoded"); 
    } else if (!Boolean.getBoolean("jna.nounpack")) {
      InputStream is = loader.getResourceAsStream(resourcePath);
      if (is == null)
        throw new IOException("Can't obtain InputStream for " + resourcePath); 
      FileOutputStream fos = null;
      try {
        File dir = getTempDir();
        lib = File.createTempFile("jna", Platform.isWindows() ? ".dll" : null, dir);
        if (!Boolean.getBoolean("jnidispatch.preserve"))
          lib.deleteOnExit(); 
        LOG.log(DEBUG, "Extracting library to {0}", lib.getAbsolutePath());
        fos = new FileOutputStream(lib);
        byte[] buf = new byte[1024];
        int count;
        while ((count = is.read(buf, 0, buf.length)) > 0)
          fos.write(buf, 0, count); 
      } catch (IOException e) {
        throw new IOException("Failed to create temporary file for " + name + " library: " + e.getMessage());
      } finally {
        try {
          is.close();
        } catch (IOException iOException) {}
        if (fos != null)
          try {
            fos.close();
          } catch (IOException iOException) {} 
      } 
    } 
    return lib;
  }
  
  public static Library synchronizedLibrary(final Library library) {
    Class<?> cls = library.getClass();
    if (!Proxy.isProxyClass(cls))
      throw new IllegalArgumentException("Library must be a proxy class"); 
    InvocationHandler ih = Proxy.getInvocationHandler(library);
    if (!(ih instanceof Library.Handler))
      throw new IllegalArgumentException("Unrecognized proxy handler: " + ih); 
    final Library.Handler handler = (Library.Handler)ih;
    InvocationHandler newHandler = new InvocationHandler() {
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
          synchronized (handler.getNativeLibrary()) {
            return handler.invoke(library, method, args);
          } 
        }
      };
    return (Library)Proxy.newProxyInstance(cls.getClassLoader(), cls
        .getInterfaces(), newHandler);
  }
  
  public static String getWebStartLibraryPath(String libName) {
    if (System.getProperty("javawebstart.version") == null)
      return null; 
    try {
      ClassLoader cl = Native.class.getClassLoader();
      Method m = AccessController.<Method>doPrivileged(new PrivilegedAction<Method>() {
            public Method run() {
              try {
                Method m = ClassLoader.class.getDeclaredMethod("findLibrary", new Class[] { String.class });
                m.setAccessible(true);
                return m;
              } catch (Exception e) {
                return null;
              } 
            }
          });
      String libpath = (String)m.invoke(cl, new Object[] { libName });
      if (libpath != null)
        return (new File(libpath)).getParent(); 
      return null;
    } catch (Exception e) {
      return null;
    } 
  }
  
  static void markTemporaryFile(File file) {
    try {
      File marker = new File(file.getParentFile(), file.getName() + ".x");
      marker.createNewFile();
    } catch (IOException e) {
      e.printStackTrace();
    } 
  }
  
  static File getTempDir() throws IOException {
    File jnatmp;
    String prop = System.getProperty("jna.tmpdir");
    if (prop != null) {
      jnatmp = new File(prop);
      jnatmp.mkdirs();
    } else {
      File tmp = new File(System.getProperty("java.io.tmpdir"));
      if (Platform.isMac()) {
        jnatmp = new File(System.getProperty("user.home"), "Library/Caches/JNA/temp");
      } else if (Platform.isLinux() || Platform.isSolaris() || Platform.isAIX() || Platform.isFreeBSD() || Platform.isNetBSD() || Platform.isOpenBSD() || Platform.iskFreeBSD()) {
        File xdgCacheFile;
        String xdgCacheEnvironment = System.getenv("XDG_CACHE_HOME");
        if (xdgCacheEnvironment == null || xdgCacheEnvironment.trim().isEmpty()) {
          xdgCacheFile = new File(System.getProperty("user.home"), ".cache");
        } else {
          xdgCacheFile = new File(xdgCacheEnvironment);
        } 
        jnatmp = new File(xdgCacheFile, "JNA/temp");
      } else {
        jnatmp = new File(tmp, "jna-" + System.getProperty("user.name").hashCode());
      } 
      jnatmp.mkdirs();
      if (!jnatmp.exists() || !jnatmp.canWrite())
        jnatmp = tmp; 
    } 
    if (!jnatmp.exists())
      throw new IOException("JNA temporary directory '" + jnatmp + "' does not exist"); 
    if (!jnatmp.canWrite())
      throw new IOException("JNA temporary directory '" + jnatmp + "' is not writable"); 
    return jnatmp;
  }
  
  static void removeTemporaryFiles() throws IOException {
    File dir = getTempDir();
    FilenameFilter filter = new FilenameFilter() {
        public boolean accept(File dir, String name) {
          return (name.endsWith(".x") && name.startsWith("jna"));
        }
      };
    File[] files = dir.listFiles(filter);
    for (int i = 0; files != null && i < files.length; i++) {
      File marker = files[i];
      String name = marker.getName();
      name = name.substring(0, name.length() - 2);
      File target = new File(marker.getParentFile(), name);
      if (!target.exists() || target.delete())
        marker.delete(); 
    } 
  }
  
  public static int getNativeSize(Class<?> type, Object value) {
    if (type.isArray()) {
      int len = Array.getLength(value);
      if (len > 0) {
        Object o = Array.get(value, 0);
        return len * getNativeSize(type.getComponentType(), o);
      } 
      throw new IllegalArgumentException("Arrays of length zero not allowed: " + type);
    } 
    if (Structure.class.isAssignableFrom(type) && 
      !Structure.ByReference.class.isAssignableFrom(type))
      return Structure.size(type, value); 
    try {
      return getNativeSize(type);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("The type \"" + type.getName() + "\" is not supported: " + e
          
          .getMessage());
    } 
  }
  
  public static int getNativeSize(Class<?> cls) {
    if (NativeMapped.class.isAssignableFrom(cls))
      cls = NativeMappedConverter.getInstance(cls).nativeType(); 
    if (cls == boolean.class || cls == Boolean.class)
      return 4; 
    if (cls == byte.class || cls == Byte.class)
      return 1; 
    if (cls == short.class || cls == Short.class)
      return 2; 
    if (cls == char.class || cls == Character.class)
      return WCHAR_SIZE; 
    if (cls == int.class || cls == Integer.class)
      return 4; 
    if (cls == long.class || cls == Long.class)
      return 8; 
    if (cls == float.class || cls == Float.class)
      return 4; 
    if (cls == double.class || cls == Double.class)
      return 8; 
    if (Structure.class.isAssignableFrom(cls)) {
      if (Structure.ByValue.class.isAssignableFrom(cls))
        return Structure.size((Class)cls); 
      return POINTER_SIZE;
    } 
    if (Pointer.class.isAssignableFrom(cls) || (Platform.HAS_BUFFERS && 
      Buffers.isBuffer(cls)) || Callback.class
      .isAssignableFrom(cls) || String.class == cls || WString.class == cls)
      return POINTER_SIZE; 
    throw new IllegalArgumentException("Native size for type \"" + cls.getName() + "\" is unknown");
  }
  
  public static boolean isSupportedNativeType(Class<?> cls) {
    if (Structure.class.isAssignableFrom(cls))
      return true; 
    try {
      return (getNativeSize(cls) != 0);
    } catch (IllegalArgumentException e) {
      return false;
    } 
  }
  
  public static void setCallbackExceptionHandler(Callback.UncaughtExceptionHandler eh) {
    callbackExceptionHandler = (eh == null) ? DEFAULT_HANDLER : eh;
  }
  
  public static Callback.UncaughtExceptionHandler getCallbackExceptionHandler() {
    return callbackExceptionHandler;
  }
  
  public static void register(String libName) {
    register(findDirectMappedClass(getCallingClass()), libName);
  }
  
  public static void register(NativeLibrary lib) {
    register(findDirectMappedClass(getCallingClass()), lib);
  }
  
  static Class<?> findDirectMappedClass(Class<?> cls) {
    Method[] methods = cls.getDeclaredMethods();
    for (Method m : methods) {
      if ((m.getModifiers() & 0x100) != 0)
        return cls; 
    } 
    int idx = cls.getName().lastIndexOf("$");
    if (idx != -1) {
      String name = cls.getName().substring(0, idx);
      try {
        return findDirectMappedClass(Class.forName(name, true, cls.getClassLoader()));
      } catch (ClassNotFoundException classNotFoundException) {}
    } 
    throw new IllegalArgumentException("Can't determine class with native methods from the current context (" + cls + ")");
  }
  
  static Class<?> getCallingClass() {
    Class<?>[] context = (new SecurityManager() {
        public Class<?>[] getClassContext() {
          return super.getClassContext();
        }
      }).getClassContext();
    if (context == null)
      throw new IllegalStateException("The SecurityManager implementation on this platform is broken; you must explicitly provide the class to register"); 
    if (context.length < 4)
      throw new IllegalStateException("This method must be called from the static initializer of a class"); 
    return context[3];
  }
  
  public static void setCallbackThreadInitializer(Callback cb, CallbackThreadInitializer initializer) {
    CallbackReference.setCallbackThreadInitializer(cb, initializer);
  }
  
  private static final Map<Class<?>, long[]> registeredClasses = (Map)new WeakHashMap<Class<?>, long>();
  
  private static final Map<Class<?>, NativeLibrary> registeredLibraries = new WeakHashMap<Class<?>, NativeLibrary>();
  
  static final int CB_HAS_INITIALIZER = 1;
  
  private static final int CVT_UNSUPPORTED = -1;
  
  private static final int CVT_DEFAULT = 0;
  
  private static final int CVT_POINTER = 1;
  
  private static final int CVT_STRING = 2;
  
  private static final int CVT_STRUCTURE = 3;
  
  private static final int CVT_STRUCTURE_BYVAL = 4;
  
  private static final int CVT_BUFFER = 5;
  
  private static final int CVT_ARRAY_BYTE = 6;
  
  private static final int CVT_ARRAY_SHORT = 7;
  
  private static final int CVT_ARRAY_CHAR = 8;
  
  private static final int CVT_ARRAY_INT = 9;
  
  private static final int CVT_ARRAY_LONG = 10;
  
  private static final int CVT_ARRAY_FLOAT = 11;
  
  private static final int CVT_ARRAY_DOUBLE = 12;
  
  private static final int CVT_ARRAY_BOOLEAN = 13;
  
  private static final int CVT_BOOLEAN = 14;
  
  private static final int CVT_CALLBACK = 15;
  
  private static final int CVT_FLOAT = 16;
  
  private static final int CVT_NATIVE_MAPPED = 17;
  
  private static final int CVT_NATIVE_MAPPED_STRING = 18;
  
  private static final int CVT_NATIVE_MAPPED_WSTRING = 19;
  
  private static final int CVT_WSTRING = 20;
  
  private static final int CVT_INTEGER_TYPE = 21;
  
  private static final int CVT_POINTER_TYPE = 22;
  
  private static final int CVT_TYPE_MAPPER = 23;
  
  private static final int CVT_TYPE_MAPPER_STRING = 24;
  
  private static final int CVT_TYPE_MAPPER_WSTRING = 25;
  
  private static final int CVT_OBJECT = 26;
  
  private static final int CVT_JNIENV = 27;
  
  static final int CB_OPTION_DIRECT = 1;
  
  static final int CB_OPTION_IN_DLL = 2;
  
  private static void unregisterAll() {
    synchronized (registeredClasses) {
      for (Map.Entry<Class<?>, long[]> e : registeredClasses.entrySet())
        unregister(e.getKey(), e.getValue()); 
      registeredClasses.clear();
    } 
  }
  
  public static void unregister() {
    unregister(findDirectMappedClass(getCallingClass()));
  }
  
  public static void unregister(Class<?> cls) {
    synchronized (registeredClasses) {
      long[] handles = registeredClasses.get(cls);
      if (handles != null) {
        unregister(cls, handles);
        registeredClasses.remove(cls);
        registeredLibraries.remove(cls);
      } 
    } 
  }
  
  public static boolean registered(Class<?> cls) {
    synchronized (registeredClasses) {
      return registeredClasses.containsKey(cls);
    } 
  }
  
  static String getSignature(Class<?> cls) {
    if (cls.isArray())
      return "[" + getSignature(cls.getComponentType()); 
    if (cls.isPrimitive()) {
      if (cls == void.class)
        return "V"; 
      if (cls == boolean.class)
        return "Z"; 
      if (cls == byte.class)
        return "B"; 
      if (cls == short.class)
        return "S"; 
      if (cls == char.class)
        return "C"; 
      if (cls == int.class)
        return "I"; 
      if (cls == long.class)
        return "J"; 
      if (cls == float.class)
        return "F"; 
      if (cls == double.class)
        return "D"; 
    } 
    return "L" + replace(".", "/", cls.getName()) + ";";
  }
  
  static String replace(String s1, String s2, String str) {
    StringBuilder buf = new StringBuilder();
    while (true) {
      int idx = str.indexOf(s1);
      if (idx == -1) {
        buf.append(str);
        break;
      } 
      buf.append(str.substring(0, idx));
      buf.append(s2);
      str = str.substring(idx + s1.length());
    } 
    return buf.toString();
  }
  
  private static int getConversion(Class<?> type, TypeMapper mapper, boolean allowObjects) {
    if (type == Void.class)
      type = void.class; 
    if (mapper != null) {
      FromNativeConverter fromNative = mapper.getFromNativeConverter(type);
      ToNativeConverter toNative = mapper.getToNativeConverter(type);
      if (fromNative != null) {
        Class<?> nativeType = fromNative.nativeType();
        if (nativeType == String.class)
          return 24; 
        if (nativeType == WString.class)
          return 25; 
        return 23;
      } 
      if (toNative != null) {
        Class<?> nativeType = toNative.nativeType();
        if (nativeType == String.class)
          return 24; 
        if (nativeType == WString.class)
          return 25; 
        return 23;
      } 
    } 
    if (Pointer.class.isAssignableFrom(type))
      return 1; 
    if (String.class == type)
      return 2; 
    if (WString.class.isAssignableFrom(type))
      return 20; 
    if (Platform.HAS_BUFFERS && Buffers.isBuffer(type))
      return 5; 
    if (Structure.class.isAssignableFrom(type)) {
      if (Structure.ByValue.class.isAssignableFrom(type))
        return 4; 
      return 3;
    } 
    if (type.isArray())
      switch (type.getName().charAt(1)) {
        case 'Z':
          return 13;
        case 'B':
          return 6;
        case 'S':
          return 7;
        case 'C':
          return 8;
        case 'I':
          return 9;
        case 'J':
          return 10;
        case 'F':
          return 11;
        case 'D':
          return 12;
      }  
    if (type.isPrimitive())
      return (type == boolean.class) ? 14 : 0; 
    if (Callback.class.isAssignableFrom(type))
      return 15; 
    if (IntegerType.class.isAssignableFrom(type))
      return 21; 
    if (PointerType.class.isAssignableFrom(type))
      return 22; 
    if (NativeMapped.class.isAssignableFrom(type)) {
      Class<?> nativeType = NativeMappedConverter.getInstance(type).nativeType();
      if (nativeType == String.class)
        return 18; 
      if (nativeType == WString.class)
        return 19; 
      return 17;
    } 
    if (JNIEnv.class == type)
      return 27; 
    return allowObjects ? 26 : -1;
  }
  
  public static void register(Class<?> cls, String libName) {
    NativeLibrary library = NativeLibrary.getInstance(libName, Collections.singletonMap("classloader", cls.getClassLoader()));
    register(cls, library);
  }
  
  public static void register(Class<?> cls, NativeLibrary lib) {
    Method[] methods = cls.getDeclaredMethods();
    List<Method> mlist = new ArrayList<Method>();
    Map<String, ?> options = lib.getOptions();
    TypeMapper mapper = (TypeMapper)options.get("type-mapper");
    boolean allowObjects = Boolean.TRUE.equals(options.get("allow-objects"));
    options = cacheOptions(cls, options, null);
    for (Method m : methods) {
      if ((m.getModifiers() & 0x100) != 0)
        mlist.add(m); 
    } 
    long[] handles = new long[mlist.size()];
    for (int i = 0; i < handles.length; i++) {
      long rtype, closure_rtype;
      Method method = mlist.get(i);
      String sig = "(";
      Class<?> rclass = method.getReturnType();
      Class<?>[] ptypes = method.getParameterTypes();
      long[] atypes = new long[ptypes.length];
      long[] closure_atypes = new long[ptypes.length];
      int[] cvt = new int[ptypes.length];
      ToNativeConverter[] toNative = new ToNativeConverter[ptypes.length];
      FromNativeConverter fromNative = null;
      int rcvt = getConversion(rclass, mapper, allowObjects);
      boolean throwLastError = false;
      switch (rcvt) {
        case -1:
          throw new IllegalArgumentException(rclass + " is not a supported return type (in method " + method.getName() + " in " + cls + ")");
        case 23:
        case 24:
        case 25:
          fromNative = mapper.getFromNativeConverter(rclass);
          closure_rtype = (Structure.FFIType.get(rclass.isPrimitive() ? rclass : Pointer.class).getPointer()).peer;
          rtype = (Structure.FFIType.get(fromNative.nativeType()).getPointer()).peer;
          break;
        case 17:
        case 18:
        case 19:
        case 21:
        case 22:
          closure_rtype = (Structure.FFIType.get(Pointer.class).getPointer()).peer;
          rtype = (Structure.FFIType.get(NativeMappedConverter.getInstance(rclass).nativeType()).getPointer()).peer;
          break;
        case 3:
        case 26:
          closure_rtype = rtype = (Structure.FFIType.get(Pointer.class).getPointer()).peer;
        case 4:
          closure_rtype = (Structure.FFIType.get(Pointer.class).getPointer()).peer;
          rtype = (Structure.FFIType.get(rclass).getPointer()).peer;
          break;
        default:
          closure_rtype = rtype = (Structure.FFIType.get(rclass).getPointer()).peer;
          break;
      } 
      for (int t = 0; t < ptypes.length; t++) {
        Class<?> type = ptypes[t];
        sig = sig + getSignature(type);
        int conversionType = getConversion(type, mapper, allowObjects);
        cvt[t] = conversionType;
        if (conversionType == -1)
          throw new IllegalArgumentException(type + " is not a supported argument type (in method " + method.getName() + " in " + cls + ")"); 
        if (conversionType == 17 || conversionType == 18 || conversionType == 19 || conversionType == 21) {
          type = NativeMappedConverter.getInstance(type).nativeType();
        } else if (conversionType == 23 || conversionType == 24 || conversionType == 25) {
          toNative[t] = mapper.getToNativeConverter(type);
        } 
        switch (conversionType) {
          case 4:
          case 17:
          case 18:
          case 19:
          case 21:
          case 22:
            atypes[t] = (Structure.FFIType.get(type).getPointer()).peer;
            closure_atypes[t] = (Structure.FFIType.get(Pointer.class).getPointer()).peer;
            break;
          case 23:
          case 24:
          case 25:
            closure_atypes[t] = (Structure.FFIType.get(type.isPrimitive() ? type : Pointer.class).getPointer()).peer;
            atypes[t] = (Structure.FFIType.get(toNative[t].nativeType()).getPointer()).peer;
            break;
          case 0:
            atypes[t] = (Structure.FFIType.get(type).getPointer()).peer;
            closure_atypes[t] = (Structure.FFIType.get(type).getPointer()).peer;
          default:
            atypes[t] = (Structure.FFIType.get(Pointer.class).getPointer()).peer;
            closure_atypes[t] = (Structure.FFIType.get(Pointer.class).getPointer()).peer;
            break;
        } 
      } 
      sig = sig + ")";
      sig = sig + getSignature(rclass);
      Class<?>[] etypes = method.getExceptionTypes();
      for (int e = 0; e < etypes.length; e++) {
        if (LastErrorException.class.isAssignableFrom(etypes[e])) {
          throwLastError = true;
          break;
        } 
      } 
      Function f = lib.getFunction(method.getName(), method);
      try {
        handles[i] = registerMethod(cls, method.getName(), sig, cvt, closure_atypes, atypes, rcvt, closure_rtype, rtype, method, f.peer, f
            
            .getCallingConvention(), throwLastError, toNative, fromNative, f.encoding);
      } catch (NoSuchMethodError noSuchMethodError) {
        throw new UnsatisfiedLinkError("No method " + method.getName() + " with signature " + sig + " in " + cls);
      } 
    } 
    synchronized (registeredClasses) {
      registeredClasses.put(cls, handles);
      registeredLibraries.put(cls, lib);
    } 
  }
  
  private static Map<String, Object> cacheOptions(Class<?> cls, Map<String, ?> options, Object proxy) {
    Map<String, Object> libOptions = new HashMap<String, Object>(options);
    libOptions.put("enclosing-library", cls);
    typeOptions.put(cls, libOptions);
    if (proxy != null)
      libraries.put(cls, new WeakReference(proxy)); 
    if (!cls.isInterface() && Library.class
      .isAssignableFrom(cls)) {
      Class<?>[] ifaces = cls.getInterfaces();
      for (Class<?> ifc : ifaces) {
        if (Library.class.isAssignableFrom(ifc)) {
          cacheOptions(ifc, libOptions, proxy);
          break;
        } 
      } 
    } 
    return libOptions;
  }
  
  private static NativeMapped fromNative(Class<?> cls, Object value) {
    return (NativeMapped)NativeMappedConverter.getInstance(cls).fromNative(value, new FromNativeContext(cls));
  }
  
  private static NativeMapped fromNative(Method m, Object value) {
    Class<?> cls = m.getReturnType();
    return (NativeMapped)NativeMappedConverter.getInstance(cls).fromNative(value, new MethodResultContext(cls, null, null, m));
  }
  
  private static Class<?> nativeType(Class<?> cls) {
    return NativeMappedConverter.getInstance(cls).nativeType();
  }
  
  private static Object toNative(ToNativeConverter cvt, Object o) {
    return cvt.toNative(o, new ToNativeContext());
  }
  
  private static Object fromNative(FromNativeConverter cvt, Object o, Method m) {
    return cvt.fromNative(o, new MethodResultContext(m.getReturnType(), null, null, m));
  }
  
  public static void main(String[] args) {
    String DEFAULT_TITLE = "Java Native Access (JNA)";
    String DEFAULT_VERSION = "5.5.0";
    String DEFAULT_BUILD = "5.5.0 (package information missing)";
    Package pkg = Native.class.getPackage();
    String title = (pkg != null) ? pkg.getSpecificationTitle() : "Java Native Access (JNA)";
    if (title == null)
      title = "Java Native Access (JNA)"; 
    String version = (pkg != null) ? pkg.getSpecificationVersion() : "5.5.0";
    if (version == null)
      version = "5.5.0"; 
    title = title + " API Version " + version;
    System.out.println(title);
    version = (pkg != null) ? pkg.getImplementationVersion() : "5.5.0 (package information missing)";
    if (version == null)
      version = "5.5.0 (package information missing)"; 
    System.out.println("Version: " + version);
    System.out.println(" Native: " + getNativeVersion() + " (" + 
        getAPIChecksum() + ")");
    System.out.println(" Prefix: " + Platform.RESOURCE_PREFIX);
  }
  
  static Structure invokeStructure(Function function, long fp, int callFlags, Object[] args, Structure s) {
    invokeStructure(function, fp, callFlags, args, (s.getPointer()).peer, 
        (s.getTypeInfo()).peer);
    return s;
  }
  
  static long open(String name) {
    return open(name, -1);
  }
  
  static Pointer getPointer(long addr) {
    long peer = _getPointer(addr);
    return (peer == 0L) ? null : new Pointer(peer);
  }
  
  static String getString(Pointer pointer, long offset) {
    return getString(pointer, offset, getDefaultStringEncoding());
  }
  
  static String getString(Pointer pointer, long offset, String encoding) {
    byte[] data = getStringBytes(pointer, pointer.peer, offset);
    if (encoding != null)
      try {
        return new String(data, encoding);
      } catch (UnsupportedEncodingException unsupportedEncodingException) {} 
    return new String(data);
  }
  
  private static final ThreadLocal<Memory> nativeThreadTerminationFlag = new ThreadLocal<Memory>() {
      protected Memory initialValue() {
        Memory m = new Memory(4L);
        m.clear();
        return m;
      }
    };
  
  private static final Map<Thread, Pointer> nativeThreads = Collections.synchronizedMap(new WeakHashMap<Thread, Pointer>());
  
  public static void detach(boolean detach) {
    Thread thread = Thread.currentThread();
    if (detach) {
      nativeThreads.remove(thread);
      Pointer p = nativeThreadTerminationFlag.get();
      setDetachState(true, 0L);
    } else if (!nativeThreads.containsKey(thread)) {
      Pointer p = nativeThreadTerminationFlag.get();
      nativeThreads.put(thread, p);
      setDetachState(false, p.peer);
    } 
  }
  
  static Pointer getTerminationFlag(Thread t) {
    return nativeThreads.get(t);
  }
  
  private static native void initIDs();
  
  public static synchronized native void setProtected(boolean paramBoolean);
  
  public static synchronized native boolean isProtected();
  
  static native long getWindowHandle0(Component paramComponent);
  
  private static native long _getDirectBufferPointer(Buffer paramBuffer);
  
  private static native int sizeof(int paramInt);
  
  private static native String getNativeVersion();
  
  private static native String getAPIChecksum();
  
  public static native int getLastError();
  
  public static native void setLastError(int paramInt);
  
  private static native void unregister(Class<?> paramClass, long[] paramArrayOflong);
  
  private static native long registerMethod(Class<?> paramClass, String paramString1, String paramString2, int[] paramArrayOfint, long[] paramArrayOflong1, long[] paramArrayOflong2, int paramInt1, long paramLong1, long paramLong2, Method paramMethod, long paramLong3, int paramInt2, boolean paramBoolean, ToNativeConverter[] paramArrayOfToNativeConverter, FromNativeConverter paramFromNativeConverter, String paramString3);
  
  public static native long ffi_prep_cif(int paramInt1, int paramInt2, long paramLong1, long paramLong2);
  
  public static native void ffi_call(long paramLong1, long paramLong2, long paramLong3, long paramLong4);
  
  public static native long ffi_prep_closure(long paramLong, ffi_callback paramffi_callback);
  
  public static native void ffi_free_closure(long paramLong);
  
  static native int initialize_ffi_type(long paramLong);
  
  static synchronized native void freeNativeCallback(long paramLong);
  
  static synchronized native long createNativeCallback(Callback paramCallback, Method paramMethod, Class<?>[] paramArrayOfClass, Class<?> paramClass, int paramInt1, int paramInt2, String paramString);
  
  static native int invokeInt(Function paramFunction, long paramLong, int paramInt, Object[] paramArrayOfObject);
  
  static native long invokeLong(Function paramFunction, long paramLong, int paramInt, Object[] paramArrayOfObject);
  
  static native void invokeVoid(Function paramFunction, long paramLong, int paramInt, Object[] paramArrayOfObject);
  
  static native float invokeFloat(Function paramFunction, long paramLong, int paramInt, Object[] paramArrayOfObject);
  
  static native double invokeDouble(Function paramFunction, long paramLong, int paramInt, Object[] paramArrayOfObject);
  
  static native long invokePointer(Function paramFunction, long paramLong, int paramInt, Object[] paramArrayOfObject);
  
  private static native void invokeStructure(Function paramFunction, long paramLong1, int paramInt, Object[] paramArrayOfObject, long paramLong2, long paramLong3);
  
  static native Object invokeObject(Function paramFunction, long paramLong, int paramInt, Object[] paramArrayOfObject);
  
  static native long open(String paramString, int paramInt);
  
  static native void close(long paramLong);
  
  static native long findSymbol(long paramLong, String paramString);
  
  static native long indexOf(Pointer paramPointer, long paramLong1, long paramLong2, byte paramByte);
  
  static native void read(Pointer paramPointer, long paramLong1, long paramLong2, byte[] paramArrayOfbyte, int paramInt1, int paramInt2);
  
  static native void read(Pointer paramPointer, long paramLong1, long paramLong2, short[] paramArrayOfshort, int paramInt1, int paramInt2);
  
  static native void read(Pointer paramPointer, long paramLong1, long paramLong2, char[] paramArrayOfchar, int paramInt1, int paramInt2);
  
  static native void read(Pointer paramPointer, long paramLong1, long paramLong2, int[] paramArrayOfint, int paramInt1, int paramInt2);
  
  static native void read(Pointer paramPointer, long paramLong1, long paramLong2, long[] paramArrayOflong, int paramInt1, int paramInt2);
  
  static native void read(Pointer paramPointer, long paramLong1, long paramLong2, float[] paramArrayOffloat, int paramInt1, int paramInt2);
  
  static native void read(Pointer paramPointer, long paramLong1, long paramLong2, double[] paramArrayOfdouble, int paramInt1, int paramInt2);
  
  static native void write(Pointer paramPointer, long paramLong1, long paramLong2, byte[] paramArrayOfbyte, int paramInt1, int paramInt2);
  
  static native void write(Pointer paramPointer, long paramLong1, long paramLong2, short[] paramArrayOfshort, int paramInt1, int paramInt2);
  
  static native void write(Pointer paramPointer, long paramLong1, long paramLong2, char[] paramArrayOfchar, int paramInt1, int paramInt2);
  
  static native void write(Pointer paramPointer, long paramLong1, long paramLong2, int[] paramArrayOfint, int paramInt1, int paramInt2);
  
  static native void write(Pointer paramPointer, long paramLong1, long paramLong2, long[] paramArrayOflong, int paramInt1, int paramInt2);
  
  static native void write(Pointer paramPointer, long paramLong1, long paramLong2, float[] paramArrayOffloat, int paramInt1, int paramInt2);
  
  static native void write(Pointer paramPointer, long paramLong1, long paramLong2, double[] paramArrayOfdouble, int paramInt1, int paramInt2);
  
  static native byte getByte(Pointer paramPointer, long paramLong1, long paramLong2);
  
  static native char getChar(Pointer paramPointer, long paramLong1, long paramLong2);
  
  static native short getShort(Pointer paramPointer, long paramLong1, long paramLong2);
  
  static native int getInt(Pointer paramPointer, long paramLong1, long paramLong2);
  
  static native long getLong(Pointer paramPointer, long paramLong1, long paramLong2);
  
  static native float getFloat(Pointer paramPointer, long paramLong1, long paramLong2);
  
  static native double getDouble(Pointer paramPointer, long paramLong1, long paramLong2);
  
  private static native long _getPointer(long paramLong);
  
  static native String getWideString(Pointer paramPointer, long paramLong1, long paramLong2);
  
  static native byte[] getStringBytes(Pointer paramPointer, long paramLong1, long paramLong2);
  
  static native void setMemory(Pointer paramPointer, long paramLong1, long paramLong2, long paramLong3, byte paramByte);
  
  static native void setByte(Pointer paramPointer, long paramLong1, long paramLong2, byte paramByte);
  
  static native void setShort(Pointer paramPointer, long paramLong1, long paramLong2, short paramShort);
  
  static native void setChar(Pointer paramPointer, long paramLong1, long paramLong2, char paramChar);
  
  static native void setInt(Pointer paramPointer, long paramLong1, long paramLong2, int paramInt);
  
  static native void setLong(Pointer paramPointer, long paramLong1, long paramLong2, long paramLong3);
  
  static native void setFloat(Pointer paramPointer, long paramLong1, long paramLong2, float paramFloat);
  
  static native void setDouble(Pointer paramPointer, long paramLong1, long paramLong2, double paramDouble);
  
  static native void setPointer(Pointer paramPointer, long paramLong1, long paramLong2, long paramLong3);
  
  static native void setWideString(Pointer paramPointer, long paramLong1, long paramLong2, String paramString);
  
  static native ByteBuffer getDirectByteBuffer(Pointer paramPointer, long paramLong1, long paramLong2, long paramLong3);
  
  public static native long malloc(long paramLong);
  
  public static native void free(long paramLong);
  
  private static native void setDetachState(boolean paramBoolean, long paramLong);
  
  public static interface ffi_callback {
    void invoke(long param1Long1, long param1Long2, long param1Long3);
  }
  
  private static class Buffers {
    static boolean isBuffer(Class<?> cls) {
      return Buffer.class.isAssignableFrom(cls);
    }
  }
  
  private static class AWT {
    static long getWindowID(Window w) throws HeadlessException {
      return getComponentID(w);
    }
    
    static long getComponentID(Object o) throws HeadlessException {
      if (GraphicsEnvironment.isHeadless())
        throw new HeadlessException("No native windows when headless"); 
      Component c = (Component)o;
      if (c.isLightweight())
        throw new IllegalArgumentException("Component must be heavyweight"); 
      if (!c.isDisplayable())
        throw new IllegalStateException("Component must be displayable"); 
      if (Platform.isX11() && 
        System.getProperty("java.version").startsWith("1.4") && 
        !c.isVisible())
        throw new IllegalStateException("Component must be visible"); 
      return Native.getWindowHandle0(c);
    }
  }
}
