package com.replaymod.lib.de.johni0702.minecraft.gui.element.advanced;

import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.AbstractGuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Clickable;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Focusable;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Tickable;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Typeable;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Consumer;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Utils;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Color;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Point;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableColor;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.MCVer;
import java.util.Arrays;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatAllowedCharacters;

public abstract class AbstractGuiTextArea<T extends AbstractGuiTextArea<T>> extends AbstractGuiElement<T> implements Clickable, Typeable, Tickable, IGuiTextArea<T> {
  private static final ReadableColor BACKGROUND_COLOR = (ReadableColor)new Color(160, 160, 160);
  
  private static final ReadableColor CURSOR_COLOR = (ReadableColor)new Color(240, 240, 240);
  
  private static final int BORDER = 4;
  
  private static final int LINE_SPACING = 2;
  
  private boolean focused;
  
  private Focusable next;
  
  private Focusable previous;
  
  private Consumer<Boolean> focusChanged;
  
  public boolean isFocused() {
    return this.focused;
  }
  
  public Focusable getNext() {
    return this.next;
  }
  
  public Focusable getPrevious() {
    return this.previous;
  }
  
  private int maxTextWidth = -1;
  
  public int getMaxTextWidth() {
    return this.maxTextWidth;
  }
  
  private int maxTextHeight = -1;
  
  public int getMaxTextHeight() {
    return this.maxTextHeight;
  }
  
  private int maxCharCount = -1;
  
  public int getMaxCharCount() {
    return this.maxCharCount;
  }
  
  private String[] text = new String[] { "" };
  
  private String[] hint;
  
  private int cursorX;
  
  private int cursorY;
  
  private int selectionX;
  
  private int selectionY;
  
  private int currentXOffset;
  
  private int currentYOffset;
  
  private int blinkCursorTick;
  
  public ReadableColor textColorEnabled = (ReadableColor)new Color(224, 224, 224);
  
  public ReadableColor textColorDisabled = (ReadableColor)new Color(112, 112, 112);
  
  private ReadableDimension size = (ReadableDimension)new Dimension(0, 0);
  
  public AbstractGuiTextArea(GuiContainer container) {
    super(container);
  }
  
  public T setText(String[] lines) {
    if (lines.length > this.maxTextHeight)
      lines = Arrays.<String>copyOf(lines, this.maxTextHeight); 
    this.text = lines;
    for (int i = 0; i < lines.length; i++) {
      if (lines[i].length() > this.maxTextWidth)
        lines[i] = lines[i].substring(0, this.maxTextWidth); 
    } 
    return (T)getThis();
  }
  
  public String[] getText() {
    return this.text;
  }
  
  public String getText(int fromX, int fromY, int toX, int toY) {
    StringBuilder sb = new StringBuilder();
    if (fromY == toY) {
      sb.append(this.text[fromY].substring(fromX, toX));
    } else {
      sb.append(this.text[fromY].substring(fromX)).append('\n');
      for (int y = fromY + 1; y < toY; y++)
        sb.append(this.text[y]).append('\n'); 
      sb.append(this.text[toY].substring(0, toX));
    } 
    return sb.toString();
  }
  
  private void deleteText(int fromX, int fromY, int toX, int toY) {
    String[] newText = new String[this.text.length - toY - fromY];
    if (fromY > 0)
      System.arraycopy(this.text, 0, newText, 0, fromY); 
    newText[fromY] = this.text[fromY].substring(0, fromX) + this.text[toY].substring(toX);
    if (toY + 1 < this.text.length)
      System.arraycopy(this.text, toY + 1, newText, fromY + 1, this.text.length - toY - 1); 
    this.text = newText;
  }
  
  public int getSelectionFromX() {
    if (this.cursorY == this.selectionY)
      return (this.cursorX > this.selectionX) ? this.selectionX : this.cursorX; 
    return (this.cursorY > this.selectionY) ? this.selectionX : this.cursorX;
  }
  
  public int getSelectionToX() {
    if (this.cursorY == this.selectionY)
      return (this.cursorX > this.selectionX) ? this.cursorX : this.selectionX; 
    return (this.cursorY > this.selectionY) ? this.cursorX : this.selectionX;
  }
  
