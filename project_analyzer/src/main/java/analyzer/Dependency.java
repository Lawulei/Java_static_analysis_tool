package analyzer;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import java.io.*;
import java.util.*;

public class Dependency {
    private static final Map<String, Set<String>> classDependencies = new HashMap<>();
    private static final Map<String, String> classToPackage = new HashMap<>();
    private static final Map<String, Set<String>> packageToClasses = new HashMap<>();

    public static void main(String[] args) throws Exception {
        //输出依赖图
        for (File sourceDir : SourceRoots.getSourceRoots()) {
            String projectName = sourceDir.getParentFile().getParentFile().getParentFile().getName();
            System.out.println("\n================== 分析项目: " + projectName + " ==================\n");
            collectDependencies(sourceDir);
        }

        writeDotFile("dependency.dot");
        System.out.println("已生成带聚类的类依赖关系图：dependency.dot");
    }

    public static void collectDependencies(File dir) throws Exception {
        for (File file : Objects.requireNonNull(dir.listFiles())) {
           //得到当前的类名
            if (file.isDirectory()) {
                collectDependencies(file);
            } else if (file.getName().endsWith(".java")) {
                CompilationUnit cu;
                try (FileInputStream in = new FileInputStream(file)) {
                    cu = StaticJavaParser.parse(in);
                }
                String className = cu.getPrimaryTypeName().orElse(file.getName().replace(".java", ""));

                //得到当前类所属的包
                String pkg = cu.getPackageDeclaration().map(p -> p.getName().toString()).orElse("(default)");
                classToPackage.put(className, pkg);
                packageToClasses.computeIfAbsent(pkg, k -> new HashSet<>()).add(className);

                //初始化当前类的依赖集合
                Set<String> dependencies = new HashSet<>();
                //循环所有的类和接口
                cu.findAll(ClassOrInterfaceType.class).forEach(type -> {
                    String dep = type.getNameWithScope();

                    //如果不是自身依赖就将依赖类加入集合
                    if (!dep.equals(className)) {
                        dependencies.add(dep);
                    }
                });

                //建立映射关系
                classDependencies.put(className, dependencies);
            }
        }
    }

    //生成.dot文件
    private static void writeDotFile(String outputPath) throws IOException {
        try (PrintWriter out = new PrintWriter(new FileWriter(outputPath))) {
            out.println("digraph G {");
            out.println("    rankdir=LR;");
            out.println("    node [shape=box];");

            for (Map.Entry<String, Set<String>> entry : packageToClasses.entrySet()) {
                String pkg = entry.getKey();
                Set<String> classes = entry.getValue();

                out.println("    subgraph cluster_" + pkg.replace(".", "_") + " {");
                out.println("        label = \"" + pkg + "\";");
                out.println("        style=filled;");
                out.println("        color=lightgrey;");
                for (String cls : classes) {
                    out.println("        \"" + cls + "\";");
                }
                out.println("    }");
            }

            for (Map.Entry<String, Set<String>> entry : classDependencies.entrySet()) {
                String from = entry.getKey();
                for (String to : entry.getValue()) {
                    out.printf("    \"%s\" -> \"%s\";%n", from, to);
                }
            }

            out.println("}");
        }
    }
}
