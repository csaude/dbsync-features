package org.mz.csaude.dbsyncfeatures.notifications.manager.central;

import org.apache.camel.builder.RouteBuilder;
import org.mz.csaude.dbsyncfeatures.core.manager.artemis.CustomMessageListenerContainer;
import org.mz.csaude.dbsyncfeatures.core.manager.utils.ApplicationProfile;
import org.mz.csaude.dbsyncfeatures.notifications.manager.service.EmailNotificationLogService;
import org.mz.csaude.dbsyncfeatures.notifications.manager.utils.MailConfig;
import org.mz.csaude.dbsyncfeatures.notifications.manager.utils.NotificationMessageProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile(ApplicationProfile.CENTRAL)
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
	
	@Override
	public void configure() {
		String srcUri = notificationsEndpoint;
		String dstUri = "log:mylog";
		
		MailConfig mailConfig = new MailConfig(host, port, username, password);
		from(srcUri)
				.routeId("send-email-notification")
				.process(new NotificationMessageProcessor(mailConfig, emailNotificationLogService)).to(dstUri)
		        .onCompletion().onCompleteOnly().process(exchange -> {
					boolean emailSend = (boolean) exchange.getProperty("emailSent");
					if (emailSend){
						CustomMessageListenerContainer.enableAcknowledgement();
					}
		        }).end();
	}
}
