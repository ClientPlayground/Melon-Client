package net.optifine;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.entity.Entity;
import net.minecraft.src.Config;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.optifine.override.PlayerControllerOF;
import net.optifine.util.PropertiesOrdered;
import net.optifine.util.ResUtils;

public class CustomGuis {
  private static Minecraft mc = Config.getMinecraft();
  
  private static PlayerControllerOF playerControllerOF = null;
  
  private static CustomGuiProperties[][] guiProperties = (CustomGuiProperties[][])null;
  
  public static boolean isChristmas = isChristmas();
  
  public static ResourceLocation getTextureLocation(ResourceLocation loc) {
    if (guiProperties == null)
      return loc; 
    GuiScreen guiscreen = mc.currentScreen;
    if (!(guiscreen instanceof net.minecraft.client.gui.inventory.GuiContainer))
      return loc; 
    if (loc.getResourceDomain().equals("minecraft") && loc.getResourcePath().startsWith("textures/gui/")) {
      if (playerControllerOF == null)
        return loc; 
      WorldClient worldClient = mc.theWorld;
      if (worldClient == null)
        return loc; 
      if (guiscreen instanceof net.minecraft.client.gui.inventory.GuiContainerCreative)
        return getTexturePos(CustomGuiProperties.EnumContainer.CREATIVE, mc.thePlayer.getPosition(), (IBlockAccess)worldClient, loc, guiscreen); 
      if (guiscreen instanceof net.minecraft.client.gui.inventory.GuiInventory)
        return getTexturePos(CustomGuiProperties.EnumContainer.INVENTORY, mc.thePlayer.getPosition(), (IBlockAccess)worldClient, loc, guiscreen); 
      BlockPos blockpos = playerControllerOF.getLastClickBlockPos();
      if (blockpos != null) {
        if (guiscreen instanceof net.minecraft.client.gui.GuiRepair)
          return getTexturePos(CustomGuiProperties.EnumContainer.ANVIL, blockpos, (IBlockAccess)worldClient, loc, guiscreen); 
        if (guiscreen instanceof net.minecraft.client.gui.inventory.GuiBeacon)
          return getTexturePos(CustomGuiProperties.EnumContainer.BEACON, blockpos, (IBlockAccess)worldClient, loc, guiscreen); 
        if (guiscreen instanceof net.minecraft.client.gui.inventory.GuiBrewingStand)
          return getTexturePos(CustomGuiProperties.EnumContainer.BREWING_STAND, blockpos, (IBlockAccess)worldClient, loc, guiscreen); 
        if (guiscreen instanceof net.minecraft.client.gui.inventory.GuiChest)
          return getTexturePos(CustomGuiProperties.EnumContainer.CHEST, blockpos, (IBlockAccess)worldClient, loc, guiscreen); 
        if (guiscreen instanceof net.minecraft.client.gui.inventory.GuiCrafting)
          return getTexturePos(CustomGuiProperties.EnumContainer.CRAFTING, blockpos, (IBlockAccess)worldClient, loc, guiscreen); 
        if (guiscreen instanceof net.minecraft.client.gui.inventory.GuiDispenser)
          return getTexturePos(CustomGuiProperties.EnumContainer.DISPENSER, blockpos, (IBlockAccess)worldClient, loc, guiscreen); 
        if (guiscreen instanceof net.minecraft.client.gui.GuiEnchantment)
          return getTexturePos(CustomGuiProperties.EnumContainer.ENCHANTMENT, blockpos, (IBlockAccess)worldClient, loc, guiscreen); 
        if (guiscreen instanceof net.minecraft.client.gui.inventory.GuiFurnace)
          return getTexturePos(CustomGuiProperties.EnumContainer.FURNACE, blockpos, (IBlockAccess)worldClient, loc, guiscreen); 
        if (guiscreen instanceof net.minecraft.client.gui.GuiHopper)
          return getTexturePos(CustomGuiProperties.EnumContainer.HOPPER, blockpos, (IBlockAccess)worldClient, loc, guiscreen); 
      } 
      Entity entity = playerControllerOF.getLastClickEntity();
      if (entity != null) {
        if (guiscreen instanceof net.minecraft.client.gui.inventory.GuiScreenHorseInventory)
          return getTextureEntity(CustomGuiProperties.EnumContainer.HORSE, entity, (IBlockAccess)worldClient, loc); 
        if (guiscreen instanceof net.minecraft.client.gui.GuiMerchant)
          return getTextureEntity(CustomGuiProperties.EnumContainer.VILLAGER, entity, (IBlockAccess)worldClient, loc); 
      } 
      return loc;
    } 
    return loc;
  }
  
