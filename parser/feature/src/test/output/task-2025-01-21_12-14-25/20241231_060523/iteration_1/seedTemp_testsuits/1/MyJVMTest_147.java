import java.lang.reflect.Array;
import java.util.Arrays;

public class MyJVMTest_147 {

    static short[] array = { 25561, -32768, 10222, 0, -32768, 0, 32767, -32768, 10299, 18438 };

    static int size = -1941643378;

    static short initialValue = 0;

    short[] ensureArraySize(short[] array, int size, short initialValue) throws Exception {
        if (array.length >= size) {
            Arrays.fill(array, 0, size, initialValue);
        } else {
            array = new short[size];
            if (initialValue != 0) {
                Arrays.fill(array, 0, size, initialValue);
            }
        }
        return array;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(Arrays.asList(new MyJVMTest_147().ensureArraySize(array, size, initialValue)));
    }
}
