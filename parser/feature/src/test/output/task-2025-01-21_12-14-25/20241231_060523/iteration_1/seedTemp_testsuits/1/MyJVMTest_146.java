import java.util.ArrayList;

public class MyJVMTest_146 {

    static int series = 0;

    static String value = "|2RH]L\"SFx";

    static ArrayList<String> names = new ArrayList<String>();

    static double xStep = Double.POSITIVE_INFINITY;

    Comparable getSeriesKey(int series) throws Exception {
        assert 0 <= series && series < names.size();
        return names.get(series);
    }

    public static void main(String[] args) throws Exception {
        System.out.println(new MyJVMTest_146().getSeriesKey(series));
    }
}
