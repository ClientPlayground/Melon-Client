package com.github.steveice10.opennbt.conversion.builtin;

import com.github.steveice10.opennbt.conversion.TagConverter;
import com.github.steveice10.opennbt.tag.builtin.FloatTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;

public class FloatTagConverter implements TagConverter<FloatTag, Float> {
  public Float convert(FloatTag tag) {
    return tag.getValue();
  }
  
  public FloatTag convert(String name, Float value) {
    return new FloatTag(name, value.floatValue());
  }
}
