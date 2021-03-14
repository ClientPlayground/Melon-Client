package com.github.steveice10.netty.handler.codec.http.cookie;

import com.github.steveice10.netty.handler.codec.DateFormatter;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class ServerCookieEncoder extends CookieEncoder {
  public static final ServerCookieEncoder STRICT = new ServerCookieEncoder(true);
  
  public static final ServerCookieEncoder LAX = new ServerCookieEncoder(false);
  
  private ServerCookieEncoder(boolean strict) {
    super(strict);
  }
  
  public String encode(String name, String value) {
    return encode(new DefaultCookie(name, value));
  }
  
  public String encode(Cookie cookie) {
    String name = ((Cookie)ObjectUtil.checkNotNull(cookie, "cookie")).name();
    String value = (cookie.value() != null) ? cookie.value() : "";
    validateCookie(name, value);
    StringBuilder buf = CookieUtil.stringBuilder();
    if (cookie.wrap()) {
      CookieUtil.addQuoted(buf, name, value);
    } else {
      CookieUtil.add(buf, name, value);
    } 
    if (cookie.maxAge() != Long.MIN_VALUE) {
      CookieUtil.add(buf, "Max-Age", cookie.maxAge());
      Date expires = new Date(cookie.maxAge() * 1000L + System.currentTimeMillis());
      buf.append("Expires");
      buf.append('=');
      DateFormatter.append(expires, buf);
      buf.append(';');
      buf.append(' ');
    } 
    if (cookie.path() != null)
      CookieUtil.add(buf, "Path", cookie.path()); 
    if (cookie.domain() != null)
      CookieUtil.add(buf, "Domain", cookie.domain()); 
    if (cookie.isSecure())
      CookieUtil.add(buf, "Secure"); 
    if (cookie.isHttpOnly())
      CookieUtil.add(buf, "HTTPOnly"); 
    return CookieUtil.stripTrailingSeparator(buf);
  }
  
  private static List<String> dedup(List<String> encoded, Map<String, Integer> nameToLastIndex) {
    boolean[] isLastInstance = new boolean[encoded.size()];
    for (Iterator<Integer> iterator = nameToLastIndex.values().iterator(); iterator.hasNext(); ) {
      int idx = ((Integer)iterator.next()).intValue();
      isLastInstance[idx] = true;
    } 
    List<String> dedupd = new ArrayList<String>(nameToLastIndex.size());
    for (int i = 0, n = encoded.size(); i < n; i++) {
      if (isLastInstance[i])
        dedupd.add(encoded.get(i)); 
    } 
    return dedupd;
  }
  
  public List<String> encode(Cookie... cookies) {
    int j;
    if (((Cookie[])ObjectUtil.checkNotNull(cookies, "cookies")).length == 0)
      return Collections.emptyList(); 
    List<String> encoded = new ArrayList<String>(cookies.length);
    Map<String, Integer> nameToIndex = (this.strict && cookies.length > 1) ? new HashMap<String, Integer>() : null;
    boolean hasDupdName = false;
    for (int i = 0; i < cookies.length; i++) {
      Cookie c = cookies[i];
      encoded.add(encode(c));
      if (nameToIndex != null)
        j = hasDupdName | ((nameToIndex.put(c.name(), Integer.valueOf(i)) != null) ? 1 : 0); 
    } 
    return (j != 0) ? dedup(encoded, nameToIndex) : encoded;
  }
  
  public List<String> encode(Collection<? extends Cookie> cookies) {
    int j;
    if (((Collection)ObjectUtil.checkNotNull(cookies, "cookies")).isEmpty())
      return Collections.emptyList(); 
    List<String> encoded = new ArrayList<String>(cookies.size());
    Map<String, Integer> nameToIndex = (this.strict && cookies.size() > 1) ? new HashMap<String, Integer>() : null;
    int i = 0;
    boolean hasDupdName = false;
    for (Cookie c : cookies) {
      encoded.add(encode(c));
      if (nameToIndex != null)
        j = hasDupdName | ((nameToIndex.put(c.name(), Integer.valueOf(i++)) != null) ? 1 : 0); 
    } 
    return (j != 0) ? dedup(encoded, nameToIndex) : encoded;
  }
  
  public List<String> encode(Iterable<? extends Cookie> cookies) {
    int j;
    Iterator<? extends Cookie> cookiesIt = ((Iterable<? extends Cookie>)ObjectUtil.checkNotNull(cookies, "cookies")).iterator();
    if (!cookiesIt.hasNext())
      return Collections.emptyList(); 
    List<String> encoded = new ArrayList<String>();
    Cookie firstCookie = cookiesIt.next();
    Map<String, Integer> nameToIndex = (this.strict && cookiesIt.hasNext()) ? new HashMap<String, Integer>() : null;
    int i = 0;
    encoded.add(encode(firstCookie));
    boolean hasDupdName = (nameToIndex != null && nameToIndex.put(firstCookie.name(), Integer.valueOf(i++)) != null);
    while (cookiesIt.hasNext()) {
      Cookie c = cookiesIt.next();
      encoded.add(encode(c));
      if (nameToIndex != null)
        j = hasDupdName | ((nameToIndex.put(c.name(), Integer.valueOf(i++)) != null) ? 1 : 0); 
    } 
    return (j != 0) ? dedup(encoded, nameToIndex) : encoded;
  }
}
