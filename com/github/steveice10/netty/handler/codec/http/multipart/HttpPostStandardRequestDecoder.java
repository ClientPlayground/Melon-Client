package com.github.steveice10.netty.handler.codec.http.multipart;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.handler.codec.http.HttpConstants;
import com.github.steveice10.netty.handler.codec.http.HttpContent;
import com.github.steveice10.netty.handler.codec.http.HttpRequest;
import com.github.steveice10.netty.handler.codec.http.QueryStringDecoder;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class HttpPostStandardRequestDecoder implements InterfaceHttpPostRequestDecoder {
  private final HttpDataFactory factory;
  
  private final HttpRequest request;
  
  private final Charset charset;
  
  private boolean isLastChunk;
  
  private final List<InterfaceHttpData> bodyListHttpData = new ArrayList<InterfaceHttpData>();
  
  private final Map<String, List<InterfaceHttpData>> bodyMapHttpData = new TreeMap<String, List<InterfaceHttpData>>(CaseIgnoringComparator.INSTANCE);
  
  private ByteBuf undecodedChunk;
  
  private int bodyListHttpDataRank;
  
  private HttpPostRequestDecoder.MultiPartStatus currentStatus = HttpPostRequestDecoder.MultiPartStatus.NOTSTARTED;
  
  private Attribute currentAttribute;
  
  private boolean destroyed;
  
  private int discardThreshold = 10485760;
  
  public HttpPostStandardRequestDecoder(HttpRequest request) {
    this(new DefaultHttpDataFactory(16384L), request, HttpConstants.DEFAULT_CHARSET);
  }
  
  public HttpPostStandardRequestDecoder(HttpDataFactory factory, HttpRequest request) {
    this(factory, request, HttpConstants.DEFAULT_CHARSET);
  }
  
  public HttpPostStandardRequestDecoder(HttpDataFactory factory, HttpRequest request, Charset charset) {
    this.request = (HttpRequest)ObjectUtil.checkNotNull(request, "request");
    this.charset = (Charset)ObjectUtil.checkNotNull(charset, "charset");
    this.factory = (HttpDataFactory)ObjectUtil.checkNotNull(factory, "factory");
    if (request instanceof HttpContent) {
      offer((HttpContent)request);
    } else {
      this.undecodedChunk = Unpooled.buffer();
      parseBody();
    } 
  }
  
  private void checkDestroyed() {
    if (this.destroyed)
      throw new IllegalStateException(HttpPostStandardRequestDecoder.class.getSimpleName() + " was destroyed already"); 
  }
  
  public boolean isMultipart() {
    checkDestroyed();
    return false;
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
  
  public HttpPostStandardRequestDecoder offer(HttpContent content) {
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
    return this.currentAttribute;
  }
  
  private void parseBody() {
    if (this.currentStatus == HttpPostRequestDecoder.MultiPartStatus.PREEPILOGUE || this.currentStatus == HttpPostRequestDecoder.MultiPartStatus.EPILOGUE) {
      if (this.isLastChunk)
        this.currentStatus = HttpPostRequestDecoder.MultiPartStatus.EPILOGUE; 
      return;
    } 
    parseBodyAttributes();
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
  
  private void parseBodyAttributesStandard() {
    int firstpos = this.undecodedChunk.readerIndex();
    int currentpos = firstpos;
    if (this.currentStatus == HttpPostRequestDecoder.MultiPartStatus.NOTSTARTED)
      this.currentStatus = HttpPostRequestDecoder.MultiPartStatus.DISPOSITION; 
    boolean contRead = true;
    try {
      while (this.undecodedChunk.isReadable() && contRead) {
        char read = (char)this.undecodedChunk.readUnsignedByte();
        currentpos++;
        switch (this.currentStatus) {
          case DISPOSITION:
            if (read == '=') {
              this.currentStatus = HttpPostRequestDecoder.MultiPartStatus.FIELD;
              int equalpos = currentpos - 1;
              String key = decodeAttribute(this.undecodedChunk.toString(firstpos, equalpos - firstpos, this.charset), this.charset);
              this.currentAttribute = this.factory.createAttribute(this.request, key);
              firstpos = currentpos;
              continue;
            } 
            if (read == '&') {
              this.currentStatus = HttpPostRequestDecoder.MultiPartStatus.DISPOSITION;
              int ampersandpos = currentpos - 1;
              String key = decodeAttribute(this.undecodedChunk
                  .toString(firstpos, ampersandpos - firstpos, this.charset), this.charset);
              this.currentAttribute = this.factory.createAttribute(this.request, key);
              this.currentAttribute.setValue("");
              addHttpData(this.currentAttribute);
              this.currentAttribute = null;
              firstpos = currentpos;
              contRead = true;
            } 
            continue;
          case FIELD:
            if (read == '&') {
              this.currentStatus = HttpPostRequestDecoder.MultiPartStatus.DISPOSITION;
              int ampersandpos = currentpos - 1;
              setFinalBuffer(this.undecodedChunk.copy(firstpos, ampersandpos - firstpos));
              firstpos = currentpos;
              contRead = true;
              continue;
            } 
            if (read == '\r') {
              if (this.undecodedChunk.isReadable()) {
                read = (char)this.undecodedChunk.readUnsignedByte();
                currentpos++;
                if (read == '\n') {
                  this.currentStatus = HttpPostRequestDecoder.MultiPartStatus.PREEPILOGUE;
                  int ampersandpos = currentpos - 2;
                  setFinalBuffer(this.undecodedChunk.copy(firstpos, ampersandpos - firstpos));
                  firstpos = currentpos;
                  contRead = false;
                  continue;
                } 
                throw new HttpPostRequestDecoder.ErrorDataDecoderException("Bad end of line");
              } 
              currentpos--;
              continue;
            } 
            if (read == '\n') {
              this.currentStatus = HttpPostRequestDecoder.MultiPartStatus.PREEPILOGUE;
              int ampersandpos = currentpos - 1;
              setFinalBuffer(this.undecodedChunk.copy(firstpos, ampersandpos - firstpos));
              firstpos = currentpos;
              contRead = false;
            } 
            continue;
        } 
        contRead = false;
      } 
      if (this.isLastChunk && this.currentAttribute != null) {
        int ampersandpos = currentpos;
        if (ampersandpos > firstpos) {
          setFinalBuffer(this.undecodedChunk.copy(firstpos, ampersandpos - firstpos));
        } else if (!this.currentAttribute.isCompleted()) {
          setFinalBuffer(Unpooled.EMPTY_BUFFER);
        } 
        firstpos = currentpos;
        this.currentStatus = HttpPostRequestDecoder.MultiPartStatus.EPILOGUE;
      } else if (contRead && this.currentAttribute != null && this.currentStatus == HttpPostRequestDecoder.MultiPartStatus.FIELD) {
        this.currentAttribute.addContent(this.undecodedChunk.copy(firstpos, currentpos - firstpos), false);
        firstpos = currentpos;
      } 
      this.undecodedChunk.readerIndex(firstpos);
    } catch (ErrorDataDecoderException e) {
      this.undecodedChunk.readerIndex(firstpos);
      throw e;
    } catch (IOException e) {
      this.undecodedChunk.readerIndex(firstpos);
      throw new HttpPostRequestDecoder.ErrorDataDecoderException(e);
    } 
  }
  
  private void parseBodyAttributes() {
    // Byte code:
    //   0: aload_0
    //   1: getfield undecodedChunk : Lcom/github/steveice10/netty/buffer/ByteBuf;
    //   4: invokevirtual hasArray : ()Z
    //   7: ifne -> 15
    //   10: aload_0
    //   11: invokespecial parseBodyAttributesStandard : ()V
    //   14: return
    //   15: new com/github/steveice10/netty/handler/codec/http/multipart/HttpPostBodyUtil$SeekAheadOptimize
    //   18: dup
    //   19: aload_0
    //   20: getfield undecodedChunk : Lcom/github/steveice10/netty/buffer/ByteBuf;
    //   23: invokespecial <init> : (Lcom/github/steveice10/netty/buffer/ByteBuf;)V
    //   26: astore_1
    //   27: aload_0
    //   28: getfield undecodedChunk : Lcom/github/steveice10/netty/buffer/ByteBuf;
    //   31: invokevirtual readerIndex : ()I
    //   34: istore_2
    //   35: iload_2
    //   36: istore_3
    //   37: aload_0
    //   38: getfield currentStatus : Lcom/github/steveice10/netty/handler/codec/http/multipart/HttpPostRequestDecoder$MultiPartStatus;
    //   41: getstatic com/github/steveice10/netty/handler/codec/http/multipart/HttpPostRequestDecoder$MultiPartStatus.NOTSTARTED : Lcom/github/steveice10/netty/handler/codec/http/multipart/HttpPostRequestDecoder$MultiPartStatus;
    //   44: if_acmpne -> 54
    //   47: aload_0
    //   48: getstatic com/github/steveice10/netty/handler/codec/http/multipart/HttpPostRequestDecoder$MultiPartStatus.DISPOSITION : Lcom/github/steveice10/netty/handler/codec/http/multipart/HttpPostRequestDecoder$MultiPartStatus;
    //   51: putfield currentStatus : Lcom/github/steveice10/netty/handler/codec/http/multipart/HttpPostRequestDecoder$MultiPartStatus;
    //   54: iconst_1
    //   55: istore #4
    //   57: aload_1
    //   58: getfield pos : I
    //   61: aload_1
    //   62: getfield limit : I
    //   65: if_icmpge -> 522
    //   68: aload_1
    //   69: getfield bytes : [B
    //   72: aload_1
    //   73: dup
    //   74: getfield pos : I
    //   77: dup_x1
    //   78: iconst_1
    //   79: iadd
    //   80: putfield pos : I
    //   83: baload
    //   84: sipush #255
    //   87: iand
    //   88: i2c
    //   89: istore #5
    //   91: iinc #3, 1
    //   94: getstatic com/github/steveice10/netty/handler/codec/http/multipart/HttpPostStandardRequestDecoder$1.$SwitchMap$io$netty$handler$codec$http$multipart$HttpPostRequestDecoder$MultiPartStatus : [I
    //   97: aload_0
    //   98: getfield currentStatus : Lcom/github/steveice10/netty/handler/codec/http/multipart/HttpPostRequestDecoder$MultiPartStatus;
    //   101: invokevirtual ordinal : ()I
    //   104: iaload
    //   105: lookupswitch default -> 508, 1 -> 132, 2 -> 296
    //   132: iload #5
    //   134: bipush #61
    //   136: if_icmpne -> 200
    //   139: aload_0
    //   140: getstatic com/github/steveice10/netty/handler/codec/http/multipart/HttpPostRequestDecoder$MultiPartStatus.FIELD : Lcom/github/steveice10/netty/handler/codec/http/multipart/HttpPostRequestDecoder$MultiPartStatus;
    //   143: putfield currentStatus : Lcom/github/steveice10/netty/handler/codec/http/multipart/HttpPostRequestDecoder$MultiPartStatus;
    //   146: iload_3
    //   147: iconst_1
    //   148: isub
    //   149: istore #6
    //   151: aload_0
    //   152: getfield undecodedChunk : Lcom/github/steveice10/netty/buffer/ByteBuf;
    //   155: iload_2
    //   156: iload #6
    //   158: iload_2
    //   159: isub
    //   160: aload_0
    //   161: getfield charset : Ljava/nio/charset/Charset;
    //   164: invokevirtual toString : (IILjava/nio/charset/Charset;)Ljava/lang/String;
    //   167: aload_0
    //   168: getfield charset : Ljava/nio/charset/Charset;
    //   171: invokestatic decodeAttribute : (Ljava/lang/String;Ljava/nio/charset/Charset;)Ljava/lang/String;
    //   174: astore #7
    //   176: aload_0
    //   177: aload_0
    //   178: getfield factory : Lcom/github/steveice10/netty/handler/codec/http/multipart/HttpDataFactory;
    //   181: aload_0
    //   182: getfield request : Lcom/github/steveice10/netty/handler/codec/http/HttpRequest;
    //   185: aload #7
    //   187: invokeinterface createAttribute : (Lcom/github/steveice10/netty/handler/codec/http/HttpRequest;Ljava/lang/String;)Lcom/github/steveice10/netty/handler/codec/http/multipart/Attribute;
    //   192: putfield currentAttribute : Lcom/github/steveice10/netty/handler/codec/http/multipart/Attribute;
    //   195: iload_3
    //   196: istore_2
    //   197: goto -> 519
    //   200: iload #5
    //   202: bipush #38
    //   204: if_icmpne -> 519
    //   207: aload_0
    //   208: getstatic com/github/steveice10/netty/handler/codec/http/multipart/HttpPostRequestDecoder$MultiPartStatus.DISPOSITION : Lcom/github/steveice10/netty/handler/codec/http/multipart/HttpPostRequestDecoder$MultiPartStatus;
    //   211: putfield currentStatus : Lcom/github/steveice10/netty/handler/codec/http/multipart/HttpPostRequestDecoder$MultiPartStatus;
    //   214: iload_3
    //   215: iconst_1
    //   216: isub
    //   217: istore #8
    //   219: aload_0
    //   220: getfield undecodedChunk : Lcom/github/steveice10/netty/buffer/ByteBuf;
    //   223: iload_2
    //   224: iload #8
    //   226: iload_2
    //   227: isub
    //   228: aload_0
    //   229: getfield charset : Ljava/nio/charset/Charset;
    //   232: invokevirtual toString : (IILjava/nio/charset/Charset;)Ljava/lang/String;
    //   235: aload_0
    //   236: getfield charset : Ljava/nio/charset/Charset;
    //   239: invokestatic decodeAttribute : (Ljava/lang/String;Ljava/nio/charset/Charset;)Ljava/lang/String;
    //   242: astore #7
    //   244: aload_0
    //   245: aload_0
    //   246: getfield factory : Lcom/github/steveice10/netty/handler/codec/http/multipart/HttpDataFactory;
    //   249: aload_0
    //   250: getfield request : Lcom/github/steveice10/netty/handler/codec/http/HttpRequest;
    //   253: aload #7
    //   255: invokeinterface createAttribute : (Lcom/github/steveice10/netty/handler/codec/http/HttpRequest;Ljava/lang/String;)Lcom/github/steveice10/netty/handler/codec/http/multipart/Attribute;
    //   260: putfield currentAttribute : Lcom/github/steveice10/netty/handler/codec/http/multipart/Attribute;
    //   263: aload_0
    //   264: getfield currentAttribute : Lcom/github/steveice10/netty/handler/codec/http/multipart/Attribute;
    //   267: ldc_w ''
    //   270: invokeinterface setValue : (Ljava/lang/String;)V
    //   275: aload_0
    //   276: aload_0
    //   277: getfield currentAttribute : Lcom/github/steveice10/netty/handler/codec/http/multipart/Attribute;
    //   280: invokevirtual addHttpData : (Lcom/github/steveice10/netty/handler/codec/http/multipart/InterfaceHttpData;)V
    //   283: aload_0
    //   284: aconst_null
    //   285: putfield currentAttribute : Lcom/github/steveice10/netty/handler/codec/http/multipart/Attribute;
    //   288: iload_3
    //   289: istore_2
    //   290: iconst_1
    //   291: istore #4
    //   293: goto -> 519
    //   296: iload #5
    //   298: bipush #38
    //   300: if_icmpne -> 339
    //   303: aload_0
    //   304: getstatic com/github/steveice10/netty/handler/codec/http/multipart/HttpPostRequestDecoder$MultiPartStatus.DISPOSITION : Lcom/github/steveice10/netty/handler/codec/http/multipart/HttpPostRequestDecoder$MultiPartStatus;
    //   307: putfield currentStatus : Lcom/github/steveice10/netty/handler/codec/http/multipart/HttpPostRequestDecoder$MultiPartStatus;
    //   310: iload_3
    //   311: iconst_1
    //   312: isub
    //   313: istore #8
    //   315: aload_0
    //   316: aload_0
    //   317: getfield undecodedChunk : Lcom/github/steveice10/netty/buffer/ByteBuf;
    //   320: iload_2
    //   321: iload #8
    //   323: iload_2
    //   324: isub
    //   325: invokevirtual copy : (II)Lcom/github/steveice10/netty/buffer/ByteBuf;
    //   328: invokespecial setFinalBuffer : (Lcom/github/steveice10/netty/buffer/ByteBuf;)V
    //   331: iload_3
    //   332: istore_2
    //   333: iconst_1
    //   334: istore #4
    //   336: goto -> 519
    //   339: iload #5
    //   341: bipush #13
    //   343: if_icmpne -> 460
    //   346: aload_1
    //   347: getfield pos : I
    //   350: aload_1
    //   351: getfield limit : I
    //   354: if_icmpge -> 447
    //   357: aload_1
    //   358: getfield bytes : [B
    //   361: aload_1
    //   362: dup
    //   363: getfield pos : I
    //   366: dup_x1
    //   367: iconst_1
    //   368: iadd
    //   369: putfield pos : I
    //   372: baload
    //   373: sipush #255
    //   376: iand
    //   377: i2c
    //   378: istore #5
    //   380: iinc #3, 1
    //   383: iload #5
    //   385: bipush #10
    //   387: if_icmpne -> 431
    //   390: aload_0
    //   391: getstatic com/github/steveice10/netty/handler/codec/http/multipart/HttpPostRequestDecoder$MultiPartStatus.PREEPILOGUE : Lcom/github/steveice10/netty/handler/codec/http/multipart/HttpPostRequestDecoder$MultiPartStatus;
    //   394: putfield currentStatus : Lcom/github/steveice10/netty/handler/codec/http/multipart/HttpPostRequestDecoder$MultiPartStatus;
    //   397: iload_3
    //   398: iconst_2
    //   399: isub
    //   400: istore #8
    //   402: aload_1
    //   403: iconst_0
    //   404: invokevirtual setReadPosition : (I)V
    //   407: aload_0
    //   408: aload_0
    //   409: getfield undecodedChunk : Lcom/github/steveice10/netty/buffer/ByteBuf;
    //   412: iload_2
    //   413: iload #8
    //   415: iload_2
    //   416: isub
    //   417: invokevirtual copy : (II)Lcom/github/steveice10/netty/buffer/ByteBuf;
    //   420: invokespecial setFinalBuffer : (Lcom/github/steveice10/netty/buffer/ByteBuf;)V
    //   423: iload_3
    //   424: istore_2
    //   425: iconst_0
    //   426: istore #4
    //   428: goto -> 522
    //   431: aload_1
    //   432: iconst_0
    //   433: invokevirtual setReadPosition : (I)V
    //   436: new com/github/steveice10/netty/handler/codec/http/multipart/HttpPostRequestDecoder$ErrorDataDecoderException
    //   439: dup
    //   440: ldc_w 'Bad end of line'
    //   443: invokespecial <init> : (Ljava/lang/String;)V
    //   446: athrow
    //   447: aload_1
    //   448: getfield limit : I
    //   451: ifle -> 519
    //   454: iinc #3, -1
    //   457: goto -> 519
    //   460: iload #5
    //   462: bipush #10
    //   464: if_icmpne -> 519
    //   467: aload_0
    //   468: getstatic com/github/steveice10/netty/handler/codec/http/multipart/HttpPostRequestDecoder$MultiPartStatus.PREEPILOGUE : Lcom/github/steveice10/netty/handler/codec/http/multipart/HttpPostRequestDecoder$MultiPartStatus;
    //   471: putfield currentStatus : Lcom/github/steveice10/netty/handler/codec/http/multipart/HttpPostRequestDecoder$MultiPartStatus;
    //   474: iload_3
    //   475: iconst_1
    //   476: isub
    //   477: istore #8
    //   479: aload_1
    //   480: iconst_0
    //   481: invokevirtual setReadPosition : (I)V
    //   484: aload_0
    //   485: aload_0
    //   486: getfield undecodedChunk : Lcom/github/steveice10/netty/buffer/ByteBuf;
    //   489: iload_2
    //   490: iload #8
    //   492: iload_2
    //   493: isub
    //   494: invokevirtual copy : (II)Lcom/github/steveice10/netty/buffer/ByteBuf;
    //   497: invokespecial setFinalBuffer : (Lcom/github/steveice10/netty/buffer/ByteBuf;)V
    //   500: iload_3
    //   501: istore_2
    //   502: iconst_0
    //   503: istore #4
    //   505: goto -> 522
    //   508: aload_1
    //   509: iconst_0
    //   510: invokevirtual setReadPosition : (I)V
    //   513: iconst_0
    //   514: istore #4
    //   516: goto -> 522
    //   519: goto -> 57
    //   522: aload_0
    //   523: getfield isLastChunk : Z
    //   526: ifeq -> 595
    //   529: aload_0
    //   530: getfield currentAttribute : Lcom/github/steveice10/netty/handler/codec/http/multipart/Attribute;
    //   533: ifnull -> 595
    //   536: iload_3
    //   537: istore #8
    //   539: iload #8
    //   541: iload_2
    //   542: if_icmple -> 564
    //   545: aload_0
    //   546: aload_0
    //   547: getfield undecodedChunk : Lcom/github/steveice10/netty/buffer/ByteBuf;
    //   550: iload_2
    //   551: iload #8
    //   553: iload_2
    //   554: isub
    //   555: invokevirtual copy : (II)Lcom/github/steveice10/netty/buffer/ByteBuf;
    //   558: invokespecial setFinalBuffer : (Lcom/github/steveice10/netty/buffer/ByteBuf;)V
    //   561: goto -> 583
    //   564: aload_0
    //   565: getfield currentAttribute : Lcom/github/steveice10/netty/handler/codec/http/multipart/Attribute;
    //   568: invokeinterface isCompleted : ()Z
    //   573: ifne -> 583
    //   576: aload_0
    //   577: getstatic com/github/steveice10/netty/buffer/Unpooled.EMPTY_BUFFER : Lcom/github/steveice10/netty/buffer/ByteBuf;
    //   580: invokespecial setFinalBuffer : (Lcom/github/steveice10/netty/buffer/ByteBuf;)V
    //   583: iload_3
    //   584: istore_2
    //   585: aload_0
    //   586: getstatic com/github/steveice10/netty/handler/codec/http/multipart/HttpPostRequestDecoder$MultiPartStatus.EPILOGUE : Lcom/github/steveice10/netty/handler/codec/http/multipart/HttpPostRequestDecoder$MultiPartStatus;
    //   589: putfield currentStatus : Lcom/github/steveice10/netty/handler/codec/http/multipart/HttpPostRequestDecoder$MultiPartStatus;
    //   592: goto -> 640
    //   595: iload #4
    //   597: ifeq -> 640
    //   600: aload_0
    //   601: getfield currentAttribute : Lcom/github/steveice10/netty/handler/codec/http/multipart/Attribute;
    //   604: ifnull -> 640
    //   607: aload_0
    //   608: getfield currentStatus : Lcom/github/steveice10/netty/handler/codec/http/multipart/HttpPostRequestDecoder$MultiPartStatus;
    //   611: getstatic com/github/steveice10/netty/handler/codec/http/multipart/HttpPostRequestDecoder$MultiPartStatus.FIELD : Lcom/github/steveice10/netty/handler/codec/http/multipart/HttpPostRequestDecoder$MultiPartStatus;
    //   614: if_acmpne -> 640
    //   617: aload_0
    //   618: getfield currentAttribute : Lcom/github/steveice10/netty/handler/codec/http/multipart/Attribute;
    //   621: aload_0
    //   622: getfield undecodedChunk : Lcom/github/steveice10/netty/buffer/ByteBuf;
    //   625: iload_2
    //   626: iload_3
    //   627: iload_2
    //   628: isub
    //   629: invokevirtual copy : (II)Lcom/github/steveice10/netty/buffer/ByteBuf;
    //   632: iconst_0
    //   633: invokeinterface addContent : (Lcom/github/steveice10/netty/buffer/ByteBuf;Z)V
    //   638: iload_3
    //   639: istore_2
    //   640: aload_0
    //   641: getfield undecodedChunk : Lcom/github/steveice10/netty/buffer/ByteBuf;
    //   644: iload_2
    //   645: invokevirtual readerIndex : (I)Lcom/github/steveice10/netty/buffer/ByteBuf;
    //   648: pop
    //   649: goto -> 708
    //   652: astore #5
    //   654: aload_0
    //   655: getfield undecodedChunk : Lcom/github/steveice10/netty/buffer/ByteBuf;
    //   658: iload_2
    //   659: invokevirtual readerIndex : (I)Lcom/github/steveice10/netty/buffer/ByteBuf;
    //   662: pop
    //   663: aload #5
    //   665: athrow
    //   666: astore #5
    //   668: aload_0
    //   669: getfield undecodedChunk : Lcom/github/steveice10/netty/buffer/ByteBuf;
    //   672: iload_2
    //   673: invokevirtual readerIndex : (I)Lcom/github/steveice10/netty/buffer/ByteBuf;
    //   676: pop
    //   677: new com/github/steveice10/netty/handler/codec/http/multipart/HttpPostRequestDecoder$ErrorDataDecoderException
    //   680: dup
    //   681: aload #5
    //   683: invokespecial <init> : (Ljava/lang/Throwable;)V
    //   686: athrow
    //   687: astore #5
    //   689: aload_0
    //   690: getfield undecodedChunk : Lcom/github/steveice10/netty/buffer/ByteBuf;
    //   693: iload_2
    //   694: invokevirtual readerIndex : (I)Lcom/github/steveice10/netty/buffer/ByteBuf;
    //   697: pop
    //   698: new com/github/steveice10/netty/handler/codec/http/multipart/HttpPostRequestDecoder$ErrorDataDecoderException
    //   701: dup
    //   702: aload #5
    //   704: invokespecial <init> : (Ljava/lang/Throwable;)V
    //   707: athrow
    //   708: return
    // Line number table:
    //   Java source line number -> byte code offset
    //   #496	-> 0
    //   #497	-> 10
    //   #498	-> 14
    //   #500	-> 15
    //   #501	-> 27
    //   #502	-> 35
    //   #505	-> 37
    //   #506	-> 47
    //   #508	-> 54
    //   #510	-> 57
    //   #511	-> 68
    //   #512	-> 91
    //   #513	-> 94
    //   #515	-> 132
    //   #516	-> 139
    //   #517	-> 146
    //   #518	-> 151
    //   #520	-> 176
    //   #521	-> 195
    //   #522	-> 197
    //   #523	-> 207
    //   #524	-> 214
    //   #525	-> 219
    //   #526	-> 232
    //   #525	-> 239
    //   #527	-> 244
    //   #528	-> 263
    //   #529	-> 275
    //   #530	-> 283
    //   #531	-> 288
    //   #532	-> 290
    //   #533	-> 293
    //   #536	-> 296
    //   #537	-> 303
    //   #538	-> 310
    //   #539	-> 315
    //   #540	-> 331
    //   #541	-> 333
    //   #542	-> 339
    //   #543	-> 346
    //   #544	-> 357
    //   #545	-> 380
    //   #546	-> 383
    //   #547	-> 390
    //   #548	-> 397
    //   #549	-> 402
    //   #550	-> 407
    //   #551	-> 423
    //   #552	-> 425
    //   #553	-> 428
    //   #556	-> 431
    //   #557	-> 436
    //   #560	-> 447
    //   #561	-> 454
    //   #564	-> 460
    //   #565	-> 467
    //   #566	-> 474
    //   #567	-> 479
    //   #568	-> 484
    //   #569	-> 500
    //   #570	-> 502
    //   #571	-> 505
    //   #576	-> 508
    //   #577	-> 513
    //   #578	-> 516
    //   #580	-> 519
    //   #581	-> 522
    //   #583	-> 536
    //   #584	-> 539
    //   #585	-> 545
    //   #586	-> 564
    //   #587	-> 576
    //   #589	-> 583
    //   #590	-> 585
    //   #591	-> 595
    //   #593	-> 617
    //   #595	-> 638
    //   #597	-> 640
    //   #610	-> 649
    //   #598	-> 652
    //   #600	-> 654
    //   #601	-> 663
    //   #602	-> 666
    //   #604	-> 668
    //   #605	-> 677
    //   #606	-> 687
    //   #608	-> 689
    //   #609	-> 698
    //   #611	-> 708
    // Local variable table:
    //   start	length	slot	name	descriptor
    //   176	21	7	key	Ljava/lang/String;
    //   151	49	6	equalpos	I
    //   244	49	7	key	Ljava/lang/String;
    //   219	77	8	ampersandpos	I
    //   315	24	8	ampersandpos	I
    //   402	29	8	ampersandpos	I
    //   479	29	8	ampersandpos	I
    //   91	428	5	read	C
    //   539	56	8	ampersandpos	I
    //   654	12	5	e	Lcom/github/steveice10/netty/handler/codec/http/multipart/HttpPostRequestDecoder$ErrorDataDecoderException;
    //   668	19	5	e	Ljava/io/IOException;
    //   689	19	5	e	Ljava/lang/IllegalArgumentException;
    //   0	709	0	this	Lcom/github/steveice10/netty/handler/codec/http/multipart/HttpPostStandardRequestDecoder;
    //   27	682	1	sao	Lcom/github/steveice10/netty/handler/codec/http/multipart/HttpPostBodyUtil$SeekAheadOptimize;
    //   35	674	2	firstpos	I
    //   37	672	3	currentpos	I
    //   57	652	4	contRead	Z
    // Exception table:
    //   from	to	target	type
    //   57	649	652	com/github/steveice10/netty/handler/codec/http/multipart/HttpPostRequestDecoder$ErrorDataDecoderException
    //   57	649	666	java/io/IOException
    //   57	649	687	java/lang/IllegalArgumentException
  }
  
  private void setFinalBuffer(ByteBuf buffer) throws IOException {
    this.currentAttribute.addContent(buffer, true);
    String value = decodeAttribute(this.currentAttribute.getByteBuf().toString(this.charset), this.charset);
    this.currentAttribute.setValue(value);
    addHttpData(this.currentAttribute);
    this.currentAttribute = null;
  }
  
  private static String decodeAttribute(String s, Charset charset) {
    try {
      return QueryStringDecoder.decodeComponent(s, charset);
    } catch (IllegalArgumentException e) {
      throw new HttpPostRequestDecoder.ErrorDataDecoderException("Bad string: '" + s + '\'', e);
    } 
  }
  
  public void destroy() {
    cleanFiles();
    this.destroyed = true;
    if (this.undecodedChunk != null && this.undecodedChunk.refCnt() > 0) {
      this.undecodedChunk.release();
      this.undecodedChunk = null;
    } 
  }
  
  public void cleanFiles() {
    checkDestroyed();
    this.factory.cleanRequestHttpData(this.request);
  }
  
  public void removeHttpDataFromClean(InterfaceHttpData data) {
    checkDestroyed();
    this.factory.removeHttpDataFromClean(this.request, data);
  }
}
