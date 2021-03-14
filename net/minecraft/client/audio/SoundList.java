package net.minecraft.client.audio;

import com.google.common.collect.Lists;
import java.util.List;

public class SoundList {
  private final List<SoundEntry> soundList = Lists.newArrayList();
  
  private boolean replaceExisting;
  
  private SoundCategory category;
  
  public List<SoundEntry> getSoundList() {
    return this.soundList;
  }
  
  public boolean canReplaceExisting() {
    return this.replaceExisting;
  }
  
  public void setReplaceExisting(boolean p_148572_1_) {
    this.replaceExisting = p_148572_1_;
  }
  
  public SoundCategory getSoundCategory() {
    return this.category;
  }
  
  public void setSoundCategory(SoundCategory soundCat) {
    this.category = soundCat;
  }
  
  public static class SoundEntry {
    private String name;
    
    private float volume = 1.0F;
    
    private float pitch = 1.0F;
    
    private int weight = 1;
    
    private Type type = Type.FILE;
    
    private boolean streaming = false;
    
    public String getSoundEntryName() {
      return this.name;
    }
    
    public void setSoundEntryName(String nameIn) {
      this.name = nameIn;
    }
    
    public float getSoundEntryVolume() {
      return this.volume;
    }
    
    public void setSoundEntryVolume(float volumeIn) {
      this.volume = volumeIn;
    }
    
    public float getSoundEntryPitch() {
      return this.pitch;
    }
    
    public void setSoundEntryPitch(float pitchIn) {
      this.pitch = pitchIn;
    }
    
    public int getSoundEntryWeight() {
      return this.weight;
    }
    
    public void setSoundEntryWeight(int weightIn) {
      this.weight = weightIn;
    }
    
    public Type getSoundEntryType() {
      return this.type;
    }
    
    public void setSoundEntryType(Type typeIn) {
      this.type = typeIn;
    }
    
    public boolean isStreaming() {
      return this.streaming;
    }
    
    public void setStreaming(boolean isStreaming) {
      this.streaming = isStreaming;
    }
    
    public enum Type {
      FILE("file"),
      SOUND_EVENT("event");
      
      private final String field_148583_c;
      
      Type(String p_i45109_3_) {
        this.field_148583_c = p_i45109_3_;
      }
      
      public static Type getType(String p_148580_0_) {
        for (Type soundlist$soundentry$type : values()) {
          if (soundlist$soundentry$type.field_148583_c.equals(p_148580_0_))
            return soundlist$soundentry$type; 
        } 
        return null;
      }
    }
  }
}
