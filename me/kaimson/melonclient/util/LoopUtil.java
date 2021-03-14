package me.kaimson.melonclient.util;

import java.util.List;
import java.util.function.BiConsumer;
import me.kaimson.melonclient.ingames.IngameDisplay;

public class LoopUtil {
  public static void streamToIndex(List<IngameDisplay> list, BiConsumer<Integer, IngameDisplay> forEach) {
    for (int index = 0; index < list.size(); index++)
      forEach.accept(Integer.valueOf(index), list.get(index)); 
  }
}
