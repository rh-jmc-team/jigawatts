package org.openjdk.jigawatts;

import java.io.IOException;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;


class BeforeHook extends Hook {
    String str;
    BeforeHook(String s) {
	str = s;
    }
	
    public void run() {
	System.out.println("Hook:Before Checkpoint: " + str);
    }
}

class AfterHook extends Hook {
    String str;
    AfterHook(String s) {
	str = s;
    }

    public void run() {
	System.out.println("Hook:After Restore: " + str);
    }
}

public class RandomTest {

    public static double overall_average = 0;

    private static BufferedReader getOutput(Process p) {
	return new BufferedReader(new InputStreamReader(p.getInputStream()));
    }

    private static BufferedReader getError(Process p) {
	return new BufferedReader(new InputStreamReader(p.getErrorStream()));
    }

	public static void TestRandomRestore() throws Exception {
	    Path tmpDir = Paths.get("src","test","resources","jigawatts");
	    Jigawatts.restoreTheWorld(tmpDir.toString());
	}

	public static void TestRandomCheckpoint() throws Exception {
	    int upperBound = 100;
	    long sampleSize = 10000;
	    Path tmpDir = Paths.get("src","test","resources","jigawatts");	    
	    
	    long testarray[] = new long[upperBound];

        for (long i = 0; i < sampleSize; i++) {
            Double r = Math.random() * upperBound;
            int v = (int)  Math.floor(r);
            testarray[v] += 1;
        }

	Jigawatts.registerRestoreHook(new AfterHook("That's all folks"));
	Jigawatts.saveTheWorld(tmpDir.toString());
	long end = System.currentTimeMillis();

	PrintWriter os = new PrintWriter(new FileOutputStream(new File("src/test/resources/" + Long.toString(end))), true);	

        long max = 0; long min = upperBound; long average = 0;
        for (int i = 0; i < upperBound; i++) {
            long s = testarray[i];
            if (s > max) { max = s;}
            if (s < min) { min = s;}
            average = average + s;
        }
        double median = average / upperBound;

        os.println("Testing random number generator with upper bound = " + upperBound + " and sample size " + sampleSize);
        os.println("Max bin size = " + max + " Min bin size = " + min + " median bin size = " + median);
	overall_average = median;
	}
	

    private void VerifyProcess(Process p) throws Exception {

	BufferedReader output = getOutput(p);
	BufferedReader error  = getError(p);

	p.waitFor();
	
	String l = "";
	System.out.println("VerifyProcess: " + p);
	int i = 0;
	while ((l = output.readLine()) != null) {
	    System.out.println("output line " + i++ + " = " + l);
	}
	i = 0;
	while ((l = output.readLine()) != null) {
	    System.out.println("error line " + i++ + " = " + l);
	}
	

	assertEquals(0,p.exitValue());
    }

    
	
    @Test
    public void TestOne(@TempDir Path tmpDir) throws Exception {
	// Checkpoint image files go here
	Path tmpout = Paths.get("src","test","resources","jigawatts");

	Files.createDirectories(tmpout);
	// Setup to run the test and checkpoint the results
	ProcessBuilder cp = new ProcessBuilder("java", "-cp", "target/classes:target/test-classes",
						    "-XX:+UseSerialGC", "-XX:-UsePerfData",
						    "org.openjdk.jigawatts.RandomTest", "checkpoint");
	VerifyProcess(cp.start());

	// Setup to restore the test
	ProcessBuilder rs = new ProcessBuilder("java", "-cp", "target/classes:target/test-classes", "org.openjdk.jigawatts.RandomTest", "restore");
	VerifyProcess(rs.start());
    }

    // This is here so we can run from the command line if we want to.
    // sudo java -cp target/classes:target/test-classes -XX:+UseSerialGC -XX:-UsePerfData org.openjdk.jigawatts.RandomTest restore

    public static void main(String[] args) throws Exception {
	if (args[0].equals("checkpoint"))
	    TestRandomCheckpoint();
	else
	    TestRandomRestore();
    }
}
		     
	
