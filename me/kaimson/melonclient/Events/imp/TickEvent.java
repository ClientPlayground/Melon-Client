package me.kaimson.melonclient.Events.imp;

import me.kaimson.melonclient.Events.Cancellable;

public class TickEvent extends Cancellable {
  public static class ClientTick extends TickEvent {
    public TickEvent.Phase phase;
    
    public ClientTick(TickEvent.Phase phase) {
      this.phase = phase;
    }
  }
  
  public static class RenderTick extends TickEvent {
    public TickEvent.Phase phase;
    
    public float renderTickTime;
    
    public RenderTick(TickEvent.Phase phase, float renderTickTime) {
      this.phase = phase;
      this.renderTickTime = renderTickTime;
    }
    
    public static class Overlay extends RenderTick {
      public Overlay(TickEvent.Phase phase, float renderTickTime) {
        super(phase, renderTickTime);
      }
      
      public static class Crosshair extends TickEvent.RenderTick {
        public Crosshair(TickEvent.Phase phase, float renderTickTime) {
          super(phase, renderTickTime);
        }
      }
      
      public static class Stats extends TickEvent.RenderTick {
        public Stat stat;
        
        public Stats(TickEvent.Phase phase, float renderTickTime, Stat stat) {
          super(phase, renderTickTime);
          this.stat = stat;
        }
        
        public enum Stat {
          HEALTH, ARMOR, HUNGER;
        }
      }
      
      public static class Text extends TickEvent.RenderTick {
        public String text;
        
        public Text(TickEvent.Phase phase, float renderTickTime, String text) {
          super(phase, renderTickTime);
          this.text = text;
        }
        
        public static class ActionBar extends Text {
          public ActionBar(TickEvent.Phase phase, float renderTickTime, String text) {
            super(phase, renderTickTime, text);
          }
        }
      }
    }
  }
  
  public enum Phase {
    START, END;
  }
}
