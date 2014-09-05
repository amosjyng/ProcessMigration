import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class ProcessManager {
	
	private MigratableProcess ins;
	
	
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
		System.out.println("[" + processName + "] " + message);
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
	
	public  ProcessManager (String processName, String []s,String ip, int port,int tag_input) throws Exception{
		log(pm, "Starting new thread for \"" + processName + "\"");
		
		// looked up http://www.rgagnon.com/javadetails/java-0351.html
		ins = (MigratableProcess)Class.forName(processName).getConstructor(String[].class).newInstance((Object) s);
		fos = new FileOutputStream("temp.out");
		
		tag=tag_input;

    //server=new ServerSocket (8888);
		//client=new Socket("127.0.0.1",8888);
    
		//sock=server.accept();
		
	
		if (tag==1){    // this is server
		  ServerBuild(port);
		  ServerAccept();
		  oos = new ObjectOutputStream(sock.getOutputStream());
		}
		else{
		  ClientConnect(ip,port);
		  
	    
		  is=new ObjectInputStream(client.getInputStream());
		}
	}
	
	
	public void launch(){
		
		t=new Thread(ins);
		t.start();
		
	}
	public void migrate() throws InterruptedException, IOException, ClassNotFoundException{
		log(pm, "Migrating thread for \"" + ins.toString() + "\"");
		
		log(pm, "Telling \"" + ins.toString() + "\" to suspend.");
		ins.suspend();
		log(pm, "Writing out \"" + ins.toString() + "\"");
		oos.writeObject(ins);
		System.out.println("wowowo");
		
		
		
		
		//FileInputStream fis = new FileInputStream("temp.out");
		//ObjectInputStream ois = new ObjectInputStream(is);
		
		
		
		
		
}
public void ReceiveProcess() throws ClassNotFoundException, IOException{
  
  ins = (MigratableProcess)is.readObject();
  
  log(pm, "Reading in \"" + ins.toString() + "\"");
  t=new Thread(ins);
  t.start();
}
	
	public static void main(String[] args) throws Exception {
		String []s=new String[3];
		s[0]="abcde";
		s[1]="1.txt";
		s[2]="2.txt";
		
		ProcessManager a=new ProcessManager("GrepProcess", s,"127.0.0.1",8886,1);
		
		ProcessManager b=new ProcessManager("GrepProcess", s,"127.0.0.1",8886,0);
		a.launch();
		//Thread.sleep(1000);
		a.migrate();
		
	  
    b.ReceiveProcess();
		
		
		
		
	}
	


}
