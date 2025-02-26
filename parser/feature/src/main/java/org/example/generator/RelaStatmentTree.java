package org.example.generator;


import com.github.javaparser.ast.stmt.Statement;

import java.util.ArrayList;
import java.util.List;

public class RelaStatmentTree {
    public RelaStatments root = null;
    public int depths = 0;
    public List<RelaStatments> postValue = new ArrayList<>();
    public RelaStatmentTree(){}

    public RelaStatmentTree(RelaStatments root){
        this.root = root;
        this.depths = 1;
    }
    public void postSearch(RelaStatments root){
        if(root==null)return;
        for(RelaStatments next:root.nexts){
            postSearch(next);
        }
        postValue.add(root);
    }
}
