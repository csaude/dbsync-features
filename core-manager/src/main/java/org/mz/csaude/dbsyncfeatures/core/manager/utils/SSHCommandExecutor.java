package org.mz.csaude.dbsyncfeatures.core.manager.utils;

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
import java.util.HashMap;
import java.util.Map;

@Service
@Profile(ApplicationProfile.REMOTE)
public class SSHCommandExecutor {

    private static final Logger logger = LoggerFactory.getLogger(SSHCommandExecutor.class);
    private static final String loDirectory = "/home/eip/logs/automatic-updates/";

    @Value("${db-sync.senderId}")
    private String dbsyncSenderId;

    @Value("${eip.home}")
    private String homeDir;

    private String filePath;

    // @Value("${log.dir}")
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

    public Map<String, String> processBashCommand(String scriptPath, boolean saveLog) {

        Map<String, String> result = new HashMap<>();
        result.put("logFile", null);
        result.put("executionStatus", null);

        File logFile = null;
        try {
            ProcessBuilder chmodProcessBuilder = new ProcessBuilder("chmod", "+x", scriptPath);
            logFile = generateLogFile(saveLog);
            result.put("logFile", logFile.getAbsolutePath());

            Process chmodProcess = chmodProcessBuilder.start();
            InputStream errorStream = chmodProcess.getErrorStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream));

            String line;
            while ((line = reader.readLine()) != null) {
                appendToLogFile(logFile, line);
            }
            int chmodExitCode = chmodProcess.waitFor();

            if (chmodExitCode != 0) {
                result.put("executionStatus", String.valueOf(chmodExitCode));
                appendToLogFile(logFile, "Error making the script executable. Exit code: " + chmodExitCode);

                return result;
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
            result.put("executionStatus", String.valueOf(exitCode));

            return result;
        } catch (IOException | InterruptedException e) {
            appendToLogFile(logFile, "Error occurred executing the script : " + scriptPath +  "\\n"  + e.getMessage());
            result.put("executionStatus", String.valueOf(1));

            return result;
        }
    }


    private File generateLogFile(boolean saveLog) throws IOException {
        File logFile = null;

        if(saveLog){
            String scriptName = new File(filePath).getName().replaceAll("\\.sh$", "");
            String logFilePath = SSHCommandExecutor.loDirectory + scriptName + "_execution.log";

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

    public static void appendToLogFile(File logFile, String text) {
        try (FileWriter writer = new FileWriter(logFile, true); // true = append mode
             BufferedWriter bw = new BufferedWriter(writer)) {
            bw.write(text);
        } catch (IOException e) {
            logger.error("Failed to append to log file: {}\n{}", logFile.getPath(), e.getMessage());
        }
    }



    /**
     * {@link org.mz.csaude.dbsyncfeatures.core.manager.utils.SSHCommandExecutor.ProcessOutputReader }
     * Class that reads and process output streams from a shell script execution
     * It's used to read success or error execution
     */

    static class ProcessOutputReader implements Runnable {

        // InputStream from which the script is read, success or error from a script execution
        private final java.io.InputStream scriptExecutionOutput;

        // Path of the log file to append the output
        private final File logFile;

        public ProcessOutputReader(InputStream inputStream, File logFile) {
            this.scriptExecutionOutput = inputStream;
            this.logFile = logFile;
        }

        @Override
        public void run() {
            try (java.util.Scanner scanner = new java.util.Scanner(this.scriptExecutionOutput).useDelimiter("\\A")) {
                while (scanner.hasNext()) {
                    if(this.logFile != null){
                        appendToLogFile(this.logFile, scanner.next());
                    }else{
                        logger.info(scanner.next());
                    }
                }
            }
        }
    }
}
