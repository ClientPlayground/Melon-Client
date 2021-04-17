package me.kaimson.melonclient.gui;

import java.util.*;
import me.kaimson.melonclient.gui.components.*;
import com.google.common.collect.*;
import java.io.*;
import me.kaimson.melonclient.gui.elements.settings.*;
import java.util.concurrent.*;
import org.lwjgl.opengl.*;
import me.kaimson.melonclient.gui.elements.*;
import net.minecraft.client.gui.*;
import me.kaimson.melonclient.gui.settings.*;

public abstract class GuiScreen extends net.minecraft.client.gui.GuiScreen
{
    public final List<Element> elements;
    public final List<Component> components;
    private ScheduledExecutorService executorService;
    private GuiScrolling scroll;
    private int xPosition;
    private int yPosition;
    private int scissorX;
    private int scissorY;
    private boolean scissor;
    protected boolean enableInput;
    
    public GuiScreen() {
        this.elements = Lists.newArrayList();
        this.components = Lists.newArrayList();
        this.enableInput = true;
    }
    
    public void initGui() {
        super.initGui();
        this.elements.forEach(Element::init);
    }
    
    protected void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.elements.forEach(element -> element.mouseClicked(mouseX, mouseY, mouseButton));
        this.components.forEach(component -> component.elements.forEach(element -> element.mouseClicked(mouseX, mouseY, mouseButton)));
    }
    
    protected void mouseReleased(final int mouseX, final int mouseY, final int state) {
        super.mouseReleased(mouseX, mouseY, state);
        this.elements.forEach(element -> element.mouseReleased(mouseX, mouseY, state));
        this.components.forEach(component -> component.elements.forEach(element -> element.mouseReleased(mouseX, mouseY, state)));
    }
    
    protected void keyTyped(final char typedChar, final int keyCode) throws IOException {
        if (keyCode == 1) {
            if (this.elements.stream().noneMatch(element -> element instanceof SettingElementKeybind) || this.elements.stream().filter(element -> element instanceof SettingElementKeybind).noneMatch(element -> ((SettingElementKeybind) element).selection)) {
                super.keyTyped(typedChar, keyCode);
            }
        }
        else {
            super.keyTyped(typedChar, keyCode);
        }
        this.elements.forEach(element -> element.keyTyped(typedChar, keyCode));
        this.components.forEach(component -> component.elements.forEach(element -> element.keyTyped(typedChar, keyCode)));
    }
    
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        if (this.scroll != null && this.enableInput) {
            this.scroll.handleMouseInput();
        }
    }
    
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (this.executorService == null) {
            (this.executorService = Executors.newSingleThreadScheduledExecutor()).scheduleAtFixedRate(() -> {
                this.elements.forEach(Element::update);
                this.components.forEach(component -> component.elements.forEach(Element::update));
                return;
            }, 0L, 16L, TimeUnit.MILLISECONDS);
        }
        if (this.scroll != null) {
            this.scroll.drawScreen(mouseX, mouseY, partialTicks);
            this.yPosition = this.scroll.getAmountScrolled();
        }
        this.elements.forEach(element -> {
            if (element.shouldScissor) {
                if (this.scissor) {
                    GL11.glEnable(3089);
                }
                element.addOffsetToY(-this.yPosition);
            }
            else {
                GL11.glDisable(3089);
            }
            if (element instanceof ElementModule) {
                if (mouseY > this.scissorY + 18) {
                    element.hovered(mouseX, mouseY);
                }
                else {
                    element.hovered = false;
                }
            }
            element.render(mouseX, mouseY, partialTicks);
            if (element.shouldScissor) {
                if (this.scissor) {
                    GL11.glDisable(3089);
                }
                element.addOffsetToY(this.yPosition);
            }
            return;
        });
        this.components.forEach(component -> component.render(mouseX, mouseY, partialTicks));
    }
    
    protected void scissorFunc(final ScaledResolution sr, final int x, final int y, final int width, final int height) {
        this.scissorX = x;
        this.scissorY = y / sr.getScaleFactor();
        this.scissor = true;
        GL11.glScissor(x, y, width, height);
    }
    
    public void onGuiClosed() {
        super.onGuiClosed();
        if (this.executorService != null) {
            this.executorService.shutdownNow();
            this.executorService = null;
        }
    }
    
    protected void registerScroll(final GuiScrolling scroll) {
        (this.scroll = scroll).registerScrollButtons(7, 8);
    }
    
    protected void expandScroll(final int expandedAmount) {
        ((GuiModules.Scroll)this.scroll).expandBy(expandedAmount);
        this.scroll.scrollTo(this.scroll.getAmountScrolled() + expandedAmount, true, 1500L);
    }
    
    protected void setScrollState(final int scrollState) {
        this.scroll.scrollTo(scrollState, true, 1500L);
    }
    
    protected int getScrollState() {
        return this.scroll.getAmountScrolled();
    }
}
