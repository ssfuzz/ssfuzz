package org.example;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.modules.ModuleDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.utils.SourceRoot;
import org.example.generator.InsertVal;
import org.example.generator.RelaStatmentTree;
import org.example.generator.RelaStatments;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.List;


import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class InsertMain {
    InsertVal mutationbyInsert;
    static CreateJavaFile createFile = new CreateJavaFile();
    int number = 10000;
    static List<Integer> used_names = new ArrayList<>();
    List<String> mth = new ArrayList<>(List.of(new String[]{"toString", "equals", "wait", "finalize", "hashCode", "notify", "clone", "getClass", "notifyAll"}));

    public List<CompilationUnit> FindJava(SourceRoot sourceRoot) throws IOException {
        return sourceRoot.getCompilationUnits();
    }

    public Map<String, List<CompilationUnit>> groupList(List<CompilationUnit> list) {
        if (list != null && list.size() != 0) {
            int listSize = list.size();
            int toIndex = this.number;
            Map<String, List<CompilationUnit>> map = new HashMap();
            int keyToken = 0;

            for (int i = 0; i < list.size(); i += this.number) {
                if (i + this.number > listSize) {
                    toIndex = listSize - i;
                }

                List<CompilationUnit> newList = list.subList(i, i + toIndex);
                map.put("keyName" + keyToken, newList);
                ++keyToken;
            }

            return map;
        } else {
            return new HashMap();
        }
    }

    public void addStmtBetweenStmt(BlockStmt methodBody, int stmtIndex, Statement insertStmt) {
        methodBody.addStatement(stmtIndex, insertStmt);
    }

    //    compilations from pathAll, filepath, seedpath
    public Statement Insert_Cov(List<CompilationUnit> Seed_compilations, String FilePath, String Seed_file, int file_num) throws   Exception {
        Map<String, List<CompilationUnit>> seed_Map = this.groupList(Seed_compilations); 
        System.out.println("grouped success: total " + seed_Map.size() + " group!");
        for (Map.Entry<String, List<CompilationUnit>> entry_com : seed_Map.entrySet()) {
            List<CompilationUnit> temp = new ArrayList<>(entry_com.getValue());
            Insert(temp, FilePath + "/" + file_num, Seed_file + "/" + file_num);
            file_num++;
        }

        return null;
    }

    public Statement deletemethods(Statement stm) {
        List<MethodCallExpr> list = stm.findAll(MethodCallExpr.class);
        List<MethodCallExpr> unwanted = list
                .stream()
                .filter(p -> !p.getScope().isPresent() || (p.toString().contains("this") && !mth.contains(p.getNameAsString())))
                .collect(Collectors.toList());
        try {
            unwanted.forEach(Node::removeForced);
        } catch (Exception e) {
            System.out.println(e);
        }
        return stm;
    }

    public void deletevalThis(Statement stm) {
        List<FieldAccessExpr> res = stm.findAll(FieldAccessExpr.class);
        for (FieldAccessExpr n : res) {
            if (n.toString().contains("this.")) {
                NameExpr n2 = new NameExpr(n.getName());
                n.replace(n2);
            }
        }
    }

    public static void swap(int[] nums, int i, int j) {
        int temp = nums[i];
        nums[i] = nums[j];
        nums[j] = temp;
    }

    public static int search(int[] nums) {
        int len = nums.length;
        for (int i = 0; i < len; i++) {
            while (nums[i] > 0 && nums[i] < len && nums[i] - 1 != i && nums[nums[i] - 1] != nums[i]) {
                swap(nums, nums[i] - 1, i);
            }
        }
        for (int i = 0; i < len; i++) {
            if (nums[i] != i + 1) {
                return i + 1;
            }
        }
        return len + 1;
    }

    public Statement removeComment(Statement stm) {
        List<Comment> comments = stm.getAllContainedComments();
        List<Comment> unwantedComments = comments
                .stream()
                .filter(p -> !p.getCommentedNode().isPresent() || p instanceof Comment)
                .collect(Collectors.toList());
        try {
            unwantedComments.forEach(Node::remove);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stm;
    }

    public Statement deleteReturn(Statement stm) {
        List<ReturnStmt> s = stm.findAll(ReturnStmt.class);
        if (s.size() == 0) return stm;
        List<ReturnStmt> unwanteds = s
                .stream()
                .filter(p -> p instanceof ReturnStmt)
                .collect(Collectors.toList());
        try {
            unwanteds.forEach(Node::remove);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stm;
    }

    public void addImports(CompilationUnit cu, String imps) {
        HashSet<String> set = new HashSet<>();
        for (ImportDeclaration im : cu.getImports()) {
            String s = im.toString();
            if (s.startsWith("import ")) s = s.substring(6);
            if (s.endsWith(";\n")) s = s.substring(0, s.length() - 2);
            else if (s.endsWith(";")) s = s.substring(0, s.length() - 1);
            set.add(s);
        }
        if (imps != null && imps.strip().length() > 0) {
            String[] temp = imps.split("\n");
            for (String s : temp) {
                if (s.startsWith("import ")) s = s.substring(6);
                if (s.endsWith(";")) s = s.substring(0, s.length() - 1);
                if (set.contains(s)) continue;
                if (s.contains("junit.framework.")) continue;
                if (s.contains("org.testng.")) continue;
                if (s.contains("java.test.")) continue;
                if (s.contains("test.framework.")) continue;
                if (s.contains("jdk.nashorn.")) continue;
                if (s.contains("com.sun.javadoc.*")) continue;
                if (s.contains("org.jemmy2ext.")) continue;
                if (s.contains("org.netbeans.jemmy.")) continue;
                if (s.contains("nsk.share.")) continue;
                System.out.println(s);
                cu.addImport(s);
                set.add(s);
            }
        }
    }

    public Statement Insert(List<CompilationUnit> Seed_compilations, String filePath, String Seed_file) throws IOException, SQLException, ClassNotFoundException, InterruptedException {
        JDBCUtils s = new JDBCUtils();
        mutationbyInsert = new InsertVal();
        List<String> MutatorList = new ArrayList<>();
        String want_type = "all";

        List<String> for_stmt = s.select(want_type);
        List<String> stmt_ids = s.getIds();
        List<String> imp_info = s.getImps();
        List<HashMap<String, String>> needsetVarName_list = s.getNeedfHashMaps();

        int numss = 0;
        int for_num = 0;

        for (Iterator var9 = Seed_compilations.iterator(); var9.hasNext(); ++numss) {
            CompilationUnit cu2 = (CompilationUnit) var9.next();
            Optional<TokenRange> to = cu2.getTokenRange();
            TokenRange t = null;
            if (to.isPresent()) t = to.get();

            Optional<PackageDeclaration> pa = cu2.getPackageDeclaration();
            PackageDeclaration p = null;
            if (pa.isPresent()) p = pa.get();

            Optional<ModuleDeclaration> md = cu2.getModule();
            ModuleDeclaration md1 = null;
            if (md.isPresent()) md1 = md.get();

            CompilationUnit cu = new CompilationUnit(t, p, cu2.getImports(), cu2.getTypes(), md1);
            cu.setStorage((cu2.getStorage().get()).getPath());
            String ori_className = "";
            String name = (cu.getStorage().get()).getPath().toString();
            String name_1 = (cu.getStorage().get()).getFileName();
            int pos1 = name_1.indexOf('_');
            int pos2 = name_1.indexOf('.');
            int number = Integer.parseInt(name_1.substring(pos1 + 1, pos2));
            String classNewName = "";
            if (!used_names.contains(number)) {
                used_names.add(number);
            } else {
                int res = search(used_names.stream().mapToInt(Integer::valueOf).toArray());
                used_names.add(res);
                classNewName = "MyJVMTest_" + String.valueOf(res);
                name_1 = "MyJVMTest_" + String.valueOf(res) + ".java";
                number = -1;
            }
            System.out.println("---------------------Prepare Assemble---------------------");
            System.out.println("insert seed--------->"+name);

            List<ClassOrInterfaceDeclaration> c1 = cu.findAll(ClassOrInterfaceDeclaration.class);

            for (ClassOrInterfaceDeclaration c : c1) {
                if (number == -1) {
                    ori_className = String.valueOf(c.getName());
                    c.setName(classNewName);
                }
                List<MethodDeclaration> methods = c.getMethods();
                if (methods.size() == 0) continue;
                int size = methods.size() - 1;
                MethodDeclaration ma = methods.get(size);

                if (ma.getThrownExceptions() == null || ma.getThrownExceptions().size() == 0)
                    ma.addThrownException(InsertVal.getThrowinfo());

                if (number == -1) {
                    NodeList<Statement> sou = ma.getBody().get().getStatements();
                    for (Statement s3 : sou) {
                        for (MethodCallExpr f3 : s3.findAll(MethodCallExpr.class)) {
                            if (f3.toString().startsWith("new MyJVMTest_")) {
                                String newScope = "new " + classNewName + "()";
                                Expression newScopep = StaticJavaParser.parseExpression(newScope);
                                f3.setScope(newScopep);
                                break;
                            }
                        }
                    }
                }

                if (methods.size() > 2)
                    System.out.println();
                for (int i = 0; i < size; i++) {
                    MethodDeclaration m = methods.get(i);
                    if (m.getThrownExceptions() == null || m.getThrownExceptions().size() == 0)
                        m.addThrownException(InsertVal.getThrowinfo());
                }

                for (int i = 0; i < size; ++i) {
                    MethodDeclaration m = methods.get(i);
                    if (m.getBody().isPresent()) {
                        List<String> seed_content = new ArrayList();
                        seed_content.add(cu.toString());
                        CreateJavaFile var10000 = createFile;
                        CreateJavaFile.createFile(Seed_file, name_1, seed_content.get(0));
                        BlockStmt methodBody = m.getBody().get();
                        if (methodBody == null) continue;
                        Random random = new Random();
                        Statement test = null;

                        int num = random.nextInt(for_stmt.size() - 1);
                        HashMap<String, String> needsetVarName = new HashMap();
                        System.out.println("---------------------Start Assemble---------------------");
                        try {
                            test = StaticJavaParser.parseStatement(for_stmt.get(num));
                            System.out.println("Java parse success");
                        } catch (ParseProblemException e) {
                            System.out.println("Java parse failure: " + e.getMessage());
                            String stmt_tmp = "for (int i = 0; i < 10000000; i++) {\n" +
                                " double result = Math.sqrt(i * i);\n" +
                                " }";
                            test = StaticJavaParser.parseStatement(stmt_tmp);
                        }

                        removeComment(test);
                        System.out.println("use the segment id--------> "+num);
                        needsetVarName = (HashMap) needsetVarName_list.get(num);
                        String stmt_id = stmt_ids.get(num);
                        String imps = imp_info.get(num);
                        addImports(cu, imps);

                        mutationbyInsert.tempMutatorList = new ArrayList<>();
                        if (needsetVarName.values().contains("var")) continue;
                        RelaStatments root = new RelaStatments(test, needsetVarName);
                        RelaStatmentTree rt = new RelaStatmentTree(root);
                        List<Statement> stmt_gen = new ArrayList();
                        Deque<Statement> stmt_ready = new ArrayDeque<>();
                        stmt_ready.push(test);

                        Random r1 = new Random();
                        List<Statement> sts = m.getBody().get().getStatements();
                        int setIndex = 0;
                        if (sts.size() > 1)
                            setIndex = r1.nextInt(sts.size() - 1);
                        test = deletemethods(test);
                        deletevalThis(test);

                        mutationbyInsert.hasInsert = false;
                        int depth = 1;
                        Queue<RelaStatments> que = new LinkedList<>();
                        que.offer(root);
                        while (!que.isEmpty() && depth <= 20) {
                            int size2 = que.size();
                            for (int i2 = 0; i2 < size2; i2++) {
                                RelaStatments cur = que.poll();
                                mutationbyInsert.setVarNameInInsertStmt(cur, mutationbyInsert, cu, m, cur.value, cur.varList, name, setIndex);
                                mutationbyInsert.hasInsert = true;
                                if (cur.nexts.size() > 0) {
                                    for (RelaStatments next : cur.nexts) {
                                        if (next.varList.size() > 0)
                                            que.offer(next);
                                    }
                                }
                                depth++;
                            }
                        }
                        rt.postSearch(root);
                        List<RelaStatments> insertStmts = rt.postValue;
                        if (insertStmts.size() != 0) {
                            for (int index = 0; index < insertStmts.size(); index++) {
                                if (index == 0) {
                                    List<Node> nodes = insertStmts.get(index).value.findAll(Node.class);
                                    (nodes.get(0)).setLineComment("MutationbyInsert The Seed of:\t" + name);
                                }
                                if (insertStmts.get(index).pre != null && insertStmts.get(index).pre.size() > 0) {
                                    for (Statement preStmt : insertStmts.get(index).pre)
                                        addStmtBetweenStmt(methodBody, setIndex++, preStmt);
                                }
                                Statement in = deleteReturn(insertStmts.get(index).value);
                                addStmtBetweenStmt(methodBody, setIndex++, in);
                            }
                        }
                        if (want_type.equals("Expression"))
                            mutationbyInsert.tempMutatorList.add("Expression");
                        else
                            mutationbyInsert.tempMutatorList.add("Statement");
                        mutationbyInsert.tempMutatorList.add(stmt_id);
                        if (test.toString().contains("TestFailure")) {
                            System.out.println("------Insert Import-----");
                            ImportDeclaration importDeclaration = StaticJavaParser.parseImport("import nsk.share.TestFailure;");
                            cu.addImport(importDeclaration);
                        }

                        List<String> mutation_result = new ArrayList<>();
                        mutation_result.add(cu.toString());
                        CreateJavaFile.createFile(filePath, name_1, mutation_result.get(0));
                        String name2 = name_1.substring(0, name_1.indexOf(".java")) + ".txt";
                        CreateJavaFile.createFile(filePath, name2, mutationbyInsert.tempMutatorList.toString());
                        System.out.println("-------------End Assemble-------------");
                    }
                }
            }
            numss++;
        }
        return null;
    }

    private static void createDirectoryIfNotExists(String path) {
        File directory = new File(path);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (created) {
            } else {
                System.out.println("Failed to create directory: " + path);
            }
        } else {
        }
    }

    private static void deleteDirectoryRecursively(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectoryRecursively(file);
                    } else {
                        if (!file.delete()) {
                            System.err.println("Failed to delete file: " + file.getAbsolutePath());
                        }
                    }
                }
            }
            if (!directory.delete()) {
                System.err.println("Failed to delete directory: " + directory.getAbsolutePath());
            }
        }
    }

    public static void main(String[] args) {
        InsertMain mainner = new InsertMain();
        try {
            Date now = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            String timestamp = dateFormat.format(now);
            String directoryPath = String.format("/root/ssfuzz/parser/feature/src/test/output/task-%s", timestamp);

            String basePath = args.length > 0 ? args[0] : "/root/ssfuzz/01seedGenerate/pass_data";

            File baseDir = new File(basePath);
            if (!baseDir.exists() || !baseDir.isDirectory()) {
                throw new IllegalArgumentException("Invalid basePath: " + basePath);
            }

            File[] dateDirs = baseDir.listFiles(File::isDirectory);
            if (dateDirs == null || dateDirs.length == 0) {
                throw new IllegalArgumentException("No subdirectories found in basePath: " + basePath);
            }

            for (File dateDir : dateDirs) {
                String dateDirName = dateDir.getName(); // e.g., "20240118"
                File[] iterations = dateDir.listFiles(file -> file.isDirectory() && file.getName().startsWith("iteration_"));
                if (iterations == null || iterations.length == 0) {
                    System.out.println("No iteration directories found in: " + dateDir.getAbsolutePath());
                    continue;
                }
                for (File iterationDir : iterations) {

                    String iterationName = iterationDir.getName(); // e.g., "iteration_0"
                    String uniqueSeedPath = String.format("%s/%s/%s/seedTemp_testsuits", directoryPath, dateDirName, iterationName);
                    String uniqueFilePath = String.format("%s/%s/%s/target_testsuits", directoryPath, dateDirName, iterationName);
                    createDirectoryIfNotExists(uniqueSeedPath);
                    createDirectoryIfNotExists(uniqueFilePath);

                    Path pathToSource = Paths.get(iterationDir.getAbsolutePath());
                    SourceRoot sourceRoot = new SourceRoot(pathToSource);
                    sourceRoot.tryToParse();
                    List<CompilationUnit> compilations = mainner.FindJava(sourceRoot);


                    int iterationNumber = Integer.parseInt(iterationName.replace("iteration_", ""));

                    mainner.Insert_Cov(compilations, uniqueFilePath, uniqueSeedPath, iterationNumber);
                }
                System.out.println("Deleting processed directory: " + dateDir.getAbsolutePath());
                deleteDirectoryRecursively(dateDir);
            }
        } catch (FileNotFoundException var11) {
            var11.printStackTrace();
        } catch (Exception var12) {
            var12.printStackTrace();
        }
    }
}
