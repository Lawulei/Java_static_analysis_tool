package analyzer;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import java.io.File;
import java.io.FileInputStream;
import java.util.Objects;

public class Class_Interface {
    public static void main(String[] args) throws Exception {
        System.out.printf("%-10s | %-40s | %s%n", "类型", "包路径", "名称");
        System.out.println("-------------------------------------------------------------");
        //输出类和接口
        for (File sourceDir : SourceRoots.getSourceRoots()) {
            String projectName = sourceDir.getParentFile().getParentFile().getParentFile().getName();
            System.out.println("\n================== 分析项目: " + projectName + " ==================\n");
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

    //利用JavaParser提取类和接口（解析java文件）
    private static void analyzeFile(File file) throws Exception {
        try (FileInputStream in = new FileInputStream(file)) {
            //快速解析并且得到抽象语法树
            CompilationUnit cu = StaticJavaParser.parse(in);
            //获取包路径
            String packageName = cu.getPackageDeclaration()
                    .map(pd -> pd.getName().toString())
                    .orElse("(default)");
            //利用for循环得到所有类和接口
            for (ClassOrInterfaceDeclaration decl : cu.findAll(ClassOrInterfaceDeclaration.class)) {
                String kind = decl.isInterface() ? "Interface" : "Class";
                String name = decl.getNameAsString();
                System.out.printf("%-10s | %-40s | %s%n", kind, packageName, name);
            }
        }
    }
}
