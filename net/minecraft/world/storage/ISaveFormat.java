package net.minecraft.world.storage;

import java.util.List;
import net.minecraft.client.AnvilConverterException;
import net.minecraft.util.IProgressUpdate;

public interface ISaveFormat {
  String getName();
  
  ISaveHandler getSaveLoader(String paramString, boolean paramBoolean);
  
  List<SaveFormatComparator> getSaveList() throws AnvilConverterException;
  
  void flushCache();
  
  WorldInfo getWorldInfo(String paramString);
  
  boolean isNewLevelIdAcceptable(String paramString);
  
  boolean deleteWorldDirectory(String paramString);
  
  void renameWorld(String paramString1, String paramString2);
  
  boolean isConvertible(String paramString);
  
  boolean isOldMapFormat(String paramString);
  
  boolean convertMapFormat(String paramString, IProgressUpdate paramIProgressUpdate);
  
  boolean canLoadWorld(String paramString);
}
