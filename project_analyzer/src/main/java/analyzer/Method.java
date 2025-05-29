package analyzer;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import java.io.File;
import java.io.FileInputStream;
import java.util.Objects;

public class Method {
    public static void main(String[] args) throws Exception {
        System.out.printf("%-30s | %-30s | %-5s | %-5s%n", "类名", "方法名", "参数数", "行数");
        System.out.println("----------------------------------------------------------------------");
        //输出每个方法的相关信息
        for (File sourceDir : SourceRoots.getSourceRoots()) {
            String projectName = sourceDir.getParentFile().getParentFile().getParentFile().getName();
            System.out.println("\n================== 分析项目: " + projectName + " ==================\n");
            analyzeDirectory(sourceDir);
        }
    }

    //解析整个源代码目录
    private static void analyzeDirectory(File dir) throws Exception {
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory()) {
                analyzeDirectory(file);
            } else if (file.getName().endsWith(".java")) {
                analyzeFile(file);
            }
        }
    }

    private static void analyzeFile(File file) throws Exception {
        //解析Java文件为AST
        CompilationUnit cu;
        try (FileInputStream in = new FileInputStream(file)) {
            cu = StaticJavaParser.parse(in);
        }
        //循环每个类
        for (ClassOrInterfaceDeclaration cls : cu.findAll(ClassOrInterfaceDeclaration.class)) {
            String className = cls.getNameAsString();
            //循环类中的每个方法
            for (MethodDeclaration method : cls.getMethods()) {
                //提取方法中的名字，参数，行数
                String methodName = method.getNameAsString();
                int paramCount = method.getParameters().size();
                int loc =  method.getEnd().get().line - method.getBegin().get().line + 1;

                System.out.printf("%-30s | %-30s | %-5d | %-5d%n", className, methodName, paramCount, loc);
            }
        }
    }
}
