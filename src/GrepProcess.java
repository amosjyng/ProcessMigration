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
	private TransactionalFileInputStream  inFile;
	private TransactionalFileOutputStream outFile;
	private String query;
	
	transient private PrintStream out;

	public GrepProcess(String args[]) throws Exception
	{
		if (args.length != 3) {
			System.out.println("usage: GrepProcess <queryString> <inputFile> <outputFile>");
			throw new Exception("Invalid Arguments");
		}
		
		query = args[0];
		inFile = new TransactionalFileInputStream(args[1]);
		outFile = new TransactionalFileOutputStream(args[2]);
	}
	
	public boolean continueRunning() throws Exception
	{
		if (out == null) {
			out = new PrintStream(outFile);
		}
		String line = inFile.readLine();
		
		if (line == null) return false;
		
		if (line.contains(query)) {
			out.println(line);
		}
		
		return true;
	}
}