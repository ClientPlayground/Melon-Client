package com.replaymod.lib.de.johni0702.minecraft.gui.element;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
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
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatAllowedCharacters;

public abstract class AbstractGuiTextField<T extends AbstractGuiTextField<T>> extends AbstractGuiElement<T> implements Clickable, Tickable, Typeable, IGuiTextField<T> {
  private static final ReadableColor BORDER_COLOR = (ReadableColor)new Color(160, 160, 160);
  
  private static final ReadableColor CURSOR_COLOR = (ReadableColor)new Color(240, 240, 240);
  
  private static final int BORDER = 4;
  
  private boolean focused;
  
  private Focusable next;
  
  private Focusable previous;
  
  public boolean isFocused() {
    return this.focused;
  }
  
  public Focusable getNext() {
    return this.next;
  }
  
  public Focusable getPrevious() {
    return this.previous;
  }
  
  private int maxLength = 32;
  
  public int getMaxLength() {
    return this.maxLength;
  }
  
  private String text = "";
  
  private int cursorPos;
  
  private int selectionPos;
  
  private String hint;
  
  private int currentOffset;
  
  private int blinkCursorTick;
  
  public String getText() {
    return this.text;
  }
  
  public String getHint() {
    return this.hint;
  }
  
  private ReadableColor textColorEnabled = (ReadableColor)new Color(224, 224, 224);
  
  private ReadableColor textColorDisabled = (ReadableColor)new Color(112, 112, 112);
  
  private ReadableDimension size = (ReadableDimension)new Dimension(0, 0);
  
  private Consumer<String> textChanged;
  
  private Consumer<Boolean> focusChanged;
  
  private Runnable onEnter;
  
  public AbstractGuiTextField(GuiContainer container) {
    super(container);
  }
  
  public T setText(String text) {
    if (text.length() > this.maxLength)
      text = text.substring(0, this.maxLength); 
    this.text = text;
    this.selectionPos = this.cursorPos = text.length();
    return getThis();
  }
  
  public T setI18nText(String text, Object... args) {
    return setText(I18n.func_135052_a(text, args));
  }
  
  public T setMaxLength(int maxLength) {
    Preconditions.checkArgument((maxLength >= 0), "maxLength must not be negative");
    this.maxLength = maxLength;
    if (this.text.length() > maxLength)
      setText(this.text); 
    return getThis();
  }
  
  public String deleteText(int from, int to) {
    Preconditions.checkArgument((from <= to), "from must not be greater than to");
    Preconditions.checkArgument((from >= 0), "from must be greater than zero");
    Preconditions.checkArgument((to < this.text.length()), "to must be less than test.length()");
    String deleted = this.text.substring(from, to + 1);
    this.text = this.text.substring(0, from) + this.text.substring(to + 1);
    return deleted;
  }
  
  public int getSelectionFrom() {
    return (this.cursorPos > this.selectionPos) ? this.selectionPos : this.cursorPos;
  }
  
  public int getSelectionTo() {
    return (this.cursorPos > this.selectionPos) ? this.cursorPos : this.selectionPos;
  }
  
  public String getSelectedText() {
    return this.text.substring(getSelectionFrom(), getSelectionTo());
  }
  
  public String deleteSelectedText() {
    if (this.cursorPos == this.selectionPos)
      return ""; 
    int from = getSelectionFrom();
    String deleted = deleteText(from, getSelectionTo() - 1);
    this.cursorPos = this.selectionPos = from;
    return deleted;
  }
  
  private void updateCurrentOffset() {
    this.currentOffset = Math.min(this.currentOffset, this.cursorPos);
    String line = this.text.substring(this.currentOffset, this.cursorPos);
    FontRenderer fontRenderer = MCVer.getFontRenderer();
    int currentWidth = fontRenderer.func_78256_a(line);
    if (currentWidth > this.size.getWidth() - 8)
      this.currentOffset = this.cursorPos - fontRenderer.func_78262_a(line, this.size.getWidth() - 8, true).length(); 
  }
  
  public T writeText(String append) {
    for (char c : append.toCharArray())
      writeChar(c); 
    return getThis();
  }
  
  public T writeChar(char c) {
    if (!ChatAllowedCharacters.func_71566_a(c))
      return getThis(); 
    deleteSelectedText();
    if (this.text.length() >= this.maxLength)
      return getThis(); 
    this.text = this.text.substring(0, this.cursorPos) + c + this.text.substring(this.cursorPos);
    this.selectionPos = ++this.cursorPos;
    return getThis();
  }
  
