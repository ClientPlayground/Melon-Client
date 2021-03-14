package net.minecraft.client.gui;

import me.kaimson.melonclient.smoothscrolling.CustomGuiSlot;
import me.kaimson.melonclient.smoothscrolling.GuiSlotScroller;
import me.kaimson.melonclient.util.Returner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Mouse;

public abstract class GuiSlot {
  protected final Minecraft mc;
  
  protected int width;
  
  protected int height;
  
  public int top;
  
  public int bottom;
  
  public int right;
  
  public int left;
  
  protected final int slotHeight;
  
  private int scrollUpButtonID;
  
  private int scrollDownButtonID;
  
  protected int mouseX;
  
  protected int mouseY;
  
  protected boolean field_148163_i = true;
  
  protected int initialClickY = -2;
  
  protected float scrollMultiplier;
  
  public float amountScrolled;
  
  protected int selectedElement = -1;
  
  protected long lastClicked;
  
  protected boolean field_178041_q = true;
  
  protected boolean showSelectionBox = true;
  
  protected boolean hasListHeader;
  
  protected int headerPadding;
  
  private boolean enabled = true;
  
  public GuiSlotScroller scroller;
  
  public double scrollVelocity;
  
  private boolean lock = true;
  
  public GuiSlot(Minecraft mcIn, int width, int height, int topIn, int bottomIn, int slotHeightIn) {
    this.mc = mcIn;
    this.width = width;
    this.height = height;
    this.top = topIn;
    this.bottom = bottomIn;
    this.slotHeight = slotHeightIn;
    this.left = 0;
    this.right = width;
    this.scroller = new GuiSlotScroller(this);
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
  
  protected int getContentHeight() {
    return getSize() * this.slotHeight + this.headerPadding;
  }
  
  public int contentHeight() {
    return getSize() * this.slotHeight + this.headerPadding;
  }
  
  protected abstract void drawBackground();
  
  protected void func_178040_a(int p_178040_1_, int p_178040_2_, int p_178040_3_) {}
  
  protected abstract void drawSlot(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6);
  
  protected void drawListHeader(int p_148129_1_, int p_148129_2_, Tessellator p_148129_3_) {}
  
  protected void func_148132_a(int p_148132_1_, int p_148132_2_) {}
  
  protected void func_148142_b(int p_148142_1_, int p_148142_2_) {}
  
  public void func_148142_b_c(int p_148142_1_, int p_148142_2_) {
    func_148142_b(p_148142_1_, p_148142_2_);
  }
  
  public int getSlotIndexFromScreenCoords(int p_148124_1_, int p_148124_2_) {
    int i = this.left + this.width / 2 - getListWidth() / 2;
    int j = this.left + this.width / 2 + getListWidth() / 2;
    int k = p_148124_2_ - this.top - this.headerPadding + (int)this.amountScrolled - 4;
    int l = k / this.slotHeight;
    return (p_148124_1_ < getScrollBarX() && p_148124_1_ >= i && p_148124_1_ <= j && l >= 0 && k >= 0 && l < getSize()) ? l : -1;
  }
  
  public void registerScrollButtons(int scrollUpButtonIDIn, int scrollDownButtonIDIn) {
    this.scrollUpButtonID = scrollUpButtonIDIn;
    this.scrollDownButtonID = scrollDownButtonIDIn;
  }
  
  protected void bindAmountScrolled() {
    this.amountScrolled = MathHelper.clamp_float(this.amountScrolled, 0.0F, func_148135_f());
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
  
  public void actionPerformed(GuiButton button) {
    if (button.enabled)
      if (button.id == this.scrollUpButtonID) {
        this.amountScrolled -= (this.slotHeight * 2 / 3);
        this.initialClickY = -2;
        bindAmountScrolled();
      } else if (button.id == this.scrollDownButtonID) {
        this.amountScrolled += (this.slotHeight * 2 / 3);
        this.initialClickY = -2;
        bindAmountScrolled();
      }  
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
      CustomGuiSlot.renderScrollbar(this, mouseXIn, mouseYIn);
    } 
    if (!this.lock && isMouseYWithinSlotBounds(this.mouseY)) {
      if (Mouse.getEventButton() == 0 && Mouse.getEventButtonState() && this.mouseY >= this.top && this.mouseY <= this.bottom) {
        int i = (this.width - getListWidth()) / 2;
        int j = (this.width + getListWidth()) / 2;
        int k = this.mouseY - this.top - this.headerPadding + (int)this.amountScrolled - 4;
        int l = k / this.slotHeight;
        if (l < getSize() && this.mouseX >= i && this.mouseX <= j && l >= 0 && k >= 0) {
          elementClicked(l, false, this.mouseX, this.mouseY);
          this.selectedElement = l;
        } else if (this.mouseX >= i && this.mouseX <= j && k < 0) {
          func_148132_a(this.mouseX - i, this.mouseY - this.top + (int)this.amountScrolled - 4);
        } 
      } 
      if (Mouse.isButtonDown(0) && getEnabled()) {
        if (this.initialClickY == -1) {
          boolean flag1 = true;
          if (this.mouseY >= this.top && this.mouseY <= this.bottom) {
            int j2 = (this.width - getListWidth()) / 2;
            int k2 = (this.width + getListWidth()) / 2;
            int l2 = this.mouseY - this.top - this.headerPadding + (int)this.amountScrolled - 4;
            int i1 = l2 / this.slotHeight;
            if (i1 < getSize() && this.mouseX >= j2 && this.mouseX <= k2 && i1 >= 0 && l2 >= 0) {
              boolean flag = (i1 == this.selectedElement && Minecraft.getSystemTime() - this.lastClicked < 250L);
              elementClicked(i1, flag, this.mouseX, this.mouseY);
              this.selectedElement = i1;
              this.lastClicked = Minecraft.getSystemTime();
            } else if (this.mouseX >= j2 && this.mouseX <= k2 && l2 < 0) {
              func_148132_a(this.mouseX - j2, this.mouseY - this.top + (int)this.amountScrolled - 4);
              flag1 = false;
            } 
            int i3 = getScrollBarX();
            int j1 = i3 + 6;
            if (this.mouseX >= i3 && this.mouseX <= j1) {
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
              this.initialClickY = this.mouseY;
            } else {
              this.initialClickY = -2;
            } 
          } else {
            this.initialClickY = -2;
          } 
        } else if (this.initialClickY >= 0) {
          this.amountScrolled -= (this.mouseY - this.initialClickY) * this.scrollMultiplier;
          this.initialClickY = this.mouseY;
        } 
      } else {
        this.initialClickY = -1;
      } 
      if (Mouse.isButtonDown(0))
        bindAmountScrolled(); 
    } 
  }
  
  public void handleMouseInput() {
    this.lock = false;
    Returner returner = new Returner();
    if (Mouse.isButtonDown(0) && getEnabled()) {
      bindAmountScrolled();
    } else {
      returner.cancel();
      int i = Mouse.getEventDWheel();
      if (i != 0) {
        if (i > 0) {
          i = 1;
        } else if (i < 0) {
          i = -1;
        } 
        if (this.amountScrolled <= func_148135_f() && i < 0.0D)
          this.scrollVelocity += 16.0D; 
        if (this.amountScrolled >= 0.0D && i > 0.0D)
          this.scrollVelocity -= 16.0D; 
        if (!this.scroller.isRegistered())
          this.scroller.registerTick(); 
      } 
    } 
    if (returner.isCancelled())
      return; 
    int i2 = Mouse.getEventDWheel();
    if (i2 != 0) {
      if (i2 > 0) {
        i2 = -1;
      } else if (i2 < 0) {
        i2 = 1;
      } 
      this.amountScrolled += (i2 * this.slotHeight / 2);
    } 
  }
  
  public void setEnabled(boolean enabledIn) {
    this.enabled = enabledIn;
  }
  
  public boolean getEnabled() {
    return this.enabled;
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
      if (!(this instanceof GuiResourcePackList) || (k >= this.top - this.slotHeight && k <= this.bottom))
        drawSlot(j, p_148120_1_, k, l, mouseXIn, mouseYIn); 
    } 
  }
  
  protected int getScrollBarX() {
    return this.width / 2 + 124;
  }
  
  public int getScrollbarX() {
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
  
  protected void drawContainerBackground(Tessellator p_drawContainerBackground_1_) {
    WorldRenderer worldrenderer = p_drawContainerBackground_1_.getWorldRenderer();
    this.mc.getTextureManager().bindTexture(Gui.optionsBackground);
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    float f = 32.0F;
    worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
    worldrenderer.pos(this.left, this.bottom, 0.0D).tex((this.left / f), ((this.bottom + (int)this.amountScrolled) / f)).color(32, 32, 32, 255).endVertex();
    worldrenderer.pos(this.right, this.bottom, 0.0D).tex((this.right / f), ((this.bottom + (int)this.amountScrolled) / f)).color(32, 32, 32, 255).endVertex();
    worldrenderer.pos(this.right, this.top, 0.0D).tex((this.right / f), ((this.top + (int)this.amountScrolled) / f)).color(32, 32, 32, 255).endVertex();
    worldrenderer.pos(this.left, this.top, 0.0D).tex((this.left / f), ((this.top + (int)this.amountScrolled) / f)).color(32, 32, 32, 255).endVertex();
    p_drawContainerBackground_1_.draw();
  }
}
