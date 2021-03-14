package me.kaimson.melonclient.config;

public enum Language {
  ENGLISH("langs/en_us");
  
  private final String path;
  
  public String getPath() {
    return this.path;
  }
  
  Language(String path) {
    this.path = path;
  }
  
  public static Language fromPath(String path) {
    for (Language language : values()) {
      if (language.path.equals(path))
        return language; 
    } 
    return null;
  }
}
