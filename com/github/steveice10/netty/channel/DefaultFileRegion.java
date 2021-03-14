package com.github.steveice10.netty.channel;

import com.github.steveice10.netty.util.AbstractReferenceCounted;
import com.github.steveice10.netty.util.IllegalReferenceCountException;
import com.github.steveice10.netty.util.ReferenceCounted;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;

public class DefaultFileRegion extends AbstractReferenceCounted implements FileRegion {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultFileRegion.class);
  
  private final File f;
  
  private final long position;
  
  private final long count;
  
  private long transferred;
  
  private FileChannel file;
  
  public DefaultFileRegion(FileChannel file, long position, long count) {
    if (file == null)
      throw new NullPointerException("file"); 
    if (position < 0L)
      throw new IllegalArgumentException("position must be >= 0 but was " + position); 
    if (count < 0L)
      throw new IllegalArgumentException("count must be >= 0 but was " + count); 
    this.file = file;
    this.position = position;
    this.count = count;
    this.f = null;
  }
  
  public DefaultFileRegion(File f, long position, long count) {
    if (f == null)
      throw new NullPointerException("f"); 
    if (position < 0L)
      throw new IllegalArgumentException("position must be >= 0 but was " + position); 
    if (count < 0L)
      throw new IllegalArgumentException("count must be >= 0 but was " + count); 
    this.position = position;
    this.count = count;
    this.f = f;
  }
  
  public boolean isOpen() {
    return (this.file != null);
  }
  
  public void open() throws IOException {
    if (!isOpen() && refCnt() > 0)
      this.file = (new RandomAccessFile(this.f, "r")).getChannel(); 
  }
  
  public long position() {
    return this.position;
  }
  
  public long count() {
    return this.count;
  }
  
  @Deprecated
  public long transfered() {
    return this.transferred;
  }
  
  public long transferred() {
    return this.transferred;
  }
  
  public long transferTo(WritableByteChannel target, long position) throws IOException {
    long count = this.count - position;
    if (count < 0L || position < 0L)
      throw new IllegalArgumentException("position out of range: " + position + " (expected: 0 - " + (this.count - 1L) + ')'); 
    if (count == 0L)
      return 0L; 
    if (refCnt() == 0)
      throw new IllegalReferenceCountException(0); 
    open();
    long written = this.file.transferTo(this.position + position, count, target);
    if (written > 0L)
      this.transferred += written; 
    return written;
  }
  
  protected void deallocate() {
    FileChannel file = this.file;
    if (file == null)
      return; 
    this.file = null;
    try {
      file.close();
    } catch (IOException e) {
      if (logger.isWarnEnabled())
        logger.warn("Failed to close a file.", e); 
    } 
  }
  
  public FileRegion retain() {
    super.retain();
    return this;
  }
  
  public FileRegion retain(int increment) {
    super.retain(increment);
    return this;
  }
  
  public FileRegion touch() {
    return this;
  }
  
  public FileRegion touch(Object hint) {
    return this;
  }
}
