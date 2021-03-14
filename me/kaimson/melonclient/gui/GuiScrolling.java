package me.kaimson.melonclient.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Mouse;

public abstract class GuiScrolling extends GuiSlot {
  private boolean stillRunning;
  
  public GuiScrolling(Minecraft mcIn, int width, int height, int topIn, int bottomIn, int slotHeightIn) {
    super(mcIn, width, height, topIn, bottomIn, slotHeightIn);
    this.stillRunning = false;
  }
  
  public void setDimensions(int widthIn, int heightIn, int topIn, int bottomIn) {
    this.width = widthIn;
    this.height = heightIn;
    this.top = topIn;
    this.bottom = bottomIn;
    this.left = 0;
    this.right = widthIn;
  }
  
  public void setShowSelectionBox(boolean showSelectionBoxIn) {
    this.showSelectionBox = showSelectionBoxIn;
  }
  
  protected void setHasListHeader(boolean hasListHeaderIn, int headerPaddingIn) {
    this.hasListHeader = hasListHeaderIn;
    this.headerPadding = headerPaddingIn;
    if (!hasListHeaderIn)
      this.headerPadding = 0; 
  }
  
  protected abstract int getSize();
  
  protected abstract void elementClicked(int paramInt1, boolean paramBoolean, int paramInt2, int paramInt3);
  
  protected abstract boolean isSelected(int paramInt);
  
  public int getContentHeight() {
    return getSize() * this.slotHeight + this.headerPadding;
  }
  
  protected abstract void drawBackground();
  
  protected void func_178040_a(int p_178040_1_, int p_178040_2_, int p_178040_3_) {}
  
  protected abstract void drawSlot(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6);
  
  protected void drawListHeader(int p_148129_1_, int p_148129_2_, Tessellator p_148129_3_) {}
  
  protected void func_148132_a(int p_148132_1_, int p_148132_2_) {}
  
  public void func_148142_b(int p_148142_1_, int p_148142_2_) {}
  
  public int getSlotIndexFromScreenCoords(int p_148124_1_, int p_148124_2_) {
    int i = this.left + this.width / 2 - getListWidth() / 2;
    int j = this.left + this.width / 2 + getListWidth() / 2;
    int k = p_148124_2_ - this.top - this.headerPadding + (int)this.amountScrolled - 4;
    int l = k / this.slotHeight;
    return (p_148124_1_ < getScrollBarX() && p_148124_1_ >= i && p_148124_1_ <= j && l >= 0 && k >= 0 && l < getSize()) ? l : -1;
  }
  
  public int func_148135_f() {
    return Math.max(0, getContentHeight() - this.bottom - this.top - 4);
  }
  
  public int getAmountScrolled() {
    return (int)this.amountScrolled;
  }
  
  public boolean isMouseYWithinSlotBounds(int p_148141_1_) {
    return (p_148141_1_ >= this.top && p_148141_1_ <= this.bottom && this.mouseX >= this.left && this.mouseX <= this.right);
  }
  
  public void scrollBy(int amount) {
    this.amountScrolled += amount;
    bindAmountScrolled();
    this.initialClickY = -2;
  }
  
