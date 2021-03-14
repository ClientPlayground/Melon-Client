package me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.gui.overlay;

import com.google.common.base.Strings;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiPanel;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.AbstractGuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiButton;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiLabel;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiNumberField;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiTextField;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.IGuiLabel;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Typeable;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.GridLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.HorizontalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.Layout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.LayoutData;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.VerticalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.popup.AbstractGuiPopup;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Colors;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import com.replaymod.replaystudio.data.Marker;
import me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.ReplayHandler;

public class GuiEditMarkerPopup extends AbstractGuiPopup<GuiEditMarkerPopup> implements Typeable {
  private final ReplayHandler replayHandler;
  
  private final Marker marker;
  
  private static GuiNumberField newGuiNumberField() {
    return (GuiNumberField)((GuiNumberField)(new GuiNumberField()).setSize(150, 20)).setValidateOnFocusChange(true);
  }
  
  public final GuiLabel title = (GuiLabel)(new GuiLabel()).setI18nText("replaymod.gui.editkeyframe.title.marker", new Object[0]);
  
  public final GuiTextField nameField = (GuiTextField)(new GuiTextField()).setSize(150, 20);
  
  public final GuiNumberField timeField = (GuiNumberField)newGuiNumberField().setPrecision(0);
  
  public final GuiNumberField xField = (GuiNumberField)newGuiNumberField().setPrecision(10);
  
  public final GuiNumberField yField = (GuiNumberField)newGuiNumberField().setPrecision(10);
  
  public final GuiNumberField zField = (GuiNumberField)newGuiNumberField().setPrecision(10);
  
  public final GuiNumberField yawField = (GuiNumberField)newGuiNumberField().setPrecision(5);
  
  public final GuiNumberField pitchField = (GuiNumberField)newGuiNumberField().setPrecision(5);
  
  public final GuiNumberField rollField = (GuiNumberField)newGuiNumberField().setPrecision(5);
  
  public final GuiPanel inputs = GuiPanel.builder()
    .layout((Layout)(new GridLayout()).setColumns(2).setSpacingX(7).setSpacingY(3))
    .with((GuiElement)(new GuiLabel()).setI18nText("replaymod.gui.editkeyframe.markername", new Object[0]), (LayoutData)new GridLayout.Data(0.0D, 0.5D))
    .with((GuiElement)this.nameField, (LayoutData)new GridLayout.Data(1.0D, 0.5D))
    .with((GuiElement)(new GuiLabel()).setI18nText("replaymod.gui.editkeyframe.timestamp", new Object[0]), (LayoutData)new GridLayout.Data(0.0D, 0.5D))
    .with((GuiElement)this.timeField, (LayoutData)new GridLayout.Data(1.0D, 0.5D))
    .with((GuiElement)(new GuiLabel()).setI18nText("replaymod.gui.editkeyframe.xpos", new Object[0]), (LayoutData)new GridLayout.Data(0.0D, 0.5D))
    .with((GuiElement)this.xField, (LayoutData)new GridLayout.Data(1.0D, 0.5D))
    .with((GuiElement)(new GuiLabel()).setI18nText("replaymod.gui.editkeyframe.ypos", new Object[0]), (LayoutData)new GridLayout.Data(0.0D, 0.5D))
    .with((GuiElement)this.yField, (LayoutData)new GridLayout.Data(1.0D, 0.5D))
    .with((GuiElement)(new GuiLabel()).setI18nText("replaymod.gui.editkeyframe.zpos", new Object[0]), (LayoutData)new GridLayout.Data(0.0D, 0.5D))
    .with((GuiElement)this.zField, (LayoutData)new GridLayout.Data(1.0D, 0.5D))
    .with((GuiElement)(new GuiLabel()).setI18nText("replaymod.gui.editkeyframe.camyaw", new Object[0]), (LayoutData)new GridLayout.Data(0.0D, 0.5D))
    .with((GuiElement)this.yawField, (LayoutData)new GridLayout.Data(1.0D, 0.5D))
    .with((GuiElement)(new GuiLabel()).setI18nText("replaymod.gui.editkeyframe.campitch", new Object[0]), (LayoutData)new GridLayout.Data(0.0D, 0.5D))
    .with((GuiElement)this.pitchField, (LayoutData)new GridLayout.Data(1.0D, 0.5D))
    .with((GuiElement)(new GuiLabel()).setI18nText("replaymod.gui.editkeyframe.camroll", new Object[0]), (LayoutData)new GridLayout.Data(0.0D, 0.5D))
    .with((GuiElement)this.rollField, (LayoutData)new GridLayout.Data(1.0D, 0.5D))
    .build();
  
