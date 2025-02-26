import java.net.URL;

public class MyJVMTest_130 {

    static String value = "t:I+h7^_y2";

    boolean isValidURL(String value) throws Exception {
        try {
            URL u = new URL(value);
            return u.getProtocol().startsWith("http:");
        } catch (Exception e) {
            return false;
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println(new MyJVMTest_130().isValidURL(value));
    }
}
