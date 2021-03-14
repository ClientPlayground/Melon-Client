package net.optifine.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.src.Config;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.optifine.reflect.Reflector;
import net.optifine.reflect.ReflectorClass;
import net.optifine.reflect.ReflectorField;

public class ChunkUtils {
  private static ReflectorClass chunkClass = new ReflectorClass(Chunk.class);
  
  private static ReflectorField fieldHasEntities = findFieldHasEntities();
  
  private static ReflectorField fieldPrecipitationHeightMap = new ReflectorField(chunkClass, int[].class, 0);
  
  public static boolean hasEntities(Chunk chunk) {
    return Reflector.getFieldValueBoolean(chunk, fieldHasEntities, true);
  }
  
  public static int getPrecipitationHeight(Chunk chunk, BlockPos pos) {
    int[] aint = (int[])Reflector.getFieldValue(chunk, fieldPrecipitationHeightMap);
    if (aint != null && aint.length == 256) {
      int i = pos.getX() & 0xF;
      int j = pos.getZ() & 0xF;
      int k = i | j << 4;
      int l = aint[k];
      if (l >= 0)
        return l; 
      BlockPos blockpos = chunk.getPrecipitationHeight(pos);
      return blockpos.getY();
    } 
    return -1;
  }
  
  private static ReflectorField findFieldHasEntities() {
    try {
      Chunk chunk = new Chunk((World)null, 0, 0);
      List<Field> list = new ArrayList();
      List<Object> list1 = new ArrayList();
      Field[] afield = Chunk.class.getDeclaredFields();
      for (int i = 0; i < afield.length; i++) {
        Field field = afield[i];
        if (field.getType() == boolean.class) {
          field.setAccessible(true);
          list.add(field);
          list1.add(field.get(chunk));
        } 
      } 
      chunk.setHasEntities(false);
      List<Object> list2 = new ArrayList();
      for (Field e : list) {
        Field field1 = e;
        list2.add(field1.get(chunk));
      } 
      chunk.setHasEntities(true);
      List<Object> list3 = new ArrayList();
      for (Field e : list) {
        Field field2 = e;
        list3.add(field2.get(chunk));
      } 
      List<Field> list4 = new ArrayList();
      for (int j = 0; j < list.size(); j++) {
        Field field3 = list.get(j);
        Boolean obool = (Boolean)list2.get(j);
        Boolean obool1 = (Boolean)list3.get(j);
        if (!obool.booleanValue() && obool1.booleanValue()) {
          list4.add(field3);
          Boolean obool2 = (Boolean)list1.get(j);
          field3.set(chunk, obool2);
        } 
      } 
      if (list4.size() == 1) {
        Field field4 = list4.get(0);
        return new ReflectorField(field4);
      } 
    } catch (Exception exception) {
      Config.warn(exception.getClass().getName() + " " + exception.getMessage());
    } 
    Config.warn("Error finding Chunk.hasEntities");
    return new ReflectorField(new ReflectorClass(Chunk.class), "hasEntities");
  }
}
