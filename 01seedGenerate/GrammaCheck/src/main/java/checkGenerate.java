import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

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

public class checkGenerate {

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

    public static List<String> check(String fileName, String targetFileName) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromStrings(Arrays.asList(fileName));
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        File rawFile = new File(fileName);

        // Read the file (optional logic to be implemented if needed)
        try (BufferedReader reader = new BufferedReader(new FileReader(rawFile))) {
            // Do nothing; placeholder for potential operations on the file
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Compile the Java file and collect diagnostics
        List<String> options = Arrays.asList("-classpath", "./Dependencies", "-d", "./TmpClasses");
        compiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits).call();

        // Record the error messages
        List<String> messages = new ArrayList<>();
        for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {
            String line = diagnostic.getKind() + ":\t Line [" + diagnostic.getLineNumber() + "] \t Position [" + diagnostic.getPosition() + "]\t" + diagnostic.getMessage(Locale.ROOT);
            messages.add(line);
        }

        // Move passed files to the target folder
        File passDir = new File("./pass_data/" + targetFileName);
        if (!passDir.exists() && !passDir.mkdirs()) {
            System.err.println("Failed to create pass directory: " + passDir.getAbsolutePath());
        }

        if (messages.isEmpty()) {
            File targetFile = new File(passDir, rawFile.getName());
            if (!rawFile.renameTo(targetFile)) {
                System.err.println("Failed to move file: " + rawFile.getAbsolutePath());
            }
        }
        return messages;
    }

    public static void main(String[] args) {
        String rootDir = args.length > 0 ? args[0] : "/root/ssfuzz/01seedGenerate/seeds";
        File rootFolder = new File(rootDir);
        if (!rootFolder.exists() || !rootFolder.isDirectory()) {
            System.err.println("Invalid root directory: " + rootDir);
            return;
        }

        File latestFolder = findLatestFolder(rootFolder);
        if (latestFolder == null) {
            System.err.println("No subfolders found in: " + rootFolder.getAbsolutePath());
            return;
        }
        System.out.println("Processing folder: " + latestFolder.getAbsolutePath());

        File[] iterationFolders = latestFolder.listFiles(File::isDirectory);
        if (iterationFolders == null || iterationFolders.length == 0) {
            System.err.println("No iteration folders found in: " + latestFolder.getAbsolutePath());
            return;
        }

        int totalPassed = 0;
        for (File iterationFolder : iterationFolders) {
            File[] javaFiles = iterationFolder.listFiles((dir, name) -> name.endsWith(".java"));
            if (javaFiles == null || javaFiles.length == 0) {
                System.out.println("No .java files found in: " + iterationFolder.getAbsolutePath());
                continue;
            }

            for (File javaFile : javaFiles) {
                List<String> messages = check(javaFile.getAbsolutePath(), latestFolder.getName());
                if (messages.isEmpty()) {
                    totalPassed++;
                }
            }
        }
        System.out.println("Total passed files: " + totalPassed);
    }
}
