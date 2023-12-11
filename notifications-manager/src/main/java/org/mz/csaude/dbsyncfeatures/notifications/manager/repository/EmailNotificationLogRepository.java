package org.mz.csaude.dbsyncfeatures.notifications.manager.repository;

import org.mz.csaude.dbsyncfeatures.notifications.manager.model.EmailNotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface EmailNotificationLogRepository extends JpaRepository<EmailNotificationLog, Integer> {
    EmailNotificationLog findByMessageUuid(String messageUuid);
}
