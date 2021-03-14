package com.github.steveice10.netty.handler.codec.http.multipart;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.buffer.ByteBufHolder;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.DecoderResult;
import com.github.steveice10.netty.handler.codec.http.DefaultFullHttpRequest;
import com.github.steveice10.netty.handler.codec.http.DefaultHttpContent;
import com.github.steveice10.netty.handler.codec.http.EmptyHttpHeaders;
import com.github.steveice10.netty.handler.codec.http.FullHttpMessage;
import com.github.steveice10.netty.handler.codec.http.FullHttpRequest;
import com.github.steveice10.netty.handler.codec.http.HttpConstants;
import com.github.steveice10.netty.handler.codec.http.HttpContent;
import com.github.steveice10.netty.handler.codec.http.HttpHeaderNames;
import com.github.steveice10.netty.handler.codec.http.HttpHeaderValues;
import com.github.steveice10.netty.handler.codec.http.HttpHeaders;
import com.github.steveice10.netty.handler.codec.http.HttpMessage;
import com.github.steveice10.netty.handler.codec.http.HttpMethod;
import com.github.steveice10.netty.handler.codec.http.HttpRequest;
import com.github.steveice10.netty.handler.codec.http.HttpUtil;
import com.github.steveice10.netty.handler.codec.http.HttpVersion;
import com.github.steveice10.netty.handler.codec.http.LastHttpContent;
import com.github.steveice10.netty.handler.stream.ChunkedInput;
import com.github.steveice10.netty.util.ReferenceCounted;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.PlatformDependent;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.regex.Pattern;

public class HttpPostRequestEncoder implements ChunkedInput<HttpContent> {
  public enum EncoderMode {
    RFC1738, RFC3986, HTML5;
  }
  
  private static final Map.Entry[] percentEncodings = new Map.Entry[] { new AbstractMap.SimpleImmutableEntry<Pattern, String>(
        Pattern.compile("\\*"), "%2A"), new AbstractMap.SimpleImmutableEntry<Pattern, String>(
        Pattern.compile("\\+"), "%20"), new AbstractMap.SimpleImmutableEntry<Pattern, String>(
        Pattern.compile("~"), "%7E") };
  
  private final HttpDataFactory factory;
  
  private final HttpRequest request;
  
  private final Charset charset;
  
  private boolean isChunked;
  
  private final List<InterfaceHttpData> bodyListDatas;
  
  final List<InterfaceHttpData> multipartHttpDatas;
  
  private final boolean isMultipart;
  
  String multipartDataBoundary;
  
  String multipartMixedBoundary;
  
  private boolean headerFinalized;
  
  private final EncoderMode encoderMode;
  
  private boolean isLastChunk;
  
  private boolean isLastChunkSent;
  
  private FileUpload currentFileUpload;
  
  private boolean duringMixedMode;
  
  private long globalBodySize;
  
  private long globalProgress;
  
  private ListIterator<InterfaceHttpData> iterator;
  
  private ByteBuf currentBuffer;
  
  private InterfaceHttpData currentData;
  
  private boolean isKey;
  
  public HttpPostRequestEncoder(HttpRequest request, boolean multipart) throws ErrorDataEncoderException {
    this(new DefaultHttpDataFactory(16384L), request, multipart, HttpConstants.DEFAULT_CHARSET, EncoderMode.RFC1738);
  }
  
  public HttpPostRequestEncoder(HttpDataFactory factory, HttpRequest request, boolean multipart) throws ErrorDataEncoderException {
    this(factory, request, multipart, HttpConstants.DEFAULT_CHARSET, EncoderMode.RFC1738);
  }
  
  public HttpPostRequestEncoder(HttpDataFactory factory, HttpRequest request, boolean multipart, Charset charset, EncoderMode encoderMode) throws ErrorDataEncoderException {
    this.isKey = true;
    this.request = (HttpRequest)ObjectUtil.checkNotNull(request, "request");
    this.charset = (Charset)ObjectUtil.checkNotNull(charset, "charset");
    this.factory = (HttpDataFactory)ObjectUtil.checkNotNull(factory, "factory");
    if (HttpMethod.TRACE.equals(request.method()))
      throw new ErrorDataEncoderException("Cannot create a Encoder if request is a TRACE"); 
    this.bodyListDatas = new ArrayList<InterfaceHttpData>();
    this.isLastChunk = false;
    this.isLastChunkSent = false;
    this.isMultipart = multipart;
    this.multipartHttpDatas = new ArrayList<InterfaceHttpData>();
    this.encoderMode = encoderMode;
    if (this.isMultipart)
      initDataMultipart(); 
  }
  
