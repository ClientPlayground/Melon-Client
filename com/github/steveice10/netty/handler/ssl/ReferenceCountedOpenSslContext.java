package com.github.steveice10.netty.handler.ssl;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.internal.tcnative.CertificateVerifier;
import com.github.steveice10.netty.internal.tcnative.SSL;
import com.github.steveice10.netty.internal.tcnative.SSLContext;
import com.github.steveice10.netty.util.AbstractReferenceCounted;
import com.github.steveice10.netty.util.ReferenceCounted;
import com.github.steveice10.netty.util.ResourceLeakDetector;
import com.github.steveice10.netty.util.ResourceLeakDetectorFactory;
import com.github.steveice10.netty.util.ResourceLeakTracker;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.PlatformDependent;
import com.github.steveice10.netty.util.internal.SystemPropertyUtil;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.security.AccessController;
import java.security.PrivateKey;
import java.security.PrivilegedAction;
import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;

public abstract class ReferenceCountedOpenSslContext extends SslContext implements ReferenceCounted {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(ReferenceCountedOpenSslContext.class);
  
  private static final int DEFAULT_BIO_NON_APPLICATION_BUFFER_SIZE = ((Integer)AccessController.<Integer>doPrivileged(new PrivilegedAction<Integer>() {
        public Integer run() {
          return Integer.valueOf(Math.max(1, 
                SystemPropertyUtil.getInt("com.github.steveice10.netty.handler.ssl.openssl.bioNonApplicationBufferSize", 2048)));
        }
      })).intValue();
  
  private static final Integer DH_KEY_LENGTH;
  
  private static final ResourceLeakDetector<ReferenceCountedOpenSslContext> leakDetector = ResourceLeakDetectorFactory.instance().newResourceLeakDetector(ReferenceCountedOpenSslContext.class);
  
  protected static final int VERIFY_DEPTH = 10;
  
  protected long ctx;
  
  private final List<String> unmodifiableCiphers;
  
  private final long sessionCacheSize;
  
  private final long sessionTimeout;
  
  private final OpenSslApplicationProtocolNegotiator apn;
  
  private final int mode;
  
  private final ResourceLeakTracker<ReferenceCountedOpenSslContext> leak;
  
  private final AbstractReferenceCounted refCnt = new AbstractReferenceCounted() {
      public ReferenceCounted touch(Object hint) {
        if (ReferenceCountedOpenSslContext.this.leak != null)
          ReferenceCountedOpenSslContext.this.leak.record(hint); 
        return ReferenceCountedOpenSslContext.this;
      }
      
      protected void deallocate() {
        ReferenceCountedOpenSslContext.this.destroy();
        if (ReferenceCountedOpenSslContext.this.leak != null) {
          boolean closed = ReferenceCountedOpenSslContext.this.leak.close(ReferenceCountedOpenSslContext.this);
          assert closed;
        } 
      }
    };
  
  final Certificate[] keyCertChain;
  
  final ClientAuth clientAuth;
  
  final String[] protocols;
  
  final boolean enableOcsp;
  
  final OpenSslEngineMap engineMap = new DefaultOpenSslEngineMap();
  
  final ReadWriteLock ctxLock = new ReentrantReadWriteLock();
  
  private volatile int bioNonApplicationBufferSize = DEFAULT_BIO_NON_APPLICATION_BUFFER_SIZE;
  
  static final OpenSslApplicationProtocolNegotiator NONE_PROTOCOL_NEGOTIATOR = new OpenSslApplicationProtocolNegotiator() {
      public ApplicationProtocolConfig.Protocol protocol() {
        return ApplicationProtocolConfig.Protocol.NONE;
      }
      
      public List<String> protocols() {
        return Collections.emptyList();
      }
      
      public ApplicationProtocolConfig.SelectorFailureBehavior selectorFailureBehavior() {
        return ApplicationProtocolConfig.SelectorFailureBehavior.CHOOSE_MY_LAST_PROTOCOL;
      }
      
      public ApplicationProtocolConfig.SelectedListenerFailureBehavior selectedListenerFailureBehavior() {
        return ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT;
      }
    };
  
  static {
    Integer dhLen = null;
    try {
      String dhKeySize = AccessController.<String>doPrivileged(new PrivilegedAction<String>() {
            public String run() {
              return SystemPropertyUtil.get("jdk.tls.ephemeralDHKeySize");
            }
          });
      if (dhKeySize != null)
        try {
          dhLen = Integer.valueOf(dhKeySize);
        } catch (NumberFormatException e) {
          logger.debug("ReferenceCountedOpenSslContext supports -Djdk.tls.ephemeralDHKeySize={int}, but got: " + dhKeySize);
        }  
    } catch (Throwable throwable) {}
    DH_KEY_LENGTH = dhLen;
  }
  
