package org.example.generator;

import org.example.JDBCUtils;

import java.util.HashMap;

public class EnergyUpdate {
    public HashMap<Integer,Double> result = new HashMap<>();
    public EnergyUpdate(){}
    public void update(int Mutation_Method){
        updateResult(Mutation_Method);
        write2DB();
    }
    public void updateResult(int Mutation_Method){
//        JDBCUtils.pro = "";
    }
    public void write2DB(){
        if(result.size()==0)return;

    }
}