  private static ResourceLocation getTexturePos(CustomGuiProperties.EnumContainer container, BlockPos pos, IBlockAccess blockAccess, ResourceLocation loc, GuiScreen screen) {
    CustomGuiProperties[] acustomguiproperties = guiProperties[container.ordinal()];
    if (acustomguiproperties == null)
      return loc; 
    for (int i = 0; i < acustomguiproperties.length; i++) {
      CustomGuiProperties customguiproperties = acustomguiproperties[i];
      if (customguiproperties.matchesPos(container, pos, blockAccess, screen))
        return customguiproperties.getTextureLocation(loc); 
    } 
    return loc;
  }
  
  private static ResourceLocation getTextureEntity(CustomGuiProperties.EnumContainer container, Entity entity, IBlockAccess blockAccess, ResourceLocation loc) {
    CustomGuiProperties[] acustomguiproperties = guiProperties[container.ordinal()];
    if (acustomguiproperties == null)
      return loc; 
    for (int i = 0; i < acustomguiproperties.length; i++) {
      CustomGuiProperties customguiproperties = acustomguiproperties[i];
      if (customguiproperties.matchesEntity(container, entity, blockAccess))
        return customguiproperties.getTextureLocation(loc); 
    } 
    return loc;
  }
  
  public static void update() {
    guiProperties = (CustomGuiProperties[][])null;
    if (Config.isCustomGuis()) {
      List<List<CustomGuiProperties>> list = new ArrayList<>();
      IResourcePack[] airesourcepack = Config.getResourcePacks();
      for (int i = airesourcepack.length - 1; i >= 0; i--) {
        IResourcePack iresourcepack = airesourcepack[i];
        update(iresourcepack, list);
      } 
      guiProperties = propertyListToArray(list);
    } 
  }
  
  private static CustomGuiProperties[][] propertyListToArray(List<List<CustomGuiProperties>> listProps) {
    if (listProps.isEmpty())
      return (CustomGuiProperties[][])null; 
    CustomGuiProperties[][] acustomguiproperties = new CustomGuiProperties[CustomGuiProperties.EnumContainer.VALUES.length][];
    for (int i = 0; i < acustomguiproperties.length; i++) {
      if (listProps.size() > i) {
        List<CustomGuiProperties> list = listProps.get(i);
        if (list != null) {
          CustomGuiProperties[] acustomguiproperties1 = list.<CustomGuiProperties>toArray(new CustomGuiProperties[list.size()]);
          acustomguiproperties[i] = acustomguiproperties1;
        } 
      } 
    } 
    return acustomguiproperties;
  }
  
  private static void update(IResourcePack rp, List<List<CustomGuiProperties>> listProps) {
    String[] astring = ResUtils.collectFiles(rp, "optifine/gui/container/", ".properties", (String[])null);
    Arrays.sort((Object[])astring);
    for (int i = 0; i < astring.length; i++) {
      String s = astring[i];
      Config.dbg("CustomGuis: " + s);
      try {
        ResourceLocation resourcelocation = new ResourceLocation(s);
        InputStream inputstream = rp.getInputStream(resourcelocation);
        if (inputstream == null) {
          Config.warn("CustomGuis file not found: " + s);
        } else {
          PropertiesOrdered propertiesOrdered = new PropertiesOrdered();
          propertiesOrdered.load(inputstream);
          inputstream.close();
          CustomGuiProperties customguiproperties = new CustomGuiProperties((Properties)propertiesOrdered, s);
          if (customguiproperties.isValid(s))
            addToList(customguiproperties, listProps); 
        } 
      } catch (FileNotFoundException var9) {
        Config.warn("CustomGuis file not found: " + s);
      } catch (Exception exception) {
        exception.printStackTrace();
      } 
    } 
  }
  
  private static void addToList(CustomGuiProperties cgp, List<List<CustomGuiProperties>> listProps) {
    if (cgp.getContainer() == null) {
      warn("Invalid container: " + cgp.getContainer());
    } else {
      int i = cgp.getContainer().ordinal();
      while (listProps.size() <= i)
        listProps.add(null); 
      List<CustomGuiProperties> list = listProps.get(i);
      if (list == null) {
        list = new ArrayList<>();
        listProps.set(i, list);
      } 
      list.add(cgp);
    } 
  }
  
  public static PlayerControllerOF getPlayerControllerOF() {
    return playerControllerOF;
  }
  
  public static void setPlayerControllerOF(PlayerControllerOF playerControllerOF) {
    playerControllerOF = playerControllerOF;
  }
  
  private static boolean isChristmas() {
    Calendar calendar = Calendar.getInstance();
    return (calendar.get(2) + 1 == 12 && calendar.get(5) >= 24 && calendar.get(5) <= 26);
  }
  
  private static void warn(String str) {
    Config.warn("[CustomGuis] " + str);
  }
}
