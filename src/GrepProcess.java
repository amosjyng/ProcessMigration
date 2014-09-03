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
	
	transient private PrintStream out;
	transient private BufferedReader in;

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
	
	public boolean continueRunning() throws Exception
	{
		if (in == null) { // then both in and out should be null
			out = new PrintStream(outFile);
			in = new BufferedReader(new InputStreamReader(inFile));
		}
		String line = in.readLine();
		
		if (line == null) return false;
		
		if (line.contains(query)) {
			out.println(line);
		}
		
		// Make grep take longer so that we don't require extremely large files for interesting results
		ProcessManager.log(toString(), "Sleeping (still running)");
		Thread.sleep(1000);
		
		return true;
	}

	public void run()
	{
		ProcessManager.log(toString(), "RUNNING");
	
		try {
			while (!suspending && continueRunning());
		} catch (Exception e) {
			ProcessManager.error(toString(), e.getMessage());
			e.printStackTrace();
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