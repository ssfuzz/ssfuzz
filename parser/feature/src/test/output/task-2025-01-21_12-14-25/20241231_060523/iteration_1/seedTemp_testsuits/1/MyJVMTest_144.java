import java.util.ArrayList;
import java.util.Arrays;

public class MyJVMTest_144 {

    static String s = "Qh07(\"/X%:";

    static char c = '0';

    String[] split(String s, char c) throws Exception {
        ArrayList<String> arr = new ArrayList<String>();
        int start = 0;
        int end = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == c) {
                arr.add(s.substring(start, i));
                start = i + 1;
            }
        }
        if (start < s.length()) {
            arr.add(s.substring(start));
        }
        return arr.toArray(new String[arr.size()]);
    }

    public static void main(String[] args) throws Exception {
        System.out.println(Arrays.asList(new MyJVMTest_144().split(s, c)));
    }
}
