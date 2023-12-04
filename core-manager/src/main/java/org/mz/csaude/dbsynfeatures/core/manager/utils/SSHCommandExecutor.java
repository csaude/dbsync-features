package org.mz.csaude.dbsynfeatures.core.manager.utils;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Service
public class SSHCommandExecutor {

    @Value("${eip.update.file.path}")
    private String eipUpdateFile;

    @Value("${db-sync.senderId}")
    private String dbsyncSenderId;

    @Value("${log.path}")
    private String logPath;

    public String getEipUpdateFilePath() {
        return eipUpdateFile;
    }

    public String getDbsyncSenderId() {
        return dbsyncSenderId;
    }

    public String getLogPath() {
        return logPath;
    }

    public void processBashCommand(String command) throws JSchException, InterruptedException {
        runShellScript("/home/eip/updates.sh");
    }


    public static void runShellScript(String scriptPath) {

        try {
            ProcessBuilder chmodProcessBuilder = new ProcessBuilder("chmod", "+x", scriptPath);
            Process chmodProcess = chmodProcessBuilder.start();
            InputStream errorStream = chmodProcess.getErrorStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream));

            String line;
            while ((line = reader.readLine()) != null) {
                System.err.println(line);
            }
            int chmodExitCode = chmodProcess.waitFor();

            if (chmodExitCode != 0) {
                System.out.println("Error making the script executable. Exit code: " + chmodExitCode);
                return;
            }

            // Execute the script
            ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", scriptPath);
            Process process = processBuilder.start();

            // Read the output of the script
            // Note: You can redirect output/error streams as needed
            // For simplicity, we're just printing the output to the console here
            ProcessOutputReader outputReader = new ProcessOutputReader(process.getInputStream());
            ProcessOutputReader errorReader = new ProcessOutputReader(process.getErrorStream());

            Thread outputThread = new Thread(outputReader);
            Thread errorThread = new Thread(errorReader);

            outputThread.start();
            errorThread.start();

            // Wait for the process to complete
            int exitCode = process.waitFor();

            // Wait for the output and error threads to finish
            outputThread.join();
            errorThread.join();

            System.out.println("Script execution complete. Exit code: " + exitCode);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    static class ProcessOutputReader implements Runnable {
        private final java.io.InputStream inputStream;

        public ProcessOutputReader(java.io.InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            try (java.util.Scanner scanner = new java.util.Scanner(inputStream).useDelimiter("\\A")) {
                while (scanner.hasNext()) {
                    System.out.print(scanner.next());
                }
            }
        }
    }
}
