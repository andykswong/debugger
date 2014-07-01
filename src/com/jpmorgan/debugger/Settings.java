package com.jpmorgan.debugger;

import java.io.File;


/**
 * This class contains global constants and routines
 */
public class Settings {

    /** The test depth beyond which we don't care */
    public static final int MAX_DISTANCE = 20;

    /**
     * Name of the system property for the path of the output directory
     */
    public static final String WORK_DIR_PROPERTY_NAME = "work.dir";
    
    public static final String TMP_DIR_PROPERTY_NAME = "java.io.tmpdir";

    /**
     * Get the working directory name
     * @return  the working directory name
     */
    public static String getWorkDirName() {
        String workDirName = System.getProperty(
                WORK_DIR_PROPERTY_NAME);

        System.err.println(WORK_DIR_PROPERTY_NAME + "=" + workDirName);

        if (workDirName == null) {
            workDirName = System.getProperty(TMP_DIR_PROPERTY_NAME);
        }

        if (!workDirName.endsWith("/") && !workDirName.endsWith("\\")) {
            workDirName = workDirName + "/";
        }

        File workDir = new File(workDirName);

        if (!workDir.isDirectory()) {
            throw new DebuggerRuntimeException("The value in " +
                WORK_DIR_PROPERTY_NAME + " (" + workDirName +
                ") is not the name of a directory.");

        }

        if (!workDir.canWrite()) {
            throw new DebuggerRuntimeException("The value in " +
                WORK_DIR_PROPERTY_NAME + " (" + workDirName +
                ") is not the name of a directory to which pea can write.");
        }

        return workDirName;
    }

}
