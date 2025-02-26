package org.example.generator;

import com.github.javaparser.ast.stmt.Statement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RelaStatments {
    public List<RelaStatments> nexts = new ArrayList<>();
    RelaStatments parent;
    RelaStatments root;
    public List<Statement> pre;
    public Statement value = null;
    public HashMap<String, String> varList = new HashMap<>();


    public RelaStatments(){}
    public RelaStatments(Statement value){
        this.value = value;
    }
    public RelaStatments(Statement value,HashMap<String,String> list){
        this.value = value;
        this.varList = list;
    }
    public RelaStatments(Statement value,HashMap<String,String> list,List<RelaStatments> nexts){
        this.value = value;
        this.varList = list;
        this.nexts = nexts;
    }
}
