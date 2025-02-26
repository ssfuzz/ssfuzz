import java.util.ArrayList;
import java.util.List;

public class MyJVMTest_151 {

    static List<String> urls = new ArrayList<String>();

    List<String> getUrlProtocol(List<String> urls) throws Exception {
        List<String> urladds = new ArrayList<String>();
        for (String url : urls) {
            StringBuffer urlPath = new StringBuffer();
            if (url.indexOf("http") == -1 && url.indexOf("https") == -1) {
                urlPath.append("http:");
                urlPath.append(url);
            } else {
                urlPath.append(url);
            }
            String urlstr = urlPath.toString();
            if (urlstr.endsWith("/")) {
                urlstr = urlstr.substring(0, urlstr.length() - 1);
            }
            urladds.add(urlstr);
        }
        return urladds;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(new MyJVMTest_151().getUrlProtocol(urls));
    }
}
