package com.github.steveice10.packetlib.crypt;

public interface PacketEncryption {
  int getDecryptOutputSize(int paramInt);
  
  int getEncryptOutputSize(int paramInt);
  
  int decrypt(byte[] paramArrayOfbyte1, int paramInt1, int paramInt2, byte[] paramArrayOfbyte2, int paramInt3) throws Exception;
  
  int encrypt(byte[] paramArrayOfbyte1, int paramInt1, int paramInt2, byte[] paramArrayOfbyte2, int paramInt3) throws Exception;
}
