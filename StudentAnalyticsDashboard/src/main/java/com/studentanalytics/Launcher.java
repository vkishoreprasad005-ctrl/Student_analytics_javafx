package com.studentanalytics;

/**
 * Bootstrap launcher for standalone JAR execution.
 * Avoids JavaFX runtime components check when running as an executable JAR.
 */
public class Launcher {
    public static void main(String[] args) {
        Main.main(args);
    }
}
