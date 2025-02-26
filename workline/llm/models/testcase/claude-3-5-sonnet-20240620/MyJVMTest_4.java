
import java.util.ArrayList;
import java.util.List;
import java.awt.*;
import java.awt.print.*;

/**
 * 改进的JVM测试用例
 * 主要改进点：
 * 1. 增加了Printable接口实现，使pageIndex变量有实际意义
 * 2. 将测试逻辑整合到print方法中，使异常触发与业务逻辑有关联
 * 3. 增加了实际的打印相关代码，使测试更真实
 * 4. 通过printData()方法将异常触发与数据处理进行关联
 */
public class MyJVMTest_4 implements Printable {
    private List<String> data;
    
    public MyJVMTest_4() {
        this.data = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            data.add("Android -- " + i);
        }
    }

    private void printData(int pageIndex) throws IndexOutOfBoundsException {
        // 保留原始的异常触发逻辑，但与实际打印操作结合
        if (pageIndex == 1) {
            throw new IndexOutOfBoundsException("Invalid page index: " + pageIndex);
        }
        
        // 模拟分页处理数据
        int itemsPerPage = 10;
        int startIndex = (pageIndex - 1) * itemsPerPage;
        if (startIndex >= data.size()) {
            throw new IndexOutOfBoundsException("Page index out of range");
        }
        
        int endIndex = Math.min(startIndex + itemsPerPage, data.size());
        List<String> pageData = data.subList(startIndex, endIndex);
        System.out.println("Printing page " + pageIndex + ": " + pageData);
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        try {
            printData(pageIndex + 1); // 页码从1开始
            return PAGE_EXISTS;
        } catch (IndexOutOfBoundsException e) {
            if (pageIndex > 0) { // 正常结束打印
                return NO_SUCH_PAGE;
            }
            // 传递异常，触发JVM测试目标
            throw new PrinterException(e.getMessage());
        }
    }

    public static void main(String[] args) {
        MyJVMTest_4 test = new MyJVMTest_4();
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(test);
        
        try {
            // 触发打印操作，将导致print方法被调用
            job.print();
        } catch (PrinterException e) {
            System.out.println("Printing failed: " + e.getMessage());
        }
    }
}
