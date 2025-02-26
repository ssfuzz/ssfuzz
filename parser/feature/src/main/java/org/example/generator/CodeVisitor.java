package org.example.generator;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
//import org.apache.commons.lang3.StringUtils;
import org.example.InsertMain;
import org.example.RecordLog;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CodeVisitor {
    int failNum = 0;
    int hasParaNum = 0;
    int noParaNum = 0;
    int canNotGenerateNum = 0;
    int callItselfNum = 0;
    private static VariableGenerater varGenerater = new VariableGenerater();
    private static final Logger logger = RecordLog.logger;

    public CodeVisitor() {
    }

    public void setAllMethodStatic(List<MethodDeclaration> allMethod) {
        Iterator var2 = allMethod.iterator();

        while(var2.hasNext()) {
            MethodDeclaration oneMethod = (MethodDeclaration)var2.next();
            oneMethod.setStatic(true);
        }

    }

    public void addThrowExceptionToMain(MethodDeclaration mainMethod) {
        mainMethod.addThrownException(new ClassOrInterfaceType("Exception"));
    }

    public String getNameWithMaxCount(Map<String, List<Object>> map) {
        int maxValue = 0;
        String maxName = "";
        Iterator var4 = map.entrySet().iterator();

        while(var4.hasNext()) {
            Map.Entry<String, List<Object>> entry = (Map.Entry)var4.next();
            int tmpValue = (Integer)((List)entry.getValue()).get(1);
            if (tmpValue >= maxValue) {
                maxValue = tmpValue;
                maxName = (String)entry.getKey();
            }
        }

        return maxName;
    }

    public void setMethodReturnType(MethodDeclaration n) {
        Map<String, List<Object>> varMap = new HashMap();
        Iterator var3 = n.getParameters().iterator();

        String varName;
        String varType;
        while(var3.hasNext()) {
            Parameter para = (Parameter)var3.next();
            varName = para.getNameAsString();
            varType = para.getType().toString();
            varMap.put(varName, new ArrayList(Arrays.asList(varType, 0)));
        }

        var3 = ((BlockStmt)n.getBody().get()).findAll(VariableDeclarator.class).iterator();

        while(var3.hasNext()) {
            VariableDeclarator varDeclarator = (VariableDeclarator)var3.next();
            if (!varDeclarator.findAncestor(IfStmt.class).isPresent() && !varDeclarator.findAncestor(ForStmt.class).isPresent() && !varDeclarator.findAncestor(WhileStmt.class).isPresent() && !varDeclarator.findAncestor(TryStmt.class).isPresent()) {
                varName = varDeclarator.getNameAsString();
                varType = varDeclarator.getType().toString();
                if (!varName.equals("i")) {
                    varMap.put(varName, new ArrayList(Arrays.asList(varType, 0)));
                }
            }
        }

        List<NameExpr> expressionList = ((BlockStmt)n.getBody().get()).findAll(NameExpr.class);
        expressionList.forEach((e) -> {
            String varName2 = e.getNameAsString();
            if (varMap.containsKey(varName2)) {
                List<Object> varInfo = (List)varMap.get(varName2);
                varInfo.set(1, (Integer)varInfo.get(1) + 1);
            }

        });
        String maxName = this.getNameWithMaxCount(varMap);
        ((BlockStmt)n.getBody().get()).addStatement(new ReturnStmt(new NameExpr(maxName)));
        varName = ((List)varMap.get(maxName)).get(0).toString();
        n.setType(new ClassOrInterfaceType(varName));
    }

    public boolean hasCallItself(MethodDeclaration n) {
        List<MethodCallExpr> allMethodCall = n.findAll(MethodCallExpr.class);
        Iterator var3 = allMethodCall.iterator();

        MethodCallExpr oneMethodCall;
        do {
            if (!var3.hasNext()) {
                return false;
            }

            oneMethodCall = (MethodCallExpr)var3.next();
        } while(!oneMethodCall.getNameAsString().equals(n.getNameAsString()));

        return true;
    }

    public MainMethodComponent visitMethod(InsertVal curObj, List<String> globalVarList, MethodDeclaration n, String newClassName) {
        String allParaName = "";
        List<VariableComponent> needfulArgList = new ArrayList();
        NodeList<Parameter> paraList = n.getParameters();
        String methodName;
        if (paraList.size() == 0) {
            ++this.noParaNum;
        } else {
            ++this.hasParaNum;

            for(int i = 0; i < paraList.size(); ++i) {
                Parameter para = (Parameter)paraList.get(i);
                methodName = para.getNameAsString();
                if (!globalVarList.contains(methodName)) {
                    String paraType = para.getTypeAsString().replace("java.lang.", "").replace("java.io.", "").replace("java.util.", "");
                    needfulArgList.addAll(varGenerater.generateDec(curObj,methodName, paraType));
                }

                allParaName = allParaName + methodName;
                if (i < paraList.size() - 1 && paraList.size() > 1) {
                    allParaName = allParaName + ",";
                }
            }
        }

        String runStmt = "";
        String methodType = n.getTypeAsString();
        methodName = n.getNameAsString();
        if (methodType.equals("void") && paraList.size() > 0) {
            this.setMethodReturnType(n);
        }

        if (methodType.equals("void")) {
            runStmt = "new " + newClassName + "()." + methodName + "(" + allParaName + ");\n";
        } else if (methodType.contains("[]")) {
            int dimension = varGenerater.strCount(methodType, "[]");
            if (dimension >= 2) {
                for(int i = 0; i < dimension; ++i) {
                    runStmt = runStmt + "\tSystem.out.println(Arrays.asList(new " + newClassName + "()." + methodName + "(" + allParaName + ")[" + i + "]));\n";
                }
            } else {
                runStmt = "\tSystem.out.println(Arrays.asList(new " + newClassName + "()." + methodName + "(" + allParaName + ")));\n";
            }
        } else if (methodType.contains("Map")) {
            runStmt = "\tnew " + newClassName + "()." + methodName + "(" + allParaName + ").forEach((a,b)->System.out.println(a+b));";
        } else {
            runStmt = "\tSystem.out.println(new " + newClassName + "()." + methodName + "(" + allParaName + "));\n";
        }

        MainMethodComponent mainMethod = new MainMethodComponent(needfulArgList, runStmt, VariableGenerater.implementationClassList);
        VariableGenerater.implementationClassList.clear();
        return mainMethod;
    }

    public boolean alreadyIn(NodeList<ImportDeclaration> importList, String importName) {
        Iterator var3 = importList.iterator();

        String tmpImportName;
        do {
            if (!var3.hasNext()) {
                return false;
            }

            ImportDeclaration oneImport = (ImportDeclaration)var3.next();
            tmpImportName = oneImport.getNameAsString();
        } while(!tmpImportName.equals(importName));

        return true;
    }

    public void checkAndAddImports(CompilationUnit cu, String oneImportStr) {
        String[] realImportArray = oneImportStr.split("\n");
        String[] var4 = realImportArray;
        int var5 = realImportArray.length;

        for(int var6 = 0; var6 < var5; ++var6) {
            String oneImport = var4[var6];
            if (!this.alreadyIn(cu.getImports(), oneImport)) {
                cu.addImport(oneImport);
            }
        }

    }

    public void visitGloablVar(ClassOrInterfaceDeclaration classNode) {
        Iterator var2 = classNode.getFields().iterator();

        while(var2.hasNext()) {
            FieldDeclaration field = (FieldDeclaration)var2.next();
            field.setStatic(true);
            field.getVariables().forEach((v) -> {
                if (!v.getInitializer().isPresent()) {
                    String fieldName = v.getNameAsString();
                    String fieldType = v.getTypeAsString().replace("java.lang.", "").replace("java.io.", "").replace("java.util.", "");
                    v.setInitializer(varGenerater.generateValueOfDec(fieldType));
                    VariableGenerater var10000 = varGenerater;
                    if (VariableGenerater.implementationClassList.size() > 0) {
                        var10000 = varGenerater;
                        VariableGenerater.implementationClassList.forEach((imp) -> {
                            this.checkAndAddImports((CompilationUnit)v.findCompilationUnit().get(), imp);
                        });
                    }
                }

            });
        }

    }

    public String createTestcase(InsertVal curObj,CompilationUnit oldCUnit, int id) {
        String newClassName = "MyJVMTest_" + id;
        List<MethodDeclaration> allMethod = oldCUnit.findAll(MethodDeclaration.class);
        MethodDeclaration extractedMethod = (MethodDeclaration)allMethod.get(allMethod.size() - 1);
        if (this.hasCallItself(extractedMethod)) {
            ++this.callItselfNum;
            return "";
        } else {
            List<String> globalVarList = (List)oldCUnit.findAll(FieldDeclaration.class).stream().map((f) -> {
                return f.getVariable(0).getNameAsString();
            }).collect(Collectors.toList());
            MainMethodComponent mainMethodComponent = this.visitMethod(curObj,globalVarList, extractedMethod, newClassName);
            CompilationUnit newCUnit = new CompilationUnit();
            ClassOrInterfaceDeclaration oldClass = (ClassOrInterfaceDeclaration)oldCUnit.findAll(ClassOrInterfaceDeclaration.class).get(0);
            this.visitGloablVar(oldClass);
            NodeList<ImportDeclaration> importList = oldCUnit.getImports();
            importList.forEach((importNode) -> {
                newCUnit.addImport(importNode);
            });
            Iterator var11 = mainMethodComponent.getImplementationClassList().iterator();

            while(var11.hasNext()) {
                String implementationClass = (String)var11.next();
                if (!this.alreadyIn(importList, implementationClass)) {
                    this.checkAndAddImports(newCUnit, implementationClass);
                }
            }

            ClassOrInterfaceDeclaration newClass = newCUnit.addClass(newClassName);
            List<VariableComponent> needfulArgList = mainMethodComponent.getParaList();

            try {
                for(int i = 0; i < needfulArgList.size(); ++i) {
                    VariableComponent oneComponent = (VariableComponent)needfulArgList.get(i);
                    newClass.addFieldWithInitializer(oneComponent.getVarType(), oneComponent.getVarName(), oneComponent.getVarValue(), new Modifier.Keyword[]{Modifier.Keyword.STATIC});
                    if (oneComponent.getImportStmt().length() > 0) {
                        this.checkAndAddImports(newCUnit, oneComponent.getImportStmt());
                    }
                }
            } catch (Exception var22) {
                logger.info("Error when add needful arguments as gloabl variables!\n" + var22.getMessage());
            }

            Iterator var25 = oldClass.getMembers().iterator();

            while(var25.hasNext()) {
                BodyDeclaration<?> member = (BodyDeclaration)var25.next();
                newClass.addMember(member);
            }

            MethodDeclaration mainMethod = newClass.addMethod("main", new Modifier.Keyword[]{Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC});
            BlockStmt methodBody = mainMethod.createBody();
            String callStmtStr = mainMethodComponent.getCallStatement();
            String[] callStmtArray = callStmtStr.split("\n");
            String[] var17 = callStmtArray;
            int var18 = callStmtArray.length;

            for(int var19 = 0; var19 < var18; ++var19) {
                String callStmt = var17[var19];
                methodBody.addStatement(callStmt);
            }

            mainMethod.addParameter(String[].class, "args");
            this.addThrowExceptionToMain(mainMethod);
            if (callStmtStr.contains("Arrays")) {
                this.checkAndAddImports(newCUnit, "java.util.Arrays");
            }

            String code = "";

            try {
                code = newCUnit.toString();
            } catch (Exception var21) {
                ++this.failNum;
                logger.info("Content:\n{}Error message:\n{}"+needfulArgList+"\n"+var21.getMessage());
            }

            return code;
        }
    }

    class MainMethodComponent {
        private List<VariableComponent> paraList;
        private String callStatement;
        private List<String> implementationClassList;

        public MainMethodComponent(List<VariableComponent> paraList, String callStatement, List<String> implementationClassList) {
            this.paraList = paraList;
            this.callStatement = callStatement;
            this.implementationClassList = implementationClassList;
        }

        public List<VariableComponent> getParaList() {
            return this.paraList;
        }

        public String getCallStatement() {
            return this.callStatement;
        }

        public List<String> getImplementationClassList() {
            return this.implementationClassList;
        }
    }
}
