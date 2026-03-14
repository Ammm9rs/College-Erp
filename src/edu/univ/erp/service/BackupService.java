package edu.univ.erp.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BackupService {

    // ⚠️ KEEP YOUR PATHS (Mac Default)
    private static final String MYSQL_DUMP_PATH = "/usr/local/mysql/bin/mysqldump";
    private static final String MYSQL_PATH = "/usr/local/mysql/bin/mysql";

    // ⚠️ YOUR DATABASE CREDENTIALS
    private static final String DB_USER = "root";
    private static final String DB_PASS = "ammarkrishna";

    private static final String[] DATABASES = {"university_auth", "university_erp"};

    public String backupDatabase() throws Exception {
        String date = new SimpleDateFormat("yyyy-MM-dd_HH-mm").format(new Date());
        String fileName = "backup_" + date + ".sql";
        File file = new File(System.getProperty("user.home") + "/Downloads/" + fileName);

        // --- THE FIX IS HERE ---
        // Added: --set-gtid-purged=OFF
        // Added: --column-statistics=0 (Prevents another common error on Mac)
        String cmd = String.format("%s -u%s -p%s --set-gtid-purged=OFF --column-statistics=0 --databases %s %s > \"%s\"",
                MYSQL_DUMP_PATH, DB_USER, DB_PASS, DATABASES[0], DATABASES[1], file.getAbsolutePath());

        String[] processCmd = {"/bin/sh", "-c", cmd};

        System.out.println("Running Backup: " + cmd);

        ProcessBuilder pb = new ProcessBuilder(processCmd);
        Process process = pb.start();

        printErrors(process); // Log any warnings

        int exitCode = process.waitFor();

        if (exitCode == 0) {
            return file.getAbsolutePath();
        } else {
            throw new Exception("Backup failed (Code " + exitCode + "). See console.");
        }
    }

    public void restoreDatabase(File file) throws Exception {
        if (!file.exists()) throw new Exception("File not found");

        // Command: mysql -u root -pPASS < "path/to/file.sql"
        String cmd = String.format("%s -u%s -p%s < \"%s\"",
                MYSQL_PATH, DB_USER, DB_PASS, file.getAbsolutePath());

        System.out.println("Running Restore: " + cmd);

        String[] processCmd = {"/bin/sh", "-c", cmd};

        ProcessBuilder pb = new ProcessBuilder(processCmd);
        Process process = pb.start();

        printErrors(process);

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new Exception("Restore failed (Code " + exitCode + "). See console.");
        }
    }

    private void printErrors(Process process) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            // Ignore the "Insecure password" warning, it's normal
            if (!line.contains("Using a password on the command line")) {
                System.err.println("MYSQL LOG: " + line);
            }
        }
    }
}