import java.io.Serializable;

public class MyJVMTest_126 {

    static boolean[] sig1 = { false, false, true, true, false, false, true, true, false, false };

    static boolean[] sig2 = { true, true, false, false, false, false, true, true, true, false };

    static double[][] hyperplanes = { { Double.POSITIVE_INFINITY, 0.5487141134741336, 0.3561163639723738, Double.NEGATIVE_INFINITY, Double.MAX_VALUE, Double.MIN_VALUE, Double.NaN, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NaN }, { Double.NEGATIVE_INFINITY, Double.NaN, 0.5297560428557384, Double.NaN, 0d, Double.MIN_VALUE, Double.NaN, Double.NaN, 0d, Double.NEGATIVE_INFINITY } };

    static int DEFAULT_CODE_LENGTH = 10000;

    double similarity(final boolean[] sig1, final boolean[] sig2) throws Exception {
        double agg = 0;
        for (int i = 0; i < sig1.length; i++) {
            if (sig1[i] == sig2[i]) {
                agg++;
            }
        }
        agg = agg / sig1.length;
        return Math.cos((1 - agg) * Math.PI);
    }

    public static void main(String[] args) throws Exception {
        System.out.println(new MyJVMTest_126().similarity(sig1, sig2));
    }
}
