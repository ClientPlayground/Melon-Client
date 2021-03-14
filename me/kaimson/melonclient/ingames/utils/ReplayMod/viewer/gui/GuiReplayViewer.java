package me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.gui;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.mojang.realmsclient.gui.ChatFormatting;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiPanel;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiScreen;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiButton;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiImage;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiLabel;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiTextField;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.IGuiButton;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.advanced.GuiResourceLoadingList;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Typeable;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.CustomLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.GridLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.HorizontalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.Layout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.VerticalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.popup.GuiYesNoPopup;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Colors;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Consumer;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import com.replaymod.replaystudio.Studio;
import com.replaymod.replaystudio.replay.ReplayMetaData;
import com.replaymod.replaystudio.replay.ZipReplayFile;
import com.replaymod.replaystudio.studio.ReplayStudio;
import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import me.kaimson.melonclient.Client;
import me.kaimson.melonclient.ingames.utils.ReplayMod.core.ReplayCore;
import me.kaimson.melonclient.ingames.utils.ReplayMod.core.SettingsRegistry;
import me.kaimson.melonclient.ingames.utils.ReplayMod.core.Utils;
import me.kaimson.melonclient.ingames.utils.ReplayMod.customgui.AbstractGuiContainer;
import me.kaimson.melonclient.ingames.utils.ReplayMod.customgui.AbstractGuiElement;
import me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.ReplayModReplay;
import me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.Setting;
import net.minecraft.client.gui.GuiErrorScreen;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.Util;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.helpers.Strings;
import org.lwjgl.Sys;

@Deprecated
public class GuiReplayViewer extends GuiScreen implements Typeable {
  public final GuiResourceLoadingList<GuiReplayEntry> list = (GuiResourceLoadingList<GuiReplayEntry>)((GuiResourceLoadingList)((GuiResourceLoadingList)((GuiResourceLoadingList)((GuiResourceLoadingList)(new GuiResourceLoadingList((GuiContainer)this)).onSelectionChanged(new Runnable() {
        public void run() {
          ((IGuiButton)GuiReplayViewer.this.replayButtonPanel.forEach(IGuiButton.class)).setEnabled((GuiReplayViewer.this.list.getSelected() != null));
          if (GuiReplayViewer.this.list.getSelected() != null && ((GuiReplayViewer.GuiReplayEntry)GuiReplayViewer.this.list.getSelected()).incompatible)
            GuiReplayViewer.this.loadButton.setDisabled(); 
        }
      })).onLoad(new Consumer<Consumer<Supplier<GuiReplayEntry>>>() {
        public void consume(Consumer<Supplier<GuiReplayViewer.GuiReplayEntry>> obj) {
          try {
            File folder = ReplayCore.getInstance().getReplayFolder();
            for (File file : folder.listFiles((FileFilter)new SuffixFileFilter(".mcpr", IOCase.INSENSITIVE))) {
              if (Thread.interrupted())
                break; 
              try (ZipReplayFile null = new ZipReplayFile((Studio)new ReplayStudio(), file)) {
                BufferedImage theThumb;
                Optional<BufferedImage> thumb = zipReplayFile.getThumb();
                if (thumb.isPresent()) {
                  BufferedImage buf = (BufferedImage)thumb.get();
                  final int[] theIntArray = buf.getRGB(0, 0, buf.getWidth(), buf.getHeight(), null, 0, buf.getWidth());
                  theThumb = new BufferedImage(buf.getWidth(), buf.getHeight(), 2) {
                      public int[] getRGB(int startX, int startY, int w, int h, int[] rgbArray, int offset, int scansize) {
                        System.arraycopy(theIntArray, 0, rgbArray, 0, theIntArray.length);
                        return null;
                      }
                    };
                } else {
                  theThumb = null;
                } 
                ReplayMetaData metaData = zipReplayFile.getMetaData();
                if (metaData != null)
                  obj.consume(() -> new GuiReplayViewer.GuiReplayEntry(file, metaData, theThumb)); 
              } catch (Exception e) {
                Client.error("Could not load Replay File {}", new Object[] { file.getName(), e });
              } 
            } 
          } catch (IOException e) {
            e.printStackTrace();
          } 
        }
      })).onSelectionDoubleClicked(() -> {
        if (this.loadButton.isEnabled()) {
          this.loadButton.onClick();
          this.loadButton.setDisabled();
        } 
      })).setDrawShadow(true)).setDrawSlider(true);
  
  public final GuiButton loadButton = (GuiButton)((GuiButton)((GuiButton)((GuiButton)(new GuiButton()).onClick(new Runnable() {
        public void run() {
          try {
            ReplayModReplay.getInstance().startReplay(((GuiReplayViewer.GuiReplayEntry)GuiReplayViewer.this.list.getSelected()).file);
          } catch (IOException e) {
            e.printStackTrace();
          } 
        }
      })).setSize(73, 20)).setI18nLabel("replaymod.gui.load", new Object[0])).setDisabled();
  
