package org.mz.csaude.dbsyncfeatures.notifications.manager.central;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.component.jms.JmsConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.mz.csaude.dbsyncfeatures.core.manager.utils.ApplicationProfile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.stereotype.Component;

/**
 * Forwards messages from topic:openmrs.sync.topic to queue openmrs.sync.topic
 */
@Component
@Profile(ApplicationProfile.CENTRAL)
public class DbsyncMessagesForwarderRoute extends RouteBuilder {
	
	@Value("${max.reconnect.delay:1800000}")
	private int maxReconnectDelay;
	
	@Value("${spring.artemis.host}")
	private String artemisHost;
	
	@Value("${spring.artemis.port}")
	private int artemisPort;
	
	@Value("${spring.artemis.user}")
	private String artemisUser;
	
	@Value("${spring.artemis.password}")
	private String artemisPassword;
	
	private static final long REDELIVERY_DELAY = 300000;
	
	@Override
	public void configure() {
		String srcUri = "activemq:topic:openmrs.sync.topic?subscriptionDurable=true&durableSubscriptionName=DB-SYNC-RECEIVER";
		String dstUri = "activemq:queue:openmrs.sync.topic";
		
        // Use the specific JMS configuration
        getContext().addComponent("activemq", JmsComponent.jmsComponentAutoAcknowledge(getConnectionFactory()));
    
		
		//@formatter:off
		from(srcUri)
		.routeId("dbsync-msg-forwarder")
		.log("Receiving message");
	}
	
	private ConnectionFactory getConnectionFactory() {
		ActiveMQConnectionFactory cf;
		
		cf = new org.apache.activemq.spring.ActiveMQConnectionFactory();
		
		String url = ("tcp") + "://" + artemisHost + ":" + artemisPort;
		String failoverUrl = "failover:(" + url
		        + ")?initialReconnectDelay=60000&reconnectDelayExponent=5&maxReconnectDelay=" + maxReconnectDelay
		        + "&maxReconnectAttempts=-1&warnAfterReconnectAttempts=2";
		cf.setBrokerURL(failoverUrl);
		cf.setUserName(artemisUser);
		cf.setPassword(artemisPassword);
		cf.setClientID("DB-SYNC-REC.DB-SYNC-RECEIVER");
		
		RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
		redeliveryPolicy.setMaximumRedeliveries(RedeliveryPolicy.NO_MAXIMUM_REDELIVERIES);
		redeliveryPolicy.setInitialRedeliveryDelay(REDELIVERY_DELAY);
		redeliveryPolicy.setRedeliveryDelay(REDELIVERY_DELAY);
		cf.setRedeliveryPolicy(redeliveryPolicy);
		
		return new CachingConnectionFactory(cf);
	}
		
}
