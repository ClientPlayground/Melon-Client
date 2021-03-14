package com.replaymod.replaystudio.us.myles.ViaVersion.api.entities;

import com.google.common.base.Optional;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import java.util.HashMap;
import java.util.Map;

public class Entity1_11Types {
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
    SPECTRAL_ARROW(24, ARROW),
    SHULKER_BULLET(25, ENTITY),
    DRAGON_FIREBALL(26, FIREBALL),
    EVOCATION_FANGS(33, ENTITY),
    ENTITY_LIVING(-1, ENTITY),
    ENTITY_INSENTIENT(-1, ENTITY_LIVING),
    ENTITY_AGEABLE(-1, ENTITY_INSENTIENT),
    ENTITY_TAMEABLE_ANIMAL(-1, ENTITY_AGEABLE),
    ENTITY_HUMAN(-1, ENTITY_LIVING),
    ARMOR_STAND(30, ENTITY_LIVING),
    EVOCATION_ILLAGER(34, ENTITY_INSENTIENT),
    VEX(35, ENTITY_INSENTIENT),
    VINDICATION_ILLAGER(36, ENTITY_INSENTIENT),
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
    ABSTRACT_SKELETON(-1, ENTITY_INSENTIENT),
    SKELETON(51, ABSTRACT_SKELETON),
    WITHER_SKELETON(5, ABSTRACT_SKELETON),
    STRAY(6, ABSTRACT_SKELETON),
    SPIDER(52, ENTITY_INSENTIENT),
    GIANT(53, ENTITY_INSENTIENT),
    ZOMBIE(54, ENTITY_INSENTIENT),
    HUSK(23, ZOMBIE),
    ZOMBIE_VILLAGER(27, ZOMBIE),
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
    ELDER_GUARDIAN(4, GUARDIAN),
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
    ABSTRACT_HORSE(-1, ENTITY_AGEABLE),
    HORSE(100, ABSTRACT_HORSE),
    SKELETON_HORSE(28, ABSTRACT_HORSE),
    ZOMBIE_HORSE(29, ABSTRACT_HORSE),
    CHESTED_HORSE(-1, ABSTRACT_HORSE),
    DONKEY(31, CHESTED_HORSE),
    MULE(32, CHESTED_HORSE),
    LIAMA(103, CHESTED_HORSE),
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
    COMPLEX_PART(-1, ENTITY),
    LIAMA_SPIT(-1, ENTITY);
    
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
    
    public boolean is(EntityType... types) {
      for (EntityType type : types) {
        if (is(type))
          return true; 
      } 
      return false;
    }
    
    public boolean is(EntityType type) {
      return (this == type);
    }
    
    public boolean isOrHasParent(EntityType type) {
      EntityType parent = this;
      do {
        if (parent.equals(type))
          return true; 
        parent = parent.getParent();
      } while (parent != null);
      return false;
    }
  }
  
  public enum ObjectTypes {
    BOAT(1, Entity1_11Types.EntityType.BOAT),
    ITEM(2, Entity1_11Types.EntityType.DROPPED_ITEM),
    AREA_EFFECT_CLOUD(3, Entity1_11Types.EntityType.AREA_EFFECT_CLOUD),
    MINECART(10, Entity1_11Types.EntityType.MINECART_RIDEABLE),
    TNT_PRIMED(50, Entity1_11Types.EntityType.PRIMED_TNT),
    ENDER_CRYSTAL(51, Entity1_11Types.EntityType.ENDER_CRYSTAL),
    TIPPED_ARROW(60, Entity1_11Types.EntityType.ARROW),
    SNOWBALL(61, Entity1_11Types.EntityType.SNOWBALL),
    EGG(62, Entity1_11Types.EntityType.EGG),
    FIREBALL(63, Entity1_11Types.EntityType.FIREBALL),
    SMALL_FIREBALL(64, Entity1_11Types.EntityType.SMALL_FIREBALL),
    ENDER_PEARL(65, Entity1_11Types.EntityType.ENDER_PEARL),
    WITHER_SKULL(66, Entity1_11Types.EntityType.WITHER_SKULL),
    SHULKER_BULLET(67, Entity1_11Types.EntityType.SHULKER_BULLET),
    LIAMA_SPIT(68, Entity1_11Types.EntityType.LIAMA_SPIT),
    FALLING_BLOCK(70, Entity1_11Types.EntityType.FALLING_BLOCK),
    ITEM_FRAME(71, Entity1_11Types.EntityType.ITEM_FRAME),
    ENDER_SIGNAL(72, Entity1_11Types.EntityType.ENDER_SIGNAL),
    POTION(73, Entity1_11Types.EntityType.SPLASH_POTION),
    THROWN_EXP_BOTTLE(75, Entity1_11Types.EntityType.THROWN_EXP_BOTTLE),
    FIREWORK(76, Entity1_11Types.EntityType.FIREWORK),
    LEASH(77, Entity1_11Types.EntityType.LEASH_HITCH),
    ARMOR_STAND(78, Entity1_11Types.EntityType.ARMOR_STAND),
    EVOCATION_FANGS(79, Entity1_11Types.EntityType.EVOCATION_FANGS),
    FISHIHNG_HOOK(90, Entity1_11Types.EntityType.FISHING_HOOK),
    SPECTRAL_ARROW(91, Entity1_11Types.EntityType.SPECTRAL_ARROW),
    DRAGON_FIREBALL(93, Entity1_11Types.EntityType.DRAGON_FIREBALL);
    
    ObjectTypes(int id, Entity1_11Types.EntityType type) {
      this.id = id;
      this.type = type;
    }
    
    private static final Map<Integer, ObjectTypes> TYPES = new HashMap<>();
    
    private final int id;
    
    private final Entity1_11Types.EntityType type;
    
    static {
      for (ObjectTypes type : values())
        TYPES.put(Integer.valueOf(type.id), type); 
    }
    
    public int getId() {
      return this.id;
    }
    
    public Entity1_11Types.EntityType getType() {
      return this.type;
    }
    
    public static Optional<ObjectTypes> findById(int id) {
      if (id == -1)
        return Optional.absent(); 
      return Optional.fromNullable(TYPES.get(Integer.valueOf(id)));
    }
    
    public static Optional<Entity1_11Types.EntityType> getPCEntity(int id) {
      Optional<ObjectTypes> output = findById(id);
      if (!output.isPresent())
        return Optional.absent(); 
      return Optional.of(((ObjectTypes)output.get()).getType());
    }
  }
}
