package net.optifine.shaders.uniform;

import org.lwjgl.opengl.ARBShaderObjects;

public class ShaderUniform1i extends ShaderUniformBase {
  private int[] programValues;
  
  private static final int VALUE_UNKNOWN = -2147483648;
  
  public ShaderUniform1i(String name) {
    super(name);
    resetValue();
  }
  
  public void setValue(int valueNew) {
    int i = getProgram();
    int j = this.programValues[i];
    if (valueNew != j) {
      this.programValues[i] = valueNew;
      int k = getLocation();
      if (k >= 0) {
        ARBShaderObjects.glUniform1iARB(k, valueNew);
        checkGLError();
      } 
    } 
  }
  
  public int getValue() {
    int i = getProgram();
    int j = this.programValues[i];
    return j;
  }
  
  protected void onProgramSet(int program) {
    if (program >= this.programValues.length) {
      int[] aint = this.programValues;
      int[] aint1 = new int[program + 10];
      System.arraycopy(aint, 0, aint1, 0, aint.length);
      for (int i = aint.length; i < aint1.length; i++)
        aint1[i] = Integer.MIN_VALUE; 
      this.programValues = aint1;
    } 
  }
  
  protected void resetValue() {
    this.programValues = new int[] { Integer.MIN_VALUE };
  }
}
