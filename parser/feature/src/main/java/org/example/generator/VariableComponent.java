package org.example.generator;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.expr.Expression;
import org.example.RecordLog;


public class VariableComponent {
    String varType;
    String varName;
    Expression varValue;
    String importStmt = "";
    String pre = "";
    String varDec;
    public VariableComponent(){
    }
    public VariableComponent(String varType, String varName, String varValue) {
        this.varType = varType;
        this.varName = varName;
        this.varDec = varType + " " + varName + " = " + varValue;
        this.varValue = this.formatVarValue(this.varDec);
    }
    public VariableComponent(String varType, String varName, String varValue, String importStmt,String preStmt){
        this.varType = varType;
        this.varName = varName;
        this.varDec = varType + " " + varName + " = " + varValue;
        this.varValue = this.formatVarValue(this.varDec);
        this.importStmt = importStmt;
        this.pre = preStmt;
    }
    public VariableComponent(String varType, String varName, String varValue, String importStmt) {
        this.varType = varType;
        this.varName = varName;
        this.varDec = varType + " " + varName + " = " + varValue;
        System.out.println("varType: "+varType);
        System.out.println("varName: "+varName);
        System.out.println("varValue: "+varValue);
        this.varValue = this.formatVarValue(this.varDec);
        this.importStmt = importStmt;
    }
    private Expression formatVarValue(String varDec) {
        Expression formativeVarValue;
        try {
//            System.out.println("varDec: " + varDec);
            formativeVarValue = StaticJavaParser.parseVariableDeclarationExpr(varDec).getVariable(0).getInitializer().get();
        } catch (Exception var5) {
            RecordLog.logger.info("error: "+varDec + "\n\n" + var5.getMessage());
            formativeVarValue = StaticJavaParser.parseExpression("null");
        }
        return formativeVarValue;
    }

    public String getVarType() {
        return this.varType;
    }

    public String getVarName() {
        return this.varName;
    }

    public Expression getVarValue() {
        return this.varValue;
    }

    public String getImportStmt() {
        return this.importStmt;
    }

    public String getVarDec() {
        return this.varDec + ";\n";
    }
}