  public int getSelectionFromY() {
    return (this.cursorY > this.selectionY) ? this.selectionY : this.cursorY;
  }
  
  public int getSelectionToY() {
    return (this.cursorY > this.selectionY) ? this.cursorY : this.selectionY;
  }
  
  public String getSelectedText() {
    if (this.cursorX == this.selectionX && this.cursorY == this.selectionY)
      return ""; 
    int fromX = getSelectionFromX();
    int fromY = getSelectionFromY();
    int toX = getSelectionToX();
    int toY = getSelectionToY();
    return getText(fromX, fromY, toX, toY);
  }
  
  public void deleteSelectedText() {
    if (this.cursorX == this.selectionX && this.cursorY == this.selectionY)
      return; 
    int fromX = getSelectionFromX();
    int fromY = getSelectionFromY();
    int toX = getSelectionToX();
    int toY = getSelectionToY();
    deleteText(fromX, fromY, toX, toY);
    this.cursorX = this.selectionX = fromX;
    this.cursorY = this.selectionY = fromY;
  }
  
  private void updateCurrentOffset() {
    this.currentXOffset = Math.min(this.currentXOffset, this.cursorX);
    String line = this.text[this.cursorY].substring(this.currentXOffset, this.cursorX);
    FontRenderer fontRenderer = MCVer.getFontRenderer();
    int currentWidth = fontRenderer.func_78256_a(line);
    if (currentWidth > this.size.getWidth() - 8)
      this.currentXOffset = this.cursorX - fontRenderer.func_78262_a(line, this.size.getWidth() - 8, true).length(); 
    this.currentYOffset = Math.min(this.currentYOffset, this.cursorY);
    int lineHeight = (MCVer.getFontRenderer()).field_78288_b + 2;
    int contentHeight = this.size.getHeight() - 8;
    int maxLines = contentHeight / lineHeight;
    if (this.cursorY - this.currentYOffset >= maxLines)
      this.currentYOffset = this.cursorY - maxLines + 1; 
  }
  
  public String cutSelectedText() {
    String selection = getSelectedText();
    deleteSelectedText();
    return selection;
  }
  
  public void writeText(String append) {
    for (char c : append.toCharArray())
      writeChar(c); 
  }
  
  public void writeChar(char c) {
    if (!ChatAllowedCharacters.func_71566_a(c))
      return; 
    int totalCharCount = 0;
    for (String line : this.text)
      totalCharCount += line.length(); 
    if (this.maxCharCount > 0 && totalCharCount - getSelectedText().length() >= this.maxCharCount)
      return; 
    deleteSelectedText();
    if (c == '\n') {
      if (this.text.length >= this.maxTextHeight)
        return; 
      String[] newText = new String[this.text.length + 1];
      if (this.cursorY > 0)
        System.arraycopy(this.text, 0, newText, 0, this.cursorY); 
      newText[this.cursorY] = this.text[this.cursorY].substring(0, this.cursorX);
      newText[this.cursorY + 1] = this.text[this.cursorY].substring(this.cursorX);
      if (this.cursorY + 1 < this.text.length)
        System.arraycopy(this.text, this.cursorY + 1, newText, this.cursorY + 2, this.text.length - this.cursorY - 1); 
      this.text = newText;
      this.selectionX = this.cursorX = 0;
      this.selectionY = ++this.cursorY;
    } else {
      String line = this.text[this.cursorY];
      if (line.length() >= this.maxTextWidth)
        return; 
      line = line.substring(0, this.cursorX) + c + line.substring(this.cursorX);
      this.text[this.cursorY] = line;
      this.selectionX = ++this.cursorX;
    } 
  }
  
  private void deleteNextChar() {
    String line = this.text[this.cursorY];
    if (this.cursorX < line.length()) {
      line = line.substring(0, this.cursorX) + line.substring(this.cursorX + 1);
      this.text[this.cursorY] = line;
    } else if (this.cursorY + 1 < this.text.length) {
      deleteText(this.cursorX, this.cursorY, 0, this.cursorY + 1);
    } 
  }
  
