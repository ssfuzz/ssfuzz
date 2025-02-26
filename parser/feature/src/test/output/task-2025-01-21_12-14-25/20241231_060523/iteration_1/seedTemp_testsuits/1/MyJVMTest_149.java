import java.io.File;

public class MyJVMTest_149 {

    static File systemTempDir = null;

    static File cleanUpFile = null;

    static long serialNumber = 0;

    File getSystemTempFile() throws Exception {
        if (systemTempDir == null) {
            systemTempDir = new File(System.getProperty("java.io.tmpdir"));
        }
        return systemTempDir;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(new MyJVMTest_149().getSystemTempFile());
    }
}
