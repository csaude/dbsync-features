package org.mz.csaude.dbsyncfeatures.updates.manager.service;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.mz.csaude.dbsyncfeatures.core.manager.utils.ApplicationProfile;
import org.mz.csaude.dbsyncfeatures.core.manager.utils.CommonConverter;
import org.mz.csaude.dbsyncfeatures.updates.manager.model.UpdatedSite;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile(ApplicationProfile.CENTRAL)
public class LogUpdatedRemoteSiteProcessor implements Processor {

    private UpdatedSiteService updatedSiteService;

    public LogUpdatedRemoteSiteProcessor(UpdatedSiteService updatedSiteService) {
        this.updatedSiteService = updatedSiteService;
    }

    @Override
    public void process(Exchange exchange) throws Exception {

        String messageBody = exchange.getIn().getBody(String.class);
        UpdatedSite updatedSite = CommonConverter.fromJson(messageBody, UpdatedSite.class);
        if (updatedSite != null){
            this.updatedSiteService.createEntity(updatedSite);
        }
    }
}
