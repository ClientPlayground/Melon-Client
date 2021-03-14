package me.kaimson.melonclient.ingames.utils.ReplayMod.core;

import com.google.common.net.PercentEscaper;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.AbstractGuiScrollable;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiScrollable;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiLabel;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.HorizontalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.Layout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.LayoutData;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.VerticalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Colors;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;
import java.util.function.Consumer;
import me.kaimson.melonclient.Client;
import me.kaimson.melonclient.ingames.utils.ReplayMod.customgui.GuiButton;
import me.kaimson.melonclient.ingames.utils.ReplayMod.customgui.GuiInfoPopup;
import me.kaimson.melonclient.ingames.utils.ReplayMod.customgui.GuiPanel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.core.Logger;
import org.lwjgl.input.Keyboard;

public class Utils {
  public static final Image DEFAULT_THUMBNAIL;
  
  static {
    Image thumbnail;
    try {
      thumbnail = Image.read(Utils.class.getResourceAsStream("/assets/minecraft/melonclient/replaymod/default_thumb.jpg"));
    } catch (Exception e) {
      thumbnail = new Image(1, 1);
      e.printStackTrace();
    } 
    DEFAULT_THUMBNAIL = thumbnail;
  }
  
  private static final PercentEscaper REPLAY_NAME_ENCODER = new PercentEscaper(".-_ ", false);
  
  public static String replayNameToFileName(String replayName) {
    return REPLAY_NAME_ENCODER.escape(replayName) + ".mcpr";
  }
  
  public static boolean isCtrlDown() {
    return (Keyboard.isKeyDown(29) || Keyboard.isKeyDown(157));
  }
  
  public static String fileNameToReplayName(String fileName) {
    try {
      String baseName = FilenameUtils.getBaseName(fileName);
      try {
        return URLDecoder.decode(baseName, Charsets.UTF_8.name());
      } catch (IllegalArgumentException e) {
        return baseName;
      } 
    } catch (UnsupportedEncodingException $ex) {
      throw $ex;
    } 
  }
  
  public static ResourceLocation getResourceLocationForPlayerUUID(UUID uuid) {
    ResourceLocation skinLocation;
    NetworkPlayerInfo info = Minecraft.getMinecraft().getNetHandler().getPlayerInfo(uuid);
    if (info != null && info.hasLocationSkin()) {
      skinLocation = info.getLocationSkin();
    } else {
      skinLocation = DefaultPlayerSkin.getDefaultSkin(uuid);
    } 
    return skinLocation;
  }
  
  public static String convertSecondsToShortString(int seconds) {
    int hours = seconds / 3600;
    int min = seconds / 60 - hours * 60;
    int sec = seconds - min * 60 + hours * 60 * 60;
    StringBuilder builder = new StringBuilder();
    if (hours > 0)
      builder.append(String.format("%02d", new Object[] { Integer.valueOf(hours) })).append(":"); 
    builder.append(String.format("%02d", new Object[] { Integer.valueOf(min) })).append(":");
    builder.append(String.format("%02d", new Object[] { Integer.valueOf(sec) }));
    return builder.toString();
  }
  
  public static <T> void addCallback(ListenableFuture<T> future, final Consumer<T> onSuccess, final Consumer<Throwable> onFailure) {
    Futures.addCallback(future, new FutureCallback<T>() {
          public void onSuccess(T result) {
            onSuccess.accept(result);
          }
          
          public void onFailure(Throwable t) {
            onFailure.accept(t);
          }
        });
  }
  
  public static GuiInfoPopup error(GuiContainer container, CrashReport crashReport, Runnable onClose) {
    return error(Client.coreLogger, container, crashReport, onClose);
  }
  
  private static GuiInfoPopup error(final Logger logger, GuiContainer container, CrashReport crashReport, final Runnable onClose) {
    String crashReportStr = crashReport.getCompleteReport();
    logger.error(crashReportStr);
    if (crashReport.getFile() == null) {
      try {
        File folder = new File((Minecraft.getMinecraft()).mcDataDir, "crash-reports");
        File file = new File(folder, "crash-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + "-client.txt");
        logger.debug("Saving crash report to file: {}", new Object[] { file });
        crashReport.saveToFile(file);
      } catch (Throwable t) {
        logger.error("Saving crash report file:", t);
      } 
    } else {
      logger.debug("Not saving crash report as file already exists: {}", new Object[] { crashReport.getFile() });
    } 
    logger.trace("Opening crash report popup GUI");
    GuiCrashReportPopup popup = new GuiCrashReportPopup(container, crashReportStr);
    Futures.addCallback(popup.getFuture(), new FutureCallback<Void>() {
          public void onSuccess(Void result) {
            logger.trace("Crash report popup closed");
            if (onClose != null)
              onClose.run(); 
          }
          
          public void onFailure(Throwable t) {
            logger.error("During error popup:", t);
          }
        });
    return popup;
  }
  
  private static class GuiCrashReportPopup extends GuiInfoPopup {
    private final GuiScrollable scrollable;
    
    public GuiCrashReportPopup(GuiContainer container, String crashReport) {
      super(container);
      setBackgroundColor(Colors.DARK_TRANSPARENT);
      getInfo().addElements((LayoutData)new VerticalLayout.Data(0.5D), new GuiElement[] { (GuiElement)((GuiLabel)(new GuiLabel())
            .setColor(Colors.BLACK)).setI18nText("replaymod.gui.unknownerror", new Object[0]), 
            
            (GuiElement)(this.scrollable = (GuiScrollable)((GuiScrollable)((GuiScrollable)(new GuiScrollable()).setScrollDirection(AbstractGuiScrollable.Direction.VERTICAL)).setLayout((Layout)(new VerticalLayout()).setSpacing(2))).addElements(null, (GuiElement[])Arrays.stream(crashReport.replace("\t", "    ").split("\n")).map(l -> (GuiLabel)((GuiLabel)(new GuiLabel()).setText(l)).setColor(Colors.BLACK))
              .toArray(x$0 -> new GuiElement[x$0]))) });
      GuiButton copyToClipboardButton = (GuiButton)((GuiButton)((GuiButton)(new GuiButton()).setI18nLabel("chat.copy", new Object[0])).onClick(() -> {
            StringSelection selection = new StringSelection(crashReport);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
          })).setSize(150, 20);
      GuiButton closeButton = getCloseButton();
      this.popup.removeElement((GuiElement)closeButton);
      this.popup.addElements((LayoutData)new VerticalLayout.Data(1.0D), new GuiElement[] { (GuiElement)((GuiPanel)((GuiPanel)(new GuiPanel())
            .setLayout((Layout)(new HorizontalLayout()).setSpacing(5))).setSize(305, 20))
            .addElements(null, new GuiElement[] { (GuiElement)copyToClipboardButton, (GuiElement)closeButton }) });
      open();
    }
    
    public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
      this.scrollable.setSize(size.getWidth() * 3 / 4, size.getHeight() * 3 / 4);
      super.draw(renderer, size, renderInfo);
    }
  }
}
