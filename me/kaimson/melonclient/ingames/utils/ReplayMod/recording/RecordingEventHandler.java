package me.kaimson.melonclient.ingames.utils.ReplayMod.recording;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.Objects;
import me.kaimson.melonclient.Events.EventHandler;
import me.kaimson.melonclient.Events.TypeEvent;
import me.kaimson.melonclient.Events.imp.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S04PacketEntityEquipment;
import net.minecraft.network.play.server.S0BPacketAnimation;
import net.minecraft.network.play.server.S0CPacketSpawnPlayer;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S14PacketEntity;
import net.minecraft.network.play.server.S18PacketEntityTeleport;
import net.minecraft.network.play.server.S19PacketEntityHeadLook;
import net.minecraft.network.play.server.S1BPacketEntityAttach;
import net.minecraft.util.MathHelper;

public class RecordingEventHandler {
  private final Minecraft mc = Minecraft.getMinecraft();
  
  private final PacketListener packetListener;
  
  private ItemStack[] playerItems = new ItemStack[5];
  
  private boolean wasSleeping;
  
  private int lastRiding = -1;
  
  private Double lastX;
  
  private Double lastY;
  
  private Double lastZ;
  
  private int ticksSinceLastCorrection;
  
  private Integer rotationYawHeadBefore;
  
  public RecordingEventHandler(PacketListener packetListener) {
    this.packetListener = packetListener;
  }
  
  public void register() {
    EventHandler.register(this);
  }
  
  public void unregister() {
    EventHandler.unregister(this);
  }
  
  public void onPlayerJoin() {
    try {
      this.packetListener.save((Packet<?>)spawnPlayer((EntityPlayer)this.mc.thePlayer));
    } catch (Exception e) {
      e.printStackTrace();
    } 
  }
  
  @TypeEvent
  public void onRespawn() {}
  
  private S0CPacketSpawnPlayer spawnPlayer(EntityPlayer player) {
    try {
      S0CPacketSpawnPlayer packet = new S0CPacketSpawnPlayer();
      ByteBuf bb = Unpooled.buffer();
      PacketBuffer pb = new PacketBuffer(bb);
      pb.writeVarIntToBuffer(player.getEntityId());
      pb.writeUuid(EntityPlayer.getUUID(player.getGameProfile()));
      pb.writeInt(MathHelper.floor_double(player.posX * 32.0D));
      pb.writeInt(MathHelper.floor_double(player.posY * 32.0D));
      pb.writeInt(MathHelper.floor_double(player.posZ * 32.0D));
      pb.writeByte((byte)(int)(player.rotationYaw * 256.0F / 360.0F));
      pb.writeByte((byte)(int)(player.rotationPitch * 256.0F / 360.0F));
      ItemStack itemstack = player.inventory.getCurrentItem();
      pb.writeShort((itemstack == null) ? 0 : Item.getIdFromItem(itemstack.getItem()));
      player.getDataWatcher().writeTo(pb);
      packet.readPacketData(pb);
      return packet;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    } 
  }
  
