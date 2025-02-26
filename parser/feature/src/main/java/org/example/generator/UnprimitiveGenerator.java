package org.example.generator;


import org.example.InsertMain;
import org.example.JDBCUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class UnprimitiveGenerator {
    static String[] noNeedChangeSet = new String[]{"int", "short", "long", "byte", "char", "float", "double", "String"};
    static final String variableNamePattern = "Param";
    String[] unaryOperator = new String[]{"++", "--", "+", "-", "~"};
    String[] binaryOperator = new String[]{"+", "-", "*", "/", "%", "&", "|", "^", ">>", "<<"};
    List<String> availableFunctionalInterface = new ArrayList(Arrays.asList("UnaryOperator", "BinaryOperator", "IntUnaryOperator", "IntBinaryOperator", "LongUnaryOperator", "LongBinaryOperator", "DoubleUnaryOperator", "DoubleBinaryOperator", "Runnable"));

    public UnprimitiveGenerator() {
    }

    public boolean isUnprimitive(String type) {
        type = type.replace("[]", "");
        return !Arrays.asList(noNeedChangeSet).contains(type);
    }

    public int generateChooseNum(int min, int max) {
        if (min == max) {
            return min;
        } else {
            Random random = new Random();
            return random.nextInt(max - min) + min;
        }
    }

    public VariableComponent generateFunctional(String varType, String varName) {
        String lambdaStr = "";
        String importStmt = "";
        int chooseNum;
        switch (varType) {
            case "UnaryOperator":
            case "DoubleUnaryOperator":
            case "LongUnaryOperator":
            case "IntUnaryOperator":
                chooseNum = this.generateChooseNum(0, this.unaryOperator.length);
                lambdaStr = "(jvmVarA) -> " + this.unaryOperator[chooseNum] + "jvmVarA";
                importStmt = importStmt + "java.util.function." + varType;
                break;
            case "BinaryOperator":
            case "DoubleBinaryOperator":
            case "LongBinaryOperator":
            case "IntBinaryOperator":
                chooseNum = this.generateChooseNum(0, this.binaryOperator.length);
                lambdaStr = "(jvmVarA,jvmVarB) -> jvmVarA" + this.binaryOperator[chooseNum] + "jvmVarB";
                importStmt = importStmt + "java.util.function." + varType;
                break;
            default:
                lambdaStr = "() -> {}";
        }

        return new VariableComponent(varType, varName, lambdaStr, importStmt);
    }

    public VariableComponent generateIntAsParam(String varName) {
        return new VariableComponent("int", varName, String.valueOf(this.generateChooseNum(0, 1000)), "");
    }
    private static int getNum(String originStr, String targetStr){
        int res = 0;
        int i = originStr.indexOf(targetStr);
        while (i != -1){
            i = originStr.indexOf(targetStr,i+1);
            res++;
        }
        return res;
    }

    public List<VariableComponent> generateUnprimitive(InsertVal curObj, String varName, String varType) {
        VariableGenerater varGenerater = new VariableGenerater();
        List<VariableComponent> compList = new ArrayList();
        String rootImportStmt = "";
        if (varType.equals("Path")) {
            compList.add(new VariableComponent(varType, varName, "FileSystems.getDefault().getPath(\"logs\", \"access.log\")", "import java.nio.file.FileSystems;\n"));
            return compList;
        } else if (this.availableFunctionalInterface.contains(varType)) {
            compList.add(this.generateFunctional(varType, varName));
            return compList;
        } else {
            //数据库查询类信息
            String implementingClassName = "";
            String oneConstructor = "";
            ConstructorInfo constructorInfo = null;
            String importStmt;
            String[] tmpArray;
            int i;
            try {
                JDBCUtils j = new JDBCUtils();
//                JDBCUtils.pro = "jdbc.properties";
                constructorInfo = j.getConstructorInfo2(curObj,varType);
                importStmt = constructorInfo.getConstructorStmt();
                if (importStmt.length() != 0) {
                    String newClassValue = importStmt;
                    String[] temp = constructorInfo.getPackageName().split("\n");
                    for (String st:temp) {
                        st = st.substring(0,st.length()-1).substring(6);
                        rootImportStmt = rootImportStmt + st + "\n";
                    }
                    compList.add(new VariableComponent(varType, varName, newClassValue, rootImportStmt));
                    curObj.hasInsert = true;

                    return compList;
                }
//                JDBCUtils.pro = "jdbc.properties";
                constructorInfo = j.getConstructorInfo(varType);
                importStmt = constructorInfo.getConstructorStmt();
                if (importStmt.length() == 0){
                    throw new NoAvailableConstructorException("Do not find available constructor");
                }
                tmpArray = importStmt.split("\n");
                i = this.generateChooseNum(0, tmpArray.length);
                oneConstructor = tmpArray[i];
                if (!constructorInfo.getClassName().equals(varType)) {
                    implementingClassName = constructorInfo.getClassName();
                }

                rootImportStmt = constructorInfo.getPackageName() + "." + varType + "\n";
            } catch (Exception var16) {
                compList.add(new VariableComponent(varType, varName, "null"));
                return compList;
            }

            String newClassValue = "";
            importStmt = rootImportStmt;
            if (implementingClassName.length() > 0) {
                importStmt = rootImportStmt + constructorInfo.getPackageName() + "." + implementingClassName;
                if (oneConstructor.contains("()")) {
                    newClassValue = "new " + implementingClassName + "()";
                    compList.add(new VariableComponent(varType, varName, newClassValue, importStmt));
                    return compList;
                }

                newClassValue = "new " + implementingClassName + "(";
            } else {
                if (oneConstructor.contains("()")) {
                    newClassValue = "new " + varType + "()";
                    compList.add(new VariableComponent(varType, varName, newClassValue, rootImportStmt));
                    return compList;
                }

                newClassValue = "new " + varType + "(";
            }
//            tmpArray = oneConstructor.split("\\(|, |,|\\)");
//            System.out.println("tmpArray:"+Arrays.toString(tmpArray));
            String[] arr1 = oneConstructor.split("\\\\(|, |,|\\\\)");
            List<String> tmpArray2 = new ArrayList<>();
            String temp2 = "";
            for(int index = 0; index < arr1.length; index++){
                if(!temp2.equals(""))temp2+=',';
                temp2 += arr1[index];
                int count1 = getNum(temp2,"<");
                int count2 = getNum(temp2,">");
                if(count1 == count2){
                    tmpArray2.add(temp2);
                    temp2 = "";
                }
            }
            tmpArray = tmpArray2.toArray(new String[0]);
            System.out.println("tmpArray2:"+Arrays.toString(tmpArray));
            for(i = 1; i < tmpArray.length - 1; ++i) {
                String variableType = tmpArray[i];
                String variableName = varName + "Param" + i;
                if (variableType.equals("int")) {
                    compList.add(this.generateIntAsParam(variableName));
                } else if (variableType.equals("?>")) {
                    compList.add(new VariableComponent());
                } else {
                    compList.addAll(varGenerater.generateDec(curObj,variableName, variableType));
                }

                newClassValue = newClassValue + variableName;
                if (i < tmpArray.length - 2) {
                    newClassValue = newClassValue + ", ";
                }
            }

            newClassValue = newClassValue + ")";
            compList.add(new VariableComponent(varType, varName, newClassValue, importStmt));
            return compList;
        }
    }

    class NoAvailableConstructorException extends Exception {
        NoAvailableConstructorException(String s) {
            super(s);
        }
    }
}
