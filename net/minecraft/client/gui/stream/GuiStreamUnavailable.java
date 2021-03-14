package net.minecraft.client.gui.stream;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.stream.IStream;
import net.minecraft.client.stream.NullStream;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.Session;
import net.minecraft.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import tv.twitch.ErrorCode;

public class GuiStreamUnavailable extends GuiScreen {
  private static final Logger field_152322_a = LogManager.getLogger();
  
  private final IChatComponent field_152324_f;
  
  private final GuiScreen parentScreen;
  
  private final Reason field_152326_h;
  
  private final List<ChatComponentTranslation> field_152327_i;
  
  private final List<String> field_152323_r;
  
  public GuiStreamUnavailable(GuiScreen p_i1070_1_, Reason p_i1070_2_) {
    this(p_i1070_1_, p_i1070_2_, (List<ChatComponentTranslation>)null);
  }
  
  public GuiStreamUnavailable(GuiScreen parentScreenIn, Reason p_i46311_2_, List<ChatComponentTranslation> p_i46311_3_) {
    this.field_152324_f = (IChatComponent)new ChatComponentTranslation("stream.unavailable.title", new Object[0]);
    this.field_152323_r = Lists.newArrayList();
    this.parentScreen = parentScreenIn;
    this.field_152326_h = p_i46311_2_;
    this.field_152327_i = p_i46311_3_;
  }
  
  public void initGui() {
    if (this.field_152323_r.isEmpty()) {
      this.field_152323_r.addAll(this.fontRendererObj.listFormattedStringToWidth(this.field_152326_h.func_152561_a().getFormattedText(), (int)(this.width * 0.75F)));
      if (this.field_152327_i != null) {
        this.field_152323_r.add("");
        for (ChatComponentTranslation chatcomponenttranslation : this.field_152327_i)
          this.field_152323_r.add(chatcomponenttranslation.getUnformattedTextForChat()); 
      } 
    } 
    if (this.field_152326_h.func_152559_b() != null) {
      this.buttonList.add(new GuiButton(0, this.width / 2 - 155, this.height - 50, 150, 20, I18n.format("gui.cancel", new Object[0])));
      this.buttonList.add(new GuiButton(1, this.width / 2 - 155 + 160, this.height - 50, 150, 20, I18n.format(this.field_152326_h.func_152559_b().getFormattedText(), new Object[0])));
    } else {
      this.buttonList.add(new GuiButton(0, this.width / 2 - 75, this.height - 50, 150, 20, I18n.format("gui.cancel", new Object[0])));
    } 
  }
  
