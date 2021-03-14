package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.item.Item;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.Particle;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.packets.InventoryPackets;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.packets.WorldPackets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ParticleRewriter {
  private static List<NewParticle> particles = new LinkedList<>();
  
  static {
    add(34);
    add(19);
    add(18);
    add(21);
    add(4);
    add(43);
    add(22);
    add(42);
    add(42);
    add(6);
    add(14);
    add(37);
    add(30);
    add(12);
    add(26);
    add(17);
    add(0);
    add(44);
    add(10);
    add(9);
    add(1);
    add(24);
    add(32);
    add(33);
    add(35);
    add(15);
    add(23);
    add(31);
    add(-1);
    add(5);
    add(11, reddustHandler());
    add(29);
    add(34);
    add(28);
    add(25);
    add(2);
    add(27, iconcrackHandler());
    add(3, blockHandler());
    add(3, blockHandler());
    add(36);
    add(-1);
    add(13);
    add(8);
    add(16);
    add(7);
    add(40);
    add(20, blockHandler());
    add(41);
    add(38);
  }
  
  public static Particle rewriteParticle(int particleId, Integer[] data) {
    if (particleId >= particles.size()) {
      Via.getPlatform().getLogger().severe("Failed to transform particles with id " + particleId + " and data " + Arrays.toString((Object[])data));
      return null;
    } 
    NewParticle rewrite = particles.get(particleId);
    return rewrite.handle(new Particle(rewrite.getId()), data);
  }
  
  private static void add(int newId) {
    particles.add(new NewParticle(newId, null));
  }
  
  private static void add(int newId, ParticleDataHandler dataHandler) {
    particles.add(new NewParticle(newId, dataHandler));
  }
  
  private static ParticleDataHandler reddustHandler() {
    return new ParticleDataHandler() {
        public Particle handler(Particle particle, Integer[] data) {
          particle.getArguments().add(new Particle.ParticleData(Type.FLOAT, Float.valueOf(ParticleRewriter.randomBool() ? 1.0F : 0.0F)));
          particle.getArguments().add(new Particle.ParticleData(Type.FLOAT, Float.valueOf(0.0F)));
          particle.getArguments().add(new Particle.ParticleData(Type.FLOAT, Float.valueOf(ParticleRewriter.randomBool() ? 1.0F : 0.0F)));
          particle.getArguments().add(new Particle.ParticleData(Type.FLOAT, Float.valueOf(1.0F)));
          return particle;
        }
      };
  }
  
  private static boolean randomBool() {
    return ThreadLocalRandom.current().nextBoolean();
  }
  
  private static ParticleDataHandler iconcrackHandler() {
    return new ParticleDataHandler() {
        public Particle handler(Particle particle, Integer[] data) {
          Item item;
          if (data.length == 1) {
            item = new Item(data[0].shortValue(), (byte)1, (short)0, null);
          } else if (data.length == 2) {
            item = new Item(data[0].shortValue(), (byte)1, data[1].shortValue(), null);
          } else {
            return particle;
          } 
          InventoryPackets.toClient(item);
          particle.getArguments().add(new Particle.ParticleData(Type.FLAT_ITEM, item));
          return particle;
        }
      };
  }
  
  private static ParticleDataHandler blockHandler() {
    return new ParticleDataHandler() {
        public Particle handler(Particle particle, Integer[] data) {
          int value = data[0].intValue();
          int combined = (value & 0xFFF) << 4 | value >> 12 & 0xF;
          int newId = WorldPackets.toNewId(combined);
          particle.getArguments().add(new Particle.ParticleData(Type.VAR_INT, Integer.valueOf(newId)));
          return particle;
        }
      };
  }
  
  static interface ParticleDataHandler {
    Particle handler(Particle param1Particle, Integer[] param1ArrayOfInteger);
  }
  
  private static class NewParticle {
    private final int id;
    
    private final ParticleRewriter.ParticleDataHandler handler;
    
    public boolean equals(Object o) {
      if (o == this)
        return true; 
      if (!(o instanceof NewParticle))
        return false; 
      NewParticle other = (NewParticle)o;
      if (!other.canEqual(this))
        return false; 
      if (getId() != other.getId())
        return false; 
      Object this$handler = getHandler(), other$handler = other.getHandler();
      return !((this$handler == null) ? (other$handler != null) : !this$handler.equals(other$handler));
    }
    
    protected boolean canEqual(Object other) {
      return other instanceof NewParticle;
    }
    
    public int hashCode() {
      int PRIME = 59;
      result = 1;
      result = result * 59 + getId();
      Object $handler = getHandler();
      return result * 59 + (($handler == null) ? 43 : $handler.hashCode());
    }
    
    public String toString() {
      return "ParticleRewriter.NewParticle(id=" + getId() + ", handler=" + getHandler() + ")";
    }
    
    public NewParticle(int id, ParticleRewriter.ParticleDataHandler handler) {
      this.id = id;
      this.handler = handler;
    }
    
    public int getId() {
      return this.id;
    }
    
    public ParticleRewriter.ParticleDataHandler getHandler() {
      return this.handler;
    }
    
    public Particle handle(Particle particle, Integer[] data) {
      if (this.handler != null)
        return this.handler.handler(particle, data); 
      return particle;
    }
  }
}
