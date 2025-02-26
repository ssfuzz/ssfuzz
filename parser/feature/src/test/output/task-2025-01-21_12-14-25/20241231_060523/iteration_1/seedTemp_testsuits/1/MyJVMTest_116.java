import java.lang.reflect.Method;

public class MyJVMTest_116 {

    static Method method = null;

    String getFieldName(final Method method) throws Exception {
        String name = method.getName().substring(3);
        char[] chars = name.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }

    public static void main(String[] args) throws Exception {
        System.out.println(new MyJVMTest_116().getFieldName(method));
    }
}
