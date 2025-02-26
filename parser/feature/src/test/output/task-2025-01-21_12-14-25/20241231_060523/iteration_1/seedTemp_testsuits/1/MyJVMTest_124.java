import java.util.AbstractList;

public class MyJVMTest_124 {

    static byte item = -128;

    static byte[] data = { 84, -59, -44, -102, 65, 6, 121, -72, -15, 118 };

    boolean contains(byte item) throws Exception {
        for (int i = 0; i < data.length; ++i) {
            if (data[i] == item) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(new MyJVMTest_124().contains(item));
    }
}