  ReferenceCountedOpenSslContext(Iterable<String> ciphers, CipherSuiteFilter cipherFilter, ApplicationProtocolConfig apnCfg, long sessionCacheSize, long sessionTimeout, int mode, Certificate[] keyCertChain, ClientAuth clientAuth, String[] protocols, boolean startTls, boolean enableOcsp, boolean leakDetection) throws SSLException {
    this(ciphers, cipherFilter, toNegotiator(apnCfg), sessionCacheSize, sessionTimeout, mode, keyCertChain, clientAuth, protocols, startTls, enableOcsp, leakDetection);
  }
  
  ReferenceCountedOpenSslContext(Iterable<String> ciphers, CipherSuiteFilter cipherFilter, OpenSslApplicationProtocolNegotiator apn, long sessionCacheSize, long sessionTimeout, int mode, Certificate[] keyCertChain, ClientAuth clientAuth, String[] protocols, boolean startTls, boolean enableOcsp, boolean leakDetection) throws SSLException {
    super(startTls);
    OpenSsl.ensureAvailability();
    if (enableOcsp && !OpenSsl.isOcspSupported())
      throw new IllegalStateException("OCSP is not supported."); 
    if (mode != 1 && mode != 0)
      throw new IllegalArgumentException("mode most be either SSL.SSL_MODE_SERVER or SSL.SSL_MODE_CLIENT"); 
    this.leak = leakDetection ? leakDetector.track(this) : null;
    this.mode = mode;
    this.clientAuth = isServer() ? (ClientAuth)ObjectUtil.checkNotNull(clientAuth, "clientAuth") : ClientAuth.NONE;
    this.protocols = protocols;
    this.enableOcsp = enableOcsp;
    this.keyCertChain = (keyCertChain == null) ? null : (Certificate[])keyCertChain.clone();
    this.unmodifiableCiphers = Arrays.asList(((CipherSuiteFilter)ObjectUtil.checkNotNull(cipherFilter, "cipherFilter")).filterCipherSuites(ciphers, OpenSsl.DEFAULT_CIPHERS, 
          OpenSsl.availableJavaCipherSuites()));
    this.apn = (OpenSslApplicationProtocolNegotiator)ObjectUtil.checkNotNull(apn, "apn");
    boolean success = false;
    try {
      try {
        this.ctx = SSLContext.make(31, mode);
      } catch (Exception e) {
        throw new SSLException("failed to create an SSL_CTX", e);
      } 
      SSLContext.setOptions(this.ctx, SSLContext.getOptions(this.ctx) | SSL.SSL_OP_NO_SSLv2 | SSL.SSL_OP_NO_SSLv3 | SSL.SSL_OP_CIPHER_SERVER_PREFERENCE | SSL.SSL_OP_NO_COMPRESSION | SSL.SSL_OP_NO_TICKET);
      SSLContext.setMode(this.ctx, SSLContext.getMode(this.ctx) | SSL.SSL_MODE_ACCEPT_MOVING_WRITE_BUFFER);
      if (DH_KEY_LENGTH != null)
        SSLContext.setTmpDHLength(this.ctx, DH_KEY_LENGTH.intValue()); 
      try {
        SSLContext.setCipherSuite(this.ctx, CipherSuiteConverter.toOpenSsl(this.unmodifiableCiphers));
      } catch (SSLException e) {
        throw e;
      } catch (Exception e) {
        throw new SSLException("failed to set cipher suite: " + this.unmodifiableCiphers, e);
      } 
      List<String> nextProtoList = apn.protocols();
      if (!nextProtoList.isEmpty()) {
        String[] appProtocols = nextProtoList.<String>toArray(new String[nextProtoList.size()]);
        int selectorBehavior = opensslSelectorFailureBehavior(apn.selectorFailureBehavior());
        switch (apn.protocol()) {
          case CHOOSE_MY_LAST_PROTOCOL:
            SSLContext.setNpnProtos(this.ctx, appProtocols, selectorBehavior);
            break;
          case ACCEPT:
            SSLContext.setAlpnProtos(this.ctx, appProtocols, selectorBehavior);
            break;
          case null:
            SSLContext.setNpnProtos(this.ctx, appProtocols, selectorBehavior);
            SSLContext.setAlpnProtos(this.ctx, appProtocols, selectorBehavior);
            break;
          default:
            throw new Error();
        } 
      } 
      if (sessionCacheSize <= 0L)
        sessionCacheSize = SSLContext.setSessionCacheSize(this.ctx, 20480L); 
      this.sessionCacheSize = sessionCacheSize;
      SSLContext.setSessionCacheSize(this.ctx, sessionCacheSize);
      if (sessionTimeout <= 0L)
        sessionTimeout = SSLContext.setSessionCacheTimeout(this.ctx, 300L); 
      this.sessionTimeout = sessionTimeout;
      SSLContext.setSessionCacheTimeout(this.ctx, sessionTimeout);
      if (enableOcsp)
        SSLContext.enableOcsp(this.ctx, isClient()); 
      success = true;
    } finally {
      if (!success)
        release(); 
    } 
  }
  
