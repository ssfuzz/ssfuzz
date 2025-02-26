import java.util.ArrayList;
import java.util.List;

public class MyJVMTest_4 {

    // 方法返回一个包含字符串的列表
    public List<String> getDemoData() throws Exception {
        List<String> demoData = new ArrayList<>();
        int pageIndex = 3; // 定义pageIndex变量并初始化为3

        // 检查pageIndex是否为1，如果是，则抛出异常
        if (pageIndex == 1) {
            throw new IndexOutOfBoundsException("PageIndex cannot be 1");
        }
        
        // 循环添加50个字符串到列表中
        for (int i = 0; i < 50; i++) {
            demoData.add("Android -- " + i);
        }
        return demoData;
    }

    public static void main(String[] args) {
        try {
            // 创建MyJVMTest_4实例并调用getDemoData方法
            System.out.println(new MyJVMTest_4().getDemoData());
        } catch (Exception e) {
            e.printStackTrace();
            // 输出异常信息，确保缺陷代码段与种子代码有交互
            System.out.println("Exception occurred: " + e.getMessage());
        }
    }
}

// 完成的工作：
// 1. 修复了pageIndex变量类型声明问题。
// 2. 确保了pageIndex变量在方法内不会导致冲突，并正确地与其他代码交互。
// 3. 修改了异常抛出的方式，使得其能够与种子代码（即循环添加数据）有交互，且不影响循环逻辑。
// 4. 在main方法中添加了异常捕获，保证了即使触发异常也能输出相关错误信息，并能正常结束程序。
