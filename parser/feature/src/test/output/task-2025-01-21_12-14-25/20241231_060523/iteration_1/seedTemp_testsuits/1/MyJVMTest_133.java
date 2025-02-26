import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class MyJVMTest_133 {

    static String cuboidRootPath = "AWCK`uFr8V";

    static int totalRowkeyColumnCount = 0;

    static int groupRowkeyColumnsCount = 9;

    String[] getCuboidOutputPaths(String cuboidRootPath, int totalRowkeyColumnCount, int groupRowkeyColumnsCount) throws Exception {
        String[] paths = new String[groupRowkeyColumnsCount + 1];
        for (int i = 0; i <= groupRowkeyColumnsCount; i++) {
            int dimNum = totalRowkeyColumnCount - i;
            if (dimNum == totalRowkeyColumnCount) {
                paths[i] = cuboidRootPath + "base_cuboid";
            } else {
                paths[i] = cuboidRootPath + dimNum + "d_cuboid";
            }
        }
        return paths;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(Arrays.asList(new MyJVMTest_133().getCuboidOutputPaths(cuboidRootPath, totalRowkeyColumnCount, groupRowkeyColumnsCount)));
    }
}
