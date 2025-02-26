import java.util.ArrayList;
import java.util.List;

public class MyJVMTest_154 {

    static String agentRollupId = ",w6`]tG@Yx";

    List<String> getAgentRollupIds(String agentRollupId) throws Exception {
        List<String> agentRollupIds = new ArrayList<>();
        agentRollupIds.add(agentRollupId);
        int separatorLen = "::".length();
        int lastFoundIndex;
        if (agentRollupId.endsWith("::")) {
            lastFoundIndex = agentRollupId.length() - separatorLen;
        } else {
            lastFoundIndex = agentRollupId.length();
        }
        int nextFoundIndex;
        while ((nextFoundIndex = agentRollupId.lastIndexOf("::", lastFoundIndex - separatorLen - 1)) != -1) {
            if (nextFoundIndex == 0) {
                break;
            }
            agentRollupIds.add(agentRollupId.substring(0, nextFoundIndex + separatorLen));
            lastFoundIndex = nextFoundIndex;
        }
        return agentRollupIds;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(new MyJVMTest_154().getAgentRollupIds(agentRollupId));
    }
}
