package com.github.steveice10.netty.handler.ssl;

import com.github.steveice10.netty.internal.tcnative.SSLContext;
import com.github.steveice10.netty.internal.tcnative.SniHostNameMatcher;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.PlatformDependent;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;

public final class ReferenceCountedOpenSslServerContext extends ReferenceCountedOpenSslContext {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(ReferenceCountedOpenSslServerContext.class);
  
  private static final byte[] ID = new byte[] { 110, 101, 116, 116, 121 };
  
  private final OpenSslServerSessionContext sessionContext;
  
  private final OpenSslKeyMaterialManager keyMaterialManager;
  
  ReferenceCountedOpenSslServerContext(X509Certificate[] trustCertCollection, TrustManagerFactory trustManagerFactory, X509Certificate[] keyCertChain, PrivateKey key, String keyPassword, KeyManagerFactory keyManagerFactory, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, ApplicationProtocolConfig apn, long sessionCacheSize, long sessionTimeout, ClientAuth clientAuth, String[] protocols, boolean startTls, boolean enableOcsp) throws SSLException {
    this(trustCertCollection, trustManagerFactory, keyCertChain, key, keyPassword, keyManagerFactory, ciphers, cipherFilter, 
        toNegotiator(apn), sessionCacheSize, sessionTimeout, clientAuth, protocols, startTls, enableOcsp);
  }
  
  private ReferenceCountedOpenSslServerContext(X509Certificate[] trustCertCollection, TrustManagerFactory trustManagerFactory, X509Certificate[] keyCertChain, PrivateKey key, String keyPassword, KeyManagerFactory keyManagerFactory, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, OpenSslApplicationProtocolNegotiator apn, long sessionCacheSize, long sessionTimeout, ClientAuth clientAuth, String[] protocols, boolean startTls, boolean enableOcsp) throws SSLException {
    super(ciphers, cipherFilter, apn, sessionCacheSize, sessionTimeout, 1, (Certificate[])keyCertChain, clientAuth, protocols, startTls, enableOcsp, true);
    boolean success = false;
    try {
      ServerContext context = newSessionContext(this, this.ctx, this.engineMap, trustCertCollection, trustManagerFactory, keyCertChain, key, keyPassword, keyManagerFactory);
      this.sessionContext = context.sessionContext;
      this.keyMaterialManager = context.keyMaterialManager;
      success = true;
    } finally {
      if (!success)
        release(); 
    } 
  }
  
  public OpenSslServerSessionContext sessionContext() {
    return this.sessionContext;
  }
  
  OpenSslKeyMaterialManager keyMaterialManager() {
    return this.keyMaterialManager;
  }
  
  static final class ServerContext {
    OpenSslServerSessionContext sessionContext;
    
    OpenSslKeyMaterialManager keyMaterialManager;
  }
  
  static ServerContext newSessionContext(ReferenceCountedOpenSslContext thiz, long ctx, OpenSslEngineMap engineMap, X509Certificate[] trustCertCollection, TrustManagerFactory trustManagerFactory, X509Certificate[] keyCertChain, PrivateKey key, String keyPassword, KeyManagerFactory keyManagerFactory) throws SSLException {
    ServerContext result = new ServerContext();
    try {
      SSLContext.setVerify(ctx, 0, 10);
      if (!OpenSsl.useKeyManagerFactory()) {
        if (keyManagerFactory != null)
          throw new IllegalArgumentException("KeyManagerFactory not supported"); 
        ObjectUtil.checkNotNull(keyCertChain, "keyCertChain");
        setKeyMaterial(ctx, keyCertChain, key, keyPassword);
      } else {
        if (keyManagerFactory == null)
          keyManagerFactory = buildKeyManagerFactory(keyCertChain, key, keyPassword, keyManagerFactory); 
        X509KeyManager keyManager = chooseX509KeyManager(keyManagerFactory.getKeyManagers());
        result.keyMaterialManager = useExtendedKeyManager(keyManager) ? new OpenSslExtendedKeyMaterialManager((X509ExtendedKeyManager)keyManager, keyPassword) : new OpenSslKeyMaterialManager(keyManager, keyPassword);
      } 
    } catch (Exception e) {
      throw new SSLException("failed to set certificate and key", e);
    } 
    try {
      if (trustCertCollection != null) {
        trustManagerFactory = buildTrustManagerFactory(trustCertCollection, trustManagerFactory);
      } else if (trustManagerFactory == null) {
        trustManagerFactory = TrustManagerFactory.getInstance(
            TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init((KeyStore)null);
      } 
      X509TrustManager manager = chooseTrustManager(trustManagerFactory.getTrustManagers());
      if (useExtendedTrustManager(manager)) {
        SSLContext.setCertVerifyCallback(ctx, new ExtendedTrustManagerVerifyCallback(engineMap, (X509ExtendedTrustManager)manager));
      } else {
        SSLContext.setCertVerifyCallback(ctx, new TrustManagerVerifyCallback(engineMap, manager));
      } 
      X509Certificate[] issuers = manager.getAcceptedIssuers();
      if (issuers != null && issuers.length > 0) {
        long bio = 0L;
        try {
          bio = toBIO(issuers);
          if (!SSLContext.setCACertificateBio(ctx, bio))
            throw new SSLException("unable to setup accepted issuers for trustmanager " + manager); 
        } finally {
          freeBio(bio);
        } 
      } 
      if (PlatformDependent.javaVersion() >= 8)
        SSLContext.setSniHostnameMatcher(ctx, new OpenSslSniHostnameMatcher(engineMap)); 
    } catch (SSLException e) {
      throw e;
    } catch (Exception e) {
      throw new SSLException("unable to setup trustmanager", e);
    } 
    result.sessionContext = new OpenSslServerSessionContext(thiz);
    result.sessionContext.setSessionIdContext(ID);
    return result;
  }
  
  private static final class TrustManagerVerifyCallback extends ReferenceCountedOpenSslContext.AbstractCertificateVerifier {
    private final X509TrustManager manager;
    
    TrustManagerVerifyCallback(OpenSslEngineMap engineMap, X509TrustManager manager) {
      super(engineMap);
      this.manager = manager;
    }
    
    void verify(ReferenceCountedOpenSslEngine engine, X509Certificate[] peerCerts, String auth) throws Exception {
      this.manager.checkClientTrusted(peerCerts, auth);
    }
  }
  
  private static final class ExtendedTrustManagerVerifyCallback extends ReferenceCountedOpenSslContext.AbstractCertificateVerifier {
    private final X509ExtendedTrustManager manager;
    
    ExtendedTrustManagerVerifyCallback(OpenSslEngineMap engineMap, X509ExtendedTrustManager manager) {
      super(engineMap);
      this.manager = manager;
    }
    
    void verify(ReferenceCountedOpenSslEngine engine, X509Certificate[] peerCerts, String auth) throws Exception {
      this.manager.checkClientTrusted(peerCerts, auth, engine);
    }
  }
  
  private static final class OpenSslSniHostnameMatcher implements SniHostNameMatcher {
    private final OpenSslEngineMap engineMap;
    
    OpenSslSniHostnameMatcher(OpenSslEngineMap engineMap) {
      this.engineMap = engineMap;
    }
    
    public boolean match(long ssl, String hostname) {
      ReferenceCountedOpenSslEngine engine = this.engineMap.get(ssl);
      if (engine != null)
        return engine.checkSniHostnameMatch(hostname); 
      ReferenceCountedOpenSslServerContext.logger.warn("No ReferenceCountedOpenSslEngine found for SSL pointer: {}", Long.valueOf(ssl));
      return false;
    }
  }
}
