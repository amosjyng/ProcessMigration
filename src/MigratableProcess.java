import java.io.*;

public abstract class MigratableProcess implements Serializable, Runnable {
  public static boolean pauseEveryLoop = true;

  public int id;

  public boolean debug = true;
  
  public boolean finished = false;

  private transient volatile boolean suspending = false;

  public void log(String message) {
    if (debug) {
      System.out.println("[" + toString() + "] " + message);
    }
  }
  
  public void error(String message) {
    System.err.println("[" + toString() + "] " + message);
  }

  // Return false to exit the thread
  public abstract boolean continueRunning() throws Exception;

  public void run() {
    log("RUNNING");

    try {
      while (!suspending && !finished) {
        finished = !continueRunning();
        if (pauseEveryLoop) {
          log("Sleeping (still running)");
          Thread.sleep(1000);
        }
      }
    } catch (Exception e) {
      error(e.getMessage());
      e.printStackTrace();
    }

    if (suspending) {
      log("EXITING (suspended)");
    } else {
      log("EXITING (finished)");
      // notify server or something
      // ProcessManager.removeProcess(this);
    }

    suspending = false;
  }

  public void suspend() {
    log("SUSPENDING");
    suspending = true;
    while (!finished && suspending)
      ;
  }
  
  public boolean isFinished() {
    return finished;
  }

  @Override
  public String toString() {
    return this.getClass().getName() + "#" + id;
  }
}