  public T deleteNextChar() {
    if (this.cursorPos < this.text.length())
      this.text = this.text.substring(0, this.cursorPos) + this.text.substring(this.cursorPos + 1); 
    this.selectionPos = this.cursorPos;
    return getThis();
  }
  
  protected int getNextWordLength() {
    int length = 0;
    boolean inWord = true;
    for (int i = this.cursorPos; i < this.text.length(); i++) {
      if (inWord) {
        if (this.text.charAt(i) == ' ')
          inWord = false; 
      } else if (this.text.charAt(i) != ' ') {
        return length;
      } 
      length++;
    } 
    return length;
  }
  
  public String deleteNextWord() {
    int worldLength = getNextWordLength();
    if (worldLength > 0)
      return deleteText(this.cursorPos, this.cursorPos + worldLength - 1); 
    return "";
  }
  
  public T deletePreviousChar() {
    if (this.cursorPos > 0) {
      this.text = this.text.substring(0, this.cursorPos - 1) + this.text.substring(this.cursorPos);
      this.selectionPos = --this.cursorPos;
    } 
    return getThis();
  }
  
  protected int getPreviousWordLength() {
    int length = 0;
    boolean inWord = false;
    for (int i = this.cursorPos - 1; i >= 0; i--) {
      if (inWord) {
        if (this.text.charAt(i) == ' ')
          return length; 
      } else if (this.text.charAt(i) != ' ') {
        inWord = true;
      } 
      length++;
    } 
    return length;
  }
  
  public String deletePreviousWord() {
    int worldLength = getPreviousWordLength();
    String deleted = "";
    if (worldLength > 0) {
      deleted = deleteText(this.cursorPos - worldLength, this.cursorPos - 1);
      this.selectionPos = this.cursorPos -= worldLength;
    } 
    return deleted;
  }
  