  public void cleanFiles() {
    this.factory.cleanRequestHttpData(this.request);
  }
  
  public boolean isMultipart() {
    return this.isMultipart;
  }
  
  private void initDataMultipart() {
    this.multipartDataBoundary = getNewMultipartDelimiter();
  }
  
  private void initMixedMultipart() {
    this.multipartMixedBoundary = getNewMultipartDelimiter();
  }
  
  private static String getNewMultipartDelimiter() {
    return Long.toHexString(PlatformDependent.threadLocalRandom().nextLong());
  }
  
  public List<InterfaceHttpData> getBodyListAttributes() {
    return this.bodyListDatas;
  }
  
  public void setBodyHttpDatas(List<InterfaceHttpData> datas) throws ErrorDataEncoderException {
    if (datas == null)
      throw new NullPointerException("datas"); 
    this.globalBodySize = 0L;
    this.bodyListDatas.clear();
    this.currentFileUpload = null;
    this.duringMixedMode = false;
    this.multipartHttpDatas.clear();
    for (InterfaceHttpData data : datas)
      addBodyHttpData(data); 
  }
  
  public void addBodyAttribute(String name, String value) throws ErrorDataEncoderException {
    String svalue = (value != null) ? value : "";
    Attribute data = this.factory.createAttribute(this.request, (String)ObjectUtil.checkNotNull(name, "name"), svalue);
    addBodyHttpData(data);
  }
  
  public void addBodyFileUpload(String name, File file, String contentType, boolean isText) throws ErrorDataEncoderException {
    addBodyFileUpload(name, file.getName(), file, contentType, isText);
  }
  
  public void addBodyFileUpload(String name, String filename, File file, String contentType, boolean isText) throws ErrorDataEncoderException {
    ObjectUtil.checkNotNull(name, "name");
    ObjectUtil.checkNotNull(file, "file");
    if (filename == null)
      filename = ""; 
    String scontentType = contentType;
    String contentTransferEncoding = null;
    if (contentType == null)
      if (isText) {
        scontentType = "text/plain";
      } else {
        scontentType = "application/octet-stream";
      }  
    if (!isText)
      contentTransferEncoding = HttpPostBodyUtil.TransferEncodingMechanism.BINARY.value(); 
    FileUpload fileUpload = this.factory.createFileUpload(this.request, name, filename, scontentType, contentTransferEncoding, null, file.length());
    try {
      fileUpload.setContent(file);
    } catch (IOException e) {
      throw new ErrorDataEncoderException(e);
    } 
    addBodyHttpData(fileUpload);
  }
  
  public void addBodyFileUploads(String name, File[] file, String[] contentType, boolean[] isText) throws ErrorDataEncoderException {
    if (file.length != contentType.length && file.length != isText.length)
      throw new IllegalArgumentException("Different array length"); 
    for (int i = 0; i < file.length; i++)
      addBodyFileUpload(name, file[i], contentType[i], isText[i]); 
  }
  
