package com.github.steveice10.netty.resolver.dns;

import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class UnixResolverDnsServerAddressStreamProvider implements DnsServerAddressStreamProvider {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(UnixResolverDnsServerAddressStreamProvider.class);
  
  private static final String ETC_RESOLV_CONF_FILE = "/etc/resolv.conf";
  
  private static final String ETC_RESOLVER_DIR = "/etc/resolver";
  
  private static final String NAMESERVER_ROW_LABEL = "nameserver";
  
  private static final String SORTLIST_ROW_LABEL = "sortlist";
  
  private static final String OPTIONS_ROW_LABEL = "options";
  
  private static final String DOMAIN_ROW_LABEL = "domain";
  
  private static final String PORT_ROW_LABEL = "port";
  
  private static final String NDOTS_LABEL = "ndots:";
  
  static final int DEFAULT_NDOTS = 1;
  
  private final DnsServerAddresses defaultNameServerAddresses;
  
  private final Map<String, DnsServerAddresses> domainToNameServerStreamMap;
  
  static DnsServerAddressStreamProvider parseSilently() {
    try {
      UnixResolverDnsServerAddressStreamProvider nameServerCache = new UnixResolverDnsServerAddressStreamProvider("/etc/resolv.conf", "/etc/resolver");
      return nameServerCache.mayOverrideNameServers() ? nameServerCache : DefaultDnsServerAddressStreamProvider.INSTANCE;
    } catch (Exception e) {
      logger.debug("failed to parse {} and/or {}", new Object[] { "/etc/resolv.conf", "/etc/resolver", e });
      return DefaultDnsServerAddressStreamProvider.INSTANCE;
    } 
  }
  
  public UnixResolverDnsServerAddressStreamProvider(File etcResolvConf, File... etcResolverFiles) throws IOException {
    Map<String, DnsServerAddresses> etcResolvConfMap = parse(new File[] { (File)ObjectUtil.checkNotNull(etcResolvConf, "etcResolvConf") });
    boolean useEtcResolverFiles = (etcResolverFiles != null && etcResolverFiles.length != 0);
    this.domainToNameServerStreamMap = useEtcResolverFiles ? parse(etcResolverFiles) : etcResolvConfMap;
    DnsServerAddresses defaultNameServerAddresses = etcResolvConfMap.get(etcResolvConf.getName());
    if (defaultNameServerAddresses == null) {
      Collection<DnsServerAddresses> values = etcResolvConfMap.values();
      if (values.isEmpty())
        throw new IllegalArgumentException(etcResolvConf + " didn't provide any name servers"); 
      this.defaultNameServerAddresses = values.iterator().next();
    } else {
      this.defaultNameServerAddresses = defaultNameServerAddresses;
    } 
    if (useEtcResolverFiles)
      this.domainToNameServerStreamMap.putAll(etcResolvConfMap); 
  }
  
  public UnixResolverDnsServerAddressStreamProvider(String etcResolvConf, String etcResolverDir) throws IOException {
    this((etcResolvConf == null) ? null : new File(etcResolvConf), (etcResolverDir == null) ? null : (new File(etcResolverDir))
        .listFiles());
  }
  
  public DnsServerAddressStream nameServerAddressStream(String hostname) {
    while (true) {
      int i = hostname.indexOf('.', 1);
      if (i < 0 || i == hostname.length() - 1)
        return this.defaultNameServerAddresses.stream(); 
      DnsServerAddresses addresses = this.domainToNameServerStreamMap.get(hostname);
      if (addresses != null)
        return addresses.stream(); 
      hostname = hostname.substring(i + 1);
    } 
  }
  
  private boolean mayOverrideNameServers() {
    return (!this.domainToNameServerStreamMap.isEmpty() || this.defaultNameServerAddresses.stream().next() != null);
  }
  
  private static Map<String, DnsServerAddresses> parse(File... etcResolverFiles) throws IOException {
    Map<String, DnsServerAddresses> domainToNameServerStreamMap = new HashMap<String, DnsServerAddresses>(etcResolverFiles.length << 1);
    for (File etcResolverFile : etcResolverFiles) {
      if (etcResolverFile.isFile()) {
        FileReader fr = new FileReader(etcResolverFile);
        BufferedReader br = null;
      } 
    } 
    return domainToNameServerStreamMap;
  }
  
  private static void putIfAbsent(Map<String, DnsServerAddresses> domainToNameServerStreamMap, String domainName, List<InetSocketAddress> addresses) {
    putIfAbsent(domainToNameServerStreamMap, domainName, DnsServerAddresses.sequential(addresses));
  }
  
  private static void putIfAbsent(Map<String, DnsServerAddresses> domainToNameServerStreamMap, String domainName, DnsServerAddresses addresses) {
    DnsServerAddresses existingAddresses = domainToNameServerStreamMap.put(domainName, addresses);
    if (existingAddresses != null) {
      domainToNameServerStreamMap.put(domainName, existingAddresses);
      logger.debug("Domain name {} already maps to addresses {} so new addresses {} will be discarded", new Object[] { domainName, existingAddresses, addresses });
    } 
  }
  
  static int parseEtcResolverFirstNdots() throws IOException {
    return parseEtcResolverFirstNdots(new File("/etc/resolv.conf"));
  }
  
  static int parseEtcResolverFirstNdots(File etcResolvConf) throws IOException {
    FileReader fr = new FileReader(etcResolvConf);
    BufferedReader br = null;
    try {
      br = new BufferedReader(fr);
      String line;
      while ((line = br.readLine()) != null) {
        if (line.startsWith("options")) {
          int i = line.indexOf("ndots:");
          if (i >= 0) {
            i += "ndots:".length();
            int j = line.indexOf(' ', i);
            return Integer.parseInt(line.substring(i, (j < 0) ? line.length() : j));
          } 
          break;
        } 
      } 
    } finally {
      if (br == null) {
        fr.close();
      } else {
        br.close();
      } 
    } 
    return 1;
  }
}
