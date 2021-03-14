package com.github.steveice10.netty.channel;

import com.github.steveice10.netty.buffer.ByteBufUtil;
import com.github.steveice10.netty.util.internal.EmptyArrays;
import com.github.steveice10.netty.util.internal.MacAddressUtil;
import com.github.steveice10.netty.util.internal.PlatformDependent;
import com.github.steveice10.netty.util.internal.SystemPropertyUtil;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public final class DefaultChannelId implements ChannelId {
  private static final long serialVersionUID = 3884076183504074063L;
  
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultChannelId.class);
  
  private static final byte[] MACHINE_ID;
  
  private static final int PROCESS_ID_LEN = 4;
  
  private static final int PROCESS_ID;
  
  private static final int SEQUENCE_LEN = 4;
  
  private static final int TIMESTAMP_LEN = 8;
  
  private static final int RANDOM_LEN = 4;
  
  private static final AtomicInteger nextSequence = new AtomicInteger();
  
  private final byte[] data;
  
  private final int hashCode;
  
  private transient String shortValue;
  
  private transient String longValue;
  
  public static DefaultChannelId newInstance() {
    return new DefaultChannelId();
  }
  
  static {
    int processId = -1;
    String customProcessId = SystemPropertyUtil.get("com.github.steveice10.netty.processId");
    if (customProcessId != null) {
      try {
        processId = Integer.parseInt(customProcessId);
      } catch (NumberFormatException numberFormatException) {}
      if (processId < 0) {
        processId = -1;
        logger.warn("-Dio.netty.processId: {} (malformed)", customProcessId);
      } else if (logger.isDebugEnabled()) {
        logger.debug("-Dio.netty.processId: {} (user-set)", Integer.valueOf(processId));
      } 
    } 
    if (processId < 0) {
      processId = defaultProcessId();
      if (logger.isDebugEnabled())
        logger.debug("-Dio.netty.processId: {} (auto-detected)", Integer.valueOf(processId)); 
    } 
    PROCESS_ID = processId;
    byte[] machineId = null;
    String customMachineId = SystemPropertyUtil.get("com.github.steveice10.netty.machineId");
    if (customMachineId != null) {
      try {
        machineId = MacAddressUtil.parseMAC(customMachineId);
      } catch (Exception e) {
        logger.warn("-Dio.netty.machineId: {} (malformed)", customMachineId, e);
      } 
      if (machineId != null)
        logger.debug("-Dio.netty.machineId: {} (user-set)", customMachineId); 
    } 
    if (machineId == null) {
      machineId = MacAddressUtil.defaultMachineId();
      if (logger.isDebugEnabled())
        logger.debug("-Dio.netty.machineId: {} (auto-detected)", MacAddressUtil.formatAddress(machineId)); 
    } 
    MACHINE_ID = machineId;
  }
  
  private static int defaultProcessId() {
    int pid;
    String value;
    ClassLoader loader = null;
    try {
      loader = PlatformDependent.getClassLoader(DefaultChannelId.class);
      Class<?> mgmtFactoryType = Class.forName("java.lang.management.ManagementFactory", true, loader);
      Class<?> runtimeMxBeanType = Class.forName("java.lang.management.RuntimeMXBean", true, loader);
      Method getRuntimeMXBean = mgmtFactoryType.getMethod("getRuntimeMXBean", EmptyArrays.EMPTY_CLASSES);
      Object bean = getRuntimeMXBean.invoke(null, EmptyArrays.EMPTY_OBJECTS);
      Method getName = runtimeMxBeanType.getMethod("getName", EmptyArrays.EMPTY_CLASSES);
      value = (String)getName.invoke(bean, EmptyArrays.EMPTY_OBJECTS);
    } catch (Throwable t) {
      logger.debug("Could not invoke ManagementFactory.getRuntimeMXBean().getName(); Android?", t);
      try {
        Class<?> processType = Class.forName("android.os.Process", true, loader);
        Method myPid = processType.getMethod("myPid", EmptyArrays.EMPTY_CLASSES);
        value = myPid.invoke(null, EmptyArrays.EMPTY_OBJECTS).toString();
      } catch (Throwable t2) {
        logger.debug("Could not invoke Process.myPid(); not Android?", t2);
        value = "";
      } 
    } 
    int atIndex = value.indexOf('@');
    if (atIndex >= 0)
      value = value.substring(0, atIndex); 
    try {
      pid = Integer.parseInt(value);
    } catch (NumberFormatException e) {
      pid = -1;
    } 
    if (pid < 0) {
      pid = PlatformDependent.threadLocalRandom().nextInt();
      logger.warn("Failed to find the current process ID from '{}'; using a random value: {}", value, Integer.valueOf(pid));
    } 
    return pid;
  }
  
  private DefaultChannelId() {
    this.data = new byte[MACHINE_ID.length + 4 + 4 + 8 + 4];
    int i = 0;
    System.arraycopy(MACHINE_ID, 0, this.data, i, MACHINE_ID.length);
    i += MACHINE_ID.length;
    i = writeInt(i, PROCESS_ID);
    i = writeInt(i, nextSequence.getAndIncrement());
    i = writeLong(i, Long.reverse(System.nanoTime()) ^ System.currentTimeMillis());
    int random = PlatformDependent.threadLocalRandom().nextInt();
    i = writeInt(i, random);
    assert i == this.data.length;
    this.hashCode = Arrays.hashCode(this.data);
  }
  
  private int writeInt(int i, int value) {
    this.data[i++] = (byte)(value >>> 24);
    this.data[i++] = (byte)(value >>> 16);
    this.data[i++] = (byte)(value >>> 8);
    this.data[i++] = (byte)value;
    return i;
  }
  
  private int writeLong(int i, long value) {
    this.data[i++] = (byte)(int)(value >>> 56L);
    this.data[i++] = (byte)(int)(value >>> 48L);
    this.data[i++] = (byte)(int)(value >>> 40L);
    this.data[i++] = (byte)(int)(value >>> 32L);
    this.data[i++] = (byte)(int)(value >>> 24L);
    this.data[i++] = (byte)(int)(value >>> 16L);
    this.data[i++] = (byte)(int)(value >>> 8L);
    this.data[i++] = (byte)(int)value;
    return i;
  }
  
  public String asShortText() {
    String shortValue = this.shortValue;
    if (shortValue == null)
      this.shortValue = shortValue = ByteBufUtil.hexDump(this.data, this.data.length - 4, 4); 
    return shortValue;
  }
  
  public String asLongText() {
    String longValue = this.longValue;
    if (longValue == null)
      this.longValue = longValue = newLongValue(); 
    return longValue;
  }
  
  private String newLongValue() {
    StringBuilder buf = new StringBuilder(2 * this.data.length + 5);
    int i = 0;
    i = appendHexDumpField(buf, i, MACHINE_ID.length);
    i = appendHexDumpField(buf, i, 4);
    i = appendHexDumpField(buf, i, 4);
    i = appendHexDumpField(buf, i, 8);
    i = appendHexDumpField(buf, i, 4);
    assert i == this.data.length;
    return buf.substring(0, buf.length() - 1);
  }
  
  private int appendHexDumpField(StringBuilder buf, int i, int length) {
    buf.append(ByteBufUtil.hexDump(this.data, i, length));
    buf.append('-');
    i += length;
    return i;
  }
  
  public int hashCode() {
    return this.hashCode;
  }
  
  public int compareTo(ChannelId o) {
    if (this == o)
      return 0; 
    if (o instanceof DefaultChannelId) {
      byte[] otherData = ((DefaultChannelId)o).data;
      int len1 = this.data.length;
      int len2 = otherData.length;
      int len = Math.min(len1, len2);
      for (int k = 0; k < len; k++) {
        byte x = this.data[k];
        byte y = otherData[k];
        if (x != y)
          return (x & 0xFF) - (y & 0xFF); 
      } 
      return len1 - len2;
    } 
    return asLongText().compareTo(o.asLongText());
  }
  
  public boolean equals(Object obj) {
    if (this == obj)
      return true; 
    if (!(obj instanceof DefaultChannelId))
      return false; 
    DefaultChannelId other = (DefaultChannelId)obj;
    return (this.hashCode == other.hashCode && Arrays.equals(this.data, other.data));
  }
  
  public String toString() {
    return asShortText();
  }
}
