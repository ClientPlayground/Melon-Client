package com.github.steveice10.netty.resolver.dns;

import com.github.steveice10.netty.util.NetUtil;
import com.github.steveice10.netty.util.internal.SocketUtils;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

public final class DefaultDnsServerAddressStreamProvider implements DnsServerAddressStreamProvider {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultDnsServerAddressStreamProvider.class);
  
  public static final DefaultDnsServerAddressStreamProvider INSTANCE = new DefaultDnsServerAddressStreamProvider();
  
  private static final List<InetSocketAddress> DEFAULT_NAME_SERVER_LIST;
  
  private static final InetSocketAddress[] DEFAULT_NAME_SERVER_ARRAY;
  
  static {
    List<InetSocketAddress> defaultNameServers = new ArrayList<InetSocketAddress>(2);
    Hashtable<String, String> env = new Hashtable<String, String>();
    env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
    env.put("java.naming.provider.url", "dns://");
    try {
      DirContext ctx = new InitialDirContext(env);
      String dnsUrls = (String)ctx.getEnvironment().get("java.naming.provider.url");
      if (dnsUrls != null && !dnsUrls.isEmpty()) {
        String[] servers = dnsUrls.split(" ");
        for (String server : servers) {
          try {
            URI uri = new URI(server);
            String host = (new URI(server)).getHost();
            if (host == null || host.isEmpty()) {
              logger.debug("Skipping a nameserver URI as host portion could not be extracted: {}", server);
            } else {
              int port = uri.getPort();
              defaultNameServers.add(SocketUtils.socketAddress(uri.getHost(), (port == -1) ? 53 : port));
            } 
          } catch (URISyntaxException e) {
            logger.debug("Skipping a malformed nameserver URI: {}", server, e);
          } 
        } 
      } 
    } catch (NamingException namingException) {}
    if (defaultNameServers.isEmpty())
      try {
        Class<?> configClass = Class.forName("sun.net.dns.ResolverConfiguration");
        Method open = configClass.getMethod("open", new Class[0]);
        Method nameservers = configClass.getMethod("nameservers", new Class[0]);
        Object instance = open.invoke(null, new Object[0]);
        List<String> list = (List<String>)nameservers.invoke(instance, new Object[0]);
        for (String a : list) {
          if (a != null)
            defaultNameServers.add(new InetSocketAddress(SocketUtils.addressByName(a), 53)); 
        } 
      } catch (Exception exception) {} 
    if (!defaultNameServers.isEmpty()) {
      if (logger.isDebugEnabled())
        logger.debug("Default DNS servers: {} (sun.net.dns.ResolverConfiguration)", defaultNameServers); 
    } else {
      if (NetUtil.isIpV6AddressesPreferred() || (NetUtil.LOCALHOST instanceof java.net.Inet6Address && 
        !NetUtil.isIpV4StackPreferred())) {
        Collections.addAll(defaultNameServers, new InetSocketAddress[] { SocketUtils.socketAddress("2001:4860:4860::8888", 53), 
              SocketUtils.socketAddress("2001:4860:4860::8844", 53) });
      } else {
        Collections.addAll(defaultNameServers, new InetSocketAddress[] { SocketUtils.socketAddress("8.8.8.8", 53), 
              SocketUtils.socketAddress("8.8.4.4", 53) });
      } 
      if (logger.isWarnEnabled())
        logger.warn("Default DNS servers: {} (Google Public DNS as a fallback)", defaultNameServers); 
    } 
    DEFAULT_NAME_SERVER_LIST = Collections.unmodifiableList(defaultNameServers);
    DEFAULT_NAME_SERVER_ARRAY = defaultNameServers.<InetSocketAddress>toArray(new InetSocketAddress[defaultNameServers.size()]);
  }
  
  private static final DnsServerAddresses DEFAULT_NAME_SERVERS = DnsServerAddresses.sequential(DEFAULT_NAME_SERVER_ARRAY);
  
  static final int DNS_PORT = 53;
  
  public DnsServerAddressStream nameServerAddressStream(String hostname) {
    return DEFAULT_NAME_SERVERS.stream();
  }
  
  public static List<InetSocketAddress> defaultAddressList() {
    return DEFAULT_NAME_SERVER_LIST;
  }
  
  public static DnsServerAddresses defaultAddresses() {
    return DEFAULT_NAME_SERVERS;
  }
  
  static InetSocketAddress[] defaultAddressArray() {
    return (InetSocketAddress[])DEFAULT_NAME_SERVER_ARRAY.clone();
  }
}
