import java.io.*;


public abstract class MigratableProcess implements Serializable, Runnable{
	/**
   * 
   */
  private static final long serialVersionUID = 1L;
  private volatile boolean suspending = false;
	
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
