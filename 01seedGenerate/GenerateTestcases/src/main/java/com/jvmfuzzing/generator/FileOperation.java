package com.jvmfuzzing.generator;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import java.nio.file.Files;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.Locale;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileOperation {


    public static File findLatestFolder(File parentFolder) {
        File[] subFolders = parentFolder.listFiles(File::isDirectory);
        if (subFolders == null || subFolders.length == 0) {
            return null;
        }
        File latestFolder = null;
        long latestModificationTime = Long.MIN_VALUE;

        for (File folder : subFolders) {
            if (folder.lastModified() > latestModificationTime) {
                latestModificationTime = folder.lastModified();
                latestFolder = folder;
            }
        }
        return latestFolder;
    }

//    public static boolean validateClassName(File javaFile) {
//        String fileName = javaFile.getName();
//        String expectedClassName = "MyJVMTest_" + fileName.substring(fileName.indexOf('_') + 1, fileName.lastIndexOf('.'));
//        try (BufferedReader reader = new BufferedReader(new FileReader(javaFile))) {
//            String line;
//            while ((line = reader.readLine()) != null) {
//                if (line.trim().startsWith("public class ")) {
//                    String className = line.split(" ")[2];
//                    return className.equals(expectedClassName);
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return false;
//    }

//    public static List<String> check(String fileName, String targetFileName) {
//        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
//        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
//        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromStrings(Arrays.asList(fileName));
//        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
//        File rawFile = new File(fileName);
//
//        // Validate class name consistency
//        if (!validateClassName(rawFile)) {
//            return List.of("Class name in the file does not match the file name");
//        }
//
//        // Compile the Java file and collect diagnostics
//        List<String> options = Arrays.asList("-classpath", "./Dependencies", "-d", "./TmpClasses");
//        compiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits).call();
//
//        // Record the error messages
//        List<String> messages = new ArrayList<>();
//        for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {
//            String line = diagnostic.getKind() + ":\t Line [" + diagnostic.getLineNumber() + "] \t Position [" + diagnostic.getPosition() + "]\t" + diagnostic.getMessage(Locale.ROOT);
//            System.out.println(line);
//            messages.add(line);
//        }
//
//        // Move passed files to the target folder
//        File passDir = new File("./pass_data/" + targetFileName);
//        if (!passDir.exists() && !passDir.mkdirs()) {
//            System.err.println("Failed to create pass directory: " + passDir.getAbsolutePath());
//        }
//
//        if (messages.isEmpty()) {
//            File targetFile = new File(passDir, rawFile.getName());
//            if (!rawFile.renameTo(targetFile)) {
//                System.err.println("Failed to move file: " + rawFile.getAbsolutePath());
//            }
//        }
//        System.out.println(messages);
//        return messages;
//    }

    private static String extractIdFromFileName(String fileName) {
        // Assuming file name format is "MyJVMTest_21.java", extract "21"
        int underscoreIndex = fileName.lastIndexOf('_');
        int dotIndex = fileName.lastIndexOf('.');
        if (underscoreIndex != -1 && dotIndex != -1 && dotIndex > underscoreIndex) {
            return fileName.substring(underscoreIndex + 1, dotIndex);
        }
        return "";
    }

    public static List<String> readText(String filename){
        BufferedReader reader;
        List<String> textContent = new ArrayList<String>();
        try {
            reader = new BufferedReader(new FileReader(filename));
            String line = reader.readLine();
            textContent.add(line);
            while (line != null) {
                line = reader.readLine();
                textContent.add(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        textContent.remove(textContent.size()-1);
        return textContent;
    }

    public static List<List<String>> getAllMethods(String MODEL_GENERATED_PATH){
        List<List<String>> result =  new ArrayList<>();
        File rootFolder = new File(MODEL_GENERATED_PATH);

            System.err.println("Invalid root directory: " + MODEL_GENERATED_PATH);
            return result;
        }
        File latestFolder = FileOperation.findLatestFolder(rootFolder);
        if (latestFolder == null) {
            System.err.println("No subfolders found in: " + rootFolder.getAbsolutePath());
            return result;
        }
        System.out.println("latestFolder: "+latestFolder);
        System.out.println("Processing folder: " + latestFolder.getAbsolutePath());
        File[] iterationFolders = latestFolder.listFiles(File::isDirectory);
        if (iterationFolders == null || iterationFolders.length == 0) {
            System.err.println("No iteration folders found in: " + latestFolder.getAbsolutePath());
            return result;
        }
        
        for (File iterationFolder : iterationFolders) {
            File[] javaFiles = iterationFolder.listFiles((dir, name) -> name.endsWith(".java"));
            if (javaFiles == null || javaFiles.length == 0) {
                System.out.println("No .java files found in: " + iterationFolder.getAbsolutePath());
                continue;
            }

            for (File javaFile : javaFiles){
                try{
                    String content = String.join("\n",readText(String.valueOf(javaFile.toPath())));
                    String fileName = javaFile.getName();
                    String id = extractIdFromFileName(fileName);
                    List<String> fileData = new ArrayList<>();
                    fileData.add(content);
                    fileData.add(id);
                    result.add(fileData);
                }catch (Exception e){
                    System.out.println("Failed to read file : " + javaFile.getAbsolutePath());
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

}
