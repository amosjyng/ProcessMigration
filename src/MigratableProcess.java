import java.io.*;

/***
 * This class acts as the base class for other migratable process
 * its main function is to provide a thread suspend mechanism 
 *    
 * 
 *
 */

public abstract class MigratableProcess implements Serializable, Runnable {
  public static boolean pauseEveryLoop = true;   //sleep flag to make the process slow enough for migrate

  public int id;  //process id, given to Server to tell which process it is

  public boolean debug = true;   //for debug only
  
  public boolean finished = false; //flag for detect whether the task has been finished yet

  private transient volatile boolean suspending = false;   // flag for process sleeping 

  public void log(String message) {  //for debug
    if (debug) {
      System.out.println("[" + toString() + "] " + message);
    }
  }
  
  public void error(String message) { //for error message
    System.err.println("[" + toString() + "] " + message);
  }

  // Return false to exit the thread
  // inherited by other migratable process to do actual work
  public abstract boolean continueRunning() throws Exception;

  //for thread running
  public void run() {
    log("RUNNING");

    try {
      while (!suspending && !finished) {  //not finished yet and not suspending, which means running!
        finished = !continueRunning();   //finished or not, returned by continueRunning function
        if (pauseEveryLoop) {   //just make it slow!
          //log("Sleeping (still running)");
          Thread.sleep(1000);
        }
      }
    } catch (Exception e) { //debug message
      
      error(e.getMessage());
      e.printStackTrace();
    }

    if (suspending) {  //
      log("EXITING (suspended)");
    } else {
      log("EXITING (finished)");
    }

    suspending = false;
  }

  public void suspend() { 
    log("SUSPENDING");
    suspending = true;
    while (!finished && suspending) //make sure it has not finished, if finished, suspending makes no sense
     ;
  }
  
  public boolean isFinished() {  //return finished
    return finished;
  }

  @Override
  public String toString() {  //give process id, inherited by other migratable process
    return this.getClass().getName() + "#" + id;
  }
}
