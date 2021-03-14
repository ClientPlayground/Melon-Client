package com.replaymod.lib.de.johni0702.minecraft.gui.element;

public interface IGuiClickable<T extends IGuiClickable<T>> extends GuiElement<T> {
  T onClick(Runnable paramRunnable);
}
