package org.mz.csaude.dbsyncfeatures.notifications.manager.model;

import com.sun.istack.NotNull;
import org.mz.csaude.dbsyncfeatures.core.manager.entity.Base;
import org.mz.csaude.dbsyncfeatures.core.manager.utils.ApplicationProfile;
import org.mz.csaude.dbsyncfeatures.notifications.manager.central.utils.NotificationType;
import org.springframework.context.annotation.Profile;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_notification_log")
@Profile(ApplicationProfile.CENTRAL)
public class EmailNotificationLog extends Base {
	
	private static final long serialVersionUID = -6559761397983733774L;
	
	@Column(name = "message_uuid")
	private String messageUuid;
	
	@NotNull
	@Column(name = "site_id")
	private String siteId;
	
	@NotNull
	@Column(name = "message_type")
	@Enumerated(EnumType.STRING)
	private NotificationType messageType;
	
	@NotNull
	@Column(name = "subject", length = 255)
	private String subject;
	
	@NotNull
	@Column(name = "date_sent")
	private LocalDateTime dateSent;

	@Column(name="mail_recipients")
	private String mailRecipients;

	@Column(name="mail_content")
	private String mailContent;

	@Column(name="attachement_name")
	private String attachmentName;

	@Column(name="mail_attachment")
	private String mailAttachment;

	@Column(name="mail_site_origin")
	private String mailSiteOrigin;

	@Column(name="email_sent")
	private boolean emailSent;
	
	public String getSiteId() {
		return siteId;
	}
	
	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}
	
	public NotificationType getMessageType() {
		return messageType;
	}
	
	public void setMessageType(NotificationType messageType) {
		this.messageType = messageType;
	}
	
	public String getSubject() {
		return subject;
	}
	
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public LocalDateTime getDateSent() {
		return dateSent;
	}
	
	public void setDateSent(LocalDateTime dateSent) {
		this.dateSent = dateSent;
	}
	
	public void setMessageUuid(String messageUuid) {
		this.messageUuid = messageUuid;
	}
	
	public String getMessageUuid() {
		return messageUuid;
	}

	public String getMailRecipients() {
		return mailRecipients;
	}

	public void setMailRecipients(String mailRecipients) {
		this.mailRecipients = mailRecipients;
	}

	public String getMailContent() {
		return mailContent;
	}

	public void setMailContent(String mailContent) {
		this.mailContent = mailContent;
	}

	public String getAttachmentName() {
		return attachmentName;
	}

	public void setAttachmentName(String attachmentName) {
		this.attachmentName = attachmentName;
	}

	public String getMailAttachment() {
		return mailAttachment;
	}

	public void setMailAttachment(String mailAttachment) {
		this.mailAttachment = mailAttachment;
	}

	public String getMailSiteOrigin() {
		return mailSiteOrigin;
	}

	public void setMailSiteOrigin(String mailSiteOrigin) {
		this.mailSiteOrigin = mailSiteOrigin;
	}

	public boolean isEmailSent() {
		return emailSent;
	}

	public void setEmailSent(boolean emailSent) {
		this.emailSent = emailSent;
	}
}
