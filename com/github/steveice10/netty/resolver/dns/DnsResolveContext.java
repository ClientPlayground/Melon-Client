package com.github.steveice10.netty.resolver.dns;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufHolder;
import com.github.steveice10.netty.channel.AddressedEnvelope;
import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.channel.ChannelPromise;
import com.github.steveice10.netty.channel.EventLoop;
import com.github.steveice10.netty.handler.codec.CorruptedFrameException;
import com.github.steveice10.netty.handler.codec.dns.DefaultDnsQuestion;
import com.github.steveice10.netty.handler.codec.dns.DefaultDnsRecordDecoder;
import com.github.steveice10.netty.handler.codec.dns.DnsQuestion;
import com.github.steveice10.netty.handler.codec.dns.DnsRecord;
import com.github.steveice10.netty.handler.codec.dns.DnsRecordType;
import com.github.steveice10.netty.handler.codec.dns.DnsResponse;
import com.github.steveice10.netty.handler.codec.dns.DnsResponseCode;
import com.github.steveice10.netty.handler.codec.dns.DnsSection;
import com.github.steveice10.netty.util.ReferenceCountUtil;
import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.concurrent.FutureListener;
import com.github.steveice10.netty.util.concurrent.GenericFutureListener;
import com.github.steveice10.netty.util.concurrent.Promise;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.PlatformDependent;
import com.github.steveice10.netty.util.internal.StringUtil;
import com.github.steveice10.netty.util.internal.ThrowableUtil;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

abstract class DnsResolveContext<T> {
  private static final FutureListener<AddressedEnvelope<DnsResponse, InetSocketAddress>> RELEASE_RESPONSE = new FutureListener<AddressedEnvelope<DnsResponse, InetSocketAddress>>() {
      public void operationComplete(Future<AddressedEnvelope<DnsResponse, InetSocketAddress>> future) {
        if (future.isSuccess())
          ((AddressedEnvelope)future.getNow()).release(); 
      }
    };
  
  private static final RuntimeException NXDOMAIN_QUERY_FAILED_EXCEPTION = (RuntimeException)ThrowableUtil.unknownStackTrace(new RuntimeException("No answer found and NXDOMAIN response code returned"), DnsResolveContext.class, "onResponse(..)");
  
  private static final RuntimeException CNAME_NOT_FOUND_QUERY_FAILED_EXCEPTION = (RuntimeException)ThrowableUtil.unknownStackTrace(new RuntimeException("No matching CNAME record found"), DnsResolveContext.class, "onResponseCNAME(..)");
  
  private static final RuntimeException NO_MATCHING_RECORD_QUERY_FAILED_EXCEPTION = (RuntimeException)ThrowableUtil.unknownStackTrace(new RuntimeException("No matching record type found"), DnsResolveContext.class, "onResponseAorAAAA(..)");
  
  private static final RuntimeException UNRECOGNIZED_TYPE_QUERY_FAILED_EXCEPTION = (RuntimeException)ThrowableUtil.unknownStackTrace(new RuntimeException("Response type was unrecognized"), DnsResolveContext.class, "onResponse(..)");
  
  private static final RuntimeException NAME_SERVERS_EXHAUSTED_EXCEPTION = (RuntimeException)ThrowableUtil.unknownStackTrace(new RuntimeException("No name servers returned an answer"), DnsResolveContext.class, "tryToFinishResolve(..)");
  
  final DnsNameResolver parent;
  
  private final DnsServerAddressStream nameServerAddrs;
  
  private final String hostname;
  
  private final int dnsClass;
  
  private final DnsRecordType[] expectedTypes;
  
  private final int maxAllowedQueries;
  
  private final DnsRecord[] additionals;
  
  private final Set<Future<AddressedEnvelope<DnsResponse, InetSocketAddress>>> queriesInProgress = Collections.newSetFromMap(new IdentityHashMap<Future<AddressedEnvelope<DnsResponse, InetSocketAddress>>, Boolean>());
  
  private List<T> finalResult;
  
  private int allowedQueries;
  
  private boolean triedCNAME;
  
