package com.replaymod.replaystudio.util;

import com.google.common.base.Preconditions;
import java.io.IOException;
import java.io.OutputStream;

public class ThreadLocalOutputStream extends OutputStream {
  private OutputStream def;
  
  private final ThreadLocal<OutputStream> outputs = new ThreadLocal<>();
  
  public ThreadLocalOutputStream(OutputStream def) {
    this.def = (OutputStream)Preconditions.checkNotNull(def);
  }
  
  public OutputStream getDefault() {
    return this.def;
  }
  
  public void setDefault(OutputStream def) {
    this.def = (OutputStream)Preconditions.checkNotNull(def);
  }
  
  public void setOutput(OutputStream output) {
    this.outputs.set(output);
  }
  
  public void write(int b) throws IOException {
    OutputStream out = this.outputs.get();
    if (out == null)
      out = this.def; 
    out.write(b);
  }
  
  public void write(byte[] b, int off, int len) throws IOException {
    OutputStream out = this.outputs.get();
    if (out == null)
      out = this.def; 
    out.write(b, off, len);
  }
}
