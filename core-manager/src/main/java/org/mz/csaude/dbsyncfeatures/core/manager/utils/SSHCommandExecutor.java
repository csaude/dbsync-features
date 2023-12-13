package org.mz.csaude.dbsyncfeatures.core.manager.utils;

import com.jcraft.jsch.JSchException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
@Profile(ApplicationProfile.REMOTE)
public class SSHCommandExecutor {

    @Value("${db-sync.senderId}")
    private String dbsyncSenderId;

    @Value("${eip.home}")
    private String homeDir;

    public String getHomeDir() {
        return homeDir;
    }

    public String getDbsyncSenderId() {
        return dbsyncSenderId;
    }
    public int processBashCommand(String scriptPath) throws JSchException, InterruptedException {
        return runShellScript(scriptPath);
    }
    public int runShellCommand(String shellCommnad){

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(shellCommnad.split("\\s+"));

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            int exitCode = process.waitFor();
            System.out.println("Exited with error code: " + exitCode);
            return exitCode;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return  1;
    }

    public int runShellScript(String scriptPath) {

        try {
            ProcessBuilder chmodProcessBuilder = new ProcessBuilder("chmod", "+x", scriptPath);
            Process chmodProcess = chmodProcessBuilder.start();
            InputStream errorStream = chmodProcess.getErrorStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream));

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            int chmodExitCode = chmodProcess.waitFor();

            if (chmodExitCode != 0) {
                System.out.println("Error making the script executable. Exit code: " + chmodExitCode);
                return chmodExitCode;
            }

            ProcessBuilder processBuilder = new ProcessBuilder("sh", "-c", scriptPath);
            Process process = processBuilder.start();
            ProcessOutputReader outputReader = new ProcessOutputReader(process.getInputStream());
            ProcessOutputReader errorReader = new ProcessOutputReader(process.getErrorStream());

            Thread outputThread = new Thread(outputReader);
            Thread errorThread = new Thread(errorReader);

            outputThread.start();
            errorThread.start();

            int exitCode = process.waitFor();

            outputThread.join();
            errorThread.join();

            return exitCode;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return 1;
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
