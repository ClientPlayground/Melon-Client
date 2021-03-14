package me.kaimson.melonclient.Events.imp;

import me.kaimson.melonclient.Events.Cancellable;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class EntityJoinWorldEvent extends Cancellable {
  private final Entity entity;
  
  private final World world;
  
  public EntityJoinWorldEvent(Entity entity, World world) {
    this.entity = entity;
    this.world = world;
  }
}
