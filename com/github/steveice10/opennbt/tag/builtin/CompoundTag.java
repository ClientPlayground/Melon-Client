package com.github.steveice10.opennbt.tag.builtin;

import com.github.steveice10.opennbt.NBTIO;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CompoundTag extends Tag implements Iterable<Tag> {
  private Map<String, Tag> value;
  
  public CompoundTag(String name) {
    this(name, new LinkedHashMap<>());
  }
  
  public CompoundTag(String name, Map<String, Tag> value) {
    super(name);
    this.value = new LinkedHashMap<>(value);
  }
  
  public Map<String, Tag> getValue() {
    return new LinkedHashMap<>(this.value);
  }
  
  public void setValue(Map<String, Tag> value) {
    this.value = new LinkedHashMap<>(value);
  }
  
  public boolean isEmpty() {
    return this.value.isEmpty();
  }
  
  public boolean contains(String tagName) {
    return this.value.containsKey(tagName);
  }
  
  public <T extends Tag> T get(String tagName) {
    return (T)this.value.get(tagName);
  }
  
  public <T extends Tag> T put(T tag) {
    return (T)this.value.put(tag.getName(), (Tag)tag);
  }
  
  public <T extends Tag> T remove(String tagName) {
    return (T)this.value.remove(tagName);
  }
  
  public Set<String> keySet() {
    return this.value.keySet();
  }
  
  public Collection<Tag> values() {
    return this.value.values();
  }
  
  public int size() {
    return this.value.size();
  }
  
  public void clear() {
    this.value.clear();
  }
  
  public Iterator<Tag> iterator() {
    return values().iterator();
  }
  
  public void read(DataInput in) throws IOException {
    List<Tag> tags = new ArrayList<>();
    try {
      Tag tag;
      while ((tag = NBTIO.readTag(in)) != null)
        tags.add(tag); 
    } catch (EOFException e) {
      throw new IOException("Closing EndTag was not found!");
    } 
    for (Tag tag : tags)
      put(tag); 
  }
  
  public void write(DataOutput out) throws IOException {
    for (Tag tag : this.value.values())
      NBTIO.writeTag(out, tag); 
    out.writeByte(0);
  }
  
  public CompoundTag clone() {
    Map<String, Tag> newMap = new LinkedHashMap<>();
    for (Map.Entry<String, Tag> entry : this.value.entrySet())
      newMap.put(entry.getKey(), ((Tag)entry.getValue()).clone()); 
    return new CompoundTag(getName(), newMap);
  }
}
