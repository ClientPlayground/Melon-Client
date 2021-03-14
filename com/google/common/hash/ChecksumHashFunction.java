package com.google.common.hash;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import java.io.Serializable;
import java.util.zip.Checksum;

final class ChecksumHashFunction extends AbstractStreamingHashFunction implements Serializable {
  private final Supplier<? extends Checksum> checksumSupplier;
  
  private final int bits;
  
  private final String toString;
  
  private static final long serialVersionUID = 0L;
  
  ChecksumHashFunction(Supplier<? extends Checksum> checksumSupplier, int bits, String toString) {
    this.checksumSupplier = (Supplier<? extends Checksum>)Preconditions.checkNotNull(checksumSupplier);
    Preconditions.checkArgument((bits == 32 || bits == 64), "bits (%s) must be either 32 or 64", new Object[] { Integer.valueOf(bits) });
    this.bits = bits;
    this.toString = (String)Preconditions.checkNotNull(toString);
  }
  
  public int bits() {
    return this.bits;
  }
  
  public Hasher newHasher() {
    return new ChecksumHasher((Checksum)this.checksumSupplier.get());
  }
  
  public String toString() {
    return this.toString;
  }
  
  private final class ChecksumHasher extends AbstractByteHasher {
    private final Checksum checksum;
    
    private ChecksumHasher(Checksum checksum) {
      this.checksum = (Checksum)Preconditions.checkNotNull(checksum);
    }
    
    protected void update(byte b) {
      this.checksum.update(b);
    }
    
    protected void update(byte[] bytes, int off, int len) {
      this.checksum.update(bytes, off, len);
    }
    
    public HashCode hash() {
      long value = this.checksum.getValue();
      if (ChecksumHashFunction.this.bits == 32)
        return HashCode.fromInt((int)value); 
      return HashCode.fromLong(value);
    }
  }
}
