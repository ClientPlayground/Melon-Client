package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.handler.codec.Headers;
import com.github.steveice10.netty.handler.codec.UnsupportedValueConverter;
import com.github.steveice10.netty.handler.codec.ValueConverter;
import com.github.steveice10.netty.handler.codec.http.DefaultFullHttpRequest;
import com.github.steveice10.netty.handler.codec.http.DefaultFullHttpResponse;
import com.github.steveice10.netty.handler.codec.http.DefaultHttpRequest;
import com.github.steveice10.netty.handler.codec.http.DefaultHttpResponse;
import com.github.steveice10.netty.handler.codec.http.FullHttpMessage;
import com.github.steveice10.netty.handler.codec.http.FullHttpRequest;
import com.github.steveice10.netty.handler.codec.http.FullHttpResponse;
import com.github.steveice10.netty.handler.codec.http.HttpHeaderNames;
import com.github.steveice10.netty.handler.codec.http.HttpHeaderValues;
import com.github.steveice10.netty.handler.codec.http.HttpHeaders;
import com.github.steveice10.netty.handler.codec.http.HttpMessage;
import com.github.steveice10.netty.handler.codec.http.HttpMethod;
import com.github.steveice10.netty.handler.codec.http.HttpRequest;
import com.github.steveice10.netty.handler.codec.http.HttpResponse;
import com.github.steveice10.netty.handler.codec.http.HttpResponseStatus;
import com.github.steveice10.netty.handler.codec.http.HttpScheme;
import com.github.steveice10.netty.handler.codec.http.HttpUtil;
import com.github.steveice10.netty.handler.codec.http.HttpVersion;
import com.github.steveice10.netty.util.AsciiString;
import com.github.steveice10.netty.util.ByteProcessor;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.StringUtil;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class HttpConversionUtil {
  private static final CharSequenceMap<AsciiString> HTTP_TO_HTTP2_HEADER_BLACKLIST = new CharSequenceMap<AsciiString>();
  
  static {
    HTTP_TO_HTTP2_HEADER_BLACKLIST.add(HttpHeaderNames.CONNECTION, AsciiString.EMPTY_STRING);
    AsciiString keepAlive = HttpHeaderNames.KEEP_ALIVE;
    HTTP_TO_HTTP2_HEADER_BLACKLIST.add(keepAlive, AsciiString.EMPTY_STRING);
    AsciiString proxyConnection = HttpHeaderNames.PROXY_CONNECTION;
    HTTP_TO_HTTP2_HEADER_BLACKLIST.add(proxyConnection, AsciiString.EMPTY_STRING);
    HTTP_TO_HTTP2_HEADER_BLACKLIST.add(HttpHeaderNames.TRANSFER_ENCODING, AsciiString.EMPTY_STRING);
    HTTP_TO_HTTP2_HEADER_BLACKLIST.add(HttpHeaderNames.HOST, AsciiString.EMPTY_STRING);
    HTTP_TO_HTTP2_HEADER_BLACKLIST.add(HttpHeaderNames.UPGRADE, AsciiString.EMPTY_STRING);
    HTTP_TO_HTTP2_HEADER_BLACKLIST.add(ExtensionHeaderNames.STREAM_ID.text(), AsciiString.EMPTY_STRING);
    HTTP_TO_HTTP2_HEADER_BLACKLIST.add(ExtensionHeaderNames.SCHEME.text(), AsciiString.EMPTY_STRING);
    HTTP_TO_HTTP2_HEADER_BLACKLIST.add(ExtensionHeaderNames.PATH.text(), AsciiString.EMPTY_STRING);
  }
  
  public static final HttpMethod OUT_OF_MESSAGE_SEQUENCE_METHOD = HttpMethod.OPTIONS;
  
  public static final String OUT_OF_MESSAGE_SEQUENCE_PATH = "";
  
  public static final HttpResponseStatus OUT_OF_MESSAGE_SEQUENCE_RETURN_CODE = HttpResponseStatus.OK;
  
  private static final AsciiString EMPTY_REQUEST_PATH = AsciiString.cached("/");
  
  public enum ExtensionHeaderNames {
    STREAM_ID("x-http2-stream-id"),
    SCHEME("x-http2-scheme"),
    PATH("x-http2-path"),
    STREAM_PROMISE_ID("x-http2-stream-promise-id"),
    STREAM_DEPENDENCY_ID("x-http2-stream-dependency-id"),
    STREAM_WEIGHT("x-http2-stream-weight");
    
    private final AsciiString text;
    
    ExtensionHeaderNames(String text) {
      this.text = AsciiString.cached(text);
    }
    
    public AsciiString text() {
      return this.text;
    }
  }
  
  public static HttpResponseStatus parseStatus(CharSequence status) throws Http2Exception {
    HttpResponseStatus result;
    try {
      result = HttpResponseStatus.parseLine(status);
      if (result == HttpResponseStatus.SWITCHING_PROTOCOLS)
        throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Invalid HTTP/2 status code '%d'", new Object[] { Integer.valueOf(result.code()) }); 
    } catch (Http2Exception e) {
      throw e;
    } catch (Throwable t) {
      throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, t, "Unrecognized HTTP status code '%s' encountered in translation to HTTP/1.x", new Object[] { status });
    } 
    return result;
  }
  
  public static FullHttpResponse toFullHttpResponse(int streamId, Http2Headers http2Headers, ByteBufAllocator alloc, boolean validateHttpHeaders) throws Http2Exception {
    HttpResponseStatus status = parseStatus(http2Headers.status());
    DefaultFullHttpResponse defaultFullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, alloc.buffer(), validateHttpHeaders);
    try {
      addHttp2ToHttpHeaders(streamId, http2Headers, (FullHttpMessage)defaultFullHttpResponse, false);
    } catch (Http2Exception e) {
      defaultFullHttpResponse.release();
      throw e;
    } catch (Throwable t) {
      defaultFullHttpResponse.release();
      throw Http2Exception.streamError(streamId, Http2Error.PROTOCOL_ERROR, t, "HTTP/2 to HTTP/1.x headers conversion error", new Object[0]);
    } 
    return (FullHttpResponse)defaultFullHttpResponse;
  }
  
  public static FullHttpRequest toFullHttpRequest(int streamId, Http2Headers http2Headers, ByteBufAllocator alloc, boolean validateHttpHeaders) throws Http2Exception {
    CharSequence method = (CharSequence)ObjectUtil.checkNotNull(http2Headers.method(), "method header cannot be null in conversion to HTTP/1.x");
    CharSequence path = (CharSequence)ObjectUtil.checkNotNull(http2Headers.path(), "path header cannot be null in conversion to HTTP/1.x");
    DefaultFullHttpRequest defaultFullHttpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.valueOf(method.toString()), path.toString(), alloc.buffer(), validateHttpHeaders);
    try {
      addHttp2ToHttpHeaders(streamId, http2Headers, (FullHttpMessage)defaultFullHttpRequest, false);
    } catch (Http2Exception e) {
      defaultFullHttpRequest.release();
      throw e;
    } catch (Throwable t) {
      defaultFullHttpRequest.release();
      throw Http2Exception.streamError(streamId, Http2Error.PROTOCOL_ERROR, t, "HTTP/2 to HTTP/1.x headers conversion error", new Object[0]);
    } 
    return (FullHttpRequest)defaultFullHttpRequest;
  }
  
  public static HttpRequest toHttpRequest(int streamId, Http2Headers http2Headers, boolean validateHttpHeaders) throws Http2Exception {
    CharSequence method = (CharSequence)ObjectUtil.checkNotNull(http2Headers.method(), "method header cannot be null in conversion to HTTP/1.x");
    CharSequence path = (CharSequence)ObjectUtil.checkNotNull(http2Headers.path(), "path header cannot be null in conversion to HTTP/1.x");
    DefaultHttpRequest defaultHttpRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.valueOf(method.toString()), path.toString(), validateHttpHeaders);
    try {
      addHttp2ToHttpHeaders(streamId, http2Headers, defaultHttpRequest.headers(), defaultHttpRequest.protocolVersion(), false, true);
    } catch (Http2Exception e) {
      throw e;
    } catch (Throwable t) {
      throw Http2Exception.streamError(streamId, Http2Error.PROTOCOL_ERROR, t, "HTTP/2 to HTTP/1.x headers conversion error", new Object[0]);
    } 
    return (HttpRequest)defaultHttpRequest;
  }
  
  public static HttpResponse toHttpResponse(int streamId, Http2Headers http2Headers, boolean validateHttpHeaders) throws Http2Exception {
    HttpResponseStatus status = parseStatus(http2Headers.status());
    DefaultHttpResponse defaultHttpResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1, status, validateHttpHeaders);
    try {
      addHttp2ToHttpHeaders(streamId, http2Headers, defaultHttpResponse.headers(), defaultHttpResponse.protocolVersion(), false, true);
    } catch (Http2Exception e) {
      throw e;
    } catch (Throwable t) {
      throw Http2Exception.streamError(streamId, Http2Error.PROTOCOL_ERROR, t, "HTTP/2 to HTTP/1.x headers conversion error", new Object[0]);
    } 
    return (HttpResponse)defaultHttpResponse;
  }
  
  public static void addHttp2ToHttpHeaders(int streamId, Http2Headers sourceHeaders, FullHttpMessage destinationMessage, boolean addToTrailer) throws Http2Exception {
    addHttp2ToHttpHeaders(streamId, sourceHeaders, addToTrailer ? destinationMessage
        .trailingHeaders() : destinationMessage.headers(), destinationMessage
        .protocolVersion(), addToTrailer, destinationMessage instanceof HttpRequest);
  }
  
  public static void addHttp2ToHttpHeaders(int streamId, Http2Headers inputHeaders, HttpHeaders outputHeaders, HttpVersion httpVersion, boolean isTrailer, boolean isRequest) throws Http2Exception {
    Http2ToHttpHeaderTranslator translator = new Http2ToHttpHeaderTranslator(streamId, outputHeaders, isRequest);
    try {
      for (Map.Entry<CharSequence, CharSequence> entry : (Iterable<Map.Entry<CharSequence, CharSequence>>)inputHeaders)
        translator.translate(entry); 
    } catch (Http2Exception ex) {
      throw ex;
    } catch (Throwable t) {
      throw Http2Exception.streamError(streamId, Http2Error.PROTOCOL_ERROR, t, "HTTP/2 to HTTP/1.x headers conversion error", new Object[0]);
    } 
    outputHeaders.remove((CharSequence)HttpHeaderNames.TRANSFER_ENCODING);
    outputHeaders.remove((CharSequence)HttpHeaderNames.TRAILER);
    if (!isTrailer) {
      outputHeaders.setInt((CharSequence)ExtensionHeaderNames.STREAM_ID.text(), streamId);
      HttpUtil.setKeepAlive(outputHeaders, httpVersion, true);
    } 
  }
  
  public static Http2Headers toHttp2Headers(HttpMessage in, boolean validateHeaders) {
    HttpHeaders inHeaders = in.headers();
    Http2Headers out = new DefaultHttp2Headers(validateHeaders, inHeaders.size());
    if (in instanceof HttpRequest) {
      HttpRequest request = (HttpRequest)in;
      URI requestTargetUri = URI.create(request.uri());
      out.path((CharSequence)toHttp2Path(requestTargetUri));
      out.method((CharSequence)request.method().asciiName());
      setHttp2Scheme(inHeaders, requestTargetUri, out);
      if (!HttpUtil.isOriginForm(requestTargetUri) && !HttpUtil.isAsteriskForm(requestTargetUri)) {
        String host = inHeaders.getAsString((CharSequence)HttpHeaderNames.HOST);
        setHttp2Authority((host == null || host.isEmpty()) ? requestTargetUri.getAuthority() : host, out);
      } 
    } else if (in instanceof HttpResponse) {
      HttpResponse response = (HttpResponse)in;
      out.status((CharSequence)response.status().codeAsText());
    } 
    toHttp2Headers(inHeaders, out);
    return out;
  }
  
  public static Http2Headers toHttp2Headers(HttpHeaders inHeaders, boolean validateHeaders) {
    if (inHeaders.isEmpty())
      return EmptyHttp2Headers.INSTANCE; 
    Http2Headers out = new DefaultHttp2Headers(validateHeaders, inHeaders.size());
    toHttp2Headers(inHeaders, out);
    return out;
  }
  
  private static CharSequenceMap<AsciiString> toLowercaseMap(Iterator<? extends CharSequence> valuesIter, int arraySizeHint) {
    UnsupportedValueConverter<AsciiString> valueConverter = UnsupportedValueConverter.instance();
    CharSequenceMap<AsciiString> result = new CharSequenceMap<AsciiString>(true, (ValueConverter<AsciiString>)valueConverter, arraySizeHint);
    while (valuesIter.hasNext()) {
      AsciiString lowerCased = AsciiString.of(valuesIter.next()).toLowerCase();
      try {
        int index = lowerCased.forEachByte(ByteProcessor.FIND_COMMA);
        if (index != -1) {
          int start = 0;
          do {
            result.add(lowerCased.subSequence(start, index, false).trim(), AsciiString.EMPTY_STRING);
            start = index + 1;
          } while (start < lowerCased.length() && (
            index = lowerCased.forEachByte(start, lowerCased.length() - start, ByteProcessor.FIND_COMMA)) != -1);
          result.add(lowerCased.subSequence(start, lowerCased.length(), false).trim(), AsciiString.EMPTY_STRING);
          continue;
        } 
        result.add(lowerCased.trim(), AsciiString.EMPTY_STRING);
      } catch (Exception e) {
        throw new IllegalStateException(e);
      } 
    } 
    return result;
  }
  
  private static void toHttp2HeadersFilterTE(Map.Entry<CharSequence, CharSequence> entry, Http2Headers out) {
    if (AsciiString.indexOf(entry.getValue(), ',', 0) == -1) {
      if (AsciiString.contentEqualsIgnoreCase(AsciiString.trim(entry.getValue()), (CharSequence)HttpHeaderValues.TRAILERS))
        out.add(HttpHeaderNames.TE, HttpHeaderValues.TRAILERS); 
    } else {
      List<CharSequence> teValues = StringUtil.unescapeCsvFields(entry.getValue());
      for (CharSequence teValue : teValues) {
        if (AsciiString.contentEqualsIgnoreCase(AsciiString.trim(teValue), (CharSequence)HttpHeaderValues.TRAILERS)) {
          out.add(HttpHeaderNames.TE, HttpHeaderValues.TRAILERS);
          break;
        } 
      } 
    } 
  }
  
  public static void toHttp2Headers(HttpHeaders inHeaders, Http2Headers out) {
    Iterator<Map.Entry<CharSequence, CharSequence>> iter = inHeaders.iteratorCharSequence();
    CharSequenceMap<AsciiString> connectionBlacklist = toLowercaseMap(inHeaders.valueCharSequenceIterator((CharSequence)HttpHeaderNames.CONNECTION), 8);
    while (iter.hasNext()) {
      Map.Entry<CharSequence, CharSequence> entry = iter.next();
      AsciiString aName = AsciiString.of(entry.getKey()).toLowerCase();
      if (!HTTP_TO_HTTP2_HEADER_BLACKLIST.contains(aName) && !connectionBlacklist.contains(aName)) {
        if (aName.contentEqualsIgnoreCase((CharSequence)HttpHeaderNames.TE)) {
          toHttp2HeadersFilterTE(entry, out);
          continue;
        } 
        if (aName.contentEqualsIgnoreCase((CharSequence)HttpHeaderNames.COOKIE)) {
          AsciiString value = AsciiString.of(entry.getValue());
          try {
            int index = value.forEachByte(ByteProcessor.FIND_SEMI_COLON);
            if (index != -1) {
              int start = 0;
              do {
                out.add(HttpHeaderNames.COOKIE, value.subSequence(start, index, false));
                start = index + 2;
              } while (start < value.length() && (
                index = value.forEachByte(start, value.length() - start, ByteProcessor.FIND_SEMI_COLON)) != -1);
              if (start >= value.length())
                throw new IllegalArgumentException("cookie value is of unexpected format: " + value); 
              out.add(HttpHeaderNames.COOKIE, value.subSequence(start, value.length(), false));
              continue;
            } 
            out.add(HttpHeaderNames.COOKIE, value);
          } catch (Exception e) {
            throw new IllegalStateException(e);
          } 
          continue;
        } 
        out.add(aName, entry.getValue());
      } 
    } 
  }
  
  private static AsciiString toHttp2Path(URI uri) {
    StringBuilder pathBuilder = new StringBuilder(StringUtil.length(uri.getRawPath()) + StringUtil.length(uri.getRawQuery()) + StringUtil.length(uri.getRawFragment()) + 2);
    if (!StringUtil.isNullOrEmpty(uri.getRawPath()))
      pathBuilder.append(uri.getRawPath()); 
    if (!StringUtil.isNullOrEmpty(uri.getRawQuery())) {
      pathBuilder.append('?');
      pathBuilder.append(uri.getRawQuery());
    } 
    if (!StringUtil.isNullOrEmpty(uri.getRawFragment())) {
      pathBuilder.append('#');
      pathBuilder.append(uri.getRawFragment());
    } 
    String path = pathBuilder.toString();
    return path.isEmpty() ? EMPTY_REQUEST_PATH : new AsciiString(path);
  }
  
  static void setHttp2Authority(String authority, Http2Headers out) {
    if (authority != null)
      if (authority.isEmpty()) {
        out.authority((CharSequence)AsciiString.EMPTY_STRING);
      } else {
        int start = authority.indexOf('@') + 1;
        int length = authority.length() - start;
        if (length == 0)
          throw new IllegalArgumentException("authority: " + authority); 
        out.authority((CharSequence)new AsciiString(authority, start, length));
      }  
  }
  
  private static void setHttp2Scheme(HttpHeaders in, URI uri, Http2Headers out) {
    String value = uri.getScheme();
    if (value != null) {
      out.scheme((CharSequence)new AsciiString(value));
      return;
    } 
    CharSequence cValue = in.get((CharSequence)ExtensionHeaderNames.SCHEME.text());
    if (cValue != null) {
      out.scheme((CharSequence)AsciiString.of(cValue));
      return;
    } 
    if (uri.getPort() == HttpScheme.HTTPS.port()) {
      out.scheme((CharSequence)HttpScheme.HTTPS.name());
    } else if (uri.getPort() == HttpScheme.HTTP.port()) {
      out.scheme((CharSequence)HttpScheme.HTTP.name());
    } else {
      throw new IllegalArgumentException(":scheme must be specified. see https://tools.ietf.org/html/rfc7540#section-8.1.2.3");
    } 
  }
  
  private static final class Http2ToHttpHeaderTranslator {
    private static final CharSequenceMap<AsciiString> REQUEST_HEADER_TRANSLATIONS = new CharSequenceMap<AsciiString>();
    
    private static final CharSequenceMap<AsciiString> RESPONSE_HEADER_TRANSLATIONS = new CharSequenceMap<AsciiString>();
    
    private final int streamId;
    
    private final HttpHeaders output;
    
    private final CharSequenceMap<AsciiString> translations;
    
    static {
      RESPONSE_HEADER_TRANSLATIONS.add(Http2Headers.PseudoHeaderName.AUTHORITY.value(), HttpHeaderNames.HOST);
      RESPONSE_HEADER_TRANSLATIONS.add(Http2Headers.PseudoHeaderName.SCHEME.value(), HttpConversionUtil.ExtensionHeaderNames.SCHEME
          .text());
      REQUEST_HEADER_TRANSLATIONS.add((Headers)RESPONSE_HEADER_TRANSLATIONS);
      RESPONSE_HEADER_TRANSLATIONS.add(Http2Headers.PseudoHeaderName.PATH.value(), HttpConversionUtil.ExtensionHeaderNames.PATH
          .text());
    }
    
    Http2ToHttpHeaderTranslator(int streamId, HttpHeaders output, boolean request) {
      this.streamId = streamId;
      this.output = output;
      this.translations = request ? REQUEST_HEADER_TRANSLATIONS : RESPONSE_HEADER_TRANSLATIONS;
    }
    
    public void translate(Map.Entry<CharSequence, CharSequence> entry) throws Http2Exception {
      CharSequence name = entry.getKey();
      CharSequence value = entry.getValue();
      AsciiString translatedName = (AsciiString)this.translations.get(name);
      if (translatedName != null) {
        this.output.add((CharSequence)translatedName, AsciiString.of(value));
      } else if (!Http2Headers.PseudoHeaderName.isPseudoHeader(name)) {
        if (name.length() == 0 || name.charAt(0) == ':')
          throw Http2Exception.streamError(this.streamId, Http2Error.PROTOCOL_ERROR, "Invalid HTTP/2 header '%s' encountered in translation to HTTP/1.x", new Object[] { name }); 
        if (HttpHeaderNames.COOKIE.equals(name)) {
          String existingCookie = this.output.get((CharSequence)HttpHeaderNames.COOKIE);
          this.output.set((CharSequence)HttpHeaderNames.COOKIE, (existingCookie != null) ? (existingCookie + "; " + value) : value);
        } else {
          this.output.add(name, value);
        } 
      } 
    }
  }
}