  public void addBodyHttpData(InterfaceHttpData data) throws ErrorDataEncoderException {
    if (this.headerFinalized)
      throw new ErrorDataEncoderException("Cannot add value once finalized"); 
    this.bodyListDatas.add(ObjectUtil.checkNotNull(data, "data"));
    if (!this.isMultipart) {
      if (data instanceof Attribute) {
        Attribute attribute = (Attribute)data;
        try {
          String key = encodeAttribute(attribute.getName(), this.charset);
          String value = encodeAttribute(attribute.getValue(), this.charset);
          Attribute newattribute = this.factory.createAttribute(this.request, key, value);
          this.multipartHttpDatas.add(newattribute);
          this.globalBodySize += (newattribute.getName().length() + 1) + newattribute.length() + 1L;
        } catch (IOException e) {
          throw new ErrorDataEncoderException(e);
        } 
      } else if (data instanceof FileUpload) {
        FileUpload fileUpload = (FileUpload)data;
        String key = encodeAttribute(fileUpload.getName(), this.charset);
        String value = encodeAttribute(fileUpload.getFilename(), this.charset);
        Attribute newattribute = this.factory.createAttribute(this.request, key, value);
        this.multipartHttpDatas.add(newattribute);
        this.globalBodySize += (newattribute.getName().length() + 1) + newattribute.length() + 1L;
      } 
      return;
    } 
    if (data instanceof Attribute) {
      if (this.duringMixedMode) {
        InternalAttribute internalAttribute = new InternalAttribute(this.charset);
        internalAttribute.addValue("\r\n--" + this.multipartMixedBoundary + "--");
        this.multipartHttpDatas.add(internalAttribute);
        this.multipartMixedBoundary = null;
        this.currentFileUpload = null;
        this.duringMixedMode = false;
      } 
      InternalAttribute internal = new InternalAttribute(this.charset);
      if (!this.multipartHttpDatas.isEmpty())
        internal.addValue("\r\n"); 
      internal.addValue("--" + this.multipartDataBoundary + "\r\n");
      Attribute attribute = (Attribute)data;
      internal.addValue(HttpHeaderNames.CONTENT_DISPOSITION + ": " + HttpHeaderValues.FORM_DATA + "; " + HttpHeaderValues.NAME + "=\"" + attribute.getName() + "\"\r\n");
      internal.addValue(HttpHeaderNames.CONTENT_LENGTH + ": " + attribute.length() + "\r\n");
      Charset localcharset = attribute.getCharset();
      if (localcharset != null)
        internal.addValue(HttpHeaderNames.CONTENT_TYPE + ": " + "text/plain" + "; " + HttpHeaderValues.CHARSET + '=' + localcharset.name() + "\r\n"); 
      internal.addValue("\r\n");
      this.multipartHttpDatas.add(internal);
      this.multipartHttpDatas.add(data);
      this.globalBodySize += attribute.length() + internal.size();
    } else if (data instanceof FileUpload) {
      boolean localMixed;
      FileUpload fileUpload = (FileUpload)data;
      InternalAttribute internal = new InternalAttribute(this.charset);
      if (!this.multipartHttpDatas.isEmpty())
        internal.addValue("\r\n"); 
      if (this.duringMixedMode) {
        if (this.currentFileUpload != null && this.currentFileUpload.getName().equals(fileUpload.getName())) {
          localMixed = true;
        } else {
          internal.addValue("--" + this.multipartMixedBoundary + "--");
          this.multipartHttpDatas.add(internal);
          this.multipartMixedBoundary = null;
          internal = new InternalAttribute(this.charset);
          internal.addValue("\r\n");
          localMixed = false;
          this.currentFileUpload = fileUpload;
          this.duringMixedMode = false;
        } 
      } else if (this.encoderMode != EncoderMode.HTML5 && this.currentFileUpload != null && this.currentFileUpload.getName().equals(fileUpload.getName())) {
        initMixedMultipart();
        InternalAttribute pastAttribute = (InternalAttribute)this.multipartHttpDatas.get(this.multipartHttpDatas.size() - 2);
        this.globalBodySize -= pastAttribute.size();
        StringBuilder replacement = (new StringBuilder(139 + this.multipartDataBoundary.length() + this.multipartMixedBoundary.length() * 2 + fileUpload.getFilename().length() + fileUpload.getName().length())).append("--").append(this.multipartDataBoundary).append("\r\n").append((CharSequence)HttpHeaderNames.CONTENT_DISPOSITION).append(": ").append((CharSequence)HttpHeaderValues.FORM_DATA).append("; ").append((CharSequence)HttpHeaderValues.NAME).append("=\"").append(fileUpload.getName()).append("\"\r\n").append((CharSequence)HttpHeaderNames.CONTENT_TYPE).append(": ").append((CharSequence)HttpHeaderValues.MULTIPART_MIXED).append("; ").append((CharSequence)HttpHeaderValues.BOUNDARY).append('=').append(this.multipartMixedBoundary).append("\r\n\r\n").append("--").append(this.multipartMixedBoundary).append("\r\n").append((CharSequence)HttpHeaderNames.CONTENT_DISPOSITION).append(": ").append((CharSequence)HttpHeaderValues.ATTACHMENT);
        if (!fileUpload.getFilename().isEmpty())
          replacement.append("; ").append((CharSequence)HttpHeaderValues.FILENAME).append("=\"").append(fileUpload.getFilename()).append('"'); 
        replacement.append("\r\n");
        pastAttribute.setValue(replacement.toString(), 1);
        pastAttribute.setValue("", 2);
        this.globalBodySize += pastAttribute.size();
        localMixed = true;
        this.duringMixedMode = true;
      } else {
        localMixed = false;
        this.currentFileUpload = fileUpload;
        this.duringMixedMode = false;
      } 
      if (localMixed) {
        internal.addValue("--" + this.multipartMixedBoundary + "\r\n");
        if (fileUpload.getFilename().isEmpty()) {
          internal.addValue(HttpHeaderNames.CONTENT_DISPOSITION + ": " + HttpHeaderValues.ATTACHMENT + "\r\n");
        } else {
          internal.addValue(HttpHeaderNames.CONTENT_DISPOSITION + ": " + HttpHeaderValues.ATTACHMENT + "; " + HttpHeaderValues.FILENAME + "=\"" + fileUpload.getFilename() + "\"\r\n");
        } 
      } else {
        internal.addValue("--" + this.multipartDataBoundary + "\r\n");
        if (fileUpload.getFilename().isEmpty()) {
          internal.addValue(HttpHeaderNames.CONTENT_DISPOSITION + ": " + HttpHeaderValues.FORM_DATA + "; " + HttpHeaderValues.NAME + "=\"" + fileUpload.getName() + "\"\r\n");
        } else {
          internal.addValue(HttpHeaderNames.CONTENT_DISPOSITION + ": " + HttpHeaderValues.FORM_DATA + "; " + HttpHeaderValues.NAME + "=\"" + fileUpload.getName() + "\"; " + HttpHeaderValues.FILENAME + "=\"" + fileUpload.getFilename() + "\"\r\n");
        } 
      } 
      internal.addValue(HttpHeaderNames.CONTENT_LENGTH + ": " + fileUpload.length() + "\r\n");
      internal.addValue(HttpHeaderNames.CONTENT_TYPE + ": " + fileUpload.getContentType());
      String contentTransferEncoding = fileUpload.getContentTransferEncoding();
      if (contentTransferEncoding != null && contentTransferEncoding.equals(HttpPostBodyUtil.TransferEncodingMechanism.BINARY.value())) {
        internal.addValue("\r\n" + HttpHeaderNames.CONTENT_TRANSFER_ENCODING + ": " + HttpPostBodyUtil.TransferEncodingMechanism.BINARY.value() + "\r\n\r\n");
      } else if (fileUpload.getCharset() != null) {
        internal.addValue("; " + HttpHeaderValues.CHARSET + '=' + fileUpload.getCharset().name() + "\r\n\r\n");
      } else {
        internal.addValue("\r\n\r\n");
      } 
      this.multipartHttpDatas.add(internal);
      this.multipartHttpDatas.add(data);
      this.globalBodySize += fileUpload.length() + internal.size();
    } 
  }
  
