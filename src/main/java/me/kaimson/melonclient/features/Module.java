package me.kaimson.melonclient.features;

import java.util.*;
import me.kaimson.melonclient.utils.*;
import com.google.common.collect.*;
import net.minecraft.util.*;
import net.minecraft.client.*;
import me.kaimson.melonclient.features.modules.utils.*;

public abstract class Module
{
    public final String displayName;
    private final String iconName;
    private final int textureIndex;
    public final Set<Setting> settings;
    private Boolean textureExists;
    
    public Module(final String displayName) {
        this(displayName, -1, true);
    }
    
    public Module(final String displayName, final int textureIndex) {
        this(displayName, textureIndex, true);
    }
    
    public Module(final String displayName, final int textureIndex, final boolean autoadd) {
        this.settings = Sets.newLinkedHashSet();
        this.displayName = displayName;
        this.iconName = displayName.replace(" ", "").toLowerCase();
        this.textureIndex = textureIndex;
        if (autoadd) {
            ModuleManager.INSTANCE.modules.add(this);
        }
    }
    
    public String getKey() {
        return this.displayName.replace(" ", "").toUpperCase();
    }
    
    public ResourceLocation getIcon() {
        return new ResourceLocation("melonclient/icons/modules/" + this.iconName + ".png");
    }
    
    public boolean hasIcon() {
        if (this.textureExists == null) {
            this.textureExists = false;
            try {
                Minecraft.getMinecraft().getResourceManager().getAllResources(this.getIcon());
                this.textureExists = true;
            }
            catch (Exception ex) {}
        }
        return this.iconName != null && !this.iconName.isEmpty() && this.getIcon() != null && this.textureExists != null && this.textureExists;
    }
    
    public boolean isRender() {
        return this instanceof IModuleRenderer;
    }
    
    public int getTextureIndex() {
        return this.textureIndex;
    }
}
