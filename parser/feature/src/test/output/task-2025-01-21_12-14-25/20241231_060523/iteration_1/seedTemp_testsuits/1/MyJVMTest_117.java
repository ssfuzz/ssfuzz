import java.io.Serializable;

public class MyJVMTest_117 {

    static int index = -598487687;

    static short value = 1802;

    static short[] items = { -32768, -32768, 32767, -14529, -32768, -29998, 32767, 0, 0, 11795 };

    static int size = 0;

    static boolean ordered = false;

    int mul(int index, short value) throws Exception {
        if (index >= size)
            throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + size);
        items[index] *= value;
        return index;
    }

    public static void main(String[] args) throws Exception {
        new MyJVMTest_117().mul(index, value);
    }
}
