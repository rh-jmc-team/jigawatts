package org.checkpoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class CheckpointRestore {

    public static CheckpointRestore CRContext;

    private static List<Hook> CheckpointHooks;
    private static List<Hook> RestoreHooks;

    public static void CleanupTheWorld() {
        System.gc();
        System.gc();
    }

    private native void CheckTheWorldNative();

    private native void SaveTheWorldNative(String dir);

    private native void RestoreTheWorldNative(String dir);

    public static native void MigrateTheWorld();

    public static native void SaveTheWorldIncremental();

    public static void CheckTheWorld() {
        CRContext.CheckTheWorldNative();
    }

    public static void SaveTheWorld(String dir) {
        for (Hook h : CheckpointHooks) {
            h.run();
        }

        WriteRestoreHooks(dir);
        CRContext.SaveTheWorldNative(dir);
    }

    public static void WriteRestoreHooks(String dir) {
        try {
            File outputDir = new File(dir);
            outputDir.mkdir();
            File file = new File(dir, "/JavaRestoreHooks.txt");
            FileOutputStream f = new FileOutputStream(file);
            ObjectOutputStream o = new ObjectOutputStream(f);

            for (Hook h : RestoreHooks) {
                o.writeObject(h);
            }

            o.close();
            f.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error initializing stream: " + e.getMessage());
        }
    }

    public static void ReadRestoreHooks(String dir) {
        try {
            FileInputStream f = new FileInputStream(new File(dir + "/JavaRestoreHooks.txt"));
            ObjectInputStream o = new ObjectInputStream(f);

            while (true) {
                Hook h = (Hook) o.readObject();
                RestoreHooks.add(h);
            }

        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (EOFException e) {
            // This always happens.
            // It's ugly, exceptions should be exceptional.
            // Is there a better way?
        } catch (IOException e) {
            System.out.println("Error initializing stream:" + e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void RestoreTheWorld(String dir) {
        CRContext.RestoreTheWorldNative(dir);
        ReadRestoreHooks(dir);
        for (Hook h : RestoreHooks) {
            h.run();
        }
    }

    public static void RegisterCheckpointHook(Hook h) {
        CRContext.CheckpointHooks.add(h);
    }

    public static void RegisterRestoreHook(Hook h) {
        CRContext.RestoreHooks.add(h);
    }

    public static void DebugPrint(String s) {
        System.out.println(s);
    }

    public static void copyLibrary(String s) {
        String libraryName = System.mapLibraryName(s);
        String temp = "/tmp";
        InputStream is;
        FileOutputStream os;
        try {
            is = CheckpointRestore.class.getClassLoader().getResourceAsStream(libraryName);
            os = new FileOutputStream("/tmp/libCheckpointRestore.so");
            byte[] buf = new byte[1024];

            int bytesRead;

            while ((bytesRead = is.read(buf)) > 0) {
                os.write(buf, 0, bytesRead);
            }

            is.close();
            os.close();
        } catch (Exception e) {
            System.out.println(e);
        }

        System.load("/tmp/" + libraryName);
    }

    static {

        //	DebugPrint("Library path = " + System.getProperty("java.library.path"));
        //	DebugPrint("About to load Checkpoint Restore library " + System.mapLibraryName("CheckpointRestore"));
        //	DebugPrint("About to load criu library " + System.mapLibraryName("criu"));
        //	DebugPrint("Before call to load CheckpointRestore");
        String tmpLib = "/tmp/" + System.mapLibraryName("CheckpointRestore");

        File f = new File(tmpLib);
        if (!f.exists()) {
            copyLibrary("CheckpointRestore");
        }
        System.load(tmpLib);
        System.loadLibrary("criu");
        //	DebugPrint("After call to load criu");

        CheckpointHooks = new ArrayList<Hook>();
        RestoreHooks = new ArrayList<Hook>();
        CRContext = new CheckpointRestore();
        try {
            java.lang.reflect.Field libs
                    = ClassLoader.class.getDeclaredField("loadedLibraryNames");
            libs.setAccessible(true);
            System.out.println(libs.get(ClassLoader.getSystemClassLoader()));
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
