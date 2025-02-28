package org.mz.csaude.dbsyncfeatures.updates.manager.central;

import org.apache.camel.builder.RouteBuilder;
import org.mz.csaude.dbsyncfeatures.core.manager.utils.ApplicationProfile;
import org.mz.csaude.dbsyncfeatures.updates.manager.model.UpdateQueue;
import org.mz.csaude.dbsyncfeatures.updates.manager.service.UpdateQueuePollingProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile(ApplicationProfile.CENTRAL)
public class UpdateQueuePollingRoute extends RouteBuilder {
    @Value("${share.update.root.folder}")
    private String updateRootFolder;

    @Value("${polling.interval}")
    private String pollingInterval;

    @Override
    public void configure() throws Exception {
        from("timer:pollDatabase?period=" + pollingInterval)
                .routeId("update-queue-polling-route")
                .to("jpa:UpdateQueue?query=SELECT u FROM UpdateQueue u WHERE u.processed = false")
                .split(body()) // Process each record individually
                    .process(new UpdateQueuePollingProcessor(updateRootFolder))
                .process((exchange) -> {
                    UpdateQueue updateQueue = exchange.getIn().getBody(UpdateQueue.class);
                    updateQueue.setProcessed(true);
                })
                .to("jpa://UpdateQueue?useExecuteUpdate=true")
                    .log("The script Record ${body.scriptPath} as been published and marked as processed.");
    }
}
