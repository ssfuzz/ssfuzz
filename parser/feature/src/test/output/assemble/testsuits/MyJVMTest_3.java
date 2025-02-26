public class MyJVMTest_3 {

    static K k = null;

    <K> String genericMethod1(K k) {
        return null;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(new MyJVMTest_3().genericMethod1(k));
    }
}
