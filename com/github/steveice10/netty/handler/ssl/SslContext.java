package com.github.steveice10.netty.handler.ssl;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.buffer.ByteBufInputStream;
import com.github.steveice10.netty.util.internal.EmptyArrays;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.TrustManagerFactory;

public abstract class SslContext {
  static final CertificateFactory X509_CERT_FACTORY;
  
  private final boolean startTls;
  
  static {
    try {
      X509_CERT_FACTORY = CertificateFactory.getInstance("X.509");
    } catch (CertificateException e) {
      throw new IllegalStateException("unable to instance X.509 CertificateFactory", e);
    } 
  }
  
  public static SslProvider defaultServerProvider() {
    return defaultProvider();
  }
  
  public static SslProvider defaultClientProvider() {
    return defaultProvider();
  }
  
  private static SslProvider defaultProvider() {
    if (OpenSsl.isAvailable())
      return SslProvider.OPENSSL; 
    return SslProvider.JDK;
  }
  
  @Deprecated
  public static SslContext newServerContext(File certChainFile, File keyFile) throws SSLException {
    return newServerContext(certChainFile, keyFile, (String)null);
  }
  
  @Deprecated
  public static SslContext newServerContext(File certChainFile, File keyFile, String keyPassword) throws SSLException {
    return newServerContext(null, certChainFile, keyFile, keyPassword);
  }
  
  @Deprecated
  public static SslContext newServerContext(File certChainFile, File keyFile, String keyPassword, Iterable<String> ciphers, Iterable<String> nextProtocols, long sessionCacheSize, long sessionTimeout) throws SSLException {
    return newServerContext((SslProvider)null, certChainFile, keyFile, keyPassword, ciphers, nextProtocols, sessionCacheSize, sessionTimeout);
  }
  
  @Deprecated
  public static SslContext newServerContext(File certChainFile, File keyFile, String keyPassword, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, ApplicationProtocolConfig apn, long sessionCacheSize, long sessionTimeout) throws SSLException {
    return newServerContext((SslProvider)null, certChainFile, keyFile, keyPassword, ciphers, cipherFilter, apn, sessionCacheSize, sessionTimeout);
  }
  
  @Deprecated
  public static SslContext newServerContext(SslProvider provider, File certChainFile, File keyFile) throws SSLException {
    return newServerContext(provider, certChainFile, keyFile, null);
  }
  
  @Deprecated
  public static SslContext newServerContext(SslProvider provider, File certChainFile, File keyFile, String keyPassword) throws SSLException {
    return newServerContext(provider, certChainFile, keyFile, keyPassword, (Iterable<String>)null, IdentityCipherSuiteFilter.INSTANCE, (ApplicationProtocolConfig)null, 0L, 0L);
  }
  
  @Deprecated
  public static SslContext newServerContext(SslProvider provider, File certChainFile, File keyFile, String keyPassword, Iterable<String> ciphers, Iterable<String> nextProtocols, long sessionCacheSize, long sessionTimeout) throws SSLException {
    return newServerContext(provider, certChainFile, keyFile, keyPassword, ciphers, IdentityCipherSuiteFilter.INSTANCE, 
        
        toApplicationProtocolConfig(nextProtocols), sessionCacheSize, sessionTimeout);
  }
  
  @Deprecated
  public static SslContext newServerContext(SslProvider provider, File certChainFile, File keyFile, String keyPassword, TrustManagerFactory trustManagerFactory, Iterable<String> ciphers, Iterable<String> nextProtocols, long sessionCacheSize, long sessionTimeout) throws SSLException {
    return newServerContext(provider, null, trustManagerFactory, certChainFile, keyFile, keyPassword, null, ciphers, IdentityCipherSuiteFilter.INSTANCE, 
        
        toApplicationProtocolConfig(nextProtocols), sessionCacheSize, sessionTimeout);
  }
  
  @Deprecated
  public static SslContext newServerContext(SslProvider provider, File certChainFile, File keyFile, String keyPassword, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, ApplicationProtocolConfig apn, long sessionCacheSize, long sessionTimeout) throws SSLException {
    return newServerContext(provider, null, null, certChainFile, keyFile, keyPassword, null, ciphers, cipherFilter, apn, sessionCacheSize, sessionTimeout);
  }
  
