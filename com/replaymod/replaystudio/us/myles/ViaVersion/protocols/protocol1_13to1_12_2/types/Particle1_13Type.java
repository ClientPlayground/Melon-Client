package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.types;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.Particle;

public class Particle1_13Type extends Type<Particle> {
  public Particle1_13Type() {
    super("Particle", Particle.class);
  }
  
  public void write(ByteBuf buffer, Particle object) throws Exception {
    Type.VAR_INT.write(buffer, Integer.valueOf(object.getId()));
    for (Particle.ParticleData data : object.getArguments())
      data.getType().write(buffer, data.getValue()); 
  }
  
  public Particle read(ByteBuf buffer) throws Exception {
    int type = ((Integer)Type.VAR_INT.read(buffer)).intValue();
    Particle particle = new Particle(type);
    switch (type) {
      case 3:
      case 20:
        particle.getArguments().add(new Particle.ParticleData(Type.VAR_INT, Type.VAR_INT.read(buffer)));
        break;
      case 11:
        particle.getArguments().add(new Particle.ParticleData(Type.FLOAT, Type.FLOAT.read(buffer)));
        particle.getArguments().add(new Particle.ParticleData(Type.FLOAT, Type.FLOAT.read(buffer)));
        particle.getArguments().add(new Particle.ParticleData(Type.FLOAT, Type.FLOAT.read(buffer)));
        particle.getArguments().add(new Particle.ParticleData(Type.FLOAT, Type.FLOAT.read(buffer)));
        break;
      case 27:
        particle.getArguments().add(new Particle.ParticleData(Type.FLAT_ITEM, Type.FLAT_ITEM.read(buffer)));
        break;
    } 
    return particle;
  }
}
