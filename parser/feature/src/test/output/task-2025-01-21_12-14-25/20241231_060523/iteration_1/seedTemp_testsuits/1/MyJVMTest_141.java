import java.util.Stack;

public class MyJVMTest_141 {

    static int x = 0;

    static Stack<Integer> dataStack = null;

    static int min = 1;

    static Stack<Integer> minStack = null;

    int push(int x, Stack<Integer> dataStack, int min, Stack<Integer> minStack) throws Exception {
        dataStack.add(x);
        min = Math.min(min, x);
        minStack.add(min);
        return min;
    }

    public static void main(String[] args) throws Exception {
        new MyJVMTest_141().push(x, dataStack, min, minStack);
    }
}