  @TypeEvent
  public void onClientTick(TickEvent.ClientTick e) {
    if (e.phase != TickEvent.Phase.START || this.mc.thePlayer == null)
      return; 
    try {
      S14PacketEntity.S17PacketEntityLookMove s17PacketEntityLookMove;
      boolean force = false;
      if (this.lastX == null || this.lastY == null || this.lastZ == null) {
        force = true;
        this.lastX = Double.valueOf(this.mc.thePlayer.posX);
        this.lastY = Double.valueOf(this.mc.thePlayer.posY);
        this.lastZ = Double.valueOf(this.mc.thePlayer.posZ);
      } 
      this.ticksSinceLastCorrection++;
      if (this.ticksSinceLastCorrection >= 100) {
        this.ticksSinceLastCorrection = 0;
        force = true;
      } 
      double dx = this.mc.thePlayer.posX - this.lastX.doubleValue();
      double dy = this.mc.thePlayer.posY - this.lastY.doubleValue();
      double dz = this.mc.thePlayer.posZ - this.lastZ.doubleValue();
      this.lastX = Double.valueOf(this.mc.thePlayer.posX);
      this.lastY = Double.valueOf(this.mc.thePlayer.posY);
      this.lastZ = Double.valueOf(this.mc.thePlayer.posZ);
      if (force || Math.abs(dx) > 4.0D || Math.abs(dy) > 4.0D || Math.abs(dz) > 4.0D) {
        int x = MathHelper.floor_double(this.mc.thePlayer.posX * 32.0D);
        int y = MathHelper.floor_double(this.mc.thePlayer.posY * 32.0D);
        int z = MathHelper.floor_double(this.mc.thePlayer.posZ * 32.0D);
        byte yaw = (byte)(int)(this.mc.thePlayer.rotationYaw * 256.0F / 360.0F);
        byte pitch = (byte)(int)(this.mc.thePlayer.rotationPitch * 256.0F / 360.0F);
        S18PacketEntityTeleport s18PacketEntityTeleport = new S18PacketEntityTeleport(this.mc.thePlayer.getEntityId(), x, y, z, yaw, pitch, this.mc.thePlayer.onGround);
      } else {
        byte newYaw = (byte)(int)(this.mc.thePlayer.rotationYaw * 256.0F / 360.0F);
        byte newPitch = (byte)(int)(this.mc.thePlayer.rotationPitch * 256.0F / 360.0F);
        s17PacketEntityLookMove = new S14PacketEntity.S17PacketEntityLookMove(this.mc.thePlayer.getEntityId(), (byte)(int)Math.round(dx * 32.0D), (byte)(int)Math.round(dy * 32.0D), (byte)(int)Math.round(dz * 32.0D), newYaw, newPitch, this.mc.thePlayer.onGround);
      } 
      this.packetListener.save((Packet<?>)s17PacketEntityLookMove);
      int rotationYawHead = (int)(this.mc.thePlayer.rotationYawHead * 256.0F / 360.0F);
      if (!Objects.equals(Integer.valueOf(rotationYawHead), this.rotationYawHeadBefore)) {
        S19PacketEntityHeadLook head = new S19PacketEntityHeadLook();
        ByteBuf bb1 = Unpooled.buffer();
        PacketBuffer pb1 = new PacketBuffer(bb1);
        pb1.writeVarIntToBuffer(this.mc.thePlayer.getEntityId());
        pb1.writeByte(rotationYawHead);
        head.readPacketData(pb1);
        this.packetListener.save((Packet<?>)head);
        this.rotationYawHeadBefore = Integer.valueOf(rotationYawHead);
      } 
      S12PacketEntityVelocity vel = new S12PacketEntityVelocity(this.mc.thePlayer.getEntityId(), this.mc.thePlayer.motionX, this.mc.thePlayer.motionY, this.mc.thePlayer.motionZ);
      this.packetListener.save((Packet<?>)vel);
      if (this.mc.thePlayer.swingProgressInt == 1) {
        S0BPacketAnimation pac = new S0BPacketAnimation();
        ByteBuf bb = Unpooled.buffer();
        PacketBuffer pb = new PacketBuffer(bb);
        pb.writeVarIntToBuffer(this.mc.thePlayer.getEntityId());
        pb.writeByte(0);
        pac.readPacketData(pb);
        this.packetListener.save((Packet<?>)pac);
      } 
      if (this.playerItems[0] != this.mc.thePlayer.getHeldItem()) {
        this.playerItems[0] = this.mc.thePlayer.getHeldItem();
        S04PacketEntityEquipment pee = new S04PacketEntityEquipment(this.mc.thePlayer.getEntityId(), 0, this.playerItems[0]);
        this.packetListener.save((Packet<?>)pee);
      } 
      if (this.playerItems[1] != this.mc.thePlayer.inventory.armorInventory[0]) {
        this.playerItems[1] = this.mc.thePlayer.inventory.armorInventory[0];
        S04PacketEntityEquipment pee = new S04PacketEntityEquipment(this.mc.thePlayer.getEntityId(), 1, this.playerItems[1]);
        this.packetListener.save((Packet<?>)pee);
      } 
      if (this.playerItems[2] != this.mc.thePlayer.inventory.armorInventory[1]) {
        this.playerItems[2] = this.mc.thePlayer.inventory.armorInventory[1];
        S04PacketEntityEquipment pee = new S04PacketEntityEquipment(this.mc.thePlayer.getEntityId(), 2, this.playerItems[2]);
        this.packetListener.save((Packet<?>)pee);
      } 
      if (this.playerItems[3] != this.mc.thePlayer.inventory.armorInventory[2]) {
        this.playerItems[3] = this.mc.thePlayer.inventory.armorInventory[2];
        S04PacketEntityEquipment pee = new S04PacketEntityEquipment(this.mc.thePlayer.getEntityId(), 3, this.playerItems[3]);
        this.packetListener.save((Packet<?>)pee);
      } 
      if (this.playerItems[4] != this.mc.thePlayer.inventory.armorInventory[3]) {
        this.playerItems[4] = this.mc.thePlayer.inventory.armorInventory[3];
        S04PacketEntityEquipment pee = new S04PacketEntityEquipment(this.mc.thePlayer.getEntityId(), 4, this.playerItems[4]);
        this.packetListener.save((Packet<?>)pee);
      } 
      if ((!this.mc.thePlayer.isRiding() && this.lastRiding != -1) || (this.mc.thePlayer
        .isRiding() && this.lastRiding != this.mc.thePlayer.ridingEntity.getEntityId())) {
        if (!this.mc.thePlayer.isRiding()) {
          this.lastRiding = -1;
        } else {
          this.lastRiding = this.mc.thePlayer.ridingEntity.getEntityId();
        } 
        S1BPacketEntityAttach pea = new S1BPacketEntityAttach();
        ByteBuf buf = Unpooled.buffer();
        PacketBuffer pbuf = new PacketBuffer(buf);
        pbuf.writeInt(this.mc.thePlayer.getEntityId());
        pbuf.writeInt(this.lastRiding);
        pbuf.writeBoolean(false);
        pea.readPacketData(pbuf);
        this.packetListener.save((Packet<?>)pea);
      } 
      if (!this.mc.thePlayer.isPlayerSleeping() && this.wasSleeping) {
        S0BPacketAnimation pac = new S0BPacketAnimation();
        ByteBuf bb = Unpooled.buffer();
        PacketBuffer pb = new PacketBuffer(bb);
        pb.writeVarIntToBuffer(this.mc.thePlayer.getEntityId());
        pb.writeByte(2);
        pac.readPacketData(pb);
        this.packetListener.save((Packet<?>)pac);
        this.wasSleeping = false;
      } 
    } catch (Exception e1) {
      e1.printStackTrace();
    } 
  }
}
