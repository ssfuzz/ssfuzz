package org.example.generator;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ReferenceType;
import org.example.JDBCUtils;

import java.util.*;

// from MutationByInsert.Main
public class InsertVal {
    static VariableGenerater generator = new VariableGenerater();
    static ClassifyVariable classifier = new ClassifyVariable();
    static String test = "public void test() throws Exception {}";
    public static ReferenceType throwinfo = getThrowinfo();
    public List<HashMap<String,String>> name_and_type_updateNeedSetValName = new ArrayList<>();
    public List<Statement> name_and_type_statments = new ArrayList<>();
    public List<Statement> pres = new ArrayList<>();
    public boolean hasInsert;
    public ArrayList<String> tempMutatorList = new ArrayList<>();
    public InsertVal(){
    }
    public  Map<String,List<String>> getValInfo(){
        Map<String,List<String>> res = new HashMap<>();
        String sql = "select vari_id,rela_name,rela_type from rela_variable";
        List r = JDBCUtils.query(sql);
        if (r.size() != 0) {
            int index2 = 0;
            int lastid = 1;
            List<String> temp = new ArrayList<>();
            while(index2<r.size()){
                Map mp = (Map) r.get(index2);
                int id = (int) mp.get("vari_id");
                if(id==lastid) {
                    String type = (String) mp.get("rela_type");
                    if(type == null){
                        index2++;
                        continue;
                    }
                    String name = (String) mp.get("rela_name");
                    temp.add(name);
                    temp.add(type);
                }
                else{
                    if(temp.size()>0)
                        res.put(String.valueOf(lastid),new ArrayList<>(temp));
                    lastid = id;
                    temp = new ArrayList<>();
                    String type = (String) mp.get("rela_type");
                    if(type == null){
                        index2++;
                        continue;
                    }
                    String name = (String) mp.get("rela_name");
                    temp.add(name);
                    temp.add(type);
                }
                index2++;
            }
        }
        return res;
    }
    public static ReferenceType getThrowinfo(){
        String test = "public void testOther() throws Exception {}";
        MethodDeclaration test2 = StaticJavaParser.parseMethodDeclaration(test);
        NodeList<ReferenceType> th = test2.getThrownExceptions();
        return th.get(0);
    }
    public VariableComponent changeIntoVariable(HashMap val){
        String name = (String) val.get("vari_name");
        String type = (String) val.get("vari_type");
        String value = (String) val.get("vari_value");
        String import_info = (String) val.get("import_info");
        VariableComponent vc = new VariableComponent(type,name,value,import_info);
        System.out.println("##################vc################");
        System.out.println("id:"+String.valueOf(val.get("id")));
        System.out.println(vc.getVarType());
        System.out.println(vc.getVarName());
        System.out.println(vc.getVarValue().getClass());
        System.out.println(vc.getImportStmt());
        return vc;
    }

