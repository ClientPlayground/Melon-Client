package com.replaymod.replaystudio.us.myles.ViaVersion.api.entities;

import com.google.common.base.Optional;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import java.util.HashMap;
import java.util.Map;

public class Entity1_10Types {
  public static EntityType getTypeFromId(int typeID, boolean isObject) {
    Optional<EntityType> type;
    if (isObject) {
      type = ObjectTypes.getPCEntity(typeID);
    } else {
      type = EntityType.findById(typeID);
    } 
    if (!type.isPresent()) {
      Via.getPlatform().getLogger().severe("Could not find type id " + typeID + " isObject=" + isObject);
      return EntityType.ENTITY;
    } 
    return (EntityType)type.get();
  }
  
  public enum EntityType {
    ENTITY(-1),
    DROPPED_ITEM(1, ENTITY),
    EXPERIENCE_ORB(2, ENTITY),
    LEASH_HITCH(8, ENTITY),
    PAINTING(9, ENTITY),
    ARROW(10, ENTITY),
    SNOWBALL(11, ENTITY),
    FIREBALL(12, ENTITY),
    SMALL_FIREBALL(13, ENTITY),
    ENDER_PEARL(14, ENTITY),
    ENDER_SIGNAL(15, ENTITY),
    THROWN_EXP_BOTTLE(17, ENTITY),
    ITEM_FRAME(18, ENTITY),
    WITHER_SKULL(19, ENTITY),
    PRIMED_TNT(20, ENTITY),
    FALLING_BLOCK(21, ENTITY),
    FIREWORK(22, ENTITY),
    TIPPED_ARROW(23, ARROW),
    SPECTRAL_ARROW(24, ARROW),
    SHULKER_BULLET(25, ENTITY),
    DRAGON_FIREBALL(26, FIREBALL),
    ENTITY_LIVING(-1, ENTITY),
    ENTITY_INSENTIENT(-1, ENTITY_LIVING),
    ENTITY_AGEABLE(-1, ENTITY_INSENTIENT),
    ENTITY_TAMEABLE_ANIMAL(-1, ENTITY_AGEABLE),
    ENTITY_HUMAN(-1, ENTITY_LIVING),
    ARMOR_STAND(30, ENTITY_LIVING),
    MINECART_ABSTRACT(-1, ENTITY),
    MINECART_COMMAND(40, MINECART_ABSTRACT),
    BOAT(41, ENTITY),
    MINECART_RIDEABLE(42, MINECART_ABSTRACT),
    MINECART_CHEST(43, MINECART_ABSTRACT),
    MINECART_FURNACE(44, MINECART_ABSTRACT),
    MINECART_TNT(45, MINECART_ABSTRACT),
    MINECART_HOPPER(46, MINECART_ABSTRACT),
    MINECART_MOB_SPAWNER(47, MINECART_ABSTRACT),
    CREEPER(50, ENTITY_INSENTIENT),
    SKELETON(51, ENTITY_INSENTIENT),
    SPIDER(52, ENTITY_INSENTIENT),
    GIANT(53, ENTITY_INSENTIENT),
    ZOMBIE(54, ENTITY_INSENTIENT),
    SLIME(55, ENTITY_INSENTIENT),
    GHAST(56, ENTITY_INSENTIENT),
    PIG_ZOMBIE(57, ZOMBIE),
    ENDERMAN(58, ENTITY_INSENTIENT),
    CAVE_SPIDER(59, SPIDER),
    SILVERFISH(60, ENTITY_INSENTIENT),
    BLAZE(61, ENTITY_INSENTIENT),
    MAGMA_CUBE(62, SLIME),
    ENDER_DRAGON(63, ENTITY_INSENTIENT),
    WITHER(64, ENTITY_INSENTIENT),
    BAT(65, ENTITY_INSENTIENT),
    WITCH(66, ENTITY_INSENTIENT),
    ENDERMITE(67, ENTITY_INSENTIENT),
    GUARDIAN(68, ENTITY_INSENTIENT),
    IRON_GOLEM(99, ENTITY_INSENTIENT),
    SHULKER(69, IRON_GOLEM),
    PIG(90, ENTITY_AGEABLE),
    SHEEP(91, ENTITY_AGEABLE),
    COW(92, ENTITY_AGEABLE),
    CHICKEN(93, ENTITY_AGEABLE),
    SQUID(94, ENTITY_INSENTIENT),
    WOLF(95, ENTITY_TAMEABLE_ANIMAL),
    MUSHROOM_COW(96, COW),
    SNOWMAN(97, IRON_GOLEM),
    OCELOT(98, ENTITY_TAMEABLE_ANIMAL),
    HORSE(100, ENTITY_AGEABLE),
    RABBIT(101, ENTITY_AGEABLE),
    POLAR_BEAR(102, ENTITY_AGEABLE),
    VILLAGER(120, ENTITY_AGEABLE),
    ENDER_CRYSTAL(200, ENTITY),
    SPLASH_POTION(-1, ENTITY),
    LINGERING_POTION(-1, SPLASH_POTION),
    AREA_EFFECT_CLOUD(-1, ENTITY),
    EGG(-1, ENTITY),
    FISHING_HOOK(-1, ENTITY),
    LIGHTNING(-1, ENTITY),
    WEATHER(-1, ENTITY),
    PLAYER(-1, ENTITY_HUMAN),
    COMPLEX_PART(-1, ENTITY);
    
