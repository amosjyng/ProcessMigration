import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;


public class CopyProcess extends MigratableProcess {
	private TransactionalFileInputStream  inFile;
	private TransactionalFileOutputStream outFile;
	
	transient private PrintStream out;
	
	public CopyProcess(String args[]) throws Exception
	{
		if (args.length != 2) {
			System.out.println("usage: CopyProcess <inputFile> <outputFile>");
			throw new Exception("Invalid Arguments");
		}
		
		inFile = new TransactionalFileInputStream(args[0]);
		outFile = new TransactionalFileOutputStream(args[1]);
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
