import java.util.HashMap;
import java.util.Map;

public class MyJVMTest_148 {

    static String code = "5s\\_1R2b*d";

    static String UNKNOWN_STATUS = "unknown status";

    static Map<String, String> statusMap = new HashMap<>();

    String getStatusText(String code) throws Exception {
        if (!statusMap.containsKey(code))
            return UNKNOWN_STATUS;
        return statusMap.get(code);
    }

    public static void main(String[] args) throws Exception {
        System.out.println(new MyJVMTest_148().getStatusText(code));
    }
}
