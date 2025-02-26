import java.util.ArrayList;
import java.util.List;

public class MyJVMTest_156 {

    static List<Object> objs = new ArrayList<Object>();

    List<String> convert(List<Object> objs) throws Exception {
        List<String> strs = new ArrayList<>(objs.size());
        for (Object obj : objs) {
            strs.add(obj.toString());
        }
        return strs;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(new MyJVMTest_156().convert(objs));
    }
}
