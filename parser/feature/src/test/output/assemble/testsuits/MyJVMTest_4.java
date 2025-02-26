public class MyJVMTest_4 {

    static float[] a = { 0.84102476f, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NaN, 0.614239f, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, 0.6582037f, Float.MAX_VALUE, Float.POSITIVE_INFINITY };

    static float[] b = { Float.NEGATIVE_INFINITY, Float.MIN_VALUE, 0.8402482f, Float.MIN_VALUE, Float.MAX_VALUE, 0.6509196f, Float.MAX_VALUE, Float.MIN_VALUE, Float.MAX_VALUE, Float.POSITIVE_INFINITY };

    static int k = 5;

    int test_cp_inv(float[] a, float[] b, int k) {
        for (int i = 0; i < a.length - k; i += 1) {
            a[i + k] = b[i + k];
        }
        return k;
    }

    public static void main(String[] args) throws Exception {
        new MyJVMTest_4().test_cp_inv(a, b, k);
    }
}
