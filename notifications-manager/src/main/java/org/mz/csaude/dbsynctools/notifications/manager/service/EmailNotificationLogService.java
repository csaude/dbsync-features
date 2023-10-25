package org.mz.csaude.dbsynctools.notifications.manager.service;

import org.mz.csaude.dbsynctools.notifications.manager.model.EmailNotificationLog;
import org.mz.csaude.dbsynctools.notifications.manager.repository.EmailNotificationLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationLogService {
    @Autowired
    private  EmailNotificationLogRepository emailNotificationLogRepository ;

    public void createEntity(EmailNotificationLog emailNotificationLog) {
        emailNotificationLogRepository.save(emailNotificationLog);
    }
}
