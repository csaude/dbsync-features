package org.mz.csaude.dbsynfeatures.updates.manager.consumer;

import org.apache.camel.builder.RouteBuilder;
import org.mz.csaude.dbsynfeatures.core.manager.utils.ApplicationProfile;
import org.mz.csaude.dbsynfeatures.core.manager.utils.SSHCommandExecutor;
import org.mz.csaude.dbsynfeatures.updates.manager.service.ApplicationUpdateLogService;
import org.mz.csaude.dbsynfeatures.updates.manager.service.ConsumeUpdateMessageProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
@Profile(ApplicationProfile.REMOTE)
public class ConsumeRemoteUpdatesProcessorRouter extends RouteBuilder {
	
	@Value("${artemis.dbsync.updates.endpoint}")
	private String notificationsEndpoint;

	@Autowired
	private SSHCommandExecutor sshCommandExecutor;

	@Autowired
	private ApplicationUpdateLogService applicationUpdateLogService;

	@Override
	public void configure() {
		String srcUri = notificationsEndpoint;
		String dstUri = "log:mylog";

		from(srcUri)
				.process(new ConsumeUpdateMessageProcessor(sshCommandExecutor, applicationUpdateLogService))
				.to(dstUri)
					.onCompletion()
					.onCompleteOnly()
					.process( exchange -> {
						//CustomMessageListenerContainer.enableAcknowledgement();
						Logger.getAnonymousLogger().info("Site has been updated successfully");
				})
				.end();
	}
}

