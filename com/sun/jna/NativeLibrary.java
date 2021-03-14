package com.sun.jna;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NativeLibrary {
  private static final Logger LOG = Logger.getLogger(NativeLibrary.class.getName());
  
  private static final Level DEBUG_LOAD_LEVEL = Native.DEBUG_LOAD ? Level.INFO : Level.FINE;
  
  private long handle;
  
  private final String libraryName;
  
  private final String libraryPath;
  
  private final Map<String, Function> functions = new HashMap<String, Function>();
  
  final int callFlags;
  
  private String encoding;
  
  final Map<String, ?> options;
  
  private static final Map<String, Reference<NativeLibrary>> libraries = new HashMap<String, Reference<NativeLibrary>>();
  
  private static final Map<String, List<String>> searchPaths = Collections.synchronizedMap(new HashMap<String, List<String>>());
  
  private static final List<String> librarySearchPath = new ArrayList<String>();
  
  private static final int DEFAULT_OPEN_OPTIONS = -1;
  
  static {
    if (Native.POINTER_SIZE == 0)
      throw new Error("Native library not initialized"); 
  }
  
  private static String functionKey(String name, int flags, String encoding) {
    return name + "|" + flags + "|" + encoding;
  }
  
  private NativeLibrary(String libraryName, String libraryPath, long handle, Map<String, ?> options) {
    this.libraryName = getLibraryName(libraryName);
    this.libraryPath = libraryPath;
    this.handle = handle;
    Object option = options.get("calling-convention");
    int callingConvention = (option instanceof Number) ? ((Number)option).intValue() : 0;
    this.callFlags = callingConvention;
    this.options = options;
    this.encoding = (String)options.get("string-encoding");
    if (this.encoding == null)
      this.encoding = Native.getDefaultStringEncoding(); 
    if (Platform.isWindows() && "kernel32".equals(this.libraryName.toLowerCase()))
      synchronized (this.functions) {
        Function f = new Function(this, "GetLastError", 63, this.encoding) {
            Object invoke(Object[] args, Class<?> returnType, boolean b, int fixedArgs) {
              return Integer.valueOf(Native.getLastError());
            }
            
            Object invoke(Method invokingMethod, Class<?>[] paramTypes, Class<?> returnType, Object[] inArgs, Map<String, ?> options) {
              return Integer.valueOf(Native.getLastError());
            }
          };
        this.functions.put(functionKey("GetLastError", this.callFlags, this.encoding), f);
      }  
  }
  
  private static int openFlags(Map<String, ?> options) {
    Object opt = options.get("open-flags");
    if (opt instanceof Number)
      return ((Number)opt).intValue(); 
    return -1;
  }
  
  private static NativeLibrary loadLibrary(String libraryName, Map<String, ?> options) {
    LOG.log(DEBUG_LOAD_LEVEL, "Looking for library '" + libraryName + "'");
    List<Throwable> exceptions = new ArrayList<Throwable>();
    boolean isAbsolutePath = (new File(libraryName)).isAbsolute();
    List<String> searchPath = new ArrayList<String>();
    int openFlags = openFlags(options);
    String webstartPath = Native.getWebStartLibraryPath(libraryName);
    if (webstartPath != null) {
      LOG.log(DEBUG_LOAD_LEVEL, "Adding web start path " + webstartPath);
      searchPath.add(webstartPath);
    } 
    List<String> customPaths = searchPaths.get(libraryName);
    if (customPaths != null)
      synchronized (customPaths) {
        searchPath.addAll(0, customPaths);
      }  
    LOG.log(DEBUG_LOAD_LEVEL, "Adding paths from jna.library.path: " + System.getProperty("jna.library.path"));
    searchPath.addAll(initPaths("jna.library.path"));
    String libraryPath = findLibraryPath(libraryName, searchPath);
    long handle = 0L;
    try {
      LOG.log(DEBUG_LOAD_LEVEL, "Trying " + libraryPath);
      handle = Native.open(libraryPath, openFlags);
    } catch (UnsatisfiedLinkError e) {
      LOG.log(DEBUG_LOAD_LEVEL, "Loading failed with message: " + e.getMessage());
      LOG.log(DEBUG_LOAD_LEVEL, "Adding system paths: " + librarySearchPath);
      exceptions.add(e);
      searchPath.addAll(librarySearchPath);
    } 
    try {
      if (handle == 0L) {
        libraryPath = findLibraryPath(libraryName, searchPath);
        LOG.log(DEBUG_LOAD_LEVEL, "Trying " + libraryPath);
        handle = Native.open(libraryPath, openFlags);
        if (handle == 0L)
          throw new UnsatisfiedLinkError("Failed to load library '" + libraryName + "'"); 
      } 
    } catch (UnsatisfiedLinkError ule) {
      LOG.log(DEBUG_LOAD_LEVEL, "Loading failed with message: " + ule.getMessage());
      exceptions.add(ule);
      if (Platform.isAndroid()) {
        try {
          LOG.log(DEBUG_LOAD_LEVEL, "Preload (via System.loadLibrary) " + libraryName);
          System.loadLibrary(libraryName);
          handle = Native.open(libraryPath, openFlags);
        } catch (UnsatisfiedLinkError e2) {
          LOG.log(DEBUG_LOAD_LEVEL, "Loading failed with message: " + e2.getMessage());
          exceptions.add(e2);
        } 
      } else if (Platform.isLinux() || Platform.isFreeBSD()) {
        LOG.log(DEBUG_LOAD_LEVEL, "Looking for version variants");
        libraryPath = matchLibrary(libraryName, searchPath);
        if (libraryPath != null) {
          LOG.log(DEBUG_LOAD_LEVEL, "Trying " + libraryPath);
          try {
            handle = Native.open(libraryPath, openFlags);
          } catch (UnsatisfiedLinkError e2) {
            LOG.log(DEBUG_LOAD_LEVEL, "Loading failed with message: " + e2.getMessage());
            exceptions.add(e2);
          } 
        } 
      } else if (Platform.isMac() && !libraryName.endsWith(".dylib")) {
        LOG.log(DEBUG_LOAD_LEVEL, "Looking for matching frameworks");
        libraryPath = matchFramework(libraryName);
        if (libraryPath != null)
          try {
            LOG.log(DEBUG_LOAD_LEVEL, "Trying " + libraryPath);
            handle = Native.open(libraryPath, openFlags);
          } catch (UnsatisfiedLinkError e2) {
            LOG.log(DEBUG_LOAD_LEVEL, "Loading failed with message: " + e2.getMessage());
            exceptions.add(e2);
          }  
      } else if (Platform.isWindows() && !isAbsolutePath) {
        LOG.log(DEBUG_LOAD_LEVEL, "Looking for lib- prefix");
        libraryPath = findLibraryPath("lib" + libraryName, searchPath);
        if (libraryPath != null) {
          LOG.log(DEBUG_LOAD_LEVEL, "Trying " + libraryPath);
          try {
            handle = Native.open(libraryPath, openFlags);
          } catch (UnsatisfiedLinkError e2) {
            LOG.log(DEBUG_LOAD_LEVEL, "Loading failed with message: " + e2.getMessage());
            exceptions.add(e2);
          } 
        } 
      } 
      if (handle == 0L)
        try {
          File embedded = Native.extractFromResourcePath(libraryName, (ClassLoader)options.get("classloader"));
          try {
            handle = Native.open(embedded.getAbsolutePath(), openFlags);
            libraryPath = embedded.getAbsolutePath();
          } finally {
            if (Native.isUnpacked(embedded))
              Native.deleteLibrary(embedded); 
          } 
        } catch (IOException e2) {
          LOG.log(DEBUG_LOAD_LEVEL, "Loading failed with message: " + e2.getMessage());
          exceptions.add(e2);
        }  
      if (handle == 0L) {
        StringBuilder sb = new StringBuilder();
        sb.append("Unable to load library '");
        sb.append(libraryName);
        sb.append("':");
        for (Throwable t : exceptions) {
          sb.append("\n");
          sb.append(t.getMessage());
        } 
        UnsatisfiedLinkError res = new UnsatisfiedLinkError(sb.toString());
        for (Throwable t : exceptions)
          addSuppressedReflected(res, t); 
        throw res;
      } 
    } 
    LOG.log(DEBUG_LOAD_LEVEL, "Found library '" + libraryName + "' at " + libraryPath);
    return new NativeLibrary(libraryName, libraryPath, handle, options);
  }
  
  private static Method addSuppressedMethod = null;
  
  static {
    try {
      addSuppressedMethod = Throwable.class.getMethod("addSuppressed", new Class[] { Throwable.class });
    } catch (NoSuchMethodException noSuchMethodException) {
    
    } catch (SecurityException ex) {
      Logger.getLogger(NativeLibrary.class.getName()).log(Level.SEVERE, "Failed to initialize 'addSuppressed' method", ex);
    } 
    String webstartPath = Native.getWebStartLibraryPath("jnidispatch");
    if (webstartPath != null)
      librarySearchPath.add(webstartPath); 
    if (System.getProperty("jna.platform.library.path") == null && 
      !Platform.isWindows()) {
      String platformPath = "";
      String sep = "";
      String archPath = "";
      if (Platform.isLinux() || Platform.isSolaris() || 
        Platform.isFreeBSD() || Platform.iskFreeBSD())
        archPath = (Platform.isSolaris() ? "/" : "") + (Native.POINTER_SIZE * 8); 
      String[] paths = { "/usr/lib" + archPath, "/lib" + archPath, "/usr/lib", "/lib" };
      if (Platform.isLinux() || Platform.iskFreeBSD() || Platform.isGNU()) {
        String multiArchPath = getMultiArchPath();
        paths = new String[] { "/usr/lib/" + multiArchPath, "/lib/" + multiArchPath, "/usr/lib" + archPath, "/lib" + archPath, "/usr/lib", "/lib" };
      } 
      if (Platform.isLinux()) {
        ArrayList<String> ldPaths = getLinuxLdPaths();
        for (int j = paths.length - 1; 0 <= j; j--) {
          int found = ldPaths.indexOf(paths[j]);
          if (found != -1)
            ldPaths.remove(found); 
          ldPaths.add(0, paths[j]);
        } 
        paths = ldPaths.<String>toArray(new String[0]);
      } 
      for (int i = 0; i < paths.length; i++) {
        File dir = new File(paths[i]);
        if (dir.exists() && dir.isDirectory()) {
          platformPath = platformPath + sep + paths[i];
          sep = File.pathSeparator;
        } 
      } 
      if (!"".equals(platformPath))
        System.setProperty("jna.platform.library.path", platformPath); 
    } 
    librarySearchPath.addAll(initPaths("jna.platform.library.path"));
  }
  
  private static void addSuppressedReflected(Throwable target, Throwable suppressed) {
    if (addSuppressedMethod == null)
      return; 
    try {
      addSuppressedMethod.invoke(target, new Object[] { suppressed });
    } catch (IllegalAccessException ex) {
      throw new RuntimeException("Failed to call addSuppressedMethod", ex);
    } catch (IllegalArgumentException ex) {
      throw new RuntimeException("Failed to call addSuppressedMethod", ex);
    } catch (InvocationTargetException ex) {
      throw new RuntimeException("Failed to call addSuppressedMethod", ex);
    } 
  }
  
  static String matchFramework(String libraryName) {
    File framework = new File(libraryName);
    if (framework.isAbsolute()) {
      if (libraryName.indexOf(".framework") != -1 && framework.exists())
        return framework.getAbsolutePath(); 
      framework = new File(new File(framework.getParentFile(), framework.getName() + ".framework"), framework.getName());
      if (framework.exists())
        return framework.getAbsolutePath(); 
    } else {
      String[] PREFIXES = { System.getProperty("user.home"), "", "/System" };
      String suffix = (libraryName.indexOf(".framework") == -1) ? (libraryName + ".framework/" + libraryName) : libraryName;
      for (int i = 0; i < PREFIXES.length; i++) {
        String libraryPath = PREFIXES[i] + "/Library/Frameworks/" + suffix;
        if ((new File(libraryPath)).exists())
          return libraryPath; 
      } 
    } 
    return null;
  }
  
  private String getLibraryName(String libraryName) {
    String simplified = libraryName;
    String BASE = "---";
    String template = mapSharedLibraryName("---");
    int prefixEnd = template.indexOf("---");
    if (prefixEnd > 0 && simplified.startsWith(template.substring(0, prefixEnd)))
      simplified = simplified.substring(prefixEnd); 
    String suffix = template.substring(prefixEnd + "---".length());
    int suffixStart = simplified.indexOf(suffix);
    if (suffixStart != -1)
      simplified = simplified.substring(0, suffixStart); 
    return simplified;
  }
  
  public static final NativeLibrary getInstance(String libraryName) {
    return getInstance(libraryName, Collections.emptyMap());
  }
  
  public static final NativeLibrary getInstance(String libraryName, ClassLoader classLoader) {
    return getInstance(libraryName, Collections.singletonMap("classloader", classLoader));
  }
  
  public static final NativeLibrary getInstance(String libraryName, Map<String, ?> libraryOptions) {
    Map<String, Object> options = new HashMap<String, Object>(libraryOptions);
    if (options.get("calling-convention") == null)
      options.put("calling-convention", Integer.valueOf(0)); 
    if ((Platform.isLinux() || Platform.isFreeBSD() || Platform.isAIX()) && Platform.C_LIBRARY_NAME.equals(libraryName))
      libraryName = null; 
    synchronized (libraries) {
      Reference<NativeLibrary> ref = libraries.get(libraryName + options);
      NativeLibrary library = (ref != null) ? ref.get() : null;
      if (library == null) {
        if (libraryName == null) {
          library = new NativeLibrary("<process>", null, Native.open(null, openFlags(options)), options);
        } else {
          library = loadLibrary(libraryName, options);
        } 
        ref = new WeakReference<NativeLibrary>(library);
        libraries.put(library.getName() + options, ref);
        File file = library.getFile();
        if (file != null) {
          libraries.put(file.getAbsolutePath() + options, ref);
          libraries.put(file.getName() + options, ref);
        } 
      } 
      return library;
    } 
  }
  
  public static final synchronized NativeLibrary getProcess() {
    return getInstance(null);
  }
  
  public static final synchronized NativeLibrary getProcess(Map<String, ?> options) {
    return getInstance((String)null, options);
  }
  
  public static final void addSearchPath(String libraryName, String path) {
    synchronized (searchPaths) {
      List<String> customPaths = searchPaths.get(libraryName);
      if (customPaths == null) {
        customPaths = Collections.synchronizedList(new ArrayList<String>());
        searchPaths.put(libraryName, customPaths);
      } 
      customPaths.add(path);
    } 
  }
  
  public Function getFunction(String functionName) {
    return getFunction(functionName, this.callFlags);
  }
  
  Function getFunction(String name, Method method) {
    FunctionMapper mapper = (FunctionMapper)this.options.get("function-mapper");
    if (mapper != null)
      name = mapper.getFunctionName(this, method); 
    String prefix = System.getProperty("jna.profiler.prefix", "$$YJP$$");
    if (name.startsWith(prefix))
      name = name.substring(prefix.length()); 
    int flags = this.callFlags;
    Class<?>[] etypes = method.getExceptionTypes();
    for (int i = 0; i < etypes.length; i++) {
      if (LastErrorException.class.isAssignableFrom(etypes[i]))
        flags |= 0x40; 
    } 
    return getFunction(name, flags);
  }
  
  public Function getFunction(String functionName, int callFlags) {
    return getFunction(functionName, callFlags, this.encoding);
  }
  
  public Function getFunction(String functionName, int callFlags, String encoding) {
    if (functionName == null)
      throw new NullPointerException("Function name may not be null"); 
    synchronized (this.functions) {
      String key = functionKey(functionName, callFlags, encoding);
      Function function = this.functions.get(key);
      if (function == null) {
        function = new Function(this, functionName, callFlags, encoding);
        this.functions.put(key, function);
      } 
      return function;
    } 
  }
  
  public Map<String, ?> getOptions() {
    return this.options;
  }
  
  public Pointer getGlobalVariableAddress(String symbolName) {
    try {
      return new Pointer(getSymbolAddress(symbolName));
    } catch (UnsatisfiedLinkError e) {
      throw new UnsatisfiedLinkError("Error looking up '" + symbolName + "': " + e.getMessage());
    } 
  }
  
  long getSymbolAddress(String name) {
    if (this.handle == 0L)
      throw new UnsatisfiedLinkError("Library has been unloaded"); 
    return Native.findSymbol(this.handle, name);
  }
  
  public String toString() {
    return "Native Library <" + this.libraryPath + "@" + this.handle + ">";
  }
  
  public String getName() {
    return this.libraryName;
  }
  
  public File getFile() {
    if (this.libraryPath == null)
      return null; 
    return new File(this.libraryPath);
  }
  
  protected void finalize() {
    dispose();
  }
  
  static void disposeAll() {
    Set<Reference<NativeLibrary>> values;
    synchronized (libraries) {
      values = new LinkedHashSet<Reference<NativeLibrary>>(libraries.values());
    } 
    for (Reference<NativeLibrary> ref : values) {
      NativeLibrary lib = ref.get();
      if (lib != null)
        lib.dispose(); 
    } 
  }
  
  public void dispose() {
    Set<String> keys = new HashSet<String>();
    synchronized (libraries) {
      for (Map.Entry<String, Reference<NativeLibrary>> e : libraries.entrySet()) {
        Reference<NativeLibrary> ref = e.getValue();
        if (ref.get() == this)
          keys.add(e.getKey()); 
      } 
      for (String k : keys)
        libraries.remove(k); 
    } 
    synchronized (this) {
      if (this.handle != 0L) {
        Native.close(this.handle);
        this.handle = 0L;
      } 
    } 
  }
  
  private static List<String> initPaths(String key) {
    String value = System.getProperty(key, "");
    if ("".equals(value))
      return Collections.emptyList(); 
    StringTokenizer st = new StringTokenizer(value, File.pathSeparator);
    List<String> list = new ArrayList<String>();
    while (st.hasMoreTokens()) {
      String path = st.nextToken();
      if (!"".equals(path))
        list.add(path); 
    } 
    return list;
  }
  
  private static String findLibraryPath(String libName, List<String> searchPath) {
    if ((new File(libName)).isAbsolute())
      return libName; 
    String name = mapSharedLibraryName(libName);
    for (String path : searchPath) {
      File file = new File(path, name);
      if (file.exists())
        return file.getAbsolutePath(); 
      if (Platform.isMac())
        if (name.endsWith(".dylib")) {
          file = new File(path, name.substring(0, name.lastIndexOf(".dylib")) + ".jnilib");
          if (file.exists())
            return file.getAbsolutePath(); 
        }  
    } 
    return name;
  }
  
  static String mapSharedLibraryName(String libName) {
    if (Platform.isMac()) {
      if (libName.startsWith("lib") && (libName.endsWith(".dylib") || libName.endsWith(".jnilib")))
        return libName; 
      String name = System.mapLibraryName(libName);
      if (name.endsWith(".jnilib"))
        return name.substring(0, name.lastIndexOf(".jnilib")) + ".dylib"; 
      return name;
    } 
    if (Platform.isLinux() || Platform.isFreeBSD()) {
      if (isVersionedName(libName) || libName.endsWith(".so"))
        return libName; 
    } else if (Platform.isAIX()) {
      if (libName.startsWith("lib"))
        return libName; 
    } else if (Platform.isWindows() && (libName.endsWith(".drv") || libName.endsWith(".dll") || libName.endsWith(".ocx"))) {
      return libName;
    } 
    return System.mapLibraryName(libName);
  }
  
  private static boolean isVersionedName(String name) {
    if (name.startsWith("lib")) {
      int so = name.lastIndexOf(".so.");
      if (so != -1 && so + 4 < name.length()) {
        for (int i = so + 4; i < name.length(); i++) {
          char ch = name.charAt(i);
          if (!Character.isDigit(ch) && ch != '.')
            return false; 
        } 
        return true;
      } 
    } 
    return false;
  }
  
  static String matchLibrary(final String libName, List<String> searchPath) {
    File lib = new File(libName);
    if (lib.isAbsolute())
      searchPath = Arrays.asList(new String[] { lib.getParent() }); 
    FilenameFilter filter = new FilenameFilter() {
        public boolean accept(File dir, String filename) {
          return ((filename.startsWith("lib" + libName + ".so") || (filename.startsWith(libName + ".so") && libName.startsWith("lib"))) && NativeLibrary.isVersionedName(filename));
        }
      };
    Collection<File> matches = new LinkedList<File>();
    for (String path : searchPath) {
      File[] files = (new File(path)).listFiles(filter);
      if (files != null && files.length > 0)
        matches.addAll(Arrays.asList(files)); 
    } 
    double bestVersion = -1.0D;
    String bestMatch = null;
    for (File f : matches) {
      String path = f.getAbsolutePath();
      String ver = path.substring(path.lastIndexOf(".so.") + 4);
      double version = parseVersion(ver);
      if (version > bestVersion) {
        bestVersion = version;
        bestMatch = path;
      } 
    } 
    return bestMatch;
  }
  
  static double parseVersion(String ver) {
    double v = 0.0D;
    double divisor = 1.0D;
    int dot = ver.indexOf(".");
    while (ver != null) {
      String num;
      if (dot != -1) {
        num = ver.substring(0, dot);
        ver = ver.substring(dot + 1);
        dot = ver.indexOf(".");
      } else {
        num = ver;
        ver = null;
      } 
      try {
        v += Integer.parseInt(num) / divisor;
      } catch (NumberFormatException e) {
        return 0.0D;
      } 
      divisor *= 100.0D;
    } 
    return v;
  }
  
  private static String getMultiArchPath() {
    String cpu = Platform.ARCH;
    String kernel = Platform.iskFreeBSD() ? "-kfreebsd" : (Platform.isGNU() ? "" : "-linux");
    String libc = "-gnu";
    if (Platform.isIntel()) {
      cpu = Platform.is64Bit() ? "x86_64" : "i386";
    } else if (Platform.isPPC()) {
      cpu = Platform.is64Bit() ? "powerpc64" : "powerpc";
    } else if (Platform.isARM()) {
      cpu = "arm";
      libc = "-gnueabi";
    } else if (Platform.ARCH.equals("mips64el")) {
      libc = "-gnuabi64";
    } 
    return cpu + kernel + libc;
  }
  
  private static ArrayList<String> getLinuxLdPaths() {
    ArrayList<String> ldPaths = new ArrayList<String>();
    BufferedReader reader = null;
    try {
      Process process = Runtime.getRuntime().exec("/sbin/ldconfig -p");
      reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String buffer;
      while ((buffer = reader.readLine()) != null) {
        int startPath = buffer.indexOf(" => ");
        int endPath = buffer.lastIndexOf('/');
        if (startPath != -1 && endPath != -1 && startPath < endPath) {
          String path = buffer.substring(startPath + 4, endPath);
          if (!ldPaths.contains(path))
            ldPaths.add(path); 
        } 
      } 
    } catch (Exception exception) {
    
    } finally {
      if (reader != null)
        try {
          reader.close();
        } catch (IOException iOException) {} 
    } 
    return ldPaths;
  }
}
