import java.util.Arrays;

public class MyJVMTest_115 {

    static int start = 559212607;

    static int end = -392542611;

    static int nbSet = 690883121;

    static boolean[] flags = { false, false, true, false, false, false, false, false, false, false };

    void clear() throws Exception {
        Arrays.fill(flags, false);
        start = flags.length;
        end = 0;
        nbSet = 0;
    }

    public static void main(String[] args) throws Exception {
        new MyJVMTest_115().clear();
    }
}
