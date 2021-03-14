package me.kaimson.melonclient.ingames.utils.ReplayMod.simplepathing.gui;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.SettableFuture;
import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.AbstractGuiClickableContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.AbstractGuiScreen;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiScreen;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiVerticalList;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.AbstractGuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiLabel;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiTextField;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.IGuiButton;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Closeable;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.CustomLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.HorizontalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.Layout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.VerticalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.popup.GuiYesNoPopup;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Colors;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Consumer;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.replaystudio.pathing.PathingRegistry;
import com.replaymod.replaystudio.pathing.path.Path;
import com.replaymod.replaystudio.pathing.path.Timeline;
import com.replaymod.replaystudio.replay.ReplayFile;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import me.kaimson.melonclient.Client;
import me.kaimson.melonclient.ingames.utils.ReplayMod.core.Utils;
import me.kaimson.melonclient.ingames.utils.ReplayMod.customgui.GuiButton;
import me.kaimson.melonclient.ingames.utils.ReplayMod.customgui.GuiPanel;
import me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.ReplayModReplay;
import net.minecraft.client.Minecraft;

public class GuiKeyframeRepository extends GuiScreen implements Closeable {
  public final GuiPanel contentPanel = (GuiPanel)(new GuiPanel((GuiContainer)this)).setBackgroundColor(Colors.DARK_TRANSPARENT);
  
  public final GuiLabel title = (GuiLabel)(new GuiLabel((GuiContainer)this.contentPanel)).setI18nText("replaymod.gui.keyframerepository.title", new Object[0]);
  
  public final GuiVerticalList list = (GuiVerticalList)((GuiVerticalList)(new GuiVerticalList((GuiContainer)this.contentPanel)).setDrawShadow(true)).setDrawSlider(true);
  
  public final GuiPanel buttonPanel = (GuiPanel)(new GuiPanel((GuiContainer)this.contentPanel)).setLayout((Layout)(new HorizontalLayout()).setSpacing(5));
  
  public final GuiButton overwriteButton = (GuiButton)((GuiButton)((GuiButton)((GuiButton)(new GuiButton((GuiContainer)this.buttonPanel)).onClick(new Runnable() {
        public void run() {
          GuiYesNoPopup popup = GuiYesNoPopup.open((GuiContainer)GuiKeyframeRepository.this, new GuiElement[] { (GuiElement)((GuiLabel)(new GuiLabel()).setI18nText("replaymod.gui.keyframerepo.overwrite", new Object[0])).setColor(Colors.BLACK) }).setYesI18nLabel("gui.yes", new Object[0]).setNoI18nLabel("gui.no", new Object[0]);
          Utils.addCallback(popup.getFuture(), doIt -> {
                if (doIt.booleanValue()) {
                  GuiKeyframeRepository.this.timelines.put(GuiKeyframeRepository.this.selectedEntry.name, GuiKeyframeRepository.this.currentTimeline);
                  GuiKeyframeRepository.this.overwriteButton.setDisabled();
                  GuiKeyframeRepository.this.save();
                } 
              }Throwable::printStackTrace);
        }
      })).setSize(75, 20)).setI18nLabel("replaymod.gui.overwrite", new Object[0])).setDisabled();
  
  public final GuiButton saveAsButton = (GuiButton)((GuiButton)((GuiButton)(new GuiButton((GuiContainer)this.buttonPanel)).onClick(new Runnable() {
        public void run() {
          final GuiTextField nameField = (GuiTextField)((GuiTextField)(new GuiTextField()).setSize(200, 20)).setFocused(true);
          final GuiYesNoPopup popup = GuiYesNoPopup.open((GuiContainer)GuiKeyframeRepository.this, new GuiElement[] { (GuiElement)((GuiLabel)(new GuiLabel()).setI18nText("replaymod.gui.saveas", new Object[0])).setColor(Colors.BLACK), (GuiElement)nameField }).setYesI18nLabel("replaymod.gui.save", new Object[0]).setNoI18nLabel("replaymod.gui.cancel", new Object[0]);
          popup.getYesButton().setDisabled();
          ((VerticalLayout)popup.getInfo().getLayout()).setSpacing(7);
          ((GuiTextField)nameField.onEnter(new Runnable() {
                public void run() {
                  if (popup.getYesButton().isEnabled())
                    popup.getYesButton().onClick(); 
                }
              })).onTextChanged(new Consumer<String>() {
                public void consume(String obj) {
                  popup.getYesButton().setEnabled((!nameField.getText().isEmpty() && 
                      !GuiKeyframeRepository.this.timelines.containsKey(nameField.getText())));
                }
              });
          Futures.addCallback(popup.getFuture(), new FutureCallback<Boolean>() {
                public void onSuccess(Boolean save) {
                  if (save.booleanValue()) {
                    String name = nameField.getText();
                    GuiKeyframeRepository.this.timelines.put(name, GuiKeyframeRepository.this.currentTimeline);
                    GuiKeyframeRepository.this.list.getListPanel().addElements(null, new GuiElement[] { (GuiElement)new GuiKeyframeRepository.Entry(name) });
                    GuiKeyframeRepository.this.save();
                  } 
                }
                
                public void onFailure(Throwable t) {
                  t.printStackTrace();
                }
              });
        }
      })).setSize(75, 20)).setI18nLabel("replaymod.gui.saveas", new Object[0]);
  
