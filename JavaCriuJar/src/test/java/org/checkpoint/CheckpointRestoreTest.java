package org.checkpoint;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

// TODO: more tests
public class CheckpointRestoreTest {
    
    private static class TestHookA extends Hook {
        private final String data;
        public TestHookA(String data) { this.data = data; }
    }
    
    
    @Test
    public void initTest() {
        CheckpointRestore.checkTheWorld();
    }

    @Test
    public void testRestoreHookIO(@TempDir Path tmpDir) throws IOException {
        
        final String data = "frozen beans";
        
        assertEquals(0, CheckpointRestore.getRestoreHooks().size());
        CheckpointRestore.registerRestoreHook(new TestHookA(data));
        assertEquals(1, CheckpointRestore.getRestoreHooks().size());
        
        assertEquals(0, Files.list(tmpDir).count());
        CheckpointRestore.writeRestoreHooks(tmpDir.toString());
        assertEquals(1, Files.list(tmpDir).count());
        
        CheckpointRestore.clearRestoreHooks();
        assertEquals(0, CheckpointRestore.getRestoreHooks().size());
        
        CheckpointRestore.readRestoreHooks(tmpDir.toString());
        assertEquals(1, CheckpointRestore.getRestoreHooks().size());
        assertEquals(data, ((TestHookA)CheckpointRestore.getRestoreHooks().get(0)).data);
    }


    
}
