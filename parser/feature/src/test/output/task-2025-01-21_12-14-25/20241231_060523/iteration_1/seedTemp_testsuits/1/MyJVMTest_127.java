import java.util.Arrays;

public class MyJVMTest_127 {

    static long numBits = 9223372036854775807L;

    static long numValues = 594806746972050011L;

    static long upperBound = -9223372036854775808L;

    static int numLowBits = 0;

    static long lowerBitsMask = 9223372036854775807L;

    static long[] upperLongs = { 9223372036854775807L, -9223372036854775808L, 0, 9223372036854775807L, -9223372036854775808L, 9223372036854775807L, -9223372036854775808L, 9223372036854775807L, -9223372036854775808L, 9223372036854775807L };

    static long[] lowerLongs = { 310536198300638551L, -9223372036854775808L, -9223372036854775808L, 9223372036854775807L, -9223372036854775808L, -9223372036854775808L, -9223372036854775808L, -8158737034302214507L, 8653750230176749115L, 9223372036854775807L };

    static int LOG2_LONG_SIZE = Long.numberOfTrailingZeros(Long.SIZE);

    static long DEFAULT_INDEX_INTERVAL = 256;

    static long numIndexEntries = 9223372036854775807L;

    static long indexInterval = -9169156638787305553L;

    static int nIndexEntryBits = 0;

    static long[] upperZeroBitPositionIndex = { 9223372036854775807L, 5645015601614047038L, 3811401871674357232L, 0, 9223372036854775807L, -9223372036854775808L, -9223372036854775808L, 0, 9223372036854775807L, -1620726019440786600L };

    static long currentEntryIndex = 9223372036854775807L;

    long numLongsForBits(long numBits) throws Exception {
        assert numBits >= 0 : numBits;
        return (numBits + (Long.SIZE - 1)) >>> LOG2_LONG_SIZE;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(new MyJVMTest_127().numLongsForBits(numBits));
    }
}