  private static int opensslSelectorFailureBehavior(ApplicationProtocolConfig.SelectorFailureBehavior behavior) {
    switch (behavior) {
      case CHOOSE_MY_LAST_PROTOCOL:
        return 0;
      case ACCEPT:
        return 1;
    } 
    throw new Error();
  }
  
  public final List<String> cipherSuites() {
    return this.unmodifiableCiphers;
  }
  
  public final long sessionCacheSize() {
    return this.sessionCacheSize;
  }
  
  public final long sessionTimeout() {
    return this.sessionTimeout;
  }
  
  public ApplicationProtocolNegotiator applicationProtocolNegotiator() {
    return this.apn;
  }
  
  public final boolean isClient() {
    return (this.mode == 0);
  }
  
  public final SSLEngine newEngine(ByteBufAllocator alloc, String peerHost, int peerPort) {
    return newEngine0(alloc, peerHost, peerPort, true);
  }
  
  protected final SslHandler newHandler(ByteBufAllocator alloc, boolean startTls) {
    return new SslHandler(newEngine0(alloc, null, -1, false), startTls);
  }
  
  protected final SslHandler newHandler(ByteBufAllocator alloc, String peerHost, int peerPort, boolean startTls) {
    return new SslHandler(newEngine0(alloc, peerHost, peerPort, false), startTls);
  }
  
  SSLEngine newEngine0(ByteBufAllocator alloc, String peerHost, int peerPort, boolean jdkCompatibilityMode) {
    return new ReferenceCountedOpenSslEngine(this, alloc, peerHost, peerPort, jdkCompatibilityMode, true);
  }
  
  public final SSLEngine newEngine(ByteBufAllocator alloc) {
    return newEngine(alloc, null, -1);
  }
  
  @Deprecated
  public final long context() {
    Lock readerLock = this.ctxLock.readLock();
    readerLock.lock();
    try {
      return this.ctx;
    } finally {
      readerLock.unlock();
    } 
  }
  
  @Deprecated
  public final OpenSslSessionStats stats() {
    return sessionContext().stats();
  }
  
  @Deprecated
  public void setRejectRemoteInitiatedRenegotiation(boolean rejectRemoteInitiatedRenegotiation) {
    if (!rejectRemoteInitiatedRenegotiation)
      throw new UnsupportedOperationException("Renegotiation is not supported"); 
  }
  
  @Deprecated
  public boolean getRejectRemoteInitiatedRenegotiation() {
    return true;
  }
  
  public void setBioNonApplicationBufferSize(int bioNonApplicationBufferSize) {
    this
      .bioNonApplicationBufferSize = ObjectUtil.checkPositiveOrZero(bioNonApplicationBufferSize, "bioNonApplicationBufferSize");
  }
  
  public int getBioNonApplicationBufferSize() {
    return this.bioNonApplicationBufferSize;
  }
  
  @Deprecated
  public final void setTicketKeys(byte[] keys) {
    sessionContext().setTicketKeys(keys);
  }
  
  @Deprecated
  public final long sslCtxPointer() {
    Lock readerLock = this.ctxLock.readLock();
    readerLock.lock();
    try {
      return this.ctx;
    } finally {
      readerLock.unlock();
    } 
  }
  
  private void destroy() {
    Lock writerLock = this.ctxLock.writeLock();
    writerLock.lock();
    try {
      if (this.ctx != 0L) {
        if (this.enableOcsp)
          SSLContext.disableOcsp(this.ctx); 
        SSLContext.free(this.ctx);
        this.ctx = 0L;
      } 
    } finally {
      writerLock.unlock();
    } 
  }
  
  protected static X509Certificate[] certificates(byte[][] chain) {
    X509Certificate[] peerCerts = new X509Certificate[chain.length];
    for (int i = 0; i < peerCerts.length; i++)
      peerCerts[i] = new OpenSslX509Certificate(chain[i]); 
    return peerCerts;
  }
  
