
public class Client {

  public static void main(String[] args) throws Exception {
    // TODO Auto-generated method stub
    
    String []s=new String[3];
    s[0]="U";
    s[1]="1.txt";
    s[2]="2.txt";
    
    ProcessManager b=new ProcessManager("127.0.0.1",8888,0);
    b.ReceiveProcess();

  }

}
