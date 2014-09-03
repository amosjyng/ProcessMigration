import java.io.*;


public interface MigratableProcess extends Serializable,Runnable{
	
	public abstract void run();
	public abstract void suspend();

	
	@Override
    public String toString();

}
