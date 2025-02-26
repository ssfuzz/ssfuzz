package org.example;

import java.util.*;

public class jvm_test_target_1 {
    public static void main(String[] args){
        System.out.println( "Hello World!");
        List<String> used_id1 = new ArrayList<>();
        String s = "abc";
        System.out.println(s.substring(0));
        used_id1.add(s);
        System.out.println(used_id1.size());
    }
}