  protected static X509TrustManager chooseTrustManager(TrustManager[] managers) {
    for (TrustManager m : managers) {
      if (m instanceof X509TrustManager)
        return (X509TrustManager)m; 
    } 
    throw new IllegalStateException("no X509TrustManager found");
  }
  
  protected static X509KeyManager chooseX509KeyManager(KeyManager[] kms) {
    for (KeyManager km : kms) {
      if (km instanceof X509KeyManager)
        return (X509KeyManager)km; 
    } 
    throw new IllegalStateException("no X509KeyManager found");
  }
  
  static OpenSslApplicationProtocolNegotiator toNegotiator(ApplicationProtocolConfig config) {
    if (config == null)
      return NONE_PROTOCOL_NEGOTIATOR; 
    switch (config.protocol()) {
      case null:
        return NONE_PROTOCOL_NEGOTIATOR;
      case CHOOSE_MY_LAST_PROTOCOL:
      case ACCEPT:
      case null:
        switch (config.selectedListenerFailureBehavior()) {
          case CHOOSE_MY_LAST_PROTOCOL:
          case ACCEPT:
            switch (config.selectorFailureBehavior()) {
              case CHOOSE_MY_LAST_PROTOCOL:
              case ACCEPT:
                return new OpenSslDefaultApplicationProtocolNegotiator(config);
            } 
            throw new UnsupportedOperationException("OpenSSL provider does not support " + config
                
                .selectorFailureBehavior() + " behavior");
        } 
        throw new UnsupportedOperationException("OpenSSL provider does not support " + config
            
            .selectedListenerFailureBehavior() + " behavior");
    } 
    throw new Error();
  }
  
  static boolean useExtendedTrustManager(X509TrustManager trustManager) {
    return (PlatformDependent.javaVersion() >= 7 && trustManager instanceof javax.net.ssl.X509ExtendedTrustManager);
  }
  
  static boolean useExtendedKeyManager(X509KeyManager keyManager) {
    return (PlatformDependent.javaVersion() >= 7 && keyManager instanceof javax.net.ssl.X509ExtendedKeyManager);
  }
  
  public final int refCnt() {
    return this.refCnt.refCnt();
  }
  
  public final ReferenceCounted retain() {
    this.refCnt.retain();
    return this;
  }
  
  public final ReferenceCounted retain(int increment) {
    this.refCnt.retain(increment);
    return this;
  }
  
  public final ReferenceCounted touch() {
    this.refCnt.touch();
    return this;
  }
  
  public final ReferenceCounted touch(Object hint) {
    this.refCnt.touch(hint);
    return this;
  }
  
  public final boolean release() {
    return this.refCnt.release();
  }
  
  public final boolean release(int decrement) {
    return this.refCnt.release(decrement);
  }
  
  static abstract class AbstractCertificateVerifier extends CertificateVerifier {
    private final OpenSslEngineMap engineMap;
    
    AbstractCertificateVerifier(OpenSslEngineMap engineMap) {
      this.engineMap = engineMap;
    }
    
    public final int verify(long ssl, byte[][] chain, String auth) {
      X509Certificate[] peerCerts = ReferenceCountedOpenSslContext.certificates(chain);
      ReferenceCountedOpenSslEngine engine = this.engineMap.get(ssl);
      try {
        verify(engine, peerCerts, auth);
        return CertificateVerifier.X509_V_OK;
      } catch (Throwable cause) {
        ReferenceCountedOpenSslContext.logger.debug("verification of certificate failed", cause);
        SSLHandshakeException e = new SSLHandshakeException("General OpenSslEngine problem");
        e.initCause(cause);
        engine.handshakeException = e;
        if (cause instanceof OpenSslCertificateException)
          return ((OpenSslCertificateException)cause).errorCode(); 
        if (cause instanceof java.security.cert.CertificateExpiredException)
          return CertificateVerifier.X509_V_ERR_CERT_HAS_EXPIRED; 
        if (cause instanceof java.security.cert.CertificateNotYetValidException)
          return CertificateVerifier.X509_V_ERR_CERT_NOT_YET_VALID; 
        if (PlatformDependent.javaVersion() >= 7) {
          if (cause instanceof java.security.cert.CertificateRevokedException)
            return CertificateVerifier.X509_V_ERR_CERT_REVOKED; 
          Throwable wrapped = cause.getCause();
          while (wrapped != null) {
            if (wrapped instanceof CertPathValidatorException) {
              CertPathValidatorException ex = (CertPathValidatorException)wrapped;
              CertPathValidatorException.Reason reason = ex.getReason();
              if (reason == CertPathValidatorException.BasicReason.EXPIRED)
                return CertificateVerifier.X509_V_ERR_CERT_HAS_EXPIRED; 
              if (reason == CertPathValidatorException.BasicReason.NOT_YET_VALID)
                return CertificateVerifier.X509_V_ERR_CERT_NOT_YET_VALID; 
              if (reason == CertPathValidatorException.BasicReason.REVOKED)
                return CertificateVerifier.X509_V_ERR_CERT_REVOKED; 
            } 
            wrapped = wrapped.getCause();
          } 
        } 
        return CertificateVerifier.X509_V_ERR_UNSPECIFIED;
      } 
    }
    
