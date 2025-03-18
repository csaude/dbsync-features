package org.mz.csaude.dbsyncfeatures.notifications.manager.central;

import org.apache.camel.builder.RouteBuilder;
import org.mz.csaude.dbsyncfeatures.core.manager.artemis.CustomMessageListenerContainer;
import org.mz.csaude.dbsyncfeatures.core.manager.utils.ApplicationProfile;
import org.mz.csaude.dbsyncfeatures.notifications.manager.service.EmailNotificationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile(ApplicationProfile.CENTRAL)
public class NotificationsProcessorRouter extends RouteBuilder {
	
	@Value("${artemis.dbsync.notifications.endpoint}")
	private String notificationsEndpoint;
	
	@Autowired
	private EmailNotificationLogService emailNotificationLogService;
	
	@Override
	public void configure() {
		String srcUri = notificationsEndpoint;
		String dstUri = "log:mylog";
		
		from(srcUri)
				.routeId("send-email-notification")
				.process(new NotificationMessageProcessor(emailNotificationLogService)).to(dstUri)
		        .onCompletion().onCompleteOnly().process(exchange -> {
						CustomMessageListenerContainer.enableAcknowledgement();
		        }).end();
	}
}