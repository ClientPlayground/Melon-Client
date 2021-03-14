package com.github.steveice10.netty.handler.ssl;

import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.crypto.NoSuchPaddingException;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSessionContext;

public class JdkSslContext extends SslContext {
  static {
    SSLContext context;
  }
  
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(JdkSslContext.class);
  
  static final String PROTOCOL = "TLS";
  
  private static final String[] DEFAULT_PROTOCOLS;
  
  private static final List<String> DEFAULT_CIPHERS;
  
  private static final Set<String> SUPPORTED_CIPHERS;
  
  private final String[] protocols;
  
  private final String[] cipherSuites;
  
  private final List<String> unmodifiableCipherSuites;
  
  private final JdkApplicationProtocolNegotiator apn;
  
  private final ClientAuth clientAuth;
  
  private final SSLContext sslContext;
  
  private final boolean isClient;
  
  static {
    try {
      context = SSLContext.getInstance("TLS");
      context.init(null, null, null);
    } catch (Exception e) {
      throw new Error("failed to initialize the default SSL context", e);
    } 
    SSLEngine engine = context.createSSLEngine();
    String[] supportedProtocols = engine.getSupportedProtocols();
    Set<String> supportedProtocolsSet = new HashSet<String>(supportedProtocols.length);
    int i;
    for (i = 0; i < supportedProtocols.length; i++)
      supportedProtocolsSet.add(supportedProtocols[i]); 
    List<String> protocols = new ArrayList<String>();
    SslUtils.addIfSupported(supportedProtocolsSet, protocols, new String[] { "TLSv1.2", "TLSv1.1", "TLSv1" });
    if (!protocols.isEmpty()) {
      DEFAULT_PROTOCOLS = protocols.<String>toArray(new String[protocols.size()]);
    } else {
      DEFAULT_PROTOCOLS = engine.getEnabledProtocols();
    } 
    String[] supportedCiphers = engine.getSupportedCipherSuites();
    SUPPORTED_CIPHERS = new HashSet<String>(supportedCiphers.length);
    for (i = 0; i < supportedCiphers.length; i++) {
      String supportedCipher = supportedCiphers[i];
      SUPPORTED_CIPHERS.add(supportedCipher);
      if (supportedCipher.startsWith("SSL_")) {
        String tlsPrefixedCipherName = "TLS_" + supportedCipher.substring("SSL_".length());
        try {
          engine.setEnabledCipherSuites(new String[] { tlsPrefixedCipherName });
          SUPPORTED_CIPHERS.add(tlsPrefixedCipherName);
        } catch (IllegalArgumentException illegalArgumentException) {}
      } 
    } 
    List<String> ciphers = new ArrayList<String>();
    SslUtils.addIfSupported(SUPPORTED_CIPHERS, ciphers, SslUtils.DEFAULT_CIPHER_SUITES);
    SslUtils.useFallbackCiphersIfDefaultIsEmpty(ciphers, engine.getEnabledCipherSuites());
    DEFAULT_CIPHERS = Collections.unmodifiableList(ciphers);
    if (logger.isDebugEnabled()) {
      logger.debug("Default protocols (JDK): {} ", Arrays.asList(DEFAULT_PROTOCOLS));
      logger.debug("Default cipher suites (JDK): {}", DEFAULT_CIPHERS);
    } 
  }
  
  public JdkSslContext(SSLContext sslContext, boolean isClient, ClientAuth clientAuth) {
    this(sslContext, isClient, (Iterable<String>)null, IdentityCipherSuiteFilter.INSTANCE, JdkDefaultApplicationProtocolNegotiator.INSTANCE, clientAuth, (String[])null, false);
  }
  
  public JdkSslContext(SSLContext sslContext, boolean isClient, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, ApplicationProtocolConfig apn, ClientAuth clientAuth) {
    this(sslContext, isClient, ciphers, cipherFilter, toNegotiator(apn, !isClient), clientAuth, (String[])null, false);
  }
  
  JdkSslContext(SSLContext sslContext, boolean isClient, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, JdkApplicationProtocolNegotiator apn, ClientAuth clientAuth, String[] protocols, boolean startTls) {
    super(startTls);
    this.apn = (JdkApplicationProtocolNegotiator)ObjectUtil.checkNotNull(apn, "apn");
    this.clientAuth = (ClientAuth)ObjectUtil.checkNotNull(clientAuth, "clientAuth");
    this.cipherSuites = ((CipherSuiteFilter)ObjectUtil.checkNotNull(cipherFilter, "cipherFilter")).filterCipherSuites(ciphers, DEFAULT_CIPHERS, SUPPORTED_CIPHERS);
    this.protocols = (protocols == null) ? DEFAULT_PROTOCOLS : protocols;
    this.unmodifiableCipherSuites = Collections.unmodifiableList(Arrays.asList(this.cipherSuites));
    this.sslContext = (SSLContext)ObjectUtil.checkNotNull(sslContext, "sslContext");
    this.isClient = isClient;
  }
  
  public final SSLContext context() {
    return this.sslContext;
  }
  
  public final boolean isClient() {
    return this.isClient;
  }
  
