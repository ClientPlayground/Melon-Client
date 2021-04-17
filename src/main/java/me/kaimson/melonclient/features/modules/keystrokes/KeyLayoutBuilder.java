package me.kaimson.melonclient.features.modules.keystrokes;

import com.google.common.collect.*;
import me.kaimson.melonclient.features.modules.keystrokes.keys.*;
import java.util.*;

public class KeyLayoutBuilder
{
    private float width;
    private float height;
    private int gapSize;
    public final List<Rows> rows;
    
    public KeyLayoutBuilder() {
        this.rows = Lists.newArrayList();
    }
    
    public KeyLayoutBuilder addRow(final Key... keys) {
        final double keyWidth = (this.width - this.gapSize * (keys.length - 1)) / keys.length;
        final double height = Arrays.stream(keys).mapToDouble(Key::getHeight).max().orElse(0.0);
        this.rows.add(new Rows(keys, keyWidth, height));
        return this;
    }
    
    public KeyLayoutBuilder build() {
        this.height = (int)(this.rows.stream().mapToDouble(Rows::getHeight).sum() + this.gapSize * (this.rows.size() - 1));
        return this;
    }
    
    public KeyLayoutBuilder setWidth(final float width) {
        this.width = width;
        return this;
    }
    
    public float getWidth() {
        return this.width;
    }
    
    public void setHeight(final float height) {
        this.height = height;
    }
    
    public float getHeight() {
        return this.height;
    }
    
    public KeyLayoutBuilder setGapSize(final int gapSize) {
        this.gapSize = gapSize;
        return this;
    }
    
    public int getGapSize() {
        return this.gapSize;
    }
}