  private int getNextWordLength() {
    int length = 0;
    String line = this.text[this.cursorY];
    boolean inWord = true;
    for (int i = this.cursorX; i < line.length(); i++) {
      if (inWord) {
        if (line.charAt(i) == ' ')
          inWord = false; 
      } else if (line.charAt(i) != ' ') {
        return length;
      } 
      length++;
    } 
    return length;
  }
  
  private void deleteNextWord() {
    int worldLength = getNextWordLength();
    if (worldLength == 0) {
      deleteNextChar();
    } else {
      deleteText(this.cursorX, this.cursorY, this.cursorX + worldLength, this.cursorY);
    } 
  }
  
  private void deletePreviousChar() {
    if (this.cursorX > 0) {
      String line = this.text[this.cursorY];
      line = line.substring(0, this.cursorX - 1) + line.substring(this.cursorX);
      this.selectionX = --this.cursorX;
      this.text[this.cursorY] = line;
    } else if (this.cursorY > 0) {
      int fromX = this.text[this.cursorY - 1].length();
      deleteText(fromX, this.cursorY - 1, this.cursorX, this.cursorY);
      this.selectionX = this.cursorX = fromX;
      this.selectionY = --this.cursorY;
    } 
  }
  
  private int getPreviousWordLength() {
    int length = 0;
    String line = this.text[this.cursorY];
    boolean inWord = false;
    for (int i = this.cursorX - 1; i >= 0; i--) {
      if (inWord) {
        if (line.charAt(i) == ' ')
          return length; 
      } else if (line.charAt(i) != ' ') {
        inWord = true;
      } 
      length++;
    } 
    return length;
  }
  
  private void deletePreviousWord() {
    int worldLength = getPreviousWordLength();
    if (worldLength == 0) {
      deletePreviousChar();
    } else {
      deleteText(this.cursorX, this.cursorY, this.cursorX - worldLength, this.cursorY);
      this.selectionX = this.cursorX -= worldLength;
    } 
  }
  
  public T setCursorPosition(int x, int y) {
    this.selectionY = this.cursorY = Utils.clamp(y, 0, this.text.length - 1);
    this.selectionX = this.cursorX = Utils.clamp(x, 0, this.text[this.cursorY].length());
    return (T)getThis();
  }
  
  protected ReadableDimension calcMinSize() {
    return (ReadableDimension)new Dimension(0, 0);
  }
  
  public boolean mouseClick(ReadablePoint position, int button) {
    Point point;
    if (getContainer() != null)
      getContainer().convertFor(this, point = new Point(position)); 
    boolean hovering = isMouseHovering((ReadablePoint)point);
    if (hovering && isFocused() && button == 0) {
      updateCurrentOffset();
      int mouseX = point.getX() - 4;
      int mouseY = point.getY() - 4;
      FontRenderer fontRenderer = MCVer.getFontRenderer();
      int textY = Utils.clamp(mouseY / (fontRenderer.field_78288_b + 2) + this.currentYOffset, 0, this.text.length - 1);
      if (this.cursorY != textY)
        this.currentXOffset = 0; 
      String line = this.text[textY].substring(this.currentXOffset);
      int textX = fontRenderer.func_78269_a(line, mouseX).length() + this.currentXOffset;
      setCursorPosition(textX, textY);
    } 
    setFocused(hovering);
    return hovering;
  }
  
  protected boolean isMouseHovering(ReadablePoint pos) {
    return (pos.getX() > 0 && pos.getY() > 0 && pos
      .getX() < this.size.getWidth() && pos.getY() < this.size.getHeight());
  }
  
  public T setFocused(boolean isFocused) {
    if (isFocused && !this.focused)
      this.blinkCursorTick = 0; 
    if (this.focused != isFocused) {
      this.focused = isFocused;
      onFocusChanged(this.focused);
    } 
    return (T)getThis();
  }
  
  public T setNext(Focusable next) {
    this.next = next;
    return (T)getThis();
  }
  
  public T setPrevious(Focusable previous) {
    this.previous = previous;
    return (T)getThis();
  }
  
