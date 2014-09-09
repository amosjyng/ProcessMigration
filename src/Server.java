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

public class Server {
  public static List<MigratableProcess> processes = new ArrayList<MigratableProcess>();

  private static int id_count = 0;

  private static BufferedReader stdin = new java.io.BufferedReader(new java.io.InputStreamReader(
          System.in));

  private static boolean debug = true;

  private static Map<Integer, ObjectOutputStream> outputStreams = new HashMap<Integer, ObjectOutputStream>();

  private static class ClientListener implements Runnable {
    private int port;

    private InputStreamReader br;

    public ClientListener(String address, int port) throws UnknownHostException, IOException, InterruptedException {
      this.port = port;
      // sleep for a bit to allow other machine to start listening
      Thread.sleep(500);
      br = new InputStreamReader(new Socket(address, port).getInputStream());
      Server.log("Connected to client.");
    }

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
          System.out.println("[Client:" + port + "] " + line.toString());
        } catch (IOException e) {
          error("Can't read line");
          e.printStackTrace();
        }
      }
    }
  }

  public static void log(String message) {
    if (debug) {
      System.out.println("[Server] " + message);
    }
  }

  public static void error(String message) {
    System.err.println("[Server] " + message);
  }

  public static MigratableProcess addProcess(MigratableProcess mp) {
    processes.add(mp);
    return mp;
  }

  public static void removeProcess(MigratableProcess mp) {
    processes.remove(mp);
  }

  public static MigratableProcess spawn(String processName, String[] args) throws Exception {
    log("Spawning " + processName + "#" + id_count);

    // looked up http://www.rgagnon.com/javadetails/java-0351.html
    MigratableProcess ins = addProcess((MigratableProcess) Class.forName(processName)
            .getConstructor(String[].class).newInstance((Object) args));
    ins.id = id_count++;

    new Thread(ins).start();
    return ins;
  }

  public static void ServerAccept(int port) throws IOException {
    outputStreams.put(port, new ObjectOutputStream(new ServerSocket(port).accept()
            .getOutputStream()));
  }

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

  private static String prompt() throws Exception {
    System.out.print("> ");
    return stdin.readLine();
  }

  private static void addClient(int serverPort, String clientAddress, int clientPort)
          throws Exception {
    log("Waiting for connection on port " + serverPort + "...");
    ServerAccept(serverPort);
    log("Accepted connection on port " + serverPort + ".");
    log("Connecting back to client at " + clientAddress + ":" + clientPort + "...");
    new Thread(new ClientListener(clientAddress, clientPort)).start();
  }

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
