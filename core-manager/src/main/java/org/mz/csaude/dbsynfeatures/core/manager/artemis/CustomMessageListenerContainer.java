package org.mz.csaude.dbsynfeatures.core.manager.artemis;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.apache.camel.component.jms.DefaultJmsMessageListenerContainer;
import org.apache.camel.component.jms.JmsEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom {@link org.springframework.jms.listener.MessageListenerContainer} that only acknowledges
 * messages to be removed from active MQ depending on our application logic to ensure no message
 * ever goes unprocessed. Message acknowledgement is enabled only when the following happen,
 * 
 * <pre>
 * 1. A email notification is successfully sent to the recipient
 * </pre>
 */
public class CustomMessageListenerContainer extends DefaultJmsMessageListenerContainer {
	
	protected static final Logger log = LoggerFactory.getLogger(CustomMessageListenerContainer.class);
	
	private static boolean commit = false;
	
	public CustomMessageListenerContainer(JmsEndpoint endpoint) {
		super(endpoint);
	}
	
	public synchronized static void enableAcknowledgement() {
		commit = true;
	}
	
	@Override
	protected void messageReceived(Object invoker, Session session) {
		commit = false;
		if (log.isDebugEnabled()) {
			log.debug("Sync message received, disabled framework message acknowledgement");
		}
		
		super.messageReceived(invoker, session);
	}
	
	@Override
	protected void commitIfNecessary(Session session, Message message) throws JMSException {
		if (message != null && !commit) {
			log.warn("Skipping message acknowledgement possibly due to an encountered error");
			return;
		}
		
		super.commitIfNecessary(session, message);
	}
	
}
