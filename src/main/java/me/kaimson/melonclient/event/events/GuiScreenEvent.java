package me.kaimson.melonclient.event.events;

import me.kaimson.melonclient.event.*;
import net.minecraft.client.gui.*;

public class GuiScreenEvent extends Event
{
    public final GuiScreen screen;
    
    public GuiScreenEvent(final GuiScreen screen) {
        this.screen = screen;
    }
    
    public static class Open extends GuiScreenEvent
    {
        public Open(final GuiScreen screen) {
            super(screen);
        }
    }
}
