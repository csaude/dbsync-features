package org.mz.csaude.dbsyncfeatures.notifications.manager.central;

import org.apache.camel.builder.RouteBuilder;
import org.mz.csaude.dbsyncfeatures.core.manager.artemis.CustomMessageListenerContainer;
import org.mz.csaude.dbsyncfeatures.core.manager.utils.ApplicationProfile;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Forwards messages from topic:openmrs.sync.topic to queue openmrs.sync.topic
 */
@Component
@Profile(ApplicationProfile.CENTRAL)
public class DbsyncMessagesForwarderRoute extends RouteBuilder {
	
	@Override
	public void configure() {
		String srcUri = "activemq:topic:openmrs.sync.topic?subscriptionDurable=true&durableSubscriptionName=DB-SYNC-RECEIVER&connectionFactory=activeMqConnFactory&acknowledgementModeName=CLIENT_ACKNOWLEDGE&messageListenerContainerFactory=customMessageListenerContainerFactory";
		String dstUri = "activemq:queue:openmrs.sync.topic";
		
		//@formatter:off
		from(srcUri)
		.routeId("dbsync-msg-forwarder")
		.log("Receiving message")
		.to(dstUri);
	}
}