  @Deprecated
  public static SslContext newServerContext(SslProvider provider, File trustCertCollectionFile, TrustManagerFactory trustManagerFactory, File keyCertChainFile, File keyFile, String keyPassword, KeyManagerFactory keyManagerFactory, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, ApplicationProtocolConfig apn, long sessionCacheSize, long sessionTimeout) throws SSLException {
    try {
      return newServerContextInternal(provider, null, toX509Certificates(trustCertCollectionFile), trustManagerFactory, 
          toX509Certificates(keyCertChainFile), 
          toPrivateKey(keyFile, keyPassword), keyPassword, keyManagerFactory, ciphers, cipherFilter, apn, sessionCacheSize, sessionTimeout, ClientAuth.NONE, null, false, false);
    } catch (Exception e) {
      if (e instanceof SSLException)
        throw (SSLException)e; 
      throw new SSLException("failed to initialize the server-side SSL context", e);
    } 
  }
  
  static SslContext newServerContextInternal(SslProvider provider, Provider sslContextProvider, X509Certificate[] trustCertCollection, TrustManagerFactory trustManagerFactory, X509Certificate[] keyCertChain, PrivateKey key, String keyPassword, KeyManagerFactory keyManagerFactory, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, ApplicationProtocolConfig apn, long sessionCacheSize, long sessionTimeout, ClientAuth clientAuth, String[] protocols, boolean startTls, boolean enableOcsp) throws SSLException {
    if (provider == null)
      provider = defaultServerProvider(); 
    switch (provider) {
      case JDK:
        if (enableOcsp)
          throw new IllegalArgumentException("OCSP is not supported with this SslProvider: " + provider); 
        return new JdkSslServerContext(sslContextProvider, trustCertCollection, trustManagerFactory, keyCertChain, key, keyPassword, keyManagerFactory, ciphers, cipherFilter, apn, sessionCacheSize, sessionTimeout, clientAuth, protocols, startTls);
      case OPENSSL:
        verifyNullSslContextProvider(provider, sslContextProvider);
        return new OpenSslServerContext(trustCertCollection, trustManagerFactory, keyCertChain, key, keyPassword, keyManagerFactory, ciphers, cipherFilter, apn, sessionCacheSize, sessionTimeout, clientAuth, protocols, startTls, enableOcsp);
      case OPENSSL_REFCNT:
        verifyNullSslContextProvider(provider, sslContextProvider);
        return new ReferenceCountedOpenSslServerContext(trustCertCollection, trustManagerFactory, keyCertChain, key, keyPassword, keyManagerFactory, ciphers, cipherFilter, apn, sessionCacheSize, sessionTimeout, clientAuth, protocols, startTls, enableOcsp);
    } 
    throw new Error(provider.toString());
  }
  
  private static void verifyNullSslContextProvider(SslProvider provider, Provider sslContextProvider) {
    if (sslContextProvider != null)
      throw new IllegalArgumentException("Java Security Provider unsupported for SslProvider: " + provider); 
  }
  
  @Deprecated
  public static SslContext newClientContext() throws SSLException {
    return newClientContext(null, null, null);
  }
  
  @Deprecated
  public static SslContext newClientContext(File certChainFile) throws SSLException {
    return newClientContext((SslProvider)null, certChainFile);
  }
  
  @Deprecated
  public static SslContext newClientContext(TrustManagerFactory trustManagerFactory) throws SSLException {
    return newClientContext(null, null, trustManagerFactory);
  }
  
  @Deprecated
  public static SslContext newClientContext(File certChainFile, TrustManagerFactory trustManagerFactory) throws SSLException {
    return newClientContext(null, certChainFile, trustManagerFactory);
  }
  
  @Deprecated
  public static SslContext newClientContext(File certChainFile, TrustManagerFactory trustManagerFactory, Iterable<String> ciphers, Iterable<String> nextProtocols, long sessionCacheSize, long sessionTimeout) throws SSLException {
    return newClientContext((SslProvider)null, certChainFile, trustManagerFactory, ciphers, nextProtocols, sessionCacheSize, sessionTimeout);
  }
  
  @Deprecated
  public static SslContext newClientContext(File certChainFile, TrustManagerFactory trustManagerFactory, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, ApplicationProtocolConfig apn, long sessionCacheSize, long sessionTimeout) throws SSLException {
    return newClientContext(null, certChainFile, trustManagerFactory, ciphers, cipherFilter, apn, sessionCacheSize, sessionTimeout);
  }
  
