package com.github.steveice10.packetlib.io;

import java.io.IOException;
import java.util.UUID;

public interface NetInput {
  boolean readBoolean() throws IOException;
  
  byte readByte() throws IOException;
  
  int readUnsignedByte() throws IOException;
  
  short readShort() throws IOException;
  
  int readUnsignedShort() throws IOException;
  
  char readChar() throws IOException;
  
  int readInt() throws IOException;
  
  int readVarInt() throws IOException;
  
  long readLong() throws IOException;
  
  long readVarLong() throws IOException;
  
  float readFloat() throws IOException;
  
  double readDouble() throws IOException;
  
  byte[] readBytes(int paramInt) throws IOException;
  
  int readBytes(byte[] paramArrayOfbyte) throws IOException;
  
  int readBytes(byte[] paramArrayOfbyte, int paramInt1, int paramInt2) throws IOException;
  
  short[] readShorts(int paramInt) throws IOException;
  
  int readShorts(short[] paramArrayOfshort) throws IOException;
  
  int readShorts(short[] paramArrayOfshort, int paramInt1, int paramInt2) throws IOException;
  
  int[] readInts(int paramInt) throws IOException;
  
  int readInts(int[] paramArrayOfint) throws IOException;
  
  int readInts(int[] paramArrayOfint, int paramInt1, int paramInt2) throws IOException;
  
  long[] readLongs(int paramInt) throws IOException;
  
  int readLongs(long[] paramArrayOflong) throws IOException;
  
  int readLongs(long[] paramArrayOflong, int paramInt1, int paramInt2) throws IOException;
  
  String readString() throws IOException;
  
  UUID readUUID() throws IOException;
  
  int available() throws IOException;
}
