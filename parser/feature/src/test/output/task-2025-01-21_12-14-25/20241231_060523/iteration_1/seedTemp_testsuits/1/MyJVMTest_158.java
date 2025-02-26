import java.io.IOException;

public class MyJVMTest_158 {

    static String ENHANCER_JOB_CLASS_NAME = "org.datanucleus.ide.eclipse.jobs.EnhancerJob";

    boolean isPresent() throws Exception {
        try {
            Class.forName(ENHANCER_JOB_CLASS_NAME);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println(new MyJVMTest_158().isPresent());
    }
}
