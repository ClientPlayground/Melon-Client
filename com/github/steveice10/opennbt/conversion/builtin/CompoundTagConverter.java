package com.github.steveice10.opennbt.conversion.builtin;

import com.github.steveice10.opennbt.conversion.ConverterRegistry;
import com.github.steveice10.opennbt.conversion.TagConverter;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import java.util.HashMap;
import java.util.Map;

public class CompoundTagConverter implements TagConverter<CompoundTag, Map> {
  public Map convert(CompoundTag tag) {
    Map<String, Object> ret = new HashMap<>();
    Map<String, Tag> tags = tag.getValue();
    for (String name : tags.keySet()) {
      Tag t = tags.get(name);
      ret.put(t.getName(), ConverterRegistry.convertToValue(t));
    } 
    return ret;
  }
  
  public CompoundTag convert(String name, Map value) {
    Map<String, Tag> tags = new HashMap<>();
    for (Object na : value.keySet()) {
      String n = (String)na;
      tags.put(n, ConverterRegistry.convertToTag(n, value.get(n)));
    } 
    return new CompoundTag(name, tags);
  }
}