    public Map<String,String> getAvailableVar(List<FieldDeclaration> fieldList,MethodDeclaration visitMethod,int lineIndex){
        Map<String,String> varMap = new HashMap<String,String>();
        for(FieldDeclaration field:fieldList){
            for(VariableDeclarator var:field.getVariables()){
                varMap.put(var.getNameAsString(),var.getTypeAsString());
            }
        }
        List<VariableDeclarator> varList = visitMethod.findAll(VariableDeclarator.class);
        for(VariableDeclarator var:varList){
            if(var.getBegin().get().line < lineIndex){
                varMap.put(var.getNameAsString(),var.getTypeAsString());
            }
        }
        return varMap;
    }
    public void callGenerateDec2(InsertVal curObj,BlockStmt methodBody, int stmtIndex, Statement insertStmt) {
        methodBody.addStatement(stmtIndex, insertStmt);
    }
    public List<Statement> callGenerateDec(InsertVal curObj,CompilationUnit cu,String name,String type)throws NullPointerException {
        List<Statement> varStmt = new ArrayList<Statement>();
        List<VariableComponent> varComp = new ArrayList<>();
        if(type.equals("Unsafe")){
            String importinfo = "sun.misc.Unsafe\njava.lang.reflect.Field";
            String pre = "Field theUnsafe = Unsafe.class.getDeclaredField(\"theUnsafe\");\ntheUnsafe.setAccessible(true);\n";
            String value = "(Unsafe) theUnsafe.get(null)";
            VariableComponent temp = new VariableComponent(type,name,value,importinfo,pre);
            varComp.add(temp);
        } else {
            System.out.println("Generate Declaration->"+ "name: "+ name + "type: "+type);
            varComp = generator.generateDec(curObj,name, type);
        }
        for(int i=0;i< varComp.size();i++){
            VariableComponent vc = varComp.get(i);
            if(vc.getVarName() == null || vc.getVarType() == null)continue;
            if(vc.getVarType().equals("var"))continue;
            if(StaticJavaParser.parseStatement(vc.getVarType()+" "+vc.getVarName()+" = "+vc.getVarValue().toString()+";\n")==null)
                continue;
            if(!vc.pre.equals("")){
                List<String> l = Arrays.asList(vc.pre.split("\n"));
                for (String p:l) {
                    Statement s2 = StaticJavaParser.parseStatement(p);
                    pres.add(s2);
                    varStmt.add(s2);
                }
            }
            Statement temp2 = StaticJavaParser.parseStatement(vc.getVarType()+" "+vc.getVarName()+" = "+vc.getVarValue().toString()+";\n");
            varStmt.add(temp2);
            name_and_type_statments.add(temp2);
            if(vc.getImportStmt().length()>0){
                List<String> set = new ArrayList<>();
                for(ImportDeclaration im:cu.getImports()){
                    set.add(im.toString().substring(6));
                }
                List<String> temp = Arrays.asList(vc.getImportStmt().split("\n"));
                for(String s:temp){
                    if(s.startsWith("import "))s=s.substring(6);
                    if(s.endsWith(";"))s = s.substring(0,s.length()-1);
                    if(set.contains(s))continue;
                    if(s.contains("junit.framework."))continue;
                    if(s.contains("org.testng."))continue;
                    if(s.contains("java.test."))continue;
                    if(s.contains("test.framework."))continue;
                    if(s.contains("jdk.nashorn."))continue;
                    if(s.contains("com.sun.javadoc.*"))continue;
                    if(s.contains("org.jemmy2ext."))continue;
                    if(s.contains("org.netbeans.jemmy."))continue;
                    if(s.contains("nsk.share."))continue;
//                    System.out.println(s);
                    cu.addImport(s);
                    set.add(s);
                }
            }
        }
//        System.out.println(varStmt);
        return varStmt;
    }
    public void setVarName(Statement visitStmt, String oldName, String newName) {
        visitStmt.findAll(NameExpr.class).forEach(n -> {
            if (n.getNameAsString().equals(oldName)) {
                n.setName(newName);
            }
        });
    }
    public List<Statement> setVarNameInInsertStmt(RelaStatments root,InsertVal curObj, CompilationUnit cu, MethodDeclaration visitMethod, Statement insertStmt, HashMap<String, String> needfulParam, String name, int setIndex) throws NullPointerException {
        List<Statement> newVarsDec = new ArrayList();
        if(insertStmt == null) return newVarsDec;
        Map<String, String> availableVar = new HashMap<>();
        if(!curObj.hasInsert)
                classifier.getAvailableVar(cu.findAll(FieldDeclaration.class), visitMethod, setIndex);

        List<String> name_changed = new ArrayList<>();

        List<String> list_avar_key = new ArrayList(availableVar.keySet());

        List<String> list_avar_vale = new ArrayList(availableVar.values());

        NodeList<Parameter> Par = visitMethod.getParameters();

        for(Parameter par_m :Par){
            String name2 = par_m.getName().asString();
            String type2 = par_m.getType().asString();

            if(list_avar_key.contains(name2) && list_avar_vale.contains(type2) && list_avar_key.indexOf(name2)==list_avar_vale.indexOf(type2))
                continue;
            list_avar_key.add(name2);
            list_avar_vale.add(type2);
        }

        List<String> list_need_key = new ArrayList(needfulParam.keySet());
        List<String> list_need_vale = new ArrayList(needfulParam.values());
        HashMap<String, String> common = new HashMap(); 
        HashMap<String, String> commonType_differName = new HashMap(); 
        HashMap<String, String> uncommon = new HashMap();   

        if (availableVar.size() != 0) {
            for(int index = 0; index < list_avar_vale.size(); ++index) {
                for(int index_need = 0; index_need < list_need_key.size(); ++index_need) {
                    if (list_avar_vale.get(index).equals(list_need_vale.get(index_need))) {
                        System.out.println("list_avar_key.get(index): "+list_avar_key.get(index));
                        System.out.println("list_need_key.get(index_need): "+list_need_key.get(index_need));
                        if ((list_avar_key.get(index)).equals(list_need_key.get(index_need))) {
                            common.put(list_need_key.get(index_need), list_avar_key.get(index));
                        }
                        else{
                            commonType_differName.put(list_need_key.get(index_need),list_avar_key.get(index));
                        }
                    }
                    else {
                        if (list_avar_key.get(index).equals(list_need_key.get(index_need))) {
                            this.setVarName(insertStmt, list_need_key.get(index_need), list_need_key.get(index_need) + "_change");
                            uncommon.put(list_need_key.get(index_need) + "_change", list_need_vale.get(index_need));
                        }
                        else {
                            uncommon.put(list_need_key.get(index_need), list_need_vale.get(index_need));
                        }
                    }
                }
            }
        }
        else {
            System.out.println("needfulParam:" + needfulParam);
            uncommon = needfulParam;
        }
        List<String> d = getDeclaration(insertStmt);
        for(String d1:d){
            if(uncommon.size()>0) uncommon.remove(d1);
            if(commonType_differName.size()>0) common.remove(d1);
            if(common.size()>0 && common.keySet().contains(d1)){
                setVarName(insertStmt,d1,d1+ "_change");
                common.remove(d1);
            }
        }
        for(Map.Entry<String,String> entry_com:commonType_differName.entrySet()){
            setVarName(insertStmt,entry_com.getKey(),entry_com.getValue());
            name_changed.add(entry_com.getKey());
            System.out.println("name_changed:\t"+name_changed);
            break;
        }
        for(String s:name_changed){
            if(uncommon.size()>0) uncommon.remove(s);
            if(commonType_differName.size()>0) common.remove(s);
        }

        if (uncommon.size() != 0) {
            for(Map.Entry<String,String> entry1:uncommon.entrySet()){
                if(entry1.getKey() == null || entry1.getValue() == null)continue;
                if (
                        (entry1.getValue().contains(">")&&!entry1.getValue().contains("<"))
                        || (entry1.getValue().contains("<")&&!entry1.getValue().contains(">"))
                ){
                    System.out.println("insert:"+insertStmt);
                    System.out.println(entry1.getKey()+" "+entry1.getValue());
                    continue;
                }
                name_and_type_statments = new ArrayList<>();
                name_and_type_updateNeedSetValName  = new ArrayList<>();
                List<Statement> cres = callGenerateDec(curObj,cu,entry1.getKey(),entry1.getValue());
                newVarsDec.addAll(cres);
                RelaStatments newR = null;
                for(int i=0;i<curObj.name_and_type_statments.size();i++){
                    if(i<curObj.name_and_type_updateNeedSetValName.size()) {
                        newR = new RelaStatments(curObj.name_and_type_statments.get(i),
                                curObj.name_and_type_updateNeedSetValName.get(i));
                    }else{
                       newR = new RelaStatments(curObj.name_and_type_statments.get(i));
                    }
                    if(curObj.pres.size()>0){
                        newR.pre = new ArrayList<>(curObj.pres);
                        curObj.pres = new ArrayList<>();
                    }
                    root.nexts.add(newR);
                }
            }
        }
        else {
            curObj.hasInsert = false;
        }
        return newVarsDec;
    }
    public List<String> getDeclaration(Statement insertStmt){
        List<String> res = new ArrayList<>();
        for(VariableDeclarator variable : insertStmt.findAll(VariableDeclarator.class)) {
            String valname = variable.getNameAsString();
            res.add(valname);
        }
        return res;
    }
}
