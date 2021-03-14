package com.replaymod.replaystudio.pathing.interpolation;

import com.replaymod.replaystudio.pathing.path.Keyframe;
import com.replaymod.replaystudio.pathing.path.PathSegment;
import com.replaymod.replaystudio.pathing.property.Property;
import com.replaymod.replaystudio.pathing.property.PropertyPart;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public abstract class PolynomialSplineInterpolator extends AbstractInterpolator {
  private final int degree;
  
  private Map<Property<?>, Set<Keyframe>> framesToProperty = new HashMap<>();
  
  private Map<PropertyPart, Polynomials> polynomials = new HashMap<>();
  
  protected PolynomialSplineInterpolator(int degree) {
    this.degree = degree;
  }
  
  protected Map<PropertyPart, InterpolationParameters> bakeInterpolation(Map<PropertyPart, InterpolationParameters> parameters) {
    this.framesToProperty.clear();
    for (PathSegment segment : getSegments()) {
      for (Property property : getKeyframeProperties()) {
        if (segment.getStartKeyframe().getValue(property).isPresent())
          addToMap(this.framesToProperty, property, segment.getStartKeyframe()); 
        if (segment.getEndKeyframe().getValue(property).isPresent())
          addToMap(this.framesToProperty, property, segment.getEndKeyframe()); 
      } 
    } 
    this.polynomials.clear();
    parameters = new HashMap<>(parameters);
    for (Map.Entry<Property<?>, Set<Keyframe>> entry : this.framesToProperty.entrySet())
      prepareProperty(entry.getKey(), entry.getValue(), parameters); 
    return parameters;
  }
  
  private <U> void prepareProperty(Property<U> property, Set<Keyframe> keyframes, Map<PropertyPart, InterpolationParameters> parameters) {
    for (PropertyPart<U> part : (Iterable<PropertyPart<U>>)property.getParts()) {
      if (part.isInterpolatable()) {
        double[] time = new double[keyframes.size()];
        double[] values = new double[keyframes.size()];
        int i = 0;
        for (Keyframe keyframe : keyframes) {
          time[i] = keyframe.getTime();
          values[i++] = part.toDouble(keyframe.getValue(property).get());
        } 
        Polynomials polynomials = calcPolynomials(part, time, values, parameters.get(part));
        double lastTime = time[time.length - 1];
        Polynomial lastPolynomial = polynomials.polynomials[polynomials.polynomials.length - 1];
        double lastValue = lastPolynomial.eval(lastTime) + polynomials.yOffset;
        double lastVelocity = (lastPolynomial = lastPolynomial.derivative()).eval(lastTime);
        double lastAcceleration = lastPolynomial.derivative().eval(lastTime);
        parameters.put(part, new InterpolationParameters(lastValue, lastVelocity, lastAcceleration));
        this.polynomials.put(part, polynomials);
      } 
    } 
  }
  
  private void addToMap(Map<Property<?>, Set<Keyframe>> map, Property<?> property, Keyframe keyframe) {
    Set<Keyframe> set = map.get(property);
    if (set == null)
      map.put(property, set = new LinkedHashSet<>()); 
    set.add(keyframe);
  }
  
  protected <U> Polynomials calcPolynomials(PropertyPart<U> part, double[] xs, double[] ys, InterpolationParameters params) {
    double yOffset;
    int unknowns = this.degree + 1;
    int num = xs.length - 1;
    if (num == 0)
      return new Polynomials(0.0D, new Polynomial[] { new Polynomial(new double[] { ys[0] }) }); 
    for (int i = 0; i < xs.length; i++)
      xs[i] = xs[i] / 1000.0D; 
    if (Double.isNaN(part.getUpperBound())) {
      double total = 0.0D;
      for (double y : ys)
        total += y; 
      yOffset = total / ys.length;
      for (int k = 0; k < ys.length; k++)
        ys[k] = ys[k] - yOffset; 
      if (params != null)
        params = new InterpolationParameters(params.getValue() - yOffset, params.getVelocity(), params.getAcceleration()); 
    } else {
      double bound = part.getUpperBound();
      double halfBound = bound / 2.0D;
      double firstValue = (params != null) ? params.getValue() : ys[0];
      int offset = (int)Math.floor(firstValue / bound);
      double lastValue = mod(firstValue, bound);
      for (int k = 1; k < ys.length; k++) {
        double value = mod(ys[k], bound);
        if (Math.abs(value - lastValue) > halfBound)
          if (lastValue < halfBound) {
            offset--;
          } else {
            offset++;
          }  
        ys[k] = value + offset * bound;
        lastValue = value;
      } 
      yOffset = 0.0D;
    } 
    double[][] matrix = new double[num * unknowns][num * unknowns + 1];
    fillMatrix(matrix, xs, ys, num, params);
    solveMatrix(matrix);
    Polynomial[] polynomials = new Polynomial[num];
    for (int j = 0; j < num; j++) {
      double[] coefficients = new double[this.degree + 1];
      for (int k = 0; k <= this.degree; k++)
        coefficients[k] = matrix[j * unknowns + k][num * unknowns]; 
      polynomials[j] = new Polynomial(coefficients);
    } 
    return new Polynomials(yOffset, polynomials);
  }
  
  private double mod(double val, double m) {
    double off = Math.floor(val / m);
    return val - off * m;
  }
  
  protected abstract void fillMatrix(double[][] paramArrayOfdouble, double[] paramArrayOfdouble1, double[] paramArrayOfdouble2, int paramInt, InterpolationParameters paramInterpolationParameters);
  
  protected static void solveMatrix(double[][] matrix) {
    int i;
    for (i = 0; i < matrix.length; i++) {
      if (matrix[i][i] == 0.0D)
        for (int k = i + 1; k < matrix.length; k++) {
          if (matrix[k][i] != 0.0D) {
            double[] s = matrix[k];
            matrix[k] = matrix[i];
            matrix[i] = s;
            break;
          } 
        }  
      double factor = matrix[i][i];
      if (factor != 1.0D) {
        matrix[i][i] = 1.0D;
        for (int k = i + 1; k < (matrix[i]).length; k++)
          matrix[i][k] = matrix[i][k] / factor; 
      } 
      for (int j = i + 1; j < matrix.length; j++) {
        factor = matrix[j][i];
        if (factor != 0.0D) {
          matrix[j][i] = 0.0D;
          for (int k = i + 1; k < (matrix[j]).length; k++)
            matrix[j][k] = matrix[j][k] - matrix[i][k] * factor; 
        } 
      } 
    } 
    for (i = matrix.length - 1; i >= 0; i--) {
      for (int j = i - 1; j >= 0; j--) {
        if (matrix[j][i] != 0.0D) {
          int k = (matrix[j]).length - 1;
          matrix[j][k] = matrix[j][k] - matrix[j][i] / matrix[i][i] * matrix[i][k];
          matrix[j][i] = 0.0D;
        } 
      } 
    } 
  }
  
  public <T> Optional<T> getValue(Property<T> property, long time) {
    Set<Keyframe> kfSet = this.framesToProperty.get(property);
    if (kfSet == null)
      return Optional.empty(); 
    Keyframe kfBefore = null, kfAfter = null;
    int index = 0;
    for (Keyframe keyframe : kfSet) {
      if (keyframe.getTime() == time)
        return keyframe.getValue(property); 
      if (keyframe.getTime() < time) {
        kfBefore = keyframe;
      } else if (keyframe.getTime() > time) {
        kfAfter = keyframe;
        index--;
        break;
      } 
      index++;
    } 
    if (kfBefore == null || kfAfter == null)
      return Optional.empty(); 
    T interpolated = kfBefore.getValue(property).get();
    for (PropertyPart<T> part : (Iterable<PropertyPart<T>>)property.getParts()) {
      if (part.isInterpolatable()) {
        double value = ((Polynomials)this.polynomials.get(part)).eval(time, index);
        if (!Double.isNaN(part.getUpperBound()))
          value = mod(value, part.getUpperBound()); 
        interpolated = (T)part.fromDouble(interpolated, value);
      } 
    } 
    return Optional.of(interpolated);
  }
  
  private static class Polynomials {
    private final double yOffset;
    
    private final PolynomialSplineInterpolator.Polynomial[] polynomials;
    
    private Polynomials(double yOffset, PolynomialSplineInterpolator.Polynomial[] polynomials) {
      this.yOffset = yOffset;
      this.polynomials = polynomials;
    }
    
    public double eval(double time, int index) {
      return this.polynomials[index].eval(time / 1000.0D) + this.yOffset;
    }
  }
  
  public static class Polynomial {
    public final double[] coefficients;
    
    public Polynomial(double[] coefficients) {
      this.coefficients = coefficients;
    }
    
    public double eval(double at) {
      double val = 0.0D;
      for (double coefficient : this.coefficients)
        val = val * at + coefficient; 
      return val;
    }
    
    public Polynomial derivative() {
      if (this.coefficients.length == 0)
        return this; 
      Polynomial derived = new Polynomial(new double[this.coefficients.length - 1]);
      for (int i = 0; i < this.coefficients.length - 1; i++)
        derived.coefficients[i] = this.coefficients[i] * (this.coefficients.length - 1 - i); 
      return derived;
    }
  }
}
