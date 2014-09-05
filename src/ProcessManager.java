import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProcessManager {
	private static List<MigratableProcess> processes = new ArrayList<MigratableProcess>();
	private static BufferedReader stdin = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
	private static int id_count = 0;
	private static boolean debug = false;
	boolean tag;
	ObjectOutputStream oos ;
  ObjectInputStream is;

	ServerSocket server;
	Socket sock;
	Socket client;
	
	private static String pm = "ProcessManager";
	
	public static void log (String processName, String message) {
		if (debug) {
			System.out.println("[" + processName + "] " + message);
		}
	}
	
	public static void error (String processName, String message) {
		System.err.println("[" + processName + "] " + message);
	}
	public void ServerBuild(int port) throws IOException{
	  server=new ServerSocket (port);
	}
	public void ClientConnect(String ip, int port) throws UnknownHostException, IOException{
	  
	  client=new Socket(ip,port);
	}
	public void ServerAccept() throws IOException{
	  sock=server.accept();
	}
	
	public static MigratableProcess addProcess(MigratableProcess mp) {
		processes.add(mp);
		return mp;
	}
	public  ProcessManager (String ip, int port,boolean tag_input) throws Exception{
		
		
		
		tag=tag_input;

    //server=new ServerSocket (8888);
		//client=new Socket("127.0.0.1",8888);
    
		//sock=server.accept();
		
	
		if (tag){    // this is server
		  System.out.print("fuckfuck1");
		  ServerBuild(port);
		  System.out.print("fuckfuck2");
		  ServerAccept();
		  System.out.print("fuckfuck3");
		  oos = new ObjectOutputStream(sock.getOutputStream());
		  
		}
		else{
		  ClientConnect(ip,port);
		  
	    
		  is=new ObjectInputStream(client.getInputStream());
		}
	}
	
	public static void removeProcess(MigratableProcess mp) {
		processes.remove(mp);
	}
	
	public void migrate(MigratableProcess ins) throws InterruptedException, IOException, ClassNotFoundException{
		log(pm, "Migrating thread for \"" + ins.toString() + "\"");
		
		log(pm, "Telling \"" + ins.toString() + "\" to suspend.");
		ins.suspend();
		log(pm, "Sending out to other machine \"" + ins.toString() + "\"");
		
		oos.writeObject(ins);
		
		
		processes.remove(ins);
		
		
		

		

	
	}
	
	private static MigratableProcess spawn(String processName, String[] args) throws Exception {
		log(pm, "Spawning " + processName + "#" + id_count);

		
		// looked up http://www.rgagnon.com/javadetails/java-0351.html
		MigratableProcess ins = (MigratableProcess)Class.forName(processName).getConstructor(String[].class).newInstance((Object) args);
		ins.id = id_count++;
		

		new Thread(ins).start();
		return ins;
	}
	
	private static String prompt() throws Exception {
		System.out.print("> ");
		return stdin.readLine();
	}
	

public void ReceiveProcess() throws ClassNotFoundException, IOException{
  MigratableProcess newIns = addProcess((MigratableProcess)is.readObject());
  log(pm, "Resuming \"" + newIns.toString() + "\"");
  new Thread(newIns).start();
  
}
	
	public static void main(String[] args) throws Exception {
	  debug = args.length == 1 && args[0].equals("--debug");
		ProcessManager p=new ProcessManager("127.0.0.1",8850,!args[0].equals("1"));
		String line = prompt();
		while (!line.equals("exit")) {
			String[] stdinArgs = line.split(" ");
			String command = stdinArgs[0];
			
			if (command.equals("migrate")) {
				p.migrate(processes.get(Integer.parseInt(stdinArgs[1])));
			}
			else if (command.equals("ps")) {
				for (int i = 0; i < processes.size(); i++) {
					System.out.println(i + ". " + processes.get(i).toString());
				}
			}
			else {
				addProcess(spawn(command, Arrays.copyOf(Arrays.asList(stdinArgs).subList(1, stdinArgs.length).toArray(), stdinArgs.length - 1, String[].class)));
			}
			
			line = prompt();
		}
	}
	


}
