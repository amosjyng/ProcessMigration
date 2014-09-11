import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * To be run on the server machine. Spawns processes, takes in user input, and migrates processes to other machines.
 */
public class Server {
  /**
   * List of processes spawned on the server.
   */
  public static List<MigratableProcess> processes = new ArrayList<MigratableProcess>();

  /**
   * The ID of the next process to be created
   */
  private static int id_count = 0;

  /**
   * Buffered reader for stdin
   */
  private static BufferedReader stdin = new java.io.BufferedReader(new java.io.InputStreamReader(
          System.in));

  /**
   * Whether or not to print statements that illustrate what is going on behind the scenes
   */
  private static boolean debug = true;

  /**
   * Output streams for sending serializable objects to client machines
   */
  private static Map<Integer, ObjectOutputStream> outputStreams = new HashMap<Integer, ObjectOutputStream>();

  /**
   * Listens to clients sending completion messages back
   */
  private static class ClientListener implements Runnable {
    /**
     * Which port of the client we're listening to
     */
    private int port;

    /**
     * Input stream for reading messages from the client
     */
    private InputStreamReader br;

    /**
     * Create a ClientListener to listen to task completion messages
     * @param address Address of the client to listen to
     * @param port Port of the client to listen to
     * @throws UnknownHostException
     * @throws IOException
     * @throws InterruptedException
     */
    public ClientListener(String address, int port) throws UnknownHostException, IOException, InterruptedException {
      this.port = port;
      // sleep for a bit to allow other machine to start listening
      Thread.sleep(500);
      br = new InputStreamReader(new Socket(address, port).getInputStream());
      Server.log("Connected to client.");
    }

    /**
     * Start a new thread solely for listening for task completion events from the client
     */
    @Override
    public void run() {
      while (true) {
        try {
          StringBuilder line = new StringBuilder();
          int nextChar = br.read();
          while (nextChar != (int) '\n' && nextChar != -1) {
            line.append((char) nextChar);
            nextChar = br.read();
          }
          System.out.println("[Client:" + (port - 1) + "] " + line.toString());
        } catch (IOException e) {
          error("Can't read line");
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * Prints out a message from the server
   * @param message What to inform the user off
   */
  public static void log(String message) {
    if (debug) {
      System.out.println("[Server] " + message);
    }
  }

  /**
   * Prints out an error from the server
   * @param message What to notify the user of
   */
  public static void error(String message) {
    System.err.println("[Server] " + message);
  }

  /**
   * Add a process to the list of spawned processes
   * @param mp The process to be added
   * @return mp
   */
  public static MigratableProcess addProcess(MigratableProcess mp) {
    processes.add(mp);
    return mp;
  }

  /**
   * Remove a process from the list of spawned processes
   * @param mp The process to be removed
   */
  public static void removeProcess(MigratableProcess mp) {
    processes.remove(mp);
  }

  /**
   * Create a new thread for a process and store it in the list of spawned processes
   * @param processName The name of the Class to create a new thread out of
   * @param args The arguments to the constructor of that class
   * @return The newly created thread running that process
   * @throws Exception
   */
  public static MigratableProcess spawn(String processName, String[] args) throws Exception {
    log("Spawning " + processName + "#" + id_count);

    // looked up http://www.rgagnon.com/javadetails/java-0351.html
    MigratableProcess ins = addProcess((MigratableProcess) Class.forName(processName)
            .getConstructor(String[].class).newInstance((Object) args));
    ins.id = id_count++;

    new Thread(ins).start();
    return ins;
  }

  /**
   * Create an output for the client to listen to
   * @param port Which port the client listens on
   * @throws IOException
   */
  public static void ServerAccept(int port) throws IOException {
    outputStreams.put(port, new ObjectOutputStream(new ServerSocket(port).accept()
            .getOutputStream()));
  }

  /**
   * Moves a process from this machine to a client
   * @param ins The process to be migrated
   * @param port The port which the client will be listening on (on this machine)
   * @throws InterruptedException
   * @throws IOException
   * @throws ClassNotFoundException
   */
  public static void migrate(MigratableProcess ins, int port) throws InterruptedException,
          IOException, ClassNotFoundException {
    ObjectOutputStream oos = outputStreams.get(port);
    log("Migrating thread for \"" + ins.toString() + "\"");

    log("Telling \"" + ins.toString() + "\" to suspend.");
    ins.suspend();
    if (ins.isFinished()) {
      log(ins.toString() + " is already finished. No need to migrate.");
    } else {
      oos.writeObject(ins);
      log("Sent " + ins.toString() + " to client on port " + port);
    }
    oos.reset();
  }

  /**
   * Asks the user to enter a command
   * @return The command the user enters
   * @throws Exception
   */
  private static String prompt() throws Exception {
    System.out.print("> ");
    return stdin.readLine();
  }

  /**
   * Connects to a client
   * @param serverPort The port on the server the client will be listening to for tasks
   * @param clientAddress The address of the client to connect to
   * @param clientPort The port on the client to listen to for messages
   * @throws Exception
   */
  private static void addClient(int serverPort, String clientAddress, int clientPort)
          throws Exception {
    log("Waiting for connection on port " + serverPort + "...");
    ServerAccept(serverPort);
    log("Accepted connection on port " + serverPort + ".");
    log("Connecting back to client at " + clientAddress + ":" + clientPort + "...");
    new Thread(new ClientListener(clientAddress, clientPort)).start();
  }

  /**
   * Starts the server up
   * @param args Not used
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    String line = prompt();
    while (!line.equals("exit")) {
      String[] stdinArgs = line.split(" ");
      String command = stdinArgs[0];

      if (command.equals("addClient")) {
        if (stdinArgs.length != 4) {
          error("usage: addClient <server port> <client address> <client port>");
        } else {
          addClient(Integer.parseInt(stdinArgs[1]), stdinArgs[2], Integer.parseInt(stdinArgs[3]));;
        }
      } else if (command.equals("mv")) {
        if (stdinArgs.length != 3) {
          error("usage: mv <process #> <port #>");
        } else {
          try {
            migrate(processes.get(Integer.parseInt(stdinArgs[1])), Integer.parseInt(stdinArgs[2]));
          } catch (IndexOutOfBoundsException e) {
            error("No such process #" + stdinArgs[1]);
          }
        }
      } else if (command.equals("ps")) {
        for (int i = 0; i < processes.size(); i++) {
          System.out.println(i + ". " + processes.get(i).toString());
        }
      } else {
        spawn(command, Arrays.copyOf(Arrays.asList(stdinArgs).subList(1, stdinArgs.length)
                .toArray(), stdinArgs.length - 1, String[].class));
      }

      line = prompt();
    }

    System.exit(0);
  }

}
