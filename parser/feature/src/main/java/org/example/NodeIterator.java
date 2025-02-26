package org.example;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;

import java.util.ArrayList;
import java.util.List;

public class NodeIterator {
    public List<Node> AllTypedChildNode;
    public List<Node> AllChildNodes;
    public interface NodeHandler {
        boolean handle(Node node);
    }

    private NodeHandler nodeHandler;

    public NodeIterator(NodeHandler nodeHandler) {
        this.nodeHandler = nodeHandler;
        this.AllChildNodes = new ArrayList<>();
    }

    public void explore(Node node) {
        boolean handlerResult = nodeHandler.handle(node);
        if (handlerResult){
            if(node instanceof BlockStmt){
                System.out.println(node.getClass());
                AllChildNodes.addAll(separator(node));
            }
            else{
                for (Node child : node.getChildNodes()) {
                    explore(child);
                }
            }
        }
    }

    public static List<Node> separator(Node node) {
        List<Node> StmtPieces = new ArrayList<>();
        System.out.println(node.getChildNodes());
        for(Node n:node.getChildNodes()){
            if(n instanceof Statement) {
                System.out.println("BlockStmt childnode: " + n);
                System.out.println(n.getClass());
                StmtPieces.add(n);
            }
        }
        return StmtPieces;
    }
    public List<Node> getAllChildNodes(){
        System.out.println("size is:"+AllChildNodes.size());
        return AllChildNodes;
    }

}