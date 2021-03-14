package com.github.steveice10.netty.handler.codec.http.multipart;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.handler.codec.http.HttpConstants;
import com.github.steveice10.netty.handler.codec.http.HttpContent;
import com.github.steveice10.netty.handler.codec.http.HttpHeaderNames;
import com.github.steveice10.netty.handler.codec.http.HttpHeaderValues;
import com.github.steveice10.netty.handler.codec.http.HttpRequest;
import com.github.steveice10.netty.handler.codec.http.QueryStringDecoder;
import com.github.steveice10.netty.util.CharsetUtil;
import com.github.steveice10.netty.util.internal.InternalThreadLocalMap;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.StringUtil;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class HttpPostMultipartRequestDecoder implements InterfaceHttpPostRequestDecoder {
  private final HttpDataFactory factory;
  
  private final HttpRequest request;
  
  private Charset charset;
  
  private boolean isLastChunk;
  
  private final List<InterfaceHttpData> bodyListHttpData = new ArrayList<InterfaceHttpData>();
  
  private final Map<String, List<InterfaceHttpData>> bodyMapHttpData = new TreeMap<String, List<InterfaceHttpData>>(CaseIgnoringComparator.INSTANCE);
  
  private ByteBuf undecodedChunk;
  
  private int bodyListHttpDataRank;
  
  private String multipartDataBoundary;
  
  private String multipartMixedBoundary;
  
  private HttpPostRequestDecoder.MultiPartStatus currentStatus = HttpPostRequestDecoder.MultiPartStatus.NOTSTARTED;
  
  private Map<CharSequence, Attribute> currentFieldAttributes;
  
  private FileUpload currentFileUpload;
  
  private Attribute currentAttribute;
  
  private boolean destroyed;
  
  private int discardThreshold = 10485760;
  
  public HttpPostMultipartRequestDecoder(HttpRequest request) {
    this(new DefaultHttpDataFactory(16384L), request, HttpConstants.DEFAULT_CHARSET);
  }
  
  public HttpPostMultipartRequestDecoder(HttpDataFactory factory, HttpRequest request) {
    this(factory, request, HttpConstants.DEFAULT_CHARSET);
  }
  
  public HttpPostMultipartRequestDecoder(HttpDataFactory factory, HttpRequest request, Charset charset) {
    this.request = (HttpRequest)ObjectUtil.checkNotNull(request, "request");
    this.charset = (Charset)ObjectUtil.checkNotNull(charset, "charset");
    this.factory = (HttpDataFactory)ObjectUtil.checkNotNull(factory, "factory");
    setMultipart(this.request.headers().get((CharSequence)HttpHeaderNames.CONTENT_TYPE));
    if (request instanceof HttpContent) {
      offer((HttpContent)request);
    } else {
      this.undecodedChunk = Unpooled.buffer();
      parseBody();
    } 
  }
  
  private void setMultipart(String contentType) {
    String[] dataBoundary = HttpPostRequestDecoder.getMultipartDataBoundary(contentType);
    if (dataBoundary != null) {
      this.multipartDataBoundary = dataBoundary[0];
      if (dataBoundary.length > 1 && dataBoundary[1] != null)
        this.charset = Charset.forName(dataBoundary[1]); 
    } else {
      this.multipartDataBoundary = null;
    } 
    this.currentStatus = HttpPostRequestDecoder.MultiPartStatus.HEADERDELIMITER;
  }
  
  private void checkDestroyed() {
    if (this.destroyed)
      throw new IllegalStateException(HttpPostMultipartRequestDecoder.class.getSimpleName() + " was destroyed already"); 
  }
  
  public boolean isMultipart() {
    checkDestroyed();
    return true;
  }
  
  public void setDiscardThreshold(int discardThreshold) {
    this.discardThreshold = ObjectUtil.checkPositiveOrZero(discardThreshold, "discardThreshold");
  }
  
  public int getDiscardThreshold() {
    return this.discardThreshold;
  }
  
  public List<InterfaceHttpData> getBodyHttpDatas() {
    checkDestroyed();
    if (!this.isLastChunk)
      throw new HttpPostRequestDecoder.NotEnoughDataDecoderException(); 
    return this.bodyListHttpData;
  }
  
  public List<InterfaceHttpData> getBodyHttpDatas(String name) {
    checkDestroyed();
    if (!this.isLastChunk)
      throw new HttpPostRequestDecoder.NotEnoughDataDecoderException(); 
    return this.bodyMapHttpData.get(name);
  }
  
  public InterfaceHttpData getBodyHttpData(String name) {
    checkDestroyed();
    if (!this.isLastChunk)
      throw new HttpPostRequestDecoder.NotEnoughDataDecoderException(); 
    List<InterfaceHttpData> list = this.bodyMapHttpData.get(name);
    if (list != null)
      return list.get(0); 
    return null;
  }
  
  public HttpPostMultipartRequestDecoder offer(HttpContent content) {
    checkDestroyed();
    ByteBuf buf = content.content();
    if (this.undecodedChunk == null) {
      this.undecodedChunk = buf.copy();
    } else {
      this.undecodedChunk.writeBytes(buf);
    } 
    if (content instanceof com.github.steveice10.netty.handler.codec.http.LastHttpContent)
      this.isLastChunk = true; 
    parseBody();
    if (this.undecodedChunk != null && this.undecodedChunk.writerIndex() > this.discardThreshold)
      this.undecodedChunk.discardReadBytes(); 
    return this;
  }
  
  public boolean hasNext() {
    checkDestroyed();
    if (this.currentStatus == HttpPostRequestDecoder.MultiPartStatus.EPILOGUE)
      if (this.bodyListHttpDataRank >= this.bodyListHttpData.size())
        throw new HttpPostRequestDecoder.EndOfDataDecoderException();  
    return (!this.bodyListHttpData.isEmpty() && this.bodyListHttpDataRank < this.bodyListHttpData.size());
  }
  
  public InterfaceHttpData next() {
    checkDestroyed();
    if (hasNext())
      return this.bodyListHttpData.get(this.bodyListHttpDataRank++); 
    return null;
  }
  
  public InterfaceHttpData currentPartialHttpData() {
    if (this.currentFileUpload != null)
      return this.currentFileUpload; 
    return this.currentAttribute;
  }
  
  private void parseBody() {
    if (this.currentStatus == HttpPostRequestDecoder.MultiPartStatus.PREEPILOGUE || this.currentStatus == HttpPostRequestDecoder.MultiPartStatus.EPILOGUE) {
      if (this.isLastChunk)
        this.currentStatus = HttpPostRequestDecoder.MultiPartStatus.EPILOGUE; 
      return;
    } 
    parseBodyMultipart();
  }
  
  protected void addHttpData(InterfaceHttpData data) {
    if (data == null)
      return; 
    List<InterfaceHttpData> datas = this.bodyMapHttpData.get(data.getName());
    if (datas == null) {
      datas = new ArrayList<InterfaceHttpData>(1);
      this.bodyMapHttpData.put(data.getName(), datas);
    } 
    datas.add(data);
    this.bodyListHttpData.add(data);
  }
  
  private void parseBodyMultipart() {
    if (this.undecodedChunk == null || this.undecodedChunk.readableBytes() == 0)
      return; 
    InterfaceHttpData data = decodeMultipart(this.currentStatus);
    while (data != null) {
      addHttpData(data);
      if (this.currentStatus == HttpPostRequestDecoder.MultiPartStatus.PREEPILOGUE || this.currentStatus == HttpPostRequestDecoder.MultiPartStatus.EPILOGUE)
        break; 
      data = decodeMultipart(this.currentStatus);
    } 
  }
  
  private InterfaceHttpData decodeMultipart(HttpPostRequestDecoder.MultiPartStatus state) {
    Charset localCharset;
    Attribute charsetAttribute;
    Attribute nameAttribute;
    Attribute finalAttribute;
    switch (state) {
      case NOTSTARTED:
        throw new HttpPostRequestDecoder.ErrorDataDecoderException("Should not be called with the current getStatus");
      case PREAMBLE:
        throw new HttpPostRequestDecoder.ErrorDataDecoderException("Should not be called with the current getStatus");
      case HEADERDELIMITER:
        return findMultipartDelimiter(this.multipartDataBoundary, HttpPostRequestDecoder.MultiPartStatus.DISPOSITION, HttpPostRequestDecoder.MultiPartStatus.PREEPILOGUE);
      case DISPOSITION:
        return findMultipartDisposition();
      case FIELD:
        localCharset = null;
        charsetAttribute = this.currentFieldAttributes.get(HttpHeaderValues.CHARSET);
        if (charsetAttribute != null)
          try {
            localCharset = Charset.forName(charsetAttribute.getValue());
          } catch (IOException e) {
            throw new HttpPostRequestDecoder.ErrorDataDecoderException(e);
          } catch (UnsupportedCharsetException e) {
            throw new HttpPostRequestDecoder.ErrorDataDecoderException(e);
          }  
        nameAttribute = this.currentFieldAttributes.get(HttpHeaderValues.NAME);
        if (this.currentAttribute == null) {
          long size;
          Attribute lengthAttribute = this.currentFieldAttributes.get(HttpHeaderNames.CONTENT_LENGTH);
          try {
            size = (lengthAttribute != null) ? Long.parseLong(lengthAttribute
                .getValue()) : 0L;
          } catch (IOException e) {
            throw new HttpPostRequestDecoder.ErrorDataDecoderException(e);
          } catch (NumberFormatException ignored) {
            size = 0L;
          } 
          try {
            if (size > 0L) {
              this.currentAttribute = this.factory.createAttribute(this.request, 
                  cleanString(nameAttribute.getValue()), size);
            } else {
              this.currentAttribute = this.factory.createAttribute(this.request, 
                  cleanString(nameAttribute.getValue()));
            } 
          } catch (NullPointerException e) {
            throw new HttpPostRequestDecoder.ErrorDataDecoderException(e);
          } catch (IllegalArgumentException e) {
            throw new HttpPostRequestDecoder.ErrorDataDecoderException(e);
          } catch (IOException e) {
            throw new HttpPostRequestDecoder.ErrorDataDecoderException(e);
          } 
          if (localCharset != null)
            this.currentAttribute.setCharset(localCharset); 
        } 
        if (!loadDataMultipart(this.undecodedChunk, this.multipartDataBoundary, this.currentAttribute))
          return null; 
        finalAttribute = this.currentAttribute;
        this.currentAttribute = null;
        this.currentFieldAttributes = null;
        this.currentStatus = HttpPostRequestDecoder.MultiPartStatus.HEADERDELIMITER;
        return finalAttribute;
      case FILEUPLOAD:
        return getFileUpload(this.multipartDataBoundary);
      case MIXEDDELIMITER:
        return findMultipartDelimiter(this.multipartMixedBoundary, HttpPostRequestDecoder.MultiPartStatus.MIXEDDISPOSITION, HttpPostRequestDecoder.MultiPartStatus.HEADERDELIMITER);
      case MIXEDDISPOSITION:
        return findMultipartDisposition();
      case MIXEDFILEUPLOAD:
        return getFileUpload(this.multipartMixedBoundary);
      case PREEPILOGUE:
        return null;
      case EPILOGUE:
        return null;
    } 
    throw new HttpPostRequestDecoder.ErrorDataDecoderException("Shouldn't reach here.");
  }
  
  private static void skipControlCharacters(ByteBuf undecodedChunk) {
    if (!undecodedChunk.hasArray()) {
      try {
        skipControlCharactersStandard(undecodedChunk);
      } catch (IndexOutOfBoundsException e1) {
        throw new HttpPostRequestDecoder.NotEnoughDataDecoderException(e1);
      } 
      return;
    } 
    HttpPostBodyUtil.SeekAheadOptimize sao = new HttpPostBodyUtil.SeekAheadOptimize(undecodedChunk);
    while (sao.pos < sao.limit) {
      char c = (char)(sao.bytes[sao.pos++] & 0xFF);
      if (!Character.isISOControl(c) && !Character.isWhitespace(c)) {
        sao.setReadPosition(1);
        return;
      } 
    } 
    throw new HttpPostRequestDecoder.NotEnoughDataDecoderException("Access out of bounds");
  }
  
  private static void skipControlCharactersStandard(ByteBuf undecodedChunk) {
    while (true) {
      char c = (char)undecodedChunk.readUnsignedByte();
      if (!Character.isISOControl(c) && !Character.isWhitespace(c)) {
        undecodedChunk.readerIndex(undecodedChunk.readerIndex() - 1);
        return;
      } 
    } 
  }
  
  private InterfaceHttpData findMultipartDelimiter(String delimiter, HttpPostRequestDecoder.MultiPartStatus dispositionStatus, HttpPostRequestDecoder.MultiPartStatus closeDelimiterStatus) {
    String newline;
    int readerIndex = this.undecodedChunk.readerIndex();
    try {
      skipControlCharacters(this.undecodedChunk);
    } catch (NotEnoughDataDecoderException ignored) {
      this.undecodedChunk.readerIndex(readerIndex);
      return null;
    } 
    skipOneLine();
    try {
      newline = readDelimiter(this.undecodedChunk, delimiter);
    } catch (NotEnoughDataDecoderException ignored) {
      this.undecodedChunk.readerIndex(readerIndex);
      return null;
    } 
    if (newline.equals(delimiter)) {
      this.currentStatus = dispositionStatus;
      return decodeMultipart(dispositionStatus);
    } 
    if (newline.equals(delimiter + "--")) {
      this.currentStatus = closeDelimiterStatus;
      if (this.currentStatus == HttpPostRequestDecoder.MultiPartStatus.HEADERDELIMITER) {
        this.currentFieldAttributes = null;
        return decodeMultipart(HttpPostRequestDecoder.MultiPartStatus.HEADERDELIMITER);
      } 
      return null;
    } 
    this.undecodedChunk.readerIndex(readerIndex);
    throw new HttpPostRequestDecoder.ErrorDataDecoderException("No Multipart delimiter found");
  }
  
  private InterfaceHttpData findMultipartDisposition() {
    int readerIndex = this.undecodedChunk.readerIndex();
    if (this.currentStatus == HttpPostRequestDecoder.MultiPartStatus.DISPOSITION)
      this.currentFieldAttributes = new TreeMap<CharSequence, Attribute>(CaseIgnoringComparator.INSTANCE); 
    while (!skipOneLine()) {
      String newline;
      try {
        skipControlCharacters(this.undecodedChunk);
        newline = readLine(this.undecodedChunk, this.charset);
      } catch (NotEnoughDataDecoderException ignored) {
        this.undecodedChunk.readerIndex(readerIndex);
        return null;
      } 
      String[] contents = splitMultipartHeader(newline);
      if (HttpHeaderNames.CONTENT_DISPOSITION.contentEqualsIgnoreCase(contents[0])) {
        boolean checkSecondArg;
        if (this.currentStatus == HttpPostRequestDecoder.MultiPartStatus.DISPOSITION) {
          checkSecondArg = HttpHeaderValues.FORM_DATA.contentEqualsIgnoreCase(contents[1]);
        } else {
          checkSecondArg = (HttpHeaderValues.ATTACHMENT.contentEqualsIgnoreCase(contents[1]) || HttpHeaderValues.FILE.contentEqualsIgnoreCase(contents[1]));
        } 
        if (checkSecondArg)
          for (int i = 2; i < contents.length; i++) {
            Attribute attribute;
            String[] values = contents[i].split("=", 2);
            try {
              attribute = getContentDispositionAttribute(values);
            } catch (NullPointerException e) {
              throw new HttpPostRequestDecoder.ErrorDataDecoderException(e);
            } catch (IllegalArgumentException e) {
              throw new HttpPostRequestDecoder.ErrorDataDecoderException(e);
            } 
            this.currentFieldAttributes.put(attribute.getName(), attribute);
          }  
        continue;
      } 
      if (HttpHeaderNames.CONTENT_TRANSFER_ENCODING.contentEqualsIgnoreCase(contents[0])) {
        Attribute attribute;
        try {
          attribute = this.factory.createAttribute(this.request, HttpHeaderNames.CONTENT_TRANSFER_ENCODING.toString(), 
              cleanString(contents[1]));
        } catch (NullPointerException e) {
          throw new HttpPostRequestDecoder.ErrorDataDecoderException(e);
        } catch (IllegalArgumentException e) {
          throw new HttpPostRequestDecoder.ErrorDataDecoderException(e);
        } 
        this.currentFieldAttributes.put(HttpHeaderNames.CONTENT_TRANSFER_ENCODING, attribute);
        continue;
      } 
      if (HttpHeaderNames.CONTENT_LENGTH.contentEqualsIgnoreCase(contents[0])) {
        Attribute attribute;
        try {
          attribute = this.factory.createAttribute(this.request, HttpHeaderNames.CONTENT_LENGTH.toString(), 
              cleanString(contents[1]));
        } catch (NullPointerException e) {
          throw new HttpPostRequestDecoder.ErrorDataDecoderException(e);
        } catch (IllegalArgumentException e) {
          throw new HttpPostRequestDecoder.ErrorDataDecoderException(e);
        } 
        this.currentFieldAttributes.put(HttpHeaderNames.CONTENT_LENGTH, attribute);
        continue;
      } 
      if (HttpHeaderNames.CONTENT_TYPE.contentEqualsIgnoreCase(contents[0])) {
        if (HttpHeaderValues.MULTIPART_MIXED.contentEqualsIgnoreCase(contents[1])) {
          if (this.currentStatus == HttpPostRequestDecoder.MultiPartStatus.DISPOSITION) {
            String values = StringUtil.substringAfter(contents[2], '=');
            this.multipartMixedBoundary = "--" + values;
            this.currentStatus = HttpPostRequestDecoder.MultiPartStatus.MIXEDDELIMITER;
            return decodeMultipart(HttpPostRequestDecoder.MultiPartStatus.MIXEDDELIMITER);
          } 
          throw new HttpPostRequestDecoder.ErrorDataDecoderException("Mixed Multipart found in a previous Mixed Multipart");
        } 
        for (int i = 1; i < contents.length; i++) {
          String charsetHeader = HttpHeaderValues.CHARSET.toString();
          if (contents[i].regionMatches(true, 0, charsetHeader, 0, charsetHeader.length())) {
            Attribute attribute;
            String values = StringUtil.substringAfter(contents[i], '=');
            try {
              attribute = this.factory.createAttribute(this.request, charsetHeader, cleanString(values));
            } catch (NullPointerException e) {
              throw new HttpPostRequestDecoder.ErrorDataDecoderException(e);
            } catch (IllegalArgumentException e) {
              throw new HttpPostRequestDecoder.ErrorDataDecoderException(e);
            } 
            this.currentFieldAttributes.put(HttpHeaderValues.CHARSET, attribute);
          } else {
            Attribute attribute;
            try {
              attribute = this.factory.createAttribute(this.request, 
                  cleanString(contents[0]), contents[i]);
            } catch (NullPointerException e) {
              Attribute attribute1;
              throw new HttpPostRequestDecoder.ErrorDataDecoderException(attribute1);
            } catch (IllegalArgumentException e) {
              Attribute attribute1;
              throw new HttpPostRequestDecoder.ErrorDataDecoderException(attribute1);
            } 
            this.currentFieldAttributes.put(attribute.getName(), attribute);
          } 
        } 
        continue;
      } 
      throw new HttpPostRequestDecoder.ErrorDataDecoderException("Unknown Params: " + newline);
    } 
    Attribute filenameAttribute = this.currentFieldAttributes.get(HttpHeaderValues.FILENAME);
    if (this.currentStatus == HttpPostRequestDecoder.MultiPartStatus.DISPOSITION) {
      if (filenameAttribute != null) {
        this.currentStatus = HttpPostRequestDecoder.MultiPartStatus.FILEUPLOAD;
        return decodeMultipart(HttpPostRequestDecoder.MultiPartStatus.FILEUPLOAD);
      } 
      this.currentStatus = HttpPostRequestDecoder.MultiPartStatus.FIELD;
      return decodeMultipart(HttpPostRequestDecoder.MultiPartStatus.FIELD);
    } 
    if (filenameAttribute != null) {
      this.currentStatus = HttpPostRequestDecoder.MultiPartStatus.MIXEDFILEUPLOAD;
      return decodeMultipart(HttpPostRequestDecoder.MultiPartStatus.MIXEDFILEUPLOAD);
    } 
    throw new HttpPostRequestDecoder.ErrorDataDecoderException("Filename not found");
  }
  
  private static final String FILENAME_ENCODED = HttpHeaderValues.FILENAME.toString() + '*';
  
  private Attribute getContentDispositionAttribute(String... values) {
    String name = cleanString(values[0]);
    String value = values[1];
    if (HttpHeaderValues.FILENAME.contentEquals(name)) {
      int last = value.length() - 1;
      if (last > 0 && value
        .charAt(0) == '"' && value
        .charAt(last) == '"')
        value = value.substring(1, last); 
    } else if (FILENAME_ENCODED.equals(name)) {
      try {
        name = HttpHeaderValues.FILENAME.toString();
        String[] split = value.split("'", 3);
        value = QueryStringDecoder.decodeComponent(split[2], Charset.forName(split[0]));
      } catch (ArrayIndexOutOfBoundsException e) {
        throw new HttpPostRequestDecoder.ErrorDataDecoderException(e);
      } catch (UnsupportedCharsetException e) {
        throw new HttpPostRequestDecoder.ErrorDataDecoderException(e);
      } 
    } else {
      value = cleanString(value);
    } 
    return this.factory.createAttribute(this.request, name, value);
  }
  
  protected InterfaceHttpData getFileUpload(String delimiter) {
    Attribute encoding = this.currentFieldAttributes.get(HttpHeaderNames.CONTENT_TRANSFER_ENCODING);
    Charset localCharset = this.charset;
    HttpPostBodyUtil.TransferEncodingMechanism mechanism = HttpPostBodyUtil.TransferEncodingMechanism.BIT7;
    if (encoding != null) {
      String code;
      try {
        code = encoding.getValue().toLowerCase();
      } catch (IOException e) {
        throw new HttpPostRequestDecoder.ErrorDataDecoderException(e);
      } 
      if (code.equals(HttpPostBodyUtil.TransferEncodingMechanism.BIT7.value())) {
        localCharset = CharsetUtil.US_ASCII;
      } else if (code.equals(HttpPostBodyUtil.TransferEncodingMechanism.BIT8.value())) {
        localCharset = CharsetUtil.ISO_8859_1;
        mechanism = HttpPostBodyUtil.TransferEncodingMechanism.BIT8;
      } else if (code.equals(HttpPostBodyUtil.TransferEncodingMechanism.BINARY.value())) {
        mechanism = HttpPostBodyUtil.TransferEncodingMechanism.BINARY;
      } else {
        throw new HttpPostRequestDecoder.ErrorDataDecoderException("TransferEncoding Unknown: " + code);
      } 
    } 
    Attribute charsetAttribute = this.currentFieldAttributes.get(HttpHeaderValues.CHARSET);
    if (charsetAttribute != null)
      try {
        localCharset = Charset.forName(charsetAttribute.getValue());
      } catch (IOException e) {
        throw new HttpPostRequestDecoder.ErrorDataDecoderException(e);
      } catch (UnsupportedCharsetException e) {
        throw new HttpPostRequestDecoder.ErrorDataDecoderException(e);
      }  
    if (this.currentFileUpload == null) {
      long size;
      Attribute filenameAttribute = this.currentFieldAttributes.get(HttpHeaderValues.FILENAME);
      Attribute nameAttribute = this.currentFieldAttributes.get(HttpHeaderValues.NAME);
      Attribute contentTypeAttribute = this.currentFieldAttributes.get(HttpHeaderNames.CONTENT_TYPE);
      Attribute lengthAttribute = this.currentFieldAttributes.get(HttpHeaderNames.CONTENT_LENGTH);
      try {
        size = (lengthAttribute != null) ? Long.parseLong(lengthAttribute.getValue()) : 0L;
      } catch (IOException e) {
        throw new HttpPostRequestDecoder.ErrorDataDecoderException(e);
      } catch (NumberFormatException ignored) {
        size = 0L;
      } 
      try {
        String contentType;
        if (contentTypeAttribute != null) {
          contentType = contentTypeAttribute.getValue();
        } else {
          contentType = "application/octet-stream";
        } 
        this.currentFileUpload = this.factory.createFileUpload(this.request, 
            cleanString(nameAttribute.getValue()), cleanString(filenameAttribute.getValue()), contentType, mechanism
            .value(), localCharset, size);
      } catch (NullPointerException e) {
        throw new HttpPostRequestDecoder.ErrorDataDecoderException(e);
      } catch (IllegalArgumentException e) {
        throw new HttpPostRequestDecoder.ErrorDataDecoderException(e);
      } catch (IOException e) {
        throw new HttpPostRequestDecoder.ErrorDataDecoderException(e);
      } 
    } 
    if (!loadDataMultipart(this.undecodedChunk, delimiter, this.currentFileUpload))
      return null; 
    if (this.currentFileUpload.isCompleted()) {
      if (this.currentStatus == HttpPostRequestDecoder.MultiPartStatus.FILEUPLOAD) {
        this.currentStatus = HttpPostRequestDecoder.MultiPartStatus.HEADERDELIMITER;
        this.currentFieldAttributes = null;
      } else {
        this.currentStatus = HttpPostRequestDecoder.MultiPartStatus.MIXEDDELIMITER;
        cleanMixedAttributes();
      } 
      FileUpload fileUpload = this.currentFileUpload;
      this.currentFileUpload = null;
      return fileUpload;
    } 
    return null;
  }
  
  public void destroy() {
    checkDestroyed();
    cleanFiles();
    this.destroyed = true;
    if (this.undecodedChunk != null && this.undecodedChunk.refCnt() > 0) {
      this.undecodedChunk.release();
      this.undecodedChunk = null;
    } 
    for (int i = this.bodyListHttpDataRank; i < this.bodyListHttpData.size(); i++)
      ((InterfaceHttpData)this.bodyListHttpData.get(i)).release(); 
  }
  
  public void cleanFiles() {
    checkDestroyed();
    this.factory.cleanRequestHttpData(this.request);
  }
  
  public void removeHttpDataFromClean(InterfaceHttpData data) {
    checkDestroyed();
    this.factory.removeHttpDataFromClean(this.request, data);
  }
  
  private void cleanMixedAttributes() {
    this.currentFieldAttributes.remove(HttpHeaderValues.CHARSET);
    this.currentFieldAttributes.remove(HttpHeaderNames.CONTENT_LENGTH);
    this.currentFieldAttributes.remove(HttpHeaderNames.CONTENT_TRANSFER_ENCODING);
    this.currentFieldAttributes.remove(HttpHeaderNames.CONTENT_TYPE);
    this.currentFieldAttributes.remove(HttpHeaderValues.FILENAME);
  }
  
  private static String readLineStandard(ByteBuf undecodedChunk, Charset charset) {
    int readerIndex = undecodedChunk.readerIndex();
    try {
      ByteBuf line = Unpooled.buffer(64);
      while (undecodedChunk.isReadable()) {
        byte nextByte = undecodedChunk.readByte();
        if (nextByte == 13) {
          nextByte = undecodedChunk.getByte(undecodedChunk.readerIndex());
          if (nextByte == 10) {
            undecodedChunk.readByte();
            return line.toString(charset);
          } 
          line.writeByte(13);
          continue;
        } 
        if (nextByte == 10)
          return line.toString(charset); 
        line.writeByte(nextByte);
      } 
    } catch (IndexOutOfBoundsException e) {
      undecodedChunk.readerIndex(readerIndex);
      throw new HttpPostRequestDecoder.NotEnoughDataDecoderException(e);
    } 
    undecodedChunk.readerIndex(readerIndex);
    throw new HttpPostRequestDecoder.NotEnoughDataDecoderException();
  }
  
  private static String readLine(ByteBuf undecodedChunk, Charset charset) {
    if (!undecodedChunk.hasArray())
      return readLineStandard(undecodedChunk, charset); 
    HttpPostBodyUtil.SeekAheadOptimize sao = new HttpPostBodyUtil.SeekAheadOptimize(undecodedChunk);
    int readerIndex = undecodedChunk.readerIndex();
    try {
      ByteBuf line = Unpooled.buffer(64);
      while (sao.pos < sao.limit) {
        byte nextByte = sao.bytes[sao.pos++];
        if (nextByte == 13) {
          if (sao.pos < sao.limit) {
            nextByte = sao.bytes[sao.pos++];
            if (nextByte == 10) {
              sao.setReadPosition(0);
              return line.toString(charset);
            } 
            sao.pos--;
            line.writeByte(13);
            continue;
          } 
          line.writeByte(nextByte);
          continue;
        } 
        if (nextByte == 10) {
          sao.setReadPosition(0);
          return line.toString(charset);
        } 
        line.writeByte(nextByte);
      } 
    } catch (IndexOutOfBoundsException e) {
      undecodedChunk.readerIndex(readerIndex);
      throw new HttpPostRequestDecoder.NotEnoughDataDecoderException(e);
    } 
    undecodedChunk.readerIndex(readerIndex);
    throw new HttpPostRequestDecoder.NotEnoughDataDecoderException();
  }
  
  private static String readDelimiterStandard(ByteBuf undecodedChunk, String delimiter) {
    int readerIndex = undecodedChunk.readerIndex();
    try {
      StringBuilder sb = new StringBuilder(64);
      int delimiterPos = 0;
      int len = delimiter.length();
      while (undecodedChunk.isReadable() && delimiterPos < len) {
        byte nextByte = undecodedChunk.readByte();
        if (nextByte == delimiter.charAt(delimiterPos)) {
          delimiterPos++;
          sb.append((char)nextByte);
          continue;
        } 
        undecodedChunk.readerIndex(readerIndex);
        throw new HttpPostRequestDecoder.NotEnoughDataDecoderException();
      } 
      if (undecodedChunk.isReadable()) {
        byte nextByte = undecodedChunk.readByte();
        if (nextByte == 13) {
          nextByte = undecodedChunk.readByte();
          if (nextByte == 10)
            return sb.toString(); 
          undecodedChunk.readerIndex(readerIndex);
          throw new HttpPostRequestDecoder.NotEnoughDataDecoderException();
        } 
        if (nextByte == 10)
          return sb.toString(); 
        if (nextByte == 45) {
          sb.append('-');
          nextByte = undecodedChunk.readByte();
          if (nextByte == 45) {
            sb.append('-');
            if (undecodedChunk.isReadable()) {
              nextByte = undecodedChunk.readByte();
              if (nextByte == 13) {
                nextByte = undecodedChunk.readByte();
                if (nextByte == 10)
                  return sb.toString(); 
                undecodedChunk.readerIndex(readerIndex);
                throw new HttpPostRequestDecoder.NotEnoughDataDecoderException();
              } 
              if (nextByte == 10)
                return sb.toString(); 
              undecodedChunk.readerIndex(undecodedChunk.readerIndex() - 1);
              return sb.toString();
            } 
            return sb.toString();
          } 
        } 
      } 
    } catch (IndexOutOfBoundsException e) {
      undecodedChunk.readerIndex(readerIndex);
      throw new HttpPostRequestDecoder.NotEnoughDataDecoderException(e);
    } 
    undecodedChunk.readerIndex(readerIndex);
    throw new HttpPostRequestDecoder.NotEnoughDataDecoderException();
  }
  
  private static String readDelimiter(ByteBuf undecodedChunk, String delimiter) {
    if (!undecodedChunk.hasArray())
      return readDelimiterStandard(undecodedChunk, delimiter); 
    HttpPostBodyUtil.SeekAheadOptimize sao = new HttpPostBodyUtil.SeekAheadOptimize(undecodedChunk);
    int readerIndex = undecodedChunk.readerIndex();
    int delimiterPos = 0;
    int len = delimiter.length();
    try {
      StringBuilder sb = new StringBuilder(64);
      while (sao.pos < sao.limit && delimiterPos < len) {
        byte nextByte = sao.bytes[sao.pos++];
        if (nextByte == delimiter.charAt(delimiterPos)) {
          delimiterPos++;
          sb.append((char)nextByte);
          continue;
        } 
        undecodedChunk.readerIndex(readerIndex);
        throw new HttpPostRequestDecoder.NotEnoughDataDecoderException();
      } 
      if (sao.pos < sao.limit) {
        byte nextByte = sao.bytes[sao.pos++];
        if (nextByte == 13) {
          if (sao.pos < sao.limit) {
            nextByte = sao.bytes[sao.pos++];
            if (nextByte == 10) {
              sao.setReadPosition(0);
              return sb.toString();
            } 
            undecodedChunk.readerIndex(readerIndex);
            throw new HttpPostRequestDecoder.NotEnoughDataDecoderException();
          } 
          undecodedChunk.readerIndex(readerIndex);
          throw new HttpPostRequestDecoder.NotEnoughDataDecoderException();
        } 
        if (nextByte == 10) {
          sao.setReadPosition(0);
          return sb.toString();
        } 
        if (nextByte == 45) {
          sb.append('-');
          if (sao.pos < sao.limit) {
            nextByte = sao.bytes[sao.pos++];
            if (nextByte == 45) {
              sb.append('-');
              if (sao.pos < sao.limit) {
                nextByte = sao.bytes[sao.pos++];
                if (nextByte == 13) {
                  if (sao.pos < sao.limit) {
                    nextByte = sao.bytes[sao.pos++];
                    if (nextByte == 10) {
                      sao.setReadPosition(0);
                      return sb.toString();
                    } 
                    undecodedChunk.readerIndex(readerIndex);
                    throw new HttpPostRequestDecoder.NotEnoughDataDecoderException();
                  } 
                  undecodedChunk.readerIndex(readerIndex);
                  throw new HttpPostRequestDecoder.NotEnoughDataDecoderException();
                } 
                if (nextByte == 10) {
                  sao.setReadPosition(0);
                  return sb.toString();
                } 
                sao.setReadPosition(1);
                return sb.toString();
              } 
              sao.setReadPosition(0);
              return sb.toString();
            } 
          } 
        } 
      } 
    } catch (IndexOutOfBoundsException e) {
      undecodedChunk.readerIndex(readerIndex);
      throw new HttpPostRequestDecoder.NotEnoughDataDecoderException(e);
    } 
    undecodedChunk.readerIndex(readerIndex);
    throw new HttpPostRequestDecoder.NotEnoughDataDecoderException();
  }
  
  private static boolean loadDataMultipartStandard(ByteBuf undecodedChunk, String delimiter, HttpData httpData) {
    int startReaderIndex = undecodedChunk.readerIndex();
    int delimeterLength = delimiter.length();
    int index = 0;
    int lastPosition = startReaderIndex;
    byte prevByte = 10;
    boolean delimiterFound = false;
    while (undecodedChunk.isReadable()) {
      byte nextByte = undecodedChunk.readByte();
      if (prevByte == 10 && nextByte == delimiter.codePointAt(index)) {
        index++;
        if (delimeterLength == index) {
          delimiterFound = true;
          break;
        } 
        continue;
      } 
      lastPosition = undecodedChunk.readerIndex();
      if (nextByte == 10) {
        index = 0;
        lastPosition -= (prevByte == 13) ? 2 : 1;
      } 
      prevByte = nextByte;
    } 
    if (prevByte == 13)
      lastPosition--; 
    ByteBuf content = undecodedChunk.copy(startReaderIndex, lastPosition - startReaderIndex);
    try {
      httpData.addContent(content, delimiterFound);
    } catch (IOException e) {
      throw new HttpPostRequestDecoder.ErrorDataDecoderException(e);
    } 
    undecodedChunk.readerIndex(lastPosition);
    return delimiterFound;
  }
  
  private static boolean loadDataMultipart(ByteBuf undecodedChunk, String delimiter, HttpData httpData) {
    if (!undecodedChunk.hasArray())
      return loadDataMultipartStandard(undecodedChunk, delimiter, httpData); 
    HttpPostBodyUtil.SeekAheadOptimize sao = new HttpPostBodyUtil.SeekAheadOptimize(undecodedChunk);
    int startReaderIndex = undecodedChunk.readerIndex();
    int delimeterLength = delimiter.length();
    int index = 0;
    int lastRealPos = sao.pos;
    byte prevByte = 10;
    boolean delimiterFound = false;
    while (sao.pos < sao.limit) {
      byte nextByte = sao.bytes[sao.pos++];
      if (prevByte == 10 && nextByte == delimiter.codePointAt(index)) {
        index++;
        if (delimeterLength == index) {
          delimiterFound = true;
          break;
        } 
        continue;
      } 
      lastRealPos = sao.pos;
      if (nextByte == 10) {
        index = 0;
        lastRealPos -= (prevByte == 13) ? 2 : 1;
      } 
      prevByte = nextByte;
    } 
    if (prevByte == 13)
      lastRealPos--; 
    int lastPosition = sao.getReadPosition(lastRealPos);
    ByteBuf content = undecodedChunk.copy(startReaderIndex, lastPosition - startReaderIndex);
    try {
      httpData.addContent(content, delimiterFound);
    } catch (IOException e) {
      throw new HttpPostRequestDecoder.ErrorDataDecoderException(e);
    } 
    undecodedChunk.readerIndex(lastPosition);
    return delimiterFound;
  }
  
  private static String cleanString(String field) {
    int size = field.length();
    StringBuilder sb = new StringBuilder(size);
    for (int i = 0; i < size; i++) {
      char nextChar = field.charAt(i);
      switch (nextChar) {
        case '\t':
        case ',':
        case ':':
        case ';':
        case '=':
          sb.append(' ');
          break;
        case '"':
          break;
        default:
          sb.append(nextChar);
          break;
      } 
    } 
    return sb.toString().trim();
  }
  
  private boolean skipOneLine() {
    if (!this.undecodedChunk.isReadable())
      return false; 
    byte nextByte = this.undecodedChunk.readByte();
    if (nextByte == 13) {
      if (!this.undecodedChunk.isReadable()) {
        this.undecodedChunk.readerIndex(this.undecodedChunk.readerIndex() - 1);
        return false;
      } 
      nextByte = this.undecodedChunk.readByte();
      if (nextByte == 10)
        return true; 
      this.undecodedChunk.readerIndex(this.undecodedChunk.readerIndex() - 2);
      return false;
    } 
    if (nextByte == 10)
      return true; 
    this.undecodedChunk.readerIndex(this.undecodedChunk.readerIndex() - 1);
    return false;
  }
  
  private static String[] splitMultipartHeader(String sb) {
    String[] values;
    ArrayList<String> headers = new ArrayList<String>(1);
    int nameStart = HttpPostBodyUtil.findNonWhitespace(sb, 0);
    int nameEnd;
    for (nameEnd = nameStart; nameEnd < sb.length(); nameEnd++) {
      char ch = sb.charAt(nameEnd);
      if (ch == ':' || Character.isWhitespace(ch))
        break; 
    } 
    int colonEnd;
    for (colonEnd = nameEnd; colonEnd < sb.length(); colonEnd++) {
      if (sb.charAt(colonEnd) == ':') {
        colonEnd++;
        break;
      } 
    } 
    int valueStart = HttpPostBodyUtil.findNonWhitespace(sb, colonEnd);
    int valueEnd = HttpPostBodyUtil.findEndOfString(sb);
    headers.add(sb.substring(nameStart, nameEnd));
    String svalue = (valueStart >= valueEnd) ? "" : sb.substring(valueStart, valueEnd);
    if (svalue.indexOf(';') >= 0) {
      values = splitMultipartHeaderValues(svalue);
    } else {
      values = svalue.split(",");
    } 
    for (String value : values)
      headers.add(value.trim()); 
    String[] array = new String[headers.size()];
    for (int i = 0; i < headers.size(); i++)
      array[i] = headers.get(i); 
    return array;
  }
  
  private static String[] splitMultipartHeaderValues(String svalue) {
    List<String> values = InternalThreadLocalMap.get().arrayList(1);
    boolean inQuote = false;
    boolean escapeNext = false;
    int start = 0;
    for (int i = 0; i < svalue.length(); i++) {
      char c = svalue.charAt(i);
      if (inQuote) {
        if (escapeNext) {
          escapeNext = false;
        } else if (c == '\\') {
          escapeNext = true;
        } else if (c == '"') {
          inQuote = false;
        } 
      } else if (c == '"') {
        inQuote = true;
      } else if (c == ';') {
        values.add(svalue.substring(start, i));
        start = i + 1;
      } 
    } 
    values.add(svalue.substring(start));
    return values.<String>toArray(new String[values.size()]);
  }
}
