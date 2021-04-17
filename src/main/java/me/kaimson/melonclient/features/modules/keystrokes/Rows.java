package me.kaimson.melonclient.features.modules.keystrokes;

import me.kaimson.melonclient.features.modules.keystrokes.keys.*;

public class Rows
{
    private final Key[] keys;
    private double width;
    private double height;
    
    public Rows(final Key[] keys, final double width, final double height) {
        this.keys = keys;
        this.width = width;
        this.height = height;
    }
    
    public Key[] getKeys() {
        return this.keys;
    }
    
    public double getWidth() {
        return this.width;
    }
    
    public double getHeight() {
        return this.height;
    }
    
    public void setWidth(final double width) {
        this.width = width;
    }
    
    public void setHeight(final double height) {
        this.height = height;
    }
}
