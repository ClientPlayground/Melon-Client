package com.sun.jna;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.Buffer;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Structure {
  private static final Logger LOG = Logger.getLogger(Structure.class.getName());
  
  public static final int ALIGN_DEFAULT = 0;
  
  public static final int ALIGN_NONE = 1;
  
  public static final int ALIGN_GNUC = 2;
  
  public static final int ALIGN_MSVC = 3;
  
  protected static final int CALCULATE_SIZE = -1;
  
  static final Map<Class<?>, LayoutInfo> layoutInfo = new WeakHashMap<Class<?>, LayoutInfo>();
  
  static final Map<Class<?>, List<String>> fieldOrder = new WeakHashMap<Class<?>, List<String>>();
  
  private Pointer memory;
  
  private int size = -1;
  
  private int alignType;
  
  private String encoding;
  
  private int actualAlignType;
  
  private int structAlignment;
  
  private Map<String, StructField> structFields;
  
  private final Map<String, Object> nativeStrings = new HashMap<String, Object>();
  
  private TypeMapper typeMapper;
  
  private long typeInfo;
  
  private boolean autoRead = true;
  
  private boolean autoWrite = true;
  
  private Structure[] array;
  
  private boolean readCalled;
  
  protected Structure() {
    this(0);
  }
  
  protected Structure(TypeMapper mapper) {
    this(null, 0, mapper);
  }
  
  protected Structure(int alignType) {
    this((Pointer)null, alignType);
  }
  
  protected Structure(int alignType, TypeMapper mapper) {
    this(null, alignType, mapper);
  }
  
  protected Structure(Pointer p) {
    this(p, 0);
  }
  
  protected Structure(Pointer p, int alignType) {
    this(p, alignType, null);
  }
  
  protected Structure(Pointer p, int alignType, TypeMapper mapper) {
    setAlignType(alignType);
    setStringEncoding(Native.getStringEncoding(getClass()));
    initializeTypeMapper(mapper);
    validateFields();
    if (p != null) {
      useMemory(p, 0, true);
    } else {
      allocateMemory(-1);
    } 
    initializeFields();
  }
  
  Map<String, StructField> fields() {
    return this.structFields;
  }
  
  TypeMapper getTypeMapper() {
    return this.typeMapper;
  }
  
  private void initializeTypeMapper(TypeMapper mapper) {
    if (mapper == null)
      mapper = Native.getTypeMapper(getClass()); 
    this.typeMapper = mapper;
    layoutChanged();
  }
  
  private void layoutChanged() {
    if (this.size != -1) {
      this.size = -1;
      if (this.memory instanceof AutoAllocated)
        this.memory = null; 
      ensureAllocated();
    } 
  }
  
  protected void setStringEncoding(String encoding) {
    this.encoding = encoding;
  }
  
  protected String getStringEncoding() {
    return this.encoding;
  }
  
  protected void setAlignType(int alignType) {
    this.alignType = alignType;
    if (alignType == 0) {
      alignType = Native.getStructureAlignment(getClass());
      if (alignType == 0)
        if (Platform.isWindows()) {
          alignType = 3;
        } else {
          alignType = 2;
        }  
    } 
    this.actualAlignType = alignType;
    layoutChanged();
  }
  
  protected Memory autoAllocate(int size) {
    return new AutoAllocated(size);
  }
  
  protected void useMemory(Pointer m) {
    useMemory(m, 0);
  }
  
  protected void useMemory(Pointer m, int offset) {
    useMemory(m, offset, false);
  }
  
  void useMemory(Pointer m, int offset, boolean force) {
    try {
      this.nativeStrings.clear();
      if (this instanceof ByValue && !force) {
        byte[] buf = new byte[size()];
        m.read(0L, buf, 0, buf.length);
        this.memory.write(0L, buf, 0, buf.length);
      } else {
        this.memory = m.share(offset);
        if (this.size == -1)
          this.size = calculateSize(false); 
        if (this.size != -1)
          this.memory = m.share(offset, this.size); 
      } 
      this.array = null;
      this.readCalled = false;
    } catch (IndexOutOfBoundsException e) {
      throw new IllegalArgumentException("Structure exceeds provided memory bounds", e);
    } 
  }
  
  protected void ensureAllocated() {
    ensureAllocated(false);
  }
  
  private void ensureAllocated(boolean avoidFFIType) {
    if (this.memory == null) {
      allocateMemory(avoidFFIType);
    } else if (this.size == -1) {
      this.size = calculateSize(true, avoidFFIType);
      if (!(this.memory instanceof AutoAllocated))
        try {
          this.memory = this.memory.share(0L, this.size);
        } catch (IndexOutOfBoundsException e) {
          throw new IllegalArgumentException("Structure exceeds provided memory bounds", e);
        }  
    } 
  }
  
  protected void allocateMemory() {
    allocateMemory(false);
  }
  
  private void allocateMemory(boolean avoidFFIType) {
    allocateMemory(calculateSize(true, avoidFFIType));
  }
  
  protected void allocateMemory(int size) {
    if (size == -1) {
      size = calculateSize(false);
    } else if (size <= 0) {
      throw new IllegalArgumentException("Structure size must be greater than zero: " + size);
    } 
    if (size != -1) {
      if (this.memory == null || this.memory instanceof AutoAllocated)
        this.memory = autoAllocate(size); 
      this.size = size;
    } 
  }
  
  public int size() {
    ensureAllocated();
    return this.size;
  }
  
  public void clear() {
    ensureAllocated();
    this.memory.clear(size());
  }
  
  public Pointer getPointer() {
    ensureAllocated();
    return this.memory;
  }
  
  private static final ThreadLocal<Map<Pointer, Structure>> reads = new ThreadLocal<Map<Pointer, Structure>>() {
      protected synchronized Map<Pointer, Structure> initialValue() {
        return new HashMap<Pointer, Structure>();
      }
    };
  
  private static final ThreadLocal<Set<Structure>> busy = new ThreadLocal<Set<Structure>>() {
      protected synchronized Set<Structure> initialValue() {
        return new Structure.StructureSet();
      }
    };
  
  static class StructureSet extends AbstractCollection<Structure> implements Set<Structure> {
    Structure[] elements;
    
    private int count;
    
    private void ensureCapacity(int size) {
      if (this.elements == null) {
        this.elements = new Structure[size * 3 / 2];
      } else if (this.elements.length < size) {
        Structure[] e = new Structure[size * 3 / 2];
        System.arraycopy(this.elements, 0, e, 0, this.elements.length);
        this.elements = e;
      } 
    }
    
    public Structure[] getElements() {
      return this.elements;
    }
    
    public int size() {
      return this.count;
    }
    
    public boolean contains(Object o) {
      return (indexOf((Structure)o) != -1);
    }
    
    public boolean add(Structure o) {
      if (!contains(o)) {
        ensureCapacity(this.count + 1);
        this.elements[this.count++] = o;
      } 
      return true;
    }
    
    private int indexOf(Structure s1) {
      for (int i = 0; i < this.count; i++) {
        Structure s2 = this.elements[i];
        if (s1 == s2 || (s1
          .getClass() == s2.getClass() && s1
          .size() == s2.size() && s1
          .getPointer().equals(s2.getPointer())))
          return i; 
      } 
      return -1;
    }
    
    public boolean remove(Object o) {
      int idx = indexOf((Structure)o);
      if (idx != -1) {
        if (--this.count >= 0) {
          this.elements[idx] = this.elements[this.count];
          this.elements[this.count] = null;
        } 
        return true;
      } 
      return false;
    }
    
    public Iterator<Structure> iterator() {
      Structure[] e = new Structure[this.count];
      if (this.count > 0)
        System.arraycopy(this.elements, 0, e, 0, this.count); 
      return Arrays.<Structure>asList(e).iterator();
    }
  }
  
  static Set<Structure> busy() {
    return busy.get();
  }
  
  static Map<Pointer, Structure> reading() {
    return reads.get();
  }
  
  void conditionalAutoRead() {
    if (!this.readCalled)
      autoRead(); 
  }
  
  public void read() {
    if (this.memory == PLACEHOLDER_MEMORY)
      return; 
    this.readCalled = true;
    ensureAllocated();
    if (busy().contains(this))
      return; 
    busy().add(this);
    if (this instanceof ByReference)
      reading().put(getPointer(), this); 
    try {
      for (StructField structField : fields().values())
        readField(structField); 
    } finally {
      busy().remove(this);
      if (reading().get(getPointer()) == this)
        reading().remove(getPointer()); 
    } 
  }
  
  protected int fieldOffset(String name) {
    ensureAllocated();
    StructField f = fields().get(name);
    if (f == null)
      throw new IllegalArgumentException("No such field: " + name); 
    return f.offset;
  }
  
  public Object readField(String name) {
    ensureAllocated();
    StructField f = fields().get(name);
    if (f == null)
      throw new IllegalArgumentException("No such field: " + name); 
    return readField(f);
  }
  
  Object getFieldValue(Field field) {
    try {
      return field.get(this);
    } catch (Exception e) {
      throw new Error("Exception reading field '" + field.getName() + "' in " + getClass(), e);
    } 
  }
  
  void setFieldValue(Field field, Object value) {
    setFieldValue(field, value, false);
  }
  
  private void setFieldValue(Field field, Object value, boolean overrideFinal) {
    try {
      field.set(this, value);
    } catch (IllegalAccessException e) {
      int modifiers = field.getModifiers();
      if (Modifier.isFinal(modifiers)) {
        if (overrideFinal)
          throw new UnsupportedOperationException("This VM does not support Structures with final fields (field '" + field.getName() + "' within " + getClass() + ")", e); 
        throw new UnsupportedOperationException("Attempt to write to read-only field '" + field.getName() + "' within " + getClass(), e);
      } 
      throw new Error("Unexpectedly unable to write to field '" + field.getName() + "' within " + getClass(), e);
    } 
  }
  
  static <T extends Structure> T updateStructureByReference(Class<T> type, T s, Pointer address) {
    if (address == null) {
      s = null;
    } else if (s == null || !address.equals(s.getPointer())) {
      Structure s1 = reading().get(address);
      if (s1 != null && type.equals(s1.getClass())) {
        Structure structure = s1;
        structure.autoRead();
      } else {
        s = newInstance(type, address);
        s.conditionalAutoRead();
      } 
    } else {
      s.autoRead();
    } 
    return s;
  }
  
  protected Object readField(StructField structField) {
    Object result;
    int offset = structField.offset;
    Class<?> fieldType = structField.type;
    FromNativeConverter readConverter = structField.readConverter;
    if (readConverter != null)
      fieldType = readConverter.nativeType(); 
    Object currentValue = (Structure.class.isAssignableFrom(fieldType) || Callback.class.isAssignableFrom(fieldType) || (Platform.HAS_BUFFERS && Buffer.class.isAssignableFrom(fieldType)) || Pointer.class.isAssignableFrom(fieldType) || NativeMapped.class.isAssignableFrom(fieldType) || fieldType.isArray()) ? getFieldValue(structField.field) : null;
    if (fieldType == String.class) {
      Pointer p = this.memory.getPointer(offset);
      result = (p == null) ? null : p.getString(0L, this.encoding);
    } else {
      result = this.memory.getValue(offset, fieldType, currentValue);
    } 
    if (readConverter != null) {
      result = readConverter.fromNative(result, structField.context);
      if (currentValue != null && currentValue.equals(result))
        result = currentValue; 
    } 
    if (fieldType.equals(String.class) || fieldType
      .equals(WString.class)) {
      this.nativeStrings.put(structField.name + ".ptr", this.memory.getPointer(offset));
      this.nativeStrings.put(structField.name + ".val", result);
    } 
    setFieldValue(structField.field, result, true);
    return result;
  }
  
  public void write() {
    if (this.memory == PLACEHOLDER_MEMORY)
      return; 
    ensureAllocated();
    if (this instanceof ByValue)
      getTypeInfo(); 
    if (busy().contains(this))
      return; 
    busy().add(this);
    try {
      for (StructField sf : fields().values()) {
        if (!sf.isVolatile)
          writeField(sf); 
      } 
    } finally {
      busy().remove(this);
    } 
  }
  
  public void writeField(String name) {
    ensureAllocated();
    StructField f = fields().get(name);
    if (f == null)
      throw new IllegalArgumentException("No such field: " + name); 
    writeField(f);
  }
  
  public void writeField(String name, Object value) {
    ensureAllocated();
    StructField structField = fields().get(name);
    if (structField == null)
      throw new IllegalArgumentException("No such field: " + name); 
    setFieldValue(structField.field, value);
    writeField(structField);
  }
  
  protected void writeField(StructField structField) {
    if (structField.isReadOnly)
      return; 
    int offset = structField.offset;
    Object value = getFieldValue(structField.field);
    Class<?> fieldType = structField.type;
    ToNativeConverter converter = structField.writeConverter;
    if (converter != null) {
      value = converter.toNative(value, new StructureWriteContext(this, structField.field));
      fieldType = converter.nativeType();
    } 
    if (String.class == fieldType || WString.class == fieldType) {
      boolean wide = (fieldType == WString.class);
      if (value != null) {
        if (this.nativeStrings.containsKey(structField.name + ".ptr") && value
          .equals(this.nativeStrings.get(structField.name + ".val")))
          return; 
        NativeString nativeString = wide ? new NativeString(value.toString(), true) : new NativeString(value.toString(), this.encoding);
        this.nativeStrings.put(structField.name, nativeString);
        value = nativeString.getPointer();
      } else {
        this.nativeStrings.remove(structField.name);
      } 
      this.nativeStrings.remove(structField.name + ".ptr");
      this.nativeStrings.remove(structField.name + ".val");
    } 
    try {
      this.memory.setValue(offset, value, fieldType);
    } catch (IllegalArgumentException e) {
      String msg = "Structure field \"" + structField.name + "\" was declared as " + structField.type + ((structField.type == fieldType) ? "" : (" (native type " + fieldType + ")")) + ", which is not supported within a Structure";
      throw new IllegalArgumentException(msg, e);
    } 
  }
  
  protected List<String> getFieldOrder() {
    List<String> fields = new LinkedList<String>();
    for (Class<?> clazz = getClass(); clazz != Structure.class; clazz = clazz.getSuperclass()) {
      FieldOrder order = clazz.<FieldOrder>getAnnotation(FieldOrder.class);
      if (order != null)
        fields.addAll(0, Arrays.asList(order.value())); 
    } 
    return Collections.unmodifiableList(fields);
  }
  
  protected void sortFields(List<Field> fields, List<String> names) {
    for (int i = 0; i < names.size(); i++) {
      String name = names.get(i);
      for (int f = 0; f < fields.size(); f++) {
        Field field = fields.get(f);
        if (name.equals(field.getName())) {
          Collections.swap(fields, i, f);
          break;
        } 
      } 
    } 
  }
  
  protected List<Field> getFieldList() {
    List<Field> flist = new ArrayList<Field>();
    Class<?> cls = getClass();
    for (; !cls.equals(Structure.class); 
      cls = cls.getSuperclass()) {
      List<Field> classFields = new ArrayList<Field>();
      Field[] fields = cls.getDeclaredFields();
      for (int i = 0; i < fields.length; i++) {
        int modifiers = fields[i].getModifiers();
        if (!Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers))
          classFields.add(fields[i]); 
      } 
      flist.addAll(0, classFields);
    } 
    return flist;
  }
  
  private List<String> fieldOrder() {
    Class<?> clazz = getClass();
    synchronized (fieldOrder) {
      List<String> list = fieldOrder.get(clazz);
      if (list == null) {
        list = getFieldOrder();
        fieldOrder.put(clazz, list);
      } 
      return list;
    } 
  }
  
  public static List<String> createFieldsOrder(List<String> baseFields, String... extraFields) {
    return createFieldsOrder(baseFields, Arrays.asList(extraFields));
  }
  
  public static List<String> createFieldsOrder(List<String> baseFields, List<String> extraFields) {
    List<String> fields = new ArrayList<String>(baseFields.size() + extraFields.size());
    fields.addAll(baseFields);
    fields.addAll(extraFields);
    return Collections.unmodifiableList(fields);
  }
  
  public static List<String> createFieldsOrder(String field) {
    return Collections.unmodifiableList(Collections.singletonList(field));
  }
  
  public static List<String> createFieldsOrder(String... fields) {
    return Collections.unmodifiableList(Arrays.asList(fields));
  }
  
  private static <T extends Comparable<T>> List<T> sort(Collection<? extends T> c) {
    List<T> list = new ArrayList<T>(c);
    Collections.sort(list);
    return list;
  }
  
  protected List<Field> getFields(boolean force) {
    List<Field> flist = getFieldList();
    Set<String> names = new HashSet<String>();
    for (Field f : flist)
      names.add(f.getName()); 
    List<String> fieldOrder = fieldOrder();
    if (fieldOrder.size() != flist.size() && flist.size() > 1) {
      if (force)
        throw new Error("Structure.getFieldOrder() on " + getClass() + (
            (fieldOrder.size() < flist.size()) ? " does not provide enough" : " provides too many") + " names [" + fieldOrder
            
            .size() + "] (" + 
            
            sort(fieldOrder) + ") to match declared fields [" + flist
            .size() + "] (" + 
            
            sort(names) + ")"); 
      return null;
    } 
    Set<String> orderedNames = new HashSet<String>(fieldOrder);
    if (!orderedNames.equals(names))
      throw new Error("Structure.getFieldOrder() on " + getClass() + " returns names (" + 
          
          sort(fieldOrder) + ") which do not match declared field names (" + 
          
          sort(names) + ")"); 
    sortFields(flist, fieldOrder);
    return flist;
  }
  
  protected int calculateSize(boolean force) {
    return calculateSize(force, false);
  }
  
  static int size(Class<? extends Structure> type) {
    return size(type, null);
  }
  
  static <T extends Structure> int size(Class<T> type, T value) {
    LayoutInfo info;
    synchronized (layoutInfo) {
      info = layoutInfo.get(type);
    } 
    int sz = (info != null && !info.variable) ? info.size : -1;
    if (sz == -1) {
      if (value == null)
        value = newInstance(type, PLACEHOLDER_MEMORY); 
      sz = value.size();
    } 
    return sz;
  }
  
  int calculateSize(boolean force, boolean avoidFFIType) {
    LayoutInfo info;
    int size = -1;
    Class<?> clazz = getClass();
    synchronized (layoutInfo) {
      info = layoutInfo.get(clazz);
    } 
    if (info == null || this.alignType != info
      .alignType || this.typeMapper != info
      .typeMapper)
      info = deriveLayout(force, avoidFFIType); 
    if (info != null) {
      this.structAlignment = info.alignment;
      this.structFields = info.fields;
      if (!info.variable)
        synchronized (layoutInfo) {
          if (!layoutInfo.containsKey(clazz) || this.alignType != 0 || this.typeMapper != null)
            layoutInfo.put(clazz, info); 
        }  
      size = info.size;
    } 
    return size;
  }
  
  private static class LayoutInfo {
    private LayoutInfo() {}
    
    private int size = -1;
    
    private int alignment = 1;
    
    private final Map<String, Structure.StructField> fields = Collections.synchronizedMap(new LinkedHashMap<String, Structure.StructField>());
    
    private int alignType = 0;
    
    private TypeMapper typeMapper;
    
    private boolean variable;
  }
  
  private void validateField(String name, Class<?> type) {
    if (this.typeMapper != null) {
      ToNativeConverter toNative = this.typeMapper.getToNativeConverter(type);
      if (toNative != null) {
        validateField(name, toNative.nativeType());
        return;
      } 
    } 
    if (type.isArray()) {
      validateField(name, type.getComponentType());
    } else {
      try {
        getNativeSize(type);
      } catch (IllegalArgumentException e) {
        String msg = "Invalid Structure field in " + getClass() + ", field name '" + name + "' (" + type + "): " + e.getMessage();
        throw new IllegalArgumentException(msg, e);
      } 
    } 
  }
  
  private void validateFields() {
    List<Field> fields = getFieldList();
    for (Field f : fields)
      validateField(f.getName(), f.getType()); 
  }
  
  private LayoutInfo deriveLayout(boolean force, boolean avoidFFIType) {
    int calculatedSize = 0;
    List<Field> fields = getFields(force);
    if (fields == null)
      return null; 
    LayoutInfo info = new LayoutInfo();
    info.alignType = this.alignType;
    info.typeMapper = this.typeMapper;
    boolean firstField = true;
    for (Iterator<Field> i = fields.iterator(); i.hasNext(); firstField = false) {
      Field field = i.next();
      int modifiers = field.getModifiers();
      Class<?> type = field.getType();
      if (type.isArray())
        info.variable = true; 
      StructField structField = new StructField();
      structField.isVolatile = Modifier.isVolatile(modifiers);
      structField.isReadOnly = Modifier.isFinal(modifiers);
      if (structField.isReadOnly) {
        if (!Platform.RO_FIELDS)
          throw new IllegalArgumentException("This VM does not support read-only fields (field '" + field
              .getName() + "' within " + getClass() + ")"); 
        field.setAccessible(true);
      } 
      structField.field = field;
      structField.name = field.getName();
      structField.type = type;
      if (Callback.class.isAssignableFrom(type) && !type.isInterface())
        throw new IllegalArgumentException("Structure Callback field '" + field
            .getName() + "' must be an interface"); 
      if (type.isArray() && Structure.class
        .equals(type.getComponentType())) {
        String msg = "Nested Structure arrays must use a derived Structure type so that the size of the elements can be determined";
        throw new IllegalArgumentException(msg);
      } 
      int fieldAlignment = 1;
      if (Modifier.isPublic(field.getModifiers())) {
        Object value = getFieldValue(structField.field);
        if (value == null && type.isArray()) {
          if (force)
            throw new IllegalStateException("Array fields must be initialized"); 
          return null;
        } 
        Class<?> nativeType = type;
        if (NativeMapped.class.isAssignableFrom(type)) {
          NativeMappedConverter tc = NativeMappedConverter.getInstance(type);
          nativeType = tc.nativeType();
          structField.writeConverter = tc;
          structField.readConverter = tc;
          structField.context = new StructureReadContext(this, field);
        } else if (this.typeMapper != null) {
          ToNativeConverter writeConverter = this.typeMapper.getToNativeConverter(type);
          FromNativeConverter readConverter = this.typeMapper.getFromNativeConverter(type);
          if (writeConverter != null && readConverter != null) {
            value = writeConverter.toNative(value, new StructureWriteContext(this, structField.field));
            nativeType = (value != null) ? value.getClass() : Pointer.class;
            structField.writeConverter = writeConverter;
            structField.readConverter = readConverter;
            structField.context = new StructureReadContext(this, field);
          } else if (writeConverter != null || readConverter != null) {
            String msg = "Structures require bidirectional type conversion for " + type;
            throw new IllegalArgumentException(msg);
          } 
        } 
        if (value == null)
          value = initializeField(structField.field, type); 
        try {
          structField.size = getNativeSize(nativeType, value);
          fieldAlignment = getNativeAlignment(nativeType, value, firstField);
        } catch (IllegalArgumentException e) {
          if (!force && this.typeMapper == null)
            return null; 
          String msg = "Invalid Structure field in " + getClass() + ", field name '" + structField.name + "' (" + structField.type + "): " + e.getMessage();
          throw new IllegalArgumentException(msg, e);
        } 
        if (fieldAlignment == 0)
          throw new Error("Field alignment is zero for field '" + structField.name + "' within " + getClass()); 
        info.alignment = Math.max(info.alignment, fieldAlignment);
        if (calculatedSize % fieldAlignment != 0)
          calculatedSize += fieldAlignment - calculatedSize % fieldAlignment; 
        if (this instanceof Union) {
          structField.offset = 0;
          calculatedSize = Math.max(calculatedSize, structField.size);
        } else {
          structField.offset = calculatedSize;
          calculatedSize += structField.size;
        } 
        info.fields.put(structField.name, structField);
      } 
    } 
    if (calculatedSize > 0) {
      int size = addPadding(calculatedSize, info.alignment);
      if (this instanceof ByValue && !avoidFFIType)
        getTypeInfo(); 
      info.size = size;
      return info;
    } 
    throw new IllegalArgumentException("Structure " + getClass() + " has unknown or zero size (ensure all fields are public)");
  }
  
  private void initializeFields() {
    List<Field> flist = getFieldList();
    for (Field f : flist) {
      try {
        Object o = f.get(this);
        if (o == null)
          initializeField(f, f.getType()); 
      } catch (Exception e) {
        throw new Error("Exception reading field '" + f.getName() + "' in " + getClass(), e);
      } 
    } 
  }
  
  private Object initializeField(Field field, Class<?> type) {
    Object value = null;
    if (Structure.class.isAssignableFrom(type) && 
      !ByReference.class.isAssignableFrom(type)) {
      try {
        value = newInstance(type, PLACEHOLDER_MEMORY);
        setFieldValue(field, value);
      } catch (IllegalArgumentException e) {
        String msg = "Can't determine size of nested structure";
        throw new IllegalArgumentException(msg, e);
      } 
    } else if (NativeMapped.class.isAssignableFrom(type)) {
      NativeMappedConverter tc = NativeMappedConverter.getInstance(type);
      value = tc.defaultValue();
      setFieldValue(field, value);
    } 
    return value;
  }
  
  private int addPadding(int calculatedSize) {
    return addPadding(calculatedSize, this.structAlignment);
  }
  
  private int addPadding(int calculatedSize, int alignment) {
    if (this.actualAlignType != 1 && 
      calculatedSize % alignment != 0)
      calculatedSize += alignment - calculatedSize % alignment; 
    return calculatedSize;
  }
  
  protected int getStructAlignment() {
    if (this.size == -1)
      calculateSize(true); 
    return this.structAlignment;
  }
  
  protected int getNativeAlignment(Class<?> type, Object value, boolean isFirstElement) {
    int alignment = 1;
    if (NativeMapped.class.isAssignableFrom(type)) {
      NativeMappedConverter tc = NativeMappedConverter.getInstance(type);
      type = tc.nativeType();
      value = tc.toNative(value, new ToNativeContext());
    } 
    int size = Native.getNativeSize(type, value);
    if (type.isPrimitive() || Long.class == type || Integer.class == type || Short.class == type || Character.class == type || Byte.class == type || Boolean.class == type || Float.class == type || Double.class == type) {
      alignment = size;
    } else if ((Pointer.class.isAssignableFrom(type) && !Function.class.isAssignableFrom(type)) || (Platform.HAS_BUFFERS && Buffer.class
      .isAssignableFrom(type)) || Callback.class
      .isAssignableFrom(type) || WString.class == type || String.class == type) {
      alignment = Native.POINTER_SIZE;
    } else if (Structure.class.isAssignableFrom(type)) {
      if (ByReference.class.isAssignableFrom(type)) {
        alignment = Native.POINTER_SIZE;
      } else {
        if (value == null)
          value = newInstance(type, PLACEHOLDER_MEMORY); 
        alignment = ((Structure)value).getStructAlignment();
      } 
    } else if (type.isArray()) {
      alignment = getNativeAlignment(type.getComponentType(), null, isFirstElement);
    } else {
      throw new IllegalArgumentException("Type " + type + " has unknown native alignment");
    } 
    if (this.actualAlignType == 1) {
      alignment = 1;
    } else if (this.actualAlignType == 3) {
      alignment = Math.min(8, alignment);
    } else if (this.actualAlignType == 2) {
      if (!isFirstElement || !Platform.isMac() || !Platform.isPPC())
        alignment = Math.min(Native.MAX_ALIGNMENT, alignment); 
      if (!isFirstElement && Platform.isAIX() && (type == double.class || type == Double.class))
        alignment = 4; 
    } 
    return alignment;
  }
  
  public String toString() {
    return toString(Boolean.getBoolean("jna.dump_memory"));
  }
  
  public String toString(boolean debug) {
    return toString(0, true, debug);
  }
  
  private String format(Class<?> type) {
    String s = type.getName();
    int dot = s.lastIndexOf(".");
    return s.substring(dot + 1);
  }
  
  private String toString(int indent, boolean showContents, boolean dumpMemory) {
    ensureAllocated();
    String LS = System.getProperty("line.separator");
    String name = format(getClass()) + "(" + getPointer() + ")";
    if (!(getPointer() instanceof Memory))
      name = name + " (" + size() + " bytes)"; 
    String prefix = "";
    for (int idx = 0; idx < indent; idx++)
      prefix = prefix + "  "; 
    String contents = LS;
    if (!showContents) {
      contents = "...}";
    } else {
      for (Iterator<StructField> i = fields().values().iterator(); i.hasNext(); ) {
        StructField sf = i.next();
        Object value = getFieldValue(sf.field);
        String type = format(sf.type);
        String index = "";
        contents = contents + prefix;
        if (sf.type.isArray() && value != null) {
          type = format(sf.type.getComponentType());
          index = "[" + Array.getLength(value) + "]";
        } 
        contents = contents + String.format("  %s %s%s@0x%X", new Object[] { type, sf.name, index, Integer.valueOf(sf.offset) });
        if (value instanceof Structure)
          value = ((Structure)value).toString(indent + 1, !(value instanceof ByReference), dumpMemory); 
        contents = contents + "=";
        if (value instanceof Long) {
          contents = contents + String.format("0x%08X", new Object[] { value });
        } else if (value instanceof Integer) {
          contents = contents + String.format("0x%04X", new Object[] { value });
        } else if (value instanceof Short) {
          contents = contents + String.format("0x%02X", new Object[] { value });
        } else if (value instanceof Byte) {
          contents = contents + String.format("0x%01X", new Object[] { value });
        } else {
          contents = contents + String.valueOf(value).trim();
        } 
        contents = contents + LS;
        if (!i.hasNext())
          contents = contents + prefix + "}"; 
      } 
    } 
    if (indent == 0 && dumpMemory) {
      int BYTES_PER_ROW = 4;
      contents = contents + LS + "memory dump" + LS;
      byte[] buf = getPointer().getByteArray(0L, size());
      for (int i = 0; i < buf.length; i++) {
        if (i % 4 == 0)
          contents = contents + "["; 
        if (buf[i] >= 0 && buf[i] < 16)
          contents = contents + "0"; 
        contents = contents + Integer.toHexString(buf[i] & 0xFF);
        if (i % 4 == 3 && i < buf.length - 1)
          contents = contents + "]" + LS; 
      } 
      contents = contents + "]";
    } 
    return name + " {" + contents;
  }
  
  public Structure[] toArray(Structure[] array) {
    ensureAllocated();
    if (this.memory instanceof AutoAllocated) {
      Memory m = (Memory)this.memory;
      int requiredSize = array.length * size();
      if (m.size() < requiredSize)
        useMemory(autoAllocate(requiredSize)); 
    } 
    array[0] = this;
    int size = size();
    for (int i = 1; i < array.length; i++) {
      array[i] = newInstance(getClass(), this.memory.share((i * size), size));
      array[i].conditionalAutoRead();
    } 
    if (!(this instanceof ByValue))
      this.array = array; 
    return array;
  }
  
  public Structure[] toArray(int size) {
    return toArray((Structure[])Array.newInstance(getClass(), size));
  }
  
  private Class<?> baseClass() {
    if ((this instanceof ByReference || this instanceof ByValue) && Structure.class
      
      .isAssignableFrom(getClass().getSuperclass()))
      return getClass().getSuperclass(); 
    return getClass();
  }
  
  public boolean dataEquals(Structure s) {
    return dataEquals(s, false);
  }
  
  public boolean dataEquals(Structure s, boolean clear) {
    if (clear) {
      s.getPointer().clear(s.size());
      s.write();
      getPointer().clear(size());
      write();
    } 
    byte[] data = s.getPointer().getByteArray(0L, s.size());
    byte[] ref = getPointer().getByteArray(0L, size());
    if (data.length == ref.length) {
      for (int i = 0; i < data.length; i++) {
        if (data[i] != ref[i])
          return false; 
      } 
      return true;
    } 
    return false;
  }
  
  public boolean equals(Object o) {
    return (o instanceof Structure && o
      .getClass() == getClass() && ((Structure)o)
      .getPointer().equals(getPointer()));
  }
  
  public int hashCode() {
    Pointer p = getPointer();
    if (p != null)
      return getPointer().hashCode(); 
    return getClass().hashCode();
  }
  
  protected void cacheTypeInfo(Pointer p) {
    this.typeInfo = p.peer;
  }
  
  FFIType getFieldTypeInfo(StructField f) {
    Class<?> type = f.type;
    Object value = getFieldValue(f.field);
    if (this.typeMapper != null) {
      ToNativeConverter nc = this.typeMapper.getToNativeConverter(type);
      if (nc != null) {
        type = nc.nativeType();
        value = nc.toNative(value, new ToNativeContext());
      } 
    } 
    return FFIType.get(value, type);
  }
  
  Pointer getTypeInfo() {
    Pointer p = getTypeInfo(this).getPointer();
    cacheTypeInfo(p);
    return p;
  }
  
  public void setAutoSynch(boolean auto) {
    setAutoRead(auto);
    setAutoWrite(auto);
  }
  
  public void setAutoRead(boolean auto) {
    this.autoRead = auto;
  }
  
  public boolean getAutoRead() {
    return this.autoRead;
  }
  
  public void setAutoWrite(boolean auto) {
    this.autoWrite = auto;
  }
  
  public boolean getAutoWrite() {
    return this.autoWrite;
  }
  
  static FFIType getTypeInfo(Object obj) {
    return FFIType.get(obj);
  }
  
  private static <T extends Structure> T newInstance(Class<T> type, long init) {
    try {
      T s = newInstance(type, (init == 0L) ? PLACEHOLDER_MEMORY : new Pointer(init));
      if (init != 0L)
        s.conditionalAutoRead(); 
      return s;
    } catch (Throwable e) {
      LOG.log(Level.WARNING, "JNA: Error creating structure", e);
      return null;
    } 
  }
  
  public static <T extends Structure> T newInstance(Class<T> type, Pointer init) throws IllegalArgumentException {
    try {
      Constructor<T> ctor = getPointerConstructor(type);
      if (ctor != null)
        return ctor.newInstance(new Object[] { init }); 
    } catch (SecurityException securityException) {
    
    } catch (InstantiationException e) {
      String msg = "Can't instantiate " + type;
      throw new IllegalArgumentException(msg, e);
    } catch (IllegalAccessException e) {
      String msg = "Instantiation of " + type + " (Pointer) not allowed, is it public?";
      throw new IllegalArgumentException(msg, e);
    } catch (InvocationTargetException e) {
      String msg = "Exception thrown while instantiating an instance of " + type;
      throw new IllegalArgumentException(msg, e);
    } 
    T s = newInstance(type);
    if (init != PLACEHOLDER_MEMORY)
      s.useMemory(init); 
    return s;
  }
  
  public static <T extends Structure> T newInstance(Class<T> type) throws IllegalArgumentException {
    Structure structure = Klass.<Structure>newInstance(type);
    if (structure instanceof ByValue)
      structure.allocateMemory(); 
    return (T)structure;
  }
  
  private static <T> Constructor<T> getPointerConstructor(Class<T> type) {
    for (Constructor<T> constructor : type.getConstructors()) {
      Class[] parameterTypes = constructor.getParameterTypes();
      if (parameterTypes.length == 1 && parameterTypes[0].equals(Pointer.class))
        return constructor; 
    } 
    return null;
  }
  
  protected static class StructField {
    public String name;
    
    public Class<?> type;
    
    public Field field;
    
    public int size = -1;
    
    public int offset = -1;
    
    public boolean isVolatile;
    
    public boolean isReadOnly;
    
    public FromNativeConverter readConverter;
    
    public ToNativeConverter writeConverter;
    
    public FromNativeContext context;
    
    public String toString() {
      return this.name + "@" + this.offset + "[" + this.size + "] (" + this.type + ")";
    }
  }
  
  @FieldOrder({"size", "alignment", "type", "elements"})
  static class FFIType extends Structure {
    public static class size_t extends IntegerType {
      private static final long serialVersionUID = 1L;
      
      public size_t() {
        this(0L);
      }
      
      public size_t(long value) {
        super(Native.SIZE_T_SIZE, value);
      }
    }
    
    private static final Map<Class, FFIType> typeInfoMap = (Map)new WeakHashMap<Class<?>, FFIType>();
    
    private static final Map<Class, FFIType> unionHelper = (Map)new WeakHashMap<Class<?>, FFIType>();
    
    private static final Map<Pointer, FFIType> ffiTypeInfo = new HashMap<Pointer, FFIType>();
    
    private static final int FFI_TYPE_STRUCT = 13;
    
    public size_t size;
    
    public short alignment;
    
    public short type;
    
    public Pointer elements;
    
    private static class FFITypes {
      private static Pointer ffi_type_void;
      
      private static Pointer ffi_type_float;
      
      private static Pointer ffi_type_double;
      
      private static Pointer ffi_type_longdouble;
      
      private static Pointer ffi_type_uint8;
      
      private static Pointer ffi_type_sint8;
      
      private static Pointer ffi_type_uint16;
      
      private static Pointer ffi_type_sint16;
      
      private static Pointer ffi_type_uint32;
      
      private static Pointer ffi_type_sint32;
      
      private static Pointer ffi_type_uint64;
      
      private static Pointer ffi_type_sint64;
      
      private static Pointer ffi_type_pointer;
    }
    
    private static boolean isIntegerType(FFIType type) {
      Pointer typePointer = type.getPointer();
      return (typePointer.equals(FFITypes.ffi_type_uint8) || typePointer
        .equals(FFITypes.ffi_type_sint8) || typePointer
        .equals(FFITypes.ffi_type_uint16) || typePointer
        .equals(FFITypes.ffi_type_sint16) || typePointer
        .equals(FFITypes.ffi_type_uint32) || typePointer
        .equals(FFITypes.ffi_type_sint32) || typePointer
        .equals(FFITypes.ffi_type_uint64) || typePointer
        .equals(FFITypes.ffi_type_sint64) || typePointer
        .equals(FFITypes.ffi_type_pointer));
    }
    
    private static boolean isFloatType(FFIType type) {
      Pointer typePointer = type.getPointer();
      return (typePointer.equals(FFITypes.ffi_type_float) || typePointer
        .equals(FFITypes.ffi_type_double));
    }
    
    static {
      if (Native.POINTER_SIZE == 0)
        throw new Error("Native library not initialized"); 
      if (FFITypes.ffi_type_void == null)
        throw new Error("FFI types not initialized"); 
      ffiTypeInfo.put(FFITypes.ffi_type_void, Structure.newInstance(FFIType.class, FFITypes.ffi_type_void));
      ffiTypeInfo.put(FFITypes.ffi_type_float, Structure.newInstance(FFIType.class, FFITypes.ffi_type_float));
      ffiTypeInfo.put(FFITypes.ffi_type_double, Structure.newInstance(FFIType.class, FFITypes.ffi_type_double));
      ffiTypeInfo.put(FFITypes.ffi_type_longdouble, Structure.newInstance(FFIType.class, FFITypes.ffi_type_longdouble));
      ffiTypeInfo.put(FFITypes.ffi_type_uint8, Structure.newInstance(FFIType.class, FFITypes.ffi_type_uint8));
      ffiTypeInfo.put(FFITypes.ffi_type_sint8, Structure.newInstance(FFIType.class, FFITypes.ffi_type_sint8));
      ffiTypeInfo.put(FFITypes.ffi_type_uint16, Structure.newInstance(FFIType.class, FFITypes.ffi_type_uint16));
      ffiTypeInfo.put(FFITypes.ffi_type_sint16, Structure.newInstance(FFIType.class, FFITypes.ffi_type_sint16));
      ffiTypeInfo.put(FFITypes.ffi_type_uint32, Structure.newInstance(FFIType.class, FFITypes.ffi_type_uint32));
      ffiTypeInfo.put(FFITypes.ffi_type_sint32, Structure.newInstance(FFIType.class, FFITypes.ffi_type_sint32));
      ffiTypeInfo.put(FFITypes.ffi_type_uint64, Structure.newInstance(FFIType.class, FFITypes.ffi_type_uint64));
      ffiTypeInfo.put(FFITypes.ffi_type_sint64, Structure.newInstance(FFIType.class, FFITypes.ffi_type_sint64));
      ffiTypeInfo.put(FFITypes.ffi_type_pointer, Structure.newInstance(FFIType.class, FFITypes.ffi_type_pointer));
      for (FFIType f : ffiTypeInfo.values())
        f.read(); 
      typeInfoMap.put(void.class, ffiTypeInfo.get(FFITypes.ffi_type_void));
      typeInfoMap.put(Void.class, ffiTypeInfo.get(FFITypes.ffi_type_void));
      typeInfoMap.put(float.class, ffiTypeInfo.get(FFITypes.ffi_type_float));
      typeInfoMap.put(Float.class, ffiTypeInfo.get(FFITypes.ffi_type_float));
      typeInfoMap.put(double.class, ffiTypeInfo.get(FFITypes.ffi_type_double));
      typeInfoMap.put(Double.class, ffiTypeInfo.get(FFITypes.ffi_type_double));
      typeInfoMap.put(long.class, ffiTypeInfo.get(FFITypes.ffi_type_sint64));
      typeInfoMap.put(Long.class, ffiTypeInfo.get(FFITypes.ffi_type_sint64));
      typeInfoMap.put(int.class, ffiTypeInfo.get(FFITypes.ffi_type_sint32));
      typeInfoMap.put(Integer.class, ffiTypeInfo.get(FFITypes.ffi_type_sint32));
      typeInfoMap.put(short.class, ffiTypeInfo.get(FFITypes.ffi_type_sint16));
      typeInfoMap.put(Short.class, ffiTypeInfo.get(FFITypes.ffi_type_sint16));
      FFIType ctype = (Native.WCHAR_SIZE == 2) ? ffiTypeInfo.get(FFITypes.ffi_type_uint16) : ffiTypeInfo.get(FFITypes.ffi_type_uint32);
      typeInfoMap.put(char.class, ctype);
      typeInfoMap.put(Character.class, ctype);
      typeInfoMap.put(byte.class, ffiTypeInfo.get(FFITypes.ffi_type_sint8));
      typeInfoMap.put(Byte.class, ffiTypeInfo.get(FFITypes.ffi_type_sint8));
      typeInfoMap.put(Pointer.class, ffiTypeInfo.get(FFITypes.ffi_type_pointer));
      typeInfoMap.put(String.class, ffiTypeInfo.get(FFITypes.ffi_type_pointer));
      typeInfoMap.put(WString.class, ffiTypeInfo.get(FFITypes.ffi_type_pointer));
      typeInfoMap.put(boolean.class, ffiTypeInfo.get(FFITypes.ffi_type_uint32));
      typeInfoMap.put(Boolean.class, ffiTypeInfo.get(FFITypes.ffi_type_uint32));
    }
    
    public FFIType(FFIType reference) {
      this.type = 13;
      this.size = reference.size;
      this.alignment = reference.alignment;
      this.type = reference.type;
      this.elements = reference.elements;
    }
    
    public FFIType() {
      this.type = 13;
    }
    
    public FFIType(Structure ref) {
      Pointer[] els;
      this.type = 13;
      ref.ensureAllocated(true);
      if (ref instanceof Union) {
        FFIType unionType = null;
        int size = 0;
        boolean hasInteger = false;
        for (Structure.StructField sf : ref.fields().values()) {
          FFIType type = ref.getFieldTypeInfo(sf);
          if (isIntegerType(type))
            hasInteger = true; 
          if (unionType == null || size < sf.size || (size == sf.size && Structure.class
            
            .isAssignableFrom(sf.type))) {
            unionType = type;
            size = sf.size;
          } 
        } 
        if (!Platform.isWindows() && ((
          Platform.isIntel() && Platform.is64Bit()) || 
          Platform.isARM()))
          if (hasInteger && isFloatType(unionType)) {
            unionType = new FFIType(unionType);
            if (unionType.size.intValue() == 4) {
              unionType.type = ((FFIType)ffiTypeInfo.get(FFITypes.ffi_type_uint32)).type;
            } else if (unionType.size.intValue() == 8) {
              unionType.type = ((FFIType)ffiTypeInfo.get(FFITypes.ffi_type_uint64)).type;
            } 
            unionType.write();
          }  
        els = new Pointer[] { unionType.getPointer(), null };
        unionHelper.put(ref.getClass(), unionType);
      } else {
        els = new Pointer[ref.fields().size() + 1];
        int idx = 0;
        for (Structure.StructField sf : ref.fields().values())
          els[idx++] = ref.getFieldTypeInfo(sf).getPointer(); 
      } 
      init(els);
      write();
    }
    
    public FFIType(Object array, Class<?> type) {
      this.type = 13;
      int length = Array.getLength(array);
      Pointer[] els = new Pointer[length + 1];
      Pointer p = get((Object)null, type.getComponentType()).getPointer();
      for (int i = 0; i < length; i++)
        els[i] = p; 
      init(els);
      write();
    }
    
    private void init(Pointer[] els) {
      this.elements = new Memory((Native.POINTER_SIZE * els.length));
      this.elements.write(0L, els, 0, els.length);
      write();
    }
    
    static FFIType get(Object obj) {
      if (obj == null)
        return typeInfoMap.get(Pointer.class); 
      if (obj instanceof Class)
        return get((Object)null, (Class)obj); 
      return get(obj, obj.getClass());
    }
    
    private static FFIType get(Object obj, Class<?> cls) {
      TypeMapper mapper = Native.getTypeMapper(cls);
      if (mapper != null) {
        ToNativeConverter nc = mapper.getToNativeConverter(cls);
        if (nc != null)
          cls = nc.nativeType(); 
      } 
      synchronized (typeInfoMap) {
        FFIType o = typeInfoMap.get(cls);
        if (o != null)
          return o; 
        if ((Platform.HAS_BUFFERS && Buffer.class.isAssignableFrom(cls)) || Callback.class
          .isAssignableFrom(cls)) {
          typeInfoMap.put(cls, typeInfoMap.get(Pointer.class));
          return typeInfoMap.get(Pointer.class);
        } 
        if (Structure.class.isAssignableFrom(cls)) {
          if (obj == null)
            obj = newInstance(cls, Structure.PLACEHOLDER_MEMORY); 
          if (Structure.ByReference.class.isAssignableFrom(cls)) {
            typeInfoMap.put(cls, typeInfoMap.get(Pointer.class));
            return typeInfoMap.get(Pointer.class);
          } 
          FFIType type = new FFIType((Structure)obj);
          typeInfoMap.put(cls, type);
          return type;
        } 
        if (NativeMapped.class.isAssignableFrom(cls)) {
          NativeMappedConverter c = NativeMappedConverter.getInstance(cls);
          return get(c.toNative(obj, new ToNativeContext()), c.nativeType());
        } 
        if (cls.isArray()) {
          FFIType type = new FFIType(obj, cls);
          typeInfoMap.put(cls, type);
          return type;
        } 
        throw new IllegalArgumentException("Unsupported type " + cls);
      } 
    }
  }
  
  private static class AutoAllocated extends Memory {
    public AutoAllocated(int size) {
      super(size);
      clear();
    }
    
    public String toString() {
      return "auto-" + super.toString();
    }
  }
  
  private static void structureArrayCheck(Structure[] ss) {
    if (ByReference[].class.isAssignableFrom(ss.getClass()))
      return; 
    Pointer base = ss[0].getPointer();
    int size = ss[0].size();
    for (int si = 1; si < ss.length; si++) {
      if ((ss[si].getPointer()).peer != base.peer + (size * si)) {
        String msg = "Structure array elements must use contiguous memory (bad backing address at Structure array index " + si + ")";
        throw new IllegalArgumentException(msg);
      } 
    } 
  }
  
  public static void autoRead(Structure[] ss) {
    structureArrayCheck(ss);
    if ((ss[0]).array == ss) {
      ss[0].autoRead();
    } else {
      for (int si = 0; si < ss.length; si++) {
        if (ss[si] != null)
          ss[si].autoRead(); 
      } 
    } 
  }
  
  public void autoRead() {
    if (getAutoRead()) {
      read();
      if (this.array != null)
        for (int i = 1; i < this.array.length; i++)
          this.array[i].autoRead();  
    } 
  }
  
  public static void autoWrite(Structure[] ss) {
    structureArrayCheck(ss);
    if ((ss[0]).array == ss) {
      ss[0].autoWrite();
    } else {
      for (int si = 0; si < ss.length; si++) {
        if (ss[si] != null)
          ss[si].autoWrite(); 
      } 
    } 
  }
  
  public void autoWrite() {
    if (getAutoWrite()) {
      write();
      if (this.array != null)
        for (int i = 1; i < this.array.length; i++)
          this.array[i].autoWrite();  
    } 
  }
  
  protected int getNativeSize(Class<?> nativeType) {
    return getNativeSize(nativeType, null);
  }
  
  protected int getNativeSize(Class<?> nativeType, Object value) {
    return Native.getNativeSize(nativeType, value);
  }
  
  private static final Pointer PLACEHOLDER_MEMORY = new Pointer(0L) {
      public Pointer share(long offset, long sz) {
        return this;
      }
    };
  
  static void validate(Class<? extends Structure> cls) {
    try {
      cls.getConstructor(new Class[0]);
      return;
    } catch (NoSuchMethodException noSuchMethodException) {
    
    } catch (SecurityException securityException) {}
    throw new IllegalArgumentException("No suitable constructor found for class: " + cls.getName());
  }
  
  @Documented
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.TYPE})
  public static @interface FieldOrder {
    String[] value();
  }
  
  public static interface ByReference {}
  
  public static interface ByValue {}
}
