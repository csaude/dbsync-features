package org.mz.csaude.dbsynfeatures.notifications.manager.model;

import java.time.LocalDateTime;

import com.sun.istack.NotNull;
import org.mz.csaude.dbsynfeatures.core.manager.entity.Base;
import org.mz.csaude.dbsynfeatures.core.manager.utils.ApplicationProfile;
import org.springframework.context.annotation.Profile;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "email_notification_log")
@Profile(ApplicationProfile.CONSUMER)
public class EmailNotificationLog  extends Base {

    @Column(name = "message_uuid")
    private String messageUuid;

    @NotNull
    @Column(name = "site_id")
    private String siteId;

    @NotNull
    @Column(name = "message_type")
    private String messageType;

    @NotNull
    @Column(name = "subject", length = 255)
    private String subject;

    @NotNull
    @Column(name = "date_sent")
    private LocalDateTime dateSent;

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
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
}