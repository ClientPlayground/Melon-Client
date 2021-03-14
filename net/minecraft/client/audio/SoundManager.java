package net.minecraft.client.audio;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import io.netty.util.internal.ThreadLocalRandom;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemException;
import paulscode.sound.SoundSystemLogger;
import paulscode.sound.Source;
import paulscode.sound.codecs.CodecJOrbis;
import paulscode.sound.libraries.LibraryLWJGLOpenAL;

public class SoundManager {
  private static final Marker LOG_MARKER = MarkerManager.getMarker("SOUNDS");
  
  private static final Logger logger = LogManager.getLogger();
  
  private final SoundHandler sndHandler;
  
  private final GameSettings options;
  
  private SoundSystemStarterThread sndSystem;
  
  private boolean loaded;
  
  private int playTime = 0;
  
  private final Map<String, ISound> playingSounds = (Map<String, ISound>)HashBiMap.create();
  
  private final Map<ISound, String> invPlayingSounds;
  
  private Map<ISound, SoundPoolEntry> playingSoundPoolEntries;
  
  private final Multimap<SoundCategory, String> categorySounds;
  
  private final List<ITickableSound> tickableSounds;
  
  private final Map<ISound, Integer> delayedSounds;
  
  private final Map<String, Integer> playingSoundsStopTime;
  
  public SoundManager(SoundHandler p_i45119_1_, GameSettings p_i45119_2_) {
    this.invPlayingSounds = (Map<ISound, String>)((BiMap)this.playingSounds).inverse();
    this.playingSoundPoolEntries = Maps.newHashMap();
    this.categorySounds = (Multimap<SoundCategory, String>)HashMultimap.create();
    this.tickableSounds = Lists.newArrayList();
    this.delayedSounds = Maps.newHashMap();
    this.playingSoundsStopTime = Maps.newHashMap();
    this.sndHandler = p_i45119_1_;
    this.options = p_i45119_2_;
    try {
      SoundSystemConfig.addLibrary(LibraryLWJGLOpenAL.class);
      SoundSystemConfig.setCodec("ogg", CodecJOrbis.class);
    } catch (SoundSystemException soundsystemexception) {
      logger.error(LOG_MARKER, "Error linking with the LibraryJavaSound plug-in", (Throwable)soundsystemexception);
    } 
  }
  
  public void reloadSoundSystem() {
    unloadSoundSystem();
    loadSoundSystem();
  }
  
  private synchronized void loadSoundSystem() {
    if (!this.loaded)
      try {
        (new Thread(new Runnable() {
              public void run() {
                SoundSystemConfig.setLogger(new SoundSystemLogger() {
                      public void message(String p_message_1_, int p_message_2_) {
                        if (!p_message_1_.isEmpty())
                          SoundManager.logger.info(p_message_1_); 
                      }
                      
                      public void importantMessage(String p_importantMessage_1_, int p_importantMessage_2_) {
                        if (!p_importantMessage_1_.isEmpty())
                          SoundManager.logger.warn(p_importantMessage_1_); 
                      }
                      
                      public void errorMessage(String p_errorMessage_1_, String p_errorMessage_2_, int p_errorMessage_3_) {
                        if (!p_errorMessage_2_.isEmpty()) {
                          SoundManager.logger.error("Error in class '" + p_errorMessage_1_ + "'");
                          SoundManager.logger.error(p_errorMessage_2_);
                        } 
                      }
                    },  );
                SoundManager.this.getClass();
                SoundManager.this.sndSystem = new SoundManager.SoundSystemStarterThread();
                SoundManager.this.loaded = true;
                SoundManager.this.sndSystem.setMasterVolume(SoundManager.this.options.getSoundLevel(SoundCategory.MASTER));
                SoundManager.logger.info(SoundManager.LOG_MARKER, "Sound engine started");
              }
            }"Sound Library Loader")).start();
      } catch (RuntimeException runtimeexception) {
        logger.error(LOG_MARKER, "Error starting SoundSystem. Turning off sounds & music", runtimeexception);
        this.options.setSoundLevel(SoundCategory.MASTER, 0.0F);
        this.options.saveOptions();
      }  
  }
  
