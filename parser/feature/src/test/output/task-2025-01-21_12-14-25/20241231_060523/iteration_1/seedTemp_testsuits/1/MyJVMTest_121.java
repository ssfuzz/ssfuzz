import java.net.Socket;
import java.util.Map;

public class MyJVMTest_121 {

    static int writeBufferLowWaterMark = 56108054;

    static int writeBufferHighWaterMark = 64 * 1024;

    int setWriteBufferLowWaterMark0(int writeBufferLowWaterMark) throws Exception {
        if (writeBufferLowWaterMark < 0) {
            throw new IllegalArgumentException("writeBufferLowWaterMark: " + writeBufferLowWaterMark);
        }
        this.writeBufferLowWaterMark = writeBufferLowWaterMark;
        return writeBufferLowWaterMark;
    }

    public static void main(String[] args) throws Exception {
        new MyJVMTest_121().setWriteBufferLowWaterMark0(writeBufferLowWaterMark);
    }
}