  public final GuiButton loadButton = (GuiButton)((GuiButton)((GuiButton)((GuiButton)(new GuiButton((GuiContainer)this.buttonPanel)).onClick(new Runnable() {
        public void run() {
          Minecraft.getMinecraft().displayGuiScreen(null);
          try {
            Timeline timeline = (Timeline)GuiKeyframeRepository.this.timelines.get(GuiKeyframeRepository.this.selectedEntry.name);
            for (Path path : timeline.getPaths())
              path.updateAll(); 
            GuiKeyframeRepository.this.future.set(timeline);
          } catch (Throwable t) {
            GuiKeyframeRepository.this.future.setException(t);
          } 
        }
      })).setSize(75, 20)).setI18nLabel("replaymod.gui.load", new Object[0])).setDisabled();
  
  public final GuiButton renameButton = (GuiButton)((GuiButton)((GuiButton)((GuiButton)(new GuiButton((GuiContainer)this.buttonPanel)).onClick(new Runnable() {
        public void run() {
          final GuiTextField nameField = (GuiTextField)((GuiTextField)((GuiTextField)(new GuiTextField()).setSize(200, 20)).setFocused(true)).setText(GuiKeyframeRepository.this.selectedEntry.name);
          final GuiYesNoPopup popup = GuiYesNoPopup.open((GuiContainer)GuiKeyframeRepository.this, new GuiElement[] { (GuiElement)((GuiLabel)(new GuiLabel()).setI18nText("replaymod.gui.rename", new Object[0])).setColor(Colors.BLACK), (GuiElement)nameField }).setYesI18nLabel("replaymod.gui.done", new Object[0]).setNoI18nLabel("replaymod.gui.cancel", new Object[0]);
          popup.getYesButton().setDisabled();
          ((VerticalLayout)popup.getInfo().getLayout()).setSpacing(7);
          ((GuiTextField)nameField.onEnter(new Runnable() {
                public void run() {
                  if (popup.getYesButton().isEnabled())
                    popup.getYesButton().onClick(); 
                }
              })).onTextChanged(new Consumer<String>() {
                public void consume(String obj) {
                  popup.getYesButton().setEnabled((!nameField.getText().isEmpty() && 
                      !GuiKeyframeRepository.this.timelines.containsKey(nameField.getText())));
                }
              });
          Futures.addCallback(popup.getFuture(), new FutureCallback<Boolean>() {
                public void onSuccess(Boolean save) {
                  if (save.booleanValue()) {
                    String name = nameField.getText();
                    GuiKeyframeRepository.this.timelines.put(name, GuiKeyframeRepository.this.timelines.remove(GuiKeyframeRepository.this.selectedEntry.name));
                    GuiKeyframeRepository.this.selectedEntry.name = name;
                    GuiKeyframeRepository.this.selectedEntry.label.setText(name);
                    GuiKeyframeRepository.this.save();
                  } 
                }
                
                public void onFailure(Throwable t) {
                  t.printStackTrace();
                }
              });
        }
      })).setSize(75, 20)).setI18nLabel("replaymod.gui.rename", new Object[0])).setDisabled();
  
  public final GuiButton removeButton = (GuiButton)((GuiButton)((GuiButton)((GuiButton)(new GuiButton((GuiContainer)this.buttonPanel)).onClick(new Runnable() {
        public void run() {
          GuiYesNoPopup popup = GuiYesNoPopup.open((GuiContainer)GuiKeyframeRepository.this, new GuiElement[] { (GuiElement)((GuiLabel)(new GuiLabel()).setI18nText("replaymod.gui.keyframerepo.delete", new Object[0])).setColor(Colors.BLACK) }).setYesI18nLabel("replaymod.gui.delete", new Object[0]).setNoI18nLabel("replaymod.gui.cancel", new Object[0]);
          Futures.addCallback(popup.getFuture(), new FutureCallback<Boolean>() {
                public void onSuccess(Boolean delete) {
                  if (delete.booleanValue()) {
                    GuiKeyframeRepository.this.timelines.remove(GuiKeyframeRepository.this.selectedEntry.name);
                    GuiKeyframeRepository.this.list.getListPanel().removeElement((GuiElement)GuiKeyframeRepository.this.selectedEntry);
                    GuiKeyframeRepository.this.selectedEntry = null;
                    GuiKeyframeRepository.this.overwriteButton.setDisabled();
                    GuiKeyframeRepository.this.loadButton.setDisabled();
                    GuiKeyframeRepository.this.renameButton.setDisabled();
                    GuiKeyframeRepository.this.removeButton.setDisabled();
                    GuiKeyframeRepository.this.save();
                  } 
                }
                
                public void onFailure(Throwable t) {
                  t.printStackTrace();
                }
              });
        }
      })).setSize(75, 20)).setI18nLabel("replaymod.gui.remove", new Object[0])).setDisabled();
  
