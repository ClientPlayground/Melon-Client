package com.replaymod.replaystudio.launcher;

import com.replaymod.replaystudio.util.Utils;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.cli.CommandLine;

public class ReverseLauncher {
  public void launch(CommandLine cmd) throws Exception {
    ZipFile file = new ZipFile(cmd.getArgs()[0]);
    ZipEntry entry = file.getEntry("recording.tmcpr");
    if (entry == null)
      throw new IOException("Input file is not a valid replay file."); 
    long size = entry.getSize();
    if (size == -1L)
      throw new IOException("Uncompressed size of recording.tmcpr not set."); 
    InputStream from = file.getInputStream(entry);
    RandomAccessFile to = new RandomAccessFile(cmd.getArgs()[1], "rw");
    to.setLength(size);
    long pos = size;
    byte[] buffer = new byte[8192];
    long lastUpdate = -1L;
    while (true) {
      long pct = 100L - pos * 100L / size;
      if (lastUpdate != pct) {
        System.out.print("Reversing " + size + " bytes... " + pct + "%\r");
        lastUpdate = pct;
      } 
      int next = Utils.readInt(from);
      int length = Utils.readInt(from);
      if (next == -1 || length == -1)
        break; 
      if (length + 8 > buffer.length)
        buffer = new byte[length + 8]; 
      buffer[0] = (byte)(next >>> 24 & 0xFF);
      buffer[1] = (byte)(next >>> 16 & 0xFF);
      buffer[2] = (byte)(next >>> 8 & 0xFF);
      buffer[3] = (byte)(next & 0xFF);
      buffer[4] = (byte)(length >>> 24 & 0xFF);
      buffer[5] = (byte)(length >>> 16 & 0xFF);
      buffer[6] = (byte)(length >>> 8 & 0xFF);
      buffer[7] = (byte)(length & 0xFF);
      int nRead = 0;
      while (nRead < length)
        nRead += from.read(buffer, 8 + nRead, length - nRead); 
      pos -= (length + 8);
      to.seek(pos);
      to.write(buffer, 0, length + 8);
    } 
    from.close();
    to.close();
    System.out.println("\nDone!");
  }
}
