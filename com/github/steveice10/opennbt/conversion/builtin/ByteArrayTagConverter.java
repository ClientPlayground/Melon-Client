package com.github.steveice10.opennbt.conversion.builtin;

import com.github.steveice10.opennbt.conversion.TagConverter;
import com.github.steveice10.opennbt.tag.builtin.ByteArrayTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;

public class ByteArrayTagConverter implements TagConverter<ByteArrayTag, byte[]> {
  public byte[] convert(ByteArrayTag tag) {
    return tag.getValue();
  }
  
  public ByteArrayTag convert(String name, byte[] value) {
    return new ByteArrayTag(name, value);
  }
}
