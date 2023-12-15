package org.mz.csaude.dbsyncfeatures.notifications.manager.utils;

import java.time.LocalDateTime;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.mz.csaude.dbsyncfeatures.core.manager.utils.CommonConverter;
import org.mz.csaude.dbsyncfeatures.notifications.manager.central.NotificationsProcessorRouter;
import org.mz.csaude.dbsyncfeatures.notifications.manager.model.EmailNotificationLog;
import org.mz.csaude.dbsyncfeatures.notifications.manager.model.NotificationInfo;
import org.mz.csaude.dbsyncfeatures.notifications.manager.service.EmailNotificationLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationMessageProcessor implements Processor {
	
	private MailConfig mailConfig;
	
	private EmailNotificationLogService emailNotificationLogService;
	
	protected static final Logger log = LoggerFactory.getLogger(NotificationsProcessorRouter.class);
	
	public NotificationMessageProcessor(MailConfig mailConfig, EmailNotificationLogService emailNotificationLogService) {
		this.mailConfig = mailConfig;
		this.emailNotificationLogService = emailNotificationLogService;
	}
	
	@Override
	public void process(Exchange exchange) {
		
		log.info("Processing message from artemis");
		
		try {
			String messageBody = exchange.getIn().getBody(String.class);
			
			NotificationInfo notificationInfo = CommonConverter.fromJson(messageBody, NotificationInfo.class);
			exchange.getIn().setBody(notificationInfo);
			NotificationService notificationService = new NotificationService(this.mailConfig);
			
			EmailNotificationLog emailNotificationLog = new EmailNotificationLog();
			emailNotificationLog.setMessageType(determineNotificationType(notificationInfo.getMailSubject()));
			emailNotificationLog.setDateSent(LocalDateTime.now());
			emailNotificationLog.setSubject(notificationInfo.getMailSubject());
			emailNotificationLog.setSiteId(notificationInfo.getMailSiteOrigin());
			emailNotificationLog.setMessageUuid(notificationInfo.getMessageUuid());
			emailNotificationLogService.createEntity(emailNotificationLog);
			
			notificationService.emailService(notificationInfo);
			log.info("Notification Message for site: " + notificationInfo.getMailSiteOrigin() + "for type "
			        + notificationInfo.getMailSubject() + " were delivered successfully to user");
		}
		catch (Exception e) {
			log.error("An error occurred trying to process message: " + e.getMessage());
			log.error(e.getCause().toString());
		}
		
	}
	
	NotificationType determineNotificationType(String notificationSubject) {
		if (notificationSubject.startsWith("EIP REMOTO - SETUP INFO")) {
			return NotificationType.DBSYNC_INITIAL_SETUP;
		}
		
		if (notificationSubject.startsWith("EIP REMOTO - ESTADO DE ACTUALIZACAO")) {
			return NotificationType.UPDATE_TRY;
		}
		if (notificationSubject.startsWith("EIP REMOTO - SETUP INFO")) {
			return NotificationType.DBSYNC_INITIAL_SETUP;
		}
		if (notificationSubject.startsWith("EIP REMOTO - LIQUIBASE LOCK INFO")) {
			return NotificationType.LIQUIBASE_UNLOCK;
		}
		if (notificationSubject.startsWith("DB sync application at")) {
			return NotificationType.DBSYNC_SHUTDOWN;
		}
		if (notificationSubject.startsWith("EIP REMOTO - ESTADO DE HARMONIZACAO DE LOCAIS")) {
			return NotificationType.LOCATION_HARMONIZATION_STARTED;
		}
		if (notificationSubject.startsWith("EIP REMOTO - RELATORIO DA HARMONIZACAO DE LOCAIS")) {
			return NotificationType.LOCATION_HARMONIZATION_STARTED;
		}
		
		throw new RuntimeException("Unkown Message Type");
		
	}
}
