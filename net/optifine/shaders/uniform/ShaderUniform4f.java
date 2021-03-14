package net.optifine.shaders.uniform;

import org.lwjgl.opengl.ARBShaderObjects;

public class ShaderUniform4f extends ShaderUniformBase {
  private float[][] programValues;
  
  private static final float VALUE_UNKNOWN = -3.4028235E38F;
  
  public ShaderUniform4f(String name) {
    super(name);
    resetValue();
  }
  
  public void setValue(float v0, float v1, float v2, float v3) {
    int i = getProgram();
    float[] afloat = this.programValues[i];
    if (afloat[0] != v0 || afloat[1] != v1 || afloat[2] != v2 || afloat[3] != v3) {
      afloat[0] = v0;
      afloat[1] = v1;
      afloat[2] = v2;
      afloat[3] = v3;
      int j = getLocation();
      if (j >= 0) {
        ARBShaderObjects.glUniform4fARB(j, v0, v1, v2, v3);
        checkGLError();
      } 
    } 
  }
  
  public float[] getValue() {
    int i = getProgram();
    float[] afloat = this.programValues[i];
    return afloat;
  }
  
  protected void onProgramSet(int program) {
    if (program >= this.programValues.length) {
      float[][] afloat = this.programValues;
      float[][] afloat1 = new float[program + 10][];
      System.arraycopy(afloat, 0, afloat1, 0, afloat.length);
      this.programValues = afloat1;
    } 
    if (this.programValues[program] == null) {
      (new float[4])[0] = -3.4028235E38F;
      (new float[4])[1] = -3.4028235E38F;
      (new float[4])[2] = -3.4028235E38F;
      (new float[4])[3] = -3.4028235E38F;
      this.programValues[program] = new float[4];
    } 
  }
  
  protected void resetValue() {
    this.programValues = new float[][] { { -3.4028235E38F, -3.4028235E38F, -3.4028235E38F, -3.4028235E38F } };
  }
}
