import java.util.Random;

public class MyJVMTest_140 {

    static long rndParam1 = 0;

    static Random rnd = new Random(rndParam1);

    static int maxlen = 8;

    String getRandomStr(Random rnd, int maxlen) throws Exception {
        int n = rnd.nextInt(maxlen) + 1;
        char[] str = new char[n];
        for (int i = 0; i < n; i++) str[i] = (char) ('a' + rnd.nextInt(3));
        return new String(str);
    }

    public static void main(String[] args) throws Exception {
        System.out.println(new MyJVMTest_140().getRandomStr(rnd, maxlen));
    }
}
