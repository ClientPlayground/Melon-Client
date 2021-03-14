package com.google.gson.internal.bind;

import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.internal.Excluder;
import com.google.gson.internal.ObjectConstructor;
import com.google.gson.internal.Primitives;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ReflectiveTypeAdapterFactory implements TypeAdapterFactory {
  private final ConstructorConstructor constructorConstructor;
  
  private final FieldNamingStrategy fieldNamingPolicy;
  
  private final Excluder excluder;
  
  public ReflectiveTypeAdapterFactory(ConstructorConstructor constructorConstructor, FieldNamingStrategy fieldNamingPolicy, Excluder excluder) {
    this.constructorConstructor = constructorConstructor;
    this.fieldNamingPolicy = fieldNamingPolicy;
    this.excluder = excluder;
  }
  
  public boolean excludeField(Field f, boolean serialize) {
    return excludeField(f, serialize, this.excluder);
  }
  
  static boolean excludeField(Field f, boolean serialize, Excluder excluder) {
    return (!excluder.excludeClass(f.getType(), serialize) && !excluder.excludeField(f, serialize));
  }
  
  private String getFieldName(Field f) {
    return getFieldName(this.fieldNamingPolicy, f);
  }
  
  static String getFieldName(FieldNamingStrategy fieldNamingPolicy, Field f) {
    SerializedName serializedName = f.<SerializedName>getAnnotation(SerializedName.class);
    return (serializedName == null) ? fieldNamingPolicy.translateName(f) : serializedName.value();
  }
  
  public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
    Class<? super T> raw = type.getRawType();
    if (!Object.class.isAssignableFrom(raw))
      return null; 
    ObjectConstructor<T> constructor = this.constructorConstructor.get(type);
    return new Adapter<T>(constructor, getBoundFields(gson, type, raw));
  }
  
  private BoundField createBoundField(final Gson context, final Field field, String name, final TypeToken<?> fieldType, boolean serialize, boolean deserialize) {
    final boolean isPrimitive = Primitives.isPrimitive(fieldType.getRawType());
    return new BoundField(name, serialize, deserialize) {
        final TypeAdapter<?> typeAdapter = ReflectiveTypeAdapterFactory.this.getFieldAdapter(context, field, fieldType);
        
        void write(JsonWriter writer, Object value) throws IOException, IllegalAccessException {
          Object fieldValue = field.get(value);
          TypeAdapter t = new TypeAdapterRuntimeTypeWrapper(context, this.typeAdapter, fieldType.getType());
          t.write(writer, fieldValue);
        }
        
        void read(JsonReader reader, Object value) throws IOException, IllegalAccessException {
          Object fieldValue = this.typeAdapter.read(reader);
          if (fieldValue != null || !isPrimitive)
            field.set(value, fieldValue); 
        }
        
        public boolean writeField(Object value) throws IOException, IllegalAccessException {
          if (!this.serialized)
            return false; 
          Object fieldValue = field.get(value);
          return (fieldValue != value);
        }
      };
  }
  
  private TypeAdapter<?> getFieldAdapter(Gson gson, Field field, TypeToken<?> fieldType) {
    JsonAdapter annotation = field.<JsonAdapter>getAnnotation(JsonAdapter.class);
    if (annotation != null) {
      TypeAdapter<?> adapter = JsonAdapterAnnotationTypeAdapterFactory.getTypeAdapter(this.constructorConstructor, gson, fieldType, annotation);
      if (adapter != null)
        return adapter; 
    } 
    return gson.getAdapter(fieldType);
  }
  
  private Map<String, BoundField> getBoundFields(Gson context, TypeToken<?> type, Class<?> raw) {
    Map<String, BoundField> result = new LinkedHashMap<String, BoundField>();
    if (raw.isInterface())
      return result; 
    Type declaredType = type.getType();
    while (raw != Object.class) {
      Field[] fields = raw.getDeclaredFields();
      for (Field field : fields) {
        boolean serialize = excludeField(field, true);
        boolean deserialize = excludeField(field, false);
        if (serialize || deserialize) {
          field.setAccessible(true);
          Type fieldType = .Gson.Types.resolve(type.getType(), raw, field.getGenericType());
          BoundField boundField = createBoundField(context, field, getFieldName(field), TypeToken.get(fieldType), serialize, deserialize);
          BoundField previous = result.put(boundField.name, boundField);
          if (previous != null)
            throw new IllegalArgumentException(declaredType + " declares multiple JSON fields named " + previous.name); 
        } 
      } 
      type = TypeToken.get(.Gson.Types.resolve(type.getType(), raw, raw.getGenericSuperclass()));
      raw = type.getRawType();
    } 
    return result;
  }
  
  static abstract class BoundField {
    final String name;
    
    final boolean serialized;
    
    final boolean deserialized;
    
    protected BoundField(String name, boolean serialized, boolean deserialized) {
      this.name = name;
      this.serialized = serialized;
      this.deserialized = deserialized;
    }
    
    abstract boolean writeField(Object param1Object) throws IOException, IllegalAccessException;
    
    abstract void write(JsonWriter param1JsonWriter, Object param1Object) throws IOException, IllegalAccessException;
    
    abstract void read(JsonReader param1JsonReader, Object param1Object) throws IOException, IllegalAccessException;
  }
  
  public static final class Adapter<T> extends TypeAdapter<T> {
    private final ObjectConstructor<T> constructor;
    
    private final Map<String, ReflectiveTypeAdapterFactory.BoundField> boundFields;
    
    private Adapter(ObjectConstructor<T> constructor, Map<String, ReflectiveTypeAdapterFactory.BoundField> boundFields) {
      this.constructor = constructor;
      this.boundFields = boundFields;
    }
    
    public T read(JsonReader in) throws IOException {
      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
      } 
      T instance = (T)this.constructor.construct();
      try {
        in.beginObject();
        while (in.hasNext()) {
          String name = in.nextName();
          ReflectiveTypeAdapterFactory.BoundField field = this.boundFields.get(name);
          if (field == null || !field.deserialized) {
            in.skipValue();
            continue;
          } 
          field.read(in, instance);
        } 
      } catch (IllegalStateException e) {
        throw new JsonSyntaxException(e);
      } catch (IllegalAccessException e) {
        throw new AssertionError(e);
      } 
      in.endObject();
      return instance;
    }
    
    public void write(JsonWriter out, T value) throws IOException {
      if (value == null) {
        out.nullValue();
        return;
      } 
      out.beginObject();
      try {
        for (ReflectiveTypeAdapterFactory.BoundField boundField : this.boundFields.values()) {
          if (boundField.writeField(value)) {
            out.name(boundField.name);
            boundField.write(out, value);
          } 
        } 
      } catch (IllegalAccessException e) {
        throw new AssertionError();
      } 
      out.endObject();
    }
  }
}
