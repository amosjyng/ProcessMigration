import java.io.PrintStream;
import java.io.EOFException;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.Thread;
import java.lang.InterruptedException;

public class GrepProcess implements MigratableProcess
{
	private TransactionalFileInputStream  inFile;
	private TransactionalFileOutputStream outFile;
	private String query;

	private volatile boolean suspending = false;

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

	public void run()
	{
		PrintStream out = new PrintStream(outFile);
		BufferedReader in = new BufferedReader(new InputStreamReader(inFile));
		
		ProcessManager.log(toString(), "RUNNING");
		
	
		try {
			while (!suspending) {
				
				
				String line = in.readLine();
				
				

				if (line == null)// break;
				
				if (line.contains(query)) {
					out.println(line);
				}
				
				
				
				// Make grep take longer so that we don't require extremely large files for interesting results
				try {
					ProcessManager.log(toString(), "Sleeping (still running)");
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					System.err.println ("GrepProcess: Interrupted: " + e);
				}
			}
		} catch (EOFException e) {
			System.err.println ("GrepProcess: EOF: " + e);
		} catch (IOException e) {
			System.err.println ("GrepProcess: IO: " + e);
		}

		ProcessManager.log(toString(), "EXITING");
		suspending = false;
	}

	public void suspend()
	{
		ProcessManager.log(toString(), "SUSPENDING");
		suspending = true;
		while (suspending);
	}

}