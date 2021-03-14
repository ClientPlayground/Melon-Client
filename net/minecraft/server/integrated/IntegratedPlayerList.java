package net.minecraft.server.integrated;

import com.mojang.authlib.GameProfile;
import java.net.SocketAddress;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;

public class IntegratedPlayerList extends ServerConfigurationManager {
  private NBTTagCompound hostPlayerData;
  
  public IntegratedPlayerList(IntegratedServer server) {
    super(server);
    setViewDistance(10);
  }
  
  protected void writePlayerData(EntityPlayerMP playerIn) {
    if (playerIn.getCommandSenderName().equals(getServerInstance().getServerOwner())) {
      this.hostPlayerData = new NBTTagCompound();
      playerIn.writeToNBT(this.hostPlayerData);
    } 
    super.writePlayerData(playerIn);
  }
  
  public String allowUserToConnect(SocketAddress address, GameProfile profile) {
    return (profile.getName().equalsIgnoreCase(getServerInstance().getServerOwner()) && getPlayerByUsername(profile.getName()) != null) ? "That name is already taken." : super.allowUserToConnect(address, profile);
  }
  
  public IntegratedServer getServerInstance() {
    return (IntegratedServer)super.getServerInstance();
  }
  
  public NBTTagCompound getHostPlayerData() {
    return this.hostPlayerData;
  }
}
