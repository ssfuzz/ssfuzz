package org.example.generator;

import org.example.JDBCUtils;

import java.sql.*;
import java.util.*;

public class VariableGenerater {
    static CodeVisitor codeVisitor = new CodeVisitor();
    static PrimitiveGenerator primitiveGenerator = new PrimitiveGenerator();
    static GroupGenerator groupGenerator = new GroupGenerator();
    static UnprimitiveGenerator unprimitiveGenerator = new UnprimitiveGenerator();
    static List<String> implementationClassList = new ArrayList();
    static Connection conn;

    static {
        try {
            conn = JDBCUtils.getConnection();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    static Statement stmt;

    static {
        try {
            stmt = conn.createStatement();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public VariableGenerater() {
    }

    public int strCount(String string, String subString) {
        int count;
        int index;
        for(count = 0; string.indexOf(subString) != -1; string = string.substring(index + 1)) {
            ++count;
            index = string.indexOf(subString);
        }

        return count;
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
    public String generateValueOfDec2(InsertVal curObj,String type){
        String needvarList = "";
        String result = "";
        String iid = "";
        try {
            String query = "SELECT * FROM variables WHERE vari_type = '" + type + "' ORDER BY RAND() LIMIT 1;";
            ResultSet rs = stmt.executeQuery(query);
            List res = convertList2(rs);
            System.out.println("res :  "+res);
            if(!res.isEmpty()) {
                Map m = (Map) res.get(0);
                needvarList = (String) m.get("varList");
                iid = m.get("id").toString();
                HashMap valList = new HashMap<>();
                if (needvarList != null && needvarList.length() > 0 && !needvarList.equals("null")) {
                    valList = new JDBCUtils().getneedfulparmList(needvarList);
                    curObj.tempMutatorList.add("Variables");
                    curObj.tempMutatorList.add(iid);
                }
                curObj.name_and_type_updateNeedSetValName.add(valList);
                result = (String) m.get("vari_value");
                System.out.println("database's data : "+result);
            }else {
                System.out.println("need type : "+type);
                result = this.generateValueOfDec(type);
            }
            rs.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }
    public String generateValueOfDec(String type) {
        String value = "";
        String elemType = "";
        if (primitiveGenerator.isArray(type)) {
            int count = this.strCount(type, "[]");
            elemType = type.substring(0, type.length() - 2 * count);
            value = primitiveGenerator.generateArray(count, elemType);
        } else if (primitiveGenerator.isBoolean(type)) {
            value = primitiveGenerator.generateBoolean();
        } else if (primitiveGenerator.isIntegral(type)) {
            value = primitiveGenerator.generateIntegral(type);
        } else if (primitiveGenerator.isFloatingPoint(type)) {
            value = primitiveGenerator.generateFloatingPoint(type);
        } else if (primitiveGenerator.isString(type)) {
            value = primitiveGenerator.generateString();
        } else if (groupGenerator.isGroupType(type)) {
            GroupGenerator.GroupInfo groupInfo = groupGenerator.generateGroupInit(type);
            value = groupInfo.getValue();
            String implementationClass = groupInfo.getImplementationClass();
            if (!implementationClassList.contains(implementationClass)) {
                implementationClassList.add(groupInfo.getImplementationClass());
            }
        } else {
            value = "null";
        }

        return value;
    }

    public List<VariableComponent> generateDec(InsertVal curObj, String name, String type) {
        //
        List<VariableComponent> decList = new ArrayList();
        if (groupGenerator.isGroupType(type)) {
            decList.addAll(groupGenerator.generateGroup(type, name, "param"));
        } else if (!primitiveGenerator.isArray(type) && !primitiveGenerator.isIntegral(type) && !primitiveGenerator.isFloatingPoint(type) && !primitiveGenerator.isString(type) && !primitiveGenerator.isBoolean(type)) {
            decList.addAll(unprimitiveGenerator.generateUnprimitive(curObj,name, type));
        } else {
            String value = "";
            if(!type.equals("boolean")) {
                boolean choose = true;
                if(choose) {
                    value = this.generateValueOfDec2(curObj,type);
                }
                else value = this.generateValueOfDec(type);
            }
            else{
                value = this.generateValueOfDec(type);
            }
            decList.add(new VariableComponent(type, name, value));
        }
        return decList;
    }
}
