package net.optifine.shaders.config;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.src.Config;
import net.optifine.shaders.Shaders;
import net.optifine.util.StrUtils;

public class ShaderOptionVariable extends ShaderOption {
  private static final Pattern PATTERN_VARIABLE = Pattern.compile("^\\s*#define\\s+(\\w+)\\s+(-?[0-9\\.Ff]+|\\w+)\\s*(//.*)?$");
  
  public ShaderOptionVariable(String name, String description, String value, String[] values, String path) {
    super(name, description, value, values, value, path);
    setVisible(((getValues()).length > 1));
  }
  
  public String getSourceLine() {
    return "#define " + getName() + " " + getValue() + " // Shader option " + getValue();
  }
  
  public String getValueText(String val) {
    String s = Shaders.translate("prefix." + getName(), "");
    String s1 = super.getValueText(val);
    String s2 = Shaders.translate("suffix." + getName(), "");
    String s3 = s + s1 + s2;
    return s3;
  }
  
  public String getValueColor(String val) {
    String s = val.toLowerCase();
    return (!s.equals("false") && !s.equals("off")) ? "§a" : "§c";
  }
  
  public boolean matchesLine(String line) {
    Matcher matcher = PATTERN_VARIABLE.matcher(line);
    if (!matcher.matches())
      return false; 
    String s = matcher.group(1);
    return s.matches(getName());
  }
  
  public static ShaderOption parseOption(String line, String path) {
    Matcher matcher = PATTERN_VARIABLE.matcher(line);
    if (!matcher.matches())
      return null; 
    String s = matcher.group(1);
    String s1 = matcher.group(2);
    String s2 = matcher.group(3);
    String s3 = StrUtils.getSegment(s2, "[", "]");
    if (s3 != null && s3.length() > 0)
      s2 = s2.replace(s3, "").trim(); 
    String[] astring = parseValues(s1, s3);
    if (s != null && s.length() > 0) {
      path = StrUtils.removePrefix(path, "/shaders/");
      ShaderOption shaderoption = new ShaderOptionVariable(s, s2, s1, astring, path);
      return shaderoption;
    } 
    return null;
  }
  
  public static String[] parseValues(String value, String valuesStr) {
    String[] astring = { value };
    if (valuesStr == null)
      return astring; 
    valuesStr = valuesStr.trim();
    valuesStr = StrUtils.removePrefix(valuesStr, "[");
    valuesStr = StrUtils.removeSuffix(valuesStr, "]");
    valuesStr = valuesStr.trim();
    if (valuesStr.length() <= 0)
      return astring; 
    String[] astring1 = Config.tokenize(valuesStr, " ");
    if (astring1.length <= 0)
      return astring; 
    if (!Arrays.<String>asList(astring1).contains(value))
      astring1 = (String[])Config.addObjectToArray((Object[])astring1, value, 0); 
    return astring1;
  }
}
