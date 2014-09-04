import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProcessManager {
	
	private static List<MigratableProcess> processes = new ArrayList<MigratableProcess>();
	
	Thread t;
	
	ObjectOutputStream oos ;
	
	FileOutputStream fos;
	
	private static BufferedReader stdin = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
	
	private static String pm = "ProcessManager";
	
	public static void log (String processName, String message) {
		System.out.println("[" + processName + "] " + message);
	}
	
	public static void error (String processName, String message) {
		System.err.println("[" + processName + "] " + message);
	}
	
	public void migrate(MigratableProcess ins) throws InterruptedException, IOException, ClassNotFoundException{
		log(pm, "Migrating thread for \"" + ins.toString() + "\"");
		
		log(pm, "Telling \"" + ins.toString() + "\" to suspend.");
		ins.suspend();
		log(pm, "Writing out \"" + ins.toString() + "\"");
		fos = new FileOutputStream("temp.out");
		oos = new ObjectOutputStream(fos);
		oos.writeObject(ins);
		
		
		
		FileInputStream fis = new FileInputStream("temp.out");
		ObjectInputStream ois = new ObjectInputStream(fis);
		
		
		ins = (MigratableProcess)ois.readObject();
		log(pm, "Reading in \"" + ins.toString() + "\"");
		t=new Thread(ins);
		t.start();
	}
	
	private static MigratableProcess spawn(String processName, String[] args) throws Exception {
		// looked up http://www.rgagnon.com/javadetails/java-0351.html
		MigratableProcess ins = (MigratableProcess)Class.forName(processName).getConstructor(String[].class).newInstance((Object) args);
		
		new Thread(ins).start();
		return ins;
	}
	
	private static String prompt() throws Exception {
		System.out.print("> ");
		return stdin.readLine();
	}
	
	public static void main(String[] args) throws Exception {
		String line = prompt();
		while (!line.equals("exit")) {
			String[] stdinArgs = line.split(" ");
			String processName = stdinArgs[0];
			
			log(pm, "Starting new thread for \"" + processName + "\"");
			processes.add(spawn(processName, Arrays.copyOf(Arrays.asList(stdinArgs).subList(1, stdinArgs.length).toArray(), stdinArgs.length - 1, String[].class)));
			
			line = prompt();
		}
	}


}
