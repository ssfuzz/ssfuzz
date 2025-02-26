package org.example;
import java.nio.charset.MalformedInputException;

import cn.hutool.aop.interceptor.SpringCglibInterceptor;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.printer.YamlPrinter;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import org.dom4j.DocumentException;
import org.yaml.snakeyaml.Yaml;
import java.io.FileNotFoundException;

import javax.swing.*;
import java.io.*;
import java.nio.file.*;
import java.sql.Statement;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DirExplorer {
    public interface FileHandler {
        void setNewPath(String path);

        String getNewPath();

        void setParent(String path);

        String getParent();

        void handle(String parent, String path, String newPath);

        void printAST(String con);
    }

    public interface Filter {
        boolean interested(String path);
    }

    private FileHandler fileHandler;
    private Filter filter;
    private List<Integer> used_id1 = new ArrayList<>();
    private List<Integer> used_id2 = new ArrayList<>();
    private List<Integer> used_id3 = new ArrayList<>();
    private List<HashMap> db_info = JDBCUtils.query("select * from variables;");
    private Map<String, List<String>> class_method_info = get_Method_info("select Class_Name,Method_Name from Method_Info;");
    private int declarationCount = 0;
    private HashMap<String, Integer> declarationDetailCount = new HashMap<>();
    private HashMap<String, Integer> typeDetailCount = new HashMap<>();
    private HashMap<String, Integer> expAndTypeDetailCount = new HashMap<>();

    private List<String> aviLists = new ArrayList(Arrays.asList("UnaryOperator", "BinaryOperator", "IntUnaryOperator", "IntBinaryOperator", "LongUnaryOperator", "LongBinaryOperator", "DoubleUnaryOperator", "DoubleBinaryOperator", "Runnable"));

    public DirExplorer(FilterImplemt filter, FileHandlerImplemt fileHandler) {
        this.filter = filter;
        this.fileHandler = fileHandler;
    }

    public void explore(String parentDir, File root, String targetDir) {
        fileHandler.setNewPath(targetDir);
        fileHandler.setParent(parentDir);
        explore(0, parentDir, root);
    }

    private void explore(int level, String path, File file) {
        if (level >= 10) return;
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                explore(level + 1, path + "/" + child.getName(), child);
            }
        } else {
            if (filter.interested(path)) {
                String newPath = fileHandler.getNewPath();
                String parent = fileHandler.getParent();
                fileHandler.handle(parent, path, newPath);
            }
        }
    }

    public List<String> deal(String key, JSONObject init, List<String> rela_names) {
        String startSubStr = "";
        if (key.startsWith("initializer(Type="))
            startSubStr = "initializer(Type=";
        else if (key.startsWith("expression(Type="))
            startSubStr = "expression(Type=";
        else if (key.startsWith("inner(Type="))
            startSubStr = "inner(Type=";
        else if (key.startsWith("dimension(Type="))
            startSubStr = "dimension(Type=";
        else if (key.startsWith("value(Type="))
            startSubStr = "value(Type=";
        else if (key.startsWith("name(Type="))
            startSubStr = "name(Type=";
        else if (key.startsWith("index(Type="))
            startSubStr = "index(Type=";
        else if (key.startsWith("argument(Type="))
            startSubStr = "argument(Type=";
        else if (key.startsWith("scope(Type="))
            startSubStr = "scope(Type=";
        else if (key.startsWith("condition(Type="))
            startSubStr = "condition(Type=";
        else if (key.startsWith("elseExpr(Type="))
            startSubStr = "elseExpr(Type=";
        else if (key.startsWith("thenExpr(Type="))
            startSubStr = "thenExpr(Type=";

        int start = startSubStr.length();
        int end = key.length() - 1;
        String type = key.substring(start, end);
        //basic type
        if (type.contains("IntegerLiteralExpr") || type.contains("StringLiteralExpr") ||
                type.contains("BooleanLiteralExpr") || type.contains("CharLiteralExpr") || type.contains("DoubleLiteralExpr")
                || type.contains("NullLiteralExpr") || type.contains("TextBlockLiteralExpr") || type.contains("ThisExpr")) {
            return rela_names;
        } else if (type.contains("NameExpr")) {
            //eg. a = b+3;
            //name-identifier
            rela_names.add((String) ((JSONObject) init.get("name(Type=SimpleName)")).get("identifier"));
            return rela_names;
        } else if (type.contains("BinaryExpr")) {
            // eg. "a"+"b" (a*b)+c
            List<String> new_list = new ArrayList<>();
            for (String key2 : init.keySet()) {
                if (key2.contains("left(")) {
                    //left
                    deal(key2, (JSONObject) init.get(key2), rela_names);
                } else if (key2.contains("right(")) {
                    //right
                    deal(key2, (JSONObject) init.get(key2), rela_names);
                }
            }
            if (new_list.size() > 0) rela_names.addAll(new_list);
            return rela_names;
        } else if (type.contains("ObjectCreationExpr")) {
            JSONArray args = (JSONArray) init.get("arguments");
            if (args == null) return rela_names;
            for (int i = 0; i < args.size(); i++) {
                JSONObject arg = (JSONObject) args.get(i);
                for (String key2 : arg.keySet())
                    deal(key2, (JSONObject) arg.get(key2), rela_names);
            }
        } else if (type.contains("ArrayCreationExpr")) {
            JSONArray ja = (JSONArray) init.get("levels");
            for (int i = 0; i < ja.size(); i++) {
                JSONObject cur = (JSONObject) ja.get(i);
                cur = (JSONObject) cur.get("level(Type=ArrayCreationLevel)");
                if (cur == null) continue;
                for (String key2 : cur.keySet()) {
                    if (key2.contains("dimension(")) {
                        deal(key2, (JSONObject) cur.get(key2), rela_names);
                        break;
                    }
                }
            }
            for (String key2 : init.keySet()) {
                if (key2.contains("initializer(")) {
                    deal(key2, (JSONObject) init.get(key2), rela_names);
                    break;
                }
            }
            return rela_names;
        } else if (type.contains("MethodCallExpr")) {
            for (String key2 : init.keySet()) {
                if (key2.startsWith("scope(Type=")) {
                    deal(key2, (JSONObject) init.get(key2), rela_names);
                } else if (key2.equals("arguments")) {
                    JSONArray args = (JSONArray) init.get("arguments");
                    for (int i = 0; i < args.size(); i++) {
                        JSONObject arg = (JSONObject) args.get(i);
                        for (String key3 : arg.keySet())
                            deal(key3, (JSONObject) arg.get(key3), rela_names);
                    }
                }
            }
            return rela_names;
        } else if (type.contains("ArrayInitializerExpr")) {
            JSONArray ja = (JSONArray) init.get("values");
            if (ja != null) {
                for (int i = 0; i < ja.size(); i++) {
                    JSONObject jo = (JSONObject) ja.get(i);
                    for (String key3 : jo.keySet()) {
                        deal(key3, (JSONObject) jo.get(key3), rela_names);
                    }
                }
            }
        } else if (type.contains("ArrayAccessExpr")) {
            for (String key2 : init.keySet()) {
                if (key2.startsWith("name(")) {
                    deal(key2, (JSONObject) init.get(key2), rela_names);
                } else if (key2.startsWith("index(")) {
                    deal(key2, (JSONObject) init.get(key2), rela_names);
                }
            }
        } else if (type.contains("CastExpr")) {
            for (String key2 : init.keySet()) {
                if (key2.contains("expression(")) {
                    deal(key2, (JSONObject) init.get(key2), rela_names);
                    break;
                }
            }
            return rela_names;
        } else if (type.contains("EnclosedExpr")) {
            for (String key2 : init.keySet()) {
                if (key2.contains("inner(")) {
                    deal(key2, (JSONObject) init.get(key2), rela_names);
                    break;
                }
            }
            return rela_names;
        } else if (type.contains("FieldAccessExpr")) {
            for (String key2 : init.keySet()) {
                if (key2.startsWith("scope(Type=")) {
                    deal(key2, (JSONObject) init.get(key2), rela_names);
                } else if (key2.startsWith("name")) {
                    rela_names.add((String) ((JSONObject) init.get(key2)).get("identifier"));
                }
            }
//            rela_names.add((String)((JSONObject)init.get("name(Type=SimpleName)")).get("identifier"));
            return rela_names;
        } else if (type.contains("ConditionalExpr")) {
            for (String key2 : init.keySet()) {
                deal(key2, (JSONObject) init.get(key2), rela_names);
            }
        }
        return rela_names;
    }

    public Map<Integer, List<String>> searchBodyWithJudge(Object obj, String filePath, Map<Integer, List<String>> rela_names, List<String[]> vals_info) {
        String type = String.valueOf(obj.getClass());
        if (type.contains("JSONObject")) {
            searchBody((JSONObject) obj, filePath, rela_names, vals_info);
        } else if (type.contains("JSONArray")) {
            for (Object o : (JSONArray) obj)
                searchBodyWithJudge(o, filePath, rela_names, vals_info);
        }
        return rela_names;
    }

    public Map<Integer, List<String>> searchBody(JSONObject obj, String filePath, Map<Integer, List<String>> rela_names, List<String[]> vals_info) {
        JSONObject res1 = null;
        JSONArray res2 = null;
        for (String key : obj.keySet()) {
//            System.out.println(obj.get(key).getClass());
            String type = String.valueOf(obj.get(key).getClass());
            if (type.contains("JSONObject")) {
                if (key.equals("name(Type=SimpleName)") || key.contains("(Type=NameExpr)"))
                    continue;
                if (key.contains("(Type=BlockStmt)")) {
                    res1 = (JSONObject) obj.get(key);
                    Map<Integer, List<String>> temp = getRelativeValInMethod(filePath, res1, vals_info);
                    if (temp == null) continue;
                    rela_names.putAll(temp);
                } else searchBody((JSONObject) obj.get(key), filePath, rela_names, vals_info);
            } else if (type.contains("JSONArray")) {
                if (key.contains("resources")) {
                    res2 = (JSONArray) obj.get(key);
                    JSONObject[] res3 = new JSONObject[res2.size()];
                    for (int j = 0; j < res2.size(); j++) {
//                        System.out.println(res2.get(j));
                        res3[j] = new JSONObject();
                        res3[j].set("statement(Type=ExpressionStmt)", res2.get(j));
                    }
                    JSONObject res4 = new JSONObject();
                    res4.set("statements", res3);
                    Map<Integer, List<String>> temp = getRelativeValInMethod(filePath, res4, vals_info);
                    if (temp == null) continue;
                    rela_names.putAll(temp);
                } else if (key.equals("initialization")) {
                    res2 = (JSONArray) obj.get(key);
                    JSONObject[] res3 = new JSONObject[res2.size()];
                    for (int j = 0; j < res2.size(); j++) {
//                        System.out.println(res2.get(j));
                        res3[j] = new JSONObject();
                        res3[j].set("statement(Type=ExpressionStmt)", res2.get(j));
                    }
                    JSONObject res4 = new JSONObject();
                    res4.set("statements", res3);
                    Map<Integer, List<String>> temp = getRelativeValInMethod(filePath, res4, vals_info);
                    if (temp == null) continue;
                    rela_names.putAll(temp);
                } else {
                    for (Object child : (JSONArray) obj.get(key))
                        searchBodyWithJudge(child, filePath, rela_names, vals_info);
                }
            }
        }
        return rela_names;
    }

    public Map<Integer, List<String>> getRelativeValInMethod2(CompilationUnit cu, String filePath, List<String[]> vals_info) {
        Map<Integer, List<String>> rela_type = new HashMap<>();
        List<String[]> insertRes = new ArrayList<>();
        for (ClassOrInterfaceDeclaration cd : cu.findAll(ClassOrInterfaceDeclaration.class)) {
            for (FieldDeclaration field : cd.getFields()) {
                for (VariableDeclarator val : field.getVariables()) {
                    String valname = val.getNameAsString();
                    String valtype = val.getTypeAsString();
                    Optional<Expression> valexpr = val.getInitializer();
                    if (!valexpr.isPresent()) {
                        continue;
                    }
                    String valexp = "";
                    if (valexpr.isPresent())
                        valexp = String.valueOf(valexpr.get());
                    String[] valinfo = {valname, valtype, valexp, filePath};
                    int id2 = getID(valinfo);
                    String[] strList = getSplit(val.toString());
                    System.out.println(Arrays.toString(strList));
                    List<String> strList2 = new ArrayList<>(Arrays.asList(strList));
                    List<String> Stmvals = getStmvals(strList2, vals_info, filePath);
                    for (int i = 0; i < Stmvals.size(); i += 2) {
                        String n = Stmvals.get(i);
                        String t = Stmvals.get(i + 1);
                        if (n.equals(valname) && t.equals(valtype)) continue;
                        String[] temp = {t, n, String.valueOf(id2)};
                        insertRes.add(temp);
                    }
                }
            }
            for (InitializerDeclaration id : cu.findAll(InitializerDeclaration.class)) {
                for (VariableDeclarator variable : id.getBody().findAll(VariableDeclarator.class)) {
                    String valname = variable.getNameAsString();
                    String valtype = variable.getTypeAsString();
                    Optional<Expression> valexpr = variable.getInitializer();
                    if (!valexpr.isPresent()) {
                        continue;
                    }
                    String valexp = "";
                    if (valexpr.isPresent())
                        valexp = String.valueOf(valexpr.get());
                    String[] valinfo = {valname, valtype, valexp, filePath};
                    int id2 = getID(valinfo);
                    String[] strList = getSplit(variable.toString());
                    System.out.println(Arrays.toString(strList));
                    List<String> strList2 = new ArrayList<>(Arrays.asList(strList));
                    List<String> Stmvals = getStmvals(strList2, vals_info, filePath);
                    for (int i = 0; i < Stmvals.size(); i += 2) {
                        String n = Stmvals.get(i);
                        String t = Stmvals.get(i + 1);
                        if (n.equals(valname) && t.equals(valtype)) continue;
                        String[] temp = {t, n, String.valueOf(id2)};
                        insertRes.add(temp);
                    }
                }
            }
            for (MethodDeclaration method : cd.getMethods()) {
                method.getBody().ifPresent(blockStatement -> {
                    for (VariableDeclarator variable : blockStatement.findAll(VariableDeclarator.class)) {
                        String valname = variable.getNameAsString();
                        String valtype = variable.getTypeAsString();
                        Optional<Expression> valexpr = variable.getInitializer();
                        if (!valexpr.isPresent()) {
                            continue;
                        }
                        String valexp = "";
                        if (valexpr.isPresent())
                            valexp = String.valueOf(valexpr.get());
                        String[] valinfo = {valname, valtype, valexp, filePath};
                        int id2 = getID(valinfo);
                        String[] strList = getSplit(variable.toString());
                        System.out.println(Arrays.toString(strList));
                        List<String> strList2 = new ArrayList<>(Arrays.asList(strList));
                        List<String> Stmvals = getStmvals(strList2, vals_info, filePath);
                        for (int i = 0; i < Stmvals.size(); i += 2) {
                            String n = Stmvals.get(i);
                            String t = Stmvals.get(i + 1);
                            if (n.equals(valname) && t.equals(valtype)) continue;
                            String[] temp = {t, n, String.valueOf(id2)};
                            insertRes.add(temp);
                        }
                    }
                });
            }
        }
        System.out.println(insertRes);
        updateRelaType(insertRes);
        return rela_type;
    }

    public void updateRelaType(List<String[]> res) {
        String sql2 = "update rela_variable set rela_type=? where rela_name=? and vari_id=?;";
        if (res.size() > 0)
            JDBCUtils.Insert(sql2, res, false);
    }

    public Map<Integer, List<String>> getRelativeValInMethod(String filePath, JSONObject body, List<String[]> vals_info) {
        Map<Integer, List<String>> rela_names = new HashMap<>();
        //forStmt：statms[]->statm(ForStmt)->{body,initialization(Array)}->initialization(VariableDeclarationExpr)->variables[]
        //try-Resources: statms[]->resource(VariableDeclarationExpr)->variables[]->variable(Type=VariableDeclarator)
        //body: statms[]->statm->expression(VariableDeclarationExpr)->variables[]->variable(Type=VariableDeclarator)
        JSONArray statemts = (JSONArray) body.get("statements");
//        else if(body.keySet().contains("resources"))
//            statemts = (JSONArray) body.get("resources");
        for (int k = 0; statemts != null && k < statemts.size(); k++) {
            JSONObject statm = (JSONObject) statemts.get(k);
            if (statm == null) continue;
            for (String key : statm.keySet()) {
                JSONObject expr1 = (JSONObject) statm.get(key);
//                System.out.println("expr1: " + expr1);
                for (String key1 : expr1.keySet()) {
                    if (key1.contains("(Type=VariableDeclarationExpr)")) {
                        JSONObject expr = (JSONObject) expr1.get(key1);
                        JSONArray vals = (JSONArray) expr.get("variables");
                        for (int k2 = 0; vals != null && k2 < vals.size(); k2++) {
                            JSONObject val = (JSONObject) vals.get(k2);
                            if (val == null) continue;
                            for (String key2 : val.keySet()) {
                                if (key2.equals("variable(Type=VariableDeclarator)")) {
                                    JSONObject vd = (JSONObject) val.get(key2);
                                    String valname = "";
                                    String valtype = "";
                                    JSONObject init = null;
                                    List<String> res = null;
                                    if (vd == null) continue;
                                    for (String key3 : vd.keySet()) {
                                        if (key3.startsWith("name(")) {
                                            valname = ((JSONObject) vd.get(key3)).get("identifier").toString();
//                                            System.out.println("valname: " + valname);
                                        } else if (key3.startsWith("type(")) {
                                            JSONObject valt = ((JSONObject) vd.get(key3));
                                            for (String key4 : valt.keySet()) {
                                                if (key4.equals("origin")) continue;
                                                if (key4.contains("name")) {
                                                    valtype = ((JSONObject) valt.get(key4)).get("identifier").toString();
//                                                    System.out.println("valtype: " + valtype);
                                                    break;
                                                } else if (key4.contains("componentType(")) {
                                                    if (key4.equals("componentType(Type=PrimitiveType)"))
                                                        valtype = ((JSONObject) valt.get(key4)).get("type").toString();
                                                    else if (key4.equals("componentType(Type=ClassOrInterfaceType)")) {
                                                        valt = (JSONObject) ((JSONObject) valt.get(key4)).get("name(Type=SimpleName)");
                                                        valtype = valt.get("identifier").toString();
                                                    } else if (key4.equals("componentType(Type=ArrayType)")) {
                                                        valt = (JSONObject) valt.get(key4);
                                                        boolean flag = true;
                                                        while (flag) {
                                                            Set<String> keys = valt.keySet();
                                                            for (String key5 : keys) {
                                                                if (key5.equals("origin")) continue;
                                                                if (key5.equals("componentType(Type=PrimitiveType)")) {
                                                                    valtype = ((JSONObject) valt.get(key5)).get("type").toString();
                                                                    flag = false;
                                                                    break;
                                                                } else if (key5.equals("componentType(Type=ArrayType)")) {
                                                                    valt = (JSONObject) valt.get(key5);
//                                                            break;
                                                                } else if (key5.equals("componentType(Type=ClassOrInterfaceType)")) {
                                                                    valt = (JSONObject) ((JSONObject) valt.get(key5)).get("name(Type=SimpleName)");
                                                                    valtype = valt.get("identifier").toString();
                                                                    flag = false;
                                                                    break;
                                                                } else if (key5.equals("identifier")) {
                                                                    valtype = (String) valt.get(key5);
                                                                    break;
                                                                } else {
//                                                            System.out.println(vd);
                                                                    System.out.println(valt);
                                                                    System.out.println(filePath);
                                                                    System.out.println("complex Array type cannot be identified.");
//                                                            break;
                                                                }
                                                            }
                                                        }
                                                    }
                                                    break;
                                                } else if (key4.contains("type")) {
                                                    valtype = valt.get(key4).toString();
                                                    break;
                                                } else {
                                                    //others
                                                    valtype = "unknown_Type";
//                                            System.out.println(valt);
                                                }
                                            }
                                        } else if (key3.startsWith("initializer(")) {
                                            init = (JSONObject) vd.get(key3);
//                                            System.out.println("init: " + init);
                                            List<String> rela_names_list = new ArrayList<>();
                                            res = deal(key3, init, rela_names_list);
//                                            System.out.println("run here 401");
//                                            System.out.println("res401: " + res);
                                        }
//                                        System.out.println("run here 403");
//                                        System.out.println(vd);
                                    }
//                            System.out.println(res);
                                    if (res == null) continue;
//                                    System.out.println("run here 406");
                                    //valname,valtype,valexp,filePath
                                    int id = MatchId(valname, valtype, filePath, vals_info, rela_names);
//                                    System.out.println("id406: " + id);
                                    if (id == -1) {
                                        RecordLog.readAction("id not exist.");
                                        continue;
                                    }
                                    rela_names.put(id, res);
                                }
                            }
                        }
                    } else {
                        JSONObject obj = (JSONObject) statm.get(key);
                        if (obj == null) continue;
                        rela_names = searchBody(obj, filePath, rela_names, vals_info);
                    }
                }
            }
        }
        return rela_names;
    }

    public int getID(String[] infos) {
        int res = -1;
        for (HashMap map : db_info) {
            if (map.get("vari_name").equals(infos[0])
                    && map.get("vari_type").equals(infos[1])
                    && map.get("vari_value").equals(infos[2])
                    && map.get("vari_ori_file").equals(infos[3])) {
                int temp = (Integer) map.get("id");
                if (!used_id1.contains(temp)) {
                    res = temp;
                    used_id1.add(temp);
                    break;
                }
            }
        }
        return res;
    }


    public int MatchId(String valname, String valtype, String filePath, List<String[]> infos, Map<Integer, List<String>> rela_names) {
        int res = -1;
        valtype = valtype.toLowerCase();
        for (String[] list : infos) {
            if (list[1].equals(valname) && (list[2].toLowerCase().contains(valtype) || valtype == "unknown_Type") && list[4].equals(filePath)) {
                if (!used_id2.contains(Integer.valueOf(list[0]))) {
                    used_id2.add(Integer.valueOf(list[0]));
                    return Integer.valueOf(list[0]);
                }
            }
        }
        return res;
    }

    public List<String[]> getDeclarationForIdSearch(String filePath, CompilationUnit cu, boolean needReserve) {
        List<String[]> res = new ArrayList<>();
        for (ClassOrInterfaceDeclaration cd : cu.findAll(ClassOrInterfaceDeclaration.class)) {
            for (FieldDeclaration field : cd.getFields()) {
                for (VariableDeclarator val : field.getVariables()) {
                    String valname = val.getNameAsString();
                    String valtype = val.getTypeAsString();
                    Optional<Expression> valexpr = val.getInitializer();
                    if (!needReserve) {
                        if (!valexpr.isPresent())
                            continue;
                    }
                    String valexp = "";
                    if (valexpr.isPresent())
                        valexp = String.valueOf(valexpr.get());
                    String[] valinfo = {valname, valtype, valexp, filePath};
                    int id2 = getID(valinfo);
                    String[] valinfo2 = {String.valueOf(id2), valname, valtype, valexp, filePath};
                    res.add(valinfo2);
                }
            }
            for (InitializerDeclaration id : cu.findAll(InitializerDeclaration.class)) {
                for (VariableDeclarator variable : id.getBody().findAll(VariableDeclarator.class)) {
                    String valname = variable.getNameAsString();
                    String valtype = variable.getTypeAsString();
                    Optional<Expression> valexpr = variable.getInitializer();
                    if (!needReserve) {
                        if (!valexpr.isPresent())
                            continue;
                    }
                    String valexp = "";
                    if (valexpr.isPresent())
                        valexp = String.valueOf(valexpr.get());
                    String[] valinfo = {valname, valtype, valexp, filePath};
                    int id2 = getID(valinfo);
                    String[] valinfo2 = {String.valueOf(id2), valname, valtype, valexp, filePath};
                    res.add(valinfo2);
                }
            }
            for (MethodDeclaration method : cd.getMethods()) {
                NodeList<Parameter> parms = method.getParameters();
                if (parms != null && parms.size() > 0) {
                    for (Parameter p : parms) {
                        String name = p.getNameAsString();
                        String type = p.getTypeAsString();
                        String[] valinfo2 = {"-1", name, type, "", filePath};
                        res.add(valinfo2);
                    }
                }
                method.getBody().ifPresent(blockStatement -> {
                    for (VariableDeclarator variable : blockStatement.findAll(VariableDeclarator.class)) {
                        String valname = variable.getNameAsString();
                        String valtype = variable.getTypeAsString();
                        Optional<Expression> valexpr = variable.getInitializer();
                        if (!needReserve) {
                            if (!valexpr.isPresent())
                                continue;
                        }
                        String valexp = "";
                        if (valexpr.isPresent())
                            valexp = String.valueOf(valexpr.get());
                        String[] valinfo = {valname, valtype, valexp, filePath};
                        int id2 = getID(valinfo);
                        String[] valinfo2 = {String.valueOf(id2), valname, valtype, valexp, filePath};
                        res.add(valinfo2);
                    }
                });
            }
        }
        return res;
    }

    public Map<Integer, List<String>> getRelativeValInField(String filePath, JSONArray vals, List<String[]> vals_info) {
        Map<Integer, List<String>> rela_names = new HashMap<>();
        for (int k3 = 0; vals != null && k3 < vals.size(); k3++) {
            JSONObject vd = (JSONObject) ((JSONObject) vals.get(k3)).get("variable(Type=VariableDeclarator)");
            JSONObject init = null;
            List<String> rela_list = null;
            String valname = "";
            String valtype = "";
            for (String key3 : vd.keySet()) {
                if (key3.startsWith("name(")) {
                    valname = ((JSONObject) vd.get(key3)).get("identifier").toString();
                    System.out.println("valname: " + valname);
                } else if (key3.startsWith("type(")) {
                    JSONObject valt = ((JSONObject) vd.get(key3));
                    for (String key4 : valt.keySet()) {
                        if (key4.equals("origin")) continue;
                        if (key4.contains("name")) {
                            valtype = ((JSONObject) valt.get(key4)).get("identifier").toString();
//                            System.out.println(valtype);
                            break;
                        } else if (key4.contains("componentType(")) {
                            if (key4.equals("componentType(Type=PrimitiveType)"))
                                valtype = ((JSONObject) valt.get(key4)).get("type").toString();
                            else if (key4.equals("componentType(Type=ClassOrInterfaceType)")) {
                                valt = (JSONObject) ((JSONObject) valt.get(key4)).get("name(Type=SimpleName)");
                                valtype = valt.get("identifier").toString();
                            } else if (key4.equals("componentType(Type=ArrayType)")) {
                                valt = (JSONObject) valt.get(key4);
                                boolean flag = true;
                                while (flag) {
                                    Set<String> keys = valt.keySet();
                                    for (String key5 : keys) {
                                        if (key5.equals("origin")) continue;
                                        if (key5.equals("componentType(Type=PrimitiveType)")) {
                                            valtype = ((JSONObject) valt.get(key5)).get("type").toString();
                                            flag = false;
                                            break;
                                        } else if (key5.equals(key4)) {
                                            valt = (JSONObject) valt.get(key5);
//                                            break;
                                        } else if (key5.equals("componentType(Type=ClassOrInterfaceType)")) {
                                            valt = (JSONObject) ((JSONObject) valt.get(key5)).get("name(Type=SimpleName)");
                                            valtype = valt.get("identifier").toString();
                                            flag = false;
                                            break;
                                        } else if (key5.equals("identifier")) {
                                            valtype = (String) valt.get(key5);
                                            break;
                                        } else {
                                            System.out.println(valt);
                                            System.out.println("complex Array type cannot be identified.");
                                        }
                                    }
                                }
                            }
                            break;
                        } else if (key4.contains("type")) {
                            valtype = valt.get(key4).toString();
                            break;
                        } else {
                            //others
                            valtype = "unknown_Type";
//                            System.out.println(valt);
                        }
                    }
                } else if (key3.startsWith("initializer(")) {
                    init = (JSONObject) vd.get(key3);
//                    System.out.println(init);
                    List<String> rela_names_list = new ArrayList<>();
                    rela_list = deal(key3, init, rela_names_list);
//                    System.out.println("run here 589");
                }
            }
//            System.out.println(rela_list);
            int id = MatchId(valname, valtype, filePath, vals_info, rela_names);
//            System.out.println(id);
            if (id == -1) {
                RecordLog.readAction("id not exist.");
                continue;
            }
            rela_names.put(id, rela_list);
        }
        return rela_names;
    }

    public Map<Integer, List<String>> getRelaforClassORInterfaceD(String filePath, JSONObject obj, Map<Integer, List<String>> rela_names, List<String[]> vals_info) {
        if (obj == null) return null;
        JSONArray obj4 = (JSONArray) (obj.get("members"));
//        System.out.println("obj4: "+obj4);
        for (int k2 = 0; obj4 != null && k2 < obj4.size(); k2++) {
            JSONObject m = (JSONObject) obj4.get(k2);
//            System.out.println("m: " + m);
            if (m == null) continue;
            for (String key : m.keySet()) {
                if (key.equals("member(Type=ClassOrInterfaceDeclaration)")) {
                    getRelaforClassORInterfaceD(filePath, (JSONObject) m.get(key), rela_names, vals_info);
                } else if (key.equals("member(Type=MethodDeclaration)")) {
                    JSONObject md = (JSONObject) m.get(key);
//                    System.out.println("md: " + md);
                    JSONObject body = (JSONObject) md.get("body(Type=BlockStmt)");
                    if (body == null) continue;
                    Map<Integer, List<String>> rela_In_method = getRelativeValInMethod(filePath, body, vals_info);
                    if (rela_In_method == null || rela_In_method.size() == 0) continue;
                    if (rela_names != null) rela_names.putAll(rela_In_method);
//                        System.out.println(md);
                } else if (key.equals("member(Type=FieldDeclaration)")) {
                    JSONObject fd = (JSONObject) m.get(key);
                    JSONArray vals = (JSONArray) fd.get("variables");
                    if (vals == null) continue;
                    Map<Integer, List<String>> rela_In_Field = getRelativeValInField(filePath, vals, vals_info);
                    if (rela_In_Field == null || rela_In_Field.size() == 0) continue;
                    if (rela_names != null) rela_names.putAll(rela_In_Field);
                }
            }
        }
        return rela_names;
    }

    public List<String[]> getDeclaration(String filePath, CompilationUnit cu, List<String[]> res) {
        List<ImportDeclaration> importInfos = cu.getImports();
        String importInfoStr = "";
        for (int i = 0; i < importInfos.size(); i++) {
            ImportDeclaration impd = importInfos.get(i);
            String s = "import ";
            if (impd.isStatic()) s += "static ";
            s += impd.getNameAsString();
            if (impd.isAsterisk()) s += ".*";
            s += ";\n";
//            System.out.println(s);
            importInfoStr += s;
        }
        Optional<PackageDeclaration> packageInfos = cu.getPackageDeclaration();
        String packageInfoStr = "";
        if (packageInfos.isPresent()) {
            packageInfoStr += packageInfos.get().toString();
        }

        for (ClassOrInterfaceDeclaration cd : cu.findAll(ClassOrInterfaceDeclaration.class)) {
            for (FieldDeclaration field : cd.getFields()) {
                for (VariableDeclarator val : field.getVariables()) {
                    String valname = val.getNameAsString();
                    String valtype = val.getTypeAsString();
                    Optional<Expression> valexpr = val.getInitializer();
                    if (!valexpr.isPresent()) {
                        continue;
                    }
                    String valexp = String.valueOf(valexpr.get());
                    String valExp = valtype + " " + valname + " = " + valexp;
                    String val_type_and_exp = valexp + "_" + valtype;
                    expAndTypeDetailCount.put(val_type_and_exp, expAndTypeDetailCount.getOrDefault(val_type_and_exp, 0) + 1);
                    declarationDetailCount.put(valexp, declarationDetailCount.getOrDefault(valexp, 0) + 1);
                    typeDetailCount.put(valtype, typeDetailCount.getOrDefault(valtype, 0) + 1);
                    String[] valinfo = {valname, valtype, String.valueOf(valexp), valExp, filePath, importInfoStr, packageInfoStr};
                    res.add(valinfo);
                    declarationCount += 1;
                }
            }
            for (InitializerDeclaration id : cu.findAll(InitializerDeclaration.class)) {
                for (VariableDeclarator variable : id.getBody().findAll(VariableDeclarator.class)) {
                    String valname = variable.getNameAsString();
                    String valtype = variable.getTypeAsString();
                    Optional<Expression> valexpr = variable.getInitializer();
                    if (!valexpr.isPresent()) {
                        continue;
                    }
                    String valexp = String.valueOf(valexpr.get());
                    String valExp = valtype + " " + valname + " = " + valexp;
                    String val_type_and_exp = valexp + "_" + valtype;
                    expAndTypeDetailCount.put(val_type_and_exp, expAndTypeDetailCount.getOrDefault(val_type_and_exp, 0) + 1);
                    declarationDetailCount.put(valexp, declarationDetailCount.getOrDefault(valexp, 0) + 1);
                    typeDetailCount.put(valtype, typeDetailCount.getOrDefault(valtype, 0) + 1);
                    String[] valinfo = {valname, valtype, String.valueOf(valexp), valExp, filePath, importInfoStr, packageInfoStr};
                    res.add(valinfo);
                    declarationCount += 1;
                }
            }
            for (MethodDeclaration method : cd.getMethods()) {
                String finalImportInfoStr = importInfoStr;
                String finalPackageInfoStr = packageInfoStr;
                method.getBody().ifPresent(blockStatement -> {
                    for (VariableDeclarator variable : blockStatement.findAll(VariableDeclarator.class)) {
                        String valname = variable.getNameAsString();
                        String valtype = variable.getTypeAsString();
                        Optional<Expression> valexpr = variable.getInitializer();
                        if (!valexpr.isPresent()) {
                            continue;
                        }
                        String valexp = String.valueOf(valexpr.get());
                        String valExp = valtype + " " + valname + " = " + valexp;
                        String val_type_and_exp = valexp + "_" + valtype;

                        expAndTypeDetailCount.put(val_type_and_exp, expAndTypeDetailCount.getOrDefault(val_type_and_exp, 0) + 1);
                        declarationDetailCount.put(valexp, declarationDetailCount.getOrDefault(valexp, 0) + 1);
                        typeDetailCount.put(valtype, typeDetailCount.getOrDefault(valtype, 0) + 1);
                        String[] valinfo = {valname, valtype, String.valueOf(valexp), valExp, filePath, finalImportInfoStr, finalPackageInfoStr};
                        res.add(valinfo);
                        declarationCount += 1;
                    }
                });
            }
        }
        if (!res.isEmpty()) {
            String sql = "insert into variables(vari_name, vari_type, vari_value, vari_expr, vari_ori_file, import_info, package_Info) values(?,?,?,?,?,?,?);";
            int count = 1;
            for (String[] l : res) {
                JDBCUtils.Insert(sql, Collections.singletonList(l), false);
                count += 1;
            }
        }
        return res;
    }

    public void getRelativeVals(String filePath, CompilationUnit cu, List<String[]> vals_info) {
        Map<Integer, List<String>> rela_names = new HashMap<>();
        String s = (new YamlPrinter(true)).output(cu);
        Yaml yaml = new Yaml();
        Map<String, Object> objectMap = yaml.load(s);
        List<String> Jsonresult = yamlConverToJson(s);
        JSONObject jsonObject = new JSONObject(Jsonresult.get(0));
        JSONObject obj1 = (JSONObject) jsonObject.get("root(Type=CompilationUnit)");
        if (obj1 != null) {
            JSONArray ClassOrInterfaceD = (JSONArray) obj1.get("types");
            for (int k1 = 0; ClassOrInterfaceD != null && k1 < ClassOrInterfaceD.size(); k1++) {
                JSONObject obj2 = (JSONObject) ClassOrInterfaceD.get(k1);
                JSONObject obj3 = (JSONObject) obj2.get("type(Type=ClassOrInterfaceDeclaration)");
                rela_names = getRelaforClassORInterfaceD(filePath, obj3, rela_names, vals_info);
                System.out.println("rela_names: " + rela_names);
            }
            if (rela_names != null && rela_names.size() > 0) {
                System.out.println("rela_names: " + rela_names);
                insertRelaNames(rela_names);
            }
        } else {
            System.err.println("obj1 is null");
        }
    }

    public List<String[]> analyse(String filePath, boolean getRelative) throws IOException, DocumentException {
        String con = ""; 
        List<String[]> res = new ArrayList<>();
        try {
            con = Files.readString(Paths.get(filePath));
            ParserConfiguration parserConfiguration = new ParserConfiguration();
            String sourcePath = "/root/ssfuzz/feature/src/test/output/testsuits";
            TypeSolver typeSolver = SmbTypeSolver.generateTypeSolver(sourcePath);
            JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
            parserConfiguration.setSymbolResolver(symbolSolver);
            parserConfiguration.setLexicalPreservationEnabled(true);
            JavaParser javaParser = new JavaParser(parserConfiguration);
            ParseResult<CompilationUnit> result = javaParser.parse(con);
            Optional<CompilationUnit> optionalCompilationUnit = result.getResult();
            CompilationUnit cu;
            if (optionalCompilationUnit.isPresent()) {
                cu = optionalCompilationUnit.get();
            } else {
                System.out.println("CompilationUnit");
                return res;
            }
//            JDBCUtils.pro = "jdbc.properties";

            List<String[]> vals_info = getDeclarationForIdSearch(filePath, cu, true);

            List<Statement> result2 = getStatments(filePath, cu, vals_info);

            getRelativeVals(filePath, cu, vals_info);
            Map<Integer, List<String>> r = getRelativeValInMethod2(cu, filePath, vals_info);
            res = getDeclaration(filePath, cu, res);
        } catch (MalformedInputException e) {
            e.printStackTrace();
        } catch (NoSuchFileException e){
            e.printStackTrace();
        }
        return res;
    }

    public void insertRelaNames(Map<Integer, List<String>> rela_names) {
        String sql = "insert into rela_variable(vari_id, rela_name, rela_table) values(?,?,?);";
        List<String[]> res = new ArrayList<>();
        for (Integer i : rela_names.keySet()) {
            List<String> names = rela_names.get(i);
            if (names == null) continue;
            for (String n : names) {
                String[] temp = {String.valueOf(i), n, "variables"};
                res.add(temp);
            }
        }
//        System.out.println(res);
        if (res.size() > 0) {
            System.out.println("start insert");
            JDBCUtils.Insert(sql, res, false);
        }

    }

    public static List<String> yamlConverToJson(String yamlString) {
        Yaml yaml = new Yaml();
        Iterable<Object> object = yaml.loadAll(yamlString);
        List<String> yamlList = new ArrayList<>();
        object.forEach(y -> {
            if (ObjectUtil.isNotNull(y)) {
//                System.out.println(y);
                yamlList.add(JSONUtil.toJsonStr(y));
            }
        });
        return yamlList;
    }

    public List<List<String[]>> searchDeclaration(String dirPath, boolean getRelative) throws IOException, DocumentException {
        List<List<String[]>> listNode = searchDeclaration(dirPath, new ArrayList<>(), getRelative);
        List<Map.Entry<String, Integer>> info = new ArrayList<Map.Entry<String, Integer>>(expAndTypeDetailCount.entrySet());
        Collections.sort(info, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return (o2.getValue() - o1.getValue());
            }
        });
        for (int i = 0; i < info.size(); i++) {
            RecordLog.logger.info(info.get(i).toString());
        }
        return listNode;
    }

    public List<List<String[]>> searchDeclaration(String filePath, List<List<String[]>> listNode, boolean getRelative) throws IOException, DocumentException {
        File file = new File(filePath);
        Path targetDirectory = Paths.get("/root/ssfuzz/feature/src/test/data/getFeature_tmp/");

        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                searchDeclaration(filePath + "/" + child.getName(), listNode, getRelative);
//                System.out.println("processing filePath: " + filePath);
            }
        } else {
            if (filter.interested(filePath)) {
//                System.out.println("filePath: " + filePath);
                List<String[]> list = analyse(filePath, getRelative);
                if (list.size() > 0) listNode.add(list);

                try {
                    Files.move(file.toPath(), targetDirectory.resolve(file.getName()), StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("File moved successfully.");
                } catch (Exception e) {
                    System.out.println("An error occurred: " + e.getMessage());
                }
            }
        }
        return listNode;
    }

    public String[] getSplit(String str) {
        str = str.replace(".", " ").replace(":", " ").replace(",", " ");
        str = str.replace("(", " ").replace(")", " ");
        str = str.replace("<", " ").replace(">", " ");
        str = str.replace("{", " ").replace("}", " ");
        str = str.replace("[", " ").replace("]", " ");
        str = str.replace(";", "").replace("\n", "").replace("\t", "");
        str = str.replace("+", " ").replace("-", " ").replace("*", " ").replace("/", " ").replace("=", " ");
        str = str.replace("&", " ").replace("|", " ").replace("\\/", "");
//        System.out.println(str);
        String[] strList = str.trim().split("\\s+");
        return strList;
    }

    public List<String[]> geneVals(CompilationUnit cu, List<String[]> vals_info) {
        List<String[]> res = new ArrayList<>();
        for (String[] v : vals_info) {
            Integer id = Integer.parseInt(v[0]);
            if (id == -1) {
                String[] temp = {v[1], v[2]};
                res.add(temp);
            }
        }
        return res;
    }

    public static List<Integer> WhereStr(String str, String wherestr) {
        List<Integer> res = new ArrayList<>();
        int index = 0;
        if (!str.contains(wherestr))
            return res;
        else {
            index = str.indexOf(wherestr, index);
            res.add(index);
            while (str.substring(index + 1).contains(wherestr)) {
                index = str.indexOf(wherestr, index + 1);
                res.add(index);
            }
            return res;
        }
    }

    public List<String> getAPIInfo2(String str, List<String> stm_info) {
        List<String> res = new ArrayList<>();
        Set<String> set1 = new HashSet<>();
        Set<String> set2 = new HashSet<>();
        Set<String> set3 = new HashSet<>();
        for (int i = 0; i < stm_info.size(); i += 2) {
            String name = stm_info.get(i);
            String type = stm_info.get(i + 1);
            System.out.println(name + " " + type);
            String s = name + " " + type;
            if (set1.contains(s)) continue;
            set1.add(s);
            if (set3.contains(name + ".")) continue;
            boolean hasInnerName = false;
            for (String s2 : set3) {
                if (s2.endsWith(name + ".")) {
                    hasInnerName = true;
                    break;
                }
            }
            if (hasInnerName) continue;
            set3.add(name + ".");
            if (str.contains(name + ".")) {
                List<Integer> pos = WhereStr(str, name + ".");
                if (pos.size() > 0) {
                    for (Integer p : pos) {
                        int index = p + name.length();
                        if (str.charAt(index) == '.') {
                            String temp = str.substring(index);
                            int index2 = temp.indexOf("(");
                            if (index2 == -1) continue;
                            String method = temp.substring(1, index2);
//                            if(method == "println")continue;
                            res.add(type + " " + method);
                        }
                    }
                }
            }
//            if(class_method_info.keySet().contains(type)) {
            if (set2.contains("new " + type)) continue;
            set2.add("new " + type);
            if (str.contains("new " + type)) {
                List<Integer> pos2 = WhereStr(str, "new " + type + "(");
                if (pos2.size() > 0) {
                    res.add(type + " Constructor" + " " + String.valueOf(pos2.size()));
                }
            }
//            }
        }
        return res;
    }

    public Map<String, List<String>> getAPIInfo(String str) {
//        List<String> res = new ArrayList<>();
        Map<String, List<String>> res = new HashMap<>();
        str = str.trim();
        str = str.replace("new ", "¥¥¥¥");
        str = str.replace(":", " ").replace(",", " ");
        str = str.replace("<", " ").replace(">", " ");
        str = str.replace("{", " ").replace("}", " ");
        str = str.replace("[", " ").replace("]", " ");
        str = str.replace(";", "").replace("\n", "").replace("\t", "");
        str = str.replace("+", " ").replace("-", " ").replace("*", " ").replace("/", " ").replace("=", " ");
        str = str.replace("&", " ").replace("|", " ").replace("\\/", "");
//        System.out.println(str);
        String[] strList = str.trim().split("\\s+");
        String regex1 = "([\\w\\W\\d\\D]+?)\\.[\\w\\W\\d\\D]+?(\\()";
        Pattern pattern1 = Pattern.compile(regex1);
        String regex2 = "¥¥¥¥[\\w\\W\\d\\D]+?\\(";
        Pattern pattern2 = Pattern.compile(regex2);
        for (String temp : strList) {
            if (temp.contains("(")) {
                if (temp.trim().equals("(")) continue;
                if (temp.contains(".")) {
                    Matcher matcher1 = pattern1.matcher(temp);
                    if (matcher1.find()) {
                        int index2_2 = temp.indexOf("(");
                        int index2_1 = temp.indexOf(".");

                        while (index2_1 > index2_2) {
                            if (index2_2 + 1 >= temp.length()) break;
                            temp = temp.substring(index2_2 + 1);
                            index2_2 = temp.indexOf("(");
                            index2_1 = temp.indexOf(".");
                        }
                        if (index2_1 >= index2_2) continue;
                        temp = temp.substring(0, index2_2);
                        int index1 = temp.lastIndexOf(".");
                        String method = temp.substring(index1 + 1);
                        String temp2 = temp.substring(0, index1);
                        String obj = "";
                        if (temp2.contains(".")) {
                            int index = temp2.lastIndexOf(".");
                            obj = temp2.substring(index + 1);
                        } else {
                            obj = temp2;
                        }
                        if (obj.equals("this")) continue;
                        List<String> list;
                        if (res.keySet().contains(obj))
                            list = res.get(obj);
                        else list = new ArrayList<>();
                        list.add(method);
                        res.put(obj, list);
                    }
                } else if (temp.contains("¥¥¥¥")) {
                    Matcher matcher2 = pattern2.matcher(temp);
                    if (matcher2.find()) {
                        int index1 = temp.lastIndexOf("¥¥¥¥");
                        temp = temp.substring(index1 + 4);
                        int index2 = temp.indexOf("(");
                        if (index2 == -1) continue;
                        temp = temp.substring(0, index2);
                        List<String> list;
                        if (res.keySet().contains("¥¥¥¥"))
                            list = res.get("¥¥¥¥");
                        else list = new ArrayList<>();
                        list.add(temp);
                        res.put("¥¥¥¥", list);
                    }
                }
            }
        }
        System.out.println(Arrays.toString(strList));
        return res;
    }

    public List<String> change(Map<String, List<String>> temp_map, List<String> Stmvals) {
        List<String> temp = new ArrayList<>();
        Set<String> keys = temp_map.keySet();
        for (int i = 0; i < Stmvals.size(); i += 2) {
            String s1 = Stmvals.get(i);

            if (keys.contains(s1) && !class_method_info.keySet().contains(s1)) {
                String cla = Stmvals.get(i + 1);
                if (class_method_info.keySet().contains(cla)) {
                    if (temp_map.get(cla) == null || temp_map.get(cla).size() == 0) continue;
                    for (String v : temp_map.get(s1)) {
                        temp.add(cla + " " + v);
                    }
                }
            }

            else if (class_method_info.keySet().contains(s1)) {
                if (temp_map.get(s1) == null || temp_map.get(s1).size() == 0) continue;
                for (String v : temp_map.get(s1)) {
                    temp.add(s1 + " " + v);
                }
            }
        }
        List<String> temp_list = temp_map.get("¥¥¥¥");
        if (temp_list != null) {
            for (String s : temp_list) {
                if (class_method_info.keySet().contains(s)) {
                    temp.add(s + " Constructor");
                }
            }
        }
        return temp;
    }

    public CompilationUnit removeComment(CompilationUnit cu) {
        List<Comment> comments = cu.getAllContainedComments();
        List<Comment> unwantedComments = comments
                .stream()
                .filter(p -> !p.getCommentedNode().isPresent() || p instanceof Comment)
                .collect(Collectors.toList());
        try {
            unwantedComments.forEach(Node::remove);
        } catch (Exception e) {
//            e.printStackTrace();
        }
        return cu;
    }

    public static String getAdd(String fileDetect) {
        String addstring = "";
        try {
            File file = new File(fileDetect);
            if (!file.exists()) {
                System.out.println("no: " + fileDetect);
                return addstring;
            }
            try (BufferedReader br = new BufferedReader(new FileReader(fileDetect))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.startsWith("+")) {
                        if (line.substring(1).trim().startsWith("*")) continue;
                        addstring = addstring + line.substring(1).strip() + "\n";
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } finally {
            return addstring;
        }
    }

    public List<Statement> getStatments(String filePath, CompilationUnit cu, List<String[]> vals_info) throws IOException {
        List<Statement> statms = new ArrayList<>();
        List<String[]> insertRes = new ArrayList<>();
        List<String[]> insertRes3 = new ArrayList<>();
        List<String[]> ExpressionInsertRes = new ArrayList<>();
        Map<String, List<String>> insertRes2 = new HashMap<>();

        String[] pathComponents = filePath.split("/");
        String fileName = pathComponents[pathComponents.length - 1];
        String newFileName = "patch-" + fileName;
        pathComponents[pathComponents.length - 1] = newFileName;
        pathComponents[pathComponents.length - 2] = "detectFeature";
        String newPath = String.join("/", pathComponents);
        String addString;
        addString = getAdd(newPath);
        Map<String, Integer> Class_Method_info = new HashMap<>();
        cu = removeComment(cu);
        for (ClassOrInterfaceDeclaration cd : cu.findAll(ClassOrInterfaceDeclaration.class)) {
            for (MethodDeclaration method : cd.getMethods()) {
                System.out.println(method.getName());
                System.out.println("*******************************");
                method.getBody().ifPresent(blockStatement -> {
                    //for
                    for (ForStmt fos : blockStatement.findAll(ForStmt.class)) {
                        String[] strList = getSplit(fos.toString());
                        System.out.println(Arrays.toString(strList));
                        List<String> strList2 = new ArrayList<>(Arrays.asList(strList));
                        List<String> Stmvals = getStmvals(strList2, vals_info, filePath);
                        String[] temp = {fos.toString(), "ForStmt"};
                        System.out.println("ForStmt-temp: " + Arrays.toString(temp));
                        insertRes.add(temp);
                        String[] temp2 = {filePath, fos.toString()};
                        insertRes3.add(temp2);
                        insertRes2.put(fos.toString(), Stmvals);
                    }
                    //while
                    for (WhileStmt whs : blockStatement.findAll(WhileStmt.class)) {
                        String[] strList = getSplit(whs.toString());
                        List<String> strList2 = new ArrayList<>(Arrays.asList(strList));
                        List<String> Stmvals = getStmvals(strList2, vals_info, filePath);
                        String[] temp = {whs.toString(), "WhileStmt"};
                        insertRes.add(temp);
                        String[] temp2 = {filePath, whs.toString()};
                        insertRes3.add(temp2);
                        insertRes2.put(whs.toString(), Stmvals);
                    }
                    //for-each
                    for (ForEachStmt fes : blockStatement.findAll(ForEachStmt.class)) {
                        String[] strList = getSplit(fes.toString());
                        List<String> strList2 = new ArrayList<>(Arrays.asList(strList));
                        List<String> Stmvals = getStmvals(strList2, vals_info, filePath);
                        String[] temp = {fes.toString(), "ForEachStmt"};
                        insertRes.add(temp);
                        String[] temp2 = {filePath, fes.toString()};
                        insertRes3.add(temp2);
                        insertRes2.put(fes.toString(), Stmvals);
                    }
                    //if
                    for (IfStmt ifs : blockStatement.findAll(IfStmt.class)) {
                        String[] strList = getSplit(ifs.toString());
                        List<String> strList2 = new ArrayList<>(Arrays.asList(strList));
                        List<String> Stmvals = getStmvals(strList2, vals_info, filePath);
                        String[] temp = {ifs.toString(), "IfStmt"};
                        insertRes.add(temp);
                        String[] temp2 = {filePath, ifs.toString()};
                        insertRes3.add(temp2);
                        insertRes2.put(ifs.toString(), Stmvals);
                    }
                    //do-while
                    for (DoStmt dos : blockStatement.findAll(DoStmt.class)) {
                        String[] strList = getSplit(dos.toString());
                        List<String> strList2 = new ArrayList<>(Arrays.asList(strList));
                        List<String> Stmvals = getStmvals(strList2, vals_info, filePath);
                        String[] temp = {dos.toString(), "DoStmt"};
                        insertRes.add(temp);
                        String[] temp2 = {filePath, dos.toString()};
                        insertRes3.add(temp2);
                        insertRes2.put(dos.toString(), Stmvals);
                    }
                    //APIs
                    for (ExpressionStmt exps : blockStatement.findAll(ExpressionStmt.class)) {
                        if (exps.toString().contains("Main ")) continue;
                        String[] strList = getSplit(exps.toString());
                        System.out.println(Arrays.toString(strList));

                        List<String> strList2 = new ArrayList<>(Arrays.asList(strList));
                        List<String> Stmvals = getStmvals(strList2, vals_info, filePath);
                        List<String> method_info = getAPIInfo2(exps.toString(), Stmvals);
                        if (method_info.size() == 0) continue;
                        String energy;

                        System.out.println("compare");
                        System.out.println(addString);
                        StringBuilder stringBuilder = new StringBuilder();
                        exps.toString().lines().map(String::strip).forEach(line -> stringBuilder.append(line).append("\n"));
                        String strippedText = stringBuilder.toString();
                        System.out.println(strippedText);
                        if (addString.contains(strippedText) || strippedText.contains(addString)) {
                            energy = "1";
                        } else {
                            energy = "0";
                        }
                        String[] temp = {exps.toString(), "ExpressionStmt", Stmvals.toString(), filePath, method_info.toString(), energy};
                        ExpressionInsertRes.add(temp);
                    }
                });
            }
        }

        insertExp(ExpressionInsertRes);
        insertStm(insertRes);
        uppdateFP(insertRes3);
        insertValsofStm(insertRes2);
        return statms;
    }

    public List<String> get_Class_info(String sql) {
        List res2 = JDBCUtils.query(sql);
        System.out.println(res2);
        List<String> res = new ArrayList<>();
        for (int i = 0; i < res2.size(); i++) {
            Map temp = (Map) res2.get(i);
            res.add((String) temp.get("Class_Name"));
        }
        return res;
    }

    public Map<String, List<String>> get_Method_info(String sql) {
        Map<String, List<String>> r = new HashMap<>();
//        JDBCUtils.pro = "jdbc.properties";
        List res = JDBCUtils.query(sql);
//        System.out.println("res: " + res);
        for (int i = 0; i < res.size(); i++) {
            Map temp = (Map) res.get(i);
            String key = (String) temp.get("Class_Name");
            String value = (String) temp.get("Method_Name");
            List<String> v;
            if (r.keySet().contains(key)) {
                v = r.get(key);
            } else {
                v = new ArrayList<>();
            }
            v.add(value);
            r.put(key, v);
        }
        return r;
    }

    public List<String> getStmvals(List<String> stm, List<String[]> vals_info, String filePath) {
        Set<String> set = new HashSet<>();
        List<String> res = new ArrayList<>();
        for (int i = 0; i < vals_info.size(); i++) {
            String fp = vals_info.get(i)[4];
            if (fp.equals(filePath)) {
                String name = vals_info.get(i)[1];
                String type = vals_info.get(i)[2];
//                System.out.println(name+" pos: "+stm.indexOf(name));
                String s = name + " " + type;
                if (stm.contains(name) && !set.contains(s)) {
//                    System.out.println(name);
                    res.add(name);
                    res.add(type);
                    set.add(s);
//                    stm.remove(name);
                }
            }
        }
        return res;
    }

    public void insertExp(List<String[]> stm_info) {
        if (stm_info.size() > 0) {
            String sql = "insert into Expression(content,type,varList,filepath,method_info,energy) values(?,?,?,?,?,?);";
//            JDBCUtils.pro = "jdbc.properties";
            JDBCUtils.Insert(sql, stm_info, false);
        }
    }

    public void insertStm(List<String[]> stm_info) {
        if (stm_info.size() > 0) {
            String sql = "insert into Statement(content,type) values(?,?);";
//            String sql = "insert into Expression(content,type,varList,filepath,method_info) values(?,?,?,?,?);";
            System.out.println("***************insertStm***************");
            JDBCUtils.Insert(sql, stm_info, false);
            System.out.println("***************END-insertStm***************");
        }
    }

    public void insertValsofStm(Map<String, List<String>> info) {
        String sql1 = "update Statement set varList=? where content=?;";
        List<String[]> res = new ArrayList<>();
        for (String i : info.keySet()) {
            List<String> id_type = info.get(i);
            if (id_type == null) continue;
            String[] temp = {id_type.toString(), i};
            res.add(temp);
        }
        if (res.size() > 0)
            JDBCUtils.Insert(sql1, res, false);
    }

    public void uppdateFP(List<String[]> stm_info) {
        String sql2 = "update Statement set filepath=? where content=?;";
        if (stm_info.size() > 0)
            JDBCUtils.Insert(sql2, stm_info, false);
    }
}