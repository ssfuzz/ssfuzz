import java.net.MalformedURLException;

public class MyJVMTest_135 {

    static int i = 2;

    static int j = 6;

    int printStatus() throws Exception {
        j++;
        System.out.println(" j--");
        System.out.println(" | --i=" + j);
        System.out.println(" | --j=" + i);
        return 99;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(new MyJVMTest_135().printStatus());
    }
}
