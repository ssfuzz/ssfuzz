import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MyJVMTest_9 {

    static MessageDigest prototype = null;

    static boolean useClone = false;

    MessageDigest getMessageDigest(final MessageDigest prototype, final boolean useClone) {
        try {
            if (useClone) {
                return (MessageDigest) prototype.clone();
            }
            return MessageDigest.getInstance(prototype.getAlgorithm(), prototype.getProvider());
        } catch (final CloneNotSupportedException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println(new MyJVMTest_9().getMessageDigest(prototype, useClone));
    }
}
