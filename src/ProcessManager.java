import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class ProcessManager {
	public static List<MigratableProcess> processes = new ArrayList<MigratableProcess>();
	private static int id_count = 0;
	private static boolean debug = true;
	
	
	int tag;
	
	Thread t;
	
	ObjectOutputStream oos ;
	ObjectInputStream is;
	
	FileOutputStream fos;
	
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
	public static MigratableProcess addProcess(MigratableProcess mp) {
		processes.add(mp);
		return mp;
	}	
	public static void removeProcess(MigratableProcess mp) {
		processes.remove(mp);
	}
	public static MigratableProcess spawn(String processName, String[] args) throws Exception {
		log(pm, "Spawning " + processName + "#" + id_count);
		
		// looked up http://www.rgagnon.com/javadetails/java-0351.html
		MigratableProcess ins = addProcess((MigratableProcess)Class.forName(processName).getConstructor(String[].class).newInstance((Object) args));
		ins.id = id_count++;
		
		new Thread(ins).start();
		return ins;
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
	
	public  ProcessManager (String ip, int port,int tag_input) throws Exception{
		tag=tag_input;
		if (tag==1){    // this is server
		  ServerBuild(port);
		  log(pm, "Waiting for connection on port " + port);
		  ServerAccept();
		  log(pm, "Accepted connection on port " + port);
		  oos = new ObjectOutputStream(sock.getOutputStream());
		}
		else{
		  ClientConnect(ip,port);
		  is=new ObjectInputStream(client.getInputStream());
		}
	}
	public void migrate(MigratableProcess ins) throws InterruptedException, IOException, ClassNotFoundException{
		log(pm, "Migrating thread for \"" + ins.toString() + "\"");
		
		log(pm, "Telling \"" + ins.toString() + "\" to suspend.");
		ins.suspend();
		log(pm, "Sending out \"" + ins.toString() + "\"");
		oos.writeObject(ins);
	}
	public void ReceiveProcess() throws ClassNotFoundException, IOException{
	  
	  MigratableProcess ins = (MigratableProcess)is.readObject();
	  
	  log(pm, "Received \"" + ins.toString() + "\"");
	  t=new Thread(ins);
	  t.start();
	}
}
