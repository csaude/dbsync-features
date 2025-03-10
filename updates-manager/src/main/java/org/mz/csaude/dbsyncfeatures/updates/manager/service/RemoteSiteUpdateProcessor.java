package org.mz.csaude.dbsyncfeatures.updates.manager.service;

import com.jcraft.jsch.JSchException;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.mz.csaude.dbsyncfeatures.core.manager.artemis.CustomMessageListenerContainer;
import org.mz.csaude.dbsyncfeatures.core.manager.utils.ApplicationProfile;
import org.mz.csaude.dbsyncfeatures.core.manager.utils.SSHCommandExecutor;
import org.mz.csaude.dbsyncfeatures.core.manager.utils.Utils;
import org.mz.csaude.dbsyncfeatures.updates.manager.model.ApplicationUpdateLog;
import org.mz.csaude.dbsyncfeatures.updates.manager.model.ScriptExecutionStatus;
import org.mz.csaude.dbsyncfeatures.updates.manager.model.ScriptInfo;
import org.mz.csaude.dbsyncfeatures.updates.manager.model.ShareRemoteUpdateFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Service
@Profile(ApplicationProfile.REMOTE)
public class RemoteSiteUpdateProcessor implements Processor {
    private static final Logger logger = LoggerFactory.getLogger(RemoteSiteUpdateProcessor.class);

    private SSHCommandExecutor sshCommandExecutor;
    private ApplicationUpdateLogService applicationUpdateLogService;


    public RemoteSiteUpdateProcessor(SSHCommandExecutor sshCommandExecutor, ApplicationUpdateLogService applicationUpdateLogService) {
        this.sshCommandExecutor = sshCommandExecutor;
        this.applicationUpdateLogService = applicationUpdateLogService;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        logger.info(" Starting the Processing of remote site update");
        String messageBody = exchange.getIn().getBody(String.class);
        ShareRemoteUpdateFile shareRemoteUpdateFile = Utils.fromJson(messageBody, ShareRemoteUpdateFile.class);
        ScriptInfo scriptInfo =  Utils.fromBytes(shareRemoteUpdateFile.getData(), ScriptInfo.class);

        ApplicationUpdateLog applicationUpdateLog = applicationUpdateLogService.findByCurrentVersion(shareRemoteUpdateFile.getFileName());

        // Validate if the site is allowed to update
        exchange.setProperty("executeScript", Boolean.FALSE);
        if (!scriptInfo.getSitesToUpdate().contains(this.sshCommandExecutor.getDbsyncSenderId())) {
            logger.info("The site {} is not allowed to be updated.", this.sshCommandExecutor.getDbsyncSenderId());
            exchange.setProperty("ScriptExecutionStatus", ScriptExecutionStatus.SITE_NOT_ALLOWED_TO_UPDATE);
        }
        else if (applicationUpdateLog != null){
            exchange.setProperty("ScriptExecutionStatus", ScriptExecutionStatus.SCRIPT_ALREADY_EXECUTED);
        } else {

            String updateFile = this.sshCommandExecutor.getHomeDir() + "/" + shareRemoteUpdateFile.getFileName();

            if (!Files.exists(Paths.get(updateFile))) {
                Files.createFile(Paths.get(updateFile));
            }

            File file = new File(updateFile);
            Files.write(file.toPath(), scriptInfo.getScriptData().getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
            this.sshCommandExecutor.setFilePath(updateFile);
            this.createApplicationUpdateLog(shareRemoteUpdateFile.getFileName());

            exchange.setProperty("fileName", shareRemoteUpdateFile.getFileName());
            exchange.setProperty("executeScript", Boolean.TRUE);
            CustomMessageListenerContainer.enableAcknowledgement();
        }
    }

    public void createApplicationUpdateLog(String fileName) throws JSchException, InterruptedException, IOException {
        ApplicationUpdateLog newApplicationUpdateLog = new ApplicationUpdateLog();

        newApplicationUpdateLog.setCurrentVersion(fileName);
        newApplicationUpdateLog.setSiteId(this.sshCommandExecutor.getDbsyncSenderId());
        this.applicationUpdateLogService.createEntity(newApplicationUpdateLog);
    }
}
