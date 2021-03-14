package com.github.steveice10.netty.util;

import com.github.steveice10.netty.util.internal.PlatformDependent;
import com.github.steveice10.netty.util.internal.SocketUtils;
import com.github.steveice10.netty.util.internal.StringUtil;
import com.github.steveice10.netty.util.internal.SystemPropertyUtil;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public final class NetUtil {
  public static final Inet4Address LOCALHOST4;
  
  public static final Inet6Address LOCALHOST6;
  
  public static final InetAddress LOCALHOST;
  
  public static final NetworkInterface LOOPBACK_IF;
  
  public static final int SOMAXCONN;
  
  private static final int IPV6_WORD_COUNT = 8;
  
  private static final int IPV6_MAX_CHAR_COUNT = 39;
  
  private static final int IPV6_BYTE_COUNT = 16;
  
  private static final int IPV6_MAX_CHAR_BETWEEN_SEPARATOR = 4;
  
  private static final int IPV6_MIN_SEPARATORS = 2;
  
  private static final int IPV6_MAX_SEPARATORS = 8;
  
  private static final int IPV4_MAX_CHAR_BETWEEN_SEPARATOR = 3;
  
  private static final int IPV4_SEPARATORS = 3;
  
  private static final boolean IPV4_PREFERRED = SystemPropertyUtil.getBoolean("java.net.preferIPv4Stack", false);
  
  private static final boolean IPV6_ADDRESSES_PREFERRED = SystemPropertyUtil.getBoolean("java.net.preferIPv6Addresses", false);
  
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(NetUtil.class);
  
  static {
    logger.debug("-Djava.net.preferIPv4Stack: {}", Boolean.valueOf(IPV4_PREFERRED));
    logger.debug("-Djava.net.preferIPv6Addresses: {}", Boolean.valueOf(IPV6_ADDRESSES_PREFERRED));
    byte[] LOCALHOST4_BYTES = { Byte.MAX_VALUE, 0, 0, 1 };
    byte[] LOCALHOST6_BYTES = { 
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
        0, 0, 0, 0, 0, 1 };
    Inet4Address localhost4 = null;
    try {
      localhost4 = (Inet4Address)InetAddress.getByAddress("localhost", LOCALHOST4_BYTES);
    } catch (Exception e) {
      PlatformDependent.throwException(e);
    } 
    LOCALHOST4 = localhost4;
    Inet6Address localhost6 = null;
    try {
      localhost6 = (Inet6Address)InetAddress.getByAddress("localhost", LOCALHOST6_BYTES);
    } catch (Exception e) {
      PlatformDependent.throwException(e);
    } 
    LOCALHOST6 = localhost6;
    List<NetworkInterface> ifaces = new ArrayList<NetworkInterface>();
    try {
      Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
      if (interfaces != null)
        while (interfaces.hasMoreElements()) {
          NetworkInterface iface = interfaces.nextElement();
          if (SocketUtils.addressesFromNetworkInterface(iface).hasMoreElements())
            ifaces.add(iface); 
        }  
    } catch (SocketException e) {
      logger.warn("Failed to retrieve the list of available network interfaces", e);
    } 
    NetworkInterface loopbackIface = null;
    InetAddress loopbackAddr = null;
    label71: for (NetworkInterface iface : ifaces) {
      for (Enumeration<InetAddress> i = SocketUtils.addressesFromNetworkInterface(iface); i.hasMoreElements(); ) {
        InetAddress addr = i.nextElement();
        if (addr.isLoopbackAddress()) {
          loopbackIface = iface;
          loopbackAddr = addr;
          break label71;
        } 
      } 
    } 
    if (loopbackIface == null)
      try {
        for (NetworkInterface iface : ifaces) {
          if (iface.isLoopback()) {
            Enumeration<InetAddress> i = SocketUtils.addressesFromNetworkInterface(iface);
            if (i.hasMoreElements()) {
              loopbackIface = iface;
              loopbackAddr = i.nextElement();
              break;
            } 
          } 
        } 
        if (loopbackIface == null)
          logger.warn("Failed to find the loopback interface"); 
      } catch (SocketException e) {
        logger.warn("Failed to find the loopback interface", e);
      }  
    if (loopbackIface != null) {
      logger.debug("Loopback interface: {} ({}, {})", new Object[] { loopbackIface
            
            .getName(), loopbackIface.getDisplayName(), loopbackAddr.getHostAddress() });
    } else if (loopbackAddr == null) {
      try {
        if (NetworkInterface.getByInetAddress(LOCALHOST6) != null) {
          logger.debug("Using hard-coded IPv6 localhost address: {}", localhost6);
          loopbackAddr = localhost6;
        } 
      } catch (Exception exception) {
      
      } finally {
        if (loopbackAddr == null) {
          logger.debug("Using hard-coded IPv4 localhost address: {}", localhost4);
          loopbackAddr = localhost4;
        } 
      } 
    } 
    LOOPBACK_IF = loopbackIface;
    LOCALHOST = loopbackAddr;
    SOMAXCONN = ((Integer)AccessController.<Integer>doPrivileged(new PrivilegedAction<Integer>() {
          public Integer run() {
            int somaxconn = PlatformDependent.isWindows() ? 200 : 128;
            File file = new File("/proc/sys/net/core/somaxconn");
            BufferedReader in = null;
            try {
              if (file.exists()) {
                in = new BufferedReader(new FileReader(file));
                somaxconn = Integer.parseInt(in.readLine());
                if (NetUtil.logger.isDebugEnabled())
                  NetUtil.logger.debug("{}: {}", file, Integer.valueOf(somaxconn)); 
              } else {
                Integer tmp = null;
                if (SystemPropertyUtil.getBoolean("com.github.steveice10.netty.net.somaxconn.trySysctl", false)) {
                  tmp = NetUtil.sysctlGetInt("kern.ipc.somaxconn");
                  if (tmp == null) {
                    tmp = NetUtil.sysctlGetInt("kern.ipc.soacceptqueue");
                    if (tmp != null)
                      somaxconn = tmp.intValue(); 
                  } else {
                    somaxconn = tmp.intValue();
                  } 
                } 
                if (tmp == null)
                  NetUtil.logger.debug("Failed to get SOMAXCONN from sysctl and file {}. Default: {}", file, 
                      Integer.valueOf(somaxconn)); 
              } 
            } catch (Exception e) {
              NetUtil.logger.debug("Failed to get SOMAXCONN from sysctl and file {}. Default: {}", new Object[] { file, Integer.valueOf(somaxconn), e });
            } finally {
              if (in != null)
                try {
                  in.close();
                } catch (Exception exception) {} 
            } 
            return Integer.valueOf(somaxconn);
          }
        })).intValue();
  }
  
  private static Integer sysctlGetInt(String sysctlKey) throws IOException {
    Process process = (new ProcessBuilder(new String[] { "sysctl", sysctlKey })).start();
    try {
      InputStream is = process.getInputStream();
      InputStreamReader isr = new InputStreamReader(is);
      BufferedReader br = new BufferedReader(isr);
    } finally {
      if (process != null)
        process.destroy(); 
    } 
  }
  
  public static boolean isIpV4StackPreferred() {
    return IPV4_PREFERRED;
  }
  
  public static boolean isIpV6AddressesPreferred() {
    return IPV6_ADDRESSES_PREFERRED;
  }
  
  public static byte[] createByteArrayFromIpAddressString(String ipAddressString) {
    if (isValidIpV4Address(ipAddressString))
      return validIpV4ToBytes(ipAddressString); 
    if (isValidIpV6Address(ipAddressString)) {
      if (ipAddressString.charAt(0) == '[')
        ipAddressString = ipAddressString.substring(1, ipAddressString.length() - 1); 
      int percentPos = ipAddressString.indexOf('%');
      if (percentPos >= 0)
        ipAddressString = ipAddressString.substring(0, percentPos); 
      return getIPv6ByName(ipAddressString, true);
    } 
    return null;
  }
  
  private static int decimalDigit(String str, int pos) {
    return str.charAt(pos) - 48;
  }
  
  private static byte ipv4WordToByte(String ip, int from, int toExclusive) {
    int ret = decimalDigit(ip, from);
    from++;
    if (from == toExclusive)
      return (byte)ret; 
    ret = ret * 10 + decimalDigit(ip, from);
    from++;
    if (from == toExclusive)
      return (byte)ret; 
    return (byte)(ret * 10 + decimalDigit(ip, from));
  }
  
  static byte[] validIpV4ToBytes(String ip) {
    int i;
    return new byte[] { ipv4WordToByte(ip, 0, i = ip.indexOf('.', 1)), 
        ipv4WordToByte(ip, i + 1, i = ip.indexOf('.', i + 2)), 
        ipv4WordToByte(ip, i + 1, i = ip.indexOf('.', i + 2)), 
        ipv4WordToByte(ip, i + 1, ip.length()) };
  }
  
  public static String intToIpAddress(int i) {
    StringBuilder buf = new StringBuilder(15);
    buf.append(i >> 24 & 0xFF);
    buf.append('.');
    buf.append(i >> 16 & 0xFF);
    buf.append('.');
    buf.append(i >> 8 & 0xFF);
    buf.append('.');
    buf.append(i & 0xFF);
    return buf.toString();
  }
  
  public static String bytesToIpAddress(byte[] bytes) {
    return bytesToIpAddress(bytes, 0, bytes.length);
  }
  
  public static String bytesToIpAddress(byte[] bytes, int offset, int length) {
    switch (length) {
      case 4:
        return (new StringBuilder(15))
          .append(bytes[offset] & 0xFF)
          .append('.')
          .append(bytes[offset + 1] & 0xFF)
          .append('.')
          .append(bytes[offset + 2] & 0xFF)
          .append('.')
          .append(bytes[offset + 3] & 0xFF).toString();
      case 16:
        return toAddressString(bytes, offset, false);
    } 
    throw new IllegalArgumentException("length: " + length + " (expected: 4 or 16)");
  }
  
  public static boolean isValidIpV6Address(String ip) {
    return isValidIpV6Address(ip);
  }
  
  public static boolean isValidIpV6Address(CharSequence ip) {
    int start, colons, compressBegin, end = ip.length();
    if (end < 2)
      return false; 
    char c = ip.charAt(0);
    if (c == '[') {
      end--;
      if (ip.charAt(end) != ']')
        return false; 
      start = 1;
      c = ip.charAt(1);
    } else {
      start = 0;
    } 
    if (c == ':') {
      if (ip.charAt(start + 1) != ':')
        return false; 
      colons = 2;
      compressBegin = start;
      start += 2;
    } else {
      colons = 0;
      compressBegin = -1;
    } 
    int wordLen = 0;
    for (int i = start; i < end; i++) {
      c = ip.charAt(i);
      if (isValidHexChar(c)) {
        if (wordLen < 4) {
          wordLen++;
        } else {
          return false;
        } 
      } else {
        int ipv4Start;
        int j;
        int ipv4End;
        switch (c) {
          case ':':
            if (colons > 7)
              return false; 
            if (ip.charAt(i - 1) == ':') {
              if (compressBegin >= 0)
                return false; 
              compressBegin = i - 1;
            } else {
              wordLen = 0;
            } 
            colons++;
            break;
          case '.':
            if ((compressBegin < 0 && colons != 6) || (colons == 7 && compressBegin >= start) || colons > 7)
              return false; 
            ipv4Start = i - wordLen;
            j = ipv4Start - 2;
            if (isValidIPv4MappedChar(ip.charAt(j))) {
              if (!isValidIPv4MappedChar(ip.charAt(j - 1)) || 
                !isValidIPv4MappedChar(ip.charAt(j - 2)) || 
                !isValidIPv4MappedChar(ip.charAt(j - 3)))
                return false; 
              j -= 5;
            } 
            for (; j >= start; j--) {
              char tmpChar = ip.charAt(j);
              if (tmpChar != '0' && tmpChar != ':')
                return false; 
            } 
            ipv4End = AsciiString.indexOf(ip, '%', ipv4Start + 7);
            if (ipv4End < 0)
              ipv4End = end; 
            return isValidIpV4Address(ip, ipv4Start, ipv4End);
          case '%':
            end = i;
            break;
          default:
            return false;
        } 
      } 
    } 
    if (compressBegin < 0)
      return (colons == 7 && wordLen > 0); 
    return (compressBegin + 2 == end || (wordLen > 0 && (colons < 8 || compressBegin <= start)));
  }
  
  private static boolean isValidIpV4Word(CharSequence word, int from, int toExclusive) {
    int len = toExclusive - from;
    char c0;
    if (len < 1 || len > 3 || (c0 = word.charAt(from)) < '0')
      return false; 
    if (len == 3) {
      char c1;
      char c2;
      return ((c1 = word.charAt(from + 1)) >= '0' && (
        c2 = word.charAt(from + 2)) >= '0' && ((c0 <= '1' && c1 <= '9' && c2 <= '9') || (c0 == '2' && c1 <= '5' && (c2 <= '5' || (c1 < '5' && c2 <= '9')))));
    } 
    return (c0 <= '9' && (len == 1 || isValidNumericChar(word.charAt(from + 1))));
  }
  
  private static boolean isValidHexChar(char c) {
    return ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f'));
  }
  
  private static boolean isValidNumericChar(char c) {
    return (c >= '0' && c <= '9');
  }
  
  private static boolean isValidIPv4MappedChar(char c) {
    return (c == 'f' || c == 'F');
  }
  
  private static boolean isValidIPv4MappedSeparators(byte b0, byte b1, boolean mustBeZero) {
    return (b0 == b1 && (b0 == 0 || (!mustBeZero && b1 == -1)));
  }
  
  private static boolean isValidIPv4Mapped(byte[] bytes, int currentIndex, int compressBegin, int compressLength) {
    boolean mustBeZero = (compressBegin + compressLength >= 14);
    return (currentIndex <= 12 && currentIndex >= 2 && (!mustBeZero || compressBegin < 12) && 
      isValidIPv4MappedSeparators(bytes[currentIndex - 1], bytes[currentIndex - 2], mustBeZero) && 
      PlatformDependent.isZero(bytes, 0, currentIndex - 3));
  }
  
  public static boolean isValidIpV4Address(CharSequence ip) {
    return isValidIpV4Address(ip, 0, ip.length());
  }
  
  public static boolean isValidIpV4Address(String ip) {
    return isValidIpV4Address(ip, 0, ip.length());
  }
  
  private static boolean isValidIpV4Address(CharSequence ip, int from, int toExcluded) {
    return (ip instanceof String) ? isValidIpV4Address((String)ip, from, toExcluded) : ((ip instanceof AsciiString) ? 
      isValidIpV4Address((AsciiString)ip, from, toExcluded) : 
      isValidIpV4Address0(ip, from, toExcluded));
  }
  
  private static boolean isValidIpV4Address(String ip, int from, int toExcluded) {
    int len = toExcluded - from;
    int i;
    return (len <= 15 && len >= 7 && (
      i = ip.indexOf('.', from + 1)) > 0 && isValidIpV4Word(ip, from, i) && (
      i = ip.indexOf('.', from = i + 2)) > 0 && isValidIpV4Word(ip, from - 1, i) && (
      i = ip.indexOf('.', from = i + 2)) > 0 && isValidIpV4Word(ip, from - 1, i) && 
      isValidIpV4Word(ip, i + 1, toExcluded));
  }
  
  private static boolean isValidIpV4Address(AsciiString ip, int from, int toExcluded) {
    int len = toExcluded - from;
    int i;
    return (len <= 15 && len >= 7 && (
      i = ip.indexOf('.', from + 1)) > 0 && isValidIpV4Word(ip, from, i) && (
      i = ip.indexOf('.', from = i + 2)) > 0 && isValidIpV4Word(ip, from - 1, i) && (
      i = ip.indexOf('.', from = i + 2)) > 0 && isValidIpV4Word(ip, from - 1, i) && 
      isValidIpV4Word(ip, i + 1, toExcluded));
  }
  
  private static boolean isValidIpV4Address0(CharSequence ip, int from, int toExcluded) {
    int len = toExcluded - from;
    int i;
    return (len <= 15 && len >= 7 && (
      i = AsciiString.indexOf(ip, '.', from + 1)) > 0 && isValidIpV4Word(ip, from, i) && (
      i = AsciiString.indexOf(ip, '.', from = i + 2)) > 0 && isValidIpV4Word(ip, from - 1, i) && (
      i = AsciiString.indexOf(ip, '.', from = i + 2)) > 0 && isValidIpV4Word(ip, from - 1, i) && 
      isValidIpV4Word(ip, i + 1, toExcluded));
  }
  
  public static Inet6Address getByName(CharSequence ip) {
    return getByName(ip, true);
  }
  
  public static Inet6Address getByName(CharSequence ip, boolean ipv4Mapped) {
    byte[] bytes = getIPv6ByName(ip, ipv4Mapped);
    if (bytes == null)
      return null; 
    try {
      return Inet6Address.getByAddress((String)null, bytes, -1);
    } catch (UnknownHostException e) {
      throw new RuntimeException(e);
    } 
  }
  
  private static byte[] getIPv6ByName(CharSequence ip, boolean ipv4Mapped) {
    byte[] bytes = new byte[16];
    int ipLength = ip.length();
    int compressBegin = 0;
    int compressLength = 0;
    int currentIndex = 0;
    int value = 0;
    int begin = -1;
    int i = 0;
    int ipv6Separators = 0;
    int ipv4Separators = 0;
    boolean needsShift = false;
    for (; i < ipLength; i++) {
      int tmp;
      char c = ip.charAt(i);
      switch (c) {
        case ':':
          ipv6Separators++;
          if (i - begin > 4 || ipv4Separators > 0 || ipv6Separators > 8 || currentIndex + 1 >= bytes.length)
            return null; 
          value <<= 4 - i - begin << 2;
          if (compressLength > 0)
            compressLength -= 2; 
          bytes[currentIndex++] = (byte)((value & 0xF) << 4 | value >> 4 & 0xF);
          bytes[currentIndex++] = (byte)((value >> 8 & 0xF) << 4 | value >> 12 & 0xF);
          tmp = i + 1;
          if (tmp < ipLength && ip.charAt(tmp) == ':') {
            tmp++;
            if (compressBegin != 0 || (tmp < ipLength && ip.charAt(tmp) == ':'))
              return null; 
            ipv6Separators++;
            needsShift = (ipv6Separators == 2 && value == 0);
            compressBegin = currentIndex;
            compressLength = bytes.length - compressBegin - 2;
            i++;
          } 
          value = 0;
          begin = -1;
          break;
        case '.':
          ipv4Separators++;
          tmp = i - begin;
          if (tmp > 3 || begin < 0 || ipv4Separators > 3 || (ipv6Separators > 0 && currentIndex + compressLength < 12) || i + 1 >= ipLength || currentIndex >= bytes.length || (ipv4Separators == 1 && (!ipv4Mapped || (currentIndex != 0 && 
            
            !isValidIPv4Mapped(bytes, currentIndex, compressBegin, compressLength)) || (tmp == 3 && (
            
            !isValidNumericChar(ip.charAt(i - 1)) || 
            !isValidNumericChar(ip.charAt(i - 2)) || 
            !isValidNumericChar(ip.charAt(i - 3)))) || (tmp == 2 && (
            !isValidNumericChar(ip.charAt(i - 1)) || 
            !isValidNumericChar(ip.charAt(i - 2)))) || (tmp == 1 && 
            !isValidNumericChar(ip.charAt(i - 1))))))
            return null; 
          value <<= 3 - tmp << 2;
          begin = (value & 0xF) * 100 + (value >> 4 & 0xF) * 10 + (value >> 8 & 0xF);
          if (begin < 0 || begin > 255)
            return null; 
          bytes[currentIndex++] = (byte)begin;
          value = 0;
          begin = -1;
          break;
        default:
          if (!isValidHexChar(c) || (ipv4Separators > 0 && !isValidNumericChar(c)))
            return null; 
          if (begin < 0) {
            begin = i;
          } else if (i - begin > 4) {
            return null;
          } 
          value += StringUtil.decodeHexNibble(c) << i - begin << 2;
          break;
      } 
    } 
    boolean isCompressed = (compressBegin > 0);
    if (ipv4Separators > 0) {
      if ((begin > 0 && i - begin > 3) || ipv4Separators != 3 || currentIndex >= bytes.length)
        return null; 
      if (ipv6Separators == 0) {
        compressLength = 12;
      } else if (ipv6Separators >= 2 && ((!isCompressed && ipv6Separators == 6 && ip
        .charAt(0) != ':') || (isCompressed && ipv6Separators < 8 && (ip
        
        .charAt(0) != ':' || compressBegin <= 2)))) {
        compressLength -= 2;
      } else {
        return null;
      } 
      value <<= 3 - i - begin << 2;
      begin = (value & 0xF) * 100 + (value >> 4 & 0xF) * 10 + (value >> 8 & 0xF);
      if (begin < 0 || begin > 255)
        return null; 
      bytes[currentIndex++] = (byte)begin;
    } else {
      int tmp = ipLength - 1;
      if ((begin > 0 && i - begin > 4) || ipv6Separators < 2 || (!isCompressed && (ipv6Separators + 1 != 8 || ip
        
        .charAt(0) == ':' || ip.charAt(tmp) == ':')) || (isCompressed && (ipv6Separators > 8 || (ipv6Separators == 8 && ((compressBegin <= 2 && ip
        
        .charAt(0) != ':') || (compressBegin >= 14 && ip
        .charAt(tmp) != ':'))))) || currentIndex + 1 >= bytes.length || (begin < 0 && ip
        
        .charAt(tmp - 1) != ':') || (compressBegin > 2 && ip
        .charAt(0) == ':'))
        return null; 
      if (begin >= 0 && i - begin <= 4)
        value <<= 4 - i - begin << 2; 
      bytes[currentIndex++] = (byte)((value & 0xF) << 4 | value >> 4 & 0xF);
      bytes[currentIndex++] = (byte)((value >> 8 & 0xF) << 4 | value >> 12 & 0xF);
    } 
    i = currentIndex + compressLength;
    if (needsShift || i >= bytes.length) {
      if (i >= bytes.length)
        compressBegin++; 
      for (i = currentIndex; i < bytes.length; i++) {
        for (begin = bytes.length - 1; begin >= compressBegin; begin--)
          bytes[begin] = bytes[begin - 1]; 
        bytes[begin] = 0;
        compressBegin++;
      } 
    } else {
      for (i = 0; i < compressLength; ) {
        begin = i + compressBegin;
        currentIndex = begin + compressLength;
        if (currentIndex < bytes.length) {
          bytes[currentIndex] = bytes[begin];
          bytes[begin] = 0;
          i++;
        } 
      } 
    } 
    if (ipv4Separators > 0) {
      bytes[11] = -1;
      bytes[10] = -1;
    } 
    return bytes;
  }
  
  public static String toSocketAddressString(InetSocketAddress addr) {
    StringBuilder sb;
    String port = String.valueOf(addr.getPort());
    if (addr.isUnresolved()) {
      String hostname = getHostname(addr);
      sb = newSocketAddressStringBuilder(hostname, port, !isValidIpV6Address(hostname));
    } else {
      InetAddress address = addr.getAddress();
      String hostString = toAddressString(address);
      sb = newSocketAddressStringBuilder(hostString, port, address instanceof Inet4Address);
    } 
    return sb.append(':').append(port).toString();
  }
  
  public static String toSocketAddressString(String host, int port) {
    String portStr = String.valueOf(port);
    return newSocketAddressStringBuilder(host, portStr, 
        !isValidIpV6Address(host)).append(':').append(portStr).toString();
  }
  
  private static StringBuilder newSocketAddressStringBuilder(String host, String port, boolean ipv4) {
    int hostLen = host.length();
    if (ipv4)
      return (new StringBuilder(hostLen + 1 + port.length())).append(host); 
    StringBuilder stringBuilder = new StringBuilder(hostLen + 3 + port.length());
    if (hostLen > 1 && host.charAt(0) == '[' && host.charAt(hostLen - 1) == ']')
      return stringBuilder.append(host); 
    return stringBuilder.append('[').append(host).append(']');
  }
  
  public static String toAddressString(InetAddress ip) {
    return toAddressString(ip, false);
  }
  
  public static String toAddressString(InetAddress ip, boolean ipv4Mapped) {
    if (ip instanceof Inet4Address)
      return ip.getHostAddress(); 
    if (!(ip instanceof Inet6Address))
      throw new IllegalArgumentException("Unhandled type: " + ip); 
    return toAddressString(ip.getAddress(), 0, ipv4Mapped);
  }
  
  private static String toAddressString(byte[] bytes, int offset, boolean ipv4Mapped) {
    int[] words = new int[8];
    int end = offset + words.length;
    int i;
    for (i = offset; i < end; i++)
      words[i] = (bytes[i << 1] & 0xFF) << 8 | bytes[(i << 1) + 1] & 0xFF; 
    int currentStart = -1;
    int shortestStart = -1;
    int shortestLength = 0;
    for (i = 0; i < words.length; i++) {
      if (words[i] == 0) {
        if (currentStart < 0)
          currentStart = i; 
      } else if (currentStart >= 0) {
        int currentLength = i - currentStart;
        if (currentLength > shortestLength) {
          shortestStart = currentStart;
          shortestLength = currentLength;
        } 
        currentStart = -1;
      } 
    } 
    if (currentStart >= 0) {
      int currentLength = i - currentStart;
      if (currentLength > shortestLength) {
        shortestStart = currentStart;
        shortestLength = currentLength;
      } 
    } 
    if (shortestLength == 1) {
      shortestLength = 0;
      shortestStart = -1;
    } 
    int shortestEnd = shortestStart + shortestLength;
    StringBuilder b = new StringBuilder(39);
    if (shortestEnd < 0) {
      b.append(Integer.toHexString(words[0]));
      for (i = 1; i < words.length; i++) {
        b.append(':');
        b.append(Integer.toHexString(words[i]));
      } 
    } else {
      boolean isIpv4Mapped;
      if (inRangeEndExclusive(0, shortestStart, shortestEnd)) {
        b.append("::");
        isIpv4Mapped = (ipv4Mapped && shortestEnd == 5 && words[5] == 65535);
      } else {
        b.append(Integer.toHexString(words[0]));
        isIpv4Mapped = false;
      } 
      for (i = 1; i < words.length; i++) {
        if (!inRangeEndExclusive(i, shortestStart, shortestEnd)) {
          if (!inRangeEndExclusive(i - 1, shortestStart, shortestEnd))
            if (!isIpv4Mapped || i == 6) {
              b.append(':');
            } else {
              b.append('.');
            }  
          if (isIpv4Mapped && i > 5) {
            b.append(words[i] >> 8);
            b.append('.');
            b.append(words[i] & 0xFF);
          } else {
            b.append(Integer.toHexString(words[i]));
          } 
        } else if (!inRangeEndExclusive(i - 1, shortestStart, shortestEnd)) {
          b.append("::");
        } 
      } 
    } 
    return b.toString();
  }
  
  public static String getHostname(InetSocketAddress addr) {
    return (PlatformDependent.javaVersion() >= 7) ? addr.getHostString() : addr.getHostName();
  }
  
  private static boolean inRangeEndExclusive(int value, int start, int end) {
    return (value >= start && value < end);
  }
}
