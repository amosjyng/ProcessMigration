import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
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
    outputStreams.put(port, new ObjectOutputStream(new ServerSocket(port).accept().getOutputStream()));
  }

  public static void migrate(MigratableProcess ins, ObjectOutputStream oos) throws InterruptedException,
          IOException, ClassNotFoundException {
    log("Migrating thread for \"" + ins.toString() + "\"");

    log("Telling \"" + ins.toString() + "\" to suspend.");
    ins.suspend();
    if (ins.isFinished()) {
      log(ins.toString() + " is already finished. No need to migrate.");
    } else {
      log("Sending out \"" + ins.toString() + "\"");
      oos.writeObject(ins);
    }
    oos.reset();
  }

  private static String prompt() throws Exception {
    System.out.print("> ");
    return stdin.readLine();
  }

  public static void main(String[] args) throws Exception {
    for (String arg : args) {
      int port = Integer.parseInt(arg);
      log("Waiting for connection on port " + port);
      ServerAccept(port);
      log("Accepted connection on port " + port);
    }

    String line = prompt();
    while (!line.equals("exit")) {
      String[] stdinArgs = line.split(" ");
      String command = stdinArgs[0];

      if (command.equals("migrate")) {
        if (stdinArgs.length != 4) {
          error("usage: migrate <process #> to <port #>");
        }
        else {
          try {
            migrate(processes.get(Integer.parseInt(stdinArgs[1])), outputStreams.get(Integer.parseInt(stdinArgs[3])));
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

  }

}
