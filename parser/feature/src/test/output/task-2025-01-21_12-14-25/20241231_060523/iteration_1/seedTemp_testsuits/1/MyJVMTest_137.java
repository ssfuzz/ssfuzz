import java.io.IOException;

public class MyJVMTest_137 {

    static Object value = -1250939008;

    boolean verifyDataType(final Object value) throws Exception {
        if (value instanceof String) {
            return true;
        } else if (value == null) {
            return true;
        } else {
            return false;
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println(new MyJVMTest_137().verifyDataType(value));
    }
}
