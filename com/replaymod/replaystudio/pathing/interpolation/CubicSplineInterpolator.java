package com.replaymod.replaystudio.pathing.interpolation;

public class CubicSplineInterpolator extends PolynomialSplineInterpolator {
  public CubicSplineInterpolator() {
    super(3);
  }
  
  protected void fillMatrix(double[][] matrix, double[] xs, double[] ys, int num, InterpolationParameters params) {
    int row = 0;
    if (params != null) {
      ys[0] = params.getValue();
      double x = xs[0];
      matrix[row][0] = 3.0D * x * x;
      matrix[row][1] = 2.0D * x;
      matrix[row][2] = 1.0D;
      matrix[row][num * 4] = params.getVelocity();
      row++;
      matrix[row][0] = 6.0D * x;
      matrix[row][1] = 2.0D;
      matrix[row][num * 4] = params.getAcceleration();
      row++;
    } else {
      matrix[row][0] = 6.0D * xs[0];
      matrix[row][1] = 2.0D;
      row++;
      matrix[row][(num - 1) * 4] = 6.0D * xs[xs.length - 1];
      matrix[row][(num - 1) * 4 + 1] = 2.0D;
      row++;
    } 
    for (int i = 0; i < num; i++) {
      double x = xs[i];
      matrix[row][i * 4] = x * x * x;
      matrix[row][i * 4 + 1] = x * x;
      matrix[row][i * 4 + 2] = x;
      matrix[row][i * 4 + 3] = 1.0D;
      matrix[row][num * 4] = ys[i];
      row++;
      x = xs[i + 1];
      matrix[row][i * 4] = x * x * x;
      matrix[row][i * 4 + 1] = x * x;
      matrix[row][i * 4 + 2] = x;
      matrix[row][i * 4 + 3] = 1.0D;
      matrix[row][num * 4] = ys[i + 1];
      row++;
      if (i < num - 1) {
        x = xs[i + 1];
        matrix[row][i * 4 + 4] = 3.0D * x * x;
        matrix[row][i * 4] = -(3.0D * x * x);
        matrix[row][i * 4 + 5] = 2.0D * x;
        matrix[row][i * 4 + 1] = -(2.0D * x);
        matrix[row][i * 4 + 6] = 1.0D;
        matrix[row][i * 4 + 2] = -1.0D;
        row++;
      } 
      if (i < num - 1) {
        x = xs[i + 1];
        matrix[row][i * 4 + 4] = 6.0D * x;
        matrix[row][i * 4] = -(6.0D * x);
        matrix[row][i * 4 + 5] = 2.0D;
        matrix[row][i * 4 + 1] = -2.0D;
        row++;
      } 
    } 
  }
}
