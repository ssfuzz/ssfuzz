import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class MyJVMTest_152 {

    static String str = "k0;(k~= k1";

    static char delimiter = Character.MIN_VALUE;

    static boolean removeEmpty = true;

    String[] split(String str, char delimiter, boolean removeEmpty) throws Exception {
        final int len = (str == null) ? 0 : str.length();
        if (len == 0) {
            return new String[0];
        }
        final List<String> result = new ArrayList<String>();
        String elem = null;
        int i = 0, j = 0;
        while (j != -1 && j < len) {
            j = str.indexOf(delimiter, i);
            elem = (j != -1) ? str.substring(i, j) : str.substring(i);
            i = j + 1;
            if (!removeEmpty || !(elem == null || elem.length() == 0)) {
                result.add(elem);
            }
        }
        return result.toArray(new String[result.size()]);
    }

    public static void main(String[] args) throws Exception {
        System.out.println(Arrays.asList(new MyJVMTest_152().split(str, delimiter, removeEmpty)));
    }
}
