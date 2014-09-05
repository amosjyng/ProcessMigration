import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;


public class TransactionalFileInputStream extends InputStream implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3753071551924326173L;
	private String fileName;
	public long loc;
	
	TransactionalFileInputStream(String fileName){
		this.fileName = fileName;
		this.loc = 0;
	}
	
	
	@Override
	
	public int read() throws IOException {
		FileInputStream fis = new FileInputStream(fileName);
		fis.skip(loc);
		int value = fis.read();
		fis.close();
		if(value!=-1 && value=='\n')	loc++;
		return value;
	}

	@Override
	public int read(byte[] arg0, int arg1, int arg2) throws IOException {
		FileInputStream fis = new FileInputStream(fileName);
		fis.skip(loc);
		int value = fis.read(arg0,arg1,arg2);
		fis.close();
		if(value!=-1)	loc += value;
		return value;
	}

	@Override
	public int read(byte[] arg0) throws IOException {
		FileInputStream fis = new FileInputStream(fileName);
		fis.skip(loc);
		int value = fis.read(arg0);
		fis.close();
		if(value!=-1)	loc += value;
		return value;
	}
	/*
	public int read() throws IOException{
	  FileInputStream fis = new FileInputStream(fileName);
    fis.skip(loc);
    int value=0;
    while (fis.read()!='\n'){
      value += fis.read();
    }
    fis.close();
    loc+=value;
    return value;
    
	}
	*/
	
	
	

	@Override
	public long skip(long n) {
		if ( n <= 0 ) return 0;
		loc += n;
		return n;
	}
	

}
