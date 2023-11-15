package org.mz.csaude.dbsynfeatures.notifications.manager.service;

import org.mz.csaude.dbsynfeatures.notifications.manager.model.EmailNotificationLog;
import org.mz.csaude.dbsynfeatures.notifications.manager.repository.EmailNotificationLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationLogService {
    @Autowired
    private EmailNotificationLogRepository emailNotificationLogRepository ;

    public void createEntity(EmailNotificationLog emailNotificationLog) {
        if (!emailNotificationLog.getMessageUuid().isEmpty()) {
            try {
                emailNotificationLogRepository.save(emailNotificationLog);
            } catch (DataIntegrityViolationException ex) {
                EmailNotificationLog existingEmailNotificationLog = emailNotificationLogRepository.findByMessageUuid(emailNotificationLog.getMessageUuid());
                if (existingEmailNotificationLog != null) {
                    existingEmailNotificationLog.setDateSent(emailNotificationLog.getDateSent());
                    emailNotificationLogRepository.save(existingEmailNotificationLog);

                }
            }
        }
    }
}
