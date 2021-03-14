package net.minecraft.realms;

import java.util.List;
import net.minecraft.util.IChatComponent;

public class DisconnectedRealmsScreen extends RealmsScreen {
  private String title;
  
  private IChatComponent reason;
  
  private List<String> lines;
  
  private final RealmsScreen parent;
  
  private int textHeight;
  
  public DisconnectedRealmsScreen(RealmsScreen parentIn, String unlocalizedTitle, IChatComponent reasonIn) {
    this.parent = parentIn;
    this.title = getLocalizedString(unlocalizedTitle);
    this.reason = reasonIn;
  }
  
  public void init() {
    Realms.setConnectedToRealms(false);
    buttonsClear();
    this.lines = fontSplit(this.reason.getFormattedText(), width() - 50);
    this.textHeight = this.lines.size() * fontLineHeight();
    buttonsAdd(newButton(0, width() / 2 - 100, height() / 2 + this.textHeight / 2 + fontLineHeight(), getLocalizedString("gui.back")));
  }
  
  public void keyPressed(char p_keyPressed_1_, int p_keyPressed_2_) {
    if (p_keyPressed_2_ == 1)
      Realms.setScreen(this.parent); 
  }
  
  public void buttonClicked(RealmsButton p_buttonClicked_1_) {
    if (p_buttonClicked_1_.id() == 0)
      Realms.setScreen(this.parent); 
  }
  
  public void render(int p_render_1_, int p_render_2_, float p_render_3_) {
    renderBackground();
    drawCenteredString(this.title, width() / 2, height() / 2 - this.textHeight / 2 - fontLineHeight() * 2, 11184810);
    int i = height() / 2 - this.textHeight / 2;
    if (this.lines != null)
      for (String s : this.lines) {
        drawCenteredString(s, width() / 2, i, 16777215);
        i += fontLineHeight();
      }  
    super.render(p_render_1_, p_render_2_, p_render_3_);
  }
}
