package org.mz.csaude.dbsyncfeatures.updates.manager.service;

import org.apache.commons.lang3.StringUtils;
import org.mz.csaude.dbsyncfeatures.updates.manager.model.UpdatedSite;
import org.mz.csaude.dbsyncfeatures.updates.manager.repository.UpdatedSiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class UpdatedSiteService {

    @Autowired
    private UpdatedSiteRepository updatedSiteRepository;

    public void createEntity(UpdatedSite updatedSite) {
        if (!StringUtils.isEmpty(updatedSite.getVersion())) {
            try {
                updatedSiteRepository.save(updatedSite);
            } catch (DataIntegrityViolationException ex) {
                Logger.getAnonymousLogger().info("Application update already created");
                }
            }
    }
}
