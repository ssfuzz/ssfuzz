package org.example;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.io.File;
import java.io.IOException;


public class SmbTypeSolver {
    public static CombinedTypeSolver generateTypeSolver(String sourcePath) throws IOException {
        CombinedTypeSolver solver = new CombinedTypeSolver();
        solver.add(new JavaParserTypeSolver(sourcePath));
        solver.add(new ReflectionTypeSolver(false));
        return solver;
    }
    public static JavaParser testTypeSolver(File base,String sourcePath,String libPath) throws IOException {
        String p;
        if(base == null) p ="";
        else p = base.getCanonicalPath();
        TypeSolver typeSolver = generateTypeSolver( p+ File.separator + sourcePath);
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
        ParserConfiguration configuration = new ParserConfiguration();
        configuration.setSymbolResolver(symbolSolver);
        JavaParser parser = new JavaParser(configuration);
        return parser;
    }
}
