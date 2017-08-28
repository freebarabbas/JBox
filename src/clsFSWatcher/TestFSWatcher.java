package clsFSWatcher;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestFSWatcher {
    public static void main(String[] args) throws IOException {
        Path dir = Paths.get("/tmp/test");
        new FSWatcher(dir).processEvents();
    }
}
