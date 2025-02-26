package org.example;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public final class CommentsRemover {
    private CommentsRemover() {}
    private static int countFail = 0;
    private static List<String> paths = new ArrayList<>();
    public static String doAction(String content,String path) {
        JavaParser javaParser = createJavaParser();
        ParseResult<CompilationUnit> result = javaParser.parse(content);
        Optional<CompilationUnit> optionalCompilationUnit = result.getResult();
        if (!optionalCompilationUnit.isPresent()) {
            return "";
        }
        CompilationUnit compilationUnit = optionalCompilationUnit.get();
        removeComments(compilationUnit,path);
        return LexicalPreservingPrinter.print(compilationUnit);
    }

    private static void removeComments(CompilationUnit compilationUnit,String path) {
        List<Comment> comments = compilationUnit.getAllContainedComments();
        List<Comment> unwantedComments = new ArrayList<>(comments
                .stream()
                .filter(CommentsRemover::isValidCommentType)
                .collect(Collectors.toList()));
        try {
            for(Comment c:unwantedComments){
                c.remove();
            }

        }catch (UnsupportedOperationException e1){
            System.out.println(e1);
            countFail++;
            paths.add(path);
        }
        catch(IllegalStateException e2){
            System.out.println(e2);
            countFail++;
            paths.add(path);
        }

    }

    public static JavaParser createJavaParser() {
        ParserConfiguration parserConfiguration = new ParserConfiguration();
        parserConfiguration.setLexicalPreservationEnabled(true);
        return new JavaParser(parserConfiguration);
    }

    private static boolean isValidCommentType(Comment comment) {
        return comment instanceof LineComment || comment instanceof BlockComment || comment instanceof JavadocComment;
    }
}
