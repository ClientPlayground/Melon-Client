package me.kaimson.melonclient.Events.imp;

import me.kaimson.melonclient.Events.Cancellable;
import net.minecraft.entity.item.EntityItem;

public class ItemRenderEvent extends Cancellable {
  private EntityItem entity;
  
  private double x;
  
  private double y;
  
  private double z;
  
  private float entityYaw;
  
  private float partialTicks;
  
  public ItemRenderEvent(EntityItem entity, double x, double y, double z, float entityYaw, float partialTicks) {
    this.entity = entity;
    this.x = x;
    this.y = y;
    this.z = z;
    this.entityYaw = entityYaw;
    this.partialTicks = partialTicks;
  }
  
  public EntityItem getEntity() {
    return this.entity;
  }
  
  public double getX() {
    return this.x;
  }
  
  public double getY() {
    return this.y;
  }
  
  public double getZ() {
    return this.z;
  }
  
  public float getEntityYaw() {
    return this.entityYaw;
  }
  
  public float getPartialTicks() {
    return this.partialTicks;
  }
}
