import java.util.ArrayList;
import java.util.List;
import java.awt.*;
import java.awt.print.*;

public class MyJVMTest_4 {

    List<String> getDemoData() throws Exception {
        // MutationbyInsert 修正变量类型和值不符的情况，将 pageIndex 声明为整数类型
        int pageIndex = 3; 
        if (pageIndex == 1) {
            // MutationbyInsert 这里添加注释，表示模拟可能出现的索引越界异常情况
            throw new IndexOutOfBoundsException();
        }
        List<String> demoData = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            demoData.add("Android -- " + i);
        }
        return demoData;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(new MyJVMTest_4().getDemoData());
    }
}
