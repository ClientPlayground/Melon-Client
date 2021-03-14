package me.kaimson.melonclient.ingames.utils;

import java.awt.Color;
import java.util.Collection;
import me.kaimson.melonclient.gui.GuiUtils;
import me.kaimson.melonclient.ingames.IngameDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.EnumChatFormatting;

public class Scoreboard {
  private final Minecraft mc = Minecraft.getMinecraft();
  
  public void render(IngameDisplay display, int x, int y) {
    net.minecraft.scoreboard.Scoreboard scoreboard = this.mc.theWorld.getScoreboard();
    ScoreObjective scoreobjective = null;
    ScorePlayerTeam scoreplayerteam = scoreboard.getPlayersTeam(this.mc.thePlayer.getCommandSenderName());
    if (scoreplayerteam != null) {
      int i1 = scoreplayerteam.getChatFormat().getColorIndex();
      if (i1 >= 0)
        scoreobjective = scoreboard.getObjectiveInDisplaySlot(3 + i1); 
    } 
    ScoreObjective scoreobjective1 = (scoreobjective != null) ? scoreobjective : scoreboard.getObjectiveInDisplaySlot(1);
    render(display, x, y, scoreobjective1, new ScaledResolution(this.mc));
  }
  
  private void render(IngameDisplay display, int x, int y, ScoreObjective objective, ScaledResolution scaledRes) {
    if (objective == null)
      return; 
    float scale = display.getScale();
    GlStateManager.pushMatrix();
    GlStateManager.scale(scale, scale, 1.0F);
    net.minecraft.scoreboard.Scoreboard scoreboard = objective.getScoreboard();
    Collection<Score> collection = scoreboard.getSortedScores(objective);
    GuiUtils.drawRect(x, y + this.mc.fontRendererObj.FONT_HEIGHT, x + display.getWidth(), y + (collection.size() + 1) * this.mc.fontRendererObj.FONT_HEIGHT + 1, (new Color(((Integer)IngameDisplay.SCOREBOARD_BACKGROUND_COLOR.getOrDefault(Integer.valueOf(1342177280))).intValue(), true)).getRGB());
    GuiUtils.drawRect(x, y, x + display.getWidth(), y + this.mc.fontRendererObj.FONT_HEIGHT, (new Color(((Integer)IngameDisplay.SCOREBOARD_TOP_BACKGROUND_COLOR.getOrDefault(Integer.valueOf(1610612736))).intValue(), true)).getRGB());
    int j = 0;
    int width = this.mc.fontRendererObj.getStringWidth(objective.getDisplayName());
    int height = (collection.size() + 1) * this.mc.fontRendererObj.FONT_HEIGHT + 1;
    for (Score score : collection) {
      j++;
      ScorePlayerTeam scorePlayerTeam = scoreboard.getPlayersTeam(score.getPlayerName());
      String s = ScorePlayerTeam.formatPlayerName((Team)scorePlayerTeam, score.getPlayerName()) + ": " + EnumChatFormatting.RED + score.getScorePoints();
      width = Math.max(width, this.mc.fontRendererObj.getStringWidth(s));
      GuiUtils.drawString(ScorePlayerTeam.formatPlayerName((Team)scorePlayerTeam, score.getPlayerName()), x + 2, y + height - j * this.mc.fontRendererObj.FONT_HEIGHT, 553648127);
    } 
    j = 0;
    for (Score score : collection) {
      j++;
      String s2 = EnumChatFormatting.RED + "" + score.getScorePoints();
      if (IngameDisplay.SCOREBOARD_RED_NUMBERS.isEnabled())
        GuiUtils.drawString(s2, x + width + 5 - this.mc.fontRendererObj.getStringWidth(s2), y + height - j * this.mc.fontRendererObj.FONT_HEIGHT, 553648127); 
    } 
    if (IngameDisplay.SCOREBOARD_RED_NUMBERS.isEnabled())
      width += 5; 
    GuiUtils.drawCenteredString(objective.getDisplayName(), x + width / 2 + 1, y + 1);
    display.setWidth(width);
    display.setHeight(height);
    GlStateManager.scale(Math.pow(scale, -1.0D), Math.pow(scale, -1.0D), 1.0D);
    GlStateManager.popMatrix();
  }
}
