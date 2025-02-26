import sun.security.util.*;
import javax.security.auth.x500.*;
import sun.net.ftp.*;
public class MyJVMTest{

    public void test() throws Exception{
	String dn = "cn=hello, dc=com, dc=example";
	X500Principal p = new X500Principal(dn);
        byte[] encoded = p.getEncoded();
	DerInputStream dis = new DerInputStream(encoded);
        DerValue[] nameseq = dis.getSequence(3);
	DerInputStream is = new DerInputStream(nameseq[0].toByteArray());
        DerValue[] ava = is.getSet(3);
	System.out.println(ava);
	System.out.println(dis);
    } 
    public static void main(String[] args) throws Exception{
      MyJVMTest obj = new MyJVMTest();
      obj.test();
    }
}
