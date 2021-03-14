package com.replaymod.replaystudio.us.myles.ViaVersion.exception;

import java.util.HashMap;
import java.util.Map;

public class InformativeException extends Exception {
  final Map<String, Object> info = new HashMap<>();
  
  int sources = 0;
  
  public InformativeException(Throwable cause) {
    super(cause);
  }
  
  public InformativeException set(String key, Object value) {
    this.info.put(key, value);
    return this;
  }
  
  public InformativeException addSource(Class<?> sourceClazz) {
    return set("Source #" + this.sources++, getSource(sourceClazz));
  }
  
  private String getSource(Class<?> sourceClazz) {
    if (sourceClazz.isAnonymousClass())
      return sourceClazz.getName() + " (Anonymous)"; 
    return sourceClazz.getName();
  }
  
  public String getMessage() {
    StringBuilder builder = new StringBuilder();
    builder.append("Please post this error to http://github.com/ViaVersion/ViaVersion/issues\n{");
    int i = 0;
    for (Map.Entry<String, Object> entry : this.info.entrySet()) {
      builder.append(((i == 0) ? "" : ", ") + (String)entry.getKey() + ": " + entry.getValue().toString());
      i++;
    } 
    builder.append("}\nActual Error: ");
    return builder.toString();
  }
  
  public synchronized Throwable fillInStackTrace() {
    return this;
  }
}
