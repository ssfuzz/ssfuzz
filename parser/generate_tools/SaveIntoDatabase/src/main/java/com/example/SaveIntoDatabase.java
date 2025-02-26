package com.example;

import java.util.*;
import java.sql.*;
import java.io.*;


public class SaveIntoDatabase {
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://127.0.0.1:10348/ssfuzz";

    static final String USER = "root";
    static final String PASS = "root";

    public static List<String> getFiles(String path) {
        List<String> fileList = new ArrayList<String>();
        File file = new File(path);
        File[] tempList = file.listFiles();
        for (int i = 0; i < tempList.length; i++) {
            if (tempList[i].isFile()) {
                fileList.add(tempList[i].toString());
            }
            if (tempList[i].isDirectory()) {
                String newPath = path + "/" + tempList[i].getName();
                List<String> moreFileList = new ArrayList<String>();
                moreFileList = getFiles(newPath);
                fileList.addAll(moreFileList);
            }
        }
        return fileList;
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

    public static Connection createConnection(){
        Connection conn = null;
        try{
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL,USER,PASS);
        }catch(Exception e){
            e.printStackTrace();
        }
        return conn;
    }

    public static Statement createStatement(Connection conn){
        Statement stmt = null;
        try{
            stmt = conn.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stmt;
    }

    public void insertManyIntoTable(String sql, List<String> textList) {
        Connection conn = createConnection();
        Statement stmt = createStatement(conn);
        try{
            conn.setAutoCommit(false);
            PreparedStatement pstmt = conn.prepareStatement(sql);
            for (int i = 0; i < textList.size(); i++) {
                pstmt.clearParameters();
                pstmt.setString(1, textList.get(i));
                pstmt.execute();
                if (i % 1000 == 0) {
                    conn.commit();
                }
            }
            conn.commit();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int searchMaxSourceid(){
        Connection conn = createConnection();
        Statement stmt = createStatement(conn);
        int maxSourceFunId = 0;
        try {
            String sql = "SELECT MAX(SourceFun_id) FROM Table_Function";
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                maxSourceFunId = rs.getInt(1);
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return maxSourceFunId;
    }

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

    private static void clearFolder(String folderPath) {
        File folder = new File(folderPath);
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        boolean success = file.delete();
                        if (success) {
//                            System.out.println("File deleted successfully: " + file.getAbsolutePath());
                        } else {
                            System.out.println("Failed to delete file: " + file.getAbsolutePath());
                        }
                    }
                }
            }
        }
//        System.out.println("Folder cleared: " + folderPath);
    }

    public static void main(String[] args) {
        File rootFolder = new File("/root/ssfuzz/seedData/seeds");
        List<String> javaFiles = findSubFolders(rootFolder);
        int maxSourceid = searchMaxSourceid();
        for (String javaFile : javaFiles) {
            List<String> fileList = getFiles(javaFile);
            List<String> textList = new ArrayList<String>();
            for(String file:fileList){
                textList.add(String.join("\n",readText(file)));
            }
            String sql = String.format("insert into Table_Function (Function_content, SourceFun_id, Mutation_method, Mutation_times) values (?, %s, 0, 0)", maxSourceid+1);
            new SaveIntoDatabase().insertManyIntoTable(sql, textList);
            clearFolder(javaFile);
        }
    }
}