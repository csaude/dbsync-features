package org.mz.csaude.dbsyncfeatures.updates.manager.service;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.mz.csaude.dbsyncfeatures.core.manager.utils.ApplicationProfile;
import org.mz.csaude.dbsyncfeatures.core.manager.utils.Utils;
import org.mz.csaude.dbsyncfeatures.updates.manager.model.UpdateReport;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile(ApplicationProfile.CENTRAL)
public class LogUpdatedRemoteSiteProcessor implements Processor {

    private final UpdateReportService updateReportService;

    public LogUpdatedRemoteSiteProcessor(UpdateReportService updateReportService) {
        this.updateReportService = updateReportService;
    }

    @Override
    public void process(Exchange exchange) throws Exception {

        String messageBody = exchange.getIn().getBody(String.class);
        UpdateReport updateReport = Utils.fromJson(messageBody, UpdateReport.class);
        if (updateReport != null){
            this.updateReportService.createEntity(updateReport);
        }
    }
}
