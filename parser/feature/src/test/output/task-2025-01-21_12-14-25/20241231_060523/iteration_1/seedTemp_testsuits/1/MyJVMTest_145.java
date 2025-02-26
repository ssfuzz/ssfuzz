import java.util.concurrent.Semaphore;

public class MyJVMTest_145 {

    static int num = -179297794;

    static Semaphore semaphore = null;

    void run() throws Exception {
        try {
            semaphore.acquire();
            System.out.println("??" + this.num + "?????????...");
            Thread.sleep(2000);
            System.out.println("??" + this.num + "?????");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        new MyJVMTest_145().run();
    }
}
