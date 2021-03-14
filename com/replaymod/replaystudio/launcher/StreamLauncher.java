package com.replaymod.replaystudio.launcher;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.replaymod.replaystudio.PacketData;
import com.replaymod.replaystudio.Studio;
import com.replaymod.replaystudio.filter.StreamFilter;
import com.replaymod.replaystudio.io.ReplayOutputStream;
import com.replaymod.replaystudio.protocol.PacketTypeRegistry;
import com.replaymod.replaystudio.replay.ReplayMetaData;
import com.replaymod.replaystudio.replay.ZipReplayFile;
import com.replaymod.replaystudio.stream.PacketStream;
import com.replaymod.replaystudio.studio.ReplayStudio;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.ProtocolVersion;
import com.replaymod.replaystudio.us.myles.ViaVersion.packets.State;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.cli.CommandLine;

public class StreamLauncher {
  private final Studio studio = (Studio)new ReplayStudio();
  
  public void launch(CommandLine cmd) throws IOException {
    String[] instructions;
    ReplayOutputStream out;
    List<PacketStream.FilterInfo> filters = new ArrayList<>();
    if (cmd.hasOption('q')) {
      instructions = new String[] { "squash" };
    } else {
      instructions = cmd.getOptionValue('s').split(",");
    } 
    for (String instruction : instructions) {
      long l1, end;
      JsonObject config;
      if (instruction.charAt(instruction.length() - 1) == ')') {
        int index = instruction.indexOf('(');
        String time = instruction.substring(index + 1, instruction.length() - 1);
        instruction = instruction.substring(0, index);
        l1 = timeStampToMillis(time.split("-", 2)[0]);
        end = timeStampToMillis(time.split("-", 2)[1]);
      } else {
        l1 = end = -1L;
      } 
      if (instruction.charAt(instruction.length() - 1) == ']') {
        int index = instruction.indexOf('[');
        String str = instruction.substring(index + 1, instruction.length() - 1);
        instruction = instruction.substring(0, index);
        config = (new JsonParser()).parse("{" + str + "}").getAsJsonObject();
      } else {
        config = new JsonObject();
      } 
      StreamFilter filter = this.studio.loadStreamFilter(instruction);
      if (filter == null)
        throw new IllegalStateException("Filter not found: " + instruction); 
      filter.init(this.studio, config);
      filters.add(new PacketStream.FilterInfo(filter, l1, end));
    } 
    String input = cmd.getArgs()[0];
    String output = cmd.getArgs()[1];
    long start = System.nanoTime();
    System.out.println("Generating " + ("x".equals(output) ? 0 : 1) + " replay via 1 stream from 1 input applying " + filters.size() + " filter(s)");
    ZipReplayFile zipReplayFile = new ZipReplayFile(this.studio, new File(input));
    ReplayMetaData meta = zipReplayFile.getMetaData();
    ProtocolVersion inputVersion = meta.getProtocolVersion();
    if (!"x".equals(output)) {
      OutputStream buffOut = new BufferedOutputStream(new FileOutputStream(output));
      out = new ReplayOutputStream(inputVersion, buffOut, null);
    } else {
      out = null;
    } 
    PacketStream stream = zipReplayFile.getPacketData(PacketTypeRegistry.get(inputVersion, State.LOGIN)).asPacketStream();
    stream.start();
    stream.addFilter(new ProgressFilter(meta.getDuration()));
    for (PacketStream.FilterInfo info : filters)
      stream.addFilter(info.getFilter(), info.getFrom(), info.getTo()); 
    System.out.println("Built pipeline: " + stream);
    if (out != null) {
      PacketData data;
      while ((data = stream.next()) != null)
        out.write(data); 
      for (PacketData d : stream.end())
        out.write(d); 
      out.close();
    } else {
      while (stream.next() != null);
      stream.end();
    } 
    System.in.close();
    System.out.println("Done after " + (System.nanoTime() - start) + "ns");
  }
  
  private long timeStampToMillis(String string) {
    if (string.length() == 0)
      return -1L; 
    try {
      return Long.parseLong(string);
    } catch (NumberFormatException e) {
      long time = 0L;
      int hIndex = string.indexOf('h');
      if (hIndex != -1) {
        time += (3600000 * Integer.parseInt(string.substring(0, hIndex)));
        if (string.length() - 1 > hIndex)
          string = string.substring(hIndex + 1); 
      } 
      int mIndex = string.indexOf('m');
      if (mIndex != -1) {
        time += (60000 * Integer.parseInt(string.substring(0, mIndex)));
        if (string.length() - 1 > mIndex)
          string = string.substring(mIndex + 1); 
      } 
      int sIndex = string.indexOf('s');
      if (sIndex != -1) {
        time += (1000 * Integer.parseInt(string.substring(0, sIndex)));
        if (string.length() - 1 > sIndex)
          string = string.substring(sIndex + 1); 
      } 
      int msIndex = string.indexOf("ms");
      if (msIndex != -1)
        time += Integer.parseInt(string.substring(0, msIndex)); 
      return time;
    } 
  }
  
  private static class ProgressFilter implements StreamFilter {
    private final long total;
    
    private int lastUpdate;
    
    public ProgressFilter(long total) {
      this.total = total;
    }
    
    public String getName() {
      return "progress";
    }
    
    public void init(Studio studio, JsonObject config) {}
    
    public void onStart(PacketStream stream) {
      this.lastUpdate = -1;
    }
    
    public boolean onPacket(PacketStream stream, PacketData data) {
      int pct = (int)(data.getTime() * 100L / this.total);
      if (pct > this.lastUpdate) {
        this.lastUpdate = pct;
        System.out.print("Processing... " + pct + "%\r");
      } 
      return true;
    }
    
    public void onEnd(PacketStream stream, long timestamp) {
      System.out.println();
    }
  }
}
