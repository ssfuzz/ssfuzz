import java.util.LinkedList;

public class MyJVMTest_153 {

    static boolean beforePhase = false;

    static boolean afterPhase = true;

    static String LOG_BEFORE_KEY = "jsf.context.exception.handler.log_before";

    static String LOG_AFTER_KEY = "jsf.context.exception.handler.log_after";

    static String LOG_KEY = "jsf.context.exception.handler.log";

    static boolean errorPagePresent = false;

    String getLoggingKey(boolean beforePhase, boolean afterPhase) throws Exception {
        if (beforePhase) {
            return LOG_BEFORE_KEY;
        } else if (afterPhase) {
            return LOG_AFTER_KEY;
        } else {
            return LOG_KEY;
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println(new MyJVMTest_153().getLoggingKey(beforePhase, afterPhase));
    }
}
