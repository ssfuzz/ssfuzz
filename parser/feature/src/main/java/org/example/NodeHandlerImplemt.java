package org.example;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.Type;

public class NodeHandlerImplemt implements NodeIterator.NodeHandler {
    @Override
    public boolean handle(Node node) {
        if(node==null)return false;
        System.out.println("a new node:");
        return processNode(node);
    }

    public static boolean processNode(Node node) {
        System.out.println(node.getClass());
        System.out.println(node);
        if (node instanceof TypeDeclaration) {
            System.out.println("TypeDeclaration:");
            System.out.println(node);
            return false;

        } else if (node instanceof MethodDeclaration) {
            System.out.println("MethodDeclaration:");
            String methodName = ((MethodDeclaration) node).getName().getIdentifier();
            System.out.println("方法: " + methodName);
            return true;
        } else if (node instanceof FieldDeclaration) {
            System.out.println("FieldDeclaration:");
            return false;
        }else if(node instanceof Modifier || node instanceof SimpleName ||node instanceof Type || node instanceof Parameter){
            return false;
        }else if(node instanceof BlockStmt){
            System.out.println("run into BlockStmt");

            return true;
        }else if(node instanceof ClassOrInterfaceDeclaration){
            System.out.println("ClassOrInterfaceDeclaration:");
            System.out.println(node);
        }
        return false;
    }
}
