package com.github.steveice10.netty.handler.codec.http.multipart;

import com.github.steveice10.netty.handler.codec.DecoderException;
import com.github.steveice10.netty.handler.codec.http.HttpConstants;
import com.github.steveice10.netty.handler.codec.http.HttpContent;
import com.github.steveice10.netty.handler.codec.http.HttpHeaderNames;
import com.github.steveice10.netty.handler.codec.http.HttpHeaderValues;
import com.github.steveice10.netty.handler.codec.http.HttpRequest;
import com.github.steveice10.netty.util.internal.StringUtil;
import java.nio.charset.Charset;
import java.util.List;

public class HttpPostRequestDecoder implements InterfaceHttpPostRequestDecoder {
  static final int DEFAULT_DISCARD_THRESHOLD = 10485760;
  
  private final InterfaceHttpPostRequestDecoder decoder;
  
  public HttpPostRequestDecoder(HttpRequest request) {
    this(new DefaultHttpDataFactory(16384L), request, HttpConstants.DEFAULT_CHARSET);
  }
  
  public HttpPostRequestDecoder(HttpDataFactory factory, HttpRequest request) {
    this(factory, request, HttpConstants.DEFAULT_CHARSET);
  }
  
  public HttpPostRequestDecoder(HttpDataFactory factory, HttpRequest request, Charset charset) {
    if (factory == null)
      throw new NullPointerException("factory"); 
    if (request == null)
      throw new NullPointerException("request"); 
    if (charset == null)
      throw new NullPointerException("charset"); 
    if (isMultipart(request)) {
      this.decoder = new HttpPostMultipartRequestDecoder(factory, request, charset);
    } else {
      this.decoder = new HttpPostStandardRequestDecoder(factory, request, charset);
    } 
  }
  
  protected enum MultiPartStatus {
    NOTSTARTED, PREAMBLE, HEADERDELIMITER, DISPOSITION, FIELD, FILEUPLOAD, MIXEDPREAMBLE, MIXEDDELIMITER, MIXEDDISPOSITION, MIXEDFILEUPLOAD, MIXEDCLOSEDELIMITER, CLOSEDELIMITER, PREEPILOGUE, EPILOGUE;
  }
  
  public static boolean isMultipart(HttpRequest request) {
    if (request.headers().contains((CharSequence)HttpHeaderNames.CONTENT_TYPE))
      return (getMultipartDataBoundary(request.headers().get((CharSequence)HttpHeaderNames.CONTENT_TYPE)) != null); 
    return false;
  }
  
  protected static String[] getMultipartDataBoundary(String contentType) {
    String[] headerContentType = splitHeaderContentType(contentType);
    String multiPartHeader = HttpHeaderValues.MULTIPART_FORM_DATA.toString();
    if (headerContentType[0].regionMatches(true, 0, multiPartHeader, 0, multiPartHeader.length())) {
      int mrank, crank;
      String boundaryHeader = HttpHeaderValues.BOUNDARY.toString();
      if (headerContentType[1].regionMatches(true, 0, boundaryHeader, 0, boundaryHeader.length())) {
        mrank = 1;
        crank = 2;
      } else if (headerContentType[2].regionMatches(true, 0, boundaryHeader, 0, boundaryHeader.length())) {
        mrank = 2;
        crank = 1;
      } else {
        return null;
      } 
      String boundary = StringUtil.substringAfter(headerContentType[mrank], '=');
      if (boundary == null)
        throw new ErrorDataDecoderException("Needs a boundary value"); 
      if (boundary.charAt(0) == '"') {
        String bound = boundary.trim();
        int index = bound.length() - 1;
        if (bound.charAt(index) == '"')
          boundary = bound.substring(1, index); 
      } 
      String charsetHeader = HttpHeaderValues.CHARSET.toString();
      if (headerContentType[crank].regionMatches(true, 0, charsetHeader, 0, charsetHeader.length())) {
        String charset = StringUtil.substringAfter(headerContentType[crank], '=');
        if (charset != null)
          return new String[] { "--" + boundary, charset }; 
      } 
      return new String[] { "--" + boundary };
    } 
    return null;
  }
  
  public boolean isMultipart() {
    return this.decoder.isMultipart();
  }
  
  public void setDiscardThreshold(int discardThreshold) {
    this.decoder.setDiscardThreshold(discardThreshold);
  }
  
  public int getDiscardThreshold() {
    return this.decoder.getDiscardThreshold();
  }
  
  public List<InterfaceHttpData> getBodyHttpDatas() {
    return this.decoder.getBodyHttpDatas();
  }
  
  public List<InterfaceHttpData> getBodyHttpDatas(String name) {
    return this.decoder.getBodyHttpDatas(name);
  }
  
  public InterfaceHttpData getBodyHttpData(String name) {
    return this.decoder.getBodyHttpData(name);
  }
  
  public InterfaceHttpPostRequestDecoder offer(HttpContent content) {
    return this.decoder.offer(content);
  }
  
  public boolean hasNext() {
    return this.decoder.hasNext();
  }
  
  public InterfaceHttpData next() {
    return this.decoder.next();
  }
  
  public InterfaceHttpData currentPartialHttpData() {
    return this.decoder.currentPartialHttpData();
  }
  
  public void destroy() {
    this.decoder.destroy();
  }
  
  public void cleanFiles() {
    this.decoder.cleanFiles();
  }
  
  public void removeHttpDataFromClean(InterfaceHttpData data) {
    this.decoder.removeHttpDataFromClean(data);
  }
  
  private static String[] splitHeaderContentType(String sb) {
    int aStart = HttpPostBodyUtil.findNonWhitespace(sb, 0);
    int aEnd = sb.indexOf(';');
    if (aEnd == -1)
      return new String[] { sb, "", "" }; 
    int bStart = HttpPostBodyUtil.findNonWhitespace(sb, aEnd + 1);
    if (sb.charAt(aEnd - 1) == ' ')
      aEnd--; 
    int bEnd = sb.indexOf(';', bStart);
    if (bEnd == -1) {
      bEnd = HttpPostBodyUtil.findEndOfString(sb);
      return new String[] { sb.substring(aStart, aEnd), sb.substring(bStart, bEnd), "" };
    } 
    int cStart = HttpPostBodyUtil.findNonWhitespace(sb, bEnd + 1);
    if (sb.charAt(bEnd - 1) == ' ')
      bEnd--; 
    int cEnd = HttpPostBodyUtil.findEndOfString(sb);
    return new String[] { sb.substring(aStart, aEnd), sb.substring(bStart, bEnd), sb.substring(cStart, cEnd) };
  }
  
  public static class NotEnoughDataDecoderException extends DecoderException {
    private static final long serialVersionUID = -7846841864603865638L;
    
    public NotEnoughDataDecoderException() {}
    
    public NotEnoughDataDecoderException(String msg) {
      super(msg);
    }
    
    public NotEnoughDataDecoderException(Throwable cause) {
      super(cause);
    }
    
    public NotEnoughDataDecoderException(String msg, Throwable cause) {
      super(msg, cause);
    }
  }
  
  public static class EndOfDataDecoderException extends DecoderException {
    private static final long serialVersionUID = 1336267941020800769L;
  }
  
  public static class ErrorDataDecoderException extends DecoderException {
    private static final long serialVersionUID = 5020247425493164465L;
    
    public ErrorDataDecoderException() {}
    
    public ErrorDataDecoderException(String msg) {
      super(msg);
    }
    
    public ErrorDataDecoderException(Throwable cause) {
      super(cause);
    }
    
    public ErrorDataDecoderException(String msg, Throwable cause) {
      super(msg, cause);
    }
  }
}
