package org.mz.csaude.dbsyncfeatures.updates.manager.remote;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.mz.csaude.dbsyncfeatures.core.manager.artemis.CustomMessageListenerContainer;
import org.mz.csaude.dbsyncfeatures.updates.manager.model.UpdatedSite;
import org.mz.csaude.dbsyncfeatures.updates.manager.service.ApplicationUpdateLogService;
import org.mz.csaude.dbsyncfeatures.core.manager.utils.ApplicationProfile;
import org.mz.csaude.dbsyncfeatures.core.manager.utils.SSHCommandExecutor;
import org.mz.csaude.dbsyncfeatures.updates.manager.service.RemoteSiteUpdateProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

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
				.process(new RemoteSiteUpdateProcessor(sshCommandExecutor, applicationUpdateLogService))
				.to(dstUri)
					.onCompletion()
					.onCompleteOnly()
					.process( exchange -> {
						CustomMessageListenerContainer.enableAcknowledgement();
						String version = exchange.getProperty("version", String.class);
						exchange.getMessage().setBody(new UpdatedSite(this.sshCommandExecutor.getDbsyncSenderId(), version));
				})
				.marshal()
				.json(JsonLibrary.Jackson, UpdatedSite.class)
				.to(successUpdateNotificationQueue)
				.end();
	}
}

