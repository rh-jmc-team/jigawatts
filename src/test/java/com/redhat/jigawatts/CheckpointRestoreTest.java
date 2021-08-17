package com.redhat.jigawatts;

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
        Jigawatts.checkTheWorld();
    }

    @Test
    public void testRestoreHookIO(@TempDir Path tmpDir) throws IOException {
        
        final String data = "frozen beans";

	Jigawatts.clearRestoreHooks();
        assertEquals(0, Jigawatts.getRestoreHooks().size());
        Jigawatts.registerRestoreHook(new TestHookA(data));
        assertEquals(1, Jigawatts.getRestoreHooks().size());
        
        assertEquals(0, Files.list(tmpDir).count());
        Jigawatts.writeRestoreHooks(tmpDir.toString());
        assertEquals(1, Files.list(tmpDir).count());
        
        Jigawatts.clearRestoreHooks();
        assertEquals(0, Jigawatts.getRestoreHooks().size());
        
        Jigawatts.readRestoreHooks(tmpDir.toString());
        assertEquals(1, Jigawatts.getRestoreHooks().size());
        assertEquals(data, ((TestHookA)Jigawatts.getRestoreHooks().get(0)).data);
	Jigawatts.clearRestoreHooks();	
    }


    
}
