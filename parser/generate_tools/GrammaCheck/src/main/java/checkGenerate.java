import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
// import org.apache.commons.io.FileUtils;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.Locale;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URISyntaxException;

public class checkGenerate {

    public static List<String> findSubFolders(File folder) {
        List<String> subFolders = new ArrayList<>();
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    subFolders.add(file.getAbsolutePath());
                }
            }
        }
        return subFolders;
    }

    public static List<String> check(String fileName,String targetFileName) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromStrings(Arrays.asList(fileName));
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
        File rawFile = new File(fileName);
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new FileReader(rawFile));
            reader.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }
        List<String> options = new ArrayList<String>(Arrays.asList("-classpath", "/root/ssfuzz/parser/generate_tools/Dependencies", "-d", "/root/ssfuzz/parser/generate_tools/TmpClasses"));
        compiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits).call();

        // Record the error message.
        List<String> messages = new ArrayList<String>();
        for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {
            String line = diagnostic.getKind() + ":\t Line [" + diagnostic.getLineNumber() + "] \t Position [" + diagnostic.getPosition() + "]\t" + diagnostic.getMessage(Locale.ROOT) + "\n";
            System.out.println(line);
            messages.add(line);
        }

        // If this code is passed, move it to another folder.
        File passFile = new File("/root/ssfuzz/seedData/pass_data/"+targetFileName);
        if(!passFile.exists()){
            passFile.mkdir();
        }

        File targetFile = new File("/root/ssfuzz/seedData/pass_data/"+targetFileName+"/"+fileName.substring(fileName.lastIndexOf("/")+1));
        if(messages.isEmpty()){
            rawFile.renameTo(targetFile);
        } else rawFile.delete();
        return messages;
    }


    public static void main(String[] args){
        int successCount = 0;

        String absolutePath = "/root/ssfuzz";

        String relativePath = "seedData/test_case/";

        File combinedPath = new File(absolutePath, relativePath);

        File rootFolder = combinedPath;
        List<String> javaFiles = findSubFolders(rootFolder);
//        System.out.println(rootFolder);
        for (String javaFile : javaFiles) {
            int lastIndex = javaFile.lastIndexOf("/");
            String lastElement = javaFile.substring(lastIndex + 1);
            File f = new File(javaFile);
//            System.out.println("checking testcase......");
            String fileList[] = f.list();
            for (int i = 0; i < fileList.length; i++) {
                System.out.println(f + "/" + fileList[i]);
                List<String> message = checkGenerate.check(f + "/" + fileList[i],lastElement);
                if ( message.size() == 0) {
                    successCount = successCount + 1;
                }
            }
            System.out.println("Seeds Grammar pass:"+successCount+ "/" + fileList.length);
        }
    }
}