import java.io.BufferedWriter;

public class MyJVMTest_109 {

    static String content = "DvUw<x-j&<";

    static BufferedWriter bfWriter = null;

    static String filePath = "1vK,rG=i&n";

    String writeFile(String content) throws Exception {
        try {
            bfWriter.write(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content;
    }

    public static void main(String[] args) throws Exception {
        new MyJVMTest_109().writeFile(content);
    }
}