  @Deprecated
  public static SslContext newClientContext(SslProvider provider) throws SSLException {
    return newClientContext(provider, null, null);
  }
  
  @Deprecated
  public static SslContext newClientContext(SslProvider provider, File certChainFile) throws SSLException {
    return newClientContext(provider, certChainFile, null);
  }
  
  @Deprecated
  public static SslContext newClientContext(SslProvider provider, TrustManagerFactory trustManagerFactory) throws SSLException {
    return newClientContext(provider, null, trustManagerFactory);
  }
  
  @Deprecated
  public static SslContext newClientContext(SslProvider provider, File certChainFile, TrustManagerFactory trustManagerFactory) throws SSLException {
    return newClientContext(provider, certChainFile, trustManagerFactory, null, IdentityCipherSuiteFilter.INSTANCE, null, 0L, 0L);
  }
  
  @Deprecated
  public static SslContext newClientContext(SslProvider provider, File certChainFile, TrustManagerFactory trustManagerFactory, Iterable<String> ciphers, Iterable<String> nextProtocols, long sessionCacheSize, long sessionTimeout) throws SSLException {
    return newClientContext(provider, certChainFile, trustManagerFactory, null, null, null, null, ciphers, IdentityCipherSuiteFilter.INSTANCE, 
        
        toApplicationProtocolConfig(nextProtocols), sessionCacheSize, sessionTimeout);
  }
  
  @Deprecated
  public static SslContext newClientContext(SslProvider provider, File certChainFile, TrustManagerFactory trustManagerFactory, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, ApplicationProtocolConfig apn, long sessionCacheSize, long sessionTimeout) throws SSLException {
    return newClientContext(provider, certChainFile, trustManagerFactory, null, null, null, null, ciphers, cipherFilter, apn, sessionCacheSize, sessionTimeout);
  }
  
  @Deprecated
  public static SslContext newClientContext(SslProvider provider, File trustCertCollectionFile, TrustManagerFactory trustManagerFactory, File keyCertChainFile, File keyFile, String keyPassword, KeyManagerFactory keyManagerFactory, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, ApplicationProtocolConfig apn, long sessionCacheSize, long sessionTimeout) throws SSLException {
    try {
      return newClientContextInternal(provider, null, 
          toX509Certificates(trustCertCollectionFile), trustManagerFactory, 
          toX509Certificates(keyCertChainFile), toPrivateKey(keyFile, keyPassword), keyPassword, keyManagerFactory, ciphers, cipherFilter, apn, null, sessionCacheSize, sessionTimeout, false);
    } catch (Exception e) {
      if (e instanceof SSLException)
        throw (SSLException)e; 
      throw new SSLException("failed to initialize the client-side SSL context", e);
    } 
  }
  
  static SslContext newClientContextInternal(SslProvider provider, Provider sslContextProvider, X509Certificate[] trustCert, TrustManagerFactory trustManagerFactory, X509Certificate[] keyCertChain, PrivateKey key, String keyPassword, KeyManagerFactory keyManagerFactory, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, ApplicationProtocolConfig apn, String[] protocols, long sessionCacheSize, long sessionTimeout, boolean enableOcsp) throws SSLException {
    if (provider == null)
      provider = defaultClientProvider(); 
    switch (provider) {
      case JDK:
        if (enableOcsp)
          throw new IllegalArgumentException("OCSP is not supported with this SslProvider: " + provider); 
        return new JdkSslClientContext(sslContextProvider, trustCert, trustManagerFactory, keyCertChain, key, keyPassword, keyManagerFactory, ciphers, cipherFilter, apn, protocols, sessionCacheSize, sessionTimeout);
      case OPENSSL:
        verifyNullSslContextProvider(provider, sslContextProvider);
        return new OpenSslClientContext(trustCert, trustManagerFactory, keyCertChain, key, keyPassword, keyManagerFactory, ciphers, cipherFilter, apn, protocols, sessionCacheSize, sessionTimeout, enableOcsp);
      case OPENSSL_REFCNT:
        verifyNullSslContextProvider(provider, sslContextProvider);
        return new ReferenceCountedOpenSslClientContext(trustCert, trustManagerFactory, keyCertChain, key, keyPassword, keyManagerFactory, ciphers, cipherFilter, apn, protocols, sessionCacheSize, sessionTimeout, enableOcsp);
    } 
    throw new Error(provider.toString());
  }
  
