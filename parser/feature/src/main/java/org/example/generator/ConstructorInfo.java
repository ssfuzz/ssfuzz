package org.example.generator;


public class ConstructorInfo {
    String className;
    String constructorStmt;
    String packageName;

    public ConstructorInfo(String className, String constructorStmt, String packageName) {
        this.className = className;
        this.constructorStmt = constructorStmt;
        this.packageName = packageName;
    }

    public String getClassName() {
        return this.className;
    }

    public String getConstructorStmt() {
        return this.constructorStmt;
    }

    public String getPackageName() {
        return this.packageName;
    }
}

