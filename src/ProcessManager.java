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
	
	
	public  ProcessManager (String processName, String []s) throws Exception{
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
		//Thread.sleep(100);
		//t.sleep(1000);
		ins.suspend();
		oos.writeObject(ins);
		
		
		
		FileInputStream fis = new FileInputStream("temp.out");
		ObjectInputStream ois = new ObjectInputStream(fis);
		
		
		ins = (MigratableProcess)ois.readObject();
		System.out.println("After Deserialize");
		t=new Thread(ins);
		t.start();
		
		
}
	
	public static void main(String[] args) throws Exception {
		String []s=new String[3];
		s[0]="abcde";
		s[1]="1.txt";
		s[2]="2.txt";
		System.out.print("haha");
		
		ProcessManager a=new ProcessManager("GrepProcess", s);
		a.launch();
		a.migrate();
		
		
	}


}