    EntityType(int id, EntityType parent) {
      this.id = id;
      this.parent = parent;
    }
    
    private static final Map<Integer, EntityType> TYPES = new HashMap<>();
    
    private final int id;
    
    private final EntityType parent;
    
    static {
      for (EntityType type : values())
        TYPES.put(Integer.valueOf(type.id), type); 
    }
    
    public int getId() {
      return this.id;
    }
    
    public EntityType getParent() {
      return this.parent;
    }
    
    EntityType(int id) {
      this.id = id;
      this.parent = null;
    }
    
    public static Optional<EntityType> findById(int id) {
      if (id == -1)
        return Optional.absent(); 
      return Optional.fromNullable(TYPES.get(Integer.valueOf(id)));
    }
  }
  
  public enum ObjectTypes {
    BOAT(1, Entity1_10Types.EntityType.BOAT),
    ITEM(2, Entity1_10Types.EntityType.DROPPED_ITEM),
    AREA_EFFECT_CLOUD(3, Entity1_10Types.EntityType.AREA_EFFECT_CLOUD),
    MINECART(10, Entity1_10Types.EntityType.MINECART_RIDEABLE),
    TNT_PRIMED(50, Entity1_10Types.EntityType.PRIMED_TNT),
    ENDER_CRYSTAL(51, Entity1_10Types.EntityType.ENDER_CRYSTAL),
    TIPPED_ARROW(60, Entity1_10Types.EntityType.TIPPED_ARROW),
    SNOWBALL(61, Entity1_10Types.EntityType.SNOWBALL),
    EGG(62, Entity1_10Types.EntityType.EGG),
    FIREBALL(63, Entity1_10Types.EntityType.FIREBALL),
    SMALL_FIREBALL(64, Entity1_10Types.EntityType.SMALL_FIREBALL),
    ENDER_PEARL(65, Entity1_10Types.EntityType.ENDER_PEARL),
    WITHER_SKULL(66, Entity1_10Types.EntityType.WITHER_SKULL),
    SHULKER_BULLET(67, Entity1_10Types.EntityType.SHULKER_BULLET),
    FALLING_BLOCK(70, Entity1_10Types.EntityType.FALLING_BLOCK),
    ITEM_FRAME(71, Entity1_10Types.EntityType.ITEM_FRAME),
    ENDER_SIGNAL(72, Entity1_10Types.EntityType.ENDER_SIGNAL),
    POTION(73, Entity1_10Types.EntityType.SPLASH_POTION),
    THROWN_EXP_BOTTLE(75, Entity1_10Types.EntityType.THROWN_EXP_BOTTLE),
    FIREWORK(76, Entity1_10Types.EntityType.FIREWORK),
    LEASH(77, Entity1_10Types.EntityType.LEASH_HITCH),
    ARMOR_STAND(78, Entity1_10Types.EntityType.ARMOR_STAND),
    FISHIHNG_HOOK(90, Entity1_10Types.EntityType.FISHING_HOOK),
    SPECTRAL_ARROW(91, Entity1_10Types.EntityType.SPECTRAL_ARROW),
    DRAGON_FIREBALL(93, Entity1_10Types.EntityType.DRAGON_FIREBALL);
    
    ObjectTypes(int id, Entity1_10Types.EntityType type) {
      this.id = id;
      this.type = type;
    }
    
    private static final Map<Integer, ObjectTypes> TYPES = new HashMap<>();
    
    private final int id;
    
    private final Entity1_10Types.EntityType type;
    
    static {
      for (ObjectTypes type : values())
        TYPES.put(Integer.valueOf(type.id), type); 
    }
    
    public int getId() {
      return this.id;
    }
    
    public Entity1_10Types.EntityType getType() {
      return this.type;
    }
    
    public static Optional<ObjectTypes> findById(int id) {
      if (id == -1)
        return Optional.absent(); 
      return Optional.fromNullable(TYPES.get(Integer.valueOf(id)));
    }
    
    public static Optional<Entity1_10Types.EntityType> getPCEntity(int id) {
      Optional<ObjectTypes> output = findById(id);
      if (!output.isPresent())
        return Optional.absent(); 
      return Optional.of(((ObjectTypes)output.get()).getType());
    }
  }
}
