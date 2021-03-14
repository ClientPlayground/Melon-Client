package com.replaymod.lib.de.johni0702.minecraft.gui.utils;

import com.replaymod.lib.de.johni0702.minecraft.gui.versions.MCVer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.FontRenderer;

public class StringUtils {
  public static String[] splitStringInMultipleRows(String string, int maxWidth) {
    if (string == null)
      return new String[0]; 
    FontRenderer fontRenderer = MCVer.getFontRenderer();
    List<String> rows = new ArrayList<>();
    String remaining = string;
    while (remaining.length() > 0) {
      String[] split = remaining.split(" ");
      String b = "";
      for (String sp : split) {
        b = b + sp + " ";
        if (fontRenderer.func_78256_a(b.trim()) > maxWidth) {
          b = b.substring(0, b.trim().length() - sp.length());
          break;
        } 
      } 
      String trimmed = b.trim();
      rows.add(trimmed);
      try {
        remaining = remaining.substring(trimmed.length() + 1);
      } catch (Exception e) {
        break;
      } 
    } 
    return rows.<String>toArray(new String[rows.size()]);
  }
}
