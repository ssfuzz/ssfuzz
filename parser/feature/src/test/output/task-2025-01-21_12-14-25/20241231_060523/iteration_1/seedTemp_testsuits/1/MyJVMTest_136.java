import java.util.List;
import java.util.ArrayList;

public class MyJVMTest_136 {

    static List<Float> array = new ArrayList<Float>();

    float pool(List<Float> array) throws Exception {
        float sum = 0;
        float squareSum = 0;
        for (float v : array) {
            sum += v;
            squareSum += v * v;
        }
        float avg = sum / array.size();
        return (squareSum / array.size() - avg * avg);
    }

    public static void main(String[] args) throws Exception {
        System.out.println(new MyJVMTest_136().pool(array));
    }
}
