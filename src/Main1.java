
public class Main1 {

  public static void main(String[] args) throws Exception {
    // TODO Auto-generated method stub
    
    String []s=new String[3];
    s[0]="abcde";
    s[1]="1.txt";
    s[2]="2.txt";
    
    ProcessManager b=new ProcessManager("GrepProcess", s,"127.0.0.1",8886,0);
    b.ReceiveProcess();

    System.out.print("client active");

  }

}
