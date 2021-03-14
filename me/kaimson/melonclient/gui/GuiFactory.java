package me.kaimson.melonclient.gui;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import me.kaimson.melonclient.gui.buttons.GuiButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

public class GuiFactory extends GuiScreen {
  private final List<Row> rows = Lists.newArrayList();
  
  public GuiFactory addRow(Row row) {
    this.rows.add(row);
    return this;
  }
  
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    for (Row row : this.rows) {
      for (GuiButton button : row.elements)
        button.drawButton(Minecraft.getMinecraft(), mouseX, mouseY); 
    } 
  }
  
  public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
    if (mouseButton == 0)
      for (Row row : this.rows) {
        for (int i = 0; i < row.elements.size(); i++) {
          GuiButton guibutton = row.elements.get(i);
          if (guibutton.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY))
            guibutton.playPressSound(Minecraft.getMinecraft().getSoundHandler()); 
        } 
      }  
  }
  
  public GuiFactory build() {
    for (Row row : this.rows) {
      int i = 0;
      for (GuiButton button : row.elements) {
        if (row.layout != null && row.layout.layoutType == Layout.LayoutType.HORIZONTAL) {
          if (button.width != row.width / row.elements.size() - row.layout.gap)
            button.width = row.width / row.elements.size() - row.layout.gap; 
          if (button.height != 20)
            button.height = 20; 
          button.set(row.x + (button.width + row.layout.gap) * i, row.y);
        } 
        i++;
      } 
    } 
    return this;
  }
  
  public static class Row {
    private final int x;
    
    private final int y;
    
    private final int width;
    
    private final int height;
    
    private final List<GuiButton> elements = Lists.newArrayList();
    
    private GuiFactory.Layout layout;
    
    public Row(int x, int y, int width, int height) {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
    }
    
    public Row addElement(GuiButton element) {
      this.elements.add(element);
      return this;
    }
    
    public Row setLayout(GuiFactory.Layout layout) {
      this.layout = layout;
      return this;
    }
  }
  
  public static class Layout {
    private final LayoutType layoutType;
    
    private final int gap;
    
    public Layout(LayoutType layoutType, int gap) {
      this.layoutType = layoutType;
      this.gap = gap;
    }
    
    public enum LayoutType {
      HORIZONTAL, VERTICAL;
    }
  }
  
  public enum LayoutType {
    HORIZONTAL, VERTICAL;
  }
}
