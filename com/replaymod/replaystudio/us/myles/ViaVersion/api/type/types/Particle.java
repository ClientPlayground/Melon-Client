package com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import java.util.LinkedList;
import java.util.List;

public class Particle {
  private int id;
  
  public void setId(int id) {
    this.id = id;
  }
  
  public void setArguments(List<ParticleData> arguments) {
    this.arguments = arguments;
  }
  
  public boolean equals(Object o) {
    if (o == this)
      return true; 
    if (!(o instanceof Particle))
      return false; 
    Particle other = (Particle)o;
    if (!other.canEqual(this))
      return false; 
    if (getId() != other.getId())
      return false; 
    Object<ParticleData> this$arguments = (Object<ParticleData>)getArguments(), other$arguments = (Object<ParticleData>)other.getArguments();
    return !((this$arguments == null) ? (other$arguments != null) : !this$arguments.equals(other$arguments));
  }
  
  protected boolean canEqual(Object other) {
    return other instanceof Particle;
  }
  
  public int hashCode() {
    int PRIME = 59;
    result = 1;
    result = result * 59 + getId();
    Object<ParticleData> $arguments = (Object<ParticleData>)getArguments();
    return result * 59 + (($arguments == null) ? 43 : $arguments.hashCode());
  }
  
  public String toString() {
    return "Particle(id=" + getId() + ", arguments=" + getArguments() + ")";
  }
  
  public int getId() {
    return this.id;
  }
  
  private List<ParticleData> arguments = new LinkedList<>();
  
  public List<ParticleData> getArguments() {
    return this.arguments;
  }
  
  public Particle(int id) {
    this.id = id;
  }
  
  public static class ParticleData {
    private Type type;
    
    private Object value;
    
    public void setType(Type type) {
      this.type = type;
    }
    
    public void setValue(Object value) {
      this.value = value;
    }
    
    public boolean equals(Object o) {
      if (o == this)
        return true; 
      if (!(o instanceof ParticleData))
        return false; 
      ParticleData other = (ParticleData)o;
      if (!other.canEqual(this))
        return false; 
      Object this$type = getType(), other$type = other.getType();
      if ((this$type == null) ? (other$type != null) : !this$type.equals(other$type))
        return false; 
      Object this$value = getValue(), other$value = other.getValue();
      return !((this$value == null) ? (other$value != null) : !this$value.equals(other$value));
    }
    
    protected boolean canEqual(Object other) {
      return other instanceof ParticleData;
    }
    
    public int hashCode() {
      int PRIME = 59;
      result = 1;
      Object $type = getType();
      result = result * 59 + (($type == null) ? 43 : $type.hashCode());
      Object $value = getValue();
      return result * 59 + (($value == null) ? 43 : $value.hashCode());
    }
    
    public String toString() {
      return "Particle.ParticleData(type=" + getType() + ", value=" + getValue() + ")";
    }
    
    public ParticleData(Type type, Object value) {
      this.type = type;
      this.value = value;
    }
    
    public Type getType() {
      return this.type;
    }
    
    public Object getValue() {
      return this.value;
    }
  }
}
