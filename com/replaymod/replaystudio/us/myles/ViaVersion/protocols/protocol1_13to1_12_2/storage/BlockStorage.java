package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.storage;

import com.google.common.collect.Sets;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.StoredObject;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.Position;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BlockStorage extends StoredObject {
  private static final Set<Integer> whitelist = Sets.newConcurrentHashSet();
  
  static {
    whitelist.add(Integer.valueOf(5266));
    int i;
    for (i = 0; i < 16; i++)
      whitelist.add(Integer.valueOf(972 + i)); 
    for (i = 0; i < 20; i++)
      whitelist.add(Integer.valueOf(6854 + i)); 
    for (i = 0; i < 4; i++)
      whitelist.add(Integer.valueOf(7110 + i)); 
    for (i = 0; i < 5; i++)
      whitelist.add(Integer.valueOf(5447 + i)); 
  }
  
  private Map<Position, ReplacementData> blocks = new ConcurrentHashMap<>();
  
  public BlockStorage(UserConnection user) {
    super(user);
  }
  
  public void store(Position position, int block) {
    store(position, block, -1);
  }
  
  public void store(Position position, int block, int replacementId) {
    if (!whitelist.contains(Integer.valueOf(block)))
      return; 
    this.blocks.put(position, new ReplacementData(block, replacementId));
  }
  
  public boolean isWelcome(int block) {
    return whitelist.contains(Integer.valueOf(block));
  }
  
  public boolean contains(Position position) {
    return this.blocks.containsKey(position);
  }
  
  public ReplacementData get(Position position) {
    return this.blocks.get(position);
  }
  
  public ReplacementData remove(Position position) {
    return this.blocks.remove(position);
  }
  
  public class ReplacementData {
    private int original;
    
    private int replacement;
    
    public void setOriginal(int original) {
      this.original = original;
    }
    
    public void setReplacement(int replacement) {
      this.replacement = replacement;
    }
    
    public boolean equals(Object o) {
      if (o == this)
        return true; 
      if (!(o instanceof ReplacementData))
        return false; 
      ReplacementData other = (ReplacementData)o;
      return !other.canEqual(this) ? false : ((getOriginal() != other.getOriginal()) ? false : (!(getReplacement() != other.getReplacement())));
    }
    
    protected boolean canEqual(Object other) {
      return other instanceof ReplacementData;
    }
    
    public int hashCode() {
      int PRIME = 59;
      result = 1;
      result = result * 59 + getOriginal();
      return result * 59 + getReplacement();
    }
    
    public String toString() {
      return "BlockStorage.ReplacementData(original=" + getOriginal() + ", replacement=" + getReplacement() + ")";
    }
    
    public ReplacementData(int original, int replacement) {
      this.original = original;
      this.replacement = replacement;
    }
    
    public int getOriginal() {
      return this.original;
    }
    
    public int getReplacement() {
      return this.replacement;
    }
  }
}
