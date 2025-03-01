package org.example;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

public class RecordLog {
    private static final String DATE_PATTERN_FULL = "yyyy-MM-dd HH:mm";
    public static Logger logger = Logger.getLogger("log");
    private FileHandler fileHandler = null;
    private String filepath = "/root/ssfuzz/feature/src/test/log/logTest.txt";
    public RecordLog(){
        logger.setLevel(Level.INFO);
        try {
            File file = new File(filepath);
            if(file.exists()) {
                file.delete();
            }
            file.createNewFile();
            fileHandler = new FileHandler(filepath,true);
            fileHandler.setLevel(Level.INFO);
            fileHandler.setFormatter(new Formatter() {
                @Override
                public String format(LogRecord record) {
                    return "[ " + getCurrentDateStr(DATE_PATTERN_FULL) + " - Level:"
                            + record.getLevel().getName().substring(0, 1) + " ]-" + record.getMessage() + "\n";
                }
            });
            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getCurrentDateStr(String pattern) {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }

    public void read(Exception e) {
        StringBuffer emsg = new StringBuffer();
        if(e!=null){
            StackTraceElement st = e.getStackTrace()[0];
            String exclass = st.getClassName();
            String method = st.getMethodName();
            emsg.append("[class:" + exclass + "]call"+ method + "No." + st.getLineNumber()
                    + "error type:" + e.toString()+"\r\n");
        }
        logger.info(emsg.toString());
    }

    public void readAssertion(AssertionError e) {
        StringBuffer emsg = new StringBuffer();
        if(e!=null){
            StackTraceElement st = e.getStackTrace()[0];
            String exclass = st.getClassName();
            String method = st.getMethodName();
            emsg.append("[class:" + exclass + "]call"+ method + "No." + st.getLineNumber()
                    + "error type:" + e.toString()+"\r\n");
        }
        logger.info(emsg.toString());
    }

    public static void readAction(String str) {
        logger.info(str);
    }

}

