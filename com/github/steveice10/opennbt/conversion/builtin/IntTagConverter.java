package com.github.steveice10.opennbt.conversion.builtin;

import com.github.steveice10.opennbt.conversion.TagConverter;
import com.github.steveice10.opennbt.tag.builtin.IntTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;

public class IntTagConverter implements TagConverter<IntTag, Integer> {
  public Integer convert(IntTag tag) {
    return tag.getValue();
  }
  
  public IntTag convert(String name, Integer value) {
    return new IntTag(name, value.intValue());
  }
}
