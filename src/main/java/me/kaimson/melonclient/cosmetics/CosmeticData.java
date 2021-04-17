package me.kaimson.melonclient.cosmetics;

import java.util.*;
import net.minecraft.util.*;

public class CosmeticData
{
    private final List<CosmeticManager.CosmeticList> cosmetics;
    private final String data;
    private final ResourceLocation capeTexture;
    
    public List<CosmeticManager.CosmeticList> getCosmetics() {
        return this.cosmetics;
    }
    
    public String getData() {
        return this.data;
    }
    
    public ResourceLocation getCapeTexture() {
        return this.capeTexture;
    }
    
    public CosmeticData(final List<CosmeticManager.CosmeticList> cosmetics, final String data, final ResourceLocation capeTexture) {
        this.cosmetics = cosmetics;
        this.data = data;
        this.capeTexture = capeTexture;
    }
}