    abstract void verify(ReferenceCountedOpenSslEngine param1ReferenceCountedOpenSslEngine, X509Certificate[] param1ArrayOfX509Certificate, String param1String) throws Exception;
  }
  
  private static final class DefaultOpenSslEngineMap implements OpenSslEngineMap {
    private final Map<Long, ReferenceCountedOpenSslEngine> engines = PlatformDependent.newConcurrentHashMap();
    
    public ReferenceCountedOpenSslEngine remove(long ssl) {
      return this.engines.remove(Long.valueOf(ssl));
    }
    
    public void add(ReferenceCountedOpenSslEngine engine) {
      this.engines.put(Long.valueOf(engine.sslPointer()), engine);
    }
    
    public ReferenceCountedOpenSslEngine get(long ssl) {
      return this.engines.get(Long.valueOf(ssl));
    }
    
    private DefaultOpenSslEngineMap() {}
  }
  
  static void setKeyMaterial(long ctx, X509Certificate[] keyCertChain, PrivateKey key, String keyPassword) throws SSLException {
    long keyBio = 0L;
    long keyCertChainBio = 0L;
    long keyCertChainBio2 = 0L;
    PemEncoded encoded = null;
    try {
      encoded = PemX509Certificate.toPEM(ByteBufAllocator.DEFAULT, true, keyCertChain);
      keyCertChainBio = toBIO(ByteBufAllocator.DEFAULT, encoded.retain());
      keyCertChainBio2 = toBIO(ByteBufAllocator.DEFAULT, encoded.retain());
      if (key != null)
        keyBio = toBIO(key); 
      SSLContext.setCertificateBio(ctx, keyCertChainBio, keyBio, (keyPassword == null) ? "" : keyPassword);
      SSLContext.setCertificateChainBio(ctx, keyCertChainBio2, true);
    } catch (SSLException e) {
      throw e;
    } catch (Exception e) {
      throw new SSLException("failed to set certificate and key", e);
    } finally {
      freeBio(keyBio);
      freeBio(keyCertChainBio);
      freeBio(keyCertChainBio2);
      if (encoded != null)
        encoded.release(); 
    } 
  }
  
  static void freeBio(long bio) {
    if (bio != 0L)
      SSL.freeBIO(bio); 
  }
  
  static long toBIO(PrivateKey key) throws Exception {
    if (key == null)
      return 0L; 
    ByteBufAllocator allocator = ByteBufAllocator.DEFAULT;
    PemEncoded pem = PemPrivateKey.toPEM(allocator, true, key);
    try {
      return toBIO(allocator, pem.retain());
    } finally {
      pem.release();
    } 
  }
  
  static long toBIO(X509Certificate... certChain) throws Exception {
    if (certChain == null)
      return 0L; 
    if (certChain.length == 0)
      throw new IllegalArgumentException("certChain can't be empty"); 
    ByteBufAllocator allocator = ByteBufAllocator.DEFAULT;
    PemEncoded pem = PemX509Certificate.toPEM(allocator, true, certChain);
    try {
      return toBIO(allocator, pem.retain());
    } finally {
      pem.release();
    } 
  }
  
  static long toBIO(ByteBufAllocator allocator, PemEncoded pem) throws Exception {
    try {
      ByteBuf content = pem.content();
      if (content.isDirect())
        return newBIO(content.retainedSlice()); 
      ByteBuf buffer = allocator.directBuffer(content.readableBytes());
    } finally {
      pem.release();
    } 
  }
  
  private static long newBIO(ByteBuf buffer) throws Exception {
    try {
      long bio = SSL.newMemBIO();
      int readable = buffer.readableBytes();
      if (SSL.bioWrite(bio, OpenSsl.memoryAddress(buffer) + buffer.readerIndex(), readable) != readable) {
        SSL.freeBIO(bio);
        throw new IllegalStateException("Could not write data to memory BIO");
      } 
      return bio;
    } finally {
      buffer.release();
    } 
  }
  
  abstract OpenSslKeyMaterialManager keyMaterialManager();
  
  public abstract OpenSslSessionContext sessionContext();
}