  public HttpRequest finalizeRequest() throws ErrorDataEncoderException {
    if (!this.headerFinalized) {
      if (this.isMultipart) {
        InternalAttribute internal = new InternalAttribute(this.charset);
        if (this.duringMixedMode)
          internal.addValue("\r\n--" + this.multipartMixedBoundary + "--"); 
        internal.addValue("\r\n--" + this.multipartDataBoundary + "--\r\n");
        this.multipartHttpDatas.add(internal);
        this.multipartMixedBoundary = null;
        this.currentFileUpload = null;
        this.duringMixedMode = false;
        this.globalBodySize += internal.size();
      } 
      this.headerFinalized = true;
    } else {
      throw new ErrorDataEncoderException("Header already encoded");
    } 
    HttpHeaders headers = this.request.headers();
    List<String> contentTypes = headers.getAll((CharSequence)HttpHeaderNames.CONTENT_TYPE);
    List<String> transferEncoding = headers.getAll((CharSequence)HttpHeaderNames.TRANSFER_ENCODING);
    if (contentTypes != null) {
      headers.remove((CharSequence)HttpHeaderNames.CONTENT_TYPE);
      for (String contentType : contentTypes) {
        String lowercased = contentType.toLowerCase();
        if (lowercased.startsWith(HttpHeaderValues.MULTIPART_FORM_DATA.toString()) || lowercased.startsWith(HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString()))
          continue; 
        headers.add((CharSequence)HttpHeaderNames.CONTENT_TYPE, contentType);
      } 
    } 
    if (this.isMultipart) {
      String value = HttpHeaderValues.MULTIPART_FORM_DATA + "; " + HttpHeaderValues.BOUNDARY + '=' + this.multipartDataBoundary;
      headers.add((CharSequence)HttpHeaderNames.CONTENT_TYPE, value);
    } else {
      headers.add((CharSequence)HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED);
    } 
    long realSize = this.globalBodySize;
    if (!this.isMultipart)
      realSize--; 
    this.iterator = this.multipartHttpDatas.listIterator();
    headers.set((CharSequence)HttpHeaderNames.CONTENT_LENGTH, String.valueOf(realSize));
    if (realSize > 8096L || this.isMultipart) {
      this.isChunked = true;
      if (transferEncoding != null) {
        headers.remove((CharSequence)HttpHeaderNames.TRANSFER_ENCODING);
        for (CharSequence v : transferEncoding) {
          if (HttpHeaderValues.CHUNKED.contentEqualsIgnoreCase(v))
            continue; 
          headers.add((CharSequence)HttpHeaderNames.TRANSFER_ENCODING, v);
        } 
      } 
      HttpUtil.setTransferEncodingChunked((HttpMessage)this.request, true);
      return new WrappedHttpRequest(this.request);
    } 
    HttpContent chunk = nextChunk();
    if (this.request instanceof FullHttpRequest) {
      FullHttpRequest fullRequest = (FullHttpRequest)this.request;
      ByteBuf chunkContent = chunk.content();
      if (fullRequest.content() != chunkContent) {
        fullRequest.content().clear().writeBytes(chunkContent);
        chunkContent.release();
      } 
      return (HttpRequest)fullRequest;
    } 
    return new WrappedFullHttpRequest(this.request, chunk);
  }
  
