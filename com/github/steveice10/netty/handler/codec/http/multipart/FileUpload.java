package com.github.steveice10.netty.handler.codec.http.multipart;

import com.github.steveice10.netty.buffer.ByteBuf;

public interface FileUpload extends HttpData {
  String getFilename();
  
  void setFilename(String paramString);
  
  void setContentType(String paramString);
  
  String getContentType();
  
  void setContentTransferEncoding(String paramString);
  
  String getContentTransferEncoding();
  
  FileUpload copy();
  
  FileUpload duplicate();
  
  FileUpload retainedDuplicate();
  
  FileUpload replace(ByteBuf paramByteBuf);
  
  FileUpload retain();
  
  FileUpload retain(int paramInt);
  
  FileUpload touch();
  
  FileUpload touch(Object paramObject);
}
