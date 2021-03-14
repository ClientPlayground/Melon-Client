package com.github.steveice10.netty.resolver;

import com.github.steveice10.netty.util.NetUtil;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.PlatformDependent;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public final class HostsFileParser {
  private static final String WINDOWS_DEFAULT_SYSTEM_ROOT = "C:\\Windows";
  
  private static final String WINDOWS_HOSTS_FILE_RELATIVE_PATH = "\\system32\\drivers\\etc\\hosts";
  
  private static final String X_PLATFORMS_HOSTS_FILE_PATH = "/etc/hosts";
  
  private static final Pattern WHITESPACES = Pattern.compile("[ \t]+");
  
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(HostsFileParser.class);
  
  private static File locateHostsFile() {
    File hostsFile;
    if (PlatformDependent.isWindows()) {
      hostsFile = new File(System.getenv("SystemRoot") + "\\system32\\drivers\\etc\\hosts");
      if (!hostsFile.exists())
        hostsFile = new File("C:\\Windows\\system32\\drivers\\etc\\hosts"); 
    } else {
      hostsFile = new File("/etc/hosts");
    } 
    return hostsFile;
  }
  
  public static HostsFileEntries parseSilently() {
    File hostsFile = locateHostsFile();
    try {
      return parse(hostsFile);
    } catch (IOException e) {
      logger.warn("Failed to load and parse hosts file at " + hostsFile.getPath(), e);
      return HostsFileEntries.EMPTY;
    } 
  }
  
  public static HostsFileEntries parse() throws IOException {
    return parse(locateHostsFile());
  }
  
  public static HostsFileEntries parse(File file) throws IOException {
    ObjectUtil.checkNotNull(file, "file");
    if (file.exists() && file.isFile())
      return parse(new BufferedReader(new FileReader(file))); 
    return HostsFileEntries.EMPTY;
  }
  
  public static HostsFileEntries parse(Reader reader) throws IOException {
    ObjectUtil.checkNotNull(reader, "reader");
    BufferedReader buff = new BufferedReader(reader);
    try {
      Map<String, Inet4Address> ipv4Entries = new HashMap<String, Inet4Address>();
      Map<String, Inet6Address> ipv6Entries = new HashMap<String, Inet6Address>();
      String line;
      while ((line = buff.readLine()) != null) {
        int commentPosition = line.indexOf('#');
        if (commentPosition != -1)
          line = line.substring(0, commentPosition); 
        line = line.trim();
        if (line.isEmpty())
          continue; 
        List<String> lineParts = new ArrayList<String>();
        for (String s : WHITESPACES.split(line)) {
          if (!s.isEmpty())
            lineParts.add(s); 
        } 
        if (lineParts.size() < 2)
          continue; 
        byte[] ipBytes = NetUtil.createByteArrayFromIpAddressString(lineParts.get(0));
        if (ipBytes == null)
          continue; 
        for (int i = 1; i < lineParts.size(); i++) {
          String hostname = lineParts.get(i);
          String hostnameLower = hostname.toLowerCase(Locale.ENGLISH);
          InetAddress address = InetAddress.getByAddress(hostname, ipBytes);
          if (address instanceof Inet4Address) {
            Inet4Address previous = ipv4Entries.put(hostnameLower, (Inet4Address)address);
            if (previous != null)
              ipv4Entries.put(hostnameLower, previous); 
          } else {
            Inet6Address previous = ipv6Entries.put(hostnameLower, (Inet6Address)address);
            if (previous != null)
              ipv6Entries.put(hostnameLower, previous); 
          } 
        } 
      } 
      return (ipv4Entries.isEmpty() && ipv6Entries.isEmpty()) ? HostsFileEntries.EMPTY : new HostsFileEntries(ipv4Entries, ipv6Entries);
    } finally {
      try {
        buff.close();
      } catch (IOException e) {
        logger.warn("Failed to close a reader", e);
      } 
    } 
  }
}
