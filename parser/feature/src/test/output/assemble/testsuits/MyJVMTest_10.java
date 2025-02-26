import java.util.*;
import java.io.*;

public class MyJVMTest_10 {

    void showCommittedMemory() throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream("/proc/meminfo")));
        System.out.println("-------");
        String line;
        while ((line = r.readLine()) != null) {
            if (line.startsWith("Commit")) {
                System.out.printf("%s%n", line);
            }
        }
        System.out.println("-------");
    }

    public static void main(String[] args) throws Exception {
        new MyJVMTest_10().showCommittedMemory();
    }
}
