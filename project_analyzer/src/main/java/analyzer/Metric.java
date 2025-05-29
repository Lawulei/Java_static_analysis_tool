package analyzer;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.BinaryExpr;

import java.io.*;
import java.util.*;

public class Metric {

    public static void main(String[] args) throws Exception {
        //输出类的圈复杂度和注释率
        for (File sourceDir : SourceRoots.getSourceRoots()) {
            String projectName = sourceDir.getParentFile().getParentFile().getParentFile().getName();
            System.out.println("\n================== 分析项目: " + projectName + " ==================\n");
            System.out.printf("%-40s | %-10s | %-10s%n", "类名", "圈复杂度", "注释率%");
            System.out.println("----------------------------------------------------------");

            analyzeDirectory(sourceDir);
        }
    }
    //解析整个源代码目录
    private static void analyzeDirectory(File dir) throws Exception {
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            //  如果是子目录（不是.java文件）就递归调用自身
            if (file.isDirectory()) {
                analyzeDirectory(file);
            }
            //如果是.java文件就解析java文件
            else if (file.getName().endsWith(".java")) {
                analyzeFile(file);
            }
        }
    }

    private static void analyzeFile(File file) throws Exception {
        //得到类名
        CompilationUnit cu;
        try (FileInputStream in = new FileInputStream(file)) {
            cu = StaticJavaParser.parse(in);
        }
        String className = cu.getPrimaryTypeName().orElse(file.getName().replace(".java", ""));

        //计算圈复杂度
        int totalComplexity = 0;
        for (MethodDeclaration method : cu.findAll(MethodDeclaration.class)) {
            totalComplexity += calculateComplexity(method);
        }

        //计算注释率
        int commentLines = 0;
        int totalLines = TotalLines(file);
        //每条注释用的注释行
        for (Comment comment : cu.getAllContainedComments()) {
            commentLines += countLines(comment.getContent());
        }
        //与总行数的比值
        double commentRate = totalLines > 0 ? (commentLines * 100.0 / totalLines) : 0.0;

        System.out.printf("%-40s | %-10d | %-10.2f%n", className, totalComplexity, commentRate);
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

    private static int TotalLines(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            int lines = 0;
            while (reader.readLine() != null) lines++;
            return lines;
        }
    }

    private static int countLines(String content) {
        return (int) content.lines().count();
    }
}
