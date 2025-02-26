import java.io.DataInput;

public class MyJVMTest_142 {

    static int dimension = 7;

    static Number value = null;

    static int DEFAULT_LENGTH = 128;

    static float[] vector = { 0f, Float.MIN_VALUE, 0f, Float.MIN_VALUE, Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, 0.8149775f, 0.1887855f, Float.NEGATIVE_INFINITY };

    static float ori = Float.MAX_VALUE;

    static float scale = 0.90736526f;

    static float x = Float.MAX_VALUE;

    static float y = Float.POSITIVE_INFINITY;

    Number setOrdinate(int dimension, Number value) throws Exception {
        if (dimension == 0)
            x = value.floatValue();
        if (dimension == 1)
            y = value.floatValue();
        return value;
    }

    public static void main(String[] args) throws Exception {
        new MyJVMTest_142().setOrdinate(dimension, value);
    }
}
