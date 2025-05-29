package analyzer;

import java.io.*;

public class RunAll {
    public static void main(String[] args) throws Exception {
        PrintStream consoleOut = System.out;
        try (PrintStream fileOut = new PrintStream(new FileOutputStream("output.log"))) {


            PrintStream combinedOut = new PrintStream(new TeeOutputStream(fileOut));
            System.setOut(combinedOut);
            System.setErr(combinedOut);


            System.out.println("类/接口汇总");
            Class_Interface.main(null);

            System.out.println("\n方法统计");
            Method.main(null);

            System.out.println("\n类依赖图（生成 dot 文件）");
            Dependency.main(null);

            System.out.println("\n代码度量（圈复杂度 & 注释率）");
            Metric.main(null);

            System.out.println("\n所有分析已完成！");


            System.setOut(consoleOut);


            System.out.println("\n已生成output.log日志！");

        } catch (Exception e) {

            System.err.println("分析过程中出现错误: " + e.getMessage());
            e.printStackTrace();
        }
    }


    static class TeeOutputStream extends OutputStream {
        private OutputStream out;

        public TeeOutputStream(OutputStream out) {
            this.out = out;
        }

        @Override
        public void write(int b) throws IOException {
            out.write(b);
        }

        @Override
        public void write(byte[] b) throws IOException {
            out.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
        }

        @Override
        public void flush() throws IOException {
            out.flush();
        }

        @Override
        public void close() throws IOException {
            out.close();
        }
    }
}
