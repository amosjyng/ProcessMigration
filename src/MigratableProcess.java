import java.io.*;


public interface MigratableProcess extends Serializable,Runnable{
	public abstract boolean continueRunning() throws Exception;
	
	public abstract void suspend();

	
	@Override
    public String toString();

}
