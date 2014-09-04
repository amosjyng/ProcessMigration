import java.io.*;


public abstract class MigratableProcess implements Serializable, Runnable{
	public int id;
	private transient volatile boolean suspending = false;
	
	// Return false to exit the thread
	public abstract boolean continueRunning() throws Exception;
	
	public void run()
	{
		ProcessManager.log(toString(), "RUNNING");
	
		try {
			while (!suspending && continueRunning());
		} catch (Exception e) {
			ProcessManager.error(toString(), e.getMessage());
			e.printStackTrace();
		}
		
		if (suspending) {
			ProcessManager.log(toString(), "EXITING (suspended)");
		} else {
			ProcessManager.log(toString(), "EXITING (finished)");
			ProcessManager.removeProcess(this);
		}
		
		suspending = false;
	}

	public void suspend()
	{
		ProcessManager.log(toString(), "SUSPENDING");
		suspending = true;
		while (suspending);
	}
	
	@Override
	public String toString() {
		return this.getClass().getName() + "#" + id;
	}
}