  public T setCursorPosition(int pos) {
    Preconditions.checkArgument((pos >= 0 && pos <= this.text.length()));
    this.selectionPos = this.cursorPos = pos;
    return getThis();
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
      FontRenderer fontRenderer = MCVer.getFontRenderer();
      String text = this.text.substring(this.currentOffset);
      int textX = fontRenderer.func_78269_a(text, mouseX).length() + this.currentOffset;
      setCursorPosition(textX);
      return true;
    } 
    setFocused(hovering);
    return false;
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
    return getThis();
  }
  
  public T setNext(Focusable next) {
    this.next = next;
    return getThis();
  }
  
  public T setPrevious(Focusable previous) {
    this.previous = previous;
    return getThis();
  }
  
  public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
    this.size = size;
    updateCurrentOffset();
    super.draw(renderer, size, renderInfo);
    int width = size.getWidth(), height = size.getHeight();
    FontRenderer fontRenderer = MCVer.getFontRenderer();
    int posY = height / 2 - fontRenderer.field_78288_b / 2;
    renderer.drawRect(0, 0, width, height, BORDER_COLOR);
    renderer.drawRect(1, 1, width - 2, height - 2, ReadableColor.BLACK);
    if (this.text.isEmpty() && !isFocused() && !Strings.isNullOrEmpty(this.hint)) {
      String text = fontRenderer.func_78269_a(this.hint, width - 8);
      renderer.drawString(4, posY, this.textColorDisabled, text);
    } else {
      String renderText = this.text.substring(this.currentOffset);
      renderText = fontRenderer.func_78269_a(renderText, width - 8);
      ReadableColor color = isEnabled() ? this.textColorEnabled : this.textColorDisabled;
      int lineEnd = renderer.drawString(4, height / 2 - fontRenderer.field_78288_b / 2, color, renderText);
      int from = getSelectionFrom();
      int to = getSelectionTo();
      String leftStr = renderText.substring(0, Utils.clamp(from - this.currentOffset, 0, renderText.length()));
      String rightStr = renderText.substring(Utils.clamp(to - this.currentOffset, 0, renderText.length()));
      int left = 4 + fontRenderer.func_78256_a(leftStr);
      int right = lineEnd - fontRenderer.func_78256_a(rightStr) - 1;
      MCVer.invertColors(renderer, right, height - 2, left, 2);
      if (this.blinkCursorTick / 6 % 2 == 0 && this.focused) {
        String beforeCursor = renderText.substring(0, this.cursorPos - this.currentOffset);
        int posX = 4 + fontRenderer.func_78256_a(beforeCursor);
        if (this.cursorPos == this.text.length()) {
          renderer.drawString(posX, posY, CURSOR_COLOR, "_", true);
        } else {
          renderer.drawRect(posX, posY - 1, 1, 1 + fontRenderer.field_78288_b, CURSOR_COLOR);
        } 
      } 
    } 
  }
  
  public boolean typeKey(ReadablePoint mousePosition, int keyCode, char keyChar, boolean ctrlDown, boolean shiftDown) {
    if (!this.focused)
      return false; 
    if (keyCode == 15) {
      Focusable other = shiftDown ? this.previous : this.next;
      if (other != null) {
        setFocused(false);
        other.setFocused(true);
      } 
      return true;
    } 
    if (keyCode == 28) {
      onEnter();
      return true;
    } 
    String textBefore = this.text;
    try {
      if (GuiScreen.func_146271_m()) {
        boolean bool1;
        switch (keyCode) {
          case 30:
            this.cursorPos = 0;
            this.selectionPos = this.text.length();
            bool1 = true;
            return bool1;
          case 46:
            MCVer.setClipboardString(getSelectedText());
            bool1 = true;
            return bool1;
          case 47:
            if (isEnabled())
              writeText(MCVer.getClipboardString()); 
            bool1 = true;
            return bool1;
          case 45:
            if (isEnabled())
              MCVer.setClipboardString(deleteSelectedText()); 
            bool1 = true;
            return bool1;
        } 
      } 
      boolean words = GuiScreen.func_146271_m();
      boolean select = GuiScreen.func_146272_n();
      switch (keyCode) {
        case 199:
          this.cursorPos = 0;
          break;
        case 207:
          this.cursorPos = this.text.length();
          break;
        case 203:
          if (this.cursorPos != 0) {
            if (words) {
              this.cursorPos -= getPreviousWordLength();
              break;
            } 
            this.cursorPos--;
          } 
          break;
        case 205:
          if (this.cursorPos != this.text.length()) {
            if (words) {
              this.cursorPos += getNextWordLength();
              break;
            } 
            this.cursorPos++;
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
          bool = true;
          return bool;
        case 211:
          if (isEnabled())
            if (getSelectedText().length() > 0) {
              deleteSelectedText();
            } else if (words) {
              deleteNextWord();
            } else {
              deleteNextChar();
            }  
          bool = true;
          return bool;
        default:
          if (isEnabled()) {
            if (keyChar == '\r')
              keyChar = '\n'; 
            writeChar(keyChar);
          } 
          bool = true;
          return bool;
      } 
      if (!select)
        this.selectionPos = this.cursorPos; 
      boolean bool = true;
      return bool;
    } finally {
      if (!textBefore.equals(this.text))
        onTextChanged(textBefore); 
    } 
  }
  
  public void tick() {
    this.blinkCursorTick++;
  }
  
  protected void onEnter() {
    if (this.onEnter != null)
      this.onEnter.run(); 
  }
  
  protected void onTextChanged(String from) {
    if (this.textChanged != null)
      this.textChanged.consume(from); 
  }
  
  protected void onFocusChanged(boolean focused) {
    if (this.focusChanged != null)
      this.focusChanged.consume(Boolean.valueOf(focused)); 
  }
  
  public T onEnter(Runnable onEnter) {
    this.onEnter = onEnter;
    return getThis();
  }
  
  public T onTextChanged(Consumer<String> textChanged) {
    this.textChanged = textChanged;
    return getThis();
  }
  
  public T onFocusChange(Consumer<Boolean> focusChanged) {
    this.focusChanged = focusChanged;
    return getThis();
  }
  
  public T setHint(String hint) {
    this.hint = hint;
    return getThis();
  }
  
  public T setI18nHint(String hint, Object... args) {
    return setHint(I18n.func_135052_a(hint, new Object[0]));
  }
  
  public ReadableColor getTextColor() {
    return this.textColorEnabled;
  }
  
  public T setTextColor(ReadableColor textColor) {
    this.textColorEnabled = textColor;
    return getThis();
  }
  
  public ReadableColor getTextColorDisabled() {
    return this.textColorDisabled;
  }
  
  public T setTextColorDisabled(ReadableColor textColorDisabled) {
    this.textColorDisabled = textColorDisabled;
    return getThis();
  }
  
  public AbstractGuiTextField() {}
}