  DnsResolveContext(DnsNameResolver parent, String hostname, int dnsClass, DnsRecordType[] expectedTypes, DnsRecord[] additionals, DnsServerAddressStream nameServerAddrs) {
    assert expectedTypes.length > 0;
    this.parent = parent;
    this.hostname = hostname;
    this.dnsClass = dnsClass;
    this.expectedTypes = expectedTypes;
    this.additionals = additionals;
    this.nameServerAddrs = (DnsServerAddressStream)ObjectUtil.checkNotNull(nameServerAddrs, "nameServerAddrs");
    this.maxAllowedQueries = parent.maxQueriesPerResolve();
    this.allowedQueries = this.maxAllowedQueries;
  }
  
  void resolve(final Promise<List<T>> promise) {
    final String[] searchDomains = this.parent.searchDomains();
    if (searchDomains.length == 0 || this.parent.ndots() == 0 || StringUtil.endsWith(this.hostname, '.')) {
      internalResolve(promise);
    } else {
      final boolean startWithoutSearchDomain = hasNDots();
      String initialHostname = startWithoutSearchDomain ? this.hostname : (this.hostname + '.' + searchDomains[0]);
      final int initialSearchDomainIdx = startWithoutSearchDomain ? 0 : 1;
      doSearchDomainQuery(initialHostname, new FutureListener<List<T>>() {
            private int searchDomainIdx = initialSearchDomainIdx;
            
            public void operationComplete(Future<List<T>> future) {
              Throwable cause = future.cause();
              if (cause == null) {
                promise.trySuccess(future.getNow());
              } else if (DnsNameResolver.isTransportOrTimeoutError(cause)) {
                promise.tryFailure(new DnsResolveContext.SearchDomainUnknownHostException(cause, DnsResolveContext.this.hostname));
              } else if (this.searchDomainIdx < searchDomains.length) {
                DnsResolveContext.this.doSearchDomainQuery(DnsResolveContext.this.hostname + '.' + searchDomains[this.searchDomainIdx++], this);
              } else if (!startWithoutSearchDomain) {
                DnsResolveContext.this.internalResolve(promise);
              } else {
                promise.tryFailure(new DnsResolveContext.SearchDomainUnknownHostException(cause, DnsResolveContext.this.hostname));
              } 
            }
          });
    } 
  }
  
  private boolean hasNDots() {
    for (int idx = this.hostname.length() - 1, dots = 0; idx >= 0; idx--) {
      if (this.hostname.charAt(idx) == '.' && ++dots >= this.parent.ndots())
        return true; 
    } 
    return false;
  }
  
  private static final class SearchDomainUnknownHostException extends UnknownHostException {
    private static final long serialVersionUID = -8573510133644997085L;
    
    SearchDomainUnknownHostException(Throwable cause, String originalHostname) {
      super("Search domain query failed. Original hostname: '" + originalHostname + "' " + cause.getMessage());
      setStackTrace(cause.getStackTrace());
      initCause(cause.getCause());
    }
    
    public Throwable fillInStackTrace() {
      return this;
    }
  }
  
  private void doSearchDomainQuery(String hostname, FutureListener<List<T>> listener) {
    DnsResolveContext<T> nextContext = newResolverContext(this.parent, hostname, this.dnsClass, this.expectedTypes, this.additionals, this.nameServerAddrs);
    Promise<List<T>> nextPromise = this.parent.executor().newPromise();
    nextContext.internalResolve(nextPromise);
    nextPromise.addListener((GenericFutureListener)listener);
  }
  
  private void internalResolve(Promise<List<T>> promise) {
    DnsServerAddressStream nameServerAddressStream = getNameServers(this.hostname);
    int end = this.expectedTypes.length - 1;
    for (int i = 0; i < end; i++) {
      if (!query(this.hostname, this.expectedTypes[i], nameServerAddressStream.duplicate(), promise))
        return; 
    } 
    query(this.hostname, this.expectedTypes[end], nameServerAddressStream, promise);
  }
  
