/*
 * Copyright 2021 Red Hat, Inc.
 *
 * This file is part of Jigawatts.
 *
 * Jigawatts is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2, or (at your
 * option) any later version.
 *
 * Jigawatts is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Jigawatts; see the file COPYING.  If not see
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
 

package com.redhat.jigawatts;

import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Collections;

public class Jigawatts {

    private static final Jigawatts crContext;

    private static final List<Hook> checkpointHooks;
    private static final List<Hook> restoreHooks;

    public static void cleanupTheWorld() {
        System.gc();
        System.gc();
    }

    private native void checkTheWorldNative();

    private native void saveTheWorldNative(String dir, boolean leaveRunning);

    private native void restoreTheWorldNative(String dir);

    public static void checkTheWorld() {
        crContext.checkTheWorldNative();
    }

    public static void saveTheWorld(String dir, boolean leaveRunning) throws IOException {
        for (Hook h : checkpointHooks) {
            h.run();
        }

        writeRestoreHooks(dir);
        crContext.saveTheWorldNative(dir, leaveRunning);
    }

    public static void saveTheWorld(String dir) throws IOException {
	saveTheWorld(dir, true);
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

    private static URL getInternalLibrary() {
        String libraryName = System.mapLibraryName("Jigawatts");
        return Jigawatts.class.getClassLoader().getResource(libraryName);
    }

    private static InputStream getInternalLibraryStream() throws IOException {
        return getInternalLibrary().openStream();
    }

    private static void copyLibrary(String library, File desFile) throws IOException {
        jigaLog("Extracting internal libray to " + desFile.getAbsolutePath());
        jigaLog("Interal library is: " + getInternalLibrary().toExternalForm());
        try (InputStream is = getInternalLibraryStream();
             FileOutputStream os = new FileOutputStream(desFile)) {
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = is.read(buf)) > 0) {
                os.write(buf, 0, bytesRead);
            }
        }
    }

    private static void jigaLog(String s) {
        if (getVerbose()) {
            System.err.println(s);
        }
    }

    private static String getPropertyOrVar(String s) {
        String prop = System.getProperty(s);
        if (prop != null) {
            return trimToNull(prop);
        }
        return trimToNull(System.getenv(s.toUpperCase().replace(".", "_")));
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        s = s.trim();
        if (s.isEmpty()) {
            return null;
        }
        return s;
    }

    private static boolean getVerbose() {
        return "true".equalsIgnoreCase(getPropertyOrVar(VERBOSE_PROP));
    }

    private static String getInternalLibraryExtractedFile() {
        return getPropertyOrVar(LIBRARY_TARGETFILE_PROP);
    }

    private static String getExternalLibraryFile() {
        return getPropertyOrVar(LIBRARY_EXTERNAL_PROP);
    }

    private static void loadInJarLibrary() {
        File tmpLibrary = null;
        try {
            if (getInternalLibraryExtractedFile() == null) {
                tmpLibrary = File.createTempFile("jigawatts", ".so");
            } else {
                tmpLibrary = new File(getInternalLibraryExtractedFile());
            }
            if (!(tmpLibrary.exists() && tmpLibrary.length() > 0)) {
                copyLibrary("Jigawatts", tmpLibrary);
            }
        } catch (IOException ex) {
            throw new RuntimeException("can't initialize Jigawatts", ex);
        }
        jigaLog("Loading internal libray from " + tmpLibrary.getAbsolutePath());
        System.load(tmpLibrary.getAbsolutePath());
    }

    private static void loadSystemLib() {
        String lib = System.mapLibraryName("Jigawatts");
        jigaLog("Loading system library " + lib);
        for (String path : System.getProperty("java.library.path").split(":")) {
            try {
                String libPath = path + File.separator + lib;
                System.load(libPath);
                jigaLog("Loaded system library: " + libPath);
                return;
            } catch (UnsatisfiedLinkError e) {
                continue;
            }
        }
        throw new RuntimeException(lib + " library not found on java.library.path/LD_LIBRARY_PATH!");
    }

    private static final String LIBRARY_TARGETFILE_PROP = "jigawatts.library.targetfile";
    private static final String LIBRARY_EXTERNAL_PROP = "jigawatts.library";
    private static final String VERBOSE_PROP = "jigawatts.verbose";
    private static final String SYSTEM_LIB_SWITCH = "SYSTEM";

    static {
        if (getInternalLibrary() == null) {
            jigaLog("Internal library missing!");
            if (getExternalLibraryFile() == null) {
                loadSystemLib();
            } else {
                System.load(getExternalLibraryFile());
                jigaLog("Loaded custom library: " + getExternalLibraryFile());
            }
        } else {
            jigaLog("Internal library present");
            if (getExternalLibraryFile() == null) {
                loadInJarLibrary();
            } else if (SYSTEM_LIB_SWITCH.equals(getExternalLibraryFile())) {
                loadSystemLib();
            } else {
                jigaLog("Loading custom library: " + getExternalLibraryFile());
                System.load(getExternalLibraryFile());
            }
        }
        System.loadLibrary("criu");
        checkpointHooks = new ArrayList<Hook>();
        restoreHooks = new ArrayList<Hook>();
        crContext = new Jigawatts();
    }


    public static void main(String... args) {
        System.out.println("Native library loaded!");
        System.out.println(LIBRARY_TARGETFILE_PROP + ": " + getInternalLibraryExtractedFile());
        System.out.println(LIBRARY_EXTERNAL_PROP + ": " + getExternalLibraryFile());
        System.out.println("You may  use " + SYSTEM_LIB_SWITCH + " to force search of system library with internal library present");
        System.out.println(VERBOSE_PROP + ": " + getVerbose());
    }

}
