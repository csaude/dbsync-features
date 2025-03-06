package org.mz.csaude.dbsyncfeatures.notifications.manager.utils;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.mz.csaude.dbsyncfeatures.core.manager.utils.ApplicationProfile;
import org.mz.csaude.dbsyncfeatures.core.manager.utils.Utils;
import org.mz.csaude.dbsyncfeatures.notifications.manager.model.EmailNotificationLog;
import org.mz.csaude.dbsyncfeatures.notifications.manager.model.NotificationInfo;
import org.mz.csaude.dbsyncfeatures.notifications.manager.service.EmailNotificationLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@Profile(ApplicationProfile.CENTRAL)
public class NotificationMessageProcessor implements Processor {
	

	private EmailNotificationLogService emailNotificationLogService;
	
	protected static final Logger log = LoggerFactory.getLogger(NotificationMessageProcessor.class);
	
	public NotificationMessageProcessor(EmailNotificationLogService emailNotificationLogService) {
		this.emailNotificationLogService = emailNotificationLogService;
	}
	
	@Override
	public void process(Exchange exchange) {
		
		log.info("Processing message from artemis");
		
		try {
			String messageBody = exchange.getIn().getBody(String.class);
			
			NotificationInfo notificationInfo = Utils.fromJson(messageBody, NotificationInfo.class);
			exchange.getIn().setBody(notificationInfo);

			EmailNotificationLog emailNotificationLog = new EmailNotificationLog();
			emailNotificationLog.setMessageType(determineNotificationType(notificationInfo.getMailSubject()));
			LocalDateTime dateSent = notificationInfo.getDateSent() != null
					? notificationInfo.getDateSent().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime():
					LocalDateTime.now();

			emailNotificationLog.setDateSent( dateSent);
			emailNotificationLog.setSubject(notificationInfo.getMailSubject());
			emailNotificationLog.setSiteId(notificationInfo.getMailSiteOrigin());
			emailNotificationLog.setMessageUuid(notificationInfo.getMessageUuid());
			emailNotificationLog.setMailRecipients(notificationInfo.getMailRecipients());
			emailNotificationLog.setMailContent(notificationInfo.getMailContent());
			emailNotificationLog.setMailAttachment(new String(notificationInfo.getMailAttachment(), StandardCharsets.UTF_8));
			emailNotificationLog.setAttachmentName(notificationInfo.getAttachmentName());
			emailNotificationLog.setMailSiteOrigin(notificationInfo.getMailSiteOrigin());
			emailNotificationLogService.createEntity(emailNotificationLog);

            log.info("Notification Message for site: {}for type {} were saved successfully!",
					notificationInfo.getMailSiteOrigin(), notificationInfo.getMailSubject());
		}
		catch (Exception e) {
            log.error("An error occurred trying to process message: {}", e.getMessage());
		}
	}
	
	NotificationType determineNotificationType(String notificationSubject) {
		if (notificationSubject.startsWith("EIP REMOTO - INITIAL SETUP INFO")) {
			return NotificationType.DBSYNC_INITIAL_SETUP;
		}
		
		if (notificationSubject.startsWith("EIP REMOTO - ESTADO DE ACTUALIZACAO")) {
			return NotificationType.UPDATE_TRY;
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
			return NotificationType.LOCATION_HARMONIZATION_FINISHED;
		}
		return NotificationType.UNKNOWN_NOTIFICATION;
	}
}
