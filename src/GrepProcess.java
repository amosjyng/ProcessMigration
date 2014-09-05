import java.io.PrintStream;
import java.io.EOFException;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.Thread;
import java.lang.InterruptedException;

public class GrepProcess extends MigratableProcess
{
	/**
   * 
   */
  private static final long serialVersionUID = 1L;
  private TransactionalFileInputStream  inFile;
	private TransactionalFileOutputStream outFile;
	private String query;
	
	transient private PrintStream out;
	transient private BufferedReader in;

	public GrepProcess(String args[]) throws Exception
	{
		if (args.length != 3) {
			System.out.println("usage: GrepProcess <queryString> <inputFile> <outputFile>");
			throw new Exception("Invalid Arguments");
		}
		
		query = args[0];
		inFile = new TransactionalFileInputStream(args[1]);
		outFile = new TransactionalFileOutputStream(args[2], false);
	}
	
	public boolean continueRunning() throws Exception
	{
		if (in == null) { // then both in and out should be null
			out = new PrintStream(outFile);
			in = new BufferedReader (new InputStreamReader(inFile));
			
		}

    System.out.println(inFile.loc);
		String line = in.readLine();
		System.out.println(line);
		System.out.println(inFile.loc);
		if (line == null) return false;
		
		if (line.contains(query)) {
			out.println(line);
			
		}
		
		// Make grep take longer so that we don't require extremely large files for interesting results
		ProcessManager.log(toString(), "Sleeping (still running)");
		Thread.sleep(100);
		
		return true;
	}
}