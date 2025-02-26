import java.io.File;

public class MyJVMTest_157 {

    static String uuid = "XyxP<XiGxF";

    static String path = "s,Rp(\"E}/i";

    boolean createFolder(String uuid, String path) throws Exception {
        String _mountPoint = "/mnt";
        String mountPoint = _mountPoint + File.separator + uuid;
        File f = new File(mountPoint + File.separator + path);
        if (!f.exists()) {
            return f.mkdirs();
        }
        return true;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(new MyJVMTest_157().createFolder(uuid, path));
    }
}
