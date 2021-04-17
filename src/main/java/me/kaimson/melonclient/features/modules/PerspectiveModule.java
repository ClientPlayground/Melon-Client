package me.kaimson.melonclient.features.modules;

import me.kaimson.melonclient.features.*;
import me.kaimson.melonclient.utils.*;
import net.minecraft.client.*;
import org.lwjgl.opengl.*;

public class PerspectiveModule extends Module
{
    public static PerspectiveModule INSTANCE;
    private final Setting freelook;
    private final Setting behindYou;
    public float cameraYaw;
    public float cameraPitch;
    private int previousView;
    private boolean wasDown;
    private int behindYouPrevView;
    private boolean behindYouWasDown;
    
    public PerspectiveModule() {
        super("Perspective", 22);
        new Setting(this, "Freelook Options");
        this.freelook = new Setting(this, "Keybind", "freelook.keybind").setDefault(new KeyBinding(47));
        new Setting(this, "Behind You Options");
        this.behindYou = new Setting(this, "Keybind", "behindyou.keybind").setDefault(new KeyBinding(56));
        PerspectiveModule.INSTANCE = this;
    }
    
    public void onTick() {
        final boolean active = ((KeyBinding)this.behindYou.getObject()).isKeyDown();
        if (this.behindYouWasDown != active) {
            if (!(this.behindYouWasDown = active)) {
                Minecraft.getMinecraft().gameSettings.thirdPersonView = this.behindYouPrevView;
            }
            this.behindYouPrevView = Minecraft.getMinecraft().gameSettings.thirdPersonView;
            if (this.behindYouWasDown && Minecraft.getMinecraft().gameSettings.thirdPersonView != 2) {
                Minecraft.getMinecraft().gameSettings.thirdPersonView = 2;
            }
        }
    }
    
    public boolean isHeld() {
        final KeyBinding keyBinding = (KeyBinding)this.freelook.getObject();
        final boolean active = keyBinding.isKeyDown();
        if (!active) {
            this.cameraYaw = Minecraft.getMinecraft().thePlayer.cameraYaw;
            this.cameraPitch = Minecraft.getMinecraft().thePlayer.cameraPitch;
        }
        if (this.wasDown != active) {
            if (!(this.wasDown = active)) {
                Minecraft.getMinecraft().gameSettings.thirdPersonView = this.previousView;
            }
            this.previousView = Minecraft.getMinecraft().gameSettings.thirdPersonView;
            if (this.wasDown && (Minecraft.getMinecraft().gameSettings.thirdPersonView == 0 || Minecraft.getMinecraft().gameSettings.thirdPersonView == 2)) {
                Minecraft.getMinecraft().gameSettings.thirdPersonView = 1;
            }
        }
        return active;
    }
    
    public static boolean overrideMouse() {
        if (Minecraft.getMinecraft().inGameHasFocus && Display.isActive()) {
            if (!PerspectiveModule.INSTANCE.isHeld()) {
                return true;
            }
            Minecraft.getMinecraft().mouseHelper.mouseXYChange();
            final float f1 = Minecraft.getMinecraft().gameSettings.mouseSensitivity * 0.6f + 0.2f;
            final float f2 = f1 * f1 * f1 * 8.0f;
            final float f3 = Minecraft.getMinecraft().mouseHelper.deltaX * f2;
            final float f4 = Minecraft.getMinecraft().mouseHelper.deltaY * f2;
            final PerspectiveModule instance = PerspectiveModule.INSTANCE;
            instance.cameraYaw += f3 * 0.15f;
            final PerspectiveModule instance2 = PerspectiveModule.INSTANCE;
            instance2.cameraPitch += f4 * 0.15f;
            if (PerspectiveModule.INSTANCE.cameraPitch > 90.0f) {
                PerspectiveModule.INSTANCE.cameraPitch = 90.0f;
            }
            if (PerspectiveModule.INSTANCE.cameraPitch < -90.0f) {
                PerspectiveModule.INSTANCE.cameraPitch = -90.0f;
            }
            Minecraft.getMinecraft().renderGlobal.setDisplayListEntitiesDirty();
        }
        return false;
    }
    
    public static float getCameraYaw() {
        return PerspectiveModule.INSTANCE.isHeld() ? PerspectiveModule.INSTANCE.cameraYaw : Minecraft.getMinecraft().thePlayer.rotationYaw;
    }
    
    public static float getCameraPitch() {
        return PerspectiveModule.INSTANCE.isHeld() ? PerspectiveModule.INSTANCE.cameraPitch : Minecraft.getMinecraft().thePlayer.rotationPitch;
    }
}
