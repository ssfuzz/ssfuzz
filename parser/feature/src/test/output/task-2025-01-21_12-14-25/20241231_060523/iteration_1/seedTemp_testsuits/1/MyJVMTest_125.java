import java.io.File;
import java.io.FileFilter;

public class MyJVMTest_125 {

    static String fileParam1Param1 = "a>]}Ssvg6o";

    static String fileParam1Param2 = "+aS5[|:ax8";

    static File fileParam1 = new File(fileParam1Param1, fileParam1Param2);

    static String fileParam2 = "c2Xq&?n;@T";

    static File file = new File(fileParam1, fileParam2);

    boolean accept(File file) throws Exception {
        String fileName = file.getName();
        if (fileName.endsWith(".index") || fileName.endsWith(".zip") || fileName.endsWith(".threads")) {
            return false;
        }
        return file.isFile() || !fileName.startsWith(".");
    }

    public static void main(String[] args) throws Exception {
        System.out.println(new MyJVMTest_125().accept(file));
    }
}
