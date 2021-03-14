package me.kaimson.melonclient.Events.imp;

import me.kaimson.melonclient.Events.Event;

public class EntityViewRenderEvent extends Event {
  public float roll;
  
  public EntityViewRenderEvent(float roll) {
    this.roll = roll;
  }
  
  public static class CameraSetup extends EntityViewRenderEvent {
    public CameraSetup(float roll) {
      super(roll);
    }
  }
}