  public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
    this.size = size;
    updateCurrentOffset();
    super.draw(renderer, size, renderInfo);
    FontRenderer fontRenderer = MCVer.getFontRenderer();
    int width = size.getWidth();
    int height = size.getHeight();
    renderer.drawRect(0, 0, width, height, BACKGROUND_COLOR);
    renderer.drawRect(1, 1, width - 2, height - 2, ReadableColor.BLACK);
    ReadableColor textColor = isEnabled() ? this.textColorEnabled : this.textColorDisabled;
    int lineHeight = fontRenderer.field_78288_b + 2;
    int contentHeight = height - 8;
    int maxLines = contentHeight / lineHeight;
    int contentWidth = width - 8;
    if (this.hint != null && !isFocused() && Arrays.<String>stream(this.text).allMatch(String::isEmpty)) {
      for (int j = 0; j < maxLines && j < this.hint.length; j++) {
        String line = fontRenderer.func_78269_a(this.hint[j], contentWidth);
        int posY = 4 + j * lineHeight;
        renderer.drawString(4, posY, this.textColorDisabled, line, true);
      } 
      return;
    } 
    for (int i = 0; i < maxLines && i + this.currentYOffset < this.text.length; i++) {
      int lineY = i + this.currentYOffset;
      String line = this.text[lineY];
      int leftTrimmed = 0;
      if (lineY == this.cursorY) {
        line = line.substring(this.currentXOffset);
        leftTrimmed = this.currentXOffset;
      } 
      line = fontRenderer.func_78269_a(line, contentWidth);
      int posY = 4 + i * lineHeight;
      int lineEnd = renderer.drawString(4, posY, textColor, line, true);
      int fromX = getSelectionFromX();
      int fromY = getSelectionFromY();
      int toX = getSelectionToX();
      int toY = getSelectionToY();
      if (lineY > fromY && lineY < toY) {
        MCVer.invertColors(renderer, lineEnd, posY - 1 + lineHeight, 4, posY - 1);
      } else if (lineY == fromY && lineY == toY) {
        String leftStr = line.substring(0, Utils.clamp(fromX - leftTrimmed, 0, line.length()));
        String rightStr = line.substring(Utils.clamp(toX - leftTrimmed, 0, line.length()));
        int left = 4 + fontRenderer.func_78256_a(leftStr);
        int right = lineEnd - fontRenderer.func_78256_a(rightStr) - 1;
        MCVer.invertColors(renderer, right, posY - 1 + lineHeight, left, posY - 1);
      } else if (lineY == fromY) {
        String rightStr = line.substring(Utils.clamp(fromX - leftTrimmed, 0, line.length()));
        MCVer.invertColors(renderer, lineEnd, posY - 1 + lineHeight, lineEnd - fontRenderer.func_78256_a(rightStr), posY - 1);
      } else if (lineY == toY) {
        String leftStr = line.substring(0, Utils.clamp(toX - leftTrimmed, 0, line.length()));
        int right = 4 + fontRenderer.func_78256_a(leftStr);
        MCVer.invertColors(renderer, right, posY - 1 + lineHeight, 4, posY - 1);
      } 
      if (lineY == this.cursorY && this.blinkCursorTick / 6 % 2 == 0 && this.focused) {
        String beforeCursor = line.substring(0, this.cursorX - leftTrimmed);
        int posX = 4 + fontRenderer.func_78256_a(beforeCursor);
        if (this.cursorX == this.text[lineY].length()) {
          renderer.drawString(posX, posY, CURSOR_COLOR, "_", true);
        } else {
          renderer.drawRect(posX, posY - 1, 1, 1 + fontRenderer.field_78288_b, CURSOR_COLOR);
        } 
      } 
    } 
  }
  
  public boolean typeKey(ReadablePoint mousePosition, int keyCode, char keyChar, boolean ctrlDown, boolean shiftDown) {
    if (keyCode == 15) {
      Focusable other = shiftDown ? this.previous : this.next;
      if (other != null) {
        setFocused(false);
        other.setFocused(true);
      } 
      return true;
    } 
    if (!this.focused)
      return false; 
    if (GuiScreen.func_146271_m())
      switch (keyCode) {
        case 30:
          this.cursorX = this.cursorY = 0;
          this.selectionY = this.text.length - 1;
          this.selectionX = this.text[this.selectionY].length();
          return true;
        case 46:
          MCVer.setClipboardString(getSelectedText());
          return true;
        case 47:
          if (isEnabled())
            writeText(MCVer.getClipboardString()); 
          return true;
        case 45:
          if (isEnabled())
            MCVer.setClipboardString(cutSelectedText()); 
          return true;
      }  
    boolean words = GuiScreen.func_146271_m();
    boolean select = GuiScreen.func_146272_n();
    switch (keyCode) {
      case 199:
        this.cursorX = 0;
        break;
      case 207:
        this.cursorX = this.text[this.cursorY].length();
        break;
      case 203:
        if (this.cursorX == 0) {
          if (this.cursorY > 0) {
            this.cursorY--;
            this.cursorX = this.text[this.cursorY].length();
          } 
          break;
        } 
        if (words) {
          this.cursorX -= getPreviousWordLength();
          break;
        } 
        this.cursorX--;
        break;
      case 205:
        if (this.cursorX == this.text[this.cursorY].length()) {
          if (this.cursorY < this.text.length - 1) {
            this.cursorY++;
            this.cursorX = 0;
          } 
          break;
        } 
        if (words) {
          this.cursorX += getNextWordLength();
          break;
        } 
        this.cursorX++;
        break;
      case 200:
        if (this.cursorY > 0) {
          this.cursorY--;
          this.cursorX = Math.min(this.cursorX, this.text[this.cursorY].length());
        } 
        break;
      case 208:
        if (this.cursorY + 1 < this.text.length) {
          this.cursorY++;
          this.cursorX = Math.min(this.cursorX, this.text[this.cursorY].length());
        } 
        break;
      case 14:
        if (isEnabled())
          if (getSelectedText().length() > 0) {
            deleteSelectedText();
          } else if (words) {
            deletePreviousWord();
          } else {
            deletePreviousChar();
          }  
        return true;
      case 211:
        if (isEnabled())
          if (getSelectedText().length() > 0) {
            deleteSelectedText();
          } else if (words) {
            deleteNextWord();
          } else {
            deleteNextChar();
          }  
        return true;
      default:
        if (isEnabled()) {
          if (keyChar == '\r')
            keyChar = '\n'; 
          writeChar(keyChar);
        } 
        return true;
    } 
    if (!select) {
      this.selectionX = this.cursorX;
      this.selectionY = this.cursorY;
    } 
    return true;
  }
  
  public void tick() {
    this.blinkCursorTick++;
  }
  
  public T setMaxTextWidth(int maxTextWidth) {
    this.maxTextWidth = maxTextWidth;
    return (T)getThis();
  }
  
  public T setMaxTextHeight(int maxTextHeight) {
    this.maxTextHeight = maxTextHeight;
    return (T)getThis();
  }
  
  public T setMaxCharCount(int maxCharCount) {
    this.maxCharCount = maxCharCount;
    return (T)getThis();
  }
  
  public T setTextColor(ReadableColor textColor) {
    this.textColorEnabled = textColor;
    return (T)getThis();
  }
  
  public T setTextColorDisabled(ReadableColor textColorDisabled) {
    this.textColorDisabled = textColorDisabled;
    return (T)getThis();
  }
  
  public T onFocusChange(Consumer<Boolean> focusChanged) {
    this.focusChanged = focusChanged;
    return (T)getThis();
  }
  
  protected void onFocusChanged(boolean focused) {
    if (this.focusChanged != null)
      this.focusChanged.consume(Boolean.valueOf(focused)); 
  }
  
  public String[] getHint() {
    return this.hint;
  }
  
  public T setHint(String... hint) {
    this.hint = hint;
    return (T)getThis();
  }
  
  public T setI18nHint(String hint, Object... args) {
    setHint(I18n.func_135052_a(hint, args).split("/n"));
    return (T)getThis();
  }
  
  public AbstractGuiTextArea() {}
}
