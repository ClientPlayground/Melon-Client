package me.kaimson.melonclient.ingames.utils.ReplayMod.customgui;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class StringUtils {
  public static String[] splitStringInMultipleRows(String string, int maxWidth) {
    if (string == null)
      return new String[0]; 
    FontRenderer fontRenderer = (Minecraft.getMinecraft()).fontRendererObj;
    List<String> rows = new ArrayList<>();
    String remaining = string;
    while (remaining.length() > 0) {
      String[] split = remaining.split(" ");
      String b = "";
      String[] var7 = split;
      int var8 = split.length;
      for (int var9 = 0; var9 < var8; var9++) {
        String sp = var7[var9];
        b = b + sp + " ";
        if (fontRenderer.getStringWidth(b.trim()) > maxWidth) {
          b = b.substring(0, b.trim().length() - sp.length());
          break;
        } 
      } 
      String trimmed = b.trim();
      rows.add(trimmed);
      try {
        remaining = remaining.substring(trimmed.length() + 1);
      } catch (Exception var11) {
        break;
      } 
    } 
    return rows.<String>toArray(new String[rows.size()]);
  }
}