  public final GuiButton folderButton = (GuiButton)((GuiButton)((GuiButton)(new GuiButton()).onClick(new Runnable() {
        public void run() {
          try {
            File folder = ReplayCore.getInstance().getReplayFolder();
            String path = folder.getAbsolutePath();
            try {
              switch (Util.getOSType()) {
                case WINDOWS:
                  Runtime.getRuntime().exec(String.format("cmd.exe /C start \"Open file\" \"%s\"", new Object[] { path }));
                  return;
                case OSX:
                  Runtime.getRuntime().exec(new String[] { "/usr/bin/open", path });
                  return;
              } 
            } catch (IOException e) {
              LogManager.getLogger().error("Cannot open file", e);
            } 
            try {
              Desktop.getDesktop().browse(folder.toURI());
            } catch (Throwable throwable) {
              Sys.openURL("file://" + path);
            } 
          } catch (IOException e) {
            Client.error("Cannot open file", new Object[] { e });
          } 
        }
      })).setSize(150, 20)).setI18nLabel("replaymod.gui.viewer.replayfolder", new Object[0]);
  
  public final GuiButton renameButton = (GuiButton)((GuiButton)((GuiButton)((GuiButton)(new GuiButton()).onClick(new Runnable() {
        public void run() {
          final File file = ((GuiReplayViewer.GuiReplayEntry)GuiReplayViewer.this.list.getSelected()).file;
          String name = Utils.fileNameToReplayName(file.getName());
          final GuiTextField nameField = (GuiTextField)((GuiTextField)((GuiTextField)(new GuiTextField()).setSize(200, 20)).setFocused(true)).setText(name);
          final GuiYesNoPopup popup = GuiYesNoPopup.open((GuiContainer)GuiReplayViewer.this, new GuiElement[] { (GuiElement)((GuiLabel)(new GuiLabel()).setI18nText("replaymod.gui.viewer.rename.name", new Object[0])).setColor(Colors.BLACK), (GuiElement)nameField }).setYesI18nLabel("replaymod.gui.rename", new Object[0]).setNoI18nLabel("replaymod.gui.cancel", new Object[0]);
          ((VerticalLayout)popup.getInfo().getLayout()).setSpacing(7);
          ((GuiTextField)nameField.onEnter(new Runnable() {
                public void run() {
                  if (popup.getYesButton().isEnabled())
                    popup.getYesButton().onClick(); 
                }
              })).onTextChanged(obj -> popup.getYesButton().setEnabled(
                (!nameField.getText().isEmpty() && !(new File(file.getParentFile(), Utils.replayNameToFileName(nameField.getText()))).exists())));
          Futures.addCallback(popup.getFuture(), new FutureCallback<Boolean>() {
                public void onSuccess(Boolean delete) {
                  if (delete.booleanValue()) {
                    String name = nameField.getText().trim();
                    File targetFile = new File(file.getParentFile(), Utils.replayNameToFileName(name));
                    try {
                      FileUtils.moveFile(file, targetFile);
                    } catch (IOException e) {
                      e.printStackTrace();
                      GuiReplayViewer.this.getMinecraft().displayGuiScreen((GuiScreen)new GuiErrorScreen(
                            I18n.format("replaymod.gui.viewer.delete.failed1", new Object[0]), 
                            I18n.format("replaymod.gui.viewer.delete.failed2", new Object[0])));
                      return;
                    } 
                    GuiReplayViewer.this.list.load();
                  } 
                }
                
                public void onFailure(Throwable t) {
                  t.printStackTrace();
                }
              });
        }
      })).setSize(73, 20)).setI18nLabel("replaymod.gui.rename", new Object[0])).setDisabled();
  