  private final Map<String, Timeline> timelines = new LinkedHashMap<>();
  
  private final Timeline currentTimeline;
  
  private final SettableFuture<Timeline> future = SettableFuture.create();
  
  private final PathingRegistry registry;
  
  private final ReplayFile replayFile;
  
  private Entry selectedEntry;
  
  public GuiKeyframeRepository(PathingRegistry registry, ReplayFile replayFile, Timeline currentTimeline) throws IOException {
    setBackground(AbstractGuiScreen.Background.NONE);
    setLayout((Layout)new CustomLayout<GuiScreen>() {
          protected void layout(GuiScreen container, int width, int height) {
            pos((GuiElement)GuiKeyframeRepository.this.contentPanel, width / 2 - width((GuiElement)GuiKeyframeRepository.this.contentPanel) / 2, height / 2 - height((GuiElement)GuiKeyframeRepository.this.contentPanel) / 2);
          }
        });
    this.contentPanel.setLayout((Layout)new CustomLayout<GuiPanel>() {
          protected void layout(GuiPanel container, int width, int height) {
            pos((GuiElement)GuiKeyframeRepository.this.title, width / 2 - width((GuiElement)GuiKeyframeRepository.this.title) / 2, 5);
            size((GuiElement)GuiKeyframeRepository.this.list, width, height - 10 - height((GuiElement)GuiKeyframeRepository.this.buttonPanel) - 10 - y((GuiElement)GuiKeyframeRepository.this.title) - height((GuiElement)GuiKeyframeRepository.this.title) - 5);
            pos((GuiElement)GuiKeyframeRepository.this.list, width / 2 - width((GuiElement)GuiKeyframeRepository.this.list) / 2, y((GuiElement)GuiKeyframeRepository.this.title) + height((GuiElement)GuiKeyframeRepository.this.title) + 5);
            pos((GuiElement)GuiKeyframeRepository.this.buttonPanel, width / 2 - width((GuiElement)GuiKeyframeRepository.this.buttonPanel) / 2, y((GuiElement)GuiKeyframeRepository.this.list) + height((GuiElement)GuiKeyframeRepository.this.list) + 10);
          }
          
          public ReadableDimension calcMinSize(GuiContainer<?> container) {
            ReadableDimension screenSize = GuiKeyframeRepository.this.getMinSize();
            return (ReadableDimension)new Dimension(screenSize.getWidth() - 10, screenSize.getHeight() - 10);
          }
        });
    this.registry = registry;
    this.replayFile = replayFile;
    this.currentTimeline = currentTimeline;
    this.timelines.putAll(replayFile.getTimelines(registry));
    for (Map.Entry<String, Timeline> entry : this.timelines.entrySet()) {
      if (((String)entry.getKey()).isEmpty())
        continue; 
      this.list.getListPanel().addElements(null, new GuiElement[] { (GuiElement)new Entry(entry.getKey()) });
    } 
  }
  
  public void display() {
    super.display();
    ReplayModReplay.getInstance().getReplayHandler().getOverlay().setVisible(false);
  }
  
  public void close() {
    ReplayModReplay.getInstance().getReplayHandler().getOverlay().setVisible(true);
  }
  
  public SettableFuture<Timeline> getFuture() {
    return this.future;
  }
  
  public void save() {
    try {
      this.replayFile.writeTimelines(this.registry, this.timelines);
    } catch (IOException e) {
      e.printStackTrace();
      Client.sendChatMessage("Error saving timelines: " + e.getMessage());
    } 
  }
  
  public class Entry extends AbstractGuiClickableContainer<Entry> {
    public final GuiLabel label = new GuiLabel((GuiContainer)this);
    
    private String name;
    
    public Entry(String name) {
      this.name = name;
      setLayout((Layout)new CustomLayout<Entry>() {
            protected void layout(GuiKeyframeRepository.Entry container, int width, int height) {
              pos((GuiElement)GuiKeyframeRepository.Entry.this.label, 5, height / 2 - height((GuiElement)GuiKeyframeRepository.Entry.this.label) / 2);
            }
            
            public ReadableDimension calcMinSize(GuiContainer<?> container) {
              return (ReadableDimension)new Dimension(GuiKeyframeRepository.this.buttonPanel.calcMinSize().getWidth(), 16);
            }
          });
      this.label.setText(name);
    }
    
    protected void onClick() {
      GuiKeyframeRepository.this.selectedEntry = this;
      ((IGuiButton)GuiKeyframeRepository.this.buttonPanel.forEach(IGuiButton.class)).setEnabled();
    }
    
    public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
      if (GuiKeyframeRepository.this.selectedEntry == this) {
        renderer.drawRect(0, 0, size.getWidth(), size.getHeight(), Colors.BLACK);
        renderer.drawRect(0, 0, 2, size.getHeight(), Colors.WHITE);
      } 
      super.draw(renderer, size, renderInfo);
    }
    
    protected Entry getThis() {
      return this;
    }
  }
}
