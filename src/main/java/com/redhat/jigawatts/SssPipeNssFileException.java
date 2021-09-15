package com.redhat.jigawatts;

import java.io.File;
import java.io.IOException;

public class SssPipeNssFileException extends IOException {

    static final File SSS_NSS_FILE = new File("/var/lib/sss/pipes/nss");

    static void closeSssNssFile() throws IOException {
        if (LibraryLoader.SSS_NSS_IGNORE.equals(LibraryLoader.getSssNss())) {
            return;
        }
        if (LibraryLoader.getSssNss() == null && SSS_NSS_FILE.exists()) {
            LibraryLoader.jigaLog(SSS_NSS_FILE + " exists and " + LibraryLoader.SSS_NSS + " is not set.Cowardly exiting");
            throw new SssPipeNssFileException();
        }
        if ((LibraryLoader.SSS_NSS_FIERCE.equals(LibraryLoader.getSssNss()) || LibraryLoader.SSS_NSS_FORCE.equals(LibraryLoader.getSssNss())) && SSS_NSS_FILE.exists()) {
            LibraryLoader.jigaLog(SSS_NSS_FILE + " exists and " + LibraryLoader.SSS_NSS + " is set to " + LibraryLoader.getSssNss() + ". deleting");
            boolean deleted = SSS_NSS_FILE.delete();
            if (!deleted) {
                LibraryLoader.jigaLog(SSS_NSS_FILE + " removal failed");
                if (LibraryLoader.SSS_NSS_FORCE.equals(LibraryLoader.getSssNss())) {
                    LibraryLoader.jigaLog(LibraryLoader.SSS_NSS + " is set to " + LibraryLoader.getSssNss() + ". Cowardly exiting");
                    throw new SssPipeNssFileException("Failed to delete " + SSS_NSS_FILE);
                } else {
                    LibraryLoader.jigaLog(LibraryLoader.SSS_NSS + " is set to " + LibraryLoader.getSssNss() + ". Bravely continuing.");
                }
            } else {
                LibraryLoader.jigaLog(SSS_NSS_FILE + " removal succeeded");
            }
        }
    }

    static void restoreSssNssFile() throws IOException {
        if (LibraryLoader.SSS_NSS_IGNORE.equals(LibraryLoader.getSssNss())) {
            return;
        }
        if (LibraryLoader.getSssNss() == null) {
            return;
        }
        if ((LibraryLoader.SSS_NSS_FIERCE.equals(LibraryLoader.getSssNss()) || LibraryLoader.SSS_NSS_FORCE.equals(LibraryLoader.getSssNss())) && !SSS_NSS_FILE.exists()) {
            int authConfResult = -1;
            String cmd = "authconfig --enablesssd --update";
            try {
                LibraryLoader.jigaLog("Executing `" + cmd + "`");
                Process p = Runtime.getRuntime().exec(cmd);
                authConfResult = p.waitFor();
                p.destroy();
            } catch (Exception ex) {
                LibraryLoader.jigaLog("cmd fialed: " + ex);
                if (LibraryLoader.SSS_NSS_FORCE.equals(LibraryLoader.getSssNss())) {
                    LibraryLoader.jigaLog(LibraryLoader.SSS_NSS + " is set to " + LibraryLoader.getSssNss() + ". Cowardly exiting");
                    throw new SssPipeNssFileException(ex);
                } else {
                    LibraryLoader.jigaLog(LibraryLoader.SSS_NSS + " is set to " + LibraryLoader.getSssNss() + ". Bravely continuing");
                }
            }
            if (authConfResult != 0) {
                LibraryLoader.jigaLog("cmd exited nonzero: " + authConfResult);
                if (LibraryLoader.SSS_NSS_FORCE.equals(LibraryLoader.getSssNss())) {
                    LibraryLoader.jigaLog(LibraryLoader.SSS_NSS + " is set to " + LibraryLoader.getSssNss() + ". Cowardly exiting");
                    throw new SssPipeNssFileException("Failed to restore " + SSS_NSS_FILE + " cmd `" + cmd + "` exited with:" + authConfResult);
                } else {
                    LibraryLoader.jigaLog(LibraryLoader.SSS_NSS + " is set to " + LibraryLoader.getSssNss() + ". Bravely continuing");
                }
            } else {
                LibraryLoader.jigaLog("cmd succeeded.");
            }
        }
    }

    public SssPipeNssFileException() {
        super(LibraryLoader.sssNssLine1() + ";" + LibraryLoader.sssNssLine2());
    }

    public SssPipeNssFileException(String s) {
        super(s);
    }

    public SssPipeNssFileException(Exception ex) {
        super(ex);
    }

}
