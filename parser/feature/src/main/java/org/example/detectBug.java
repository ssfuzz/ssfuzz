package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class detectBug {

    public static String prerm(String filePath) throws IOException {
        String content = Files.readString(Paths.get(filePath));
        String result = CommentsRemover.doAction(content, "");
        result = result.trim();
        if (result.length() == 0) System.out.println("empty file");
        return result;
    }

    public static String getAdd(String fileDetect) {
        String addstring = "";
        try (BufferedReader br = new BufferedReader(new FileReader(fileDetect))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("+")) {
                    if (line.substring(1).trim().startsWith("*")) continue;
                    addstring = addstring + line.substring(1).trim() + "\n";
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return addstring;
    }

    public static List<List<String[]>> GetDeclaration(String FilesPath) throws Exception {
        RecordLog r = new RecordLog();
        FilterImplemt fi = new FilterImplemt();
        FileHandlerImplemt fhi = new FileHandlerImplemt();
        DirExplorer de = new DirExplorer(fi, fhi);
        List<List<String[]>> res = new ArrayList<>();
        boolean getRelative = true;
        try {
            res = de.searchDeclaration(FilesPath, getRelative);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;

    }

    public static void main(String[] args) throws Exception {
        String getFeaturePath = "/root/ssfuzz/feature/src/test/data/getFeature";
        List<List<String[]>> res = GetDeclaration(getFeaturePath);
        System.out.println("done.");
    }
}