  public void drawScreen(int mouseXIn, int mouseYIn, float p_148128_3_) {
    if (this.field_178041_q) {
      this.mouseX = mouseXIn;
      this.mouseY = mouseYIn;
      drawBackground();
      int i = getScrollBarX();
      int j = i + 6;
      GlStateManager.disableLighting();
      GlStateManager.disableFog();
      Tessellator tessellator = Tessellator.getInstance();
      WorldRenderer worldrenderer = tessellator.getWorldRenderer();
      drawContainerBackground(tessellator);
      int k = this.left + this.width / 2 - getListWidth() / 2 + 2;
      int l = this.top + 4 - (int)this.amountScrolled;
      if (this.hasListHeader)
        drawListHeader(k, l, tessellator); 
      drawSelectionBox(k, l, mouseXIn, mouseYIn);
      GlStateManager.disableDepth();
      int i1 = 4;
      overlayBackground(0, this.top, 255, 255);
      overlayBackground(this.bottom, this.height, 255, 255);
      GlStateManager.enableBlend();
      GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
      GlStateManager.disableAlpha();
      GlStateManager.shadeModel(7425);
      GlStateManager.disableTexture2D();
      worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
      worldrenderer.pos(this.left, (this.top + i1), 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 0).endVertex();
      worldrenderer.pos(this.right, (this.top + i1), 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 0).endVertex();
      worldrenderer.pos(this.right, this.top, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
      worldrenderer.pos(this.left, this.top, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
      tessellator.draw();
      worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
      worldrenderer.pos(this.left, this.bottom, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
      worldrenderer.pos(this.right, this.bottom, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
      worldrenderer.pos(this.right, (this.bottom - i1), 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 0).endVertex();
      worldrenderer.pos(this.left, (this.bottom - i1), 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 0).endVertex();
      tessellator.draw();
      int j1 = func_148135_f();
      drawScroll(i, j);
    } 
    handleDragging(mouseXIn, mouseYIn);
  }
  
  public void drawScroll(int i, int j) {
    Tessellator tessellator = Tessellator.getInstance();
    WorldRenderer buffer = tessellator.getWorldRenderer();
    int scrollbarPositionMinX = getScrollBarX();
    int scrollbarPositionMaxX = scrollbarPositionMinX + 6;
    int maxScroll = func_148135_f();
    int contentHeight = getContentHeight();
    if (maxScroll > 0) {
      int height = (this.bottom - this.top) * (this.bottom - this.top) / contentHeight;
      height = MathHelper.clamp_int(height, 32, this.bottom - this.top - 8);
      height -= (int)Math.min((this.amountScrolled < 0.0D) ? (int)-this.amountScrolled : ((this.amountScrolled > func_148135_f()) ? ((int)this.amountScrolled - func_148135_f()) : false), height * 0.75D);
      int minY = Math.min(Math.max(getAmountScrolled() * (this.bottom - this.top - height) / maxScroll + this.top, this.top), this.bottom - height);
      buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
      buffer.pos(scrollbarPositionMinX, this.bottom, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
      buffer.pos(scrollbarPositionMaxX, this.bottom, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
      buffer.pos(scrollbarPositionMaxX, this.top, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
      buffer.pos(scrollbarPositionMinX, this.top, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
      tessellator.draw();
      buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
      buffer.pos(scrollbarPositionMinX, (minY + height), 0.0D).tex(0.0D, 1.0D).color(128, 128, 128, 255).endVertex();
      buffer.pos(scrollbarPositionMaxX, (minY + height), 0.0D).tex(1.0D, 1.0D).color(128, 128, 128, 255).endVertex();
      buffer.pos(scrollbarPositionMaxX, minY, 0.0D).tex(1.0D, 0.0D).color(128, 128, 128, 255).endVertex();
      buffer.pos(scrollbarPositionMinX, minY, 0.0D).tex(0.0D, 0.0D).color(128, 128, 128, 255).endVertex();
      tessellator.draw();
      buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
      buffer.pos(scrollbarPositionMinX, (minY + height - 1), 0.0D).tex(0.0D, 1.0D).color(192, 192, 192, 255).endVertex();
      buffer.pos((scrollbarPositionMaxX - 1), (minY + height - 1), 0.0D).tex(1.0D, 1.0D).color(192, 192, 192, 255).endVertex();
      buffer.pos((scrollbarPositionMaxX - 1), minY, 0.0D).tex(1.0D, 0.0D).color(192, 192, 192, 255).endVertex();
      buffer.pos(scrollbarPositionMinX, minY, 0.0D).tex(0.0D, 0.0D).color(192, 192, 192, 255).endVertex();
      tessellator.draw();
    } 
    func_148142_b(this.mouseX, this.mouseY);
    GlStateManager.enableTexture2D();
    GlStateManager.shadeModel(7424);
    GlStateManager.enableAlpha();
    GlStateManager.disableBlend();
  }
  
  public void handleDragging(int mouseX, int mouseY) {
    if (isMouseYWithinSlotBounds(mouseY)) {
      if (Mouse.getEventButton() == 0 && Mouse.getEventButtonState() && mouseY >= this.top && mouseY <= this.bottom) {
        int i = (this.width - getListWidth()) / 2;
        int j = (this.width + getListWidth()) / 2;
        int k = mouseY - this.top - this.headerPadding + (int)this.amountScrolled - 4;
        int l = k / this.slotHeight;
        if (l < getSize() && mouseX >= i && mouseX <= j && l >= 0 && k >= 0) {
          elementClicked(l, false, mouseX, mouseY);
          this.selectedElement = l;
        } else if (mouseX >= i && mouseX <= j && k < 0) {
          func_148132_a(mouseX - i, mouseY - this.top + (int)this.amountScrolled - 4);
        } 
      } 
      if (Mouse.isButtonDown(0) && getEnabled()) {
        if (this.initialClickY == -1) {
          boolean flag1 = true;
          if (mouseY >= this.top && mouseY <= this.bottom) {
            int j2 = (this.width - getListWidth()) / 2;
            int k2 = (this.width + getListWidth()) / 2;
            int l2 = mouseY - this.top - this.headerPadding + (int)this.amountScrolled - 4;
            int i1 = l2 / this.slotHeight;
            if (i1 < getSize() && mouseX >= j2 && mouseX <= k2 && i1 >= 0 && l2 >= 0) {
              boolean flag = (i1 == this.selectedElement && Minecraft.getSystemTime() - this.lastClicked < 250L);
              elementClicked(i1, flag, mouseX, mouseY);
              this.selectedElement = i1;
              this.lastClicked = Minecraft.getSystemTime();
            } else if (mouseX >= j2 && mouseX <= k2 && l2 < 0) {
              func_148132_a(mouseX - j2, mouseY - this.top + (int)this.amountScrolled - 4);
              flag1 = false;
            } 
            int i3 = getScrollBarX();
            int j1 = i3 + 6;
            if (mouseX >= i3 && mouseX <= j1) {
              this.scrollMultiplier = -1.0F;
              int k1 = func_148135_f();
              if (k1 < 1)
                k1 = 1; 
              int l1 = (int)(((this.bottom - this.top) * (this.bottom - this.top)) / getContentHeight());
              l1 = MathHelper.clamp_int(l1, 32, this.bottom - this.top - 8);
              this.scrollMultiplier /= (this.bottom - this.top - l1) / k1;
            } else {
              this.scrollMultiplier = 1.0F;
            } 
            if (flag1) {
              this.initialClickY = mouseY;
            } else {
              this.initialClickY = -2;
            } 
          } else {
            this.initialClickY = -2;
          } 
        } else if (this.initialClickY >= 0) {
          this.amountScrolled -= (mouseY - this.initialClickY) * this.scrollMultiplier;
          this.initialClickY = mouseY;
        } 
      } else {
        this.initialClickY = -1;
      } 
      if (Mouse.isButtonDown(0))
        bindAmountScrolled(); 
    } 
  }
  
  public int getListWidth() {
    return 220;
  }
  
  protected void drawSelectionBox(int p_148120_1_, int p_148120_2_, int mouseXIn, int mouseYIn) {
    int i = getSize();
    Tessellator tessellator = Tessellator.getInstance();
    WorldRenderer worldrenderer = tessellator.getWorldRenderer();
    for (int j = 0; j < i; j++) {
      int k = p_148120_2_ + j * this.slotHeight + this.headerPadding;
      int l = this.slotHeight - 4;
      if (k > this.bottom || k + l < this.top)
        func_178040_a(j, p_148120_1_, k); 
      if (this.showSelectionBox && isSelected(j)) {
        int i1 = this.left + this.width / 2 - getListWidth() / 2;
        int j1 = this.left + this.width / 2 + getListWidth() / 2;
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableTexture2D();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        worldrenderer.pos(i1, (k + l + 2), 0.0D).tex(0.0D, 1.0D).color(128, 128, 128, 255).endVertex();
        worldrenderer.pos(j1, (k + l + 2), 0.0D).tex(1.0D, 1.0D).color(128, 128, 128, 255).endVertex();
        worldrenderer.pos(j1, (k - 2), 0.0D).tex(1.0D, 0.0D).color(128, 128, 128, 255).endVertex();
        worldrenderer.pos(i1, (k - 2), 0.0D).tex(0.0D, 0.0D).color(128, 128, 128, 255).endVertex();
        worldrenderer.pos((i1 + 1), (k + l + 1), 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
        worldrenderer.pos((j1 - 1), (k + l + 1), 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
        worldrenderer.pos((j1 - 1), (k - 1), 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
        worldrenderer.pos((i1 + 1), (k - 1), 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
      } 
      drawSlot(j, p_148120_1_, k, l, mouseXIn, mouseYIn);
    } 
  }
  
  public int getScrollBarX() {
    return this.width / 2 + 124;
  }
  
  protected void overlayBackground(int startY, int endY, int startAlpha, int endAlpha) {
    Tessellator tessellator = Tessellator.getInstance();
    WorldRenderer worldrenderer = tessellator.getWorldRenderer();
    this.mc.getTextureManager().bindTexture(Gui.optionsBackground);
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    float f = 32.0F;
    worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
    worldrenderer.pos(this.left, endY, 0.0D).tex(0.0D, (endY / 32.0F)).color(64, 64, 64, endAlpha).endVertex();
    worldrenderer.pos((this.left + this.width), endY, 0.0D).tex((this.width / 32.0F), (endY / 32.0F)).color(64, 64, 64, endAlpha).endVertex();
    worldrenderer.pos((this.left + this.width), startY, 0.0D).tex((this.width / 32.0F), (startY / 32.0F)).color(64, 64, 64, startAlpha).endVertex();
    worldrenderer.pos(this.left, startY, 0.0D).tex(0.0D, (startY / 32.0F)).color(64, 64, 64, startAlpha).endVertex();
    tessellator.draw();
  }
  
  public void setSlotXBoundsFromLeft(int leftIn) {
    this.left = leftIn;
    this.right = leftIn + this.width;
  }
  
  public int getSlotHeight() {
    return this.slotHeight;
  }
}
