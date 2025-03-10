package org.mz.csaude.dbsyncfeatures.updates.manager.service;

import org.apache.commons.lang3.StringUtils;
import org.mz.csaude.dbsyncfeatures.updates.manager.model.UpdateReport;
import org.mz.csaude.dbsyncfeatures.updates.manager.repository.UpdateReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class UpdateReportService {

    @Autowired
    private UpdateReportRepository updateReportRepository;

    public void createEntity(UpdateReport updateReport) {
        if (!StringUtils.isEmpty(updateReport.getVersion())) {
            try {
                updateReportRepository.save(updateReport);
            } catch (DataIntegrityViolationException ex) {
                Logger.getAnonymousLogger().info("Application update already created");
                }
            }
    }
}
