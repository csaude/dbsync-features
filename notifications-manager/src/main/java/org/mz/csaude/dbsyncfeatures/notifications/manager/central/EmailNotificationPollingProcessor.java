package org.mz.csaude.dbsyncfeatures.notifications.manager.central;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.mz.csaude.dbsyncfeatures.core.manager.utils.ApplicationProfile;
import org.mz.csaude.dbsyncfeatures.notifications.manager.central.utils.MailConfig;
import org.mz.csaude.dbsyncfeatures.notifications.manager.central.utils.NotificationService;
import org.mz.csaude.dbsyncfeatures.notifications.manager.model.EmailNotificationLog;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile(ApplicationProfile.CENTRAL)
public class EmailNotificationPollingProcessor implements Processor {


    private MailConfig mailConfig;

    public EmailNotificationPollingProcessor(MailConfig mailConfig) {
        this.mailConfig = mailConfig;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        EmailNotificationLog emailNotificationLog = exchange.getIn().getBody(EmailNotificationLog.class);
        NotificationService notificationService = new NotificationService(this.mailConfig);

        notificationService.sendEmail(emailNotificationLog);
        exchange.setProperty("emailSent", Boolean.TRUE);
    }
}
