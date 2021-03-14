package net.minecraft.client.gui.inventory;

import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

public abstract class GuiContainer extends GuiScreen {
  protected static final ResourceLocation inventoryBackground = new ResourceLocation("textures/gui/container/inventory.png");
  
  protected int xSize = 176;
  
  protected int ySize = 166;
  
  public Container inventorySlots;
  
  protected int guiLeft;
  
  protected int guiTop;
  
  private Slot theSlot;
  
  private Slot clickedSlot;
  
  private boolean isRightMouseClick;
  
  private ItemStack draggedStack;
  
  private int touchUpX;
  
  private int touchUpY;
  
  private Slot returningStackDestSlot;
  
  private long returningStackTime;
  
  private ItemStack returningStack;
  
  private Slot currentDragTargetSlot;
  
  private long dragItemDropDelay;
  
  protected final Set<Slot> dragSplittingSlots = Sets.newHashSet();
  
  protected boolean dragSplitting;
  
  private int dragSplittingLimit;
  
  private int dragSplittingButton;
  
  private boolean ignoreMouseUp;
  
  private int dragSplittingRemnant;
  
  private long lastClickTime;
  
  private Slot lastClickSlot;
  
  private int lastClickButton;
  
  private boolean doubleClick;
  
  private ItemStack shiftClickedSlot;
  
  public GuiContainer(Container inventorySlotsIn) {
    this.inventorySlots = inventorySlotsIn;
    this.ignoreMouseUp = true;
  }
  
