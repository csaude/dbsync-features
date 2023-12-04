package org.mz.csaude.dbsynfeatures.updates.manager.service;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.mz.csaude.dbsynfeatures.core.manager.utils.CommonConverter;
import org.mz.csaude.dbsynfeatures.core.manager.utils.SSHCommandExecutor;
import org.mz.csaude.dbsynfeatures.updates.manager.model.ApplicationUpdateLog;
import org.mz.csaude.dbsynfeatures.updates.manager.model.ShareRemoteUpdateFile;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

@Service
public class ConsumeUpdateMessageProcessor implements Processor {

    private SSHCommandExecutor sshCommandExecutor;
    private ApplicationUpdateLogService applicationUpdateLogService;


    public ConsumeUpdateMessageProcessor(SSHCommandExecutor sshCommandExecutor, ApplicationUpdateLogService applicationUpdateLogService) {
        this.sshCommandExecutor = sshCommandExecutor;
        this.applicationUpdateLogService = applicationUpdateLogService;
    }

    @Override
    public void process(Exchange exchange) throws Exception {

        String messageBody = exchange.getIn().getBody(String.class);
        ShareRemoteUpdateFile shareRemoteUpdateFile = CommonConverter.fromJson(messageBody, ShareRemoteUpdateFile.class);
        ApplicationUpdateLog applicationUpdateLog = applicationUpdateLogService.findByCurrentVersion(shareRemoteUpdateFile.getFileName());

        if (applicationUpdateLog != null){
            return;
        }

        String scriptContent = new String(shareRemoteUpdateFile.getData(), StandardCharsets.UTF_8);
        File file = new File(this.sshCommandExecutor.getEipUpdateFilePath());
        //update specific file
        Files.write(file.toPath(), shareRemoteUpdateFile.getData(), StandardOpenOption.TRUNCATE_EXISTING);

        //Grant file permission
        String grantPermission = "chmod +x /home/eip/updates";
        this.sshCommandExecutor.processBashCommand(this.sshCommandExecutor.getEipUpdateFilePath());

        // Mark file as executable
        String chmodCommand = "chmod +x " + this.sshCommandExecutor.getEipUpdateFilePath();
        this.sshCommandExecutor.processBashCommand(this.sshCommandExecutor.getEipUpdateFilePath());

        //Execute update file
        this.sshCommandExecutor.processBashCommand(this.sshCommandExecutor.getEipUpdateFilePath());


        //After updating successuly the aplication, log the update
        applicationUpdateLog = new ApplicationUpdateLog();
        applicationUpdateLog.setCurrentVersion(shareRemoteUpdateFile.getFileName());
        applicationUpdateLog.setSiteId(this.sshCommandExecutor.getDbsyncSenderId());
        this.applicationUpdateLogService.createEntity(applicationUpdateLog);
    }
}
