package com.replaymod.lib.de.johni0702.minecraft.gui.utils;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Point;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.MCVer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;

public class MouseUtils {
  private static final Minecraft mc = MCVer.getMinecraft();
  
  public static Point getMousePos() {
    Point scaled = getScaledDimensions();
    int width = scaled.getX();
    int height = scaled.getY();
    int mouseX = Mouse.getX() * width / mc.field_71443_c;
    int mouseY = height - Mouse.getY() * height / mc.field_71440_d;
    return new Point(mouseX, mouseY);
  }
  
  public static Point getScaledDimensions() {
    ScaledResolution res = MCVer.newScaledResolution(mc);
    return new Point(res.func_78326_a(), res.func_78328_b());
  }
}
