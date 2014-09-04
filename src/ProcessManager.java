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
	private static BufferedReader stdin = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
	private static int id_count = 0;
	
	private static String pm = "ProcessManager";
	
	public static void log (String processName, String message) {
		System.out.println("[" + processName + "] " + message);
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
	
	public static void migrate(MigratableProcess ins) throws InterruptedException, IOException, ClassNotFoundException{
		log(pm, "Migrating thread for \"" + ins.toString() + "\"");
		
		log(pm, "Telling \"" + ins.toString() + "\" to suspend.");
		ins.suspend();
		log(pm, "Writing out \"" + ins.toString() + "\"");
		ObjectOutputStream oos ;
		
		
		FileOutputStream fos;
		fos = new FileOutputStream("temp.out");
		oos = new ObjectOutputStream(fos);
		oos.writeObject(ins);
		
		processes.remove(ins);
		
		
		
		FileInputStream fis = new FileInputStream("temp.out");
		ObjectInputStream ois = new ObjectInputStream(fis);
		
		
		MigratableProcess newIns = addProcess((MigratableProcess)ois.readObject());
		log(pm, "Resuming \"" + newIns.toString() + "\"");
		new Thread(newIns).start();
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
	
	public static void main(String[] args) throws Exception {
		String line = prompt();
		while (!line.equals("exit")) {
			String[] stdinArgs = line.split(" ");
			String command = stdinArgs[0];
			
			if (command.equals("migrate")) {
				migrate(processes.get(Integer.parseInt(stdinArgs[1])));
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
