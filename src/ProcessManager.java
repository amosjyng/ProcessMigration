import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;

public class ProcessManager {
	
	private MigratableProcess ins;
	
	Thread t;
	
	ObjectOutputStream oos ;
	
	FileOutputStream fos;
	
	private static String pm = "ProcessManager";
	
	public static void log (String processName, String message) {
		System.out.println("[" + processName + "] " + message);
	}
	
	public static void error (String processName, String message) {
		System.err.println("[" + processName + "] " + message);
	}
	
	
	public  ProcessManager (String processName, String []s) throws Exception{
		log(pm, "Starting new thread for \"" + processName + "\"");
		
		// looked up http://www.rgagnon.com/javadetails/java-0351.html
		ins = (MigratableProcess)Class.forName(processName).getConstructor(String[].class).newInstance((Object) s);
		fos = new FileOutputStream("temp.out");
		oos = new ObjectOutputStream(fos);
		
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
		
		
		
		FileInputStream fis = new FileInputStream("temp.out");
		ObjectInputStream ois = new ObjectInputStream(fis);
		
		
		ins = (MigratableProcess)ois.readObject();
		log(pm, "Reading in \"" + ins.toString() + "\"");
		t=new Thread(ins);
		t.start();
		
		
}
	
	public static void main(String[] args) throws Exception {
		String []s=new String[3];
		s[0]="abcde";
		s[1]="1.txt";
		s[2]="2.txt";
		
		ProcessManager a=new ProcessManager("GrepProcess", s);
		a.launch();
		Thread.sleep(1000);
		a.migrate();
		
		
	}


}
