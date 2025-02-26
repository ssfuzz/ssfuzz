import java.util.List;
import java.util.ArrayList;

public class MyJVMTest_139 {

    static List<String> currentProcesses = new ArrayList<String>();

    static int NO_CONFLICT = 0;

    static int SAME_PROCESS = 1;

    static int PREV_PROCESS = 3;

    int processFlowConflict(List<String> currentProcesses) throws Exception {
        if (currentProcesses.size() == 0)
            return NO_CONFLICT;
        if (currentProcesses.contains("processFlow"))
            return SAME_PROCESS;
        return PREV_PROCESS;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(new MyJVMTest_139().processFlowConflict(currentProcesses));
    }
}