  public void onGuiClosed() {}
  
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    drawDefaultBackground();
    int i = Math.max((int)(this.height * 0.85D / 2.0D - ((this.field_152323_r.size() * this.fontRendererObj.FONT_HEIGHT) / 2.0F)), 50);
    drawCenteredString(this.fontRendererObj, this.field_152324_f.getFormattedText(), this.width / 2, i - this.fontRendererObj.FONT_HEIGHT * 2, 16777215);
    for (String s : this.field_152323_r) {
      drawCenteredString(this.fontRendererObj, s, this.width / 2, i, 10526880);
      i += this.fontRendererObj.FONT_HEIGHT;
    } 
    super.drawScreen(mouseX, mouseY, partialTicks);
  }
  
  protected void actionPerformed(GuiButton button) throws IOException {
    if (button.enabled) {
      if (button.id == 1)
        switch (this.field_152326_h) {
          case INVALID_TOKEN:
          case ERROR:
            func_152320_a("https://account.mojang.com/me/settings");
            break;
          case null:
            func_152320_a("https://account.mojang.com/migrate");
            break;
          case null:
            func_152320_a("http://www.apple.com/osx/");
            break;
          case null:
          case null:
          case null:
            func_152320_a("http://bugs.mojang.com/browse/MC");
            break;
        }  
      this.mc.displayGuiScreen(this.parentScreen);
    } 
  }
  
  private void func_152320_a(String p_152320_1_) {
    try {
      Class<?> oclass = Class.forName("java.awt.Desktop");
      Object object = oclass.getMethod("getDesktop", new Class[0]).invoke(null, new Object[0]);
      oclass.getMethod("browse", new Class[] { URI.class }).invoke(object, new Object[] { new URI(p_152320_1_) });
    } catch (Throwable throwable) {
      field_152322_a.error("Couldn't open link", throwable);
    } 
  }
  
  public static void func_152321_a(GuiScreen p_152321_0_) {
    Minecraft minecraft = Minecraft.getMinecraft();
    IStream istream = minecraft.getTwitchStream();
    if (!OpenGlHelper.framebufferSupported) {
      List<ChatComponentTranslation> list = Lists.newArrayList();
      list.add(new ChatComponentTranslation("stream.unavailable.no_fbo.version", new Object[] { GL11.glGetString(7938) }));
      list.add(new ChatComponentTranslation("stream.unavailable.no_fbo.blend", new Object[] { Boolean.valueOf((GLContext.getCapabilities()).GL_EXT_blend_func_separate) }));
      list.add(new ChatComponentTranslation("stream.unavailable.no_fbo.arb", new Object[] { Boolean.valueOf((GLContext.getCapabilities()).GL_ARB_framebuffer_object) }));
      list.add(new ChatComponentTranslation("stream.unavailable.no_fbo.ext", new Object[] { Boolean.valueOf((GLContext.getCapabilities()).GL_EXT_framebuffer_object) }));
      minecraft.displayGuiScreen(new GuiStreamUnavailable(p_152321_0_, Reason.NO_FBO, list));
    } else if (istream instanceof NullStream) {
      if (((NullStream)istream).func_152937_a().getMessage().contains("Can't load AMD 64-bit .dll on a IA 32-bit platform")) {
        minecraft.displayGuiScreen(new GuiStreamUnavailable(p_152321_0_, Reason.LIBRARY_ARCH_MISMATCH));
      } else {
        minecraft.displayGuiScreen(new GuiStreamUnavailable(p_152321_0_, Reason.LIBRARY_FAILURE));
      } 
    } else if (!istream.func_152928_D() && istream.func_152912_E() == ErrorCode.TTV_EC_OS_TOO_OLD) {
      switch (Util.getOSType()) {
        case INVALID_TOKEN:
          minecraft.displayGuiScreen(new GuiStreamUnavailable(p_152321_0_, Reason.UNSUPPORTED_OS_WINDOWS));
          return;
        case ERROR:
          minecraft.displayGuiScreen(new GuiStreamUnavailable(p_152321_0_, Reason.UNSUPPORTED_OS_MAC));
          return;
      } 
      minecraft.displayGuiScreen(new GuiStreamUnavailable(p_152321_0_, Reason.UNSUPPORTED_OS_OTHER));
    } else if (!minecraft.getTwitchDetails().containsKey("twitch_access_token")) {
      if (minecraft.getSession().getSessionType() == Session.Type.LEGACY) {
        minecraft.displayGuiScreen(new GuiStreamUnavailable(p_152321_0_, Reason.ACCOUNT_NOT_MIGRATED));
      } else {
        minecraft.displayGuiScreen(new GuiStreamUnavailable(p_152321_0_, Reason.ACCOUNT_NOT_BOUND));
      } 
    } else if (!istream.func_152913_F()) {
      switch (istream.func_152918_H()) {
        case INVALID_TOKEN:
          minecraft.displayGuiScreen(new GuiStreamUnavailable(p_152321_0_, Reason.FAILED_TWITCH_AUTH));
          return;
      } 
      minecraft.displayGuiScreen(new GuiStreamUnavailable(p_152321_0_, Reason.FAILED_TWITCH_AUTH_ERROR));
    } else if (istream.func_152912_E() != null) {
      List<ChatComponentTranslation> list1 = Arrays.asList(new ChatComponentTranslation[] { new ChatComponentTranslation("stream.unavailable.initialization_failure.extra", new Object[] { ErrorCode.getString(istream.func_152912_E()) }) });
      minecraft.displayGuiScreen(new GuiStreamUnavailable(p_152321_0_, Reason.INITIALIZATION_FAILURE, list1));
    } else {
      minecraft.displayGuiScreen(new GuiStreamUnavailable(p_152321_0_, Reason.UNKNOWN));
    } 
  }
  
  public enum Reason {
    NO_FBO((String)new ChatComponentTranslation("stream.unavailable.no_fbo", new Object[0])),
    LIBRARY_ARCH_MISMATCH((String)new ChatComponentTranslation("stream.unavailable.library_arch_mismatch", new Object[0])),
    LIBRARY_FAILURE((String)new ChatComponentTranslation("stream.unavailable.library_failure", new Object[0]), new ChatComponentTranslation("stream.unavailable.report_to_mojang", new Object[0])),
    UNSUPPORTED_OS_WINDOWS((String)new ChatComponentTranslation("stream.unavailable.not_supported.windows", new Object[0])),
    UNSUPPORTED_OS_MAC((String)new ChatComponentTranslation("stream.unavailable.not_supported.mac", new Object[0]), new ChatComponentTranslation("stream.unavailable.not_supported.mac.okay", new Object[0])),
    UNSUPPORTED_OS_OTHER((String)new ChatComponentTranslation("stream.unavailable.not_supported.other", new Object[0])),
    ACCOUNT_NOT_MIGRATED((String)new ChatComponentTranslation("stream.unavailable.account_not_migrated", new Object[0]), new ChatComponentTranslation("stream.unavailable.account_not_migrated.okay", new Object[0])),
    ACCOUNT_NOT_BOUND((String)new ChatComponentTranslation("stream.unavailable.account_not_bound", new Object[0]), new ChatComponentTranslation("stream.unavailable.account_not_bound.okay", new Object[0])),
    FAILED_TWITCH_AUTH((String)new ChatComponentTranslation("stream.unavailable.failed_auth", new Object[0]), new ChatComponentTranslation("stream.unavailable.failed_auth.okay", new Object[0])),
    FAILED_TWITCH_AUTH_ERROR((String)new ChatComponentTranslation("stream.unavailable.failed_auth_error", new Object[0])),
    INITIALIZATION_FAILURE((String)new ChatComponentTranslation("stream.unavailable.initialization_failure", new Object[0]), new ChatComponentTranslation("stream.unavailable.report_to_mojang", new Object[0])),
    UNKNOWN((String)new ChatComponentTranslation("stream.unavailable.unknown", new Object[0]), new ChatComponentTranslation("stream.unavailable.report_to_mojang", new Object[0]));
    
    private final IChatComponent field_152574_m;
    
    private final IChatComponent field_152575_n;
    
    Reason(IChatComponent p_i1067_3_, IChatComponent p_i1067_4_) {
      this.field_152574_m = p_i1067_3_;
      this.field_152575_n = p_i1067_4_;
    }
    
    public IChatComponent func_152561_a() {
      return this.field_152574_m;
    }
    
    public IChatComponent func_152559_b() {
      return this.field_152575_n;
    }
  }
}
