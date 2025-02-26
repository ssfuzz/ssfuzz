package com.jvmfuzzing.generator;

import java.io.IOException;
import java.util.*;
import java.sql.*;
import java.io.InputStream;
import java.util.Properties;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DBOperation { 
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://127.0.0.1:10348/ssfuzz";
    static final String USER = "root";
    static final String PASS = "root";

    public DBOperation() throws IOException {
    }

    public Connection createConnection(){
        Connection conn = null;
        try{
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL,USER,PASS);
        }catch(Exception e){
            e.printStackTrace();
        }
        return conn;
    }

    public void updateTableFunction(String id) throws SQLException {
        Connection conn = null;
        Statement stmt = null;
        try{
            conn = createConnection();
            stmt = conn.createStatement();
            String query = "UPDATE Table_Function SET Mutation_times = Mutation_times + 1 " + "WHERE id = "+ id + ";";
            stmt.executeUpdate(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            conn.close();
        }
    }

    public List<List<String>> getAllMethods() {
        Statement stmt = null;
        Connection conn = null;
        List<String> methodList = new ArrayList<String>();
        List<String> idList = new ArrayList<String>();
        List<List<String>> multipleLists = new ArrayList<>();
        conn = createConnection();
        try{
            stmt = conn.createStatement();
            String query = "SELECT * FROM Table_Function WHERE Mutation_times = 0 AND SourceFun_id = (SELECT MAX(SourceFun_id) FROM Table_Function);";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                methodList.add(rs.getString("Function_content"));
                idList.add(rs.getString("id"));
            }
            multipleLists.add(methodList);
            multipleLists.add(idList);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return multipleLists;
    }
 
    public void insertIntoTable(List<String> contentList) {
        Statement stmt = null;
        Connection conn = null;
        try{
            conn = createConnection();
            stmt = conn.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // String sql;
        try{
            conn.setAutoCommit(false); 
            // PreparedStatement pstmt = conn.prepareStatement("insert into Table_Testcase (Testcase_context) values (?)");
            PreparedStatement pstmt = conn.prepareStatement("insert into Table_Testcase (Testcase_context, SourceFun_id, SourceTestcase_id, Fuzzing_times, Mutation_method, Mutation_times, Interesting_times, Probability) values (?, 0, 0, 0, 0, 0, 0, 0)"); 
            for (int i = 0; i < contentList.size(); i++) {     
                pstmt.clearParameters();     
                pstmt.setString(1, contentList.get(i));     
                pstmt.execute();     
                if (i % 1000 == 0) {         
                    conn.commit();  
                } 
            } 
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }finally{
            // Clean-up environment
            try{
                stmt.close();
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public ConstructorInfo getConstructorInfo(String className) {
        Statement stmt = null;
        Connection conn = null;
        String result = "",implementingClassName = "",packageName = "";
        ConstructorInfo constructorInfo = null;
        // try{
        //     conn = createConnection();
        //     stmt = conn.createStatement();
        // } catch (Exception e) {
        //     e.printStackTrace();
        // }
        try{
            conn = createConnection();
            stmt = conn.createStatement();
            String query = "SELECT * FROM Constructor_Info WHERE Class_Name = '"+className+"';";
            // System.out.println("query:"+query);
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()){
                // result = rs.getString("Constructor_Stmt");
                String classDesc = rs.getString("Class_Description");
                // System.out.println("class desc:"+classDesc);
                if(classDesc.contains("abstract class")){
                    // System.out.println("1");
                    String queryAgain = String.format("SELECT * FROM Constructor_Info WHERE Class_Description like '%%extends %s' and Constructor_Stmt not like '%%(%s%%';",className,className);
                    // System.out.println("query again:"+queryAgain);
                    ResultSet rsAgain = stmt.executeQuery(queryAgain);
                    while(rsAgain.next()){
                        result = rsAgain.getString("Constructor_Stmt");
                        implementingClassName = rsAgain.getString("Class_Name");
                        packageName = rsAgain.getString("Package_Name");
                        break;
                    }
                    constructorInfo = new ConstructorInfo(implementingClassName, result, packageName);
                    break;
                }
                else{                
                    // System.out.println("2");
                    result = rs.getString("Constructor_Stmt");
                    packageName = rs.getString("Package_Name");
                    constructorInfo = new ConstructorInfo(className, result, packageName);
                    break;
                }
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally{
            // Clean-up environment
            try{
                stmt.close();
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return constructorInfo;
    }
}