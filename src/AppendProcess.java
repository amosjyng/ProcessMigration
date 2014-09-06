import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;


public class AppendProcess extends MigratableProcess {
	private TransactionalFileInputStream  inFile;
	private TransactionalFileOutputStream outFile;
	
	transient private PrintStream out;
	transient private BufferedReader in;
	
	public AppendProcess(String args[]) throws Exception
	{
		if (args.length != 2) {
			System.out.println("usage: AppendProcess <inputFile> <outputFile>");
			throw new Exception("Invalid Arguments");
		}
		
		inFile = new TransactionalFileInputStream(args[0]);
		outFile = new TransactionalFileOutputStream(args[1], true);
	}
	
	public boolean continueRunning() throws Exception
	{
		if (out == null) {
			out = new PrintStream(outFile);
		}
		String line = inFile.readLine();
		
		if (line == null) return false;
		
		out.println(line);
		
		return true;
	}
}
