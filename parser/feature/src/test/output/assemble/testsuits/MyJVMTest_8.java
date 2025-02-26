public class MyJVMTest_8 {

    static int LEN = 1000;

    static double[] a = new double[LEN];

    static double[] aopt = new double[LEN];

    boolean eCheck() {
        boolean r = true;
        for (int i = 0; i < LEN; i++) {
            if (a[i] != aopt[i]) {
                System.out.println("Bad result: a[" + i + "]=" + a[i] + "; aopt[" + i + "]=" + aopt[i]);
                r = false;
            }
        }
        return r;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(new MyJVMTest_8().eCheck());
    }
}
