package com.github.steveice10.netty.handler.codec.stomp;

import com.github.steveice10.netty.handler.codec.Headers;
import com.github.steveice10.netty.util.AsciiString;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public interface StompHeaders extends Headers<CharSequence, CharSequence, StompHeaders> {
  public static final AsciiString ACCEPT_VERSION = AsciiString.cached("accept-version");
  
  public static final AsciiString HOST = AsciiString.cached("host");
  
  public static final AsciiString LOGIN = AsciiString.cached("login");
  
  public static final AsciiString PASSCODE = AsciiString.cached("passcode");
  
  public static final AsciiString HEART_BEAT = AsciiString.cached("heart-beat");
  
  public static final AsciiString VERSION = AsciiString.cached("version");
  
  public static final AsciiString SESSION = AsciiString.cached("session");
  
  public static final AsciiString SERVER = AsciiString.cached("server");
  
  public static final AsciiString DESTINATION = AsciiString.cached("destination");
  
  public static final AsciiString ID = AsciiString.cached("id");
  
  public static final AsciiString ACK = AsciiString.cached("ack");
  
  public static final AsciiString TRANSACTION = AsciiString.cached("transaction");
  
  public static final AsciiString RECEIPT = AsciiString.cached("receipt");
  
  public static final AsciiString MESSAGE_ID = AsciiString.cached("message-id");
  
  public static final AsciiString SUBSCRIPTION = AsciiString.cached("subscription");
  
  public static final AsciiString RECEIPT_ID = AsciiString.cached("receipt-id");
  
  public static final AsciiString MESSAGE = AsciiString.cached("message");
  
  public static final AsciiString CONTENT_LENGTH = AsciiString.cached("content-length");
  
  public static final AsciiString CONTENT_TYPE = AsciiString.cached("content-type");
  
  String getAsString(CharSequence paramCharSequence);
  
  List<String> getAllAsString(CharSequence paramCharSequence);
  
  Iterator<Map.Entry<String, String>> iteratorAsString();
  
  boolean contains(CharSequence paramCharSequence1, CharSequence paramCharSequence2, boolean paramBoolean);
}