  public final GuiButton saveButton = (GuiButton)((GuiButton)((GuiButton)(new GuiButton()).onClick(new Runnable() {
        public void run() {
          GuiEditMarkerPopup.this.marker.setName(Strings.emptyToNull(GuiEditMarkerPopup.this.nameField.getText()));
          GuiEditMarkerPopup.this.marker.setTime(GuiEditMarkerPopup.this.timeField.getInteger());
          GuiEditMarkerPopup.this.marker.setX(GuiEditMarkerPopup.this.xField.getDouble());
          GuiEditMarkerPopup.this.marker.setY(GuiEditMarkerPopup.this.yField.getDouble());
          GuiEditMarkerPopup.this.marker.setZ(GuiEditMarkerPopup.this.zField.getDouble());
          GuiEditMarkerPopup.this.marker.setYaw(GuiEditMarkerPopup.this.yawField.getFloat());
          GuiEditMarkerPopup.this.marker.setPitch(GuiEditMarkerPopup.this.pitchField.getFloat());
          GuiEditMarkerPopup.this.marker.setRoll(GuiEditMarkerPopup.this.rollField.getFloat());
          GuiEditMarkerPopup.this.close();
        }
      })).setSize(150, 20)).setI18nLabel("replaymod.gui.save", new Object[0]);
  
  public final GuiButton cancelButton = (GuiButton)((GuiButton)((GuiButton)(new GuiButton()).onClick(new Runnable() {
        public void run() {
          GuiEditMarkerPopup.this.close();
        }
      })).setSize(150, 20)).setI18nLabel("replaymod.gui.cancel", new Object[0]);
  
  public final GuiPanel buttons = (GuiPanel)((GuiPanel)(new GuiPanel())
    .setLayout((Layout)(new HorizontalLayout(HorizontalLayout.Alignment.CENTER)).setSpacing(7)))
    .addElements((LayoutData)new HorizontalLayout.Data(0.5D), new GuiElement[] { (GuiElement)this.saveButton, (GuiElement)this.cancelButton });
  
  public GuiEditMarkerPopup(ReplayHandler replayHandler, GuiContainer container, Marker marker) {
    super(container);
    this.replayHandler = replayHandler;
    this.marker = marker;
    setBackgroundColor(Colors.DARK_TRANSPARENT);
    ((GuiPanel)this.popup.setLayout((Layout)(new VerticalLayout()).setSpacing(5)))
      .addElements((LayoutData)new VerticalLayout.Data(0.5D), new GuiElement[] { (GuiElement)this.title, (GuiElement)this.inputs, (GuiElement)this.buttons });
    ((IGuiLabel)this.popup.forEach(IGuiLabel.class)).setColor(Colors.BLACK);
    this.nameField.setText(Strings.nullToEmpty(marker.getName()));
    this.timeField.setValue(marker.getTime());
    this.xField.setValue(marker.getX());
    this.yField.setValue(marker.getY());
    this.zField.setValue(marker.getZ());
    this.yawField.setValue(marker.getYaw());
    this.pitchField.setValue(marker.getPitch());
    this.rollField.setValue(marker.getRoll());
  }
  
  public void open() {
    super.open();
  }
  
  protected GuiEditMarkerPopup getThis() {
    return this;
  }
  
  public boolean typeKey(ReadablePoint mousePosition, int keyCode, char keyChar, boolean ctrlDown, boolean shiftDown) {
    if (keyCode == 1) {
      this.cancelButton.onClick();
      return true;
    } 
    return false;
  }
}
