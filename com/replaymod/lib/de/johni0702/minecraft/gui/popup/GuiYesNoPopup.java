package com.replaymod.lib.de.johni0702.minecraft.gui.popup;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiPanel;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.AbstractGuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiButton;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Typeable;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.HorizontalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.Layout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.LayoutData;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.VerticalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Colors;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;

public class GuiYesNoPopup extends AbstractGuiPopup<GuiYesNoPopup> implements Typeable {
  public static GuiYesNoPopup open(GuiContainer container, GuiElement... info) {
    GuiYesNoPopup popup = (GuiYesNoPopup)(new GuiYesNoPopup(container)).setBackgroundColor(Colors.DARK_TRANSPARENT);
    popup.getInfo().addElements((LayoutData)new VerticalLayout.Data(0.5D), info);
    popup.open();
    return popup;
  }
  
  private final SettableFuture<Boolean> future = SettableFuture.create();
  
  private final GuiButton yesButton = (GuiButton)((GuiButton)(new GuiButton())
    .setSize(150, 20)).onClick(new Runnable() {
        public void run() {
          GuiYesNoPopup.this.close();
          GuiYesNoPopup.this.future.set(Boolean.valueOf(true));
        }
      });
  
  public GuiButton getYesButton() {
    return this.yesButton;
  }
  
  private final GuiButton noButton = (GuiButton)((GuiButton)(new GuiButton())
    .setSize(150, 20)).onClick(new Runnable() {
        public void run() {
          GuiYesNoPopup.this.close();
          GuiYesNoPopup.this.future.set(Boolean.valueOf(false));
        }
      });
  
  public GuiButton getNoButton() {
    return this.noButton;
  }
  
  private final GuiPanel info = (GuiPanel)((GuiPanel)(new GuiPanel())
    .setMinSize((ReadableDimension)new Dimension(320, 50)))
    .setLayout((Layout)(new VerticalLayout(VerticalLayout.Alignment.TOP)).setSpacing(2));
  
  public GuiPanel getInfo() {
    return this.info;
  }
  
  private final GuiPanel buttons = (GuiPanel)((GuiPanel)(new GuiPanel())
    
    .setLayout((Layout)(new HorizontalLayout(HorizontalLayout.Alignment.CENTER)).setSpacing(5)))
    .addElements((LayoutData)new HorizontalLayout.Data(0.5D), new GuiElement[] { (GuiElement)this.yesButton, (GuiElement)this.noButton });
  
  private int layer;
  
  public GuiPanel getButtons() {
    return this.buttons;
  }
  
  public int getLayer() {
    return this.layer;
  }
  
  public GuiYesNoPopup setLayer(int layer) {
    this.layer = layer;
    return this;
  }
  
  public GuiYesNoPopup(GuiContainer container) {
    super(container);
    ((GuiPanel)this.popup.setLayout((Layout)(new VerticalLayout()).setSpacing(10))).addElements((LayoutData)new VerticalLayout.Data(0.5D), new GuiElement[] { (GuiElement)this.info, (GuiElement)this.buttons });
  }
  
  public GuiYesNoPopup setYesLabel(String label) {
    this.yesButton.setLabel(label);
    return this;
  }
  
  public GuiYesNoPopup setNoLabel(String label) {
    this.noButton.setLabel(label);
    return this;
  }
  
  public GuiYesNoPopup setYesI18nLabel(String label, Object... args) {
    this.yesButton.setI18nLabel(label, args);
    return this;
  }
  
  public GuiYesNoPopup setNoI18nLabel(String label, Object... args) {
    this.noButton.setI18nLabel(label, args);
    return this;
  }
  
  public ListenableFuture<Boolean> getFuture() {
    return (ListenableFuture<Boolean>)this.future;
  }
  
  protected GuiYesNoPopup getThis() {
    return this;
  }
  
  public boolean typeKey(ReadablePoint mousePosition, int keyCode, char keyChar, boolean ctrlDown, boolean shiftDown) {
    if (keyCode == 1) {
      this.noButton.onClick();
      return true;
    } 
    return false;
  }
}
