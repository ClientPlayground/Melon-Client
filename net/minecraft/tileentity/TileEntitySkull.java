package net.minecraft.tileentity;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.util.UUID;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.StringUtils;

public class TileEntitySkull extends TileEntity {
  private int skullType;
  
  private int skullRotation;
  
  private GameProfile playerProfile = null;
  
  public void writeToNBT(NBTTagCompound compound) {
    super.writeToNBT(compound);
    compound.setByte("SkullType", (byte)(this.skullType & 0xFF));
    compound.setByte("Rot", (byte)(this.skullRotation & 0xFF));
    if (this.playerProfile != null) {
      NBTTagCompound nbttagcompound = new NBTTagCompound();
      NBTUtil.writeGameProfile(nbttagcompound, this.playerProfile);
      compound.setTag("Owner", (NBTBase)nbttagcompound);
    } 
  }
  
  public void readFromNBT(NBTTagCompound compound) {
    super.readFromNBT(compound);
    this.skullType = compound.getByte("SkullType");
    this.skullRotation = compound.getByte("Rot");
    if (this.skullType == 3)
      if (compound.hasKey("Owner", 10)) {
        this.playerProfile = NBTUtil.readGameProfileFromNBT(compound.getCompoundTag("Owner"));
      } else if (compound.hasKey("ExtraType", 8)) {
        String s = compound.getString("ExtraType");
        if (!StringUtils.isNullOrEmpty(s)) {
          this.playerProfile = new GameProfile((UUID)null, s);
          updatePlayerProfile();
        } 
      }  
  }
  
  public GameProfile getPlayerProfile() {
    return this.playerProfile;
  }
  
  public Packet getDescriptionPacket() {
    NBTTagCompound nbttagcompound = new NBTTagCompound();
    writeToNBT(nbttagcompound);
    return (Packet)new S35PacketUpdateTileEntity(this.pos, 4, nbttagcompound);
  }
  
  public void setType(int type) {
    this.skullType = type;
    this.playerProfile = null;
  }
  
  public void setPlayerProfile(GameProfile playerProfile) {
    this.skullType = 3;
    this.playerProfile = playerProfile;
    updatePlayerProfile();
  }
  
  private void updatePlayerProfile() {
    this.playerProfile = updateGameprofile(this.playerProfile);
    markDirty();
  }
  
  public static GameProfile updateGameprofile(GameProfile input) {
    if (input != null && !StringUtils.isNullOrEmpty(input.getName())) {
      if (input.isComplete() && input.getProperties().containsKey("textures"))
        return input; 
      if (MinecraftServer.getServer() == null)
        return input; 
      GameProfile gameprofile = MinecraftServer.getServer().getPlayerProfileCache().getGameProfileForUsername(input.getName());
      if (gameprofile == null)
        return input; 
      Property property = (Property)Iterables.getFirst(gameprofile.getProperties().get("textures"), null);
      if (property == null)
        gameprofile = MinecraftServer.getServer().getMinecraftSessionService().fillProfileProperties(gameprofile, true); 
      return gameprofile;
    } 
    return input;
  }
  
  public int getSkullType() {
    return this.skullType;
  }
  
  public int getSkullRotation() {
    return this.skullRotation;
  }
  
  public void setSkullRotation(int rotation) {
    this.skullRotation = rotation;
  }
}
