package com.github.steveice10.opennbt.conversion.builtin;

import com.github.steveice10.opennbt.conversion.TagConverter;
import com.github.steveice10.opennbt.tag.builtin.LongTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;

public class LongTagConverter implements TagConverter<LongTag, Long> {
  public Long convert(LongTag tag) {
    return tag.getValue();
  }
  
  public LongTag convert(String name, Long value) {
    return new LongTag(name, value.longValue());
  }
}
