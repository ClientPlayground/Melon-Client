package net.minecraft.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public interface IChatComponent extends Iterable<IChatComponent> {
  IChatComponent setChatStyle(ChatStyle paramChatStyle);
  
  ChatStyle getChatStyle();
  
  IChatComponent appendText(String paramString);
  
  IChatComponent appendSibling(IChatComponent paramIChatComponent);
  
  String getUnformattedTextForChat();
  
  String getUnformattedText();
  
  String getFormattedText();
  
  List<IChatComponent> getSiblings();
  
  IChatComponent createCopy();
  
  public static class Serializer implements JsonDeserializer<IChatComponent>, JsonSerializer<IChatComponent> {
    private static final Gson GSON;
    
    public IChatComponent deserialize(JsonElement p_deserialize_1_, Type p_deserialize_2_, JsonDeserializationContext p_deserialize_3_) throws JsonParseException {
      IChatComponent ichatcomponent;
      if (p_deserialize_1_.isJsonPrimitive())
        return new ChatComponentText(p_deserialize_1_.getAsString()); 
      if (!p_deserialize_1_.isJsonObject()) {
        if (p_deserialize_1_.isJsonArray()) {
          JsonArray jsonarray1 = p_deserialize_1_.getAsJsonArray();
          IChatComponent ichatcomponent1 = null;
          for (JsonElement jsonelement : jsonarray1) {
            IChatComponent ichatcomponent2 = deserialize(jsonelement, jsonelement.getClass(), p_deserialize_3_);
            if (ichatcomponent1 == null) {
              ichatcomponent1 = ichatcomponent2;
              continue;
            } 
            ichatcomponent1.appendSibling(ichatcomponent2);
          } 
          return ichatcomponent1;
        } 
        throw new JsonParseException("Don't know how to turn " + p_deserialize_1_.toString() + " into a Component");
      } 
      JsonObject jsonobject = p_deserialize_1_.getAsJsonObject();
      if (jsonobject.has("text")) {
        ichatcomponent = new ChatComponentText(jsonobject.get("text").getAsString());
      } else if (jsonobject.has("translate")) {
        String s = jsonobject.get("translate").getAsString();
        if (jsonobject.has("with")) {
          JsonArray jsonarray = jsonobject.getAsJsonArray("with");
          Object[] aobject = new Object[jsonarray.size()];
          for (int i = 0; i < aobject.length; i++) {
            aobject[i] = deserialize(jsonarray.get(i), p_deserialize_2_, p_deserialize_3_);
            if (aobject[i] instanceof ChatComponentText) {
              ChatComponentText chatcomponenttext = (ChatComponentText)aobject[i];
              if (chatcomponenttext.getChatStyle().isEmpty() && chatcomponenttext.getSiblings().isEmpty())
                aobject[i] = chatcomponenttext.getChatComponentText_TextValue(); 
            } 
          } 
          ichatcomponent = new ChatComponentTranslation(s, aobject);
        } else {
          ichatcomponent = new ChatComponentTranslation(s, new Object[0]);
        } 
      } else if (jsonobject.has("score")) {
        JsonObject jsonobject1 = jsonobject.getAsJsonObject("score");
        if (!jsonobject1.has("name") || !jsonobject1.has("objective"))
          throw new JsonParseException("A score component needs a least a name and an objective"); 
        ichatcomponent = new ChatComponentScore(JsonUtils.getString(jsonobject1, "name"), JsonUtils.getString(jsonobject1, "objective"));
        if (jsonobject1.has("value"))
          ((ChatComponentScore)ichatcomponent).setValue(JsonUtils.getString(jsonobject1, "value")); 
      } else {
        if (!jsonobject.has("selector"))
          throw new JsonParseException("Don't know how to turn " + p_deserialize_1_.toString() + " into a Component"); 
        ichatcomponent = new ChatComponentSelector(JsonUtils.getString(jsonobject, "selector"));
      } 
      if (jsonobject.has("extra")) {
        JsonArray jsonarray2 = jsonobject.getAsJsonArray("extra");
        if (jsonarray2.size() <= 0)
          throw new JsonParseException("Unexpected empty array of components"); 
        for (int j = 0; j < jsonarray2.size(); j++)
          ichatcomponent.appendSibling(deserialize(jsonarray2.get(j), p_deserialize_2_, p_deserialize_3_)); 
      } 
      ichatcomponent.setChatStyle((ChatStyle)p_deserialize_3_.deserialize(p_deserialize_1_, ChatStyle.class));
      return ichatcomponent;
    }
    
    private void serializeChatStyle(ChatStyle style, JsonObject object, JsonSerializationContext ctx) {
      JsonElement jsonelement = ctx.serialize(style);
      if (jsonelement.isJsonObject()) {
        JsonObject jsonobject = (JsonObject)jsonelement;
        for (Map.Entry<String, JsonElement> entry : (Iterable<Map.Entry<String, JsonElement>>)jsonobject.entrySet())
          object.add(entry.getKey(), entry.getValue()); 
      } 
    }
    
    public JsonElement serialize(IChatComponent p_serialize_1_, Type p_serialize_2_, JsonSerializationContext p_serialize_3_) {
      if (p_serialize_1_ instanceof ChatComponentText && p_serialize_1_.getChatStyle().isEmpty() && p_serialize_1_.getSiblings().isEmpty())
        return (JsonElement)new JsonPrimitive(((ChatComponentText)p_serialize_1_).getChatComponentText_TextValue()); 
      JsonObject jsonobject = new JsonObject();
      if (!p_serialize_1_.getChatStyle().isEmpty())
        serializeChatStyle(p_serialize_1_.getChatStyle(), jsonobject, p_serialize_3_); 
      if (!p_serialize_1_.getSiblings().isEmpty()) {
        JsonArray jsonarray = new JsonArray();
        for (IChatComponent ichatcomponent : p_serialize_1_.getSiblings())
          jsonarray.add(serialize(ichatcomponent, ichatcomponent.getClass(), p_serialize_3_)); 
        jsonobject.add("extra", (JsonElement)jsonarray);
      } 
      if (p_serialize_1_ instanceof ChatComponentText) {
        jsonobject.addProperty("text", ((ChatComponentText)p_serialize_1_).getChatComponentText_TextValue());
      } else if (p_serialize_1_ instanceof ChatComponentTranslation) {
        ChatComponentTranslation chatcomponenttranslation = (ChatComponentTranslation)p_serialize_1_;
        jsonobject.addProperty("translate", chatcomponenttranslation.getKey());
        if (chatcomponenttranslation.getFormatArgs() != null && (chatcomponenttranslation.getFormatArgs()).length > 0) {
          JsonArray jsonarray1 = new JsonArray();
          for (Object object : chatcomponenttranslation.getFormatArgs()) {
            if (object instanceof IChatComponent) {
              jsonarray1.add(serialize((IChatComponent)object, object.getClass(), p_serialize_3_));
            } else {
              jsonarray1.add((JsonElement)new JsonPrimitive(String.valueOf(object)));
            } 
          } 
          jsonobject.add("with", (JsonElement)jsonarray1);
        } 
      } else if (p_serialize_1_ instanceof ChatComponentScore) {
        ChatComponentScore chatcomponentscore = (ChatComponentScore)p_serialize_1_;
        JsonObject jsonobject1 = new JsonObject();
        jsonobject1.addProperty("name", chatcomponentscore.getName());
        jsonobject1.addProperty("objective", chatcomponentscore.getObjective());
        jsonobject1.addProperty("value", chatcomponentscore.getUnformattedTextForChat());
        jsonobject.add("score", (JsonElement)jsonobject1);
      } else {
        if (!(p_serialize_1_ instanceof ChatComponentSelector))
          throw new IllegalArgumentException("Don't know how to serialize " + p_serialize_1_ + " as a Component"); 
        ChatComponentSelector chatcomponentselector = (ChatComponentSelector)p_serialize_1_;
        jsonobject.addProperty("selector", chatcomponentselector.getSelector());
      } 
      return (JsonElement)jsonobject;
    }
    
    public static String componentToJson(IChatComponent component) {
      return GSON.toJson(component);
    }
    
    public static IChatComponent jsonToComponent(String json) {
      return (IChatComponent)GSON.fromJson(json, IChatComponent.class);
    }
    
    static {
      GsonBuilder gsonbuilder = new GsonBuilder();
      gsonbuilder.registerTypeHierarchyAdapter(IChatComponent.class, new Serializer());
      gsonbuilder.registerTypeHierarchyAdapter(ChatStyle.class, new ChatStyle.Serializer());
      gsonbuilder.registerTypeAdapterFactory(new EnumTypeAdapterFactory());
      GSON = gsonbuilder.create();
    }
  }
}
