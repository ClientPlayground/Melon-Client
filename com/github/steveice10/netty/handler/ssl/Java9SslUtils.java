package com.github.steveice10.netty.handler.ssl;

import com.github.steveice10.netty.util.internal.EmptyArrays;
import com.github.steveice10.netty.util.internal.PlatformDependent;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.List;
import java.util.function.BiFunction;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;

final class Java9SslUtils {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(Java9SslUtils.class);
  
  private static final Method SET_APPLICATION_PROTOCOLS;
  
  private static final Method GET_APPLICATION_PROTOCOL;
  
  private static final Method GET_HANDSHAKE_APPLICATION_PROTOCOL;
  
  private static final Method SET_HANDSHAKE_APPLICATION_PROTOCOL_SELECTOR;
  
  private static final Method GET_HANDSHAKE_APPLICATION_PROTOCOL_SELECTOR;
  
  static {
    Method getHandshakeApplicationProtocol = null;
    Method getApplicationProtocol = null;
    Method setApplicationProtocols = null;
    Method setHandshakeApplicationProtocolSelector = null;
    Method getHandshakeApplicationProtocolSelector = null;
    try {
      SSLContext context = SSLContext.getInstance("TLS");
      context.init(null, null, null);
      SSLEngine engine = context.createSSLEngine();
      getHandshakeApplicationProtocol = AccessController.<Method>doPrivileged(new PrivilegedExceptionAction<Method>() {
            public Method run() throws Exception {
              return SSLEngine.class.getMethod("getHandshakeApplicationProtocol", new Class[0]);
            }
          });
      getHandshakeApplicationProtocol.invoke(engine, new Object[0]);
      getApplicationProtocol = AccessController.<Method>doPrivileged(new PrivilegedExceptionAction<Method>() {
            public Method run() throws Exception {
              return SSLEngine.class.getMethod("getApplicationProtocol", new Class[0]);
            }
          });
      getApplicationProtocol.invoke(engine, new Object[0]);
      setApplicationProtocols = AccessController.<Method>doPrivileged(new PrivilegedExceptionAction<Method>() {
            public Method run() throws Exception {
              return SSLParameters.class.getMethod("setApplicationProtocols", new Class[] { String[].class });
            }
          });
      setApplicationProtocols.invoke(engine.getSSLParameters(), new Object[] { EmptyArrays.EMPTY_STRINGS });
      setHandshakeApplicationProtocolSelector = AccessController.<Method>doPrivileged(new PrivilegedExceptionAction<Method>() {
            public Method run() throws Exception {
              return SSLEngine.class.getMethod("setHandshakeApplicationProtocolSelector", new Class[] { BiFunction.class });
            }
          });
      setHandshakeApplicationProtocolSelector.invoke(engine, new Object[] { new BiFunction<SSLEngine, List<String>, String>() {
              public String apply(SSLEngine sslEngine, List<String> strings) {
                return null;
              }
            } });
      getHandshakeApplicationProtocolSelector = AccessController.<Method>doPrivileged(new PrivilegedExceptionAction<Method>() {
            public Method run() throws Exception {
              return SSLEngine.class.getMethod("getHandshakeApplicationProtocolSelector", new Class[0]);
            }
          });
      getHandshakeApplicationProtocolSelector.invoke(engine, new Object[0]);
    } catch (Throwable t) {
      logger.error("Unable to initialize Java9SslUtils, but the detected javaVersion was: {}", 
          Integer.valueOf(PlatformDependent.javaVersion()), t);
      getHandshakeApplicationProtocol = null;
      getApplicationProtocol = null;
      setApplicationProtocols = null;
      setHandshakeApplicationProtocolSelector = null;
      getHandshakeApplicationProtocolSelector = null;
    } 
    GET_HANDSHAKE_APPLICATION_PROTOCOL = getHandshakeApplicationProtocol;
    GET_APPLICATION_PROTOCOL = getApplicationProtocol;
    SET_APPLICATION_PROTOCOLS = setApplicationProtocols;
    SET_HANDSHAKE_APPLICATION_PROTOCOL_SELECTOR = setHandshakeApplicationProtocolSelector;
    GET_HANDSHAKE_APPLICATION_PROTOCOL_SELECTOR = getHandshakeApplicationProtocolSelector;
  }
  
  static boolean supportsAlpn() {
    return (GET_APPLICATION_PROTOCOL != null);
  }
  
  static String getApplicationProtocol(SSLEngine sslEngine) {
    try {
      return (String)GET_APPLICATION_PROTOCOL.invoke(sslEngine, new Object[0]);
    } catch (UnsupportedOperationException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new IllegalStateException(ex);
    } 
  }
  
  static String getHandshakeApplicationProtocol(SSLEngine sslEngine) {
    try {
      return (String)GET_HANDSHAKE_APPLICATION_PROTOCOL.invoke(sslEngine, new Object[0]);
    } catch (UnsupportedOperationException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new IllegalStateException(ex);
    } 
  }
  
  static void setApplicationProtocols(SSLEngine engine, List<String> supportedProtocols) {
    SSLParameters parameters = engine.getSSLParameters();
    String[] protocolArray = supportedProtocols.<String>toArray(EmptyArrays.EMPTY_STRINGS);
    try {
      SET_APPLICATION_PROTOCOLS.invoke(parameters, new Object[] { protocolArray });
    } catch (UnsupportedOperationException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new IllegalStateException(ex);
    } 
    engine.setSSLParameters(parameters);
  }
  
  static void setHandshakeApplicationProtocolSelector(SSLEngine engine, BiFunction<SSLEngine, List<String>, String> selector) {
    try {
      SET_HANDSHAKE_APPLICATION_PROTOCOL_SELECTOR.invoke(engine, new Object[] { selector });
    } catch (UnsupportedOperationException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new IllegalStateException(ex);
    } 
  }
  
  static BiFunction<SSLEngine, List<String>, String> getHandshakeApplicationProtocolSelector(SSLEngine engine) {
    try {
      return (BiFunction<SSLEngine, List<String>, String>)GET_HANDSHAKE_APPLICATION_PROTOCOL_SELECTOR
        .invoke(engine, new Object[0]);
    } catch (UnsupportedOperationException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new IllegalStateException(ex);
    } 
  }
}
