package me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.gui.overlay;

import com.mojang.realmsclient.gui.ChatFormatting;
import com.replaymod.replaystudio.replay.ReplayFile;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import me.kaimson.melonclient.gui.GuiFactory;
import me.kaimson.melonclient.gui.GuiUtils;
import me.kaimson.melonclient.gui.buttons.GuiButton;
import me.kaimson.melonclient.ingames.utils.ReplayMod.core.Utils;
import me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.ReplayModReplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;

public class GuiReplayList extends GuiScreen {
  private Slot slot;
  
  private final int DEFAULT_THUMBNAIL = Utils.DEFAULT_THUMBNAIL.toTexture().getGlTextureId();
  
  private GuiFactory factory;
  
  private GuiButton loadButton;
  
  public void initGui() {
    this.buttonList.clear();
    int dWidth = this.width / 2 - 185 + 36;
    this
      
      .factory = (new GuiFactory()).addRow((new GuiFactory.Row(dWidth, this.height - 55, 305, 20)).addElement(this.loadButton = (new GuiButton("Load")).setEnabled(false).onPress(button -> {
              this.loadButton.setEnabled(false);
              try {
                ReplayModReplay.getInstance().startReplay(((Entry)this.slot.list.get(this.slot.selectedSlotIndex)).getReplayFile());
              } catch (IOException e) {
                e.printStackTrace();
              } 
            })).addElement((new GuiButton("Open folder")).setEnabled(false).onPress(button -> {
            
            })).addElement((new GuiButton("Settings")).setEnabled(false).onPress(button -> {
            
            })).setLayout(new GuiFactory.Layout(GuiFactory.Layout.LayoutType.HORIZONTAL, 4))).addRow((new GuiFactory.Row(dWidth, this.height - 30, 305, 20)).addElement((new GuiButton("Rename")).setEnabled(false).onPress(button -> {
            
            })).addElement((new GuiButton("Delete")).setEnabled(false).onPress(button -> {
            
            })).addElement((new GuiButton("Edit")).setEnabled(false).onPress(button -> {
            
            })).addElement((new GuiButton("Cancel")).onPress(button -> Minecraft.getMinecraft().displayGuiScreen(null))).setLayout(new GuiFactory.Layout(GuiFactory.Layout.LayoutType.HORIZONTAL, 4))).build();
    ArrayList<Entry> replays = getReplays();
    replays.sort((o1, o2) -> {
          try {
            return (int)(o2.getReplayFile().getMetaData().getDate() - o1.getReplayFile().getMetaData().getDate());
          } catch (IOException e) {
            e.printStackTrace();
            return 0;
          } 
        });
    this
      
      .slot = (new Slot(replays, this)).onElementClicked(() -> this.loadButton.setEnabled(true)).onElementDoubleClicked(() -> {
          if (!this.loadButton.isEnabled())
            return; 
          this.loadButton.onPress().accept(this.loadButton);
          this.loadButton.setEnabled(false);
        });
    this.slot.registerScrollButtons(7, 8);
  }
  
  protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
    super.mouseClicked(mouseX, mouseY, mouseButton);
    this.factory.mouseClicked(mouseX, mouseY, mouseButton);
  }
  
  public void handleMouseInput() throws IOException {
    super.handleMouseInput();
    this.slot.handleMouseInput();
  }
  
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    this.slot.drawScreen(mouseX, mouseY, partialTicks);
    this.factory.drawScreen(mouseX, mouseY, partialTicks);
    super.drawScreen(mouseX, mouseY, partialTicks);
  }
  
  private ArrayList<Entry> getReplays() {
    // Byte code:
    //   0: invokestatic newArrayList : ()Ljava/util/ArrayList;
    //   3: astore_1
    //   4: invokestatic getInstance : ()Lme/kaimson/melonclient/ingames/utils/ReplayMod/core/ReplayCore;
    //   7: invokevirtual getReplayFolder : ()Ljava/io/File;
    //   10: new org/apache/commons/io/filefilter/SuffixFileFilter
    //   13: dup
    //   14: ldc '.mcpr'
    //   16: getstatic org/apache/commons/io/IOCase.INSENSITIVE : Lorg/apache/commons/io/IOCase;
    //   19: invokespecial <init> : (Ljava/lang/String;Lorg/apache/commons/io/IOCase;)V
    //   22: invokevirtual listFiles : (Ljava/io/FileFilter;)[Ljava/io/File;
    //   25: invokestatic requireNonNull : (Ljava/lang/Object;)Ljava/lang/Object;
    //   28: checkcast [Ljava/io/File;
    //   31: astore_2
    //   32: aload_2
    //   33: arraylength
    //   34: istore_3
    //   35: iconst_0
    //   36: istore #4
    //   38: iload #4
    //   40: iload_3
    //   41: if_icmpge -> 483
    //   44: aload_2
    //   45: iload #4
    //   47: aaload
    //   48: astore #5
    //   50: new com/replaymod/replaystudio/replay/ZipReplayFile
    //   53: dup
    //   54: new com/replaymod/replaystudio/studio/ReplayStudio
    //   57: dup
    //   58: invokespecial <init> : ()V
    //   61: aload #5
    //   63: invokespecial <init> : (Lcom/replaymod/replaystudio/Studio;Ljava/io/File;)V
    //   66: astore #6
    //   68: aconst_null
    //   69: astore #7
    //   71: aload #6
    //   73: invokeinterface getMetaData : ()Lcom/replaymod/replaystudio/replay/ReplayMetaData;
    //   78: ifnull -> 297
    //   81: aload #6
    //   83: invokeinterface getMetaData : ()Lcom/replaymod/replaystudio/replay/ReplayMetaData;
    //   88: invokevirtual getServerName : ()Ljava/lang/String;
    //   91: ifnonnull -> 180
    //   94: new java/lang/StringBuilder
    //   97: dup
    //   98: invokespecial <init> : ()V
    //   101: ldc 'Invalid replay '
    //   103: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   106: aload #5
    //   108: invokevirtual getName : ()Ljava/lang/String;
    //   111: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   114: ldc ' ('
    //   116: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   119: aload #5
    //   121: invokevirtual getAbsolutePath : ()Ljava/lang/String;
    //   124: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   127: ldc ')!'
    //   129: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   132: invokevirtual toString : ()Ljava/lang/String;
    //   135: invokestatic log : (Ljava/lang/Object;)V
    //   138: aload #6
    //   140: ifnull -> 177
    //   143: aload #7
    //   145: ifnull -> 170
    //   148: aload #6
    //   150: invokeinterface close : ()V
    //   155: goto -> 177
    //   158: astore #8
    //   160: aload #7
    //   162: aload #8
    //   164: invokevirtual addSuppressed : (Ljava/lang/Throwable;)V
    //   167: goto -> 177
    //   170: aload #6
    //   172: invokeinterface close : ()V
    //   177: goto -> 477
    //   180: aload #6
    //   182: invokeinterface getThumb : ()Lcom/google/common/base/Optional;
    //   187: astore #8
    //   189: aload #8
    //   191: invokevirtual isPresent : ()Z
    //   194: ifeq -> 259
    //   197: aload #8
    //   199: invokevirtual get : ()Ljava/lang/Object;
    //   202: checkcast java/awt/image/BufferedImage
    //   205: astore #10
    //   207: aload #10
    //   209: iconst_0
    //   210: iconst_0
    //   211: aload #10
    //   213: invokevirtual getWidth : ()I
    //   216: aload #10
    //   218: invokevirtual getHeight : ()I
    //   221: aconst_null
    //   222: iconst_0
    //   223: aload #10
    //   225: invokevirtual getWidth : ()I
    //   228: invokevirtual getRGB : (IIII[III)[I
    //   231: astore #11
    //   233: new me/kaimson/melonclient/ingames/utils/ReplayMod/viewer/gui/overlay/GuiReplayList$1
    //   236: dup
    //   237: aload_0
    //   238: aload #10
    //   240: invokevirtual getWidth : ()I
    //   243: aload #10
    //   245: invokevirtual getHeight : ()I
    //   248: iconst_2
    //   249: aload #11
    //   251: invokespecial <init> : (Lme/kaimson/melonclient/ingames/utils/ReplayMod/viewer/gui/overlay/GuiReplayList;III[I)V
    //   254: astore #9
    //   256: goto -> 262
    //   259: aconst_null
    //   260: astore #9
    //   262: aload_1
    //   263: new me/kaimson/melonclient/ingames/utils/ReplayMod/viewer/gui/overlay/GuiReplayList$Entry
    //   266: dup
    //   267: aload #5
    //   269: new com/replaymod/replaystudio/replay/ZipReplayFile
    //   272: dup
    //   273: new com/replaymod/replaystudio/studio/ReplayStudio
    //   276: dup
    //   277: invokespecial <init> : ()V
    //   280: aload #5
    //   282: invokespecial <init> : (Lcom/replaymod/replaystudio/Studio;Ljava/io/File;)V
    //   285: aload #9
    //   287: invokespecial <init> : (Ljava/io/File;Lcom/replaymod/replaystudio/replay/ReplayFile;Ljava/awt/image/BufferedImage;)V
    //   290: invokevirtual add : (Ljava/lang/Object;)Z
    //   293: pop
    //   294: goto -> 341
    //   297: new java/lang/StringBuilder
    //   300: dup
    //   301: invokespecial <init> : ()V
    //   304: ldc 'Invalid replay '
    //   306: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   309: aload #5
    //   311: invokevirtual getName : ()Ljava/lang/String;
    //   314: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   317: ldc ' ('
    //   319: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   322: aload #5
    //   324: invokevirtual getAbsolutePath : ()Ljava/lang/String;
    //   327: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   330: ldc ')!'
    //   332: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   335: invokevirtual toString : ()Ljava/lang/String;
    //   338: invokestatic log : (Ljava/lang/Object;)V
    //   341: aload #6
    //   343: ifnull -> 436
    //   346: aload #7
    //   348: ifnull -> 373
    //   351: aload #6
    //   353: invokeinterface close : ()V
    //   358: goto -> 436
    //   361: astore #8
    //   363: aload #7
    //   365: aload #8
    //   367: invokevirtual addSuppressed : (Ljava/lang/Throwable;)V
    //   370: goto -> 436
    //   373: aload #6
    //   375: invokeinterface close : ()V
    //   380: goto -> 436
    //   383: astore #8
    //   385: aload #8
    //   387: astore #7
    //   389: aload #8
    //   391: athrow
    //   392: astore #12
    //   394: aload #6
    //   396: ifnull -> 433
    //   399: aload #7
    //   401: ifnull -> 426
    //   404: aload #6
    //   406: invokeinterface close : ()V
    //   411: goto -> 433
    //   414: astore #13
    //   416: aload #7
    //   418: aload #13
    //   420: invokevirtual addSuppressed : (Ljava/lang/Throwable;)V
    //   423: goto -> 433
    //   426: aload #6
    //   428: invokeinterface close : ()V
    //   433: aload #12
    //   435: athrow
    //   436: goto -> 477
    //   439: astore #6
    //   441: aload #6
    //   443: invokevirtual printStackTrace : ()V
    //   446: new java/lang/StringBuilder
    //   449: dup
    //   450: invokespecial <init> : ()V
    //   453: ldc 'Error while trying to parse file '
    //   455: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   458: aload #5
    //   460: invokevirtual getName : ()Ljava/lang/String;
    //   463: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   466: ldc ' as a replay!'
    //   468: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   471: invokevirtual toString : ()Ljava/lang/String;
    //   474: invokestatic log : (Ljava/lang/Object;)V
    //   477: iinc #4, 1
    //   480: goto -> 38
    //   483: goto -> 496
    //   486: astore_2
    //   487: aload_2
    //   488: invokevirtual printStackTrace : ()V
    //   491: ldc 'Could not look through replays!'
    //   493: invokestatic log : (Ljava/lang/Object;)V
    //   496: aload_1
    //   497: areturn
    // Line number table:
    //   Java source line number -> byte code offset
    //   #147	-> 0
    //   #149	-> 4
    //   #150	-> 50
    //   #151	-> 71
    //   #152	-> 81
    //   #153	-> 94
    //   #178	-> 138
    //   #156	-> 180
    //   #159	-> 189
    //   #160	-> 197
    //   #163	-> 207
    //   #164	-> 233
    //   #171	-> 256
    //   #172	-> 259
    //   #174	-> 262
    //   #175	-> 294
    //   #176	-> 297
    //   #178	-> 341
    //   #150	-> 383
    //   #178	-> 392
    //   #181	-> 436
    //   #178	-> 439
    //   #179	-> 441
    //   #180	-> 446
    //   #149	-> 477
    //   #186	-> 483
    //   #183	-> 486
    //   #184	-> 487
    //   #185	-> 491
    //   #187	-> 496
    // Local variable table:
    //   start	length	slot	name	descriptor
    //   207	49	10	buf	Ljava/awt/image/BufferedImage;
    //   233	23	11	theIntArray	[I
    //   256	3	9	theThumb	Ljava/awt/image/BufferedImage;
    //   189	105	8	thumb	Lcom/google/common/base/Optional;
    //   262	32	9	theThumb	Ljava/awt/image/BufferedImage;
    //   68	368	6	replayFile	Lcom/replaymod/replaystudio/replay/ReplayFile;
    //   441	36	6	e	Ljava/lang/Exception;
    //   50	427	5	file	Ljava/io/File;
    //   487	9	2	e	Ljava/lang/Exception;
    //   0	498	0	this	Lme/kaimson/melonclient/ingames/utils/ReplayMod/viewer/gui/overlay/GuiReplayList;
    //   4	494	1	replays	Ljava/util/ArrayList;
    // Local variable type table:
    //   start	length	slot	name	signature
    //   189	105	8	thumb	Lcom/google/common/base/Optional<Ljava/awt/image/BufferedImage;>;
    //   4	494	1	replays	Ljava/util/ArrayList<Lme/kaimson/melonclient/ingames/utils/ReplayMod/viewer/gui/overlay/GuiReplayList$Entry;>;
    // Exception table:
    //   from	to	target	type
    //   4	483	486	java/lang/Exception
    //   50	177	439	java/lang/Exception
    //   71	138	383	java/lang/Throwable
    //   71	138	392	finally
    //   148	155	158	java/lang/Throwable
    //   180	341	383	java/lang/Throwable
    //   180	341	392	finally
    //   180	436	439	java/lang/Exception
    //   351	358	361	java/lang/Throwable
    //   383	394	392	finally
    //   404	411	414	java/lang/Throwable
  }
  
  private class Slot extends GuiSlot {
    private Runnable elementDoubleClicked;
    
    private Runnable elementClicked;
    
    private final List<GuiReplayList.Entry> list;
    
    private int selectedSlotIndex = -1;
    
    public Slot(List<GuiReplayList.Entry> list, GuiScreen parentScreen) {
      super(Minecraft.getMinecraft(), parentScreen.width, parentScreen.height, 30, parentScreen.height - 65, 34);
      this.list = list;
    }
    
    protected int getSize() {
      return this.list.size();
    }
    
    public Slot onElementClicked(Runnable elementClicked) {
      this.elementClicked = elementClicked;
      return this;
    }
    
    public Slot onElementDoubleClicked(Runnable elementDoubleClicked) {
      this.elementDoubleClicked = elementDoubleClicked;
      return this;
    }
    
    protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
      this.selectedSlotIndex = slotIndex;
      if (this.elementClicked != null)
        this.elementClicked.run(); 
      if (this.elementDoubleClicked != null && isDoubleClick)
        this.elementDoubleClicked.run(); 
    }
    
    protected boolean isSelected(int slotIndex) {
      return (this.selectedSlotIndex == slotIndex);
    }
    
    protected void drawBackground() {}
    
    public int getListWidth() {
      return super.getListWidth() + 85;
    }
    
    public int getScrollBarX() {
      return super.getScrollBarX() + 30;
    }
    
    protected void drawSelectionBox(int p_148120_1_, int p_148120_2_, int mouseXIn, int mouseYIn) {
      int i = getSize();
      Tessellator tessellator = Tessellator.getInstance();
      WorldRenderer worldrenderer = tessellator.getWorldRenderer();
      for (int j = 0; j < i; j++) {
        int k = p_148120_2_ + j * this.slotHeight + this.headerPadding;
        int l = this.slotHeight - 4;
        if (k > this.bottom || k + l < this.top)
          func_178040_a(j, p_148120_1_, k); 
        if (this.showSelectionBox && isSelected(j)) {
          int i1 = this.left + this.width / 2 - getListWidth() / 2;
          int j1 = this.left + this.width / 2 + getListWidth() / 2;
          GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
          GlStateManager.disableTexture2D();
          GuiUtils.instance.drawGradientRect(i1, k - 2, i1 + 1, k + l + 2, (new Color(0, 230, 230, 255)).getRGB(), (new Color(0, 120, 120, 255)).getRGB());
          GuiUtils.instance.drawGradientRect(j1 - 1, k - 2, j1, k + l + 2, (new Color(0, 230, 230, 255)).getRGB(), (new Color(0, 120, 120, 255)).getRGB());
          Gui.drawRect(i1, k - 2 + 1, j1, k - 2, (new Color(0, 230, 230, 255)).getRGB());
          Gui.drawRect(i1, k + l + 2, j1, k + l + 2 - 1, (new Color(0, 120, 120, 255)).getRGB());
          GlStateManager.enableTexture2D();
        } 
        if (k >= this.top - this.slotHeight && k <= this.bottom)
          drawSlot(j, p_148120_1_, k, l, mouseXIn, mouseYIn); 
      } 
    }
    
    protected void drawSlot(int entryID, int p_180791_2_, int p_180791_3_, int p_180791_4_, int mouseXIn, int mouseYIn) {
      try {
        Gui.drawRect(p_180791_2_ - 1, p_180791_3_, p_180791_2_ + getListWidth() - 5, p_180791_3_ + getSlotHeight() - 4, (new Color(70, 70, 70, 80)).getRGB());
        GuiReplayList.Entry entry = this.list.get(entryID);
        this.mc.fontRendererObj.drawString(ChatFormatting.UNDERLINE + entry.getFile().getName(), p_180791_2_ + 53 + 2, p_180791_3_, 16777215);
        this.mc.fontRendererObj.drawString(ChatFormatting.GRAY + entry.getReplayFile().getMetaData().getServerName(), p_180791_2_ + 53 + 2, p_180791_3_ + this.mc.fontRendererObj.FONT_HEIGHT + 2, 16777215);
        this.mc.fontRendererObj.drawString(ChatFormatting.GRAY + (new SimpleDateFormat()).format(Long.valueOf(entry.getReplayFile().getMetaData().getDate())), p_180791_2_ + 53 + 2, p_180791_3_ + this.mc.fontRendererObj.FONT_HEIGHT * 2 + 3, 16777215);
        GlStateManager.color(0.0F, 0.625F, 0.625F, 1.0F);
        GlStateManager.bindTexture(((entry.getBufferedImage() == null) ? Integer.valueOf(GuiReplayList.this.DEFAULT_THUMBNAIL) : null).intValue());
        Gui.drawModalRectWithCustomSizedTexture(p_180791_2_ - 1, p_180791_3_, 0.0F, 0.0F, 53, 30, 53.0F, 30.0F);
        String duration = Utils.convertSecondsToShortString(entry.getReplayFile().getMetaData().getDuration() / 1000);
        this.mc.fontRendererObj.drawString(duration, p_180791_2_ + 53 + 2 - this.mc.fontRendererObj.getStringWidth(duration) - 6, p_180791_3_ + this.mc.fontRendererObj.FONT_HEIGHT * 2 + 3, 16777215);
      } catch (Exception e) {
        e.printStackTrace();
      } 
    }
  }
  
  private static class Entry {
    private final File file;
    
    private final ReplayFile replayFile;
    
    private final BufferedImage bufferedImage;
    
    public File getFile() {
      return this.file;
    }
    
    public ReplayFile getReplayFile() {
      return this.replayFile;
    }
    
    public BufferedImage getBufferedImage() {
      return this.bufferedImage;
    }
    
    public Entry(File file, ReplayFile replayFile, BufferedImage bufferedImage) {
      this.file = file;
      this.replayFile = replayFile;
      this.bufferedImage = bufferedImage;
    }
  }
}