  private void addNameServerToCache(AuthoritativeNameServer name, InetAddress resolved, long ttl) {
    if (!name.isRootServer())
      this.parent.authoritativeDnsServerCache().cache(name.domainName(), this.additionals, resolved, ttl, this.parent.ch
          .eventLoop()); 
  }
  
  private DnsServerAddressStream getNameServersFromCache(String hostname) {
    int len = hostname.length();
    if (len == 0)
      return null; 
    if (hostname.charAt(len - 1) != '.')
      hostname = hostname + "."; 
    int idx = hostname.indexOf('.');
    if (idx == hostname.length() - 1)
      return null; 
    while (true) {
      hostname = hostname.substring(idx + 1);
      int idx2 = hostname.indexOf('.');
      if (idx2 <= 0 || idx2 == hostname.length() - 1)
        return null; 
      idx = idx2;
      List<? extends DnsCacheEntry> entries = this.parent.authoritativeDnsServerCache().get(hostname, this.additionals);
      if (entries != null && !entries.isEmpty())
        return DnsServerAddresses.sequential(new DnsCacheIterable(entries)).stream(); 
    } 
  }
  
  private final class DnsCacheIterable implements Iterable<InetSocketAddress> {
    private final List<? extends DnsCacheEntry> entries;
    
    DnsCacheIterable(List<? extends DnsCacheEntry> entries) {
      this.entries = entries;
    }
    
    public Iterator<InetSocketAddress> iterator() {
      return new Iterator<InetSocketAddress>() {
          Iterator<? extends DnsCacheEntry> entryIterator = DnsResolveContext.DnsCacheIterable.this.entries.iterator();
          
          public boolean hasNext() {
            return this.entryIterator.hasNext();
          }
          
          public InetSocketAddress next() {
            InetAddress address = ((DnsCacheEntry)this.entryIterator.next()).address();
            return new InetSocketAddress(address, DnsResolveContext.this.parent.dnsRedirectPort(address));
          }
          
          public void remove() {
            this.entryIterator.remove();
          }
        };
    }
  }
  
  private void query(DnsServerAddressStream nameServerAddrStream, int nameServerAddrStreamIndex, DnsQuestion question, Promise<List<T>> promise, Throwable cause) {
    query(nameServerAddrStream, nameServerAddrStreamIndex, question, this.parent
        .dnsQueryLifecycleObserverFactory().newDnsQueryLifecycleObserver(question), promise, cause);
  }
  
  private void query(final DnsServerAddressStream nameServerAddrStream, final int nameServerAddrStreamIndex, final DnsQuestion question, final DnsQueryLifecycleObserver queryLifecycleObserver, final Promise<List<T>> promise, Throwable cause) {
    if (nameServerAddrStreamIndex >= nameServerAddrStream.size() || this.allowedQueries == 0 || promise.isCancelled()) {
      tryToFinishResolve(nameServerAddrStream, nameServerAddrStreamIndex, question, queryLifecycleObserver, promise, cause);
      return;
    } 
    this.allowedQueries--;
    InetSocketAddress nameServerAddr = nameServerAddrStream.next();
    ChannelPromise writePromise = this.parent.ch.newPromise();
    Future<AddressedEnvelope<DnsResponse, InetSocketAddress>> f = this.parent.query0(nameServerAddr, question, this.additionals, writePromise, this.parent.ch
        
        .eventLoop().newPromise());
    this.queriesInProgress.add(f);
    queryLifecycleObserver.queryWritten(nameServerAddr, (ChannelFuture)writePromise);
    f.addListener((GenericFutureListener)new FutureListener<AddressedEnvelope<DnsResponse, InetSocketAddress>>() {
          public void operationComplete(Future<AddressedEnvelope<DnsResponse, InetSocketAddress>> future) {
            DnsResolveContext.this.queriesInProgress.remove(future);
            if (promise.isDone() || future.isCancelled()) {
              queryLifecycleObserver.queryCancelled(DnsResolveContext.this.allowedQueries);
              AddressedEnvelope<DnsResponse, InetSocketAddress> result = (AddressedEnvelope<DnsResponse, InetSocketAddress>)future.getNow();
              if (result != null)
                result.release(); 
              return;
            } 
            Throwable queryCause = future.cause();
            try {
              if (queryCause == null) {
                DnsResolveContext.this.onResponse(nameServerAddrStream, nameServerAddrStreamIndex, question, (AddressedEnvelope)future.getNow(), queryLifecycleObserver, promise);
              } else {
                queryLifecycleObserver.queryFailed(queryCause);
                DnsResolveContext.this.query(nameServerAddrStream, nameServerAddrStreamIndex + 1, question, promise, queryCause);
              } 
            } finally {
              DnsResolveContext.this.tryToFinishResolve(nameServerAddrStream, nameServerAddrStreamIndex, question, NoopDnsQueryLifecycleObserver.INSTANCE, promise, queryCause);
            } 
          }
        });
  }
  
