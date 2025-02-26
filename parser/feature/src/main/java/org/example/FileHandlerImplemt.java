package org.example;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.printer.YamlPrinter;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class ClassNameCollector extends VoidVisitorAdapter<List<String>> {
    static int count = 0;
    @Override
    public void visit(ClassOrInterfaceDeclaration n, List<String> collector) {
        super.visit(n, collector);
        collector.add(n.getNameAsString());
    }
}

public class FileHandlerImplemt implements DirExplorer.FileHandler {
    String newPath;
    String parent;

    int EmptyFileCount = 0;
    List<String> emptyFiles = new ArrayList<>();
    int tooLongFileCount = 0;

    List<String> tooLongFiles = new ArrayList<>();
    public static int getFileLineNum(String filePath) {
        try (LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(filePath))){
            lineNumberReader.skip(Long.MAX_VALUE);
            int lineNumber = lineNumberReader.getLineNumber();
            return lineNumber + 1;
        } catch (IOException e) {
            return -1;
        }
    }

    @Override
    public String getNewPath(){
        return this.newPath;
    }
    @Override
    public void setNewPath(String path){
        this.newPath = path;
    }
    @Override
    public String getParent(){
        return this.parent;
    }
    @Override
    public void setParent(String path){
        this.parent = path;
    }
    @Override
    public void handle(String parent,String path,String newPath){
        String newContent = "";
        try {
            long lineNum = getFileLineNum(path);
            if(lineNum > 1000){
                tooLongFileCount+=1;
                tooLongFiles.add(path);
                RecordLog.readAction(String.valueOf(tooLongFileCount));
                RecordLog.readAction("tooLongFile:"+path);
                return;
            }
            String con = Files.readString(Paths.get(path));
            List<String> className = new ArrayList<>();
            // Create Compilation.
//            System.out.println(path);
//            System.out.println(con);
            ParserConfiguration parserConfiguration = new ParserConfiguration();
            parserConfiguration.setLexicalPreservationEnabled(true);
            JavaParser javaParser = new JavaParser(parserConfiguration);
            ParseResult<CompilationUnit> result = javaParser.parse(con);
            Optional<CompilationUnit> optionalCompilationUnit = result.getResult();
            if(!optionalCompilationUnit.isPresent()) {
                RecordLog.readAction("EmptyFile"+path);
                RecordLog.readAction(String.valueOf(result));
                EmptyFileCount +=1;
                emptyFiles.add(path);
                return;
            }
            CompilationUnit cu = optionalCompilationUnit.get();
            // Create Visitor.
            ClassNameCollector classNameVisitor = new ClassNameCollector();
            // Visit.
            classNameVisitor.visit(cu,className);
            if(className.size() == 0){
                classNameVisitor.count+=1;
                RecordLog.readAction(String.valueOf(classNameVisitor.count));
                RecordLog.readAction("NoCodeFile:"+path);
                return;
            }
            newContent = con;
        }
        catch (IOException e){
            System.out.println("File not exists. (line 38 in FileHandlerImplemt)");
            e.printStackTrace();
        }
        try {
            if(newContent.equals("")){
                System.out.println("warning:File "+path+" has no content.");
            }else{
                URI path1 = new File(path).toURI();
                URI path2 = new File(parent).toURI();
                URI relativePath = path2.relativize(path1);
                String pathres = relativePath.getPath();
                System.out.println("Relative Path: " + pathres);
                String newfilePath = newPath+"/"+pathres;
                File newf = new File(newfilePath);
                newf.getParentFile().mkdirs();
                Files.writeString(Paths.get(newfilePath),newContent);
            }
        }
        catch (IOException e){
            System.out.println("New file write failure. (line 39 in FileHandlerImplemt)");
            e.printStackTrace();
        }
    }

    public static List<Node> parseOneFile(CompilationUnit cu) {
        NodeList<TypeDeclaration<?>> types = cu.getTypes();
        NodeHandlerImplemt nh = new NodeHandlerImplemt();
        NodeIterator ni = new NodeIterator(nh);
        for (TypeDeclaration<?> type : types) {
            System.out.println("## " + type.getName());
            NodeList<BodyDeclaration<?>> members = type.getMembers();
            System.out.println("members: "+members);
            System.out.println(members.size());
            for(Node n:members) {
                ni.explore(n);
            }
        }
        return ni.getAllChildNodes();
    }
    public void printAST(String con){
        ParseResult<CompilationUnit> result = new JavaParser().parse(con);
        Optional<CompilationUnit> expr = result.getResult();
        expr.ifPresent(YamlPrinter::print);
    }
}
