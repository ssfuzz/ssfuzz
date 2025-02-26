public class MyJVMTest_201363 {

    private void execute(String args, String msgKey) throws Exception {

        String[] safeArgs = Stream.concat(commonArgs.stream(),
                Stream.of(args.split("\\s+"))).filter(s -> {
            if (s.contains(" ")) {
                throw new RuntimeException("No spaces in args");
            }
            return !s.isEmpty();
        }).toArray(String[]::new);

    }
}
