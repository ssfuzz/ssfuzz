package org.example;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CreateJavaFile {
    public CreateJavaFile() {
    }

    public static void createFile(String filePath1, String fileName, String stringBuffer) throws IOException {
        File dir = new File(filePath1);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File checkFile = new File(filePath1 + "/" + fileName);
        if (!checkFile.exists()) {
            System.out.println("CREAT:\t" + checkFile.toString());
            FileWriter writer = null;

            try {
                if (!checkFile.exists()) {
                    checkFile.createNewFile();
                }

                writer = new FileWriter(checkFile, true);
                writer.append(stringBuffer);
                writer.flush();
            } catch (IOException var11) {
                var11.printStackTrace();
            } finally {
                if (null != writer) {
                    writer.close();
                }

            }
        }else{
            FileWriter writer  = new FileWriter(checkFile, false);
            writer.append(stringBuffer);
            writer.flush();
        }

    }
}
