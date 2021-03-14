package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data;

import com.google.gson.reflect.TypeToken;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.item.Item;
import com.replaymod.replaystudio.us.myles.ViaVersion.util.GsonUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Map;
import lombok.NonNull;

public class RecipeData {
  public static Map<String, Recipe> recipes;
  
  public static void init() {
    InputStream stream = MappingData.class.getClassLoader().getResourceAsStream("assets/viaversion/data/itemrecipes1_12_2to1_13.json");
    InputStreamReader reader = new InputStreamReader(stream);
    try {
      recipes = (Map<String, Recipe>)GsonUtil.getGson().fromJson(reader, (new TypeToken<Map<String, Recipe>>() {
          
          }).getType());
    } finally {
      try {
        reader.close();
      } catch (IOException iOException) {}
    } 
  }
  
  public static class Recipe {
    @NonNull
    private String type;
    
    private String group;
    
    private int width;
    
    private int height;
    
    private float experience;
    
    private int cookingTime;
    
    private Item[] ingredient;
    
    private Item[][] ingredients;
    
    private Item result;
    
    public Recipe(@NonNull String type) {
      if (type == null)
        throw new NullPointerException("type is marked @NonNull but is null"); 
      this.type = type;
    }
    
    public void setType(@NonNull String type) {
      if (type == null)
        throw new NullPointerException("type is marked @NonNull but is null"); 
      this.type = type;
    }
    
    public void setGroup(String group) {
      this.group = group;
    }
    
    public void setWidth(int width) {
      this.width = width;
    }
    
    public void setHeight(int height) {
      this.height = height;
    }
    
    public void setExperience(float experience) {
      this.experience = experience;
    }
    
    public void setCookingTime(int cookingTime) {
      this.cookingTime = cookingTime;
    }
    
    public void setIngredient(Item[] ingredient) {
      this.ingredient = ingredient;
    }
    
    public void setIngredients(Item[][] ingredients) {
      this.ingredients = ingredients;
    }
    
    public void setResult(Item result) {
      this.result = result;
    }
    
    public boolean equals(Object o) {
      if (o == this)
        return true; 
      if (!(o instanceof Recipe))
        return false; 
      Recipe other = (Recipe)o;
      if (!other.canEqual(this))
        return false; 
      Object this$type = getType(), other$type = other.getType();
      if ((this$type == null) ? (other$type != null) : !this$type.equals(other$type))
        return false; 
      Object this$group = getGroup(), other$group = other.getGroup();
      if ((this$group == null) ? (other$group != null) : !this$group.equals(other$group))
        return false; 
      if (getWidth() != other.getWidth())
        return false; 
      if (getHeight() != other.getHeight())
        return false; 
      if (Float.compare(getExperience(), other.getExperience()) != 0)
        return false; 
      if (getCookingTime() != other.getCookingTime())
        return false; 
      if (!Arrays.deepEquals((Object[])getIngredient(), (Object[])other.getIngredient()))
        return false; 
      if (!Arrays.deepEquals((Object[])getIngredients(), (Object[])other.getIngredients()))
        return false; 
      Object this$result = getResult(), other$result = other.getResult();
      return !((this$result == null) ? (other$result != null) : !this$result.equals(other$result));
    }
    
    protected boolean canEqual(Object other) {
      return other instanceof Recipe;
    }
    
    public int hashCode() {
      int PRIME = 59;
      result = 1;
      Object $type = getType();
      result = result * 59 + (($type == null) ? 43 : $type.hashCode());
      Object $group = getGroup();
      result = result * 59 + (($group == null) ? 43 : $group.hashCode());
      result = result * 59 + getWidth();
      result = result * 59 + getHeight();
      result = result * 59 + Float.floatToIntBits(getExperience());
      result = result * 59 + getCookingTime();
      result = result * 59 + Arrays.deepHashCode((Object[])getIngredient());
      result = result * 59 + Arrays.deepHashCode((Object[])getIngredients());
      Object $result = getResult();
      return result * 59 + (($result == null) ? 43 : $result.hashCode());
    }
    
    public String toString() {
      return "RecipeData.Recipe(type=" + getType() + ", group=" + getGroup() + ", width=" + getWidth() + ", height=" + getHeight() + ", experience=" + getExperience() + ", cookingTime=" + getCookingTime() + ", ingredient=" + Arrays.deepToString((Object[])getIngredient()) + ", ingredients=" + Arrays.deepToString((Object[])getIngredients()) + ", result=" + getResult() + ")";
    }
    
    @NonNull
    public String getType() {
      return this.type;
    }
    
    public String getGroup() {
      return this.group;
    }
    
    public int getWidth() {
      return this.width;
    }
    
    public int getHeight() {
      return this.height;
    }
    
    public float getExperience() {
      return this.experience;
    }
    
    public int getCookingTime() {
      return this.cookingTime;
    }
    
    public Item[] getIngredient() {
      return this.ingredient;
    }
    
    public Item[][] getIngredients() {
      return this.ingredients;
    }
    
    public Item getResult() {
      return this.result;
    }
  }
}
