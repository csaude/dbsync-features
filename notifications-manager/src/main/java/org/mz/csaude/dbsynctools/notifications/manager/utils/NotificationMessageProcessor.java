package org.mz.csaude.dbsynctools.notifications.manager.utils;

import java.time.LocalDateTime;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.mz.csaude.dbsynctools.notifications.manager.consumer.NotificationsProcessorRouter;
import org.mz.csaude.dbsynctools.notifications.manager.model.EmailNotificationLog;
import org.mz.csaude.dbsynctools.notifications.manager.model.NotificationInfo;
import org.mz.csaude.dbsynctools.notifications.manager.repository.EmailNotificationLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationMessageProcessor implements Processor {

    private MailConfig mailConfig;

    private CommonConverter commonConverter;
    private EmailNotificationLogRepository emailNotificationLogRepository;

    protected static final Logger log = LoggerFactory.getLogger(NotificationsProcessorRouter.class);

    public NotificationMessageProcessor(MailConfig mailConfig, EmailNotificationLogRepository emailNotificationLogRepository,
                                        CommonConverter commonConverter) {
        this.mailConfig=mailConfig;
        this.emailNotificationLogRepository = emailNotificationLogRepository;
        this.commonConverter = commonConverter;
    }

    @Override
    public void process(Exchange exchange) {

        log.info("Processing message from artemis");

        try {
            String messageBody = exchange.getIn().getBody(String.class);

            NotificationInfo notificationInfo = commonConverter.fromJson(messageBody, NotificationInfo.class);
            exchange.getIn().setBody(notificationInfo);
            NotificationService notificationService = new NotificationService(this.mailConfig);
            notificationService.emailService(notificationInfo);
            log.info("Notification Message for site: " + notificationInfo.getMailSiteOrigin() + "for type " + notificationInfo.getMailSubject() + " were delivered successfully to user" );
            CustomMessageListenerContainer.enableAcknowledgement();

            // Save the log in database
            EmailNotificationLog emailNotificationLog = new EmailNotificationLog();
            emailNotificationLog.setMessageType(notificationInfo.getMailSubject());
            emailNotificationLog.setDateSent(LocalDateTime.now());
            emailNotificationLog.setSubject(notificationInfo.getMailSubject());
            emailNotificationLog.setSiteId(notificationInfo.getMailSiteOrigin());

            emailNotificationLogRepository.save(emailNotificationLog);
        } catch (Exception e){
            log.error("An error occurred trying to process message: " + e.getMessage());
            log.error(e.getCause().toString());
        }

    }

}
