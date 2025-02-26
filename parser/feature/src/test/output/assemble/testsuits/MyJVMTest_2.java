public class MyJVMTest_2 {

    String testDiv_hand_opt() {
        int ia, ib;
        long lc, ld;
        double de, df;
        System.out.println("testDiv_hand_opt:");
        if (1 != 513 / 513)
            return "case 1 failed";
        if (-1 != -513 / 513)
            return "case 2 failed";
        if (1 != 1073741824 / 1073741824)
            return "case 3 failed";
        if (-1 != 1073741824 / -1073741824)
            return "case 4 failed";
        if (1 != 8L / 8L)
            return "case 5 failed";
        if (-1 != -8L / 8L)
            return "case 6 failed";
        if (1 != 1073741824L / 1073741824L)
            return "case 7 failed";
        if (-1 != 1073741824L / -1073741824L)
            return "case 8 failed";
        ib = 0;
        try {
            ia = -1073741824 / ib;
            return "case 9 failed";
        } catch (java.lang.Exception x) {
        }
        ld = 0L;
        try {
            lc = -1073741824L / ld;
            return "case 10 failed";
        } catch (java.lang.Exception x) {
        }
        try {
            lc = -1073741824L % ld;
            return "case 11 failed";
        } catch (java.lang.Exception x) {
        }
        if (1.0 != 16385.0 / 16385.0)
            return "case 12 failed";
        if (-1.0 != -16385.0 / 16385.0)
            return "case 13 failed";
        df = 0.0;
        try {
            de = -1073741824L / df;
        } catch (java.lang.Exception x) {
            return "case 14 failed";
        }
        try {
            de = -1073741824L % 0.0;
            de = 5.66666666666 % df;
        } catch (java.lang.Exception x) {
            return "cnase 15 failed";
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(new MyJVMTest_2().testDiv_hand_opt());
    }
}
