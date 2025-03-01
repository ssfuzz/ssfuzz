package com.jvmfuzzing.generator;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.sql.*;
 
public class DBOperation {
    private static final Properties props = new Properties();
    static {
        try (InputStream input = DBOperation.class.getClassLoader().getResourceAsStream("db-config.properties")) {
            if (input == null) {
                throw new IOException("can't find  'db-config.properties'");
            }
            props.load(input);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("load file wrong: ", e);
        }
    }

    public static String getJdbcDriver() {
        return props.getProperty("jdbc.driver");
    }

    public static String getDbUrl() {
        return props.getProperty("db.url");
    }

    public static String getUser() {
        return props.getProperty("db.user");
    }

    public static String getPassword() {
        return props.getProperty("db.password");
    }

    static final String JDBC_DRIVER = getJdbcDriver();
    static final String DB_URL = getDbUrl();
    static final String USER = getUser();
    static final String PASS = getPassword();



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
            String query = "SELECT * FROM Table_Function where Mutation_times=0;";
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