package com.github.steveice10.netty.handler.ssl.util;

import com.github.steveice10.netty.util.concurrent.FastThreadLocal;
import com.github.steveice10.netty.util.internal.PlatformDependent;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Provider;
import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.TrustManagerFactorySpi;
import javax.net.ssl.X509TrustManager;

public abstract class SimpleTrustManagerFactory extends TrustManagerFactory {
  private static final Provider PROVIDER = new Provider("", 0.0D, "") {
      private static final long serialVersionUID = -2680540247105807895L;
    };
  
  private static final FastThreadLocal<SimpleTrustManagerFactorySpi> CURRENT_SPI = new FastThreadLocal<SimpleTrustManagerFactorySpi>() {
      protected SimpleTrustManagerFactory.SimpleTrustManagerFactorySpi initialValue() {
        return new SimpleTrustManagerFactory.SimpleTrustManagerFactorySpi();
      }
    };
  
  protected SimpleTrustManagerFactory() {
    this("");
  }
  
  protected SimpleTrustManagerFactory(String name) {
    super((TrustManagerFactorySpi)CURRENT_SPI.get(), PROVIDER, name);
    ((SimpleTrustManagerFactorySpi)CURRENT_SPI.get()).init(this);
    CURRENT_SPI.remove();
    if (name == null)
      throw new NullPointerException("name"); 
  }
  
  protected abstract void engineInit(KeyStore paramKeyStore) throws Exception;
  
  protected abstract void engineInit(ManagerFactoryParameters paramManagerFactoryParameters) throws Exception;
  
  protected abstract TrustManager[] engineGetTrustManagers();
  
  static final class SimpleTrustManagerFactorySpi extends TrustManagerFactorySpi {
    private SimpleTrustManagerFactory parent;
    
    private volatile TrustManager[] trustManagers;
    
    void init(SimpleTrustManagerFactory parent) {
      this.parent = parent;
    }
    
    protected void engineInit(KeyStore keyStore) throws KeyStoreException {
      try {
        this.parent.engineInit(keyStore);
      } catch (KeyStoreException e) {
        throw e;
      } catch (Exception e) {
        throw new KeyStoreException(e);
      } 
    }
    
    protected void engineInit(ManagerFactoryParameters managerFactoryParameters) throws InvalidAlgorithmParameterException {
      try {
        this.parent.engineInit(managerFactoryParameters);
      } catch (InvalidAlgorithmParameterException e) {
        throw e;
      } catch (Exception e) {
        throw new InvalidAlgorithmParameterException(e);
      } 
    }
    
    protected TrustManager[] engineGetTrustManagers() {
      TrustManager[] trustManagers = this.trustManagers;
      if (trustManagers == null) {
        trustManagers = this.parent.engineGetTrustManagers();
        if (PlatformDependent.javaVersion() >= 7)
          for (int i = 0; i < trustManagers.length; i++) {
            TrustManager tm = trustManagers[i];
            if (tm instanceof X509TrustManager && !(tm instanceof javax.net.ssl.X509ExtendedTrustManager))
              trustManagers[i] = new X509TrustManagerWrapper((X509TrustManager)tm); 
          }  
        this.trustManagers = trustManagers;
      } 
      return (TrustManager[])trustManagers.clone();
    }
  }
}
