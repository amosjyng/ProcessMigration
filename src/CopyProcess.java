import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;

/**
 * Appends all the content from one file into another
 */
public class CopyProcess extends MigratableProcess {
	private TransactionalFileInputStream  inFile;
	private TransactionalFileOutputStream outFile;
	
	transient private PrintStream out;
	
	/**
   * Create a new such process
   * @param args Two arguments -- first is the file to read from, second is the file to write to
   * @throws Exception
   */
	public CopyProcess(String args[]) throws Exception
	{
		if (args.length != 2) {
			System.out.println("usage: CopyProcess <inputFile> <outputFile>");
			throw new Exception("Invalid Arguments");
		}
		
		inFile = new TransactionalFileInputStream(args[0]);
		outFile = new TransactionalFileOutputStream(args[1]);
	}
	
	/**
   * Whether or not there should be another loop. Copies one line from the input file to the output file every time
   */
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
