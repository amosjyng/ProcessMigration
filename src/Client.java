import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class Client {  
  private static OutputStreamWriter bs;
  
  private static ObjectInputStream is;

  private static boolean debug = true;

  private static int port;

  public static void log(String message) {
    if (debug) {
      System.out.println("[Client:" + port + "] " + message);
    }
  }

  public static void receiveProcesses() throws ClassNotFoundException, IOException {
    while (true) {
      log("Waiting for server to send tasks...");
      MigratableProcess ins = (MigratableProcess) is.readObject();

      log("Received " + ins.toString());
      // when receiving, run single-threaded
      ins.run();
      
      log("Notifying server that task has been completed.");
      bs.write(ins.toString() + " has completed.\n");
      bs.flush();
    }
  }
  
  public static void main(String[] args) throws Exception {
    port = Integer.parseInt(args[1]);
    log("Connecting to server at " + args[0] + " on port " + args[1] + "...");
    is = new ObjectInputStream(new Socket(args[0], port).getInputStream());
    // port + 1 so we can easily debug on the same machine
    log("Connected to server. Now waiting for server connection on port " + (port + 1) + "...");
    bs = new OutputStreamWriter(new ServerSocket(port + 1).accept().getOutputStream());
    log("Server has connected.");
    try {
      receiveProcesses();
    } catch (SocketException e) {
      log("It appears the server has exited. Exiting as well.");
    }
  }
}
