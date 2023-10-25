package org.mz.csaude.dbsynctools.notifications.manager.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * The Source of notification
 */
public class NotificationInfoSrc {
	
	private String notificationContentPath;
	
	private File mailSubjectFile;
	
	private File mailRecipientsFile;
	
	private File mailContentFile;
	
	private File mailAttachmentFile;
	
	private File mailSiteOriginFile;
	
	private boolean contentFilesInitialized;
	
	private final String stringLock = new String("LOCK_STRING");
	
	public NotificationInfoSrc() {
	}
	
	private void initContentFiles() {
		if (isContentFilesInitialized())
			return;
		
		synchronized (stringLock) {
			this.mailSubjectFile = new File(notificationContentPath + File.separator + "MAIL_SUBJECT");
			this.mailRecipientsFile = new File(notificationContentPath + File.separator + "MAIL_RECIPIENTS");
			this.mailContentFile = new File(notificationContentPath + File.separator + "MAIL_CONTENT_FILE");
			this.mailAttachmentFile = new File(notificationContentPath + File.separator + "MAIL_ATTACHMENT");
			this.mailSiteOriginFile = new File(notificationContentPath + File.separator + "MAIL_SITE_ORIGIN");
			
			this.contentFilesInitialized = true;
		}
	}
	
	private boolean isContentFilesInitialized() {
		return contentFilesInitialized;
	}
	
	public String getNotificationContentPath() {
		return notificationContentPath;
	}
	
	public void setNotificationContentPath(String notificationContentPath) {
		this.notificationContentPath = notificationContentPath;
	}
	
	public boolean isNotificationContentFolderExists() {
		return new File(notificationContentPath).exists();
	}
	
	public boolean isAllNotificationContentExists() {
		if (!isNotificationContentFolderExists()) {
			return false;
		}
		
		initContentFiles();
		
		if (!this.mailAttachmentFile.exists()) return false;
		if (!this.mailContentFile.exists()) return false;
		if (!this.mailRecipientsFile.exists()) return false;
		if (!this.mailSubjectFile.exists()) return false;
		if (!this.mailSiteOriginFile.exists()) return false;
		
		return true;
	}
	
	public NotificationInfo loadContent() throws IOException {
		initContentFiles();
		
		NotificationInfo notificationInfo = new NotificationInfo();
		
		String mailSubject = new String(Files.readAllBytes(mailSubjectFile.toPath()), StandardCharsets.UTF_8);
		String mailRecipients = new String(Files.readAllBytes(mailRecipientsFile.toPath()), StandardCharsets.UTF_8);
		String mailContent = new String(Files.readAllBytes(mailContentFile.toPath()), StandardCharsets.UTF_8);
		String mailSiteOrigin = new String(Files.readAllBytes(mailSiteOriginFile.toPath()), StandardCharsets.UTF_8);
			
		String attachmentName = mailAttachmentFile.getName();
		
		byte[] mailAttachment = new byte[(int) mailAttachmentFile.length()];
		
		FileInputStream attStream = new FileInputStream(mailAttachmentFile);
		
		attStream.read(mailAttachment);
		
		notificationInfo.setMailSubject(mailSubject);
		
		attStream.close();
		
		notificationInfo.setAttachmentName(attachmentName);
		notificationInfo.setMailAttachment(mailAttachment);
		notificationInfo.setMailContent(mailContent);
		notificationInfo.setMailRecipients(mailRecipients);
		notificationInfo.setMailSubject(mailSubject);
		notificationInfo.setMailSiteOrigin(mailSiteOrigin);
		notificationInfo.setSrcFolderPath(this.getNotificationContentPath());
		
		return notificationInfo;
	}
}
