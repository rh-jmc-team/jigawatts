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

class Node {
    Node left;
    Node right;
    int value;
    int count;

    Node(int val) {
	value = val;
	count = 0;
    }

    public void add(Node l) {

	if (left == null && l.value < value)
	    left = l;
	else if (right == null && l.value > value)
	    right = l;
	else if (l.value < value)
	    left.add(l);
	else if (l.value > value)
	    right.add(l);
	else if (l.value == value)
	    count++;
    }

    public void print(Node l) {
	if (l.left != null) print(l.left);
	System.out.println("Node l: = " + l + " value = " + l.value + " left = " + l.left + " right = " + l.right);
	if (l.right != null) print(l.right);
    }

}

class TestHooksBeforeHook extends Hook {
    String str;
    TestHooksBeforeHook(String s) {
	str = s;
    }
	
    public void run() {
	System.gc();
	System.gc();
    }
}

public class TestHooks {


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
	while ((l = error.readLine()) != null) {
	    System.out.println("error line " + i++ + " = " + l);
	}
	
	
	assertEquals(0,p.exitValue());
    }
    

     @Test
     public void TestOne(@TempDir Path tmpDir) throws Exception {
     	// Checkpoint image files go here
	 Path tmpoutwithout = Paths.get("src","test","resources","jigawatts","testhooks", "without");

	Files.createDirectories(tmpoutwithout);
     	// Setup to run the test and checkpoint the results
     	ProcessBuilder cp = new ProcessBuilder("java", "-cp", "target/classes:target/test-classes",
     					       "-XX:+UseG1GC", "-XX:-UsePerfData", "-XX:+PrintGC", "-Xms500m", "-Xmx2g",  "-XX:MinHeapFreeRatio=30", "-XX:MaxHeapFreeRatio=40", 
     					       "TestHooks", "without");
     	VerifyProcess(cp.start());

	Path tmpoutwith = Paths.get("src","test","resources","jigawatts","testhooks", "with");
	Files.createDirectories(tmpoutwith);

     	ProcessBuilder cp1 = new ProcessBuilder("java", "-cp", "target/classes:target/test-classes",
						"-XX:+UseG1GC", "-XX:-UsePerfData", "-XX:+PrintGC", "-Xms500m", "-Xmx2g",  "-XX:MinHeapFreeRatio=30", "-XX:MaxHeapFreeRatio=40", 
						"TestHooks", "with");
	VerifyProcess(cp1.start());

	String without = "du " + tmpoutwithout.toString();
	String with =    "du " +  tmpoutwith.toString();

	Process pwith = Runtime.getRuntime().exec(with);
        Process pwithout = Runtime.getRuntime().exec(without);
	pwith.waitFor();
	pwithout.waitFor();

	assertTrue(getLeadingSize(pwithout) > getLeadingSize(pwith));
     }

    public static int getLeadingSize(Process p) throws Exception {
	BufferedReader reader = getOutput(p);
	String str = reader.readLine();
	String s = str.substring(0, str.indexOf("src") - 1);
	System.out.println("Process " + p + " has leading size " + s);
	return Integer.parseInt(s);
    }
	
    public static Node generateATree(int height) {
	Node root = new Node((int) (Math.random() * Integer.MAX_VALUE));

	for (int i = 0; i < height; i++) {
	    int val = (int)(Math.random() * Integer.MAX_VALUE);
	    root.add(new Node(val));
	}

	return root;
    }

    private static BufferedReader getOutput(Process p) {
	return new BufferedReader(new InputStreamReader(p.getInputStream()));
    }

    private static BufferedReader getError(Process p) {
	return new BufferedReader(new InputStreamReader(p.getErrorStream()));
    }
    
    public static Node[]  generateALotOfGarbage(int numTrees, int height, int numReplacements) {
	Node[] trees = new Node[numTrees];

	for (int i = 0; i < numTrees; i++) {
	    trees[i] = generateATree(height);
	}
	for (int i = 0; i < numReplacements; i++) {
	    int victim = (int) (Math.random() * numTrees);
	    trees[victim] = generateATree(height);
	}
	return trees;
    }

    public static void TestWithout() throws Exception {
	Path tmpDir = Paths.get("src","test","resources","jigawatts", "testhooks", "without");
	Node[] trees = generateALotOfGarbage(5000, 1000, 100000);
	// we want trees alive right up until the call to savetheworld	
	Jigawatts.saveTheWorld(tmpDir.toString());
    }

    public static void TestWith() throws Exception {
	Jigawatts.registerCheckpointHook(new TestHooksBeforeHook("Test Hooks Before Hook"));
	Path tmpDir = Paths.get("src","test","resources","jigawatts", "testhooks", "with");
	Node[] trees = generateALotOfGarbage(5000, 1000, 100000);
	Jigawatts.saveTheWorld(tmpDir.toString());
    }

    // This test compares two runs, one with a pair of full gcs added via a checkpointing hook and one without.
    // The one with the hooks is expected to be smaller.
    public static void main(String[] args) throws Exception {
	if (args[0].equals("without"))
	    TestWithout();
	else
	    TestWith();
    }
}

