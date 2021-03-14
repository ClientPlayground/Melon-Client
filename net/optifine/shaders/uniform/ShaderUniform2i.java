package net.optifine.shaders.uniform;

import org.lwjgl.opengl.ARBShaderObjects;

public class ShaderUniform2i extends ShaderUniformBase {
  private int[][] programValues;
  
  private static final int VALUE_UNKNOWN = -2147483648;
  
  public ShaderUniform2i(String name) {
    super(name);
    resetValue();
  }
  
  public void setValue(int v0, int v1) {
    int i = getProgram();
    int[] aint = this.programValues[i];
    if (aint[0] != v0 || aint[1] != v1) {
      aint[0] = v0;
      aint[1] = v1;
      int j = getLocation();
      if (j >= 0) {
        ARBShaderObjects.glUniform2iARB(j, v0, v1);
        checkGLError();
      } 
    } 
  }
  
  public int[] getValue() {
    int i = getProgram();
    int[] aint = this.programValues[i];
    return aint;
  }
  
  protected void onProgramSet(int program) {
    if (program >= this.programValues.length) {
      int[][] aint = this.programValues;
      int[][] aint1 = new int[program + 10][];
      System.arraycopy(aint, 0, aint1, 0, aint.length);
      this.programValues = aint1;
    } 
    if (this.programValues[program] == null) {
      (new int[2])[0] = Integer.MIN_VALUE;
      (new int[2])[1] = Integer.MIN_VALUE;
      this.programValues[program] = new int[2];
    } 
  }
  
  protected void resetValue() {
    this.programValues = new int[][] { { Integer.MIN_VALUE, Integer.MIN_VALUE } };
  }
}
