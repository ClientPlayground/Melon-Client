package com.github.steveice10.netty.handler.codec.http.multipart;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufHolder;
import com.github.steveice10.netty.channel.ChannelException;
import com.github.steveice10.netty.handler.codec.http.HttpHeaderNames;
import com.github.steveice10.netty.handler.codec.http.HttpHeaderValues;
import com.github.steveice10.netty.util.ReferenceCounted;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class DiskFileUpload extends AbstractDiskHttpData implements FileUpload {
  public static String baseDirectory;
  
  public static boolean deleteOnExitTemporaryFile = true;
  
  public static final String prefix = "FUp_";
  
  public static final String postfix = ".tmp";
  
  private String filename;
  
  private String contentType;
  
  private String contentTransferEncoding;
  
  public DiskFileUpload(String name, String filename, String contentType, String contentTransferEncoding, Charset charset, long size) {
    super(name, charset, size);
    setFilename(filename);
    setContentType(contentType);
    setContentTransferEncoding(contentTransferEncoding);
  }
  
  public InterfaceHttpData.HttpDataType getHttpDataType() {
    return InterfaceHttpData.HttpDataType.FileUpload;
  }
  
  public String getFilename() {
    return this.filename;
  }
  
  public void setFilename(String filename) {
    if (filename == null)
      throw new NullPointerException("filename"); 
    this.filename = filename;
  }
  
  public int hashCode() {
    return FileUploadUtil.hashCode(this);
  }
  
  public boolean equals(Object o) {
    return (o instanceof FileUpload && FileUploadUtil.equals(this, (FileUpload)o));
  }
  
  public int compareTo(InterfaceHttpData o) {
    if (!(o instanceof FileUpload))
      throw new ClassCastException("Cannot compare " + getHttpDataType() + " with " + o
          .getHttpDataType()); 
    return compareTo((FileUpload)o);
  }
  
  public int compareTo(FileUpload o) {
    return FileUploadUtil.compareTo(this, o);
  }
  
  public void setContentType(String contentType) {
    if (contentType == null)
      throw new NullPointerException("contentType"); 
    this.contentType = contentType;
  }
  
  public String getContentType() {
    return this.contentType;
  }
  
  public String getContentTransferEncoding() {
    return this.contentTransferEncoding;
  }
  
  public void setContentTransferEncoding(String contentTransferEncoding) {
    this.contentTransferEncoding = contentTransferEncoding;
  }
  
  public String toString() {
    File file = null;
    try {
      file = getFile();
    } catch (IOException iOException) {}
    return HttpHeaderNames.CONTENT_DISPOSITION + ": " + HttpHeaderValues.FORM_DATA + "; " + HttpHeaderValues.NAME + "=\"" + 
      getName() + "\"; " + HttpHeaderValues.FILENAME + "=\"" + this.filename + "\"\r\n" + HttpHeaderNames.CONTENT_TYPE + ": " + this.contentType + (
      
      (getCharset() != null) ? ("; " + HttpHeaderValues.CHARSET + '=' + getCharset().name() + "\r\n") : "\r\n") + HttpHeaderNames.CONTENT_LENGTH + ": " + 
      length() + "\r\nCompleted: " + 
      isCompleted() + "\r\nIsInMemory: " + 
      isInMemory() + "\r\nRealFile: " + ((file != null) ? file
      .getAbsolutePath() : "null") + " DefaultDeleteAfter: " + deleteOnExitTemporaryFile;
  }
  
  protected boolean deleteOnExit() {
    return deleteOnExitTemporaryFile;
  }
  
  protected String getBaseDirectory() {
    return baseDirectory;
  }
  
  protected String getDiskFilename() {
    return "upload";
  }
  
  protected String getPostfix() {
    return ".tmp";
  }
  
  protected String getPrefix() {
    return "FUp_";
  }
  
  public FileUpload copy() {
    ByteBuf content = content();
    return replace((content != null) ? content.copy() : null);
  }
  
  public FileUpload duplicate() {
    ByteBuf content = content();
    return replace((content != null) ? content.duplicate() : null);
  }
  
  public FileUpload retainedDuplicate() {
    ByteBuf content = content();
    if (content != null) {
      content = content.retainedDuplicate();
      boolean success = false;
      try {
        FileUpload duplicate = replace(content);
        success = true;
        return duplicate;
      } finally {
        if (!success)
          content.release(); 
      } 
    } 
    return replace((ByteBuf)null);
  }
  
  public FileUpload replace(ByteBuf content) {
    DiskFileUpload upload = new DiskFileUpload(getName(), getFilename(), getContentType(), getContentTransferEncoding(), getCharset(), this.size);
    if (content != null)
      try {
        upload.setContent(content);
      } catch (IOException e) {
        throw new ChannelException(e);
      }  
    return upload;
  }
  
  public FileUpload retain(int increment) {
    super.retain(increment);
    return this;
  }
  
  public FileUpload retain() {
    super.retain();
    return this;
  }
  
  public FileUpload touch() {
    super.touch();
    return this;
  }
  
  public FileUpload touch(Object hint) {
    super.touch(hint);
    return this;
  }
}
