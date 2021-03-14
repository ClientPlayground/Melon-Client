package com.github.steveice10.netty.handler.codec.http.multipart;

import com.github.steveice10.netty.handler.codec.http.HttpConstants;
import com.github.steveice10.netty.handler.codec.http.HttpRequest;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DefaultHttpDataFactory implements HttpDataFactory {
  public static final long MINSIZE = 16384L;
  
  public static final long MAXSIZE = -1L;
  
  private final boolean useDisk;
  
  private final boolean checkSize;
  
  private long minSize;
  
  private long maxSize = -1L;
  
  private Charset charset = HttpConstants.DEFAULT_CHARSET;
  
  private final Map<HttpRequest, List<HttpData>> requestFileDeleteMap = Collections.synchronizedMap(new IdentityHashMap<HttpRequest, List<HttpData>>());
  
  public DefaultHttpDataFactory() {
    this.useDisk = false;
    this.checkSize = true;
    this.minSize = 16384L;
  }
  
  public DefaultHttpDataFactory(Charset charset) {
    this();
    this.charset = charset;
  }
  
  public DefaultHttpDataFactory(boolean useDisk) {
    this.useDisk = useDisk;
    this.checkSize = false;
  }
  
  public DefaultHttpDataFactory(boolean useDisk, Charset charset) {
    this(useDisk);
    this.charset = charset;
  }
  
  public DefaultHttpDataFactory(long minSize) {
    this.useDisk = false;
    this.checkSize = true;
    this.minSize = minSize;
  }
  
  public DefaultHttpDataFactory(long minSize, Charset charset) {
    this(minSize);
    this.charset = charset;
  }
  
  public void setMaxLimit(long maxSize) {
    this.maxSize = maxSize;
  }
  
  private List<HttpData> getList(HttpRequest request) {
    List<HttpData> list = this.requestFileDeleteMap.get(request);
    if (list == null) {
      list = new ArrayList<HttpData>();
      this.requestFileDeleteMap.put(request, list);
    } 
    return list;
  }
  
  public Attribute createAttribute(HttpRequest request, String name) {
    if (this.useDisk) {
      Attribute attribute1 = new DiskAttribute(name, this.charset);
      attribute1.setMaxSize(this.maxSize);
      List<HttpData> list = getList(request);
      list.add(attribute1);
      return attribute1;
    } 
    if (this.checkSize) {
      Attribute attribute1 = new MixedAttribute(name, this.minSize, this.charset);
      attribute1.setMaxSize(this.maxSize);
      List<HttpData> list = getList(request);
      list.add(attribute1);
      return attribute1;
    } 
    MemoryAttribute attribute = new MemoryAttribute(name);
    attribute.setMaxSize(this.maxSize);
    return attribute;
  }
  
  public Attribute createAttribute(HttpRequest request, String name, long definedSize) {
    if (this.useDisk) {
      Attribute attribute1 = new DiskAttribute(name, definedSize, this.charset);
      attribute1.setMaxSize(this.maxSize);
      List<HttpData> list = getList(request);
      list.add(attribute1);
      return attribute1;
    } 
    if (this.checkSize) {
      Attribute attribute1 = new MixedAttribute(name, definedSize, this.minSize, this.charset);
      attribute1.setMaxSize(this.maxSize);
      List<HttpData> list = getList(request);
      list.add(attribute1);
      return attribute1;
    } 
    MemoryAttribute attribute = new MemoryAttribute(name, definedSize);
    attribute.setMaxSize(this.maxSize);
    return attribute;
  }
  
  private static void checkHttpDataSize(HttpData data) {
    try {
      data.checkSize(data.length());
    } catch (IOException ignored) {
      throw new IllegalArgumentException("Attribute bigger than maxSize allowed");
    } 
  }
  
  public Attribute createAttribute(HttpRequest request, String name, String value) {
    if (this.useDisk) {
      Attribute attribute;
      try {
        attribute = new DiskAttribute(name, value, this.charset);
        attribute.setMaxSize(this.maxSize);
      } catch (IOException e) {
        attribute = new MixedAttribute(name, value, this.minSize, this.charset);
        attribute.setMaxSize(this.maxSize);
      } 
      checkHttpDataSize(attribute);
      List<HttpData> list = getList(request);
      list.add(attribute);
      return attribute;
    } 
    if (this.checkSize) {
      Attribute attribute = new MixedAttribute(name, value, this.minSize, this.charset);
      attribute.setMaxSize(this.maxSize);
      checkHttpDataSize(attribute);
      List<HttpData> list = getList(request);
      list.add(attribute);
      return attribute;
    } 
    try {
      MemoryAttribute attribute = new MemoryAttribute(name, value, this.charset);
      attribute.setMaxSize(this.maxSize);
      checkHttpDataSize(attribute);
      return attribute;
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    } 
  }
  
  public FileUpload createFileUpload(HttpRequest request, String name, String filename, String contentType, String contentTransferEncoding, Charset charset, long size) {
    if (this.useDisk) {
      FileUpload fileUpload1 = new DiskFileUpload(name, filename, contentType, contentTransferEncoding, charset, size);
      fileUpload1.setMaxSize(this.maxSize);
      checkHttpDataSize(fileUpload1);
      List<HttpData> list = getList(request);
      list.add(fileUpload1);
      return fileUpload1;
    } 
    if (this.checkSize) {
      FileUpload fileUpload1 = new MixedFileUpload(name, filename, contentType, contentTransferEncoding, charset, size, this.minSize);
      fileUpload1.setMaxSize(this.maxSize);
      checkHttpDataSize(fileUpload1);
      List<HttpData> list = getList(request);
      list.add(fileUpload1);
      return fileUpload1;
    } 
    MemoryFileUpload fileUpload = new MemoryFileUpload(name, filename, contentType, contentTransferEncoding, charset, size);
    fileUpload.setMaxSize(this.maxSize);
    checkHttpDataSize(fileUpload);
    return fileUpload;
  }
  
  public void removeHttpDataFromClean(HttpRequest request, InterfaceHttpData data) {
    if (!(data instanceof HttpData))
      return; 
    List<HttpData> list = this.requestFileDeleteMap.get(request);
    if (list == null)
      return; 
    Iterator<HttpData> i = list.iterator();
    while (i.hasNext()) {
      HttpData n = i.next();
      if (n == data) {
        i.remove();
        if (list.isEmpty())
          this.requestFileDeleteMap.remove(request); 
        return;
      } 
    } 
  }
  
  public void cleanRequestHttpData(HttpRequest request) {
    List<HttpData> list = this.requestFileDeleteMap.remove(request);
    if (list != null)
      for (HttpData data : list)
        data.release();  
  }
  
  public void cleanAllHttpData() {
    Iterator<Map.Entry<HttpRequest, List<HttpData>>> i = this.requestFileDeleteMap.entrySet().iterator();
    while (i.hasNext()) {
      Map.Entry<HttpRequest, List<HttpData>> e = i.next();
      List<HttpData> list = e.getValue();
      for (HttpData data : list)
        data.release(); 
      i.remove();
    } 
  }
  
  public void cleanRequestHttpDatas(HttpRequest request) {
    cleanRequestHttpData(request);
  }
  
  public void cleanAllHttpDatas() {
    cleanAllHttpData();
  }
}
