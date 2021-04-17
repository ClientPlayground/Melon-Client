package me.kaimson.melonclient.mixins.client.gui;

import net.minecraft.client.gui.*;

import java.util.*;

import me.kaimson.melonclient.gui.*;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.injection.*;
import net.minecraft.client.renderer.*;
import me.kaimson.melonclient.*;
import me.kaimson.melonclient.gui.utils.*;
import org.spongepowered.asm.mixin.*;

@Mixin(GuiMainMenu.class)
public class MixinGuiMainMenu extends GuiScreen implements GuiYesNoCallback {
    @ModifyArg(method = "initGui", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0))
    private Object moveOptionsButton(Object buttonIn) {
        GuiButton guiButton = (GuiButton) buttonIn;
        guiButton.xPosition += 23;
        guiButton.setWidth(154);
        return guiButton;
    }

    @Redirect(method = "initGui", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 1))
    private boolean moveQuitButton(List<GuiButton> list, Object e) {
        final GuiButton quit = (GuiButton) e;
        return list.add(new GuiButtonIcon(quit.id, quit.xPosition + 78, quit.yPosition, 20, 20, "close.png"));
    }

    @ModifyArg(method = "initGui", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 2))
    private Object moveLanguageButton(Object buttonIn) {
        final GuiButton guiButton = (GuiButton) buttonIn;
        guiButton.xPosition += 24;
        return guiButton;
    }

    /**
     * @author Kaimson the Clown.
     */
    @Overwrite
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        this.mc.getTextureManager().bindTexture(Client.BACKGROUND);
        GuiUtils.drawModalRectWithCustomSizedTexture(0, 0, (float) this.width, (float) this.height, this.width, this.height, (float) this.width, (float) this.height);
        this.mc.getTextureManager().bindTexture(Client.LOGO);
        GuiUtils.drawModalRectWithCustomSizedTexture(this.width / 2 - 118 + 2, 50, 0.0f, 0.0f, 113, 36, 242.0f, 36.0f);
        GuiUtils.setGlColor(Client.getMainColor(255));
        GuiUtils.drawModalRectWithCustomSizedTexture(this.width / 2, 50, 112.0f, 0.0f, 124, 36, 237.0f, 36.0f);
        GuiUtils.setGlColor(Client.getMainColor(255));
        GlStateManager.popMatrix();
        this.drawString(this.fontRendererObj, "Melon Client 1.8.9", 2, this.height - 10, -1);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