  private void onResponse(DnsServerAddressStream nameServerAddrStream, int nameServerAddrStreamIndex, DnsQuestion question, AddressedEnvelope<DnsResponse, InetSocketAddress> envelope, DnsQueryLifecycleObserver queryLifecycleObserver, Promise<List<T>> promise) {
    try {
      DnsResponse res = (DnsResponse)envelope.content();
      DnsResponseCode code = res.code();
      if (code == DnsResponseCode.NOERROR) {
        if (handleRedirect(question, envelope, queryLifecycleObserver, promise))
          return; 
        DnsRecordType type = question.type();
        if (type == DnsRecordType.CNAME) {
          onResponseCNAME(question, buildAliasMap((DnsResponse)envelope.content()), queryLifecycleObserver, promise);
          return;
        } 
        for (DnsRecordType expectedType : this.expectedTypes) {
          if (type == expectedType) {
            onExpectedResponse(question, envelope, queryLifecycleObserver, promise);
            return;
          } 
        } 
        queryLifecycleObserver.queryFailed(UNRECOGNIZED_TYPE_QUERY_FAILED_EXCEPTION);
        return;
      } 
      if (code != DnsResponseCode.NXDOMAIN) {
        query(nameServerAddrStream, nameServerAddrStreamIndex + 1, question, queryLifecycleObserver
            .queryNoAnswer(code), promise, null);
      } else {
        queryLifecycleObserver.queryFailed(NXDOMAIN_QUERY_FAILED_EXCEPTION);
      } 
    } finally {
      ReferenceCountUtil.safeRelease(envelope);
    } 
  }
  
  private boolean handleRedirect(DnsQuestion question, AddressedEnvelope<DnsResponse, InetSocketAddress> envelope, DnsQueryLifecycleObserver queryLifecycleObserver, Promise<List<T>> promise) {
    DnsResponse res = (DnsResponse)envelope.content();
    if (res.count(DnsSection.ANSWER) == 0) {
      AuthoritativeNameServerList serverNames = extractAuthoritativeNameServers(question.name(), res);
      if (serverNames != null) {
        List<InetSocketAddress> nameServers = new ArrayList<InetSocketAddress>(serverNames.size());
        int additionalCount = res.count(DnsSection.ADDITIONAL);
        for (int i = 0; i < additionalCount; i++) {
          DnsRecord r = res.recordAt(DnsSection.ADDITIONAL, i);
          if ((r.type() != DnsRecordType.A || this.parent.supportsARecords()) && (r
            .type() != DnsRecordType.AAAA || this.parent.supportsAAAARecords())) {
            String recordName = r.name();
            AuthoritativeNameServer authoritativeNameServer = serverNames.remove(recordName);
            if (authoritativeNameServer != null) {
              InetAddress resolved = DnsAddressDecoder.decodeAddress(r, recordName, this.parent.isDecodeIdn());
              if (resolved != null) {
                nameServers.add(new InetSocketAddress(resolved, this.parent.dnsRedirectPort(resolved)));
                addNameServerToCache(authoritativeNameServer, resolved, r.timeToLive());
              } 
            } 
          } 
        } 
        if (!nameServers.isEmpty()) {
          query(this.parent.uncachedRedirectDnsServerStream(nameServers), 0, question, queryLifecycleObserver
              .queryRedirected(Collections.unmodifiableList(nameServers)), promise, null);
          return true;
        } 
      } 
    } 
    return false;
  }
  