  public boolean isChunked() {
    return this.isChunked;
  }
  
  private String encodeAttribute(String s, Charset charset) throws ErrorDataEncoderException {
    if (s == null)
      return ""; 
    try {
      String encoded = URLEncoder.encode(s, charset.name());
      if (this.encoderMode == EncoderMode.RFC3986)
        for (Map.Entry<Pattern, String> entry : percentEncodings) {
          String replacement = entry.getValue();
          encoded = ((Pattern)entry.getKey()).matcher(encoded).replaceAll(replacement);
        }  
      return encoded;
    } catch (UnsupportedEncodingException e) {
      throw new ErrorDataEncoderException(charset.name(), e);
    } 
  }
  
  private ByteBuf fillByteBuf() {
    int length = this.currentBuffer.readableBytes();
    if (length > 8096)
      return this.currentBuffer.readRetainedSlice(8096); 
    ByteBuf slice = this.currentBuffer;
    this.currentBuffer = null;
    return slice;
  }
  
  private HttpContent encodeNextChunkMultipart(int sizeleft) throws ErrorDataEncoderException {
    if (this.currentData == null)
      return null; 
    if (this.currentData instanceof InternalAttribute) {
      buffer = ((InternalAttribute)this.currentData).toByteBuf();
      this.currentData = null;
    } else {
      try {
        buffer = ((HttpData)this.currentData).getChunk(sizeleft);
      } catch (IOException e) {
        throw new ErrorDataEncoderException(e);
      } 
      if (buffer.capacity() == 0) {
        this.currentData = null;
        return null;
      } 
    } 
    if (this.currentBuffer == null) {
      this.currentBuffer = buffer;
    } else {
      this.currentBuffer = Unpooled.wrappedBuffer(new ByteBuf[] { this.currentBuffer, buffer });
    } 
    if (this.currentBuffer.readableBytes() < 8096) {
      this.currentData = null;
      return null;
    } 
    ByteBuf buffer = fillByteBuf();
    return (HttpContent)new DefaultHttpContent(buffer);
  }
  
