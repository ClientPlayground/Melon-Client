package com.github.steveice10.netty.handler.ssl;

import com.github.steveice10.netty.util.internal.ObjectUtil;
import java.io.File;
import java.io.InputStream;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.cert.X509Certificate;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;

public final class SslContextBuilder {
  private final boolean forServer;
  
  private SslProvider provider;
  
  private Provider sslContextProvider;
  
  private X509Certificate[] trustCertCollection;
  
  private TrustManagerFactory trustManagerFactory;
  
  private X509Certificate[] keyCertChain;
  
  private PrivateKey key;
  
  private String keyPassword;
  
  private KeyManagerFactory keyManagerFactory;
  
  private Iterable<String> ciphers;
  
  public static SslContextBuilder forClient() {
    return new SslContextBuilder(false);
  }
  
  public static SslContextBuilder forServer(File keyCertChainFile, File keyFile) {
    return (new SslContextBuilder(true)).keyManager(keyCertChainFile, keyFile);
  }
  
  public static SslContextBuilder forServer(InputStream keyCertChainInputStream, InputStream keyInputStream) {
    return (new SslContextBuilder(true)).keyManager(keyCertChainInputStream, keyInputStream);
  }
  
  public static SslContextBuilder forServer(PrivateKey key, X509Certificate... keyCertChain) {
    return (new SslContextBuilder(true)).keyManager(key, keyCertChain);
  }
  
  public static SslContextBuilder forServer(File keyCertChainFile, File keyFile, String keyPassword) {
    return (new SslContextBuilder(true)).keyManager(keyCertChainFile, keyFile, keyPassword);
  }
  
  public static SslContextBuilder forServer(InputStream keyCertChainInputStream, InputStream keyInputStream, String keyPassword) {
    return (new SslContextBuilder(true)).keyManager(keyCertChainInputStream, keyInputStream, keyPassword);
  }
  
  public static SslContextBuilder forServer(PrivateKey key, String keyPassword, X509Certificate... keyCertChain) {
    return (new SslContextBuilder(true)).keyManager(key, keyPassword, keyCertChain);
  }
  
  public static SslContextBuilder forServer(KeyManagerFactory keyManagerFactory) {
    return (new SslContextBuilder(true)).keyManager(keyManagerFactory);
  }
  
  private CipherSuiteFilter cipherFilter = IdentityCipherSuiteFilter.INSTANCE;
  
  private ApplicationProtocolConfig apn;
  
  private long sessionCacheSize;
  
  private long sessionTimeout;
  
  private ClientAuth clientAuth = ClientAuth.NONE;
  
  private String[] protocols;
  
  private boolean startTls;
  
  private boolean enableOcsp;
  
  private SslContextBuilder(boolean forServer) {
    this.forServer = forServer;
  }
  
  public SslContextBuilder sslProvider(SslProvider provider) {
    this.provider = provider;
    return this;
  }
  
  public SslContextBuilder sslContextProvider(Provider sslContextProvider) {
    this.sslContextProvider = sslContextProvider;
    return this;
  }
  
  public SslContextBuilder trustManager(File trustCertCollectionFile) {
    try {
      return trustManager(SslContext.toX509Certificates(trustCertCollectionFile));
    } catch (Exception e) {
      throw new IllegalArgumentException("File does not contain valid certificates: " + trustCertCollectionFile, e);
    } 
  }
  
  public SslContextBuilder trustManager(InputStream trustCertCollectionInputStream) {
    try {
      return trustManager(SslContext.toX509Certificates(trustCertCollectionInputStream));
    } catch (Exception e) {
      throw new IllegalArgumentException("Input stream does not contain valid certificates.", e);
    } 
  }
  
  public SslContextBuilder trustManager(X509Certificate... trustCertCollection) {
    this.trustCertCollection = (trustCertCollection != null) ? (X509Certificate[])trustCertCollection.clone() : null;
    this.trustManagerFactory = null;
    return this;
  }
  
  public SslContextBuilder trustManager(TrustManagerFactory trustManagerFactory) {
    this.trustCertCollection = null;
    this.trustManagerFactory = trustManagerFactory;
    return this;
  }
  
  public SslContextBuilder keyManager(File keyCertChainFile, File keyFile) {
    return keyManager(keyCertChainFile, keyFile, (String)null);
  }
  
  public SslContextBuilder keyManager(InputStream keyCertChainInputStream, InputStream keyInputStream) {
    return keyManager(keyCertChainInputStream, keyInputStream, (String)null);
  }
  
  public SslContextBuilder keyManager(PrivateKey key, X509Certificate... keyCertChain) {
    return keyManager(key, (String)null, keyCertChain);
  }
  
