package com.google.common.io;

import java.io.DataInput;

public interface ByteArrayDataInput extends DataInput {
  void readFully(byte[] paramArrayOfbyte);
  
  void readFully(byte[] paramArrayOfbyte, int paramInt1, int paramInt2);
  
  int skipBytes(int paramInt);
  
  boolean readBoolean();
  
  byte readByte();
  
  int readUnsignedByte();
  
  short readShort();
  
  int readUnsignedShort();
  
  char readChar();
  
  int readInt();
  
  long readLong();
  
  float readFloat();
  
  double readDouble();
  
  String readLine();
  
  String readUTF();
}
