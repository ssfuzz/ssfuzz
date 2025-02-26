package com.jvmfuzzing.generator;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

import java.io.File;
import java.util.*;


public class GenerateMain{

    public static void deleteFiles(String directoryPath) {
        File directory = new File(directoryPath);

        if (!directory.exists() || !directory.isDirectory()) {
//            System.out.println("Directory does not exist or is not a folder");
            return;
        }

        File[] files = directory.listFiles();

        for (File file : files) {
            if (file.isFile()) {
                if (file.delete()) {
//                    System.out.println("Deleted documents: " + file.getName());
                } else {
//                    System.out.println("Unable to delete file: " + file.getName());
                }
            }
        }
    }

    public static void main( String[] args ) throws Exception 
    {
        // Get all files from path.
        GetFiles fileGetter = new GetFiles();
        // Get all files from database.
        List<String> testcases = new ArrayList<String>();
        CodeVisitor codeVisitor = new CodeVisitor();
        DBOperation dbOperation = new DBOperation();
        List<List<String>> files = dbOperation.getAllMethods();
        List<String> methodList = files.get(0);
        List<String> idList = files.get(1);
        deleteFiles("/root/ssfuzz/seedData/test_case/0");
        for(int i=1;i<methodList.size();i++)
        {
            try{
                CompilationUnit cu = StaticJavaParser.parse(methodList.get(i));
                String allContent = "";
                int newNum = i+1;
                String testcaseContent = codeVisitor.createTestcase(cu, newNum);
                testcases.add(testcaseContent);
                // Write out.
                fileGetter.overWriteFile(testcaseContent, "/root/ssfuzz/seedData/test_case/0/MyJVMTest_"+newNum+".java");
            }catch (ParseProblemException e) {
//                System.out.println("ParseProblemException error:");
//                System.out.println(e.getMessage());
            }catch (AssertionError e) {
//                System.out.println("AssertionError: " + e.getMessage());
            } catch (Exception e){
                e.printStackTrace();
            }finally {
                dbOperation.updateTableFunction(idList.get(i));
            }
        }
//        System.out.println( "has parameter:"+codeVisitor.hasParaNum+
//                            " no parameter:"+codeVisitor.noParaNum+
//                            " failfully parse:"+codeVisitor.failNum+
//                            " call itself:"+codeVisitor.callItselfNum+
//                            " totally:"+files.size());
    }
}