  public final GuiButton deleteButton = (GuiButton)((GuiButton)((GuiButton)((GuiButton)(new GuiButton()).onClick(new Runnable() {
        public void run() {
          String name = ((GuiReplayViewer.GuiReplayEntry)GuiReplayViewer.this.list.getSelected()).name.getText();
          GuiYesNoPopup popup = GuiYesNoPopup.open((GuiContainer)GuiReplayViewer.this, new GuiElement[] { (GuiElement)((GuiLabel)(new GuiLabel()).setI18nText("replaymod.gui.viewer.delete.linea", new Object[0])).setColor(Colors.BLACK), (GuiElement)((GuiLabel)(new GuiLabel()).setI18nText("replaymod.gui.viewer.delete.lineb", new Object[] { name + ChatFormatting.RESET })).setColor(Colors.BLACK) }).setYesI18nLabel("replaymod.gui.delete", new Object[0]).setNoI18nLabel("replaymod.gui.cancel", new Object[0]);
          Futures.addCallback(popup.getFuture(), new FutureCallback<Boolean>() {
                public void onSuccess(Boolean delete) {
                  if (delete.booleanValue()) {
                    try {
                      FileUtils.forceDelete(((GuiReplayViewer.GuiReplayEntry)GuiReplayViewer.this.list.getSelected()).file);
                    } catch (IOException e) {
                      e.printStackTrace();
                    } 
                    GuiReplayViewer.this.list.load();
                  } 
                }
                
                public void onFailure(Throwable t) {
                  t.printStackTrace();
                }
              });
        }
      })).setSize(73, 20)).setI18nLabel("replaymod.gui.delete", new Object[0])).setDisabled();
  
  public final GuiButton settingsButton = (GuiButton)((GuiButton)((GuiButton)(new GuiButton()).onClick(new Runnable() {
        public void run() {}
      })).setSize(73, 20)).setI18nLabel("replaymod.gui.settings", new Object[0]);
  
  public final GuiButton cancelButton = (GuiButton)((GuiButton)((GuiButton)(new GuiButton()).onClick(new Runnable() {
        public void run() {
          GuiReplayViewer.this.getMinecraft().displayGuiScreen(null);
        }
      })).setSize(73, 20)).setI18nLabel("replaymod.gui.cancel", new Object[0]);
  
  public final GuiPanel replayButtonPanel = (GuiPanel)((GuiPanel)(new GuiPanel()).setLayout((Layout)(new GridLayout()).setSpacingX(5).setSpacingY(5)
      .setColumns(2))).addElements(null, new GuiElement[] { (GuiElement)this.loadButton, (GuiElement)new GuiPanel(), (GuiElement)this.renameButton, (GuiElement)this.deleteButton });
  
  public final GuiPanel generalButtonPanel = (GuiPanel)((GuiPanel)(new GuiPanel()).setLayout((Layout)(new VerticalLayout()).setSpacing(5)))
    .addElements(null, new GuiElement[] { (GuiElement)this.folderButton, (GuiElement)((GuiPanel)(new GuiPanel()).setLayout((Layout)(new HorizontalLayout()).setSpacing(5)))
        .addElements(null, new GuiElement[] { (GuiElement)this.settingsButton, (GuiElement)this.cancelButton }) });
  
  public final GuiPanel buttonPanel = (GuiPanel)((GuiPanel)(new GuiPanel((GuiContainer)this)).setLayout((Layout)(new HorizontalLayout()).setSpacing(6)))
    .addElements(null, new GuiElement[] { (GuiElement)this.replayButtonPanel, (GuiElement)this.generalButtonPanel });
  
  private final GuiImage defaultThumbnail;
  
  public GuiReplayViewer() {
    this.defaultThumbnail = (GuiImage)(new GuiImage()).setTexture(Utils.DEFAULT_THUMBNAIL);
    setTitle((GuiLabel)(new GuiLabel()).setI18nText("replaymod.gui.replayviewer", new Object[0]));
    setLayout((Layout)new CustomLayout<GuiScreen>() {
          protected void layout(GuiScreen container, int width, int height) {
            pos((GuiElement)GuiReplayViewer.this.buttonPanel, width / 2 - width((GuiElement)GuiReplayViewer.this.buttonPanel) / 2, height - 10 - height((GuiElement)GuiReplayViewer.this.buttonPanel));
            pos((GuiElement)GuiReplayViewer.this.list, 0, 30);
            size((GuiElement)GuiReplayViewer.this.list, width, y((GuiElement)GuiReplayViewer.this.buttonPanel) - 10 - y((GuiElement)GuiReplayViewer.this.list));
          }
        });
  }
  
  public boolean typeKey(ReadablePoint mousePosition, int keyCode, char keyChar, boolean ctrlDown, boolean shiftDown) {
    if (keyCode == 59) {
      SettingsRegistry reg = ReplayCore.getInstance().getSettingsRegistry();
      reg.set((SettingsRegistry.SettingKey)Setting.SHOW_SERVER_IPS, Boolean.valueOf(!((Boolean)reg.get((SettingsRegistry.SettingKey)Setting.SHOW_SERVER_IPS)).booleanValue()));
      reg.save();
      this.list.load();
    } 
    return false;
  }
  
  public class GuiReplayEntry extends AbstractGuiContainer<GuiReplayEntry> implements Comparable<GuiReplayEntry> {
    public final File file;
    
    public final GuiLabel name = new GuiLabel();
    
    public final GuiLabel server = (GuiLabel)(new GuiLabel()).setColor(Colors.LIGHT_GRAY);
    
