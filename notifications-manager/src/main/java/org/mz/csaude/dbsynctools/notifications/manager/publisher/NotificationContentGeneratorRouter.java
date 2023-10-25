package org.mz.csaude.dbsynctools.notifications.manager.publisher;

import java.io.IOException;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.mz.csaude.dbsynctools.notifications.manager.model.NotificationInfo;
import org.mz.csaude.dbsynctools.notifications.manager.model.NotificationInfoSrc;
import org.mz.csaude.dbsynctools.notifications.manager.utils.ApplicationProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile(ApplicationProfile.PUBLISHER)
public class NotificationContentGeneratorRouter extends RouteBuilder {
	
	@Autowired
	private NotificationContentGenerator contentGenerator;
	
	@Value("${notification.content.root.folder}")
	private String notificationContentRootFolder;
	
	@Value("${artemis.dbsync.notifications.endpoint}")
	private String notificationsEndpoint;
	
	@Autowired
	PerfomeActionsAfterContentSent perfomeActionsAfterContentSent;
	
	@Override
	public void configure() throws Exception {
		String srcUri = "file:" + notificationContentRootFolder + "?includeExt=json";
		String dstUri = notificationsEndpoint;
		
		from(srcUri).unmarshal()
		.json(JsonLibrary.Jackson, NotificationInfoSrc.class)
		.choice()
			.when(simple("${body.allNotificationContentExists}"))
				.bean(contentGenerator)
				.marshal()
				.json(JsonLibrary.Jackson, NotificationInfo.class)
				.to(dstUri)
				.unmarshal()
				.json(JsonLibrary.Jackson, NotificationInfo.class)
				.process(perfomeActionsAfterContentSent)
			.otherwise()
				.log("One or mor notification content files does not exists")
		.endChoice();
	}
}

@Component
class PerfomeActionsAfterContentSent implements Processor {
	
	@Override
	public void process(Exchange exchange) throws Exception {
		NotificationInfo notificationInfo = (NotificationInfo) exchange.getMessage().getBody();
		
		notificationInfo.clearContentSrc();
	}
	
}

@Component
class NotificationContentGenerator {
	
	public NotificationInfo generate(NotificationInfoSrc body) throws IOException {
		return body.loadContent();
	}
	
}
