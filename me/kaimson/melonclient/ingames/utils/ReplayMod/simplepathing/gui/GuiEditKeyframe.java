package me.kaimson.melonclient.ingames.utils.ReplayMod.simplepathing.gui;

import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.IGuiClickable;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.IGuiLabel;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Focusable;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Typeable;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.GridLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.HorizontalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.Layout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.LayoutData;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.VerticalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Colors;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Consumer;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Utils;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import com.replaymod.replaystudio.pathing.change.Change;
import com.replaymod.replaystudio.pathing.change.CombinedChange;
import com.replaymod.replaystudio.pathing.interpolation.CatmullRomSplineInterpolator;
import com.replaymod.replaystudio.pathing.interpolation.CubicSplineInterpolator;
import com.replaymod.replaystudio.pathing.interpolation.Interpolator;
import com.replaymod.replaystudio.pathing.interpolation.LinearInterpolator;
import com.replaymod.replaystudio.pathing.path.Keyframe;
import com.replaymod.replaystudio.pathing.path.Path;
import com.replaymod.replaystudio.pathing.path.PathSegment;
import com.replaymod.replaystudio.pathing.property.Property;
import java.util.Map;
import java.util.Optional;
import me.kaimson.melonclient.ingames.utils.ReplayMod.core.ReplayCore;
import me.kaimson.melonclient.ingames.utils.ReplayMod.core.SettingsRegistry;
import me.kaimson.melonclient.ingames.utils.ReplayMod.customgui.AbstractGuiContainer;
import me.kaimson.melonclient.ingames.utils.ReplayMod.customgui.AbstractGuiElement;
import me.kaimson.melonclient.ingames.utils.ReplayMod.customgui.AbstractGuiPopup;
import me.kaimson.melonclient.ingames.utils.ReplayMod.customgui.GuiButton;
import me.kaimson.melonclient.ingames.utils.ReplayMod.customgui.GuiDropdownMenu;
import me.kaimson.melonclient.ingames.utils.ReplayMod.customgui.GuiLabel;
import me.kaimson.melonclient.ingames.utils.ReplayMod.customgui.GuiNumberField;
import me.kaimson.melonclient.ingames.utils.ReplayMod.customgui.GuiPanel;
import me.kaimson.melonclient.ingames.utils.ReplayMod.customgui.GuiTooltip;
import me.kaimson.melonclient.ingames.utils.ReplayMod.simplepathing.InterpolatorType;
import me.kaimson.melonclient.ingames.utils.ReplayMod.simplepathing.ReplayModSimplePathing;
import me.kaimson.melonclient.ingames.utils.ReplayMod.simplepathing.SPTimeline;
import me.kaimson.melonclient.ingames.utils.ReplayMod.simplepathing.Setting;
import me.kaimson.melonclient.ingames.utils.ReplayMod.simplepathing.properties.CameraProperties;
import me.kaimson.melonclient.ingames.utils.ReplayMod.simplepathing.properties.ExplicitInterpolationProperty;
import me.kaimson.melonclient.ingames.utils.ReplayMod.simplepathing.properties.TimestampProperty;
import me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.ReplayModReplay;
import net.minecraft.client.resources.I18n;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class GuiEditKeyframe<T extends GuiEditKeyframe<T>> extends AbstractGuiPopup<T> implements Typeable {
  private static GuiNumberField newGuiNumberField() {
    return (GuiNumberField)((GuiNumberField)(new GuiNumberField()).setPrecision(0)).setValidateOnFocusChange(true);
  }
  
  protected static final Logger logger = LogManager.getLogger();
  
  protected final GuiPathing guiPathing;
  
  protected final long time;
  
  protected final Keyframe keyframe;
  
  protected final Path path;
  
  public final GuiLabel title = new GuiLabel();
  
  public final GuiPanel inputs = new GuiPanel();
  
  public final GuiNumberField timeMinField = (GuiNumberField)((GuiNumberField)newGuiNumberField().setSize(30, 20)).setMinValue(0);
  
  public final GuiNumberField timeSecField = (GuiNumberField)((GuiNumberField)((GuiNumberField)newGuiNumberField().setSize(20, 20)).setMinValue(0)).setMaxValue(59);
  
  public final GuiNumberField timeMSecField = (GuiNumberField)((GuiNumberField)((GuiNumberField)newGuiNumberField().setSize(30, 20)).setMinValue(0)).setMaxValue(999);
  
  public final GuiPanel timePanel = (GuiPanel)((GuiPanel)(new GuiPanel())
    .setLayout((Layout)(new HorizontalLayout(HorizontalLayout.Alignment.RIGHT)).setSpacing(3)))
    .addElements((LayoutData)new HorizontalLayout.Data(0.5D), new GuiElement[] { (GuiElement)(new GuiLabel()).setI18nText("replaymod.gui.editkeyframe.timelineposition", new Object[0]), (GuiElement)this.timeMinField, (GuiElement)(new GuiLabel())
        .setI18nText("replaymod.gui.minutes", new Object[0]), (GuiElement)this.timeSecField, (GuiElement)(new GuiLabel())
        .setI18nText("replaymod.gui.seconds", new Object[0]), (GuiElement)this.timeMSecField, (GuiElement)(new GuiLabel())
        .setI18nText("replaymod.gui.milliseconds", new Object[0]) });
  
  public final GuiButton saveButton = (GuiButton)((GuiButton)(new GuiButton()).setSize(150, 20)).setI18nLabel("replaymod.gui.save", new Object[0]);
  
  public final GuiButton cancelButton = (GuiButton)((GuiButton)((GuiButton)(new GuiButton())
    .onClick(this::close)).setSize(150, 20)).setI18nLabel("replaymod.gui.cancel", new Object[0]);
  
  public final GuiPanel buttons = (GuiPanel)((GuiPanel)(new GuiPanel())
    .setLayout((Layout)(new HorizontalLayout(HorizontalLayout.Alignment.CENTER)).setSpacing(7)))
    .addElements((LayoutData)new HorizontalLayout.Data(0.5D), new GuiElement[] { (GuiElement)this.saveButton, (GuiElement)this.cancelButton });
  
  public GuiEditKeyframe(GuiPathing gui, SPTimeline.SPPath path, long time, String type) {
    super((GuiContainer)ReplayModReplay.getInstance().getReplayHandler().getOverlay());
    setBackgroundColor(Colors.DARK_TRANSPARENT);
    ((GuiPanel)this.popup.setLayout((Layout)(new VerticalLayout()).setSpacing(10))).addElements((LayoutData)new VerticalLayout.Data(0.5D, false), new GuiElement[] { (GuiElement)this.title, (GuiElement)this.inputs, (GuiElement)this.timePanel, (GuiElement)this.buttons });
    this.guiPathing = gui;
    this.time = time;
    this.path = ReplayModSimplePathing.getInstance().getCurrentTimeline().getPath(path);
    this.keyframe = this.path.getKeyframe(time);
    Consumer<String> updateSaveButtonState = s -> (GuiButton)this.saveButton.setEnabled(canSave());
    ((GuiNumberField)this.timeMinField.setValue((time / 1000L / 60L))).onTextChanged(updateSaveButtonState);
    ((GuiNumberField)this.timeSecField.setValue((time / 1000L % 60L))).onTextChanged(updateSaveButtonState);
    ((GuiNumberField)this.timeMSecField.setValue((time % 1000L))).onTextChanged(updateSaveButtonState);
    this.title.setI18nText("replaymod.gui.editkeyframe.title." + type, new Object[0]);
    this.saveButton.onClick(() -> {
          CombinedChange combinedChange;
          Change change = save();
          long newTime = ((this.timeMinField.getInteger() * 60 + this.timeSecField.getInteger()) * 1000 + this.timeMSecField.getInteger());
          if (newTime != time) {
            combinedChange = CombinedChange.createFromApplied(new Change[] { change, ReplayModSimplePathing.getInstance().getCurrentTimeline().moveKeyframe(path, time, newTime) });
            if (ReplayModSimplePathing.getInstance().getSelectedPath() == path && ReplayModSimplePathing.getInstance().getSelectedTime() == time)
              ReplayModSimplePathing.getInstance().setSelected(path, newTime); 
          } 
          ReplayModSimplePathing.getInstance().getCurrentTimeline().getTimeline().pushChange((Change)combinedChange);
          close();
        });
  }
  
  private boolean canSave() {
    long newTime = ((this.timeMinField.getInteger() * 60 + this.timeSecField.getInteger()) * 1000 + this.timeMSecField.getInteger());
    if (newTime < 0L || newTime > this.guiPathing.timeline.getLength())
      return false; 
    if (newTime != this.keyframe.getTime() && this.path.getKeyframe(newTime) != null)
      return false; 
    return true;
  }
  
  public boolean typeKey(ReadablePoint mousePosition, int keyCode, char keyChar, boolean ctrlDown, boolean shiftDown) {
    if (keyCode == 1) {
      this.cancelButton.onClick();
      return true;
    } 
    return false;
  }
  
  public void open() {
    super.open();
  }
  
  protected abstract Change save();
  
  public static class Spectator extends GuiEditKeyframe<Spectator> {
    public Spectator(GuiPathing gui, SPTimeline.SPPath path, long keyframe) {
      super(gui, path, keyframe, "spec");
      Utils.link(new Focusable[] { (Focusable)this.timeMinField, (Focusable)this.timeSecField, (Focusable)this.timeMSecField });
      ((IGuiLabel)this.popup.forEach(IGuiLabel.class)).setColor(Colors.BLACK);
    }
    
    protected Change save() {
      return (Change)CombinedChange.createFromApplied(new Change[0]);
    }
    
    protected Spectator getThis() {
      return this;
    }
  }
  
  public static class Time extends GuiEditKeyframe<Time> {
    public final GuiNumberField timestampMinField = (GuiNumberField)((GuiNumberField)GuiEditKeyframe.newGuiNumberField().setSize(30, 20)).setMinValue(0);
    
    public final GuiNumberField timestampSecField = (GuiNumberField)((GuiNumberField)((GuiNumberField)GuiEditKeyframe.newGuiNumberField().setSize(20, 20)).setMinValue(0)).setMaxValue(59);
    
    public final GuiNumberField timestampMSecField = (GuiNumberField)((GuiNumberField)((GuiNumberField)GuiEditKeyframe.newGuiNumberField().setSize(30, 20)).setMinValue(0)).setMaxValue(999);
    
    public Time(GuiPathing gui, SPTimeline.SPPath path, long keyframe) {
      super(gui, path, keyframe, "time");
      ((GuiPanel)this.inputs.setLayout((Layout)(new HorizontalLayout(HorizontalLayout.Alignment.RIGHT)).setSpacing(3))).addElements((LayoutData)new HorizontalLayout.Data(0.5D), new GuiElement[] { (GuiElement)(new GuiLabel()).setI18nText("replaymod.gui.editkeyframe.timestamp", new Object[0]), (GuiElement)this.timestampMinField, (GuiElement)(new GuiLabel()).setI18nText("replaymod.gui.minutes", new Object[0]), (GuiElement)this.timestampSecField, (GuiElement)(new GuiLabel()).setI18nText("replaymod.gui.seconds", new Object[0]), (GuiElement)this.timestampMSecField, (GuiElement)(new GuiLabel()).setI18nText("replaymod.gui.milliseconds", new Object[0]) });
      this.keyframe.getValue((Property)TimestampProperty.PROPERTY).ifPresent(time -> {
            this.timestampMinField.setValue(time.intValue() / 1000 / 60);
            this.timestampSecField.setValue(time.intValue() / 1000 % 60);
            this.timestampMSecField.setValue(time.intValue() % 1000);
          });
      Utils.link(new Focusable[] { (Focusable)this.timestampMinField, (Focusable)this.timestampSecField, (Focusable)this.timestampMSecField, (Focusable)this.timeMinField, (Focusable)this.timeSecField, (Focusable)this.timeMSecField });
      ((IGuiLabel)this.popup.forEach(IGuiLabel.class)).setColor(Colors.BLACK);
    }
    
    protected Change save() {
      int time = (this.timestampMinField.getInteger() * 60 + this.timestampSecField.getInteger()) * 1000 + this.timestampMSecField.getInteger();
      return ReplayModSimplePathing.getInstance().getCurrentTimeline().updateTimeKeyframe(this.keyframe.getTime(), time);
    }
    
    protected Time getThis() {
      return this;
    }
  }
  
  public static class Position extends GuiEditKeyframe<Position> {
    public final GuiNumberField xField = (GuiNumberField)((GuiNumberField)GuiEditKeyframe.newGuiNumberField().setSize(60, 20)).setPrecision(5);
    
    public final GuiNumberField yField = (GuiNumberField)((GuiNumberField)GuiEditKeyframe.newGuiNumberField().setSize(60, 20)).setPrecision(5);
    
    public final GuiNumberField zField = (GuiNumberField)((GuiNumberField)GuiEditKeyframe.newGuiNumberField().setSize(60, 20)).setPrecision(5);
    
    public final GuiNumberField yawField = (GuiNumberField)((GuiNumberField)GuiEditKeyframe.newGuiNumberField().setSize(60, 20)).setPrecision(5);
    
    public final GuiNumberField pitchField = (GuiNumberField)((GuiNumberField)GuiEditKeyframe.newGuiNumberField().setSize(60, 20)).setPrecision(5);
    
    public final GuiNumberField rollField = (GuiNumberField)((GuiNumberField)GuiEditKeyframe.newGuiNumberField().setSize(60, 20)).setPrecision(5);
    
    public final InterpolationPanel interpolationPanel = new InterpolationPanel();
    
    public Position(GuiPathing gui, SPTimeline.SPPath path, long keyframe) {
      super(gui, path, keyframe, "pos");
      GuiPanel positionInputs = (GuiPanel)((GuiPanel)(new GuiPanel()).setLayout((Layout)(new GridLayout()).setCellsEqualSize(false).setColumns(4).setSpacingX(3).setSpacingY(5))).addElements((LayoutData)new GridLayout.Data(1.0D, 0.5D), new GuiElement[] { 
            (GuiElement)(new GuiLabel()).setI18nText("replaymod.gui.editkeyframe.xpos", new Object[0]), (GuiElement)this.xField, (GuiElement)(new GuiLabel()).setI18nText("replaymod.gui.editkeyframe.camyaw", new Object[0]), (GuiElement)this.yawField, (GuiElement)(new GuiLabel()).setI18nText("replaymod.gui.editkeyframe.ypos", new Object[0]), (GuiElement)this.yField, (GuiElement)(new GuiLabel()).setI18nText("replaymod.gui.editkeyframe.campitch", new Object[0]), (GuiElement)this.pitchField, (GuiElement)(new GuiLabel()).setI18nText("replaymod.gui.editkeyframe.zpos", new Object[0]), (GuiElement)this.zField, 
            (GuiElement)(new GuiLabel()).setI18nText("replaymod.gui.editkeyframe.camroll", new Object[0]), (GuiElement)this.rollField });
      ((GuiPanel)this.inputs.setLayout((Layout)(new VerticalLayout()).setSpacing(10))).addElements((LayoutData)new VerticalLayout.Data(0.5D, false), new GuiElement[] { (GuiElement)positionInputs, (GuiElement)this.interpolationPanel });
      this.keyframe.getValue((Property)CameraProperties.POSITION).ifPresent(pos -> {
            this.xField.setValue(((Double)pos.getLeft()).doubleValue());
            this.yField.setValue(((Double)pos.getMiddle()).doubleValue());
            this.zField.setValue(((Double)pos.getRight()).doubleValue());
          });
      this.keyframe.getValue((Property)CameraProperties.ROTATION).ifPresent(rot -> {
            this.yawField.setValue(((Float)rot.getLeft()).floatValue());
            this.pitchField.setValue(((Float)rot.getMiddle()).floatValue());
            this.rollField.setValue(((Float)rot.getRight()).floatValue());
          });
      Utils.link(new Focusable[] { (Focusable)this.xField, (Focusable)this.yField, (Focusable)this.zField, (Focusable)this.yawField, (Focusable)this.pitchField, (Focusable)this.rollField, (Focusable)this.timeMinField, (Focusable)this.timeSecField, (Focusable)this.timeMSecField });
      ((IGuiLabel)this.popup.forEach(IGuiLabel.class)).setColor(Colors.BLACK);
    }
    
    protected Change save() {
      SPTimeline timeline = ReplayModSimplePathing.getInstance().getCurrentTimeline();
      Change positionChange = timeline.updatePositionKeyframe(this.time, this.xField
          .getDouble(), this.yField.getDouble(), this.zField.getDouble(), this.yawField
          .getFloat(), this.pitchField.getFloat(), this.rollField.getFloat());
      if (this.interpolationPanel.getSettingsPanel() == null)
        return positionChange; 
      Interpolator interpolator = (Interpolator)this.interpolationPanel.getSettingsPanel().createInterpolator();
      if (this.interpolationPanel.getInterpolatorType() == InterpolatorType.DEFAULT)
        return (Change)CombinedChange.createFromApplied(new Change[] { positionChange, timeline.setInterpolatorToDefault(this.time), timeline
              .setDefaultInterpolator(interpolator) }); 
      return (Change)CombinedChange.createFromApplied(new Change[] { positionChange, timeline.setInterpolator(this.time, interpolator) });
    }
    
    protected Position getThis() {
      return this;
    }
    
    public class InterpolationPanel extends AbstractGuiContainer<InterpolationPanel> {
      private SettingsPanel settingsPanel;
      
      private GuiDropdownMenu<InterpolatorType> dropdown;
      
      public SettingsPanel getSettingsPanel() {
        return this.settingsPanel;
      }
      
      public InterpolationPanel() {
        setLayout((Layout)new VerticalLayout());
        this
          
          .dropdown = (GuiDropdownMenu<InterpolatorType>)((GuiDropdownMenu)((GuiDropdownMenu)((GuiDropdownMenu)(new GuiDropdownMenu()).setToString(s -> I18n.format(s.getI18nName(), new Object[0]))).setValues((Object[])InterpolatorType.values())).setHeight(20)).onSelection(i -> setSettingsPanel((InterpolatorType)this.dropdown.getSelectedValue()));
        for (Map.Entry<InterpolatorType, IGuiClickable> e : (Iterable<Map.Entry<InterpolatorType, IGuiClickable>>)this.dropdown.getDropdownEntries().entrySet())
          ((IGuiClickable)e.getValue()).setTooltip((GuiElement)(new GuiTooltip()).setI18nText(((InterpolatorType)e.getKey()).getI18nDescription(), new Object[0])); 
        GuiPanel dropdownPanel = (GuiPanel)((GuiPanel)(new GuiPanel()).setLayout((Layout)(new GridLayout()).setCellsEqualSize(false).setColumns(2).setSpacingX(3).setSpacingY(5))).addElements((LayoutData)new GridLayout.Data(1.0D, 0.5D), new GuiElement[] { (GuiElement)(new GuiLabel()).setI18nText("replaymod.gui.editkeyframe.interpolator", new Object[0]), (GuiElement)this.dropdown });
        addElements((LayoutData)new VerticalLayout.Data(0.5D, false), new GuiElement[] { (GuiElement)dropdownPanel });
        Optional<PathSegment> segment = GuiEditKeyframe.Position.this.path.getSegments().stream().filter(s -> (s.getStartKeyframe() == GuiEditKeyframe.Position.this.keyframe)).findFirst();
        if (segment.isPresent()) {
          Interpolator interpolator = ((PathSegment)segment.get()).getInterpolator();
          InterpolatorType type = InterpolatorType.fromClass(interpolator.getClass());
          if (GuiEditKeyframe.Position.this.keyframe.getValue((Property)ExplicitInterpolationProperty.PROPERTY).isPresent()) {
            this.dropdown.setSelected(type);
          } else {
            setSettingsPanel(InterpolatorType.DEFAULT);
            type = InterpolatorType.DEFAULT;
          } 
          if (getInterpolatorTypeNoDefault(type).getInterpolatorClass().isInstance(interpolator))
            this.settingsPanel.loadSettings(interpolator); 
        } else {
          this.dropdown.setDisabled();
        } 
      }
      
      public void setSettingsPanel(InterpolatorType type) {
        removeElement((GuiElement)this.settingsPanel);
        switch (getInterpolatorTypeNoDefault(type)) {
          case CATMULL_ROM:
            this.settingsPanel = new CatmullRomSettingsPanel();
            break;
          case CUBIC:
            this.settingsPanel = new CubicSettingsPanel();
            break;
          case LINEAR:
            this.settingsPanel = new LinearSettingsPanel();
            break;
        } 
        addElements((LayoutData)new GridLayout.Data(0.5D, 0.5D), new GuiElement[] { (GuiElement)this.settingsPanel });
      }
      
      protected InterpolatorType getInterpolatorTypeNoDefault(InterpolatorType interpolatorType) {
        if (interpolatorType == InterpolatorType.DEFAULT || interpolatorType == null) {
          InterpolatorType defaultType = InterpolatorType.fromString(
              (String)ReplayCore.getInstance().getSettingsRegistry().get((SettingsRegistry.SettingKey)Setting.DEFAULT_INTERPOLATION));
          return defaultType;
        } 
        return interpolatorType;
      }
      
      public InterpolatorType getInterpolatorType() {
        return (InterpolatorType)this.dropdown.getSelectedValue();
      }
      
      protected InterpolationPanel getThis() {
        return this;
      }
      
      public abstract class SettingsPanel<I extends Interpolator, T extends SettingsPanel<I, T>> extends AbstractGuiContainer<T> {
        public abstract void loadSettings(I param3I);
        
        public abstract I createInterpolator();
      }
      
      public class CatmullRomSettingsPanel extends SettingsPanel<CatmullRomSplineInterpolator, CatmullRomSettingsPanel> {
        public final GuiLabel alphaLabel;
        
        public final GuiNumberField alphaField;
        
        public CatmullRomSettingsPanel() {
          this
            .alphaLabel = (GuiLabel)((GuiLabel)(new GuiLabel()).setColor(Colors.BLACK)).setI18nText("replaymod.gui.editkeyframe.interpolator.catmullrom.alpha", new Object[0]);
          this
            .alphaField = (GuiNumberField)((GuiNumberField)((GuiNumberField)((GuiNumberField)(new GuiNumberField()).setSize(100, 20)).setPrecision(5)).setMinValue(0)).setValidateOnFocusChange(true);
          setLayout((Layout)new HorizontalLayout(HorizontalLayout.Alignment.CENTER));
          addElements((LayoutData)new HorizontalLayout.Data(0.5D), new GuiElement[] { (GuiElement)this.alphaLabel, (GuiElement)this.alphaField });
        }
        
        public void loadSettings(CatmullRomSplineInterpolator interpolator) {
          this.alphaField.setValue(interpolator.getAlpha());
        }
        
        public CatmullRomSplineInterpolator createInterpolator() {
          return new CatmullRomSplineInterpolator(this.alphaField.getDouble());
        }
        
        protected CatmullRomSettingsPanel getThis() {
          return this;
        }
      }
      
      public class CubicSettingsPanel extends SettingsPanel<CubicSplineInterpolator, CubicSettingsPanel> {
        public void loadSettings(CubicSplineInterpolator interpolator) {}
        
        public CubicSplineInterpolator createInterpolator() {
          return new CubicSplineInterpolator();
        }
        
        protected CubicSettingsPanel getThis() {
          return this;
        }
      }
      
      public class LinearSettingsPanel extends SettingsPanel<LinearInterpolator, LinearSettingsPanel> {
        public void loadSettings(LinearInterpolator interpolator) {}
        
        public LinearInterpolator createInterpolator() {
          return new LinearInterpolator();
        }
        
        protected LinearSettingsPanel getThis() {
          return this;
        }
      }
    }
    
    public abstract class SettingsPanel<I extends Interpolator, T extends InterpolationPanel.SettingsPanel<I, T>> extends AbstractGuiContainer<T> {
      public abstract void loadSettings(I param2I);
      
      public abstract I createInterpolator();
    }
  }
}