  static ApplicationProtocolConfig toApplicationProtocolConfig(Iterable<String> nextProtocols) {
    ApplicationProtocolConfig apn;
    if (nextProtocols == null) {
      apn = ApplicationProtocolConfig.DISABLED;
    } else {
      apn = new ApplicationProtocolConfig(ApplicationProtocolConfig.Protocol.NPN_AND_ALPN, ApplicationProtocolConfig.SelectorFailureBehavior.CHOOSE_MY_LAST_PROTOCOL, ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT, nextProtocols);
    } 
    return apn;
  }
  
  protected SslContext() {
    this(false);
  }
  
  protected SslContext(boolean startTls) {
    this.startTls = startTls;
  }
  
  public final boolean isServer() {
    return !isClient();
  }
  
  @Deprecated
  public final List<String> nextProtocols() {
    return applicationProtocolNegotiator().protocols();
  }
  
  public final SslHandler newHandler(ByteBufAllocator alloc) {
    return newHandler(alloc, this.startTls);
  }
  
  protected SslHandler newHandler(ByteBufAllocator alloc, boolean startTls) {
    return new SslHandler(newEngine(alloc), startTls);
  }
  
  public final SslHandler newHandler(ByteBufAllocator alloc, String peerHost, int peerPort) {
    return newHandler(alloc, peerHost, peerPort, this.startTls);
  }
  
  protected SslHandler newHandler(ByteBufAllocator alloc, String peerHost, int peerPort, boolean startTls) {
    return new SslHandler(newEngine(alloc, peerHost, peerPort), startTls);
  }
  
  protected static PKCS8EncodedKeySpec generateKeySpec(char[] password, byte[] key) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, InvalidKeyException, InvalidAlgorithmParameterException {
    if (password == null)
      return new PKCS8EncodedKeySpec(key); 
    EncryptedPrivateKeyInfo encryptedPrivateKeyInfo = new EncryptedPrivateKeyInfo(key);
    SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(encryptedPrivateKeyInfo.getAlgName());
    PBEKeySpec pbeKeySpec = new PBEKeySpec(password);
    SecretKey pbeKey = keyFactory.generateSecret(pbeKeySpec);
    Cipher cipher = Cipher.getInstance(encryptedPrivateKeyInfo.getAlgName());
    cipher.init(2, pbeKey, encryptedPrivateKeyInfo.getAlgParameters());
    return encryptedPrivateKeyInfo.getKeySpec(cipher);
  }
  
