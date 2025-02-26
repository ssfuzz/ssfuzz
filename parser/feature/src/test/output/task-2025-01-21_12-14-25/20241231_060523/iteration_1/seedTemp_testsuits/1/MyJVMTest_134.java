import java.util.ArrayList;
import java.util.List;

public class MyJVMTest_134 {

    static int taskId = 0;

    static List<Object> values = new ArrayList<Object>();

    static int streamIdIndex = 5;

    static int sequenceNrIndex = 0;

    static List<Integer> targetTasks = new ArrayList<Integer>();

    List<Integer> chooseTasks(int taskId, List<Object> values) throws Exception {
        String streamId = (String) values.get(streamIdIndex);
        Long sequence = (Long) values.get(sequenceNrIndex);
        streamId = streamId.substring(0, streamId.lastIndexOf('_'));
        values.set(streamIdIndex, streamId);
        int hash = streamId.hashCode() + sequence.hashCode();
        int targetId = targetTasks.get(hash % targetTasks.size());
        List<Integer> result = new ArrayList<Integer>();
        result.add(targetId);
        return result;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(new MyJVMTest_134().chooseTasks(taskId, values));
    }
}
