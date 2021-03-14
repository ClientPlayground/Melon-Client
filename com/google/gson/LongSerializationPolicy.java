package com.google.gson;

public enum LongSerializationPolicy {
  DEFAULT {
    public JsonElement serialize(Long value) {
      return new JsonPrimitive(value);
    }
  },
  STRING {
    public JsonElement serialize(Long value) {
      return new JsonPrimitive(String.valueOf(value));
    }
  };
  
  public abstract JsonElement serialize(Long paramLong);
}
