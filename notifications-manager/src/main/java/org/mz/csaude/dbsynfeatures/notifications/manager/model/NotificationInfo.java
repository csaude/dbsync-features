package org.mz.csaude.dbsynfeatures.notifications.manager.model;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class NotificationInfo {
	
	private String mailSubject;
	
	private String mailRecipients;
	
	private String mailContent;
	
	private String attachmentName;
	
	private byte[] mailAttachment;
	
	private String mailSiteOrigin;
	
	private String srcFolderPath;

	private String messageUuid;
	
	public NotificationInfo() {
	}
	
	public String getMailSiteOrigin() {
		return mailSiteOrigin;
	}
	
	public void setMailSiteOrigin(String mailSiteOrigin) {
		this.mailSiteOrigin = mailSiteOrigin;
	}


	public String getMailSubject() {
		return mailSubject;
	}
	
	public void setMailSubject(String mailSubject) {
		this.mailSubject = mailSubject;
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
	
	public byte[] getMailAttachment() {
		return mailAttachment;
	}
	
	public void setMailAttachment(byte[] mailAttachment) {
		this.mailAttachment = mailAttachment;
	}
	
	public String getSrcFolderPath() {
		return srcFolderPath;
	}
	
	public void setSrcFolderPath(String srcFolderPath) {
		this.srcFolderPath = srcFolderPath;
	}

	public void setMessageUuid(String messageUuid) {
		this.messageUuid = messageUuid;
	}

	public String getMessageUuid() {
		return messageUuid;
	}

	@Override
	public String toString() {
		return "Mail: [subject:" + mailSubject + ", to: " + mailRecipients + "]";
	}
	
	public void clearContentSrc() throws IOException {
		File srcDir = new File(this.srcFolderPath);
		
		if (srcDir.exists()) {
			FileUtils.deleteDirectory(srcDir);
		}
	}
}
