package com.replaymod.replaystudio.launcher;

import com.replaymod.replaystudio.util.ThreadLocalOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class DaemonLauncher {
  private static final int PORT = Integer.parseInt(System.getProperty("replaystudio.port", "4002"));
  
  private ExecutorService worker;
  
  private ThreadLocalOutputStream systemOut;
  
  public void launch(CommandLine cmd) throws Exception {
    int threads = Integer.parseInt(cmd.getOptionValue('d', "" + Runtime.getRuntime().availableProcessors()));
    this.worker = Executors.newFixedThreadPool(threads);
    System.setOut(new PrintStream((OutputStream)(this.systemOut = new ThreadLocalOutputStream(System.out))));
    ServerSocket serverSocket = new ServerSocket(PORT);
    System.out.println("Daemon started on port " + PORT + " with " + threads + " worker threads.");
    while (!Thread.interrupted()) {
      Socket socket = serverSocket.accept();
      try {
        Client client = new Client(socket);
        (new Thread(client)).start();
      } catch (IOException e) {
        e.printStackTrace();
      } 
    } 
  }
  
  private class Client implements Runnable {
    private final Socket socket;
    
    private final BufferedReader in;
    
    private final DataOutputStream out;
    
    public Client(Socket socket) throws IOException {
      this.socket = socket;
      this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      this.out = new DataOutputStream(socket.getOutputStream());
    }
    
    public void run() {
      try {
        while (!this.socket.isClosed()) {
          String command = this.in.readLine();
          this.out.write(0);
          this.out.write(0);
          Future<?> future = DaemonLauncher.this.worker.submit(() -> {
                System.out.println("[" + Thread.currentThread().getName() + "] Running: " + command);
                DaemonLauncher.this.systemOut.setOutput(this.out);
                List<String> parts = new ArrayList<>();
                Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(command);
                while (m.find())
                  parts.add(m.group(1).replace("\"", "")); 
                try {
                  this.out.write(0);
                  this.out.write(1);
                  Launcher.run(parts.<String>toArray(new String[parts.size()]));
                } catch (Exception e) {
                  e.printStackTrace();
                  try {
                    this.out.write(0);
                    this.out.write(3);
                    this.out.writeUTF(ExceptionUtils.getStackTrace(e));
                    this.out.close();
                    this.socket.close();
                  } catch (IOException e1) {
                    e1.printStackTrace();
                  } 
                } 
                DaemonLauncher.this.systemOut.setOutput(DaemonLauncher.this.systemOut.getDefault());
                System.out.println("[" + Thread.currentThread().getName() + "] Done: " + command);
              });
          try {
            future.get();
          } catch (InterruptedException|java.util.concurrent.ExecutionException e) {
            e.printStackTrace();
          } 
          if (!this.socket.isClosed()) {
            this.out.write(0);
            this.out.write(2);
          } 
        } 
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        if (!this.socket.isClosed())
          try {
            this.socket.close();
          } catch (IOException e) {
            e.printStackTrace();
          }  
      } 
    }
  }
}
