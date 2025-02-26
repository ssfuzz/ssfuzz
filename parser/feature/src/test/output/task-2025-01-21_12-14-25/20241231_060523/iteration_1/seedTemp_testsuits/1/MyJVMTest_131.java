import java.util.Random;

public class MyJVMTest_131 {

    static int length = 2;

    String getRandomStringByLength(int length) throws Exception {
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    public static void main(String[] args) throws Exception {
        System.out.println(new MyJVMTest_131().getRandomStringByLength(length));
    }
}
