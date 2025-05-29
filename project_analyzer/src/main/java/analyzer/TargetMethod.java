package analyzer;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.stmt.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.util.*;

public class TargetMethod {
    //初始化
    private static final List<jsonFormat> collectedMethods = new ArrayList<>();  //用于存储筛选出的目标方法
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();    //json输出

    public static void main(String[] args) throws Exception {
        System.out.printf("%-30s | %-30s | %-10s | %-10s%n", "类名", "方法名", "圈复杂度", "LOC");
        System.out.println("----------------------------------------------------------------------------------");

        //遍历源码
        for (File sourceDir : SourceRoots.getSourceRoots()) {
            String projectName = sourceDir.getParentFile().getParentFile().getParentFile().getName();
            System.out.println("\n================== 分析项目: " + projectName + " ==================\n");
            analyzeDirectory(sourceDir, projectName);
        }

        //将结果写进json文件
        writeJson("selected_methods.json");
        System.out.println("\n方法源码已保存到 selected_methods.json");
    }

    //分析目录并找到.java文件
    private static void analyzeDirectory(File dir, String projectName) throws Exception {
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory()) {
                analyzeDirectory(file, projectName);
            } else if (file.getName().endsWith(".java")) {
                analyzeFile(file, projectName);
            }
        }
    }

    //分析Java文件，筛选出符合条件的方法
    private static void analyzeFile(File file, String projectName) throws Exception {
        //构建AST（利用JavaParser）
        CompilationUnit cu;
        try (FileInputStream in = new FileInputStream(file)) {
            cu = StaticJavaParser.parse(in);
        }
        //遍历每一个类里面包含的方法
        for (ClassOrInterfaceDeclaration cls : cu.findAll(ClassOrInterfaceDeclaration.class)) {
            String className = cls.getNameAsString();

            for (MethodDeclaration method : cls.getMethods()) {
                //名称
                String methodName = method.getNameAsString();
                //复杂度
                int complexity = calculateComplexity(method);
                //LOC
                int loc = method.getEnd().get().line - method.getBegin().get().line + 1;

                if (complexity > 10 || loc > 50) {
                    System.out.printf("%-30s | %-30s | %-10d | %-10d%n", className, methodName, complexity, loc);
                    jsonFormat format = new jsonFormat(projectName, className, methodName, complexity, loc, method.toString());
                    collectedMethods.add(format);
                }
            }
        }
    }

    private static int calculateComplexity(MethodDeclaration method) {
        int complexity = 1;
        complexity += method.findAll(IfStmt.class).size();
        complexity += method.findAll(ForStmt.class).size();
        complexity += method.findAll(ForEachStmt.class).size();
        complexity += method.findAll(WhileStmt.class).size();
        complexity += method.findAll(DoStmt.class).size();
        complexity += method.findAll(SwitchEntry.class).size();
        complexity += method.findAll(BinaryExpr.class, b ->
                b.getOperator() == BinaryExpr.Operator.AND ||
                        b.getOperator() == BinaryExpr.Operator.OR).size();
        return complexity;
    }

    private static void writeJson(String filePath) throws IOException {
        try (Writer writer = new FileWriter(filePath)) {
            gson.toJson(collectedMethods, writer);
        }
    }

    //定义json文件的格式
    static class jsonFormat {
        String project;
        String className;
        String methodName;
        int complexity;
        int loc;
        String code;

        jsonFormat(String project, String className, String methodName, int complexity, int loc, String code) {
            this.project = project;
            this.className = className;
            this.methodName = methodName;
            this.complexity = complexity;
            this.loc = loc;
            this.code = code;
        }
    }
}