  private static AuthoritativeNameServerList extractAuthoritativeNameServers(String questionName, DnsResponse res) {
    int authorityCount = res.count(DnsSection.AUTHORITY);
    if (authorityCount == 0)
      return null; 
    AuthoritativeNameServerList serverNames = new AuthoritativeNameServerList(questionName);
    for (int i = 0; i < authorityCount; i++)
      serverNames.add(res.recordAt(DnsSection.AUTHORITY, i)); 
    return serverNames;
  }
  
  private void onExpectedResponse(DnsQuestion question, AddressedEnvelope<DnsResponse, InetSocketAddress> envelope, DnsQueryLifecycleObserver queryLifecycleObserver, Promise<List<T>> promise) {
    DnsResponse response = (DnsResponse)envelope.content();
    Map<String, String> cnames = buildAliasMap(response);
    int answerCount = response.count(DnsSection.ANSWER);
    boolean found = false;
    for (int i = 0; i < answerCount; i++) {
      DnsRecord r = response.recordAt(DnsSection.ANSWER, i);
      DnsRecordType type = r.type();
      boolean matches = false;
      for (DnsRecordType expectedType : this.expectedTypes) {
        if (type == expectedType) {
          matches = true;
          break;
        } 
      } 
      if (!matches)
        continue; 
      String questionName = question.name().toLowerCase(Locale.US);
      String recordName = r.name().toLowerCase(Locale.US);
      if (!recordName.equals(questionName)) {
        String resolved = questionName;
        do {
          resolved = cnames.get(resolved);
          if (recordName.equals(resolved))
            break; 
        } while (resolved != null);
        if (resolved == null)
          continue; 
      } 
      T converted = convertRecord(r, this.hostname, this.additionals, this.parent.ch.eventLoop());
      if (converted != null) {
        if (this.finalResult == null)
          this.finalResult = new ArrayList<T>(8); 
        this.finalResult.add(converted);
        cache(this.hostname, this.additionals, r, converted);
        found = true;
      } 
      continue;
    } 
    if (cnames.isEmpty()) {
      if (found) {
        queryLifecycleObserver.querySucceed();
        return;
      } 
      queryLifecycleObserver.queryFailed(NO_MATCHING_RECORD_QUERY_FAILED_EXCEPTION);
    } else {
      queryLifecycleObserver.querySucceed();
      onResponseCNAME(question, cnames, this.parent
          .dnsQueryLifecycleObserverFactory().newDnsQueryLifecycleObserver(question), promise);
    } 
  }
  
  private void onResponseCNAME(DnsQuestion question, Map<String, String> cnames, DnsQueryLifecycleObserver queryLifecycleObserver, Promise<List<T>> promise) {
    String resolved = question.name().toLowerCase(Locale.US);
    boolean found = false;
    while (!cnames.isEmpty()) {
      String next = cnames.remove(resolved);
      if (next != null) {
        found = true;
        resolved = next;
      } 
    } 
    if (found) {
      followCname(question, resolved, queryLifecycleObserver, promise);
    } else {
      queryLifecycleObserver.queryFailed(CNAME_NOT_FOUND_QUERY_FAILED_EXCEPTION);
    } 
  }
  
  private static Map<String, String> buildAliasMap(DnsResponse response) {
    int answerCount = response.count(DnsSection.ANSWER);
    Map<String, String> cnames = null;
    for (int i = 0; i < answerCount; i++) {
      DnsRecord r = response.recordAt(DnsSection.ANSWER, i);
      DnsRecordType type = r.type();
      if (type == DnsRecordType.CNAME)
        if (r instanceof com.github.steveice10.netty.handler.codec.dns.DnsRawRecord) {
          ByteBuf recordContent = ((ByteBufHolder)r).content();
          String domainName = decodeDomainName(recordContent);
          if (domainName != null) {
            if (cnames == null)
              cnames = new HashMap<String, String>(Math.min(8, answerCount)); 
            cnames.put(r.name().toLowerCase(Locale.US), domainName.toLowerCase(Locale.US));
          } 
        }  
    } 
    return (cnames != null) ? cnames : Collections.<String, String>emptyMap();
  }
  