  private float getSoundCategoryVolume(SoundCategory category) {
    return (category != null && category != SoundCategory.MASTER) ? this.options.getSoundLevel(category) : 1.0F;
  }
  
  public void setSoundCategoryVolume(SoundCategory category, float volume) {
    if (this.loaded)
      if (category == SoundCategory.MASTER) {
        this.sndSystem.setMasterVolume(volume);
      } else {
        for (String s : this.categorySounds.get(category)) {
          ISound isound = this.playingSounds.get(s);
          float f = getNormalizedVolume(isound, this.playingSoundPoolEntries.get(isound), category);
          if (f <= 0.0F) {
            stopSound(isound);
            continue;
          } 
          this.sndSystem.setVolume(s, f);
        } 
      }  
  }
  
  public void unloadSoundSystem() {
    if (this.loaded) {
      stopAllSounds();
      this.sndSystem.cleanup();
      this.loaded = false;
    } 
  }
  
  public void stopAllSounds() {
    if (this.loaded) {
      for (String s : this.playingSounds.keySet())
        this.sndSystem.stop(s); 
      this.playingSounds.clear();
      this.delayedSounds.clear();
      this.tickableSounds.clear();
      this.categorySounds.clear();
      this.playingSoundPoolEntries.clear();
      this.playingSoundsStopTime.clear();
    } 
  }
  
