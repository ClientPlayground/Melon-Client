package com.github.steveice10.netty.resolver.dns;

import com.github.steveice10.netty.bootstrap.Bootstrap;
import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.channel.AddressedEnvelope;
import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.ChannelFactory;
import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.channel.ChannelFutureListener;
import com.github.steveice10.netty.channel.ChannelHandler;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.ChannelInboundHandlerAdapter;
import com.github.steveice10.netty.channel.ChannelInitializer;
import com.github.steveice10.netty.channel.ChannelOption;
import com.github.steveice10.netty.channel.ChannelPromise;
import com.github.steveice10.netty.channel.EventLoop;
import com.github.steveice10.netty.channel.EventLoopGroup;
import com.github.steveice10.netty.channel.FixedRecvByteBufAllocator;
import com.github.steveice10.netty.channel.RecvByteBufAllocator;
import com.github.steveice10.netty.channel.socket.DatagramChannel;
import com.github.steveice10.netty.channel.socket.InternetProtocolFamily;
import com.github.steveice10.netty.handler.codec.dns.DatagramDnsQueryEncoder;
import com.github.steveice10.netty.handler.codec.dns.DatagramDnsResponse;
import com.github.steveice10.netty.handler.codec.dns.DatagramDnsResponseDecoder;
import com.github.steveice10.netty.handler.codec.dns.DefaultDnsRawRecord;
import com.github.steveice10.netty.handler.codec.dns.DnsQuestion;
import com.github.steveice10.netty.handler.codec.dns.DnsRecord;
import com.github.steveice10.netty.handler.codec.dns.DnsRecordType;
import com.github.steveice10.netty.handler.codec.dns.DnsResponse;
import com.github.steveice10.netty.resolver.HostsFileEntriesResolver;
import com.github.steveice10.netty.resolver.InetNameResolver;
import com.github.steveice10.netty.resolver.ResolvedAddressTypes;
import com.github.steveice10.netty.util.NetUtil;
import com.github.steveice10.netty.util.ReferenceCountUtil;
import com.github.steveice10.netty.util.concurrent.EventExecutor;
import com.github.steveice10.netty.util.concurrent.FastThreadLocal;
import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.concurrent.FutureListener;
import com.github.steveice10.netty.util.concurrent.GenericFutureListener;
import com.github.steveice10.netty.util.concurrent.Promise;
import com.github.steveice10.netty.util.internal.EmptyArrays;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.PlatformDependent;
import com.github.steveice10.netty.util.internal.StringUtil;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.lang.reflect.Method;
import java.net.IDN;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class DnsNameResolver extends InetNameResolver {
  static {
    int ndots;
    String[] searchDomains;
  }
  
  static {
    logger = InternalLoggerFactory.getInstance(DnsNameResolver.class);
    EMPTY_ADDITIONALS = new DnsRecord[0];
    IPV4_ONLY_RESOLVED_RECORD_TYPES = new DnsRecordType[] { DnsRecordType.A };
    IPV4_ONLY_RESOLVED_PROTOCOL_FAMILIES = new InternetProtocolFamily[] { InternetProtocolFamily.IPv4 };
    IPV4_PREFERRED_RESOLVED_RECORD_TYPES = new DnsRecordType[] { DnsRecordType.A, DnsRecordType.AAAA };
    IPV4_PREFERRED_RESOLVED_PROTOCOL_FAMILIES = new InternetProtocolFamily[] { InternetProtocolFamily.IPv4, InternetProtocolFamily.IPv6 };
    IPV6_ONLY_RESOLVED_RECORD_TYPES = new DnsRecordType[] { DnsRecordType.AAAA };
    IPV6_ONLY_RESOLVED_PROTOCOL_FAMILIES = new InternetProtocolFamily[] { InternetProtocolFamily.IPv6 };
    IPV6_PREFERRED_RESOLVED_RECORD_TYPES = new DnsRecordType[] { DnsRecordType.AAAA, DnsRecordType.A };
    IPV6_PREFERRED_RESOLVED_PROTOCOL_FAMILIES = new InternetProtocolFamily[] { InternetProtocolFamily.IPv6, InternetProtocolFamily.IPv4 };
    if (NetUtil.isIpV4StackPreferred()) {
      DEFAULT_RESOLVE_ADDRESS_TYPES = ResolvedAddressTypes.IPV4_ONLY;
      LOCALHOST_ADDRESS = NetUtil.LOCALHOST4;
    } else if (NetUtil.isIpV6AddressesPreferred()) {
      DEFAULT_RESOLVE_ADDRESS_TYPES = ResolvedAddressTypes.IPV6_PREFERRED;
      LOCALHOST_ADDRESS = NetUtil.LOCALHOST6;
    } else {
      DEFAULT_RESOLVE_ADDRESS_TYPES = ResolvedAddressTypes.IPV4_PREFERRED;
      LOCALHOST_ADDRESS = NetUtil.LOCALHOST4;
    } 
    try {
      Class<?> configClass = Class.forName("sun.net.dns.ResolverConfiguration");
      Method open = configClass.getMethod("open", new Class[0]);
      Method nameservers = configClass.getMethod("searchlist", new Class[0]);
      Object instance = open.invoke(null, new Object[0]);
      List<String> list = (List<String>)nameservers.invoke(instance, new Object[0]);
      searchDomains = list.<String>toArray(new String[list.size()]);
    } catch (Exception ignore) {
      searchDomains = EmptyArrays.EMPTY_STRINGS;
    } 
    DEFAULT_SEARCH_DOMAINS = searchDomains;
    try {
      ndots = UnixResolverDnsServerAddressStreamProvider.parseEtcResolverFirstNdots();
    } catch (Exception ignore) {
      ndots = 1;
    } 
    DEFAULT_NDOTS = ndots;
    DECODER = new DatagramDnsResponseDecoder();
    ENCODER = new DatagramDnsQueryEncoder();
  }
  
  final DnsQueryContextManager queryContextManager = new DnsQueryContextManager();
  
  private final FastThreadLocal<DnsServerAddressStream> nameServerAddrStream = new FastThreadLocal<DnsServerAddressStream>() {
      protected DnsServerAddressStream initialValue() throws Exception {
        return DnsNameResolver.this.dnsServerAddressStreamProvider.nameServerAddressStream("");
      }
    };
  
  private static final InternalLogger logger;
  
  private static final String LOCALHOST = "localhost";
  
  private static final InetAddress LOCALHOST_ADDRESS;
  
  private static final DnsRecord[] EMPTY_ADDITIONALS;
  
  private static final DnsRecordType[] IPV4_ONLY_RESOLVED_RECORD_TYPES;
  
  private static final InternetProtocolFamily[] IPV4_ONLY_RESOLVED_PROTOCOL_FAMILIES;
  
  private static final DnsRecordType[] IPV4_PREFERRED_RESOLVED_RECORD_TYPES;
  
  private static final InternetProtocolFamily[] IPV4_PREFERRED_RESOLVED_PROTOCOL_FAMILIES;
  
  private static final DnsRecordType[] IPV6_ONLY_RESOLVED_RECORD_TYPES;
  
  private static final InternetProtocolFamily[] IPV6_ONLY_RESOLVED_PROTOCOL_FAMILIES;
  
  private static final DnsRecordType[] IPV6_PREFERRED_RESOLVED_RECORD_TYPES;
  
  private static final InternetProtocolFamily[] IPV6_PREFERRED_RESOLVED_PROTOCOL_FAMILIES;
  
  static final ResolvedAddressTypes DEFAULT_RESOLVE_ADDRESS_TYPES;
  
  static final String[] DEFAULT_SEARCH_DOMAINS;
  
  private static final int DEFAULT_NDOTS;
  
  private static final DatagramDnsResponseDecoder DECODER;
  
  private static final DatagramDnsQueryEncoder ENCODER;
  
  final Future<Channel> channelFuture;
  
  final DatagramChannel ch;
  
  private final DnsCache resolveCache;
  
  private final DnsCache authoritativeDnsServerCache;
  
  private final long queryTimeoutMillis;
  
  private final int maxQueriesPerResolve;
  
  private final ResolvedAddressTypes resolvedAddressTypes;
  
  private final InternetProtocolFamily[] resolvedInternetProtocolFamilies;
  
  private final boolean recursionDesired;
  
  private final int maxPayloadSize;
  
  private final boolean optResourceEnabled;
  
  private final HostsFileEntriesResolver hostsFileEntriesResolver;
  
  private final DnsServerAddressStreamProvider dnsServerAddressStreamProvider;
  
  private final String[] searchDomains;
  
  private final int ndots;
  
  private final boolean supportsAAAARecords;
  
  private final boolean supportsARecords;
  
  private final InternetProtocolFamily preferredAddressType;
  
  private final DnsRecordType[] resolveRecordTypes;
  
  private final boolean decodeIdn;
  
  private final DnsQueryLifecycleObserverFactory dnsQueryLifecycleObserverFactory;
  
  public DnsNameResolver(EventLoop eventLoop, ChannelFactory<? extends DatagramChannel> channelFactory, final DnsCache resolveCache, DnsCache authoritativeDnsServerCache, DnsQueryLifecycleObserverFactory dnsQueryLifecycleObserverFactory, long queryTimeoutMillis, ResolvedAddressTypes resolvedAddressTypes, boolean recursionDesired, int maxQueriesPerResolve, boolean traceEnabled, int maxPayloadSize, boolean optResourceEnabled, HostsFileEntriesResolver hostsFileEntriesResolver, DnsServerAddressStreamProvider dnsServerAddressStreamProvider, String[] searchDomains, int ndots, boolean decodeIdn) {
    super((EventExecutor)eventLoop);
    this.queryTimeoutMillis = ObjectUtil.checkPositive(queryTimeoutMillis, "queryTimeoutMillis");
    this.resolvedAddressTypes = (resolvedAddressTypes != null) ? resolvedAddressTypes : DEFAULT_RESOLVE_ADDRESS_TYPES;
    this.recursionDesired = recursionDesired;
    this.maxQueriesPerResolve = ObjectUtil.checkPositive(maxQueriesPerResolve, "maxQueriesPerResolve");
    this.maxPayloadSize = ObjectUtil.checkPositive(maxPayloadSize, "maxPayloadSize");
    this.optResourceEnabled = optResourceEnabled;
    this.hostsFileEntriesResolver = (HostsFileEntriesResolver)ObjectUtil.checkNotNull(hostsFileEntriesResolver, "hostsFileEntriesResolver");
    this
      .dnsServerAddressStreamProvider = (DnsServerAddressStreamProvider)ObjectUtil.checkNotNull(dnsServerAddressStreamProvider, "dnsServerAddressStreamProvider");
    this.resolveCache = (DnsCache)ObjectUtil.checkNotNull(resolveCache, "resolveCache");
    this.authoritativeDnsServerCache = (DnsCache)ObjectUtil.checkNotNull(authoritativeDnsServerCache, "authoritativeDnsServerCache");
    this
      
      .dnsQueryLifecycleObserverFactory = traceEnabled ? ((dnsQueryLifecycleObserverFactory instanceof NoopDnsQueryLifecycleObserverFactory) ? new TraceDnsQueryLifeCycleObserverFactory() : new BiDnsQueryLifecycleObserverFactory(new TraceDnsQueryLifeCycleObserverFactory(), dnsQueryLifecycleObserverFactory)) : (DnsQueryLifecycleObserverFactory)ObjectUtil.checkNotNull(dnsQueryLifecycleObserverFactory, "dnsQueryLifecycleObserverFactory");
    this.searchDomains = (searchDomains != null) ? (String[])searchDomains.clone() : DEFAULT_SEARCH_DOMAINS;
    this.ndots = (ndots >= 0) ? ndots : DEFAULT_NDOTS;
    this.decodeIdn = decodeIdn;
    switch (this.resolvedAddressTypes) {
      case IPV4_ONLY:
        this.supportsAAAARecords = false;
        this.supportsARecords = true;
        this.resolveRecordTypes = IPV4_ONLY_RESOLVED_RECORD_TYPES;
        this.resolvedInternetProtocolFamilies = IPV4_ONLY_RESOLVED_PROTOCOL_FAMILIES;
        this.preferredAddressType = InternetProtocolFamily.IPv4;
        break;
      case IPV4_PREFERRED:
        this.supportsAAAARecords = true;
        this.supportsARecords = true;
        this.resolveRecordTypes = IPV4_PREFERRED_RESOLVED_RECORD_TYPES;
        this.resolvedInternetProtocolFamilies = IPV4_PREFERRED_RESOLVED_PROTOCOL_FAMILIES;
        this.preferredAddressType = InternetProtocolFamily.IPv4;
        break;
      case IPV6_ONLY:
        this.supportsAAAARecords = true;
        this.supportsARecords = false;
        this.resolveRecordTypes = IPV6_ONLY_RESOLVED_RECORD_TYPES;
        this.resolvedInternetProtocolFamilies = IPV6_ONLY_RESOLVED_PROTOCOL_FAMILIES;
        this.preferredAddressType = InternetProtocolFamily.IPv6;
        break;
      case IPV6_PREFERRED:
        this.supportsAAAARecords = true;
        this.supportsARecords = true;
        this.resolveRecordTypes = IPV6_PREFERRED_RESOLVED_RECORD_TYPES;
        this.resolvedInternetProtocolFamilies = IPV6_PREFERRED_RESOLVED_PROTOCOL_FAMILIES;
        this.preferredAddressType = InternetProtocolFamily.IPv6;
        break;
      default:
        throw new IllegalArgumentException("Unknown ResolvedAddressTypes " + resolvedAddressTypes);
    } 
    Bootstrap b = new Bootstrap();
    b.group((EventLoopGroup)executor());
    b.channelFactory(channelFactory);
    b.option(ChannelOption.DATAGRAM_CHANNEL_ACTIVE_ON_REGISTRATION, Boolean.valueOf(true));
    final DnsResponseHandler responseHandler = new DnsResponseHandler(executor().newPromise());
    b.handler((ChannelHandler)new ChannelInitializer<DatagramChannel>() {
          protected void initChannel(DatagramChannel ch) throws Exception {
            ch.pipeline().addLast(new ChannelHandler[] { (ChannelHandler)DnsNameResolver.access$100(), (ChannelHandler)DnsNameResolver.access$200(), (ChannelHandler)this.val$responseHandler });
          }
        });
    this.channelFuture = (Future<Channel>)responseHandler.channelActivePromise;
    this.ch = (DatagramChannel)b.register().channel();
    this.ch.config().setRecvByteBufAllocator((RecvByteBufAllocator)new FixedRecvByteBufAllocator(maxPayloadSize));
    this.ch.closeFuture().addListener((GenericFutureListener)new ChannelFutureListener() {
          public void operationComplete(ChannelFuture future) throws Exception {
            resolveCache.clear();
          }
        });
  }
  
  int dnsRedirectPort(InetAddress server) {
    return 53;
  }
  
  final DnsQueryLifecycleObserverFactory dnsQueryLifecycleObserverFactory() {
    return this.dnsQueryLifecycleObserverFactory;
  }
  
  protected DnsServerAddressStream uncachedRedirectDnsServerStream(List<InetSocketAddress> nameServers) {
    return DnsServerAddresses.sequential(nameServers).stream();
  }
  
  public DnsCache resolveCache() {
    return this.resolveCache;
  }
  
  public DnsCache authoritativeDnsServerCache() {
    return this.authoritativeDnsServerCache;
  }
  
  public long queryTimeoutMillis() {
    return this.queryTimeoutMillis;
  }
  
  public ResolvedAddressTypes resolvedAddressTypes() {
    return this.resolvedAddressTypes;
  }
  
  InternetProtocolFamily[] resolvedInternetProtocolFamiliesUnsafe() {
    return this.resolvedInternetProtocolFamilies;
  }
  
  final String[] searchDomains() {
    return this.searchDomains;
  }
  
  final int ndots() {
    return this.ndots;
  }
  
  final boolean supportsAAAARecords() {
    return this.supportsAAAARecords;
  }
  
  final boolean supportsARecords() {
    return this.supportsARecords;
  }
  
  final InternetProtocolFamily preferredAddressType() {
    return this.preferredAddressType;
  }
  
  final DnsRecordType[] resolveRecordTypes() {
    return this.resolveRecordTypes;
  }
  
  final boolean isDecodeIdn() {
    return this.decodeIdn;
  }
  
  public boolean isRecursionDesired() {
    return this.recursionDesired;
  }
  
  public int maxQueriesPerResolve() {
    return this.maxQueriesPerResolve;
  }
  
  public int maxPayloadSize() {
    return this.maxPayloadSize;
  }
  
  public boolean isOptResourceEnabled() {
    return this.optResourceEnabled;
  }
  
  public HostsFileEntriesResolver hostsFileEntriesResolver() {
    return this.hostsFileEntriesResolver;
  }
  
  public void close() {
    if (this.ch.isOpen())
      this.ch.close(); 
  }
  
  protected EventLoop executor() {
    return (EventLoop)super.executor();
  }
  
  private InetAddress resolveHostsFileEntry(String hostname) {
    if (this.hostsFileEntriesResolver == null)
      return null; 
    InetAddress address = this.hostsFileEntriesResolver.address(hostname, this.resolvedAddressTypes);
    if (address == null && PlatformDependent.isWindows() && "localhost".equalsIgnoreCase(hostname))
      return LOCALHOST_ADDRESS; 
    return address;
  }
  
  public final Future<InetAddress> resolve(String inetHost, Iterable<DnsRecord> additionals) {
    return resolve(inetHost, additionals, executor().newPromise());
  }
  
  public final Future<InetAddress> resolve(String inetHost, Iterable<DnsRecord> additionals, Promise<InetAddress> promise) {
    ObjectUtil.checkNotNull(promise, "promise");
    DnsRecord[] additionalsArray = toArray(additionals, true);
    try {
      doResolve(inetHost, additionalsArray, promise, this.resolveCache);
      return (Future<InetAddress>)promise;
    } catch (Exception e) {
      return (Future<InetAddress>)promise.setFailure(e);
    } 
  }
  
  public final Future<List<InetAddress>> resolveAll(String inetHost, Iterable<DnsRecord> additionals) {
    return resolveAll(inetHost, additionals, executor().newPromise());
  }
  
  public final Future<List<InetAddress>> resolveAll(String inetHost, Iterable<DnsRecord> additionals, Promise<List<InetAddress>> promise) {
    ObjectUtil.checkNotNull(promise, "promise");
    DnsRecord[] additionalsArray = toArray(additionals, true);
    try {
      doResolveAll(inetHost, additionalsArray, promise, this.resolveCache);
      return (Future<List<InetAddress>>)promise;
    } catch (Exception e) {
      return (Future<List<InetAddress>>)promise.setFailure(e);
    } 
  }
  
  protected void doResolve(String inetHost, Promise<InetAddress> promise) throws Exception {
    doResolve(inetHost, EMPTY_ADDITIONALS, promise, this.resolveCache);
  }
  
  public final Future<List<DnsRecord>> resolveAll(DnsQuestion question) {
    return resolveAll(question, EMPTY_ADDITIONALS, executor().newPromise());
  }
  
  public final Future<List<DnsRecord>> resolveAll(DnsQuestion question, Iterable<DnsRecord> additionals) {
    return resolveAll(question, additionals, executor().newPromise());
  }
  
  public final Future<List<DnsRecord>> resolveAll(DnsQuestion question, Iterable<DnsRecord> additionals, Promise<List<DnsRecord>> promise) {
    DnsRecord[] additionalsArray = toArray(additionals, true);
    return resolveAll(question, additionalsArray, promise);
  }
  
  private Future<List<DnsRecord>> resolveAll(DnsQuestion question, DnsRecord[] additionals, Promise<List<DnsRecord>> promise) {
    ObjectUtil.checkNotNull(question, "question");
    ObjectUtil.checkNotNull(promise, "promise");
    DnsRecordType type = question.type();
    String hostname = question.name();
    if (type == DnsRecordType.A || type == DnsRecordType.AAAA) {
      InetAddress hostsFileEntry = resolveHostsFileEntry(hostname);
      if (hostsFileEntry != null) {
        ByteBuf content = null;
        if (hostsFileEntry instanceof java.net.Inet4Address) {
          if (type == DnsRecordType.A)
            content = Unpooled.wrappedBuffer(hostsFileEntry.getAddress()); 
        } else if (hostsFileEntry instanceof java.net.Inet6Address && 
          type == DnsRecordType.AAAA) {
          content = Unpooled.wrappedBuffer(hostsFileEntry.getAddress());
        } 
        if (content != null) {
          trySuccess(promise, (List)Collections.singletonList(new DefaultDnsRawRecord(hostname, type, 86400L, content)));
          return (Future<List<DnsRecord>>)promise;
        } 
      } 
    } 
    DnsServerAddressStream nameServerAddrs = this.dnsServerAddressStreamProvider.nameServerAddressStream(hostname);
    (new DnsRecordResolveContext(this, question, additionals, nameServerAddrs)).resolve(promise);
    return (Future<List<DnsRecord>>)promise;
  }
  
  private static DnsRecord[] toArray(Iterable<DnsRecord> additionals, boolean validateType) {
    ObjectUtil.checkNotNull(additionals, "additionals");
    if (additionals instanceof Collection) {
      Collection<DnsRecord> collection = (Collection<DnsRecord>)additionals;
      for (DnsRecord r : additionals)
        validateAdditional(r, validateType); 
      return collection.<DnsRecord>toArray(new DnsRecord[collection.size()]);
    } 
    Iterator<DnsRecord> additionalsIt = additionals.iterator();
    if (!additionalsIt.hasNext())
      return EMPTY_ADDITIONALS; 
    List<DnsRecord> records = new ArrayList<DnsRecord>();
    do {
      DnsRecord r = additionalsIt.next();
      validateAdditional(r, validateType);
      records.add(r);
    } while (additionalsIt.hasNext());
    return records.<DnsRecord>toArray(new DnsRecord[records.size()]);
  }
  
  private static void validateAdditional(DnsRecord record, boolean validateType) {
    ObjectUtil.checkNotNull(record, "record");
    if (validateType && record instanceof com.github.steveice10.netty.handler.codec.dns.DnsRawRecord)
      throw new IllegalArgumentException("DnsRawRecord implementations not allowed: " + record); 
  }
  
  private InetAddress loopbackAddress() {
    return preferredAddressType().localhost();
  }
  
  protected void doResolve(String inetHost, DnsRecord[] additionals, Promise<InetAddress> promise, DnsCache resolveCache) throws Exception {
    if (inetHost == null || inetHost.isEmpty()) {
      promise.setSuccess(loopbackAddress());
      return;
    } 
    byte[] bytes = NetUtil.createByteArrayFromIpAddressString(inetHost);
    if (bytes != null) {
      promise.setSuccess(InetAddress.getByAddress(bytes));
      return;
    } 
    String hostname = hostname(inetHost);
    InetAddress hostsFileEntry = resolveHostsFileEntry(hostname);
    if (hostsFileEntry != null) {
      promise.setSuccess(hostsFileEntry);
      return;
    } 
    if (!doResolveCached(hostname, additionals, promise, resolveCache))
      doResolveUncached(hostname, additionals, promise, resolveCache); 
  }
  
  private boolean doResolveCached(String hostname, DnsRecord[] additionals, Promise<InetAddress> promise, DnsCache resolveCache) {
    List<? extends DnsCacheEntry> cachedEntries = resolveCache.get(hostname, additionals);
    if (cachedEntries == null || cachedEntries.isEmpty())
      return false; 
    Throwable cause = ((DnsCacheEntry)cachedEntries.get(0)).cause();
    if (cause == null) {
      int numEntries = cachedEntries.size();
      for (InternetProtocolFamily f : this.resolvedInternetProtocolFamilies) {
        for (int i = 0; i < numEntries; i++) {
          DnsCacheEntry e = cachedEntries.get(i);
          if (f.addressType().isInstance(e.address())) {
            trySuccess(promise, e.address());
            return true;
          } 
        } 
      } 
      return false;
    } 
    tryFailure(promise, cause);
    return true;
  }
  
  static <T> void trySuccess(Promise<T> promise, T result) {
    if (!promise.trySuccess(result))
      logger.warn("Failed to notify success ({}) to a promise: {}", result, promise); 
  }
  
  private static void tryFailure(Promise<?> promise, Throwable cause) {
    if (!promise.tryFailure(cause))
      logger.warn("Failed to notify failure to a promise: {}", promise, cause); 
  }
  
  private void doResolveUncached(String hostname, DnsRecord[] additionals, final Promise<InetAddress> promise, DnsCache resolveCache) {
    Promise<List<InetAddress>> allPromise = executor().newPromise();
    doResolveAllUncached(hostname, additionals, allPromise, resolveCache);
    allPromise.addListener((GenericFutureListener)new FutureListener<List<InetAddress>>() {
          public void operationComplete(Future<List<InetAddress>> future) {
            if (future.isSuccess()) {
              DnsNameResolver.trySuccess(promise, ((List)future.getNow()).get(0));
            } else {
              DnsNameResolver.tryFailure(promise, future.cause());
            } 
          }
        });
  }
  
  protected void doResolveAll(String inetHost, Promise<List<InetAddress>> promise) throws Exception {
    doResolveAll(inetHost, EMPTY_ADDITIONALS, promise, this.resolveCache);
  }
  
  protected void doResolveAll(String inetHost, DnsRecord[] additionals, Promise<List<InetAddress>> promise, DnsCache resolveCache) throws Exception {
    if (inetHost == null || inetHost.isEmpty()) {
      promise.setSuccess(Collections.singletonList(loopbackAddress()));
      return;
    } 
    byte[] bytes = NetUtil.createByteArrayFromIpAddressString(inetHost);
    if (bytes != null) {
      promise.setSuccess(Collections.singletonList(InetAddress.getByAddress(bytes)));
      return;
    } 
    String hostname = hostname(inetHost);
    InetAddress hostsFileEntry = resolveHostsFileEntry(hostname);
    if (hostsFileEntry != null) {
      promise.setSuccess(Collections.singletonList(hostsFileEntry));
      return;
    } 
    if (!doResolveAllCached(hostname, additionals, promise, resolveCache))
      doResolveAllUncached(hostname, additionals, promise, resolveCache); 
  }
  
  private boolean doResolveAllCached(String hostname, DnsRecord[] additionals, Promise<List<InetAddress>> promise, DnsCache resolveCache) {
    List<? extends DnsCacheEntry> cachedEntries = resolveCache.get(hostname, additionals);
    if (cachedEntries == null || cachedEntries.isEmpty())
      return false; 
    Throwable cause = ((DnsCacheEntry)cachedEntries.get(0)).cause();
    if (cause == null) {
      List<InetAddress> result = null;
      int numEntries = cachedEntries.size();
      for (InternetProtocolFamily f : this.resolvedInternetProtocolFamilies) {
        for (int i = 0; i < numEntries; i++) {
          DnsCacheEntry e = cachedEntries.get(i);
          if (f.addressType().isInstance(e.address())) {
            if (result == null)
              result = new ArrayList<InetAddress>(numEntries); 
            result.add(e.address());
          } 
        } 
      } 
      if (result != null) {
        trySuccess(promise, result);
        return true;
      } 
      return false;
    } 
    tryFailure(promise, cause);
    return true;
  }
  
  private void doResolveAllUncached(String hostname, DnsRecord[] additionals, Promise<List<InetAddress>> promise, DnsCache resolveCache) {
    DnsServerAddressStream nameServerAddrs = this.dnsServerAddressStreamProvider.nameServerAddressStream(hostname);
    (new DnsAddressResolveContext(this, hostname, additionals, nameServerAddrs, resolveCache)).resolve(promise);
  }
  
  private static String hostname(String inetHost) {
    String hostname = IDN.toASCII(inetHost);
    if (StringUtil.endsWith(inetHost, '.') && !StringUtil.endsWith(hostname, '.'))
      hostname = hostname + "."; 
    return hostname;
  }
  
  public Future<AddressedEnvelope<DnsResponse, InetSocketAddress>> query(DnsQuestion question) {
    return query(nextNameServerAddress(), question);
  }
  
  public Future<AddressedEnvelope<DnsResponse, InetSocketAddress>> query(DnsQuestion question, Iterable<DnsRecord> additionals) {
    return query(nextNameServerAddress(), question, additionals);
  }
  
  public Future<AddressedEnvelope<DnsResponse, InetSocketAddress>> query(DnsQuestion question, Promise<AddressedEnvelope<? extends DnsResponse, InetSocketAddress>> promise) {
    return query(nextNameServerAddress(), question, Collections.emptyList(), promise);
  }
  
  private InetSocketAddress nextNameServerAddress() {
    return ((DnsServerAddressStream)this.nameServerAddrStream.get()).next();
  }
  
  public Future<AddressedEnvelope<DnsResponse, InetSocketAddress>> query(InetSocketAddress nameServerAddr, DnsQuestion question) {
    return query0(nameServerAddr, question, EMPTY_ADDITIONALS, this.ch
        .eventLoop().newPromise());
  }
  
  public Future<AddressedEnvelope<DnsResponse, InetSocketAddress>> query(InetSocketAddress nameServerAddr, DnsQuestion question, Iterable<DnsRecord> additionals) {
    return query0(nameServerAddr, question, toArray(additionals, false), this.ch
        .eventLoop().newPromise());
  }
  
  public Future<AddressedEnvelope<DnsResponse, InetSocketAddress>> query(InetSocketAddress nameServerAddr, DnsQuestion question, Promise<AddressedEnvelope<? extends DnsResponse, InetSocketAddress>> promise) {
    return query0(nameServerAddr, question, EMPTY_ADDITIONALS, promise);
  }
  
  public Future<AddressedEnvelope<DnsResponse, InetSocketAddress>> query(InetSocketAddress nameServerAddr, DnsQuestion question, Iterable<DnsRecord> additionals, Promise<AddressedEnvelope<? extends DnsResponse, InetSocketAddress>> promise) {
    return query0(nameServerAddr, question, toArray(additionals, false), promise);
  }
  
  public static boolean isTransportOrTimeoutError(Throwable cause) {
    return (cause != null && cause.getCause() instanceof DnsNameResolverException);
  }
  
  public static boolean isTimeoutError(Throwable cause) {
    return (cause != null && cause.getCause() instanceof DnsNameResolverTimeoutException);
  }
  
  final Future<AddressedEnvelope<DnsResponse, InetSocketAddress>> query0(InetSocketAddress nameServerAddr, DnsQuestion question, DnsRecord[] additionals, Promise<AddressedEnvelope<? extends DnsResponse, InetSocketAddress>> promise) {
    return query0(nameServerAddr, question, additionals, this.ch.newPromise(), promise);
  }
  
  final Future<AddressedEnvelope<DnsResponse, InetSocketAddress>> query0(InetSocketAddress nameServerAddr, DnsQuestion question, DnsRecord[] additionals, ChannelPromise writePromise, Promise<AddressedEnvelope<? extends DnsResponse, InetSocketAddress>> promise) {
    assert !writePromise.isVoid();
    Promise<AddressedEnvelope<DnsResponse, InetSocketAddress>> castPromise = cast(
        (Promise)ObjectUtil.checkNotNull(promise, "promise"));
    try {
      (new DnsQueryContext(this, nameServerAddr, question, additionals, castPromise)).query(writePromise);
      return (Future<AddressedEnvelope<DnsResponse, InetSocketAddress>>)castPromise;
    } catch (Exception e) {
      return (Future<AddressedEnvelope<DnsResponse, InetSocketAddress>>)castPromise.setFailure(e);
    } 
  }
  
  private static Promise<AddressedEnvelope<DnsResponse, InetSocketAddress>> cast(Promise<?> promise) {
    return (Promise)promise;
  }
  
  private final class DnsResponseHandler extends ChannelInboundHandlerAdapter {
    private final Promise<Channel> channelActivePromise;
    
    DnsResponseHandler(Promise<Channel> channelActivePromise) {
      this.channelActivePromise = channelActivePromise;
    }
    
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
      try {
        DatagramDnsResponse res = (DatagramDnsResponse)msg;
        int queryId = res.id();
        if (DnsNameResolver.logger.isDebugEnabled())
          DnsNameResolver.logger.debug("{} RECEIVED: [{}: {}], {}", new Object[] { this.this$0.ch, Integer.valueOf(queryId), res.sender(), res }); 
        DnsQueryContext qCtx = DnsNameResolver.this.queryContextManager.get(res.sender(), queryId);
        if (qCtx == null) {
          DnsNameResolver.logger.warn("{} Received a DNS response with an unknown ID: {}", DnsNameResolver.this.ch, Integer.valueOf(queryId));
          return;
        } 
        qCtx.finish((AddressedEnvelope<? extends DnsResponse, InetSocketAddress>)res);
      } finally {
        ReferenceCountUtil.safeRelease(msg);
      } 
    }
    
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
      super.channelActive(ctx);
      this.channelActivePromise.setSuccess(ctx.channel());
    }
    
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
      DnsNameResolver.logger.warn("{} Unexpected exception: ", DnsNameResolver.this.ch, cause);
    }
  }
}
