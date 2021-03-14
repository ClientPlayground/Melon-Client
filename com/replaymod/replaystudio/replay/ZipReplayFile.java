package com.replaymod.replaystudio.replay;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.replaymod.replaystudio.Studio;
import com.replaymod.replaystudio.util.Utils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipReplayFile extends AbstractReplayFile {
  private static final String ENTRY_RECORDING_HASH = "recording.tmcpr.crc32";
  
  private final File input;
  
  private final File output;
  
  private final File cache;
  
  private final File tmpFiles;
  
  private final File changedFiles;
  
  private final File removedFiles;
  
  private final File sourceFile;
  
  private CRC32 recordingCrc;
  
  private boolean shouldSaveInputFile;
  
  private final Map<String, OutputStream> outputStreams = new HashMap<>();
  
  private final Map<String, File> changedEntries = new HashMap<>();
  
  private final Set<String> removedEntries = new HashSet<>();
  
  private ZipFile zipFile;
  
  public ZipReplayFile(Studio studio, File file) throws IOException {
    this(studio, file, file);
  }
  
  public ZipReplayFile(Studio studio, File input, File output) throws IOException {
    this(studio, input, output, new File(output.getParentFile(), output.getName() + ".cache"));
  }
  
  public ZipReplayFile(Studio studio, File input, File output, File cache) throws IOException {
    super(studio);
    this.tmpFiles = new File(output.getParentFile(), output.getName() + ".tmp");
    this.changedFiles = new File(this.tmpFiles, "changed");
    this.removedFiles = new File(this.tmpFiles, "removed");
    this.sourceFile = new File(this.tmpFiles, "source");
    if (input != null && input.exists()) {
      this.shouldSaveInputFile = true;
    } else if (input == null && this.sourceFile.exists()) {
      input = new File(new String(Files.readAllBytes(this.sourceFile.toPath()), Charsets.UTF_8));
      if (!input.exists())
        throw new IOException("Recovered source file no longer exists."); 
    } 
    this.output = output;
    this.input = input;
    this.cache = cache;
    if (input != null && input.exists())
      this.zipFile = new ZipFile(input); 
    if (this.changedFiles.exists())
      Files.fileTreeTraverser()
        .breadthFirstTraversal(this.changedFiles)
        .filter(Files.isFile())
        .forEach(f -> (File)this.changedEntries.put(this.changedFiles.toURI().relativize(f.toURI()).getPath(), f)); 
    if (this.removedFiles.exists())
      Files.fileTreeTraverser()
        .breadthFirstTraversal(this.removedFiles)
        .filter(Files.isFile())
        .transform(f -> this.removedFiles.toURI().relativize(f.toURI()).getPath())
        .forEach(this.removedEntries::add); 
    String cacheHash = null;
    String mcprHash = null;
    Optional<InputStream> cacheIn = getCache("recording.tmcpr.crc32");
    if (cacheIn.isPresent())
      try(InputStream in = (InputStream)cacheIn.get(); 
          Reader rin = new InputStreamReader(in); 
          BufferedReader brin = new BufferedReader(rin)) {
        cacheHash = brin.readLine();
      } catch (IOException iOException) {} 
    Optional<InputStream> mcprIn = get("recording.tmcpr.crc32");
    if (mcprIn.isPresent())
      try(InputStream in = (InputStream)mcprIn.get(); 
          Reader rin = new InputStreamReader(in); 
          BufferedReader brin = new BufferedReader(rin)) {
        mcprHash = brin.readLine();
      } catch (IOException iOException) {} 
    if (!Objects.equals(cacheHash, mcprHash)) {
      delete(cache);
      createCache(mcprHash);
    } 
  }
  
  private void createCache(String hash) throws IOException {
    if (hash == null)
      return; 
    try(OutputStream out = writeCache("recording.tmcpr.crc32"); 
        Writer writer = new OutputStreamWriter(out)) {
      writer.write(hash);
    } 
  }
  
  private void saveInputFile() throws IOException {
    if (this.shouldSaveInputFile) {
      Files.createParentDirs(this.sourceFile);
      try (OutputStream out = new BufferedOutputStream(new FileOutputStream(this.sourceFile))) {
        out.write(this.input.getCanonicalPath().getBytes(Charsets.UTF_8));
      } 
      this.shouldSaveInputFile = false;
    } 
  }
  
  public Optional<InputStream> get(String entry) throws IOException {
    if (this.changedEntries.containsKey(entry))
      return Optional.of(new BufferedInputStream(new FileInputStream(this.changedEntries.get(entry)))); 
    if (this.zipFile == null || this.removedEntries.contains(entry))
      return Optional.absent(); 
    ZipEntry zipEntry = this.zipFile.getEntry(entry);
    if (zipEntry == null)
      return Optional.absent(); 
    return Optional.of(new BufferedInputStream(this.zipFile.getInputStream(zipEntry)));
  }
  
  public Optional<InputStream> getCache(String entry) throws IOException {
    Path path = this.cache.toPath().resolve(entry);
    if (!Files.exists(path, new java.nio.file.LinkOption[0]))
      return Optional.absent(); 
    return Optional.of(new GZIPInputStream(new BufferedInputStream(Files.newInputStream(path, new java.nio.file.OpenOption[0]))));
  }
  
  public Map<String, InputStream> getAll(Pattern pattern) throws IOException {
    Map<String, InputStream> streams = new HashMap<>();
    for (Map.Entry<String, File> entry : this.changedEntries.entrySet()) {
      String name = entry.getKey();
      if (pattern.matcher(name).matches())
        streams.put(name, new BufferedInputStream(new FileInputStream(this.changedEntries.get(name)))); 
    } 
    if (this.zipFile != null) {
      Enumeration<? extends ZipEntry> entries = this.zipFile.entries();
      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        String name = entry.getName();
        if (pattern.matcher(name).matches() && 
          !streams.containsKey(name) && !this.removedEntries.contains(name))
          streams.put(name, new BufferedInputStream(this.zipFile.getInputStream(entry))); 
      } 
    } 
    return streams;
  }
  
  public OutputStream write(String entry) throws IOException {
    saveInputFile();
    File file = this.changedEntries.get(entry);
    if (file == null) {
      file = new File(this.changedFiles, entry);
      Files.createParentDirs(file);
      this.changedEntries.put(entry, file);
    } 
    OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
    Closeables.close(this.outputStreams.put(entry, out), true);
    if (this.removedEntries.remove(entry))
      Files.deleteIfExists((new File(this.removedFiles, entry)).toPath()); 
    if ("recording.tmcpr".equals(entry)) {
      try(OutputStream os = write("recording.tmcpr.crc32"); 
          Writer writer = new OutputStreamWriter(os)) {
        writer.write("invalid");
      } 
      this.recordingCrc = new CRC32();
      final OutputStream inner = out;
      out = new OutputStream() {
          public void write(int i) throws IOException {
            ZipReplayFile.this.recordingCrc.update(i);
            inner.write(i);
          }
          
          public void write(byte[] b, int off, int len) throws IOException {
            ZipReplayFile.this.recordingCrc.update(b, off, len);
            inner.write(b, off, len);
          }
          
          public void flush() throws IOException {
            inner.flush();
          }
          
          public void close() throws IOException {
            inner.close();
            String crc = "" + ZipReplayFile.this.recordingCrc.getValue();
            ZipReplayFile.this.recordingCrc = null;
            try(OutputStream out = ZipReplayFile.this.write("recording.tmcpr.crc32"); 
                Writer writer = new OutputStreamWriter(out)) {
              writer.write(crc);
            } 
            ZipReplayFile.this.delete(ZipReplayFile.this.cache);
            ZipReplayFile.this.createCache(String.valueOf(crc));
          }
        };
    } 
    return out;
  }
  
  public OutputStream writeCache(String entry) throws IOException {
    Path path = this.cache.toPath().resolve(entry);
    Files.createDirectories(path.getParent(), (FileAttribute<?>[])new FileAttribute[0]);
    return new GZIPOutputStream(new BufferedOutputStream(Files.newOutputStream(path, new java.nio.file.OpenOption[0])));
  }
  
  public void remove(String entry) throws IOException {
    saveInputFile();
    Closeables.close(this.outputStreams.remove(entry), true);
    File file = this.changedEntries.remove(entry);
    if (file != null && file.exists())
      delete(file); 
    this.removedEntries.add(entry);
    File removedFile = new File(this.removedFiles, entry);
    Files.createParentDirs(removedFile);
    Files.touch(removedFile);
  }
  
  public void removeCache(String entry) throws IOException {
    Path path = this.cache.toPath().resolve(entry);
    Files.deleteIfExists(path);
  }
  
  public void save() throws IOException {
    if (this.zipFile != null && this.changedEntries.isEmpty() && this.removedEntries.isEmpty())
      return; 
    File outputFile = Files.createTempFile("replaystudio", "replayfile", (FileAttribute<?>[])new FileAttribute[0]).toFile();
    saveTo(outputFile);
    close();
    if (this.output.exists())
      delete(this.output); 
    Files.move(outputFile.toPath(), this.output.toPath(), new java.nio.file.CopyOption[0]);
    this.zipFile = new ZipFile(this.output);
  }
  
  public void saveTo(File target) throws IOException {
    for (OutputStream outputStream : this.outputStreams.values())
      Closeables.close(outputStream, false); 
    this.outputStreams.clear();
    try (ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(target)))) {
      if (this.zipFile != null)
        for (ZipEntry entry : Collections.<ZipEntry>list(this.zipFile.entries())) {
          if (!this.changedEntries.containsKey(entry.getName()) && !this.removedEntries.contains(entry.getName())) {
            out.putNextEntry(entry);
            Utils.copy(this.zipFile.getInputStream(entry), out);
          } 
        }  
      for (Map.Entry<String, File> e : this.changedEntries.entrySet()) {
        out.putNextEntry(new ZipEntry(e.getKey()));
        Utils.copy(new BufferedInputStream(new FileInputStream(e.getValue())), out);
      } 
    } 
  }
  
  public void close() throws IOException {
    if (this.zipFile != null)
      this.zipFile.close(); 
    for (OutputStream out : this.outputStreams.values())
      Closeables.close(out, true); 
    this.outputStreams.clear();
    this.changedEntries.clear();
    this.removedEntries.clear();
    delete(this.tmpFiles);
  }
  
  private void delete(File file) throws IOException {
    File[] children = file.listFiles();
    if (children != null)
      for (File child : children)
        delete(child);  
    Files.deleteIfExists(file.toPath());
  }
}
