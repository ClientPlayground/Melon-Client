package me.kaimson.melonclient.mixins.client;

import net.minecraft.client.*;
import net.minecraft.client.resources.*;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.injection.callback.*;
import me.kaimson.melonclient.*;
import org.lwjgl.input.*;
import net.minecraft.client.gui.*;
import org.spongepowered.asm.mixin.injection.*;
import me.kaimson.melonclient.features.*;
import org.lwjgl.opengl.*;
import org.lwjgl.*;
import net.minecraft.util.*;
import java.nio.*;
import java.io.*;
import org.spongepowered.asm.mixin.*;

@Mixin({ Minecraft.class })
public abstract class MixinMinecraft
{
    @Shadow
    @Final
    private DefaultResourcePack mcDefaultResourcePack;
    @Shadow
    private boolean fullscreen;
    @Shadow
    public int displayWidth;
    @Shadow
    public int displayHeight;
    
    @Inject(method = { "startGame" }, at = { @At("HEAD") })
    private void startGame(final CallbackInfo ci) {
        Client.INSTANCE.onPreInit();
    }
    
    @Inject(method = { "startGame" }, at = { @At("RETURN") })
    private void postStartGame(final CallbackInfo ci) {
        Client.INSTANCE.onPostInit();
    }
    
    @Inject(method = { "shutdownMinecraftApplet" }, at = { @At("HEAD") })
    private void shutdownMinecraftApplet(final CallbackInfo ci) {
        Client.INSTANCE.onShutdown();
    }
    
    @Inject(method = { "dispatchKeypresses" }, at = { @At("HEAD") })
    private void dispatchKeypresses(final CallbackInfo ci) {
        final int i = (Keyboard.getEventKey() == 0) ? Keyboard.getEventCharacter() : Keyboard.getEventKey();
        if (i != 0 && !Keyboard.isRepeatEvent() && (!(Minecraft.getMinecraft().currentScreen instanceof GuiControls) || (((GuiControls)Minecraft.getMinecraft().currentScreen).time <= Minecraft.getSystemTime() - 20L && Keyboard.getEventKeyState()))) {
            Client.INSTANCE.onKeyPress(i);
        }
    }
    
    @ModifyArg(method = { "createDisplay" }, at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/Display;setTitle(Ljava/lang/String;)V", remap = false))
    private String getDisplayTitle(final String title) {
        return "Melon Client 1.8.9";
    }
    
    @Inject(method = { "toggleFullscreen" }, at = { @At(value = "INVOKE", remap = false, target = "Lorg/lwjgl/opengl/Display;setVSyncEnabled(Z)V", shift = At.Shift.AFTER) })
    private void toggleFullscreen(final CallbackInfo ci) throws LWJGLException {
        if (SettingsManager.INSTANCE.borderlessWindow.getBoolean()) {
            if (this.fullscreen) {
                System.setProperty("org.lwjgl.opengl.Window.undecorated", "true");
                Display.setDisplayMode(Display.getDesktopDisplayMode());
                Display.setLocation(0, 0);
                Display.setFullscreen(false);
            }
            else {
                System.setProperty("org.lwjgl.opengl.Window.undecorated", "false");
                Display.setDisplayMode(new DisplayMode(this.displayWidth, this.displayHeight));
            }
        }
        else {
            Display.setFullscreen(this.fullscreen);
            System.setProperty("org.lwjgl.opengl.Window.undecorated", "false");
        }
        Display.setResizable(false);
        Display.setResizable(true);
    }

    /**
     * @author Kaimson the Clown.
     */
    @Overwrite
    private void setWindowIcon() {
        if (Util.getOSType() != Util.EnumOS.OSX) {
            try {
                InputStream inputStream = MixinMinecraft.class.getResourceAsStream("/assets/minecraft/melonclient/icons/windows/logo16.png");
                InputStream inputStream2 = MixinMinecraft.class.getResourceAsStream("/assets/minecraft/melonclient/icons/windows/logo32.png");
                if (inputStream == null) {
                    inputStream = this.mcDefaultResourcePack.getInputStreamAssets(new ResourceLocation("icons/icon_16x16.png"));
                }
                if (inputStream2 == null) {
                    inputStream2 = this.mcDefaultResourcePack.getInputStreamAssets(new ResourceLocation("icons/icon_32x32.png"));
                }
                Display.setIcon(new ByteBuffer[] { this.readImageToBuffer(inputStream), this.readImageToBuffer(inputStream2) });
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    @Shadow
    protected abstract ByteBuffer readImageToBuffer(final InputStream p0);
    
    /**
     * @author Kaimson the Clown
     */
    @Overwrite
    public int getLimitFramerate() {
        return (Minecraft.getMinecraft().theWorld == null && Minecraft.getMinecraft().currentScreen != null) ? 60 : Minecraft.getMinecraft().gameSettings.limitFramerate;
    }
}
