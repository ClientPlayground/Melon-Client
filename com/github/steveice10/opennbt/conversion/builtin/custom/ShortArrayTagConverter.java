package com.github.steveice10.opennbt.conversion.builtin.custom;

import com.github.steveice10.opennbt.conversion.TagConverter;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.github.steveice10.opennbt.tag.builtin.custom.ShortArrayTag;

public class ShortArrayTagConverter implements TagConverter<ShortArrayTag, short[]> {
  public short[] convert(ShortArrayTag tag) {
    return tag.getValue();
  }
  
  public ShortArrayTag convert(String name, short[] value) {
    return new ShortArrayTag(name, value);
  }
}