  public SslContextBuilder keyManager(File keyCertChainFile, File keyFile, String keyPassword) {
    X509Certificate[] keyCertChain;
    PrivateKey key;
    try {
      keyCertChain = SslContext.toX509Certificates(keyCertChainFile);
    } catch (Exception e) {
      throw new IllegalArgumentException("File does not contain valid certificates: " + keyCertChainFile, e);
    } 
    try {
      key = SslContext.toPrivateKey(keyFile, keyPassword);
    } catch (Exception e) {
      throw new IllegalArgumentException("File does not contain valid private key: " + keyFile, e);
    } 
    return keyManager(key, keyPassword, keyCertChain);
  }
  
  public SslContextBuilder keyManager(InputStream keyCertChainInputStream, InputStream keyInputStream, String keyPassword) {
    X509Certificate[] keyCertChain;
    PrivateKey key;
    try {
      keyCertChain = SslContext.toX509Certificates(keyCertChainInputStream);
    } catch (Exception e) {
      throw new IllegalArgumentException("Input stream not contain valid certificates.", e);
    } 
    try {
      key = SslContext.toPrivateKey(keyInputStream, keyPassword);
    } catch (Exception e) {
      throw new IllegalArgumentException("Input stream does not contain valid private key.", e);
    } 
    return keyManager(key, keyPassword, keyCertChain);
  }
  
  public SslContextBuilder keyManager(PrivateKey key, String keyPassword, X509Certificate... keyCertChain) {
    if (this.forServer) {
      ObjectUtil.checkNotNull(keyCertChain, "keyCertChain required for servers");
      if (keyCertChain.length == 0)
        throw new IllegalArgumentException("keyCertChain must be non-empty"); 
      ObjectUtil.checkNotNull(key, "key required for servers");
    } 
    if (keyCertChain == null || keyCertChain.length == 0) {
      this.keyCertChain = null;
    } else {
      for (X509Certificate cert : keyCertChain) {
        if (cert == null)
          throw new IllegalArgumentException("keyCertChain contains null entry"); 
      } 
      this.keyCertChain = (X509Certificate[])keyCertChain.clone();
    } 
    this.key = key;
    this.keyPassword = keyPassword;
    this.keyManagerFactory = null;
    return this;
  }
  
  public SslContextBuilder keyManager(KeyManagerFactory keyManagerFactory) {
    if (this.forServer)
      ObjectUtil.checkNotNull(keyManagerFactory, "keyManagerFactory required for servers"); 
    this.keyCertChain = null;
    this.key = null;
    this.keyPassword = null;
    this.keyManagerFactory = keyManagerFactory;
    return this;
  }
  
  public SslContextBuilder ciphers(Iterable<String> ciphers) {
    return ciphers(ciphers, IdentityCipherSuiteFilter.INSTANCE);
  }
  
  public SslContextBuilder ciphers(Iterable<String> ciphers, CipherSuiteFilter cipherFilter) {
    ObjectUtil.checkNotNull(cipherFilter, "cipherFilter");
    this.ciphers = ciphers;
    this.cipherFilter = cipherFilter;
    return this;
  }
  
  public SslContextBuilder applicationProtocolConfig(ApplicationProtocolConfig apn) {
    this.apn = apn;
    return this;
  }
  
  public SslContextBuilder sessionCacheSize(long sessionCacheSize) {
    this.sessionCacheSize = sessionCacheSize;
    return this;
  }
  
  public SslContextBuilder sessionTimeout(long sessionTimeout) {
    this.sessionTimeout = sessionTimeout;
    return this;
  }
  
  public SslContextBuilder clientAuth(ClientAuth clientAuth) {
    this.clientAuth = (ClientAuth)ObjectUtil.checkNotNull(clientAuth, "clientAuth");
    return this;
  }
  
  public SslContextBuilder protocols(String... protocols) {
    this.protocols = (protocols == null) ? null : (String[])protocols.clone();
    return this;
  }
  
  public SslContextBuilder startTls(boolean startTls) {
    this.startTls = startTls;
    return this;
  }
  
  public SslContextBuilder enableOcsp(boolean enableOcsp) {
    this.enableOcsp = enableOcsp;
    return this;
  }
  
  public SslContext build() throws SSLException {
    if (this.forServer)
      return SslContext.newServerContextInternal(this.provider, this.sslContextProvider, this.trustCertCollection, this.trustManagerFactory, this.keyCertChain, this.key, this.keyPassword, this.keyManagerFactory, this.ciphers, this.cipherFilter, this.apn, this.sessionCacheSize, this.sessionTimeout, this.clientAuth, this.protocols, this.startTls, this.enableOcsp); 
    return SslContext.newClientContextInternal(this.provider, this.sslContextProvider, this.trustCertCollection, this.trustManagerFactory, this.keyCertChain, this.key, this.keyPassword, this.keyManagerFactory, this.ciphers, this.cipherFilter, this.apn, this.protocols, this.sessionCacheSize, this.sessionTimeout, this.enableOcsp);
  }
}
