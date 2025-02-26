import java.util.HashMap;
import java.util.Map;

public class MyJVMTest_159 {

    static String key = ".6 'gf`VaO";

    static Map<String, Float> metrics = new HashMap<String, Float>();

    float getBac(String key) throws Exception {
        float result = metrics.get(key);
        if (result < 0) {
            result += 1;
        }
        return result;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(new MyJVMTest_159().getBac(key));
    }
}
