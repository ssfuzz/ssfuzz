import java.io.BufferedWriter;

public class MyJVMTest_118 {

    static String content = "B`PCND.T)'";

    static BufferedWriter bfWriter = null;

    static String filePath = "<)L8d.xiPm";

    String writeFile(String content) throws Exception {
        try {
            bfWriter.write(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content;
    }

    public static void main(String[] args) throws Exception {
        new MyJVMTest_118().writeFile(content);
    }
}