  public void updateAllSounds() {
    this.playTime++;
    for (ITickableSound itickablesound : this.tickableSounds) {
      itickablesound.update();
      if (itickablesound.isDonePlaying()) {
        stopSound(itickablesound);
        continue;
      } 
      String s = this.invPlayingSounds.get(itickablesound);
      this.sndSystem.setVolume(s, getNormalizedVolume(itickablesound, this.playingSoundPoolEntries.get(itickablesound), this.sndHandler.getSound(itickablesound.getSoundLocation()).getSoundCategory()));
      this.sndSystem.setPitch(s, getNormalizedPitch(itickablesound, this.playingSoundPoolEntries.get(itickablesound)));
      this.sndSystem.setPosition(s, itickablesound.getXPosF(), itickablesound.getYPosF(), itickablesound.getZPosF());
    } 
    Iterator<Map.Entry<String, ISound>> iterator = this.playingSounds.entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry<String, ISound> entry = iterator.next();
      String s1 = entry.getKey();
      ISound isound = entry.getValue();
      if (!this.sndSystem.playing(s1)) {
        int i = ((Integer)this.playingSoundsStopTime.get(s1)).intValue();
        if (i <= this.playTime) {
          int j = isound.getRepeatDelay();
          if (isound.canRepeat() && j > 0)
            this.delayedSounds.put(isound, Integer.valueOf(this.playTime + j)); 
          iterator.remove();
          logger.debug(LOG_MARKER, "Removed channel {} because it's not playing anymore", new Object[] { s1 });
          this.sndSystem.removeSource(s1);
          this.playingSoundsStopTime.remove(s1);
          this.playingSoundPoolEntries.remove(isound);
          try {
            this.categorySounds.remove(this.sndHandler.getSound(isound.getSoundLocation()).getSoundCategory(), s1);
          } catch (RuntimeException runtimeException) {}
          if (isound instanceof ITickableSound)
            this.tickableSounds.remove(isound); 
        } 
      } 
    } 
    Iterator<Map.Entry<ISound, Integer>> iterator1 = this.delayedSounds.entrySet().iterator();
    while (iterator1.hasNext()) {
      Map.Entry<ISound, Integer> entry1 = iterator1.next();
      if (this.playTime >= ((Integer)entry1.getValue()).intValue()) {
        ISound isound1 = entry1.getKey();
        if (isound1 instanceof ITickableSound)
          ((ITickableSound)isound1).update(); 
        playSound(isound1);
        iterator1.remove();
      } 
    } 
  }
  
  public boolean isSoundPlaying(ISound sound) {
    if (!this.loaded)
      return false; 
    String s = this.invPlayingSounds.get(sound);
    return (s == null) ? false : ((this.sndSystem.playing(s) || (this.playingSoundsStopTime.containsKey(s) && ((Integer)this.playingSoundsStopTime.get(s)).intValue() <= this.playTime)));
  }
  
  public void stopSound(ISound sound) {
    if (this.loaded) {
      String s = this.invPlayingSounds.get(sound);
      if (s != null)
        this.sndSystem.stop(s); 
    } 
  }
  
  public void playSound(ISound sound) {
    if (this.loaded)
      if (this.sndSystem.getMasterVolume() <= 0.0F) {
        logger.debug(LOG_MARKER, "Skipped playing soundEvent: {}, master volume was zero", new Object[] { sound.getSoundLocation() });
      } else {
        SoundEventAccessorComposite soundeventaccessorcomposite = this.sndHandler.getSound(sound.getSoundLocation());
        if (soundeventaccessorcomposite == null) {
          logger.warn(LOG_MARKER, "Unable to play unknown soundEvent: {}", new Object[] { sound.getSoundLocation() });
        } else {
          SoundPoolEntry soundpoolentry = soundeventaccessorcomposite.cloneEntry();
          if (soundpoolentry == SoundHandler.missing_sound) {
            logger.warn(LOG_MARKER, "Unable to play empty soundEvent: {}", new Object[] { soundeventaccessorcomposite.getSoundEventLocation() });
          } else {
            float f = sound.getVolume();
            float f1 = 16.0F;
            if (f > 1.0F)
              f1 *= f; 
            SoundCategory soundcategory = soundeventaccessorcomposite.getSoundCategory();
            float f2 = getNormalizedVolume(sound, soundpoolentry, soundcategory);
            double d0 = getNormalizedPitch(sound, soundpoolentry);
            ResourceLocation resourcelocation = soundpoolentry.getSoundPoolEntryLocation();
            if (f2 == 0.0F) {
              logger.debug(LOG_MARKER, "Skipped playing sound {}, volume was zero.", new Object[] { resourcelocation });
            } else {
              boolean flag = (sound.canRepeat() && sound.getRepeatDelay() == 0);
              String s = MathHelper.getRandomUuid((Random)ThreadLocalRandom.current()).toString();
              if (soundpoolentry.isStreamingSound()) {
                this.sndSystem.newStreamingSource(false, s, getURLForSoundResource(resourcelocation), resourcelocation.toString(), flag, sound.getXPosF(), sound.getYPosF(), sound.getZPosF(), sound.getAttenuationType().getTypeInt(), f1);
              } else {
                this.sndSystem.newSource(false, s, getURLForSoundResource(resourcelocation), resourcelocation.toString(), flag, sound.getXPosF(), sound.getYPosF(), sound.getZPosF(), sound.getAttenuationType().getTypeInt(), f1);
              } 
              logger.debug(LOG_MARKER, "Playing sound {} for event {} as channel {}", new Object[] { soundpoolentry.getSoundPoolEntryLocation(), soundeventaccessorcomposite.getSoundEventLocation(), s });
              this.sndSystem.setPitch(s, (float)d0);
              this.sndSystem.setVolume(s, f2);
              this.sndSystem.play(s);
              this.playingSoundsStopTime.put(s, Integer.valueOf(this.playTime + 20));
              this.playingSounds.put(s, sound);
              this.playingSoundPoolEntries.put(sound, soundpoolentry);
              if (soundcategory != SoundCategory.MASTER)
                this.categorySounds.put(soundcategory, s); 
              if (sound instanceof ITickableSound)
                this.tickableSounds.add((ITickableSound)sound); 
            } 
          } 
        } 
      }  
  }
  
  private float getNormalizedPitch(ISound sound, SoundPoolEntry entry) {
    return (float)MathHelper.clamp_double(sound.getPitch() * entry.getPitch(), 0.5D, 2.0D);
  }
  
  private float getNormalizedVolume(ISound sound, SoundPoolEntry entry, SoundCategory category) {
    return (float)MathHelper.clamp_double(sound.getVolume() * entry.getVolume(), 0.0D, 1.0D) * getSoundCategoryVolume(category);
  }
  
  public void pauseAllSounds() {
    for (String s : this.playingSounds.keySet()) {
      logger.debug(LOG_MARKER, "Pausing channel {}", new Object[] { s });
      this.sndSystem.pause(s);
    } 
  }
  
  public void resumeAllSounds() {
    for (String s : this.playingSounds.keySet()) {
      logger.debug(LOG_MARKER, "Resuming channel {}", new Object[] { s });
      this.sndSystem.play(s);
    } 
  }
  
  public void playDelayedSound(ISound sound, int delay) {
    this.delayedSounds.put(sound, Integer.valueOf(this.playTime + delay));
  }
  
  private static URL getURLForSoundResource(final ResourceLocation p_148612_0_) {
    String s = String.format("%s:%s:%s", new Object[] { "mcsounddomain", p_148612_0_.getResourceDomain(), p_148612_0_.getResourcePath() });
    URLStreamHandler urlstreamhandler = new URLStreamHandler() {
        protected URLConnection openConnection(URL p_openConnection_1_) {
          return new URLConnection(p_openConnection_1_) {
              public void connect() throws IOException {}
              
              public InputStream getInputStream() throws IOException {
                return Minecraft.getMinecraft().getResourceManager().getResource(p_148612_0_).getInputStream();
              }
            };
        }
      };
    try {
      return new URL((URL)null, s, urlstreamhandler);
    } catch (MalformedURLException var4) {
      throw new Error("TODO: Sanely handle url exception! :D");
    } 
  }
  
  public void setListener(EntityPlayer player, float p_148615_2_) {
    if (this.loaded && player != null) {
      float f = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * p_148615_2_;
      float f1 = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * p_148615_2_;
      double d0 = player.prevPosX + (player.posX - player.prevPosX) * p_148615_2_;
      double d1 = player.prevPosY + (player.posY - player.prevPosY) * p_148615_2_ + player.getEyeHeight();
      double d2 = player.prevPosZ + (player.posZ - player.prevPosZ) * p_148615_2_;
      float f2 = MathHelper.cos((f1 + 90.0F) * 0.017453292F);
      float f3 = MathHelper.sin((f1 + 90.0F) * 0.017453292F);
      float f4 = MathHelper.cos(-f * 0.017453292F);
      float f5 = MathHelper.sin(-f * 0.017453292F);
      float f6 = MathHelper.cos((-f + 90.0F) * 0.017453292F);
      float f7 = MathHelper.sin((-f + 90.0F) * 0.017453292F);
      float f8 = f2 * f4;
      float f9 = f3 * f4;
      float f10 = f2 * f6;
      float f11 = f3 * f6;
      this.sndSystem.setListenerPosition((float)d0, (float)d1, (float)d2);
      this.sndSystem.setListenerOrientation(f8, f5, f9, f10, f7, f11);
    } 
  }
  
  class SoundSystemStarterThread extends SoundSystem {
    private SoundSystemStarterThread() {}
    
    public boolean playing(String p_playing_1_) {
      synchronized (SoundSystemConfig.THREAD_SYNC) {
        if (this.soundLibrary == null)
          return false; 
        Source source = (Source)this.soundLibrary.getSources().get(p_playing_1_);
        return (source == null) ? false : ((source.playing() || source.paused() || source.preLoad));
      } 
    }
  }
}