  private void tryToFinishResolve(DnsServerAddressStream nameServerAddrStream, int nameServerAddrStreamIndex, DnsQuestion question, DnsQueryLifecycleObserver queryLifecycleObserver, Promise<List<T>> promise, Throwable cause) {
    if (!this.queriesInProgress.isEmpty()) {
      queryLifecycleObserver.queryCancelled(this.allowedQueries);
      return;
    } 
    if (this.finalResult == null) {
      if (nameServerAddrStreamIndex < nameServerAddrStream.size()) {
        if (queryLifecycleObserver == NoopDnsQueryLifecycleObserver.INSTANCE) {
          query(nameServerAddrStream, nameServerAddrStreamIndex + 1, question, promise, cause);
        } else {
          query(nameServerAddrStream, nameServerAddrStreamIndex + 1, question, queryLifecycleObserver, promise, cause);
        } 
        return;
      } 
      queryLifecycleObserver.queryFailed(NAME_SERVERS_EXHAUSTED_EXCEPTION);
      if (cause == null && !this.triedCNAME) {
        this.triedCNAME = true;
        query(this.hostname, DnsRecordType.CNAME, getNameServers(this.hostname), promise);
        return;
      } 
    } else {
      queryLifecycleObserver.queryCancelled(this.allowedQueries);
    } 
    finishResolve(promise, cause);
  }
  
  private void finishResolve(Promise<List<T>> promise, Throwable cause) {
    if (!this.queriesInProgress.isEmpty()) {
      Iterator<Future<AddressedEnvelope<DnsResponse, InetSocketAddress>>> i = this.queriesInProgress.iterator();
      while (i.hasNext()) {
        Future<AddressedEnvelope<DnsResponse, InetSocketAddress>> f = i.next();
        i.remove();
        if (!f.cancel(false))
          f.addListener((GenericFutureListener)RELEASE_RESPONSE); 
      } 
    } 
    if (this.finalResult != null) {
      DnsNameResolver.trySuccess(promise, filterResults(this.finalResult));
      return;
    } 
    int tries = this.maxAllowedQueries - this.allowedQueries;
    StringBuilder buf = new StringBuilder(64);
    buf.append("failed to resolve '").append(this.hostname).append('\'');
    if (tries > 1)
      if (tries < this.maxAllowedQueries) {
        buf.append(" after ")
          .append(tries)
          .append(" queries ");
      } else {
        buf.append(". Exceeded max queries per resolve ")
          .append(this.maxAllowedQueries)
          .append(' ');
      }  
    UnknownHostException unknownHostException = new UnknownHostException(buf.toString());
    if (cause == null) {
      cache(this.hostname, this.additionals, unknownHostException);
    } else {
      unknownHostException.initCause(cause);
    } 
    promise.tryFailure(unknownHostException);
  }
  
  static String decodeDomainName(ByteBuf in) {
    in.markReaderIndex();
    try {
      return DefaultDnsRecordDecoder.decodeName(in);
    } catch (CorruptedFrameException e) {
      return null;
    } finally {
      in.resetReaderIndex();
    } 
  }
  
  private DnsServerAddressStream getNameServers(String hostname) {
    DnsServerAddressStream stream = getNameServersFromCache(hostname);
    return (stream == null) ? this.nameServerAddrs.duplicate() : stream;
  }
  
  private void followCname(DnsQuestion question, String cname, DnsQueryLifecycleObserver queryLifecycleObserver, Promise<List<T>> promise) {
    DnsQuestion cnameQuestion;
    DnsServerAddressStream stream = getNameServers(cname);
    try {
      cnameQuestion = newQuestion(cname, question.type());
    } catch (Throwable cause) {
      queryLifecycleObserver.queryFailed(cause);
      PlatformDependent.throwException(cause);
      return;
    } 
    query(stream, 0, cnameQuestion, queryLifecycleObserver.queryCNAMEd(cnameQuestion), promise, null);
  }
  
