package org.mz.csaude.dbsynctools.notificationsmanager;

import java.io.IOException;

import javax.mail.MessagingException;
import org.junit.jupiter.api.Test;
import org.mz.csaude.dbsynctools.notifications.manager.model.NotificationInfo;
import org.mz.csaude.dbsynctools.notifications.manager.utils.CommonConverter;
import org.mz.csaude.dbsynctools.notifications.manager.utils.MailConfig;
import org.mz.csaude.dbsynctools.notifications.manager.utils.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@SpringBootTest
class NotificationServiceTests {

	@Test
	public void emailService_shouldSendEmailToAdministrators() throws IOException, MessagingException {
//		String filePath = "openmrs-eip.log";
//		MailConfig mailConfig = new MailConfig();
//		NotificationService notificationService = new NotificationService(mailConfig);
//		CommonConverter converter = new CommonConverter(null);
//
//		NotificationInfo notificationInfo = new NotificationInfo();
//		notificationInfo.setMailSubject("Implementacao de testes");
//		notificationInfo.setMailContent("Este e' o primeiro teste de envio de email usando spring boot");
//		notificationInfo.setMailRecipients("jorge.boane@fgh.org.mz,prosperino.mbalame@fgh.org.mz,daniel.chirinda@fgh.org.mz,jose.chambule@fgh.org.mz,muhamad.ynusse@fgh.org.mz,marques.maraquire@fgh.org.mz");
//		notificationInfo.setAttachmentName("Teste-unitario");
//		notificationInfo.setMailAttachment(converter.convertToByteAyyay(filePath));
//		notificationInfo.setMailSiteOrigin("Prod_teste");
//
//		notificationService.emailService(notificationInfo);
	}

}
