package com.github.steveice10.netty.handler.codec.http.cookie;

import com.github.steveice10.netty.util.internal.InternalThreadLocalMap;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public final class ClientCookieEncoder extends CookieEncoder {
  public static final ClientCookieEncoder STRICT = new ClientCookieEncoder(true);
  
  public static final ClientCookieEncoder LAX = new ClientCookieEncoder(false);
  
  private ClientCookieEncoder(boolean strict) {
    super(strict);
  }
  
  public String encode(String name, String value) {
    return encode(new DefaultCookie(name, value));
  }
  
  public String encode(Cookie cookie) {
    StringBuilder buf = CookieUtil.stringBuilder();
    encode(buf, (Cookie)ObjectUtil.checkNotNull(cookie, "cookie"));
    return CookieUtil.stripTrailingSeparator(buf);
  }
  
  private static final Comparator<Cookie> COOKIE_COMPARATOR = new Comparator<Cookie>() {
      public int compare(Cookie c1, Cookie c2) {
        String path1 = c1.path();
        String path2 = c2.path();
        int len1 = (path1 == null) ? Integer.MAX_VALUE : path1.length();
        int len2 = (path2 == null) ? Integer.MAX_VALUE : path2.length();
        int diff = len2 - len1;
        if (diff != 0)
          return diff; 
        return -1;
      }
    };
  
  public String encode(Cookie... cookies) {
    if (((Cookie[])ObjectUtil.checkNotNull(cookies, "cookies")).length == 0)
      return null; 
    StringBuilder buf = CookieUtil.stringBuilder();
    if (this.strict) {
      if (cookies.length == 1) {
        encode(buf, cookies[0]);
      } else {
        Cookie[] cookiesSorted = Arrays.<Cookie>copyOf(cookies, cookies.length);
        Arrays.sort(cookiesSorted, COOKIE_COMPARATOR);
        for (Cookie c : cookiesSorted)
          encode(buf, c); 
      } 
    } else {
      for (Cookie c : cookies)
        encode(buf, c); 
    } 
    return CookieUtil.stripTrailingSeparatorOrNull(buf);
  }
  
  public String encode(Collection<? extends Cookie> cookies) {
    if (((Collection)ObjectUtil.checkNotNull(cookies, "cookies")).isEmpty())
      return null; 
    StringBuilder buf = CookieUtil.stringBuilder();
    if (this.strict) {
      if (cookies.size() == 1) {
        encode(buf, cookies.iterator().next());
      } else {
        Cookie[] cookiesSorted = cookies.<Cookie>toArray(new Cookie[cookies.size()]);
        Arrays.sort(cookiesSorted, COOKIE_COMPARATOR);
        for (Cookie c : cookiesSorted)
          encode(buf, c); 
      } 
    } else {
      for (Cookie c : cookies)
        encode(buf, c); 
    } 
    return CookieUtil.stripTrailingSeparatorOrNull(buf);
  }
  
  public String encode(Iterable<? extends Cookie> cookies) {
    Iterator<? extends Cookie> cookiesIt = ((Iterable<? extends Cookie>)ObjectUtil.checkNotNull(cookies, "cookies")).iterator();
    if (!cookiesIt.hasNext())
      return null; 
    StringBuilder buf = CookieUtil.stringBuilder();
    if (this.strict) {
      Cookie firstCookie = cookiesIt.next();
      if (!cookiesIt.hasNext()) {
        encode(buf, firstCookie);
      } else {
        List<Cookie> cookiesList = InternalThreadLocalMap.get().arrayList();
        cookiesList.add(firstCookie);
        while (cookiesIt.hasNext())
          cookiesList.add(cookiesIt.next()); 
        Cookie[] cookiesSorted = cookiesList.<Cookie>toArray(new Cookie[cookiesList.size()]);
        Arrays.sort(cookiesSorted, COOKIE_COMPARATOR);
        for (Cookie c : cookiesSorted)
          encode(buf, c); 
      } 
    } else {
      while (cookiesIt.hasNext())
        encode(buf, cookiesIt.next()); 
    } 
    return CookieUtil.stripTrailingSeparatorOrNull(buf);
  }
  
  private void encode(StringBuilder buf, Cookie c) {
    String name = c.name();
    String value = (c.value() != null) ? c.value() : "";
    validateCookie(name, value);
    if (c.wrap()) {
      CookieUtil.addQuoted(buf, name, value);
    } else {
      CookieUtil.add(buf, name, value);
    } 
  }
}
