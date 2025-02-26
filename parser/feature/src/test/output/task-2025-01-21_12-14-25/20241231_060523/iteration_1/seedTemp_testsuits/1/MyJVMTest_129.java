import java.util.Random;

public class MyJVMTest_129 {

    static String MESSAGE_SIZE_S = "size";

    static String MESSAGE_INTERVAL_S = "interval";

    static String HOST_RANGE_S = "hosts";

    static String TO_HOST_RANGE_S = "tohosts";

    static String MESSAGE_ID_PREFIX_S = "prefix";

    static String MESSAGE_TIME_S = "time";

    static double nextEventsTime = 0;

    static int[] hostRange = { 0, 0 };

    static int[] toHostRange = null;

    static int id = 0;

    static String idPrefix = "PxM%!y]an\\";

    static int[] sizeRange = { 2, 0, 2, -2081635479, 1, 0, 1, 621732589, 399137816, 1604908729 };

    static int[] msgInterval = { 0, 9, 9, 939743674, 0, 5, 6, 4, 4, 0 };

    static double[] msgTime = { 0.5467566643141935, 0.14918940380059809, Double.MAX_VALUE, 0d, 0d, 0d, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 0d };

    static Random rng = null;

    int drawNextEventTimeDiff() throws Exception {
        int timeDiff = msgInterval[0] == msgInterval[1] ? 0 : rng.nextInt(msgInterval[1] - msgInterval[0]);
        return msgInterval[0] + timeDiff;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(new MyJVMTest_129().drawNextEventTimeDiff());
    }
}
