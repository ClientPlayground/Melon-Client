package com.github.steveice10.opennbt.conversion.builtin;

import com.github.steveice10.opennbt.conversion.TagConverter;
import com.github.steveice10.opennbt.tag.builtin.LongArrayTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;

public class LongArrayTagConverter implements TagConverter<LongArrayTag, long[]> {
  public long[] convert(LongArrayTag tag) {
    return tag.getValue();
  }
  
  public LongArrayTag convert(String name, long[] value) {
    return new LongArrayTag(name, value);
  }
}
