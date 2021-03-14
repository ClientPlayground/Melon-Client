package com.github.steveice10.netty.handler.ssl;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PublicKey;
import java.security.SignatureException;
import java.util.Date;
import javax.security.cert.CertificateException;
import javax.security.cert.CertificateExpiredException;
import javax.security.cert.CertificateNotYetValidException;
import javax.security.cert.X509Certificate;

final class OpenSslJavaxX509Certificate extends X509Certificate {
  private final byte[] bytes;
  
  private X509Certificate wrapped;
  
  public OpenSslJavaxX509Certificate(byte[] bytes) {
    this.bytes = bytes;
  }
  
  public void checkValidity() throws CertificateExpiredException, CertificateNotYetValidException {
    unwrap().checkValidity();
  }
  
  public void checkValidity(Date date) throws CertificateExpiredException, CertificateNotYetValidException {
    unwrap().checkValidity(date);
  }
  
  public int getVersion() {
    return unwrap().getVersion();
  }
  
  public BigInteger getSerialNumber() {
    return unwrap().getSerialNumber();
  }
  
  public Principal getIssuerDN() {
    return unwrap().getIssuerDN();
  }
  
  public Principal getSubjectDN() {
    return unwrap().getSubjectDN();
  }
  
  public Date getNotBefore() {
    return unwrap().getNotBefore();
  }
  
  public Date getNotAfter() {
    return unwrap().getNotAfter();
  }
  
  public String getSigAlgName() {
    return unwrap().getSigAlgName();
  }
  
  public String getSigAlgOID() {
    return unwrap().getSigAlgOID();
  }
  
  public byte[] getSigAlgParams() {
    return unwrap().getSigAlgParams();
  }
  
  public byte[] getEncoded() {
    return (byte[])this.bytes.clone();
  }
  
  public void verify(PublicKey key) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
    unwrap().verify(key);
  }
  
  public void verify(PublicKey key, String sigProvider) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
    unwrap().verify(key, sigProvider);
  }
  
  public String toString() {
    return unwrap().toString();
  }
  
  public PublicKey getPublicKey() {
    return unwrap().getPublicKey();
  }
  
  private X509Certificate unwrap() {
    X509Certificate wrapped = this.wrapped;
    if (wrapped == null)
      try {
        wrapped = this.wrapped = X509Certificate.getInstance(this.bytes);
      } catch (CertificateException e) {
        throw new IllegalStateException(e);
      }  
    return wrapped;
  }
}
