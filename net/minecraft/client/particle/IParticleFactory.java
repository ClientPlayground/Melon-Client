package net.minecraft.client.particle;

import net.minecraft.world.World;

public interface IParticleFactory {
  EntityFX getEntityFX(int paramInt, World paramWorld, double paramDouble1, double paramDouble2, double paramDouble3, double paramDouble4, double paramDouble5, double paramDouble6, int... paramVarArgs);
}
