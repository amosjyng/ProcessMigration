import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

/**
 * Migratable (i.e. serializable) file input stream
 */
public class TransactionalFileInputStream extends InputStream implements Serializable {
  private static final long serialVersionUID = 3753071551924326173L;

  /**
   * The name of the file to read from
   */
  private String fileName;

  /**
   * Which location we're at right now
   */
  public long loc;

  /**
   * Create a new input stream
   * @param fileName Which file to read
   */
  TransactionalFileInputStream(String fileName) {
    this.fileName = fileName;
    this.loc = 0;
  }

  /**
   * Read one byte
   */
  @Override
  public int read() throws IOException {
    FileInputStream fis = new FileInputStream(fileName);
    fis.skip(loc);
    int value = fis.read();
    fis.close();
    if (value != -1)
      loc++;
    return value;
  }

  /**
   * Read  up to arg2 bytes
   */
  @Override
  public int read(byte[] arg0, int arg1, int arg2) throws IOException {
    FileInputStream fis = new FileInputStream(fileName);
    fis.skip(loc);
    int value = fis.read(arg0, arg1, arg2);
    fis.close();
    if (value != -1)
      loc += value;
    return value;
  }

  /**
   * Read up to arg0.length bytes of data
   */
  @Override
  public int read(byte[] arg0) throws IOException {
    FileInputStream fis = new FileInputStream(fileName);
    fis.skip(loc);
    int value = fis.read(arg0);
    fis.close();
    if (value != -1)
      loc += value;
    return value;
  }

  /**
   * Read a single line (until a \n character is encountered) without buffering
   * @return Returns that line (without the newline)
   * @throws IOException
   */
  public String readLine() throws IOException {
    StringBuilder line = new StringBuilder();

    int nextChar = read();
    while (nextChar != (int) '\n' && nextChar != -1) {
      line.append((char) nextChar);
      nextChar = read();
    }

    if (nextChar == -1 && line.toString().isEmpty()) {
      return null;
    } else {
      return line.toString();
    }
  }

  /**
   * Skip to a location in the fiile
   */
  @Override
  public long skip(long n) {
    if (n <= 0)
      return 0;
    loc += n;
    return n;
  }

}
