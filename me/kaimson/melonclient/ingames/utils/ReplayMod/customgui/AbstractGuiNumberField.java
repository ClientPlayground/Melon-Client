package me.kaimson.melonclient.ingames.utils.ReplayMod.customgui;

import com.google.common.base.Preconditions;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.IGuiNumberField;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.IGuiTextField;
import java.util.Locale;
import java.util.regex.Pattern;

public abstract class AbstractGuiNumberField<T extends AbstractGuiNumberField<T>> extends AbstractGuiTextField<T> implements IGuiNumberField<T> {
  private int precision;
  
  private volatile Pattern precisionPattern;
  
  private Double minValue;
  
  private Double maxValue;
  
  private boolean validateOnFocusChange = false;
  
  public AbstractGuiNumberField() {
    setValue(0);
  }
  
  public AbstractGuiNumberField(GuiContainer container) {
    super(container);
    setValue(0);
  }
  
  public T setText(String text) {
    if (!isTextValid(text, !this.validateOnFocusChange))
      throw new IllegalArgumentException(text + " is not a valid number!"); 
    return super.setText(text);
  }
  
  public T setValidateOnFocusChange(boolean validateOnFocusChange) {
    this.validateOnFocusChange = validateOnFocusChange;
    return getThis();
  }
  
  private boolean isSemiZero(String text) {
    return (text.isEmpty() || "-".equals(text));
  }
  
  private boolean isTextValid(String text, boolean validateRange) {
    if (this.validateOnFocusChange && isSemiZero(text))
      return (!validateRange || valueInRange(0.0D)); 
    try {
      if (this.precision == 0) {
        int i = Integer.parseInt(text);
        return (!validateRange || valueInRange(i));
      } 
      double val = Double.parseDouble(text);
      return (!validateRange || (valueInRange(val) && this.precisionPattern.matcher(text).matches()));
    } catch (NumberFormatException var5) {
      return false;
    } 
  }
  
  private boolean valueInRange(double value) {
    return ((this.minValue == null || value >= this.minValue.doubleValue()) && (this.maxValue == null || value <= this.maxValue.doubleValue()));
  }
  
  protected void onTextChanged(String from) {
    if (isTextValid(getText(), !this.validateOnFocusChange)) {
      super.onTextChanged(from);
    } else {
      setText(from);
    } 
  }
  
  public byte getByte() {
    return (this.validateOnFocusChange && isSemiZero(getText())) ? 0 : Byte.parseByte(getText());
  }
  
  public short getShort() {
    return (this.validateOnFocusChange && isSemiZero(getText())) ? 0 : Short.parseShort(getText());
  }
  
  public int getInteger() {
    return (this.validateOnFocusChange && isSemiZero(getText())) ? 0 : Integer.parseInt(getText());
  }
  
  public long getLong() {
    return (this.validateOnFocusChange && isSemiZero(getText())) ? 0L : Long.parseLong(getText());
  }
  
  public float getFloat() {
    return (this.validateOnFocusChange && isSemiZero(getText())) ? 0.0F : Float.parseFloat(getText());
  }
  
  public double getDouble() {
    return (this.validateOnFocusChange && isSemiZero(getText())) ? 0.0D : Double.parseDouble(getText());
  }
  
  public T setValue(int value) {
    setText(Integer.toString(value));
    return getThis();
  }
  
  public T setValue(double value) {
    setText(String.format(Locale.ROOT, "%." + this.precision + "f", new Object[] { Double.valueOf(value) }));
    return getThis();
  }
  
  public T setPrecision(int precision) {
    Preconditions.checkArgument((precision >= 0), "precision must not be negative");
    this.precisionPattern = Pattern.compile(String.format("-?[0-9]*+((\\.[0-9]{0,%d})?)||(\\.)?", new Object[] { Integer.valueOf(precision) }));
    this.precision = precision;
    return getThis();
  }
  
  public T setMinValue(Double minValue) {
    this.minValue = minValue;
    return getThis();
  }
  
  public T setMaxValue(Double maxValue) {
    this.maxValue = maxValue;
    return getThis();
  }
  
  public T setMinValue(int minValue) {
    return setMinValue(Double.valueOf(minValue));
  }
  
  public T setMaxValue(int maxValue) {
    return setMaxValue(Double.valueOf(maxValue));
  }
  
  private double clampToBounds() {
    double d = getDouble();
    if (getMinValue() != null && d < getMinValue().doubleValue())
      return getMinValue().doubleValue(); 
    return (getMaxValue() != null && d > getMaxValue().doubleValue()) ? getMaxValue().doubleValue() : d;
  }
  
  protected void onFocusChanged(boolean focused) {
    setValue(clampToBounds());
    super.onFocusChanged(focused);
  }
  
  public Double getMinValue() {
    return this.minValue;
  }
  
  public Double getMaxValue() {
    return this.maxValue;
  }
}
