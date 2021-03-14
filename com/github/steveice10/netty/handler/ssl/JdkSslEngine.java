package com.github.steveice10.netty.handler.ssl;

import java.nio.ByteBuffer;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;

class JdkSslEngine extends SSLEngine implements ApplicationProtocolAccessor {
  private final SSLEngine engine;
  
  private volatile String applicationProtocol;
  
  JdkSslEngine(SSLEngine engine) {
    this.engine = engine;
  }
  
  public String getNegotiatedApplicationProtocol() {
    return this.applicationProtocol;
  }
  
  void setNegotiatedApplicationProtocol(String applicationProtocol) {
    this.applicationProtocol = applicationProtocol;
  }
  
  public SSLSession getSession() {
    return this.engine.getSession();
  }
  
  public SSLEngine getWrappedEngine() {
    return this.engine;
  }
  
  public void closeInbound() throws SSLException {
    this.engine.closeInbound();
  }
  
  public void closeOutbound() {
    this.engine.closeOutbound();
  }
  
  public String getPeerHost() {
    return this.engine.getPeerHost();
  }
  
  public int getPeerPort() {
    return this.engine.getPeerPort();
  }
  
  public SSLEngineResult wrap(ByteBuffer byteBuffer, ByteBuffer byteBuffer2) throws SSLException {
    return this.engine.wrap(byteBuffer, byteBuffer2);
  }
  
  public SSLEngineResult wrap(ByteBuffer[] byteBuffers, ByteBuffer byteBuffer) throws SSLException {
    return this.engine.wrap(byteBuffers, byteBuffer);
  }
  
  public SSLEngineResult wrap(ByteBuffer[] byteBuffers, int i, int i2, ByteBuffer byteBuffer) throws SSLException {
    return this.engine.wrap(byteBuffers, i, i2, byteBuffer);
  }
  
  public SSLEngineResult unwrap(ByteBuffer byteBuffer, ByteBuffer byteBuffer2) throws SSLException {
    return this.engine.unwrap(byteBuffer, byteBuffer2);
  }
  
  public SSLEngineResult unwrap(ByteBuffer byteBuffer, ByteBuffer[] byteBuffers) throws SSLException {
    return this.engine.unwrap(byteBuffer, byteBuffers);
  }
  
  public SSLEngineResult unwrap(ByteBuffer byteBuffer, ByteBuffer[] byteBuffers, int i, int i2) throws SSLException {
    return this.engine.unwrap(byteBuffer, byteBuffers, i, i2);
  }
  
  public Runnable getDelegatedTask() {
    return this.engine.getDelegatedTask();
  }
  
  public boolean isInboundDone() {
    return this.engine.isInboundDone();
  }
  
  public boolean isOutboundDone() {
    return this.engine.isOutboundDone();
  }
  
  public String[] getSupportedCipherSuites() {
    return this.engine.getSupportedCipherSuites();
  }
  
  public String[] getEnabledCipherSuites() {
    return this.engine.getEnabledCipherSuites();
  }
  
  public void setEnabledCipherSuites(String[] strings) {
    this.engine.setEnabledCipherSuites(strings);
  }
  
  public String[] getSupportedProtocols() {
    return this.engine.getSupportedProtocols();
  }
  
  public String[] getEnabledProtocols() {
    return this.engine.getEnabledProtocols();
  }
  
  public void setEnabledProtocols(String[] strings) {
    this.engine.setEnabledProtocols(strings);
  }
  
  public SSLSession getHandshakeSession() {
    return this.engine.getHandshakeSession();
  }
  
  public void beginHandshake() throws SSLException {
    this.engine.beginHandshake();
  }
  
  public SSLEngineResult.HandshakeStatus getHandshakeStatus() {
    return this.engine.getHandshakeStatus();
  }
  
  public void setUseClientMode(boolean b) {
    this.engine.setUseClientMode(b);
  }
  
  public boolean getUseClientMode() {
    return this.engine.getUseClientMode();
  }
  
  public void setNeedClientAuth(boolean b) {
    this.engine.setNeedClientAuth(b);
  }
  
  public boolean getNeedClientAuth() {
    return this.engine.getNeedClientAuth();
  }
  
  public void setWantClientAuth(boolean b) {
    this.engine.setWantClientAuth(b);
  }
  
  public boolean getWantClientAuth() {
    return this.engine.getWantClientAuth();
  }
  
  public void setEnableSessionCreation(boolean b) {
    this.engine.setEnableSessionCreation(b);
  }
  
  public boolean getEnableSessionCreation() {
    return this.engine.getEnableSessionCreation();
  }
  
  public SSLParameters getSSLParameters() {
    return this.engine.getSSLParameters();
  }
  
  public void setSSLParameters(SSLParameters sslParameters) {
    this.engine.setSSLParameters(sslParameters);
  }
}
