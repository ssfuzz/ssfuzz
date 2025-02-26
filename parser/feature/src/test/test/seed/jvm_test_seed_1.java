package org.example;

import java.util.*;

public class jvm_test_seed_1 {
    public int TEST = 10;
    public String str = "HELLO WORLD!";
    public int TEST2 = TEST;
//    public int a = TEST;
//    public static int test2(){
//        System.out.println("test2");
//        int a = 10;
//        return 100;
//    }
//    public int test3(){
//        return 150;
//    }
//    public static void M2(){
//        for(int i=0;i<100;i+=1){
//            System.out.println(this.TEST);
//            int a = this.TEST+this.TEST2;
//            int b = this.str.substring(this.TEST);
//        }
//    }
//    public static void M3(){
//        List<Integer> list = new ArrayList();
//        list.add(1);
//        list.add(this.TEST);
//        for (Integer i:list) {
//            System.out.println(i);
//            int a = this.TEST+i;
//        }
//    }
//    public static void M4(){
//        int m = 100;
//        int k2;
//        while(m>10){
//            int k = 100/m;
//            k2+=m;
//            m--;
//        }
//    }
    public static void M5(){
        if(this.TEST<this.TEST2){
            System.out.println();
        } else if (this.TEST>this.TEST2) {
            System.out.println();
        }
        for(int i=0;i<this.TEST;i++){
            for(int k=0;k<this.TEST2;k+=2){
                System.out.println("hello");
            }
        }
        int i = this.str.indexOf("H");
        String m1 = this.str.substring(this.TEST,this.str);
        String m2 = this.str.substring(str.toString());
        String m3 = this.str.substring(new Test(a,b,this.str.indexof("a")));
//        m = m+"abc";
//        i+=this.TEST;
        Test t2 = new Test(this.str.indexof("A"));
        t.m5();
        t.abdc(m1,m2);
        int a = t.m5(b1,c1,d1)+t.abdced(m1,m2);
    }
    public static void M1(){
        if(this.TEST>10){
            System.out.println();
        }else if(this.TEST<=10 && this.TEST>=5){
            System.out.println();
        }else {
            System.out.println();
        }
    }
//    public static void test(){
//        int a1 = TEST;
//        int k1 = 1+((TEST+3)*(a1+100));
//        boolean a2 = false;
//        byte a3 = (byte)(a1+2);
//        long a4 = (long)2;
//        char a5 = 'a';
//        double a6 = 1.0;
//        float a7 = 1.5;
//        int[][][] arr11 = new int[3][2][1];
//        int[][] arr1 = new int[1][2];
//        int[][] arr1 = new int[2][];
//        int[] arr2 = new int[TEST];
//        int[][] arr3 = new int[][]{{TEST, 1}, {2, TEST},{0,0}};
//        int arr4 = arr3[0][1];
//        int[] arr3 = {1,2,TEST};
//        int b3 = (TEST*2)+1;
//        String s = str.subString(0,3);
//        Random r4 = new Random(TEST,2);
//        Random r5 = new Random();
//        String s5 = "hello s5";
//        String s6 = s5+" s6";
//        String s7 = String.valueOf(r4)+" s7";
//        int a91 =this.TEST;
//        int a92 =this.TEST+10;
//        int a10 = jvm_test_seed_1.TEST;
//        int a11 = jvm_test_seed_1.test2();
//        int a12 = Math.pow(1,10);
//        jvm_test_seed_1 obj1 = new jvm_test_seed_1();
//        int a13 = obj1.test3();
//        int a14 = obj1.TEST;
//        int a15;
//    }
    public static void main( String[] args ){
        TestClass t = new TestClass();
        t.M1();
        System.out.println( "Hello World!");
    }

}
class TestClass{
    int val = 1000;
    TestClass(){}
    void m1(){
        System.out.println("testClass m1.");
    }
}