  static KeyStore buildKeyStore(X509Certificate[] certChain, PrivateKey key, char[] keyPasswordChars) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
    KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
    ks.load(null, null);
    ks.setKeyEntry("key", key, keyPasswordChars, (Certificate[])certChain);
    return ks;
  }
  
  static PrivateKey toPrivateKey(File keyFile, String keyPassword) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, InvalidAlgorithmParameterException, KeyException, IOException {
    if (keyFile == null)
      return null; 
    return getPrivateKeyFromByteBuffer(PemReader.readPrivateKey(keyFile), keyPassword);
  }
  
  static PrivateKey toPrivateKey(InputStream keyInputStream, String keyPassword) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, InvalidAlgorithmParameterException, KeyException, IOException {
    if (keyInputStream == null)
      return null; 
    return getPrivateKeyFromByteBuffer(PemReader.readPrivateKey(keyInputStream), keyPassword);
  }
  
  private static PrivateKey getPrivateKeyFromByteBuffer(ByteBuf encodedKeyBuf, String keyPassword) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, InvalidAlgorithmParameterException, KeyException, IOException {
    byte[] encodedKey = new byte[encodedKeyBuf.readableBytes()];
    encodedKeyBuf.readBytes(encodedKey).release();
    PKCS8EncodedKeySpec encodedKeySpec = generateKeySpec((keyPassword == null) ? null : keyPassword
        .toCharArray(), encodedKey);
    try {
      return KeyFactory.getInstance("RSA").generatePrivate(encodedKeySpec);
    } catch (InvalidKeySpecException ignore) {
      try {
        return KeyFactory.getInstance("DSA").generatePrivate(encodedKeySpec);
      } catch (InvalidKeySpecException ignore2) {
        try {
          return KeyFactory.getInstance("EC").generatePrivate(encodedKeySpec);
        } catch (InvalidKeySpecException e) {
          throw new InvalidKeySpecException("Neither RSA, DSA nor EC worked", e);
        } 
      } 
    } 
  }
  
  @Deprecated
  protected static TrustManagerFactory buildTrustManagerFactory(File certChainFile, TrustManagerFactory trustManagerFactory) throws NoSuchAlgorithmException, CertificateException, KeyStoreException, IOException {
    X509Certificate[] x509Certs = toX509Certificates(certChainFile);
    return buildTrustManagerFactory(x509Certs, trustManagerFactory);
  }
  
  static X509Certificate[] toX509Certificates(File file) throws CertificateException {
    if (file == null)
      return null; 
    return getCertificatesFromBuffers(PemReader.readCertificates(file));
  }
  
  static X509Certificate[] toX509Certificates(InputStream in) throws CertificateException {
    if (in == null)
      return null; 
    return getCertificatesFromBuffers(PemReader.readCertificates(in));
  }
  
  private static X509Certificate[] getCertificatesFromBuffers(ByteBuf[] certs) throws CertificateException {
    CertificateFactory cf = CertificateFactory.getInstance("X.509");
    X509Certificate[] x509Certs = new X509Certificate[certs.length];
    int i = 0;
    try {
      for (; i < certs.length; i++)
        ByteBufInputStream byteBufInputStream = new ByteBufInputStream(certs[i], true); 
    } finally {
      for (; i < certs.length; i++)
        certs[i].release(); 
    } 
    return x509Certs;
  }
  
  static TrustManagerFactory buildTrustManagerFactory(X509Certificate[] certCollection, TrustManagerFactory trustManagerFactory) throws NoSuchAlgorithmException, CertificateException, KeyStoreException, IOException {
    KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
    ks.load(null, null);
    int i = 1;
    for (X509Certificate cert : certCollection) {
      String alias = Integer.toString(i);
      ks.setCertificateEntry(alias, cert);
      i++;
    } 
    if (trustManagerFactory == null)
      trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()); 
    trustManagerFactory.init(ks);
    return trustManagerFactory;
  }
  
  static PrivateKey toPrivateKeyInternal(File keyFile, String keyPassword) throws SSLException {
    try {
      return toPrivateKey(keyFile, keyPassword);
    } catch (Exception e) {
      throw new SSLException(e);
    } 
  }
  
  static X509Certificate[] toX509CertificatesInternal(File file) throws SSLException {
    try {
      return toX509Certificates(file);
    } catch (CertificateException e) {
      throw new SSLException(e);
    } 
  }
  
  static KeyManagerFactory buildKeyManagerFactory(X509Certificate[] certChain, PrivateKey key, String keyPassword, KeyManagerFactory kmf) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
    return buildKeyManagerFactory(certChain, KeyManagerFactory.getDefaultAlgorithm(), key, keyPassword, kmf);
  }
  
  static KeyManagerFactory buildKeyManagerFactory(X509Certificate[] certChainFile, String keyAlgorithm, PrivateKey key, String keyPassword, KeyManagerFactory kmf) throws KeyStoreException, NoSuchAlgorithmException, IOException, CertificateException, UnrecoverableKeyException {
    char[] keyPasswordChars = (keyPassword == null) ? EmptyArrays.EMPTY_CHARS : keyPassword.toCharArray();
    KeyStore ks = buildKeyStore(certChainFile, key, keyPasswordChars);
    if (kmf == null)
      kmf = KeyManagerFactory.getInstance(keyAlgorithm); 
    kmf.init(ks, keyPasswordChars);
    return kmf;
  }
  
  public abstract boolean isClient();
  
  public abstract List<String> cipherSuites();
  
  public abstract long sessionCacheSize();
  
  public abstract long sessionTimeout();
  
  public abstract ApplicationProtocolNegotiator applicationProtocolNegotiator();
  
  public abstract SSLEngine newEngine(ByteBufAllocator paramByteBufAllocator);
  
  public abstract SSLEngine newEngine(ByteBufAllocator paramByteBufAllocator, String paramString, int paramInt);
  
  public abstract SSLSessionContext sessionContext();
}
