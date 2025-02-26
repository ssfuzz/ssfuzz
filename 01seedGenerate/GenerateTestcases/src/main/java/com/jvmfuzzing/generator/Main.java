package com.jvmfuzzing.generator;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import java.io.File;
import java.util.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Main{
    static final String MODEL_GENERATED_PATH = "/root/ssfuzz/01seedGenerate/seeds";
    static final String FULL_SEEDS_PATH = "/root/ssfuzz/01seedGenerate/testCases";

    public static void main( String[] args ) throws Exception 
    {
        // Get all files from path.
        GetFiles fileGetter = new GetFiles();
        List<String> testcases = new ArrayList<String>();
        CodeVisitor codeVisitor = new CodeVisitor();

        List<List<String>> methodList = FileOperation.getAllMethods(MODEL_GENERATED_PATH);
        for (int i=1; i<methodList.size();i++){
            try {
                System.out.println(methodList.get(i).get(0));
                CompilationUnit cu = StaticJavaParser.parse(methodList.get(i).get(0));
                String allContent = "";
                String newNum = methodList.get(i).get(1);
                int newnum;
                try {
                    newnum = Integer.parseInt(newNum);
                } catch (NumberFormatException e) {
                    newnum = 0; // Default value if conversion fails
                }
                // TODO: 为什么用数据库
                String testcaseContent = codeVisitor.createTestcase(cu, newnum);
                testcases.add(testcaseContent);
                File directory = new File(FULL_SEEDS_PATH);
                if (!directory.exists()) {
                    if (directory.mkdirs()) {
                        System.out.println("Directories created: " + FULL_SEEDS_PATH);
                    } else {
                        System.err.println("Failed to create directories: " + FULL_SEEDS_PATH);
                        return;
                    }
                }
                fileGetter.overWriteFile(testcaseContent, FULL_SEEDS_PATH + "/MyJVMTest_"+newNum+".java");

            }catch (Exception e){
                 e.printStackTrace();
             }
            break;
        }
         System.out.println( "has parameter:"+codeVisitor.hasParaNum+
                             " no parameter:"+codeVisitor.noParaNum+
                             " failfully parse:"+codeVisitor.failNum+
                             " call itself:"+codeVisitor.callItselfNum+
                             " totally:"+methodList.size());
    }
}