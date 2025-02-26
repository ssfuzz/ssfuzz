import java.util.Enumeration;

public class MyJVMTest_113 {

    static int c = 9;

    static short curindex = 32767;

    static short length = -32768;

    static byte[] bytes = { 79, 37, -10, 28, 75, 95, 122, -21, 7, -58 };

    Object nextElement() throws Exception {
        byte b;
        b = bytes[curindex++];
        if ((b & ((byte) 0x80)) == 0) {
            c = b;
        } else if ((b & ((byte) 0xe0)) == 0xc0) {
            c = ((b & ((byte) 0x1f))) << 6;
            b = bytes[curindex++];
            c |= (b & ((byte) 0x3f));
        } else {
            c = ((b & ((byte) 0x0f))) << 12;
            b = bytes[curindex++];
            c |= (b & ((byte) 0x3f));
        }
        return this;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(new MyJVMTest_113().nextElement());
    }
}
