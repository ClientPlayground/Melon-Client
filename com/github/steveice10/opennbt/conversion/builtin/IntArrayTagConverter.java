package com.github.steveice10.opennbt.conversion.builtin;

import com.github.steveice10.opennbt.conversion.TagConverter;
import com.github.steveice10.opennbt.tag.builtin.IntArrayTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;

public class IntArrayTagConverter implements TagConverter<IntArrayTag, int[]> {
  public int[] convert(IntArrayTag tag) {
    return tag.getValue();
  }
  
  public IntArrayTag convert(String name, int[] value) {
    return new IntArrayTag(name, value);
  }
}
