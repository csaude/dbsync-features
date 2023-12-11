package org.mz.csaude.dbsyncfeatures.core.manager.utils;

import com.jcraft.jsch.JSchException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class SSHCommandExecutor {

    @Value("${eip.update.file.path}")
    private String eipUpdateFile;

    @Value("${db-sync.senderId}")
    private String dbsyncSenderId;

    @Value("${log.path}")
    private String logPath;

    @Value("${eip.run.update.file.path}")
    private String eipRunUpdateFile;

    public String getEipUpdateFilePath() {
        return eipUpdateFile;
    }

    public String getDbsyncSenderId() {
        return dbsyncSenderId;
    }

    public String getLogPath() {
        return logPath;
    }


    public String getEipRunUpdateFile() {
        return eipRunUpdateFile;
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
            this.writeLogExecutions(reader);

            int exitCode = process.waitFor();
            System.out.println("Exited with error code: " + exitCode);
            return exitCode;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return  1;
    }

    public void writeLogExecutions(BufferedReader reader) throws IOException {
        if(!Files.exists(Paths.get(getLogPath()))){
            System.out.println(" Log File not found. Creating it");
            Files.createFile(Paths.get(getLogPath()));
        }
        if (reader != null){
            FileWriter writer = new FileWriter(getLogPath());
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                writer.write(line + System.lineSeparator());
            }
        }

    }
    public int runShellScript(String scriptPath) {

        try {
            ProcessBuilder chmodProcessBuilder = new ProcessBuilder("chmod", "+x", scriptPath);
            Process chmodProcess = chmodProcessBuilder.start();
            InputStream errorStream = chmodProcess.getErrorStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream));

            this.writeLogExecutions(reader);
            int chmodExitCode = chmodProcess.waitFor();

            if (chmodExitCode != 0) {
                System.out.println("Error making the script executable. Exit code: " + chmodExitCode);
                return chmodExitCode;
            }

            ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", scriptPath);
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

            //Log the wrun of updates file
            this.writeLogExecutions(new BufferedReader(new InputStreamReader(process.getErrorStream())));

            System.out.println("Script execution complete. Exit code: " + exitCode);
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
