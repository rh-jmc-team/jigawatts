package com.redhat.jigawatts;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Date;

class LibraryLoader {

    private static URL getInternalLibrary() {
        String libraryName = System.mapLibraryName("Jigawatts");
        return Jigawatts.class.getClassLoader().getResource(libraryName);
    }

    private static InputStream getInternalLibraryStream() throws IOException {
        return getInternalLibrary().openStream();
    }

    private static void copyLibrary(String library, File desFile) throws IOException {
        jigaLog("Extracting internal library to " + desFile.getAbsolutePath());
        jigaLog("Internal library is: " + getInternalLibrary().toExternalForm());
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
            if (getVerboseFile() == null) {
                System.err.println(s);
            } else {
                Path logPath = Paths.get(getVerboseFile());
                try {
                    if (!logPath.toFile().exists()){
                        logPath.toFile().createNewFile();
                    }
                    Files.write(logPath, ("[" + new Date().toString() + "] " + s + "\n").getBytes(), StandardOpenOption.APPEND);
                } catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        }
    }

    private static String getPropertyOrVar(String s) {
        String prop = System.getProperty(s);
        if (prop != null) {
            return trimToNull(prop);
        }
        return trimToNull(System.getenv(propertyToVar(s)));
    }

    static String propertyToVar(String s) {
        return s.toUpperCase().replace(".", "_");
    }

    static String trimToNull(String s) {
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

    private static String getVerboseFile() {
        return getPropertyOrVar(VERBOSE_FILE_PROP);
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
        jigaLog("Loading internal library from " + tmpLibrary.getAbsolutePath());
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
    private static final String VERBOSE_FILE_PROP = "jigawatts.verbose.file";
    private static final String SYSTEM_LIB_SWITCH = "SYSTEM";

    static void loadLibrary() {
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
    }


    private static String PROP = "-D";
    private static String VAR = "$";

    static void sayHello() {
        System.out.println("Native library loaded!");
        System.out.println("If jigawatts is packed with embedded dynamic library, it is used in advance. If it is missing, the system library is searched for.");
        System.out.println("To change the loading of native bits you can use following properties/variables. Property is used with priority.");
        System.out.println(" * The internal library will be unpacked and loaded from given file. If given file exists, it is not overwritten");
        System.out.println("    " + PROP + LIBRARY_TARGETFILE_PROP + "/" + VAR + propertyToVar(LIBRARY_TARGETFILE_PROP) + ": " + getInternalLibraryExtractedFile());
        System.out.println(" * The internal library will not be used, given file will be used");
        System.out.println("    " + PROP + LIBRARY_EXTERNAL_PROP + "/" + VAR + propertyToVar(LIBRARY_EXTERNAL_PROP) + ": " + getExternalLibraryFile());
        System.out.println("   You may use value of " + SYSTEM_LIB_SWITCH + " to force search of system library even with internal library present");
        System.out.println(" * This switch can set library loading logging to 'true'");
        System.out.println("    " + PROP + VERBOSE_FILE_PROP + "/" + VAR + propertyToVar(VERBOSE_FILE_PROP) + ": " + getVerboseFile());
        System.out.println(" * This switch can set library loading logging to append to exact file instead of stderr");
        System.out.println("    " + PROP + VERBOSE_PROP + "/" + VAR + propertyToVar(VERBOSE_PROP) + ": " + getVerbose());
    }
}
