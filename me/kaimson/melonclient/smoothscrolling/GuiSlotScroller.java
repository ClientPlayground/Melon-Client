package me.kaimson.melonclient.smoothscrolling;

import me.kaimson.melonclient.Client;
import net.minecraft.client.gui.GuiSlot;

public class GuiSlotScroller implements RunSixtyTimesEverySec {
  private final GuiSlot list;
  
  public GuiSlotScroller(GuiSlot list) {
    this.list = list;
  }
  
  public void run() {
    if (this.list == null) {
      Client.log("LIST IS GONE!");
      return;
    } 
    double scrollVelocity = this.list.scrollVelocity;
    if (scrollVelocity == 0.0D && this.list.amountScrolled >= 0.0D && this.list.amountScrolled <= this.list.func_148135_f()) {
      unregisterTick();
    } else {
      double change = this.list.scrollVelocity * 0.3D;
      if (this.list.scrollVelocity != 0.0D) {
        GuiSlot list = this.list;
        list.amountScrolled = (float)(list.amountScrolled + change);
        double minus = list.scrollVelocity * ((this.list.amountScrolled >= 0.0D && this.list.amountScrolled <= this.list.func_148135_f()) ? 0.2D : 0.4D);
        list.scrollVelocity = (float)(list.scrollVelocity - minus);
        if (Math.abs(list.scrollVelocity) < 0.1D)
          list.scrollVelocity = 0.0D; 
      } 
      if (this.list.amountScrolled < 0.0D && this.list.scrollVelocity == 0.0D) {
        this.list.amountScrolled = (float)Math.min(this.list.amountScrolled + (0.0D - this.list.amountScrolled) * 0.2D, 0.0D);
        if (this.list.amountScrolled > -0.1D && this.list.amountScrolled < 0.0D)
          this.list.amountScrolled = 0.0F; 
      } else if (this.list.amountScrolled > this.list.func_148135_f() && this.list.scrollVelocity == 0.0D) {
        this.list.amountScrolled = (float)Math.max(this.list.amountScrolled - (this.list.amountScrolled - this.list.func_148135_f()) * 0.2D, this.list.func_148135_f());
        if (this.list.amountScrolled > this.list.func_148135_f() && this.list.amountScrolled < this.list.func_148135_f() + 0.1D)
          this.list.amountScrolled = this.list.func_148135_f(); 
      } 
    } 
  }
}
