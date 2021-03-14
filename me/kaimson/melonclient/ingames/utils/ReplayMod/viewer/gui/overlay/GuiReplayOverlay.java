package me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.gui.overlay;

import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.CustomLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.HorizontalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.Layout;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.WritablePoint;
import me.kaimson.melonclient.Events.EventHandler;
import me.kaimson.melonclient.Events.TypeEvent;
import me.kaimson.melonclient.Events.imp.KeyInputEvent;
import me.kaimson.melonclient.ingames.utils.ReplayMod.core.ReplayCore;
import me.kaimson.melonclient.ingames.utils.ReplayMod.customgui.AbstractGuiElement;
import me.kaimson.melonclient.ingames.utils.ReplayMod.customgui.AbstractGuiOverlay;
import me.kaimson.melonclient.ingames.utils.ReplayMod.customgui.GuiPanel;
import me.kaimson.melonclient.ingames.utils.ReplayMod.customgui.GuiSlider;
import me.kaimson.melonclient.ingames.utils.ReplayMod.customgui.GuiTexturedButton;
import me.kaimson.melonclient.ingames.utils.ReplayMod.customgui.GuiTooltip;
import me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.ReplayHandler;
import me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.ReplaySender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import org.lwjgl.input.Keyboard;

public class GuiReplayOverlay extends AbstractGuiOverlay<GuiReplayOverlay> {
  public final GuiPanel topPanel = (GuiPanel)(new GuiPanel((GuiContainer)this)).setLayout((Layout)(new HorizontalLayout(HorizontalLayout.Alignment.LEFT)).setSpacing(5));
  
  public final GuiTexturedButton playPauseButton = (GuiTexturedButton)((GuiTexturedButton)((GuiTexturedButton)(new GuiTexturedButton() {
      public GuiElement<?> getTooltip(RenderInfo renderInfo) {
        GuiTooltip tooltip = (GuiTooltip)super.getTooltip(renderInfo);
        if (tooltip != null)
          if (getTextureNormal().getY() == 0) {
            tooltip.setText("Play");
          } else {
            tooltip.setText("Pause");
          }  
        return (GuiElement<?>)tooltip;
      }
    }).setSize(20, 20)).setTexture(ReplayCore.TEXTURE, 256)).setTooltip((GuiElement)new GuiTooltip());
  
  public final GuiSlider speedSlider = (GuiSlider)((GuiSlider)(new GuiSlider()).setSize(100, 20)).setSteps(37);
  
  public final GuiMarkerTimeline timeline;
  
  public final GuiPanel statusIndicatorPanel = (GuiPanel)((GuiPanel)(new GuiPanel((GuiContainer)this)).setSize(100, 20)).setLayout((Layout)(new HorizontalLayout(HorizontalLayout.Alignment.RIGHT)).setSpacing(5));
  
  public GuiReplayOverlay(final ReplayHandler replayHandler) {
    this
      
      .timeline = (GuiMarkerTimeline)(new GuiMarkerTimeline(replayHandler) {
        public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
          setCursorPosition(replayHandler.getReplaySender().currentTimeStamp());
          super.draw(renderer, size, renderInfo);
        }
      }).setSize(2147483647, 20);
    this.topPanel.addElements(null, new GuiElement[] { (GuiElement)this.playPauseButton, (GuiElement)this.speedSlider, (GuiElement)this.timeline });
    setLayout((Layout)new CustomLayout<GuiReplayOverlay>() {
          protected void layout(GuiReplayOverlay guiReplayOverlay, int width, int height) {
            pos((GuiElement)GuiReplayOverlay.this.topPanel, 10, 10);
            size((GuiElement)GuiReplayOverlay.this.topPanel, width - 20, 20);
            pos((GuiElement)GuiReplayOverlay.this.statusIndicatorPanel, width / 2, height - 25);
            width((GuiElement)GuiReplayOverlay.this.statusIndicatorPanel, width / 2 - 5);
          }
        });
    ((GuiTexturedButton)this.playPauseButton.setTexturePosH(new ReadablePoint() {
          public int getX() {
            return 0;
          }
          
          public int getY() {
            return replayHandler.getReplaySender().paused() ? 0 : 20;
          }
          
          public void getLocation(WritablePoint writablePoint) {
            writablePoint.setLocation(getX(), getY());
          }
        })).onClick(() -> {
          ReplaySender replaySender = replayHandler.getReplaySender();
          if (replaySender.paused()) {
            replaySender.setReplaySpeed(getSpeedSliderValue());
          } else {
            replaySender.setReplaySpeed(0.0D);
          } 
        });
    ((GuiSlider)this.speedSlider.onValueChanged(() -> {
          double speed = getSpeedSliderValue();
          this.speedSlider.setText("Speed: " + speed + "x");
          ReplaySender replaySender = replayHandler.getReplaySender();
          if (!replaySender.paused())
            replaySender.setReplaySpeed(speed); 
        })).setValue(9);
    ((GuiMarkerTimeline)this.timeline.onClick(time -> replayHandler.doJump(time, true))).setLength(replayHandler.getReplayDuration());
  }
  
  public double getSpeedSliderValue() {
    int value = this.speedSlider.getValue() + 1;
    if (value <= 9)
      return value / 10.0D; 
    return 1.0D + 0.25D * (value - 10);
  }
  
  public void setVisible(boolean visible) {
    if (isVisible() != visible)
      if (visible) {
        EventHandler.register(this);
      } else {
        EventHandler.unregister(this);
      }  
    super.setVisible(visible);
  }
  
  @TypeEvent
  public void onKeyPressed(KeyInputEvent e) {
    GameSettings gameSettings = (Minecraft.getMinecraft()).gameSettings;
    while (gameSettings.keyBindChat.isPressed() || gameSettings.keyBindCommand.isPressed()) {
      if (!isMouseVisible())
        setMouseVisible(true); 
    } 
    if (Keyboard.getEventKeyState())
      if (isMouseVisible() && Keyboard.getEventKey() == 59)
        gameSettings.hideGUI = !gameSettings.hideGUI;  
  }
  
  public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
    if ((Minecraft.getMinecraft()).gameSettings.hideGUI && isAllowUserInput())
      return; 
    super.draw(renderer, size, renderInfo);
  }
  
  protected GuiReplayOverlay getThis() {
    return null;
  }
}
