package org.mz.csaude.dbsyncfeatures.updates.manager.remote;

import com.jcraft.jsch.JSchException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.mz.csaude.dbsyncfeatures.core.manager.artemis.CustomMessageListenerContainer;
import org.mz.csaude.dbsyncfeatures.core.manager.utils.ApplicationProfile;
import org.mz.csaude.dbsyncfeatures.core.manager.utils.SSHCommandExecutor;
import org.mz.csaude.dbsyncfeatures.core.manager.utils.Utils;
import org.mz.csaude.dbsyncfeatures.updates.manager.model.ScriptExecutionStatus;
import org.mz.csaude.dbsyncfeatures.updates.manager.model.UpdateReport;
import org.mz.csaude.dbsyncfeatures.updates.manager.service.ApplicationUpdateLogService;
import org.mz.csaude.dbsyncfeatures.updates.manager.service.RemoteSiteUpdateProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

@Component
@Profile(ApplicationProfile.REMOTE)
public class RemoteSiteUpdateProcessorRouter extends RouteBuilder {
	
	@Value("${artemis.dbsync.updates.endpoint}")
	private String notificationsEndpoint;

	@Autowired
	private SSHCommandExecutor sshCommandExecutor;

	@Autowired
	private ApplicationUpdateLogService applicationUpdateLogService;

	@Value("${artemis.dbsync.update.response.endpoint}")
	private String successUpdateNotificationQueue;
	@Override
	public void configure() {
		String srcUri = notificationsEndpoint;
		String dstUri = "log:mylog";

		from(srcUri)
				.routeId("process-update-file")
				.process(new RemoteSiteUpdateProcessor(sshCommandExecutor, applicationUpdateLogService))
				.to(dstUri)
					.onCompletion()
					.onCompleteOnly()
					.process( exchange -> {
						boolean executeScript = (boolean) exchange.getProperty("executeScript");

						CustomMessageListenerContainer.enableAcknowledgement();
						String fileName = exchange.getProperty("fileName", String.class);
						ScriptExecutionStatus status = exchange.getProperty("ScriptExecutionStatus", ScriptExecutionStatus.class);
						Map<String, String> scriptExecutionState = null;

						if(executeScript){
							if (exchange.getException() == null) {
								Logger.getAnonymousLogger().info("Executing update Script");
								scriptExecutionState = this.sshCommandExecutor.processBashCommand(sshCommandExecutor.getFilePath(), true);
							}
						}
						exchange.getMessage().setBody(this.createUpdateSiteLog(executeScript, fileName, scriptExecutionState, status));
						Logger.getAnonymousLogger().info("Processing of remote site update finished.");
					})
				.marshal()
				.json(JsonLibrary.Jackson, UpdateReport.class)
				.to(successUpdateNotificationQueue)
					.process( exchange -> {
						Logger.getAnonymousLogger().info("Update process finalized.");
					})
				.end();
	}

	public UpdateReport createUpdateSiteLog(boolean executeScript, String fileName, Map<String, String> scriptExecutionState, ScriptExecutionStatus status) throws JSchException, InterruptedException, IOException {
		String homeDir = this.sshCommandExecutor.getHomeDir();
		UpdateReport updateSiteLog = new UpdateReport();
		String dbsyncVersion = this.getDbSyncVersion(homeDir + "/scripts/release_info.sh");

		if (scriptExecutionState != null) {
			String logContent =  Utils.readFileContent(scriptExecutionState.get("logFile"));
			updateSiteLog.setLog(logContent);
			updateSiteLog.setExecuted(Objects.equals(scriptExecutionState.get("executionStatus"), "0"));
		}

		updateSiteLog.setVersion(dbsyncVersion
		);
		updateSiteLog.setSiteId(this.sshCommandExecutor.getDbsyncSenderId());

		updateSiteLog.setReceivedDate(new Date());
		updateSiteLog.setScriptName(fileName);
		updateSiteLog.setCreatedAt(new Date());

		if  (executeScript) {
			if(updateSiteLog.isExecuted()){
				updateSiteLog.setExecutionStatus(ScriptExecutionStatus.SCRIPT_SUCCESSFULLY_EXECUTED);
			} else {
				updateSiteLog.setExecutionStatus(ScriptExecutionStatus.ERROR_TRYING_TO_EXECUTE_SCRIPT);
			}
		}else{
			updateSiteLog.setExecutionStatus(status);
		}

		return updateSiteLog;
	}

	public String getDbSyncVersion(String path) {
		String dbsyncVersion = null;
		try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
			String line;
			while ((line = reader.readLine()) != null && dbsyncVersion == null) {
				if (line.startsWith("export OPENMRS_EIP_APP_RELEASE_URL=")) {
					dbsyncVersion = line.split("=")[1].replace("\"", "");
				}
			}

			return dbsyncVersion;
		} catch (IOException e) {
			Logger.getAnonymousLogger().info("Error while reading remote site dbsync version.");
			return "Undetermined";
		}
	}
}