  public final SSLSessionContext sessionContext() {
    if (isServer())
      return context().getServerSessionContext(); 
    return context().getClientSessionContext();
  }
  
  public final List<String> cipherSuites() {
    return this.unmodifiableCipherSuites;
  }
  
  public final long sessionCacheSize() {
    return sessionContext().getSessionCacheSize();
  }
  
  public final long sessionTimeout() {
    return sessionContext().getSessionTimeout();
  }
  
  public final SSLEngine newEngine(ByteBufAllocator alloc) {
    return configureAndWrapEngine(context().createSSLEngine(), alloc);
  }
  
  public final SSLEngine newEngine(ByteBufAllocator alloc, String peerHost, int peerPort) {
    return configureAndWrapEngine(context().createSSLEngine(peerHost, peerPort), alloc);
  }
  
  private SSLEngine configureAndWrapEngine(SSLEngine engine, ByteBufAllocator alloc) {
    engine.setEnabledCipherSuites(this.cipherSuites);
    engine.setEnabledProtocols(this.protocols);
    engine.setUseClientMode(isClient());
    if (isServer())
      switch (this.clientAuth) {
        case NONE:
          engine.setWantClientAuth(true);
          break;
        case ALPN:
          engine.setNeedClientAuth(true);
          break;
        case NPN:
          break;
        default:
          throw new Error("Unknown auth " + this.clientAuth);
      }  
    JdkApplicationProtocolNegotiator.SslEngineWrapperFactory factory = this.apn.wrapperFactory();
    if (factory instanceof JdkApplicationProtocolNegotiator.AllocatorAwareSslEngineWrapperFactory)
      return ((JdkApplicationProtocolNegotiator.AllocatorAwareSslEngineWrapperFactory)factory)
        .wrapSslEngine(engine, alloc, this.apn, isServer()); 
    return factory.wrapSslEngine(engine, this.apn, isServer());
  }
  
  public final JdkApplicationProtocolNegotiator applicationProtocolNegotiator() {
    return this.apn;
  }
  
  static JdkApplicationProtocolNegotiator toNegotiator(ApplicationProtocolConfig config, boolean isServer) {
    if (config == null)
      return JdkDefaultApplicationProtocolNegotiator.INSTANCE; 
    switch (config.protocol()) {
      case NONE:
        return JdkDefaultApplicationProtocolNegotiator.INSTANCE;
      case ALPN:
        if (isServer) {
          switch (config.selectorFailureBehavior()) {
            case NONE:
              return new JdkAlpnApplicationProtocolNegotiator(true, config.supportedProtocols());
            case ALPN:
              return new JdkAlpnApplicationProtocolNegotiator(false, config.supportedProtocols());
          } 
          throw new UnsupportedOperationException("JDK provider does not support " + config
              .selectorFailureBehavior() + " failure behavior");
        } 
        switch (config.selectedListenerFailureBehavior()) {
          case NONE:
            return new JdkAlpnApplicationProtocolNegotiator(false, config.supportedProtocols());
          case ALPN:
            return new JdkAlpnApplicationProtocolNegotiator(true, config.supportedProtocols());
        } 
        throw new UnsupportedOperationException("JDK provider does not support " + config
            .selectedListenerFailureBehavior() + " failure behavior");
      case NPN:
        if (isServer) {
          switch (config.selectedListenerFailureBehavior()) {
            case NONE:
              return new JdkNpnApplicationProtocolNegotiator(false, config.supportedProtocols());
            case ALPN:
              return new JdkNpnApplicationProtocolNegotiator(true, config.supportedProtocols());
          } 
          throw new UnsupportedOperationException("JDK provider does not support " + config
              .selectedListenerFailureBehavior() + " failure behavior");
        } 
        switch (config.selectorFailureBehavior()) {
          case NONE:
            return new JdkNpnApplicationProtocolNegotiator(true, config.supportedProtocols());
          case ALPN:
            return new JdkNpnApplicationProtocolNegotiator(false, config.supportedProtocols());
        } 
        throw new UnsupportedOperationException("JDK provider does not support " + config
            .selectorFailureBehavior() + " failure behavior");
    } 
    throw new UnsupportedOperationException("JDK provider does not support " + config
        .protocol() + " protocol");
  }
  
  @Deprecated
  protected static KeyManagerFactory buildKeyManagerFactory(File certChainFile, File keyFile, String keyPassword, KeyManagerFactory kmf) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, InvalidAlgorithmParameterException, CertificateException, KeyException, IOException {
    String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
    if (algorithm == null)
      algorithm = "SunX509"; 
    return buildKeyManagerFactory(certChainFile, algorithm, keyFile, keyPassword, kmf);
  }
  
  @Deprecated
  protected static KeyManagerFactory buildKeyManagerFactory(File certChainFile, String keyAlgorithm, File keyFile, String keyPassword, KeyManagerFactory kmf) throws KeyStoreException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, InvalidAlgorithmParameterException, IOException, CertificateException, KeyException, UnrecoverableKeyException {
    return buildKeyManagerFactory(toX509Certificates(certChainFile), keyAlgorithm, 
        toPrivateKey(keyFile, keyPassword), keyPassword, kmf);
  }
}
