import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * Serializable output stream for writing out to files in a migratable way
 */
public class TransactionalFileOutputStream extends OutputStream implements Serializable {
  private static final long serialVersionUID = 1257451613214481934L;

  /**
   * Filename to write out to
   */
  private String fileName;

  /**
   * Which location in the file we're at
   */
  private long loc;

  /**
   * Which file to overwrite to
   * @param fileName The name of the file to overwrite
   * @throws Exception
   */
  TransactionalFileOutputStream(String fileName) throws Exception {
    this(fileName, false);
  }

  /**
   * Which file to write to
   * @param fileName The name of the file to write to
   * @param append Whether to append to the end of the file or overwrite the file
   * @throws Exception
   */
  TransactionalFileOutputStream(String fileName, boolean append) throws Exception {
    this.fileName = fileName;
    this.loc = 0;
    if (!append) {
      new FileOutputStream(fileName, append).close();
    }
  }

  /**
   * Write one byte
   */
  @Override
  public void write(int arg0) throws IOException {
    FileOutputStream fos = new FileOutputStream(fileName, true);
    fos.write(arg0);
    fos.close();
    loc++;
  }

  /**
   * Write arg0.length bytes
   */
  @Override
  public void write(byte[] arg0) throws IOException {
    FileOutputStream fos = new FileOutputStream(fileName, true);
    fos.write(arg0);
    fos.close();
    loc += arg0.length;
  }

  /**
   * Write arg2 bytes with an offset of arg1
   */
  @Override
  public void write(byte[] arg0, int arg1, int arg2) throws IOException {
    FileOutputStream fos = new FileOutputStream(fileName, true);
    fos.write(arg0, arg1, arg2);
    fos.close();
    loc += arg2;
  }

  /**
   * Get the current location of where this output stream is at
   * @return The stream's location
   */
  public long getLoc() {
    return this.loc;
  }

}
