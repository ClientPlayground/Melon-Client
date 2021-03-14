package net.optifine.shaders.uniform;

import java.util.Arrays;
import net.optifine.shaders.Shaders;
import org.lwjgl.opengl.ARBShaderObjects;

public abstract class ShaderUniformBase {
  private String name;
  
  private int program = 0;
  
  private int[] locations = new int[] { -1 };
  
  private static final int LOCATION_UNDEFINED = -1;
  
  private static final int LOCATION_UNKNOWN = -2147483648;
  
  public ShaderUniformBase(String name) {
    this.name = name;
  }
  
  public void setProgram(int program) {
    if (this.program != program) {
      this.program = program;
      expandLocations();
      onProgramSet(program);
    } 
  }
  
  private void expandLocations() {
    if (this.program >= this.locations.length) {
      int[] aint = new int[this.program * 2];
      Arrays.fill(aint, -2147483648);
      System.arraycopy(this.locations, 0, aint, 0, this.locations.length);
      this.locations = aint;
    } 
  }
  
  protected abstract void onProgramSet(int paramInt);
  
  public String getName() {
    return this.name;
  }
  
  public int getProgram() {
    return this.program;
  }
  
  public int getLocation() {
    if (this.program <= 0)
      return -1; 
    int i = this.locations[this.program];
    if (i == Integer.MIN_VALUE) {
      i = ARBShaderObjects.glGetUniformLocationARB(this.program, this.name);
      this.locations[this.program] = i;
    } 
    return i;
  }
  
  public boolean isDefined() {
    return (getLocation() >= 0);
  }
  
  public void disable() {
    this.locations[this.program] = -1;
  }
  
  public void reset() {
    this.program = 0;
    this.locations = new int[] { -1 };
    resetValue();
  }
  
  protected abstract void resetValue();
  
  protected void checkGLError() {
    if (Shaders.checkGLError(this.name) != 0)
      disable(); 
  }
  
  public String toString() {
    return this.name;
  }
}
