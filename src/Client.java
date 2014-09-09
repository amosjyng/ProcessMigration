import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.SocketException;

public class Client {
  static Socket serverSocket;

  private static boolean debug = true;

  private static int port;

  public static void log(String message) {
    if (debug) {
      System.out.println("[Client:" + port + "] " + message);
    }
  }

  public static void main(String[] args) throws Exception {
    port = Integer.parseInt(args[1]);
    serverSocket = new Socket(args[0], port);
    try {
      receiveProcesses();
    } catch (SocketException e) {
      log("It appears the server has exited. Exiting as well.");
    }
  }

  public static void receiveProcesses() throws ClassNotFoundException, IOException {
    ObjectInputStream is = new ObjectInputStream(serverSocket.getInputStream());
    while (true) {
      MigratableProcess ins = (MigratableProcess) is.readObject();

      log("Received " + ins.toString());
      // when receiving, run single-threaded
      ins.run();
    }
  }
}
