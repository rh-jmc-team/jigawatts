/*
 * Copyright 2021 Red Hat, Inc.
 *
 * This file is part of JavaCRIU.
 *
 * JavaCRIU is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2, or (at your
 * option) any later version.
 *
 * JavaCRIU is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JavaCRIU; see the file COPYING.  If not see
 * <http://www.gnu.org/licenses/>.
 *
 * Linking this library statically or dynamically with other modules
 * is making a combined work based on this library.  Thus, the terms
 * and conditions of the GNU General Public License cover the whole
 * combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent
 * modules, and to copy and distribute the resulting executable under
 * terms of your choice, provided that you also meet, for each linked
 * independent module, the terms and conditions of the license of that
 * module.  An independent module is a module which is not derived from
 * or based on this library.  If you modify this library, you may extend
 * this exception to your version of the library, but you are not
 * obligated to do so.  If you do not wish to do so, delete this
 * exception statement from your version.
 */
 

package org.checkpoint;

import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;

public class CheckpointRestore {

    private static final CheckpointRestore crContext;

    private static final List<Hook> checkpointHooks;
    private static final List<Hook> restoreHooks;

    public static void cleanupTheWorld() {
        System.gc();
        System.gc();
    }

    private native void checkTheWorldNative();

    private native void saveTheWorldNative(String dir);

    private native void restoreTheWorldNative(String dir);

    public static native void migrateTheWorld();

    public static native void saveTheWorldIncremental();

    public static void checkTheWorld() {
        crContext.checkTheWorldNative();
    }

    public static void saveTheWorld(String dir) throws IOException {
        for (Hook h : checkpointHooks) {
            h.run();
        }

        writeRestoreHooks(dir);
        crContext.saveTheWorldNative(dir);
    }

    public static void writeRestoreHooks(String dir) throws IOException {
        
        Files.createDirectories(Paths.get(dir));
            
        try (FileOutputStream f = new FileOutputStream(new File(dir, "/JavaRestoreHooks.txt"));
             ObjectOutputStream o = new ObjectOutputStream(f)) {
            
            o.writeInt(restoreHooks.size());
            
            for (Hook h : restoreHooks) {
                o.writeObject(h);
            }
        }
    }

    public static void readRestoreHooks(String dir) throws IOException {
        
        try (FileInputStream f = new FileInputStream(new File(dir + "/JavaRestoreHooks.txt"));
             ObjectInputStream o = new ObjectInputStream(f)) {

            int count = o.readInt();
            for (int i = 0; i < count; i++) {
                Hook h = (Hook) o.readObject();
                restoreHooks.add(h);
            }

        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void restoreTheWorld(String dir) throws IOException {
        crContext.restoreTheWorldNative(dir);
        readRestoreHooks(dir);
        for (Hook h : restoreHooks) {
            h.run();
        }
    }

    public static void registerCheckpointHook(Hook h) {
        crContext.checkpointHooks.add(h);
    }

    public static void registerRestoreHook(Hook h) {
        crContext.restoreHooks.add(h);
    }
    
    // package private for junit tests
    static void clearCheckpointHooks() {
        crContext.checkpointHooks.clear();
    }
    
    static void clearRestoreHooks() {
        crContext.restoreHooks.clear();
    }
    
    static List<Hook> getCheckpointHooks() {
        return Collections.unmodifiableList(crContext.checkpointHooks);
    }
    
    static List<Hook> getRestoreHooks() {
        return Collections.unmodifiableList(crContext.restoreHooks);
    }

    private static void copyLibrary(String library, String destDir) throws IOException {
        
        String libraryName = System.mapLibraryName(library);
        String libraryFile = destDir + "/" + libraryName;
        
        try (InputStream is = CheckpointRestore.class.getClassLoader().getResourceAsStream(libraryName);
             FileOutputStream os = new FileOutputStream(libraryFile)) {
            
            byte[] buf = new byte[1024];

            int bytesRead;

            while ((bytesRead = is.read(buf)) > 0) {
                os.write(buf, 0, bytesRead);
            }
        }
    }

    static {

//        System.out.println("Library path = " + System.getProperty("java.library.path"));
//        System.out.println("About to load Checkpoint Restore library " + System.mapLibraryName("CheckpointRestore"));
//        System.out.println("About to load criu library " + System.mapLibraryName("criu"));
//        System.out.println("Before call to load CheckpointRestore");
        
        String libDir = "/tmp";
        String tmpLib = libDir+ "/" + System.mapLibraryName("CheckpointRestore");
        
        if (!Files.exists(Paths.get(tmpLib))) {
            try {
                copyLibrary("CheckpointRestore", libDir);
            } catch (IOException ex) {
                throw new RuntimeException("can't initialize CheckpointRestore",ex);
            }
        }
        System.load(tmpLib);
        System.loadLibrary("criu");
//        System.out.println("After call to load criu");

        checkpointHooks = new ArrayList<Hook>();
        restoreHooks = new ArrayList<Hook>();
        crContext = new CheckpointRestore();
        
        // this won't work with newer jdks since libloading impl changed
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