  public void initGui() {
    super.initGui();
    this.mc.thePlayer.openContainer = this.inventorySlots;
    this.guiLeft = (this.width - this.xSize) / 2;
    this.guiTop = (this.height - this.ySize) / 2;
  }
  
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    drawDefaultBackground();
    int i = this.guiLeft;
    int j = this.guiTop;
    drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
    GlStateManager.disableRescaleNormal();
    RenderHelper.disableStandardItemLighting();
    GlStateManager.disableLighting();
    GlStateManager.disableDepth();
    super.drawScreen(mouseX, mouseY, partialTicks);
    RenderHelper.enableGUIStandardItemLighting();
    GlStateManager.pushMatrix();
    GlStateManager.translate(i, j, 0.0F);
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    GlStateManager.enableRescaleNormal();
    this.theSlot = null;
    int k = 240;
    int l = 240;
    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, k / 1.0F, l / 1.0F);
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    for (int i1 = 0; i1 < this.inventorySlots.inventorySlots.size(); i1++) {
      Slot slot = this.inventorySlots.inventorySlots.get(i1);
      drawSlot(slot);
      if (isMouseOverSlot(slot, mouseX, mouseY) && slot.canBeHovered()) {
        this.theSlot = slot;
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        int j1 = slot.xDisplayPosition;
        int k1 = slot.yDisplayPosition;
        GlStateManager.colorMask(true, true, true, false);
        drawGradientRect(j1, k1, j1 + 16, k1 + 16, -2130706433, -2130706433);
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
      } 
    } 
    RenderHelper.disableStandardItemLighting();
    drawGuiContainerForegroundLayer(mouseX, mouseY);
    RenderHelper.enableGUIStandardItemLighting();
    InventoryPlayer inventoryplayer = this.mc.thePlayer.inventory;
    ItemStack itemstack = (this.draggedStack == null) ? inventoryplayer.getItemStack() : this.draggedStack;
    if (itemstack != null) {
      int j2 = 8;
      int k2 = (this.draggedStack == null) ? 8 : 16;
      String s = null;
      if (this.draggedStack != null && this.isRightMouseClick) {
        itemstack = itemstack.copy();
        itemstack.stackSize = MathHelper.ceiling_float_int(itemstack.stackSize / 2.0F);
      } else if (this.dragSplitting && this.dragSplittingSlots.size() > 1) {
        itemstack = itemstack.copy();
        itemstack.stackSize = this.dragSplittingRemnant;
        if (itemstack.stackSize == 0)
          s = "" + EnumChatFormatting.YELLOW + "0"; 
      } 
      drawItemStack(itemstack, mouseX - i - j2, mouseY - j - k2, s);
    } 
    if (this.returningStack != null) {
      float f = (float)(Minecraft.getSystemTime() - this.returningStackTime) / 100.0F;
      if (f >= 1.0F) {
        f = 1.0F;
        this.returningStack = null;
      } 
      int l2 = this.returningStackDestSlot.xDisplayPosition - this.touchUpX;
      int i3 = this.returningStackDestSlot.yDisplayPosition - this.touchUpY;
      int l1 = this.touchUpX + (int)(l2 * f);
      int i2 = this.touchUpY + (int)(i3 * f);
      drawItemStack(this.returningStack, l1, i2, (String)null);
    } 
    GlStateManager.popMatrix();
    if (inventoryplayer.getItemStack() == null && this.theSlot != null && this.theSlot.getHasStack()) {
      ItemStack itemstack1 = this.theSlot.getStack();
      renderToolTip(itemstack1, mouseX, mouseY);
    } 
    GlStateManager.enableLighting();
    GlStateManager.enableDepth();
    RenderHelper.enableStandardItemLighting();
  }
  
  private void drawItemStack(ItemStack stack, int x, int y, String altText) {
    GlStateManager.translate(0.0F, 0.0F, 32.0F);
    this.zLevel = 200.0F;
    this.itemRender.zLevel = 200.0F;
    this.itemRender.renderItemAndEffectIntoGUI(stack, x, y);
    this.itemRender.renderItemOverlayIntoGUI(this.fontRendererObj, stack, x, y - ((this.draggedStack == null) ? 0 : 8), altText);
    this.zLevel = 0.0F;
    this.itemRender.zLevel = 0.0F;
  }
  
  protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {}
  
  protected abstract void drawGuiContainerBackgroundLayer(float paramFloat, int paramInt1, int paramInt2);
  
  private void drawSlot(Slot slotIn) {
    int i = slotIn.xDisplayPosition;
    int j = slotIn.yDisplayPosition;
    ItemStack itemstack = slotIn.getStack();
    boolean flag = false;
    boolean flag1 = (slotIn == this.clickedSlot && this.draggedStack != null && !this.isRightMouseClick);
    ItemStack itemstack1 = this.mc.thePlayer.inventory.getItemStack();
    String s = null;
    if (slotIn == this.clickedSlot && this.draggedStack != null && this.isRightMouseClick && itemstack != null) {
      itemstack = itemstack.copy();
      itemstack.stackSize /= 2;
    } else if (this.dragSplitting && this.dragSplittingSlots.contains(slotIn) && itemstack1 != null) {
      if (this.dragSplittingSlots.size() == 1)
        return; 
      if (Container.canAddItemToSlot(slotIn, itemstack1, true) && this.inventorySlots.canDragIntoSlot(slotIn)) {
        itemstack = itemstack1.copy();
        flag = true;
        Container.computeStackSize(this.dragSplittingSlots, this.dragSplittingLimit, itemstack, (slotIn.getStack() == null) ? 0 : (slotIn.getStack()).stackSize);
        if (itemstack.stackSize > itemstack.getMaxStackSize()) {
          s = EnumChatFormatting.YELLOW + "" + itemstack.getMaxStackSize();
          itemstack.stackSize = itemstack.getMaxStackSize();
        } 
        if (itemstack.stackSize > slotIn.getItemStackLimit(itemstack)) {
          s = EnumChatFormatting.YELLOW + "" + slotIn.getItemStackLimit(itemstack);
          itemstack.stackSize = slotIn.getItemStackLimit(itemstack);
        } 
      } else {
        this.dragSplittingSlots.remove(slotIn);
        updateDragSplitting();
      } 
    } 
    this.zLevel = 100.0F;
    this.itemRender.zLevel = 100.0F;
    if (itemstack == null) {
      String s1 = slotIn.getSlotTexture();
      if (s1 != null) {
        TextureAtlasSprite textureatlassprite = this.mc.getTextureMapBlocks().getAtlasSprite(s1);
        GlStateManager.disableLighting();
        this.mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
        drawTexturedModalRect(i, j, textureatlassprite, 16, 16);
        GlStateManager.enableLighting();
        flag1 = true;
      } 
    } 
    if (!flag1) {
      if (flag)
        drawRect(i, j, i + 16, j + 16, -2130706433); 
      GlStateManager.enableDepth();
      this.itemRender.renderItemAndEffectIntoGUI(itemstack, i, j);
      this.itemRender.renderItemOverlayIntoGUI(this.fontRendererObj, itemstack, i, j, s);
    } 
    this.itemRender.zLevel = 0.0F;
    this.zLevel = 0.0F;
  }
  
  private void updateDragSplitting() {
    ItemStack itemstack = this.mc.thePlayer.inventory.getItemStack();
    if (itemstack != null && this.dragSplitting) {
      this.dragSplittingRemnant = itemstack.stackSize;
      for (Slot slot : this.dragSplittingSlots) {
        ItemStack itemstack1 = itemstack.copy();
        int i = (slot.getStack() == null) ? 0 : (slot.getStack()).stackSize;
        Container.computeStackSize(this.dragSplittingSlots, this.dragSplittingLimit, itemstack1, i);
        if (itemstack1.stackSize > itemstack1.getMaxStackSize())
          itemstack1.stackSize = itemstack1.getMaxStackSize(); 
        if (itemstack1.stackSize > slot.getItemStackLimit(itemstack1))
          itemstack1.stackSize = slot.getItemStackLimit(itemstack1); 
        this.dragSplittingRemnant -= itemstack1.stackSize - i;
      } 
    } 
  }
  
  private Slot getSlotAtPosition(int x, int y) {
    for (int i = 0; i < this.inventorySlots.inventorySlots.size(); i++) {
      Slot slot = this.inventorySlots.inventorySlots.get(i);
      if (isMouseOverSlot(slot, x, y))
        return slot; 
    } 
    return null;
  }
  
  protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
    super.mouseClicked(mouseX, mouseY, mouseButton);
    boolean flag = (mouseButton == this.mc.gameSettings.keyBindPickBlock.getKeyCode() + 100);
    Slot slot = getSlotAtPosition(mouseX, mouseY);
    long i = Minecraft.getSystemTime();
    this.doubleClick = (this.lastClickSlot == slot && i - this.lastClickTime < 250L && this.lastClickButton == mouseButton);
    this.ignoreMouseUp = false;
    if (mouseButton == 0 || mouseButton == 1 || flag) {
      int j = this.guiLeft;
      int k = this.guiTop;
      boolean flag1 = (mouseX < j || mouseY < k || mouseX >= j + this.xSize || mouseY >= k + this.ySize);
      int l = -1;
      if (slot != null)
        l = slot.slotNumber; 
      if (flag1)
        l = -999; 
      if (this.mc.gameSettings.touchscreen && flag1 && this.mc.thePlayer.inventory.getItemStack() == null) {
        this.mc.displayGuiScreen((GuiScreen)null);
        return;
      } 
      if (l != -1)
        if (this.mc.gameSettings.touchscreen) {
          if (slot != null && slot.getHasStack()) {
            this.clickedSlot = slot;
            this.draggedStack = null;
            this.isRightMouseClick = (mouseButton == 1);
          } else {
            this.clickedSlot = null;
          } 
        } else if (!this.dragSplitting) {
          if (this.mc.thePlayer.inventory.getItemStack() == null) {
            if (mouseButton == this.mc.gameSettings.keyBindPickBlock.getKeyCode() + 100) {
              handleMouseClick(slot, l, mouseButton, 3);
            } else {
              boolean flag2 = (l != -999 && (Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54)));
              int i1 = 0;
              if (flag2) {
                this.shiftClickedSlot = (slot != null && slot.getHasStack()) ? slot.getStack() : null;
                i1 = 1;
              } else if (l == -999) {
                i1 = 4;
              } 
              handleMouseClick(slot, l, mouseButton, i1);
            } 
            this.ignoreMouseUp = true;
          } else {
            this.dragSplitting = true;
            this.dragSplittingButton = mouseButton;
            this.dragSplittingSlots.clear();
            if (mouseButton == 0) {
              this.dragSplittingLimit = 0;
            } else if (mouseButton == 1) {
              this.dragSplittingLimit = 1;
            } else if (mouseButton == this.mc.gameSettings.keyBindPickBlock.getKeyCode() + 100) {
              this.dragSplittingLimit = 2;
            } 
          } 
        }  
    } 
    this.lastClickSlot = slot;
    this.lastClickTime = i;
    this.lastClickButton = mouseButton;
  }
  
  protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
    Slot slot = getSlotAtPosition(mouseX, mouseY);
    ItemStack itemstack = this.mc.thePlayer.inventory.getItemStack();
    if (this.clickedSlot != null && this.mc.gameSettings.touchscreen) {
      if (clickedMouseButton == 0 || clickedMouseButton == 1)
        if (this.draggedStack == null) {
          if (slot != this.clickedSlot && this.clickedSlot.getStack() != null)
            this.draggedStack = this.clickedSlot.getStack().copy(); 
        } else if (this.draggedStack.stackSize > 1 && slot != null && Container.canAddItemToSlot(slot, this.draggedStack, false)) {
          long i = Minecraft.getSystemTime();
          if (this.currentDragTargetSlot == slot) {
            if (i - this.dragItemDropDelay > 500L) {
              handleMouseClick(this.clickedSlot, this.clickedSlot.slotNumber, 0, 0);
              handleMouseClick(slot, slot.slotNumber, 1, 0);
              handleMouseClick(this.clickedSlot, this.clickedSlot.slotNumber, 0, 0);
              this.dragItemDropDelay = i + 750L;
              this.draggedStack.stackSize--;
            } 
          } else {
            this.currentDragTargetSlot = slot;
            this.dragItemDropDelay = i;
          } 
        }  
    } else if (this.dragSplitting && slot != null && itemstack != null && itemstack.stackSize > this.dragSplittingSlots.size() && Container.canAddItemToSlot(slot, itemstack, true) && slot.isItemValid(itemstack) && this.inventorySlots.canDragIntoSlot(slot)) {
      this.dragSplittingSlots.add(slot);
      updateDragSplitting();
    } 
  }
  
  protected void mouseReleased(int mouseX, int mouseY, int state) {
    Slot slot = getSlotAtPosition(mouseX, mouseY);
    int i = this.guiLeft;
    int j = this.guiTop;
    boolean flag = (mouseX < i || mouseY < j || mouseX >= i + this.xSize || mouseY >= j + this.ySize);
    int k = -1;
    if (slot != null)
      k = slot.slotNumber; 
    if (flag)
      k = -999; 
    if (this.doubleClick && slot != null && state == 0 && this.inventorySlots.canMergeSlot((ItemStack)null, slot)) {
      if (isShiftKeyDown()) {
        if (slot != null && slot.inventory != null && this.shiftClickedSlot != null)
          for (Slot slot2 : this.inventorySlots.inventorySlots) {
            if (slot2 != null && slot2.canTakeStack((EntityPlayer)this.mc.thePlayer) && slot2.getHasStack() && slot2.inventory == slot.inventory && Container.canAddItemToSlot(slot2, this.shiftClickedSlot, true))
              handleMouseClick(slot2, slot2.slotNumber, state, 1); 
          }  
      } else {
        handleMouseClick(slot, k, state, 6);
      } 
      this.doubleClick = false;
      this.lastClickTime = 0L;
    } else {
      if (this.dragSplitting && this.dragSplittingButton != state) {
        this.dragSplitting = false;
        this.dragSplittingSlots.clear();
        this.ignoreMouseUp = true;
        return;
      } 
      if (this.ignoreMouseUp) {
        this.ignoreMouseUp = false;
        return;
      } 
      if (this.clickedSlot != null && this.mc.gameSettings.touchscreen) {
        if (state == 0 || state == 1) {
          if (this.draggedStack == null && slot != this.clickedSlot)
            this.draggedStack = this.clickedSlot.getStack(); 
          boolean flag2 = Container.canAddItemToSlot(slot, this.draggedStack, false);
          if (k != -1 && this.draggedStack != null && flag2) {
            handleMouseClick(this.clickedSlot, this.clickedSlot.slotNumber, state, 0);
            handleMouseClick(slot, k, 0, 0);
            if (this.mc.thePlayer.inventory.getItemStack() != null) {
              handleMouseClick(this.clickedSlot, this.clickedSlot.slotNumber, state, 0);
              this.touchUpX = mouseX - i;
              this.touchUpY = mouseY - j;
              this.returningStackDestSlot = this.clickedSlot;
              this.returningStack = this.draggedStack;
              this.returningStackTime = Minecraft.getSystemTime();
            } else {
              this.returningStack = null;
            } 
          } else if (this.draggedStack != null) {
            this.touchUpX = mouseX - i;
            this.touchUpY = mouseY - j;
            this.returningStackDestSlot = this.clickedSlot;
            this.returningStack = this.draggedStack;
            this.returningStackTime = Minecraft.getSystemTime();
          } 
          this.draggedStack = null;
          this.clickedSlot = null;
        } 
      } else if (this.dragSplitting && !this.dragSplittingSlots.isEmpty()) {
        handleMouseClick((Slot)null, -999, Container.func_94534_d(0, this.dragSplittingLimit), 5);
        for (Slot slot1 : this.dragSplittingSlots)
          handleMouseClick(slot1, slot1.slotNumber, Container.func_94534_d(1, this.dragSplittingLimit), 5); 
        handleMouseClick((Slot)null, -999, Container.func_94534_d(2, this.dragSplittingLimit), 5);
      } else if (this.mc.thePlayer.inventory.getItemStack() != null) {
        if (state == this.mc.gameSettings.keyBindPickBlock.getKeyCode() + 100) {
          handleMouseClick(slot, k, state, 3);
        } else {
          boolean flag1 = (k != -999 && (Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54)));
          if (flag1)
            this.shiftClickedSlot = (slot != null && slot.getHasStack()) ? slot.getStack() : null; 
          handleMouseClick(slot, k, state, flag1 ? 1 : 0);
        } 
      } 
    } 
    if (this.mc.thePlayer.inventory.getItemStack() == null)
      this.lastClickTime = 0L; 
    this.dragSplitting = false;
  }
  
  private boolean isMouseOverSlot(Slot slotIn, int mouseX, int mouseY) {
    return isPointInRegion(slotIn.xDisplayPosition, slotIn.yDisplayPosition, 16, 16, mouseX, mouseY);
  }
  
  protected boolean isPointInRegion(int left, int top, int right, int bottom, int pointX, int pointY) {
    int i = this.guiLeft;
    int j = this.guiTop;
    pointX -= i;
    pointY -= j;
    return (pointX >= left - 1 && pointX < left + right + 1 && pointY >= top - 1 && pointY < top + bottom + 1);
  }
  
  protected void handleMouseClick(Slot slotIn, int slotId, int clickedButton, int clickType) {
    if (slotIn != null)
      slotId = slotIn.slotNumber; 
    this.mc.playerController.windowClick(this.inventorySlots.windowId, slotId, clickedButton, clickType, (EntityPlayer)this.mc.thePlayer);
  }
  
  protected void keyTyped(char typedChar, int keyCode) throws IOException {
    if (keyCode == 1 || keyCode == this.mc.gameSettings.keyBindInventory.getKeyCode())
      this.mc.thePlayer.closeScreen(); 
    checkHotbarKeys(keyCode);
    if (this.theSlot != null && this.theSlot.getHasStack())
      if (keyCode == this.mc.gameSettings.keyBindPickBlock.getKeyCode()) {
        handleMouseClick(this.theSlot, this.theSlot.slotNumber, 0, 3);
      } else if (keyCode == this.mc.gameSettings.keyBindDrop.getKeyCode()) {
        handleMouseClick(this.theSlot, this.theSlot.slotNumber, isCtrlKeyDown() ? 1 : 0, 4);
      }  
  }
  
  protected boolean checkHotbarKeys(int keyCode) {
    if (this.mc.thePlayer.inventory.getItemStack() == null && this.theSlot != null)
      for (int i = 0; i < 9; i++) {
        if (keyCode == this.mc.gameSettings.keyBindsHotbar[i].getKeyCode()) {
          handleMouseClick(this.theSlot, this.theSlot.slotNumber, i, 2);
          return true;
        } 
      }  
    return false;
  }
  
  public void onGuiClosed() {
    if (this.mc.thePlayer != null)
      this.inventorySlots.onContainerClosed((EntityPlayer)this.mc.thePlayer); 
  }
  
  public boolean doesGuiPauseGame() {
    return false;
  }
  
  public void updateScreen() {
    super.updateScreen();
    if (!this.mc.thePlayer.isEntityAlive() || this.mc.thePlayer.isDead)
      this.mc.thePlayer.closeScreen(); 
  }
}