  private HttpContent encodeNextChunkUrlEncoded(int sizeleft) throws ErrorDataEncoderException {
    if (this.currentData == null)
      return null; 
    int size = sizeleft;
    if (this.isKey) {
      String key = this.currentData.getName();
      buffer = Unpooled.wrappedBuffer(key.getBytes());
      this.isKey = false;
      if (this.currentBuffer == null) {
        this.currentBuffer = Unpooled.wrappedBuffer(new ByteBuf[] { buffer, Unpooled.wrappedBuffer("=".getBytes()) });
      } else {
        this.currentBuffer = Unpooled.wrappedBuffer(new ByteBuf[] { this.currentBuffer, buffer, Unpooled.wrappedBuffer("=".getBytes()) });
      } 
      size -= buffer.readableBytes() + 1;
      if (this.currentBuffer.readableBytes() >= 8096) {
        buffer = fillByteBuf();
        return (HttpContent)new DefaultHttpContent(buffer);
      } 
    } 
    try {
      buffer = ((HttpData)this.currentData).getChunk(size);
    } catch (IOException e) {
      throw new ErrorDataEncoderException(e);
    } 
    ByteBuf delimiter = null;
    if (buffer.readableBytes() < size) {
      this.isKey = true;
      delimiter = this.iterator.hasNext() ? Unpooled.wrappedBuffer("&".getBytes()) : null;
    } 
    if (buffer.capacity() == 0) {
      this.currentData = null;
      if (this.currentBuffer == null) {
        this.currentBuffer = delimiter;
      } else if (delimiter != null) {
        this.currentBuffer = Unpooled.wrappedBuffer(new ByteBuf[] { this.currentBuffer, delimiter });
      } 
      if (this.currentBuffer.readableBytes() >= 8096) {
        buffer = fillByteBuf();
        return (HttpContent)new DefaultHttpContent(buffer);
      } 
      return null;
    } 
    if (this.currentBuffer == null) {
      if (delimiter != null) {
        this.currentBuffer = Unpooled.wrappedBuffer(new ByteBuf[] { buffer, delimiter });
      } else {
        this.currentBuffer = buffer;
      } 
    } else if (delimiter != null) {
      this.currentBuffer = Unpooled.wrappedBuffer(new ByteBuf[] { this.currentBuffer, buffer, delimiter });
    } else {
      this.currentBuffer = Unpooled.wrappedBuffer(new ByteBuf[] { this.currentBuffer, buffer });
    } 
    if (this.currentBuffer.readableBytes() < 8096) {
      this.currentData = null;
      this.isKey = true;
      return null;
    } 
    ByteBuf buffer = fillByteBuf();
    return (HttpContent)new DefaultHttpContent(buffer);
  }
  
  public void close() throws Exception {}
  
  @Deprecated
  public HttpContent readChunk(ChannelHandlerContext ctx) throws Exception {
    return readChunk(ctx.alloc());
  }
  
  public HttpContent readChunk(ByteBufAllocator allocator) throws Exception {
    if (this.isLastChunkSent)
      return null; 
    HttpContent nextChunk = nextChunk();
    this.globalProgress += nextChunk.content().readableBytes();
    return nextChunk;
  }
  
  private HttpContent nextChunk() throws ErrorDataEncoderException {
    if (this.isLastChunk) {
      this.isLastChunkSent = true;
      return (HttpContent)LastHttpContent.EMPTY_LAST_CONTENT;
    } 
    int size = calculateRemainingSize();
    if (size <= 0) {
      ByteBuf buffer = fillByteBuf();
      return (HttpContent)new DefaultHttpContent(buffer);
    } 
    if (this.currentData != null) {
      HttpContent chunk;
      if (this.isMultipart) {
        chunk = encodeNextChunkMultipart(size);
      } else {
        chunk = encodeNextChunkUrlEncoded(size);
      } 
      if (chunk != null)
        return chunk; 
      size = calculateRemainingSize();
    } 
    if (!this.iterator.hasNext())
      return lastChunk(); 
    while (size > 0 && this.iterator.hasNext()) {
      HttpContent chunk;
      this.currentData = this.iterator.next();
      if (this.isMultipart) {
        chunk = encodeNextChunkMultipart(size);
      } else {
        chunk = encodeNextChunkUrlEncoded(size);
      } 
      if (chunk == null) {
        size = calculateRemainingSize();
        continue;
      } 
      return chunk;
    } 
    return lastChunk();
  }
  
  private int calculateRemainingSize() {
    int size = 8096;
    if (this.currentBuffer != null)
      size -= this.currentBuffer.readableBytes(); 
    return size;
  }
  
  private HttpContent lastChunk() {
    this.isLastChunk = true;
    if (this.currentBuffer == null) {
      this.isLastChunkSent = true;
      return (HttpContent)LastHttpContent.EMPTY_LAST_CONTENT;
    } 
    ByteBuf buffer = this.currentBuffer;
    this.currentBuffer = null;
    return (HttpContent)new DefaultHttpContent(buffer);
  }
  
