import java.util.function.Supplier;

public class MyJVMTest_1 {

    static Supplier<String> supplier = null;

    static String msg = "l|4R(bI)B%";

    Supplier<String> test(Supplier<String> supplier) {
        msg = supplier.get();
        return supplier;
    }

    public static void main(String[] args) throws Exception {
        new MyJVMTest_1().test(supplier);
    }
}
