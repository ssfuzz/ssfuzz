import java.util.Arrays;

public class MyJVMTest_120 {

    static int[] nums = { -1967773400, -483739585, 9, 0, 0, 0, -2121746757, 0, 0, 3 };

    int minMoves2(int[] nums) throws Exception {
        Arrays.sort(nums);
        int move = 0;
        int l = 0, h = nums.length - 1;
        while (l < h) {
            move += nums[h] - nums[l];
            l++;
            h--;
        }
        return move;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(new MyJVMTest_120().minMoves2(nums));
    }
}
