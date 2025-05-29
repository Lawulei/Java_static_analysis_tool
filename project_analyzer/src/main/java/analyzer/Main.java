package analyzer;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        System.out.println("请选择要运行的分析模块：");
        System.out.println("1. 类/接口汇总");
        System.out.println("2. 方法统计");
        System.out.println("3. 类依赖图（生成 dot 文件）");
        System.out.println("4. 代码度量（圈复杂度 & 注释率）");
        System.out.println("5. 一键执行所有分析");
        System.out.println("6. 筛选高复杂度/长方法（供摘要生成）");
        System.out.print("输入编号 (1-6)：");

        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1":
                System.out.println("执行：类/接口汇总...");
                Class_Interface.main(null);
                break;
            case "2":
                System.out.println("执行：方法统计...");
                Method.main(null);
                break;
            case "3":
                System.out.println("执行：类依赖图（生成 dot 文件）...");
                Dependency.main(null);
                break;
            case "4":
                System.out.println("执行：代码度量（圈复杂度 & 注释率）...");
                Metric.main(null);
                break;
            case "5":
                System.out.println("执行：一键运行所有分析模块...");
                RunAll.main(null);
                break;
            case "6":
                System.out.println("执行：筛选高复杂度/长方法...");
                TargetMethod.main(null);
                break;
            default:
                System.out.println("无效的输入，请输入 1~6");
        }

        scanner.close();
    }
}
