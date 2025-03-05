package org.mz.csaude.dbsyncfeatures.updates.manager.remote;

import com.jcraft.jsch.JSchException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.mz.csaude.dbsyncfeatures.core.manager.artemis.CustomMessageListenerContainer;
import org.mz.csaude.dbsyncfeatures.core.manager.utils.ApplicationProfile;
import org.mz.csaude.dbsyncfeatures.core.manager.utils.SSHCommandExecutor;
import org.mz.csaude.dbsyncfeatures.core.manager.utils.Utils;
import org.mz.csaude.dbsyncfeatures.updates.manager.model.UpdatedSite;
import org.mz.csaude.dbsyncfeatures.updates.manager.service.ApplicationUpdateLogService;
import org.mz.csaude.dbsyncfeatures.updates.manager.service.RemoteSiteUpdateProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
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

						if(executeScript){
							CustomMessageListenerContainer.enableAcknowledgement();
							String fileName = exchange.getProperty("fileName", String.class);

							if (exchange.getException() == null) {
								Logger.getAnonymousLogger().info("Executing update Script");
								this.sshCommandExecutor.processBashCommand(sshCommandExecutor.getFilePath(), true);
							}
							Logger.getAnonymousLogger().info("Processing of remote site update finished.");

							exchange.getMessage().setBody(this.createUpdateSiteLog(fileName));
						}
					})
				.marshal()
				.json(JsonLibrary.Jackson, UpdatedSite.class)
				.to(successUpdateNotificationQueue)
					.process( exchange -> {
						Logger.getAnonymousLogger().info("Update process finalized.");
					})
				.end();
	}

	public UpdatedSite createUpdateSiteLog(String fileName) throws JSchException, InterruptedException, IOException {
		String homeDir = this.sshCommandExecutor.getHomeDir();
		UpdatedSite updateSiteLog = new UpdatedSite();
		String dbsyncVersion = this.getDbSyncVersion(homeDir + "/scripts/release_info.sh");

		String scriptName = new File(fileName).getName().replaceAll("\\.sh$", "");
		String logFilePath = this.sshCommandExecutor.getLogDir() + scriptName + "_execution.log";

		String logContent =  Utils.readFileContent(logFilePath);
		updateSiteLog.setVersion(dbsyncVersion
		);
		updateSiteLog.setSiteId(this.sshCommandExecutor.getDbsyncSenderId());
		updateSiteLog.setLog(logContent);
		updateSiteLog.setExecuted(true);
		updateSiteLog.setReceivedDate(new Date());
		updateSiteLog.setScriptName(fileName);
		updateSiteLog.setCreatedAt(new Date());
		return updateSiteLog;
	}

	public String getDbSyncVersion(String path) {
		String dbsyncVersion = null;
		try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("export OPENMRS_EIP_APP_RELEASE_URL=")) {
					dbsyncVersion = line.split("=")[1].replace("\"", "");
					break;
				}
			}

			return dbsyncVersion;
		} catch (IOException e) {
			Logger.getAnonymousLogger().info("Error while reading remote site dbsync version.");
			return null;
		}
	}
}