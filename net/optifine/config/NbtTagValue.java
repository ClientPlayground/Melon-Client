package net.optifine.config;

import java.util.Arrays;
import java.util.regex.Pattern;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.src.Config;
import net.optifine.util.StrUtils;
import org.apache.commons.lang3.StringEscapeUtils;

public class NbtTagValue {
  private String[] parents = null;
  
  private String name = null;
  
  private boolean negative = false;
  
  private int type = 0;
  
  private String value = null;
  
  private int valueFormat = 0;
  
  private static final int TYPE_TEXT = 0;
  
  private static final int TYPE_PATTERN = 1;
  
  private static final int TYPE_IPATTERN = 2;
  
  private static final int TYPE_REGEX = 3;
  
  private static final int TYPE_IREGEX = 4;
  
  private static final String PREFIX_PATTERN = "pattern:";
  
  private static final String PREFIX_IPATTERN = "ipattern:";
  
  private static final String PREFIX_REGEX = "regex:";
  
  private static final String PREFIX_IREGEX = "iregex:";
  
  private static final int FORMAT_DEFAULT = 0;
  
  private static final int FORMAT_HEX_COLOR = 1;
  
  private static final String PREFIX_HEX_COLOR = "#";
  
  private static final Pattern PATTERN_HEX_COLOR = Pattern.compile("^#[0-9a-f]{6}+$");
  
  public NbtTagValue(String tag, String value) {
    String[] astring = Config.tokenize(tag, ".");
    this.parents = Arrays.<String>copyOfRange(astring, 0, astring.length - 1);
    this.name = astring[astring.length - 1];
    if (value.startsWith("!")) {
      this.negative = true;
      value = value.substring(1);
    } 
    if (value.startsWith("pattern:")) {
      this.type = 1;
      value = value.substring("pattern:".length());
    } else if (value.startsWith("ipattern:")) {
      this.type = 2;
      value = value.substring("ipattern:".length()).toLowerCase();
    } else if (value.startsWith("regex:")) {
      this.type = 3;
      value = value.substring("regex:".length());
    } else if (value.startsWith("iregex:")) {
      this.type = 4;
      value = value.substring("iregex:".length()).toLowerCase();
    } else {
      this.type = 0;
    } 
    value = StringEscapeUtils.unescapeJava(value);
    if (this.type == 0 && PATTERN_HEX_COLOR.matcher(value).matches())
      this.valueFormat = 1; 
    this.value = value;
  }
  
  public boolean matches(NBTTagCompound nbt) {
    return this.negative ? (!matchesCompound(nbt)) : matchesCompound(nbt);
  }
  
  public boolean matchesCompound(NBTTagCompound nbt) {
    if (nbt == null)
      return false; 
    NBTTagCompound nBTTagCompound = nbt;
    for (int i = 0; i < this.parents.length; i++) {
      String s = this.parents[i];
      nBTBase = getChildTag((NBTBase)nBTTagCompound, s);
      if (nBTBase == null)
        return false; 
    } 
    if (this.name.equals("*"))
      return matchesAnyChild(nBTBase); 
    NBTBase nBTBase = getChildTag(nBTBase, this.name);
    if (nBTBase == null)
      return false; 
    if (matchesBase(nBTBase))
      return true; 
    return false;
  }
  
  private boolean matchesAnyChild(NBTBase tagBase) {
    if (tagBase instanceof NBTTagCompound) {
      NBTTagCompound nbttagcompound = (NBTTagCompound)tagBase;
      for (String s : nbttagcompound.getKeySet()) {
        NBTBase nbtbase = nbttagcompound.getTag(s);
        if (matchesBase(nbtbase))
          return true; 
      } 
    } 
    if (tagBase instanceof NBTTagList) {
      NBTTagList nbttaglist = (NBTTagList)tagBase;
      int i = nbttaglist.tagCount();
      for (int j = 0; j < i; j++) {
        NBTBase nbtbase1 = nbttaglist.get(j);
        if (matchesBase(nbtbase1))
          return true; 
      } 
    } 
    return false;
  }
  