  private boolean query(String hostname, DnsRecordType type, DnsServerAddressStream dnsServerAddressStream, Promise<List<T>> promise) {
    DnsQuestion question = newQuestion(hostname, type);
    if (question == null)
      return false; 
    query(dnsServerAddressStream, 0, question, promise, null);
    return true;
  }
  
  private DnsQuestion newQuestion(String hostname, DnsRecordType type) {
    try {
      return (DnsQuestion)new DefaultDnsQuestion(hostname, type, this.dnsClass);
    } catch (IllegalArgumentException e) {
      return null;
    } 
  }
  
  abstract DnsResolveContext<T> newResolverContext(DnsNameResolver paramDnsNameResolver, String paramString, int paramInt, DnsRecordType[] paramArrayOfDnsRecordType, DnsRecord[] paramArrayOfDnsRecord, DnsServerAddressStream paramDnsServerAddressStream);
  
  abstract T convertRecord(DnsRecord paramDnsRecord, String paramString, DnsRecord[] paramArrayOfDnsRecord, EventLoop paramEventLoop);
  
  abstract List<T> filterResults(List<T> paramList);
  
  abstract void cache(String paramString, DnsRecord[] paramArrayOfDnsRecord, DnsRecord paramDnsRecord, T paramT);
  
  abstract void cache(String paramString, DnsRecord[] paramArrayOfDnsRecord, UnknownHostException paramUnknownHostException);
  
  private static final class AuthoritativeNameServerList {
    private final String questionName;
    
    private DnsResolveContext.AuthoritativeNameServer head;
    
    private int count;
    
    AuthoritativeNameServerList(String questionName) {
      this.questionName = questionName.toLowerCase(Locale.US);
    }
    
    void add(DnsRecord r) {
      if (r.type() != DnsRecordType.NS || !(r instanceof com.github.steveice10.netty.handler.codec.dns.DnsRawRecord))
        return; 
      if (this.questionName.length() < r.name().length())
        return; 
      String recordName = r.name().toLowerCase(Locale.US);
      int dots = 0;
      for (int a = recordName.length() - 1, b = this.questionName.length() - 1; a >= 0; a--, b--) {
        char c = recordName.charAt(a);
        if (this.questionName.charAt(b) != c)
          return; 
        if (c == '.')
          dots++; 
      } 
      if (this.head != null && this.head.dots > dots)
        return; 
      ByteBuf recordContent = ((ByteBufHolder)r).content();
      String domainName = DnsResolveContext.decodeDomainName(recordContent);
      if (domainName == null)
        return; 
      if (this.head == null || this.head.dots < dots) {
        this.count = 1;
        this.head = new DnsResolveContext.AuthoritativeNameServer(dots, recordName, domainName);
      } else if (this.head.dots == dots) {
        DnsResolveContext.AuthoritativeNameServer serverName = this.head;
        while (serverName.next != null)
          serverName = serverName.next; 
        serverName.next = new DnsResolveContext.AuthoritativeNameServer(dots, recordName, domainName);
        this.count++;
      } 
    }
    
    DnsResolveContext.AuthoritativeNameServer remove(String nsName) {
      DnsResolveContext.AuthoritativeNameServer serverName = this.head;
      while (serverName != null) {
        if (!serverName.removed && serverName.nsName.equalsIgnoreCase(nsName)) {
          serverName.removed = true;
          return serverName;
        } 
        serverName = serverName.next;
      } 
      return null;
    }
    
    int size() {
      return this.count;
    }
  }
  
  static final class AuthoritativeNameServer {
    final int dots;
    
    final String nsName;
    
    final String domainName;
    
    AuthoritativeNameServer next;
    
    boolean removed;
    
    AuthoritativeNameServer(int dots, String domainName, String nsName) {
      this.dots = dots;
      this.nsName = nsName;
      this.domainName = domainName;
    }
    
    boolean isRootServer() {
      return (this.dots == 1);
    }
    
    String domainName() {
      return this.domainName;
    }
  }
}
