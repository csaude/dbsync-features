package org.mz.csaude.dbsyncfeatures.notifications.manager.service;

import org.apache.commons.lang3.StringUtils;
import org.mz.csaude.dbsyncfeatures.notifications.manager.model.EmailNotificationLog;
import org.mz.csaude.dbsyncfeatures.notifications.manager.repository.EmailNotificationLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationLogService {
    @Autowired
    private EmailNotificationLogRepository emailNotificationLogRepository ;

    public void createEntity(EmailNotificationLog emailNotificationLog) {

        if(StringUtils.isNotEmpty(emailNotificationLog.getMessageUuid())){
            EmailNotificationLog existingEmailNotificationLog = emailNotificationLogRepository.findByMessageUuid(emailNotificationLog.getMessageUuid());

            if (existingEmailNotificationLog == null){
                emailNotificationLogRepository.save(emailNotificationLog);
            }
        }
    }
}