  public boolean isEndOfInput() throws Exception {
    return this.isLastChunkSent;
  }
  
  public long length() {
    return this.isMultipart ? this.globalBodySize : (this.globalBodySize - 1L);
  }
  
  public long progress() {
    return this.globalProgress;
  }
  
  public static class ErrorDataEncoderException extends Exception {
    private static final long serialVersionUID = 5020247425493164465L;
    
    public ErrorDataEncoderException() {}
    
    public ErrorDataEncoderException(String msg) {
      super(msg);
    }
    
    public ErrorDataEncoderException(Throwable cause) {
      super(cause);
    }
    
    public ErrorDataEncoderException(String msg, Throwable cause) {
      super(msg, cause);
    }
  }
  
  private static class WrappedHttpRequest implements HttpRequest {
    private final HttpRequest request;
    
    WrappedHttpRequest(HttpRequest request) {
      this.request = request;
    }
    
    public HttpRequest setProtocolVersion(HttpVersion version) {
      this.request.setProtocolVersion(version);
      return this;
    }
    
    public HttpRequest setMethod(HttpMethod method) {
      this.request.setMethod(method);
      return this;
    }
    
    public HttpRequest setUri(String uri) {
      this.request.setUri(uri);
      return this;
    }
    
    public HttpMethod getMethod() {
      return this.request.method();
    }
    
    public HttpMethod method() {
      return this.request.method();
    }
    
    public String getUri() {
      return this.request.uri();
    }
    
    public String uri() {
      return this.request.uri();
    }
    
    public HttpVersion getProtocolVersion() {
      return this.request.protocolVersion();
    }
    
    public HttpVersion protocolVersion() {
      return this.request.protocolVersion();
    }
    
    public HttpHeaders headers() {
      return this.request.headers();
    }
    
    public DecoderResult decoderResult() {
      return this.request.decoderResult();
    }
    
    @Deprecated
    public DecoderResult getDecoderResult() {
      return this.request.getDecoderResult();
    }
    
    public void setDecoderResult(DecoderResult result) {
      this.request.setDecoderResult(result);
    }
  }
  
  private static final class WrappedFullHttpRequest extends WrappedHttpRequest implements FullHttpRequest {
    private final HttpContent content;
    
    private WrappedFullHttpRequest(HttpRequest request, HttpContent content) {
      super(request);
      this.content = content;
    }
    
    public FullHttpRequest setProtocolVersion(HttpVersion version) {
      super.setProtocolVersion(version);
      return this;
    }
    
    public FullHttpRequest setMethod(HttpMethod method) {
      super.setMethod(method);
      return this;
    }
    
    public FullHttpRequest setUri(String uri) {
      super.setUri(uri);
      return this;
    }
    
    public FullHttpRequest copy() {
      return replace(content().copy());
    }
    
    public FullHttpRequest duplicate() {
      return replace(content().duplicate());
    }
    
    public FullHttpRequest retainedDuplicate() {
      return replace(content().retainedDuplicate());
    }
    
    public FullHttpRequest replace(ByteBuf content) {
      DefaultFullHttpRequest duplicate = new DefaultFullHttpRequest(protocolVersion(), method(), uri(), content);
      duplicate.headers().set(headers());
      duplicate.trailingHeaders().set(trailingHeaders());
      return (FullHttpRequest)duplicate;
    }
    
    public FullHttpRequest retain(int increment) {
      this.content.retain(increment);
      return this;
    }
    
    public FullHttpRequest retain() {
      this.content.retain();
      return this;
    }
    
    public FullHttpRequest touch() {
      this.content.touch();
      return this;
    }
    
    public FullHttpRequest touch(Object hint) {
      this.content.touch(hint);
      return this;
    }
    
    public ByteBuf content() {
      return this.content.content();
    }
    
    public HttpHeaders trailingHeaders() {
      if (this.content instanceof LastHttpContent)
        return ((LastHttpContent)this.content).trailingHeaders(); 
      return (HttpHeaders)EmptyHttpHeaders.INSTANCE;
    }
    
    public int refCnt() {
      return this.content.refCnt();
    }
    
    public boolean release() {
      return this.content.release();
    }
    
    public boolean release(int decrement) {
      return this.content.release(decrement);
    }
  }
}
