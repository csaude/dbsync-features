package org.mz.csaude.dbsyncfeatures.updates.manager.service;

import org.apache.commons.lang3.StringUtils;
import org.mz.csaude.dbsyncfeatures.updates.manager.model.ApplicationUpdateLog;
import org.mz.csaude.dbsyncfeatures.updates.manager.repository.ApplicationUpdateLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class ApplicationUpdateLogService {

    @Autowired
    private ApplicationUpdateLogRepository applicationUpdateLogRepository;

    public void createEntity(ApplicationUpdateLog applicationUpdateLog) {
        if (!StringUtils.isEmpty(applicationUpdateLog.getCurrentVersion())) {
            try {
                applicationUpdateLogRepository.save(applicationUpdateLog);
            } catch (DataIntegrityViolationException ex) {
                Logger.getAnonymousLogger().info("Application update already created");
                }
            }
    }

    public ApplicationUpdateLog findByCurrentVersion(String fileName) {
        if (!StringUtils.isEmpty(fileName)) {
            ApplicationUpdateLog ApplicationUpdateLog = applicationUpdateLogRepository.findByCurrentVersion(fileName);
        }
        return null;
    }
}
