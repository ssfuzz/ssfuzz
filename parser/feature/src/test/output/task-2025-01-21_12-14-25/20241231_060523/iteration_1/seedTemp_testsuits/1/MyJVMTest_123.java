import java.util.concurrent.Semaphore;

public class MyJVMTest_123 {

    static Object obj = 0;

    static Semaphore notFull = new Semaphore(10);

    static Semaphore notEmpty = new Semaphore(0);

    static Semaphore mutex = new Semaphore(1);

    static Object[] items = new Object[10];

    static int putptr = 3, takeptr = 0, count = 4;

    Object put(Object obj) throws InterruptedException {
        notFull.acquire();
        mutex.acquire();
        items[putptr] = obj;
        try {
            if (++putptr == items.length) {
                putptr = 0;
                ++count;
            }
        } finally {
            mutex.release();
            notEmpty.release();
        }
        return obj;
    }

    public static void main(String[] args) throws Exception {
        new MyJVMTest_123().put(obj);
    }
}
