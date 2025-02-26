import java.io.File;

public class MyJVMTest_150 {

    static String path = "^MJiNjp2pj";

    boolean deleteFileNoThrow(String path) throws Exception {
        File file;
        boolean result;
        try {
            result = true;
        } catch (NullPointerException e) {
            result = false;
        }
        return result;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(new MyJVMTest_150().deleteFileNoThrow(path));
    }
}
