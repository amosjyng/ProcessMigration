import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Server {
  public static List<MigratableProcess> processes = new ArrayList<MigratableProcess>();

  private static int id_count = 0;

  private static BufferedReader stdin = new java.io.BufferedReader(new java.io.InputStreamReader(
          System.in));

  private static boolean debug = true;

  static ServerSocket serverSocket;

  static Socket socket;

  public static void log(String message) {
    if (debug) {
      System.out.println("[Server] " + message);
    }
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

  public static void ServerBuild(int port) throws IOException {
    serverSocket = new ServerSocket(port);
  }

  public static void ServerAccept() throws IOException {
    socket = serverSocket.accept();
  }

  public static void migrate(MigratableProcess ins) throws InterruptedException, IOException,
          ClassNotFoundException {
    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
    log("Migrating thread for \"" + ins.toString() + "\"");

    log("Telling \"" + ins.toString() + "\" to suspend.");
    ins.suspend();
    if (ins.isFinished()) {
      log(ins.toString() + " is already finished. No need to migrate.");
    }
    else {
      log("Sending out \"" + ins.toString() + "\"");
      oos.writeObject(ins);
    }
  }

  private static String prompt() throws Exception {
    System.out.print("> ");
    return stdin.readLine();
  }

  public static void main(String[] args) throws Exception {
    int port = Integer.parseInt(args[0]);
    ServerBuild(port);
    log("Waiting for connection on port " + port);
    ServerAccept();
    log("Accepted connection on port " + port);

    String line = prompt();
    while (!line.equals("exit")) {
      String[] stdinArgs = line.split(" ");
      String command = stdinArgs[0];

      if (command.equals("migrate")) {
        migrate(processes.get(Integer.parseInt(stdinArgs[1])));
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

  }

}
