package com.replaymod.lib.de.johni0702.minecraft.gui.popup;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiPanel;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiScrollable;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiVerticalList;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.AbstractGuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiButton;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiTextField;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.advanced.GuiDropdownMenu;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Typeable;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.CustomLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.HorizontalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.Layout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.LayoutData;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.VerticalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Colors;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Consumer;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.MCVer;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class GuiFileChooserPopup extends AbstractGuiPopup<GuiFileChooserPopup> implements Typeable {
  public static GuiFileChooserPopup openSaveGui(GuiContainer container, String buttonLabel, String... fileExtensions) {
    GuiFileChooserPopup popup = (GuiFileChooserPopup)(new GuiFileChooserPopup(container, fileExtensions, false)).setBackgroundColor(Colors.DARK_TRANSPARENT);
    popup.acceptButton.setI18nLabel(buttonLabel, new Object[0]);
    popup.open();
    return popup;
  }
  
  public static GuiFileChooserPopup openLoadGui(GuiContainer container, String buttonLabel, String... fileExtensions) {
    GuiFileChooserPopup popup = (GuiFileChooserPopup)(new GuiFileChooserPopup(container, fileExtensions, true)).setBackgroundColor(Colors.DARK_TRANSPARENT);
    ((GuiButton)popup.acceptButton.setI18nLabel(buttonLabel, new Object[0])).setDisabled();
    popup.open();
    return popup;
  }
  
  private final SettableFuture<File> future = SettableFuture.create();
  
  private final GuiScrollable pathScrollable = new GuiScrollable((GuiContainer)this.popup) {
      public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
        scrollX(0);
        super.draw(renderer, size, renderInfo);
      }
    };
  
  private final GuiPanel pathPanel = (GuiPanel)(new GuiPanel((GuiContainer)this.pathScrollable)).setLayout((Layout)new HorizontalLayout());
  
  private final GuiVerticalList fileList = new GuiVerticalList((GuiContainer)this.popup);
  
  private final GuiTextField nameField = (GuiTextField)((GuiTextField)(new GuiTextField((GuiContainer)this.popup)).onEnter(new Runnable() {
        public void run() {
          if (GuiFileChooserPopup.this.acceptButton.isEnabled())
            GuiFileChooserPopup.this.acceptButton.onClick(); 
        }
      })).onTextChanged(new Consumer<String>() {
        public void consume(String oldName) {
          GuiFileChooserPopup.this.updateButton();
        }
      });
  
  private final GuiButton acceptButton = (GuiButton)((GuiButton)(new GuiButton((GuiContainer)this.popup))
    .onClick(new Runnable() {
        public void run() {
          String fileName = GuiFileChooserPopup.this.nameField.getText();
          if (!GuiFileChooserPopup.this.load && GuiFileChooserPopup.this.fileExtensions.length > 0 && 
            !GuiFileChooserPopup.this.hasValidExtension(fileName))
            fileName = fileName + "." + GuiFileChooserPopup.this.fileExtensions[0]; 
          GuiFileChooserPopup.this.future.set(new File(GuiFileChooserPopup.this.folder, fileName));
          GuiFileChooserPopup.this.close();
        }
      })).setSize(50, 20);
  
  public GuiButton getAcceptButton() {
    return this.acceptButton;
  }
  
  private final GuiButton cancelButton = (GuiButton)((GuiButton)((GuiButton)(new GuiButton((GuiContainer)this.popup))
    .onClick(new Runnable() {
        public void run() {
          GuiFileChooserPopup.this.future.set(null);
          GuiFileChooserPopup.this.close();
        }
      })).setI18nLabel("gui.cancel", new Object[0])).setSize(50, 20);
  
  private final String[] fileExtensions;
  
  private final boolean load;
  
  private File folder;
  
  public GuiButton getCancelButton() {
    return this.cancelButton;
  }
  
  public GuiFileChooserPopup(GuiContainer container, String[] fileExtensions, boolean load) {
    super(container);
    this.fileList.setLayout((Layout)(new VerticalLayout()).setSpacing(1));
    this.popup.setLayout((Layout)new CustomLayout<GuiPanel>() {
          protected void layout(GuiPanel container, int width, int height) {
            pos((GuiElement)GuiFileChooserPopup.this.pathScrollable, 0, 0);
            size((GuiElement)GuiFileChooserPopup.this.pathScrollable, width, 20);
            pos((GuiElement)GuiFileChooserPopup.this.cancelButton, width - width((GuiElement)GuiFileChooserPopup.this.cancelButton), height - height((GuiElement)GuiFileChooserPopup.this.cancelButton));
            pos((GuiElement)GuiFileChooserPopup.this.acceptButton, x((GuiElement)GuiFileChooserPopup.this.cancelButton) - 5 - width((GuiElement)GuiFileChooserPopup.this.acceptButton), y((GuiElement)GuiFileChooserPopup.this.cancelButton));
            size((GuiElement)GuiFileChooserPopup.this.nameField, x((GuiElement)GuiFileChooserPopup.this.acceptButton) - 5, 20);
            pos((GuiElement)GuiFileChooserPopup.this.nameField, 0, height - height((GuiElement)GuiFileChooserPopup.this.nameField));
            pos((GuiElement)GuiFileChooserPopup.this.fileList, 0, y((GuiElement)GuiFileChooserPopup.this.pathScrollable) + height((GuiElement)GuiFileChooserPopup.this.pathScrollable) + 5);
            size((GuiElement)GuiFileChooserPopup.this.fileList, width, y((GuiElement)GuiFileChooserPopup.this.nameField) - y((GuiElement)GuiFileChooserPopup.this.fileList) - 5);
          }
          
          public ReadableDimension calcMinSize(GuiContainer container) {
            return (ReadableDimension)new Dimension(300, 200);
          }
        });
    this.fileExtensions = fileExtensions;
    this.load = load;
    setFolder(new File("."));
  }
  
  protected void updateButton() {
    if (this.load)
      this.acceptButton.setEnabled((new File(this.folder, this.nameField.getText())).exists()); 
  }
  
  public void setFolder(File folder) {
    if (!folder.isDirectory())
      throw new IllegalArgumentException("Folder has to be a directory."); 
    try {
      this.folder = folder = folder.getCanonicalFile();
    } catch (IOException e) {
      this.future.setException(e);
      close();
      return;
    } 
    updateButton();
    for (GuiElement element : new ArrayList(this.pathPanel.getElements().keySet()))
      this.pathPanel.removeElement(element); 
    for (GuiElement element : new ArrayList(this.fileList.getListPanel().getElements().keySet()))
      this.fileList.getListPanel().removeElement(element); 
    File[] files = folder.listFiles();
    if (files != null) {
      Arrays.sort(files, new Comparator<File>() {
            public int compare(File f1, File f2) {
              if (f1.isDirectory() && !f2.isDirectory())
                return -1; 
              if (!f1.isDirectory() && f2.isDirectory())
                return 1; 
              return f1.getName().compareToIgnoreCase(f2.getName());
            }
          });
      for (File file : files) {
        if (file.isDirectory()) {
          this.fileList.getListPanel().addElements((LayoutData)new VerticalLayout.Data(0.0D), new GuiElement[] { (GuiElement)((GuiButton)(new GuiButton()).onClick(new Runnable() {
                    public void run() {
                      GuiFileChooserPopup.this.setFolder(file);
                    }
                  })).setLabel(file.getName() + File.separator) });
        } else if (hasValidExtension(file.getName())) {
          this.fileList.getListPanel().addElements((LayoutData)new VerticalLayout.Data(0.0D), new GuiElement[] { (GuiElement)((GuiButton)(new GuiButton()).onClick(new Runnable() {
                    public void run() {
                      GuiFileChooserPopup.this.setFileName(file.getName());
                    }
                  })).setLabel(file.getName()) });
        } 
      } 
    } 
    this.fileList.setOffsetY(0);
    File[] roots = File.listRoots();
    if (roots != null && roots.length > 1) {
      final GuiDropdownMenu<File> dropdown = new GuiDropdownMenu<File>((GuiContainer)this.pathPanel) {
          private final GuiButton skin = new GuiButton();
          
          protected ReadableDimension calcMinSize() {
            ReadableDimension dim = super.calcMinSize();
            return (ReadableDimension)new Dimension(dim.getWidth() - 5 - (MCVer.getFontRenderer()).field_78288_b, dim
                .getHeight());
          }
          
          public void layout(ReadableDimension size, RenderInfo renderInfo) {
            super.layout(size, renderInfo);
            if (renderInfo.layer == 0)
              this.skin.layout(size, renderInfo); 
          }
          
          public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
            super.draw(renderer, size, renderInfo);
            if (renderInfo.layer == 0) {
              this.skin.setLabel(((File)getSelectedValue()).toString());
              this.skin.draw(renderer, size, renderInfo);
            } 
          }
        };
      List<File> actualRoots = new ArrayList<>();
      File selected = null;
      for (File root : roots) {
        if (root.isDirectory()) {
          actualRoots.add(root);
          if (folder.getAbsolutePath().startsWith(root.getAbsolutePath()))
            selected = root; 
        } 
      } 
      assert selected != null;
      ((GuiDropdownMenu)dropdown.setValues(actualRoots.toArray((Object[])new File[actualRoots.size()]))).setSelected(selected);
      dropdown.onSelection(new Consumer<Integer>() {
            public void consume(Integer old) {
              GuiFileChooserPopup.this.setFolder((File)dropdown.getSelectedValue());
            }
          });
    } 
    LinkedList<File> parents = new LinkedList<>();
    while (folder != null) {
      parents.addFirst(folder);
      folder = folder.getParentFile();
    } 
    for (File parent : parents) {
      this.pathPanel.addElements(null, new GuiElement[] { (GuiElement)((GuiButton)(new GuiButton()).onClick(new Runnable() {
                public void run() {
                  GuiFileChooserPopup.this.setFolder(parent);
                }
              })).setLabel(parent.getName() + File.separator) });
    } 
    this.pathScrollable.setOffsetX(2147483647);
  }
  
  public void setFileName(String fileName) {
    this.nameField.setText(fileName);
    this.nameField.setCursorPosition(fileName.length());
    updateButton();
  }
  
  private boolean hasValidExtension(String name) {
    for (String fileExtension : this.fileExtensions) {
      if (name.endsWith("." + fileExtension))
        return true; 
    } 
    return false;
  }
  
  protected GuiFileChooserPopup getThis() {
    return this;
  }
  
  public ListenableFuture<File> getFuture() {
    return (ListenableFuture<File>)this.future;
  }
  
  public boolean typeKey(ReadablePoint mousePosition, int keyCode, char keyChar, boolean ctrlDown, boolean shiftDown) {
    if (keyCode == 1) {
      this.cancelButton.onClick();
      return true;
    } 
    return false;
  }
}
