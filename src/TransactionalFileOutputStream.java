import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;


public class TransactionalFileOutputStream extends OutputStream implements Serializable{

	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1257451613214481934L;
	private String fileName;
	private long loc;
	
	TransactionalFileOutputStream(String fileName, boolean migrated){
		this.fileName = fileName;
		this.loc = 0;
	}

	@Override
	public void write(int arg0) throws IOException {
		FileOutputStream fos = new FileOutputStream(fileName);
		fos.write(arg0);
		fos.close();
		loc++;
	}
	
	@Override
	public void write(byte[] arg0) throws IOException {
		FileOutputStream fos = new FileOutputStream(fileName);
		fos.write(arg0);
		fos.close();
		loc += arg0.length;
	}
	
	@Override
	public void write(byte[] arg0, int arg1, int arg2) throws IOException {
		FileOutputStream fos = new FileOutputStream(fileName);
		fos.write(arg0, arg1, arg2);
		fos.close();
		loc += arg2;
	}
	
	public long getLoc(){
		return this.loc;
	}

}
