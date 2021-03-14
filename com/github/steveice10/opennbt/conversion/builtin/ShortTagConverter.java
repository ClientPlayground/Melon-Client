package com.github.steveice10.opennbt.conversion.builtin;

import com.github.steveice10.opennbt.conversion.TagConverter;
import com.github.steveice10.opennbt.tag.builtin.ShortTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;

public class ShortTagConverter implements TagConverter<ShortTag, Short> {
  public Short convert(ShortTag tag) {
    return tag.getValue();
  }
  
  public ShortTag convert(String name, Short value) {
    return new ShortTag(name, value.shortValue());
  }
}
