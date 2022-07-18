import com.redhat.jigawatts.*;

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



public class TestStopRunning {

    private static BufferedReader getOutput(Process p) {
	return new BufferedReader(new InputStreamReader(p.getInputStream()));
    }

    private static BufferedReader getError(Process p) {
	return new BufferedReader(new InputStreamReader(p.getErrorStream()));
    }

    private void VerifyRunningProcess(Process p) throws Exception {
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
	while ((l = error.readLine()) != null) {
	    System.out.println("error line " + i++ + " = " + l);
	}
	

	assertEquals(0,p.exitValue());
    }
	
    private void VerifyStoppedProcess(Process p) throws Exception {
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
	while ((l = error.readLine()) != null) {
	    System.out.println("error line " + i++ + " = " + l);
	}
	
	assertNotEquals(0,p.exitValue());
    }

	

    @Test
    public void TestOne(@TempDir Path tmpDir) throws Exception {
	// Checkpoint image files go here
	Path tmpout = Paths.get("src","test","resources","jigawatts");

	Files.createDirectories(tmpout);
	// Setup to run the test and checkpoint the results
	ProcessBuilder cp = new ProcessBuilder("java", "-cp", "target/classes:target/test-classes",
						    "-XX:+UseSerialGC", "-XX:-UsePerfData",
						    "TestStopRunning", "leaveRunning");
	VerifyRunningProcess(cp.start());

	cp = new ProcessBuilder("java", "-cp", "target/classes:target/test-classes",
						    "-XX:+UseSerialGC", "-XX:-UsePerfData",
						    "TestStopRunning", "stopRunning");
	VerifyStoppedProcess(cp.start());
    }

    public static void TestLeaveRunning()  throws Exception {
	Path tmpDir = Paths.get("src","test","resources","jigawatts",Long.toString(System.nanoTime()));
	System.out.println("Saving the state in " + tmpDir.toString() + " and keep on trucking");
	Jigawatts.saveTheWorld(tmpDir.toString(), true);
	System.out.println("A good days work saving the world but we aren't done yet");
    }

    public static void TestStopRunning()  throws Exception {
	Path tmpDir = Paths.get("src","test","resources","jigawatts",Long.toString(System.nanoTime()));
	System.out.println("Saving the state in " + tmpDir.toString() + " and then end for the day");
	Jigawatts.saveTheWorld(tmpDir.toString(), false);
	System.out.println("We shouldn't get here");
    }
								     

    public static void main(String[] args)  throws Exception {
	if (args[0].equals("leaveRunning"))
	    TestLeaveRunning();
	else
	    TestStopRunning();
    }
    
}
