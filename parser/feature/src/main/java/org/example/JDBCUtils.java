package org.example;

import org.example.generator.ConstructorInfo;
import org.example.generator.InsertVal;

import java.io.InputStream;
import java.sql.*;
import java.util.*;

public class JDBCUtils {
    public static int id = 1;
    List<String> needfulparmList = new ArrayList();
    List<String> ids = new ArrayList<>();
    List<String> imps = new ArrayList<>();
    List<HashMap<String, String>> needsetVarName_list = new ArrayList();
    String needforString = "";

    public static Connection getConnection() throws Exception {

        Properties properties = new Properties();
        try (InputStream input = JDBCUtils.class.getClassLoader().getResourceAsStream("jdbc.properties")) {
            if (input == null) {
                throw new Exception("Unable to find jdbc.properties");
            }
            properties.load(input);
        }

        String driverClass = properties.getProperty("driverClass");
        String url = properties.getProperty("url");
        String user = properties.getProperty("user");
        String password = properties.getProperty("password");

        Class.forName(driverClass);

        return DriverManager.getConnection(url, user, password);
    }

    public static List query(String sql) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List list = null;
        try {
            long start = System.currentTimeMillis();
            conn = JDBCUtils.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            list = convertList(rs);
            long end = System.currentTimeMillis();
            System.out.println(end - start + "ms");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JDBCUtils.closeResource(conn, ps);
        }
        return list;
    }

    public static void query2(String sql) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List list = null;
        try {
            long start = System.currentTimeMillis();
            conn = JDBCUtils.getConnection();
            ps = conn.prepareStatement(sql);
            ps.execute();
            long end = System.currentTimeMillis();
            System.out.println(end - start + "ms");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JDBCUtils.closeResource(conn, ps);
        }
    }

    public static List convertList(ResultSet rs) throws SQLException {
        List list = new ArrayList();
        ResultSetMetaData md = rs.getMetaData();
        int columnCount = md.getColumnCount();
        while (rs.next()) {
            Map rowData = new HashMap();
            for (int i = 1; i <= columnCount; i++) {
                rowData.put(md.getColumnName(i), rs.getObject(i));
            }
            list.add(rowData);
        }
        return list;
    }

    public static void Insert(String sql) {
        Insert(sql, null, false);
    }

    public static void Insert(String sql, List<String[]> vals, boolean hasId) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            long start = System.currentTimeMillis();
            conn = JDBCUtils.getConnection();
            ps = conn.prepareStatement(sql);
            int indexVals = 0;
            while (indexVals < vals.size()) {
                String[] strs = vals.get(indexVals);
                System.out.println("strs: " + Arrays.toString(strs));
                int index = 1;
                int j = 0;
                if (hasId) {
                    ps.setObject(index++, id);
                    id++;
                }
                for (; j < strs.length; j++) {
                    ps.setObject(index++, strs[j]);
                }
                System.out.println("ps: " + ps);

                int result = ps.executeUpdate();
                System.out.println("insert" + result);
                indexVals += 1;
            }
            long end = System.currentTimeMillis();
            System.out.println(end - start + "ms");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JDBCUtils.closeResource(conn, ps);
        }
    }

    public static void closeResource(Connection conn, Statement ps) {
        try {
            if (ps != null)
                ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (conn != null)
                conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void closeResource(Connection conn, Statement ps, ResultSet rs) {
        try {
            if (ps != null)
                ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (conn != null)
                conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (rs != null)
                rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> select(String want_type) {
        List<String> results = new ArrayList();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql0 = "";
            String sql1 = "";
            String sql2 = "";
            String sql3 = "";
            conn = JDBCUtils.getConnection();
            if (want_type.equals("Expression")) {
                sql0 = "select id from Expression";
                sql1 = "select content from Expression";
                sql2 = "select varList from Expression";
                sql3 = "select import_info from Expression";
            } else if (want_type.equals("all")) {
                sql0 = "select id from Statement";
                sql1 = "select content from Statement";
                sql2 = "select varList from Statement";
                sql3 = "select import_info from Statement";
            } else {
                sql0 = "select id from Statement where type=\'" + want_type + "\';";
                sql1 = "select content from Statement where type=\'" + want_type + "\';";
                sql2 = "select varList from Statement where type=\'" + want_type + "\';";
                sql3 = "select import_info from Statement where type=\'" + want_type + "\';";
//                System.out.println(sql0);
            }
//             where type='IfStmt' and id = '12322'
            ps = conn.prepareStatement(sql0);
            rs = ps.executeQuery();
            ResultSetMetaData md = rs.getMetaData();
            int columnSize = md.getColumnCount();
            int i;
            while (rs.next()) {
                for (i = 1; i <= columnSize; ++i) {
                    this.ids.add(rs.getObject(i).toString());
                }
            }


            ps = conn.prepareStatement(sql1);
            rs = ps.executeQuery();
            md = rs.getMetaData();
            columnSize = md.getColumnCount();
            while (rs.next()) {
                for (i = 1; i <= columnSize; ++i) {
                    results.add(rs.getObject(i).toString());
                }
            }

            ps = conn.prepareStatement(sql2);
            rs = ps.executeQuery();
            md = rs.getMetaData();
            columnSize = md.getColumnCount();
            while (rs.next()) {
                for (i = 1; i <= columnSize; ++i) {
                    this.needfulparmList.add(rs.getObject(i).toString());
                }
            }

            ps = conn.prepareStatement(sql3);
            rs = ps.executeQuery();
            md = rs.getMetaData();
            columnSize = md.getColumnCount();
            while (rs.next()) {
                for (i = 1; i <= columnSize; ++i) {
                    if (rs.getObject(i) == null) {
                        this.imps.add(null);
                    } else this.imps.add(rs.getObject(i).toString());
                }
            }
        } catch (ClassNotFoundException var31) {
            var31.printStackTrace();
        } catch (Exception var32) {
            var32.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception var30) {
                var30.printStackTrace();
            }

            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (Exception var29) {
                var29.printStackTrace();
            }

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception var28) {
                var28.printStackTrace();
            }

        }

        return results;
    }

    public List<String> getIds() {
        return this.ids;
    }

    public List<String> getImps() {
        return this.imps;
    }

    public List<HashMap<String, String>> getNeedfHashMaps() {
        Iterator var1 = this.needfulparmList.iterator();

        while (var1.hasNext()) {
            String context = (String) var1.next();
            this.needsetVarName_list.add(this.getneedfulparmList(context));
        }

        return this.needsetVarName_list;
    }

    private static int getNum(String originStr, String targetStr) {
        int res = 0;
        int i = originStr.indexOf(targetStr);
        while (i != -1) {
            i = originStr.indexOf(targetStr, i + 1);
            res++;
        }
        return res;
    }


    public HashMap<String, String> getneedfulparmList(String context) {
        HashMap<String, String> needsetVarName = new HashMap();
        this.needforString = context;
        this.needforString = this.needforString.substring(1);
        this.needforString = this.needforString.substring(0, this.needforString.length() - 1);
        String[] arr1 = this.needforString.split(",");

        List<String> temp = new ArrayList<>();
        String temp2 = "";
        for (int index = 0; index < arr1.length; index++) {
            if (!temp2.equals("")) temp2 += ',';
            temp2 += arr1[index];
            int count1 = getNum(temp2, "<");
            int count2 = getNum(temp2, ">");
            if (count1 == count2) {
                temp.add(temp2);
                temp2 = "";
            }
        }
        for (int index = 0; index < temp.size(); ++index) {
            if (index % 2 != 0) {
                needsetVarName.put(temp.get(index - 1).trim(), temp.get(index).trim());
            }
        }
        return needsetVarName;
    }

    private List convertList2(ResultSet rs) throws SQLException {
        List list = new ArrayList();
        ResultSetMetaData md = rs.getMetaData();
        int columnCount = md.getColumnCount();
        while (rs.next()) {
            Map rowData = new HashMap();
            for (int i = 1; i <= columnCount; i++) {
                rowData.put(md.getColumnName(i), rs.getObject(i));
            }
            list.add(rowData);
        }
        return list;
    }

    public ConstructorInfo getConstructorInfo2(InsertVal curObj, String className) {
        Statement stmt = null;
        Connection conn = null;
        String needvarList = "";
        String result = "";
        String packageName = "";
        String iid = "";
        ConstructorInfo constructorInfo = null;
        try {
            conn = JDBCUtils.getConnection();
            stmt = conn.createStatement();
            String query = "SELECT * FROM variables WHERE vari_type = '" + className + "' ORDER BY energy DESC LIMIT 1;";
            ResultSet rs = stmt.executeQuery(query);
            List res = convertList2(rs);
            if (res.size() > 1) {
                Random r = new Random();
                int setindex = 0;
                if (res.size() > 1)
                    setindex = r.nextInt(res.size() - 1);
                Map m = (Map) res.get(setindex);
                needvarList = (String) m.get("varList");
                HashMap valList = new HashMap<>();
                if (needvarList != null && needvarList.length() > 0 && !needvarList.equals("null")) {
                    valList = getneedfulparmList(needvarList);
                }
                curObj.name_and_type_updateNeedSetValName.add(valList);
                result = (String) m.get("vari_value");
                packageName = (String) m.get("import_info");
                iid = m.get("id").toString();
                curObj.tempMutatorList.add("Variables");
                curObj.tempMutatorList.add(iid);
            }
            constructorInfo = new ConstructorInfo(className, result, packageName);
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return constructorInfo;
    }

    public ConstructorInfo getConstructorInfo(String className) {
        Statement stmt = null;
        Connection conn = null;
        String result = "";
        String implementingClassName = "";
        String packageName = "";
        ConstructorInfo constructorInfo = null;

        try {
            conn = JDBCUtils.getConnection();
            stmt = conn.createStatement();
            String query = "SELECT * FROM Constructor_Info WHERE Class_Name = '" + className + "';";
//            System.out.println("query: " + query);
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) {
                String classDesc = rs.getString("Class_Description");
                if (classDesc.contains("abstract class")) {
                    String queryAgain = String.format("SELECT * FROM Constructor_Info WHERE Class_Description like '%%extends %s' and Constructor_Stmt not like '%%(%s%%';", className, className);
                    ResultSet rsAgain = stmt.executeQuery(queryAgain);
                    if (rsAgain.next()) {
                        result = rsAgain.getString("Constructor_Stmt");
                        implementingClassName = rsAgain.getString("Class_Name");
                        packageName = rsAgain.getString("Package_Name");
                    }

                    constructorInfo = new ConstructorInfo(implementingClassName, result, packageName);
                } else {
                    result = rs.getString("Constructor_Stmt");
                    packageName = rs.getString("Package_Name");
                    constructorInfo = new ConstructorInfo(className, result, packageName);
                }
            }

            rs.close();
        } catch (SQLException var21) {
            var21.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                stmt.close();
                conn.close();
            } catch (Exception var20) {
                var20.printStackTrace();
            }

        }

        return constructorInfo;
    }
}
