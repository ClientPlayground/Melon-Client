package com.github.steveice10.packetlib.io;

import java.io.IOException;
import java.util.UUID;

public interface NetOutput {
  void writeBoolean(boolean paramBoolean) throws IOException;
  
  void writeByte(int paramInt) throws IOException;
  
  void writeShort(int paramInt) throws IOException;
  
  void writeChar(int paramInt) throws IOException;
  
  void writeInt(int paramInt) throws IOException;
  
  void writeVarInt(int paramInt) throws IOException;
  
  void writeLong(long paramLong) throws IOException;
  
  void writeVarLong(long paramLong) throws IOException;
  
  void writeFloat(float paramFloat) throws IOException;
  
  void writeDouble(double paramDouble) throws IOException;
  
  void writeBytes(byte[] paramArrayOfbyte) throws IOException;
  
  void writeBytes(byte[] paramArrayOfbyte, int paramInt) throws IOException;
  
  void writeShorts(short[] paramArrayOfshort) throws IOException;
  
  void writeShorts(short[] paramArrayOfshort, int paramInt) throws IOException;
  
  void writeInts(int[] paramArrayOfint) throws IOException;
  
  void writeInts(int[] paramArrayOfint, int paramInt) throws IOException;
  
  void writeLongs(long[] paramArrayOflong) throws IOException;
  
  void writeLongs(long[] paramArrayOflong, int paramInt) throws IOException;
  
  void writeString(String paramString) throws IOException;
  
  void writeUUID(UUID paramUUID) throws IOException;
  
  void flush() throws IOException;
}
