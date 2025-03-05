package org.mz.csaude.dbsyncfeatures.core.manager.utils;

import com.jcraft.jsch.JSchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
@Profile(ApplicationProfile.REMOTE)
public class SSHCommandExecutor {

    private static final Logger logger = LoggerFactory.getLogger(SSHCommandExecutor.class);

    @Value("${db-sync.senderId}")
    private String dbsyncSenderId;

    @Value("${eip.home}")
    private String homeDir;

    private String filePath;

    @Value("${log.dir}")
    private String logDir;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getHomeDir() {
        return homeDir;
    }

    public void setDbsyncSenderId(String dbsyncSenderId) {
        this.dbsyncSenderId = dbsyncSenderId;
    }

    public void setHomeDir(String homeDir) {
        this.homeDir = homeDir;
    }

    public String getLogDir() {
        return logDir;
    }

    public void setLogDir(String logDir) {
        this.logDir = logDir;
    }

    public String getDbsyncSenderId() {
        return dbsyncSenderId;
    }
    public int processBashCommand(String scriptPath, boolean saveLOg) throws JSchException, InterruptedException {
        return runShellScript(scriptPath,saveLOg);
    }
    public int runShellCommand(String shellCommand) throws JSchException, InterruptedException {

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("sh", "-c", shellCommand);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            return process.waitFor();
        } catch (IOException | InterruptedException e) {
            logger.error("Error occurred executing this command: {}\n{}", shellCommand, e.getMessage());
            return -1;
        }
    }

    public int runShellScript(String scriptPath, boolean saveLog) {


        try {
            ProcessBuilder chmodProcessBuilder = new ProcessBuilder("chmod", "+x", scriptPath);
            File logFile = getLogFile(saveLog);

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
            ProcessOutputReader outputReader = new ProcessOutputReader(process.getInputStream(), logFile);
            ProcessOutputReader errorReader = new ProcessOutputReader(process.getErrorStream(), logFile);

            Thread outputThread = new Thread(outputReader);
            Thread errorThread = new Thread(errorReader);

            outputThread.start();
            errorThread.start();

            int exitCode = process.waitFor();

            outputThread.join();
            errorThread.join();

            return exitCode;
        } catch (IOException | InterruptedException e) {
            logger.error("Error occurred executing the script : {}\n{}", scriptPath, e.getMessage());
        }
        return 1;
    }


    private File getLogFile(boolean saveLog) throws IOException {
        File logFile = null;

        if(saveLog){
            String scriptName = new File(filePath).getName().replaceAll("\\.sh$", "");
            String logFilePath = this.logDir + scriptName + "_execution.log";

            if(!Files.exists(Paths.get(logFilePath))){
                if (Files.notExists(Paths.get(this.logDir))){
                    Files.createDirectory(Paths.get(this.logDir));
                }
                Files.createFile(Paths.get(logFilePath));
            }
            logFile = new File(logFilePath);
        }
        return logFile;
    }
    static class ProcessOutputReader implements Runnable {
        private final java.io.InputStream inputStream;
        private final File logFile;

        public ProcessOutputReader(InputStream inputStream, File logFile) {

            this.inputStream = inputStream;
            this.logFile = logFile;
        }

        private void appendToLogFile(File logFile, String text) {
            try (FileWriter writer = new FileWriter(logFile, true); // true = append mode
                 BufferedWriter bw = new BufferedWriter(writer)) {
                bw.write(text);
            } catch (IOException e) {
                logger.error("Failed to append to log file: " + logFile.getPath() + "\n" + e.getMessage());
            }
        }

        @Override
        public void run() {
            try (java.util.Scanner scanner = new java.util.Scanner(inputStream).useDelimiter("\\A")) {
                while (scanner.hasNext()) {
                    if(this.logFile != null){
                        this.appendToLogFile(this.logFile, scanner.next());
                    }else{
                        System.out.print(scanner.next());
                    }
                }

                }
            }
        }
}
