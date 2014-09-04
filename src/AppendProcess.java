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
		if (in == null) { // then both in and out should be null
			out = new PrintStream(outFile);
			in = new BufferedReader(new InputStreamReader(inFile));
		}
		String line = in.readLine();
		
		if (line == null) return false;
		
		out.println(line);
		
		return true;
	}
}
