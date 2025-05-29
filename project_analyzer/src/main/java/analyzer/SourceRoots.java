package analyzer;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class SourceRoots {
    public static List<File> getSourceRoots() {
        return Arrays.asList(
                new File("../java_project/commons-cli/src/main/java"),
                new File("../java_project/commons-math/src/userguide/java")
        );
    }
}
