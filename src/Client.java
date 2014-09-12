import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * To be run on the client machines. Listens for and runs processes from the server.
 *
 */
public class Client {
  /**
   * Output stream for sending messages back to server
   */
  private static OutputStreamWriter bs;

  /**
   * Input stream for receiving tasks from server
   */
  private static ObjectInputStream is;

  /**
   * Whether to show log messages
   */
  private static boolean debug = true;

  /**
   * Which port this is listening on
   */
  private static int port;

  /**
   * Prints out a message from this client
   * @param message The message to print out
   */
  public static void log(String message) {
    if (debug) {
      System.out.println("[Client:" + port + "] " + message);
    }
  }

  /**
   * Listens for processes from the server and runs them, and sends a message back notifying the server
   * that the task has finished when it ends. Single-threaded, so runs processes one at a time. Infinite
   * loop
   * @throws ClassNotFoundException
   * @throws IOException
   */
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

  /**
   * Starts up the Client
   * @param args Two arguments -- the address of the server to connect to, and the port to connect to on that address
   * @throws Exception
   */
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
    } catch (Exception e) {
      log("It appears the server has exited. Exiting as well.");
    }
  }
}