    public final GuiLabel date = (GuiLabel)(new GuiLabel()).setColor(Colors.LIGHT_GRAY);
    
    public final GuiPanel infoPanel = (GuiPanel)((GuiPanel)(new GuiPanel((GuiContainer)this)).setLayout((Layout)(new VerticalLayout()).setSpacing(2)))
      .addElements(null, new GuiElement[] { (GuiElement)this.name, (GuiElement)this.server, (GuiElement)this.date });
    
    public final GuiLabel version = (GuiLabel)(new GuiLabel((GuiContainer)this)).setColor(Colors.RED);
    
    public final GuiImage thumbnail;
    
    public final GuiLabel duration = new GuiLabel();
    
    public final GuiPanel durationPanel = (GuiPanel)((GuiPanel)((GuiPanel)(new GuiPanel()).setBackgroundColor(Colors.HALF_TRANSPARENT))
      .addElements(null, new GuiElement[] { (GuiElement)this.duration })).setLayout((Layout)new CustomLayout<GuiPanel>() {
          protected void layout(GuiPanel container, int width, int height) {
            pos((GuiElement)GuiReplayViewer.GuiReplayEntry.this.duration, 2, 2);
          }
          
          public ReadableDimension calcMinSize(GuiContainer<?> container) {
            ReadableDimension dimension = GuiReplayViewer.GuiReplayEntry.this.duration.calcMinSize();
            return (ReadableDimension)new Dimension(dimension.getWidth() + 2, dimension.getHeight() + 2);
          }
        });
    
    private final long dateMillis;
    
    private final boolean incompatible;
    
    public GuiReplayEntry(File file, ReplayMetaData metaData, BufferedImage thumbImage) {
      this.file = file;
      this.name.setText(ChatFormatting.UNDERLINE + Utils.fileNameToReplayName(file.getName()));
      if (Strings.isEmpty(metaData.getServerName()) || 
        !((Boolean)ReplayCore.getInstance().getSettingsRegistry().get((SettingsRegistry.SettingKey)Setting.SHOW_SERVER_IPS)).booleanValue()) {
        ((GuiLabel)this.server.setI18nText("replaymod.gui.iphidden", new Object[0])).setColor(Colors.DARK_RED);
      } else {
        this.server.setText(metaData.getServerName());
      } 
      this.incompatible = false;
      if (this.incompatible)
        this.version.setText("Minecraft " + metaData.getMcVersion()); 
      this.dateMillis = metaData.getDate();
      this.date.setText((new SimpleDateFormat()).format(new Date(this.dateMillis)));
      if (thumbImage == null) {
        this.thumbnail = (GuiImage)(new GuiImage(GuiReplayViewer.this.defaultThumbnail)).setSize(53, 30);
        addElements(null, new GuiElement[] { (GuiElement)this.thumbnail });
      } else {
        this.thumbnail = null;
      } 
      this.duration.setText(Utils.convertSecondsToShortString(metaData.getDuration() / 1000));
      addElements(null, new GuiElement[] { (GuiElement)this.durationPanel });
      setLayout((Layout)new CustomLayout<GuiReplayEntry>() {
            protected void layout(GuiReplayViewer.GuiReplayEntry container, int width, int height) {
              pos((GuiElement)GuiReplayViewer.GuiReplayEntry.this.thumbnail, 0, 0);
              x((GuiElement)GuiReplayViewer.GuiReplayEntry.this.durationPanel, width((GuiElement)GuiReplayViewer.GuiReplayEntry.this.thumbnail) - width((GuiElement)GuiReplayViewer.GuiReplayEntry.this.durationPanel));
              y((GuiElement)GuiReplayViewer.GuiReplayEntry.this.durationPanel, height((GuiElement)GuiReplayViewer.GuiReplayEntry.this.thumbnail) - height((GuiElement)GuiReplayViewer.GuiReplayEntry.this.durationPanel));
              pos((GuiElement)GuiReplayViewer.GuiReplayEntry.this.infoPanel, width((GuiElement)GuiReplayViewer.GuiReplayEntry.this.thumbnail) + 5, 0);
              pos((GuiElement)GuiReplayViewer.GuiReplayEntry.this.version, width - width((GuiElement)GuiReplayViewer.GuiReplayEntry.this.version), 0);
            }
            
            public ReadableDimension calcMinSize(GuiContainer<?> container) {
              return (ReadableDimension)new Dimension(300, GuiReplayViewer.GuiReplayEntry.this.thumbnail.getMinSize().getHeight());
            }
          });
    }
    
    protected GuiReplayEntry getThis() {
      return this;
    }
    
    public int compareTo(GuiReplayEntry o) {
      return Long.compare(o.dateMillis, this.dateMillis);
    }
  }
}
