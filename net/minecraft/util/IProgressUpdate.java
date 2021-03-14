package net.minecraft.util;

public interface IProgressUpdate {
  void displaySavingString(String paramString);
  
  void resetProgressAndMessage(String paramString);
  
  void displayLoadingString(String paramString);
  
  void setLoadingProgress(int paramInt);
  
  void setDoneWorking();
}