  private static NBTBase getChildTag(NBTBase tagBase, String tag) {
    if (tagBase instanceof NBTTagCompound) {
      NBTTagCompound nbttagcompound = (NBTTagCompound)tagBase;
      return nbttagcompound.getTag(tag);
    } 
    if (tagBase instanceof NBTTagList) {
      NBTTagList nbttaglist = (NBTTagList)tagBase;
      if (tag.equals("count"))
        return (NBTBase)new NBTTagInt(nbttaglist.tagCount()); 
      int i = Config.parseInt(tag, -1);
      return (i >= 0 && i < nbttaglist.tagCount()) ? nbttaglist.get(i) : null;
    } 
    return null;
  }
  
  public boolean matchesBase(NBTBase nbtBase) {
    if (nbtBase == null)
      return false; 
    String s = getNbtString(nbtBase, this.valueFormat);
    return matchesValue(s);
  }
  
  public boolean matchesValue(String nbtValue) {
    if (nbtValue == null)
      return false; 
    switch (this.type) {
      case 0:
        return nbtValue.equals(this.value);
      case 1:
        return matchesPattern(nbtValue, this.value);
      case 2:
        return matchesPattern(nbtValue.toLowerCase(), this.value);
      case 3:
        return matchesRegex(nbtValue, this.value);
      case 4:
        return matchesRegex(nbtValue.toLowerCase(), this.value);
    } 
    throw new IllegalArgumentException("Unknown NbtTagValue type: " + this.type);
  }
  
  private boolean matchesPattern(String str, String pattern) {
    return StrUtils.equalsMask(str, pattern, '*', '?');
  }
  
  private boolean matchesRegex(String str, String regex) {
    return str.matches(regex);
  }
  
  private static String getNbtString(NBTBase nbtBase, int format) {
    if (nbtBase == null)
      return null; 
    if (nbtBase instanceof NBTTagString) {
      NBTTagString nbttagstring = (NBTTagString)nbtBase;
      return nbttagstring.getString();
    } 
    if (nbtBase instanceof NBTTagInt) {
      NBTTagInt nbttagint = (NBTTagInt)nbtBase;
      return (format == 1) ? ("#" + StrUtils.fillLeft(Integer.toHexString(nbttagint.getInt()), 6, '0')) : Integer.toString(nbttagint.getInt());
    } 
    if (nbtBase instanceof NBTTagByte) {
      NBTTagByte nbttagbyte = (NBTTagByte)nbtBase;
      return Byte.toString(nbttagbyte.getByte());
    } 
    if (nbtBase instanceof NBTTagShort) {
      NBTTagShort nbttagshort = (NBTTagShort)nbtBase;
      return Short.toString(nbttagshort.getShort());
    } 
    if (nbtBase instanceof NBTTagLong) {
      NBTTagLong nbttaglong = (NBTTagLong)nbtBase;
      return Long.toString(nbttaglong.getLong());
    } 
    if (nbtBase instanceof NBTTagFloat) {
      NBTTagFloat nbttagfloat = (NBTTagFloat)nbtBase;
      return Float.toString(nbttagfloat.getFloat());
    } 
    if (nbtBase instanceof NBTTagDouble) {
      NBTTagDouble nbttagdouble = (NBTTagDouble)nbtBase;
      return Double.toString(nbttagdouble.getDouble());
    } 
    return nbtBase.toString();
  }
  
  public String toString() {
    StringBuffer stringbuffer = new StringBuffer();
    for (int i = 0; i < this.parents.length; i++) {
      String s = this.parents[i];
      if (i > 0)
        stringbuffer.append("."); 
      stringbuffer.append(s);
    } 
    if (stringbuffer.length() > 0)
      stringbuffer.append("."); 
    stringbuffer.append(this.name);
    stringbuffer.append(" = ");
    stringbuffer.append(this.value);
    return stringbuffer.toString();
  }
}
