package org.mz.csaude.dbsynctools.notifications.manager.consumer;

import org.apache.camel.builder.RouteBuilder;
import org.mz.csaude.dbsynctools.notifications.manager.service.EmailNotificationLogService;
import org.mz.csaude.dbsynctools.notifications.manager.utils.ApplicationProfile;
import org.mz.csaude.dbsynctools.notifications.manager.utils.CommonConverter;
import org.mz.csaude.dbsynctools.notifications.manager.utils.MailConfig;
import org.mz.csaude.dbsynctools.notifications.manager.utils.NotificationMessageProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.mz.csaude.dbsynctools.notifications.manager.utils.CustomMessageListenerContainer;

@Component
@Profile(ApplicationProfile.CONSUMER)
public class NotificationsProcessorRouter extends RouteBuilder {
	
	@Value("${artemis.dbsync.notifications.endpoint}")
	private String notificationsEndpoint;

	@Value("${smtp.host.name}")
	private String host;

	@Value("${smtp.host.port}")
	private int port;

	@Value("${smtp.auth.user}")
	private String username;

	@Value("${smtp.auth.pass}")
	private String password;

	@Autowired
	private EmailNotificationLogService emailNotificationLogService;

	@Autowired
	private CommonConverter commonConverter;

	@Override
	public void configure() {
		String srcUri = notificationsEndpoint;
		String dstUri = "log:mylog";

		MailConfig mailConfig = new MailConfig(host, port, username, password);
		from(srcUri)
				.process(new NotificationMessageProcessor(mailConfig, emailNotificationLogService, commonConverter ))
				.to(dstUri)
					.onCompletion()
					.onCompleteOnly()
					.process( exchange -> {
						CustomMessageListenerContainer.enableAcknowledgement();
				})
				.end();
	}
}

