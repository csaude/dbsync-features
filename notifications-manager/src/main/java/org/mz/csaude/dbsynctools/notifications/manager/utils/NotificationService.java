package org.mz.csaude.dbsynctools.notifications.manager.utils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.mz.csaude.dbsynctools.notifications.manager.model.NotificationInfo;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    public MailConfig mailConfig;
    public NotificationService(MailConfig mailConfig) {
        this.mailConfig = mailConfig;
    }

    public void emailService(NotificationInfo notificationInfo) throws MessagingException {
        JavaMailSender javaMailSender = mailConfig.javaMailSender();
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(message, true);

        messageHelper.setTo(notificationInfo.getMailRecipients().split(","));
        messageHelper.setSubject(notificationInfo.getMailSubject());
        messageHelper.setText(notificationInfo.getMailContent());
        messageHelper.addAttachment(notificationInfo.getAttachmentName(), new ByteArrayResource(notificationInfo.getMailAttachment()));

        javaMailSender.send(message);

    }

}
