package com.jvmfuzzing.generator;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import java.util.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Main {
    public static void main(String[] args) throws Exception {
        // Get all files from path.
        GetFiles fileGetter = new GetFiles();
        // List<String> files = new ArrayList<String>();
        // files = fileGetter.getFiles("/JVMfuzzing/htm/java_code/v4");
        // Get all files from database.
        List<String> testcases = new ArrayList<String>();
        CodeVisitor codeVisitor = new CodeVisitor();
        DBOperation dbOperation = new DBOperation();
        List<List<String>> files = dbOperation.getAllMethods();
        List<String> methodList = files.get(0);
        List<String> idList = files.get(1);
        System.out.println(methodList.size());
        for (int i = 1; i < methodList.size(); i++) {
            try {
                // System.out.println(files.get(i));
                CompilationUnit cu = StaticJavaParser.parse(methodList.get(i));
                String allContent = "";
                int newNum = i + 1;
                String testcaseContent = codeVisitor.createTestcase(cu, newNum);
                testcases.add(testcaseContent);
                // Write out.
                fileGetter.overWriteFile(testcaseContent, "/root/JVMFuzzing/data/test_case/0/MyJVMTest_" + newNum + ".java");
            } catch (ParseProblemException e) {
                // Catch ParseProblemException and handle it
                System.out.println("Caught ParseProblemException:");
                System.out.println(e.getMessage()); // Output exception message
            } catch (AssertionError e) {
                System.out.println("AssertionError: " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                dbOperation.updateTableFunction(idList.get(i));
            }
            // break;
        }
        System.out.println("has parameter:" + codeVisitor.hasParaNum +
                           " no parameter:" + codeVisitor.noParaNum +
                           " failfully parse:" + codeVisitor.failNum +
                           " call itself:" + codeVisitor.callItselfNum +
                           " totally:" + files.size());
    }
}