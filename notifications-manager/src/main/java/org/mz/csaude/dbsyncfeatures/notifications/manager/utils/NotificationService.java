package org.mz.csaude.dbsyncfeatures.notifications.manager.utils;

import org.mz.csaude.dbsyncfeatures.core.manager.utils.ApplicationProfile;
import org.mz.csaude.dbsyncfeatures.notifications.manager.model.EmailNotificationLog;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
@Profile(ApplicationProfile.CENTRAL)
public class NotificationService {

    public MailConfig mailConfig;
    public NotificationService(MailConfig mailConfig) {
        this.mailConfig = mailConfig;
    }

    public void sendEmail(EmailNotificationLog emailNotificationLog) throws MessagingException {
        JavaMailSender javaMailSender = mailConfig.javaMailSender();
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(message, true);

        messageHelper.setTo(emailNotificationLog.getMailRecipients().split(","));
        messageHelper.setSubject(emailNotificationLog.getSubject());
        messageHelper.setText(emailNotificationLog.getMailContent());

        messageHelper.addAttachment(emailNotificationLog.getAttachmentName(), new ByteArrayResource(emailNotificationLog.getMailAttachment().getBytes()));

        javaMailSender.send(message);
    }
}
