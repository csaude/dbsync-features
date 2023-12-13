package org.mz.csaude.dbsyncfeatures.updates.manager.service;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.mz.csaude.dbsyncfeatures.core.manager.utils.ApplicationProfile;
import org.mz.csaude.dbsyncfeatures.core.manager.utils.CommonConverter;
import org.mz.csaude.dbsyncfeatures.core.manager.utils.SSHCommandExecutor;
import org.mz.csaude.dbsyncfeatures.updates.manager.model.ApplicationUpdateLog;
import org.mz.csaude.dbsyncfeatures.updates.manager.model.ShareRemoteUpdateFile;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Service
@Profile(ApplicationProfile.REMOTE)
public class RemoteSiteUpdateProcessor implements Processor {

    private SSHCommandExecutor sshCommandExecutor;
    private ApplicationUpdateLogService applicationUpdateLogService;


    public RemoteSiteUpdateProcessor(SSHCommandExecutor sshCommandExecutor, ApplicationUpdateLogService applicationUpdateLogService) {
        this.sshCommandExecutor = sshCommandExecutor;
        this.applicationUpdateLogService = applicationUpdateLogService;
    }

    @Override
    public void process(Exchange exchange) throws Exception {

        String messageBody = exchange.getIn().getBody(String.class);
        ShareRemoteUpdateFile shareRemoteUpdateFile = CommonConverter.fromJson(messageBody, ShareRemoteUpdateFile.class);
        exchange.setProperty("version", shareRemoteUpdateFile.getFileName());
        ApplicationUpdateLog applicationUpdateLog = applicationUpdateLogService.findByCurrentVersion(shareRemoteUpdateFile.getFileName());

        if (applicationUpdateLog != null){
            return;
        }

        String updateFile = this.sshCommandExecutor.getHomeDir() + "/" + shareRemoteUpdateFile.getFileName();

        if(!Files.exists(Paths.get(updateFile))){
            Files.createFile(Paths.get(updateFile));
        }

        File file = new File(updateFile);
        Files.write(file.toPath(), shareRemoteUpdateFile.getData(), StandardOpenOption.TRUNCATE_EXISTING);

        int executionStatus = this.sshCommandExecutor.processBashCommand(updateFile);

        if (executionStatus == 0){
            ApplicationUpdateLog newApplicationUpdateLog = new ApplicationUpdateLog();
            newApplicationUpdateLog.setCurrentVersion(shareRemoteUpdateFile.getFileName());
            newApplicationUpdateLog.setSiteId(this.sshCommandExecutor.getDbsyncSenderId());
            this.applicationUpdateLogService.createEntity(newApplicationUpdateLog);
        }
    }
}
