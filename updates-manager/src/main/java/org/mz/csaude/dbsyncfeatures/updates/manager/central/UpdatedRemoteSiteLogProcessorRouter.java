package org.mz.csaude.dbsyncfeatures.updates.manager.central;

import org.apache.camel.builder.RouteBuilder;
import org.mz.csaude.dbsyncfeatures.core.manager.artemis.CustomMessageListenerContainer;
import org.mz.csaude.dbsyncfeatures.core.manager.utils.ApplicationProfile;
import org.mz.csaude.dbsyncfeatures.updates.manager.service.LogUpdatedRemoteSiteProcessor;
import org.mz.csaude.dbsyncfeatures.updates.manager.service.UpdatedSiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile(ApplicationProfile.CENTRAL)
public class UpdatedRemoteSiteLogProcessorRouter extends RouteBuilder {
	
	@Value("${artemis.dbsync.update.response.endpoint}")
	private String notificationsEndpoint;

	@Autowired
	private UpdatedSiteService updatedSiteService;

	@Override
	public void configure() {
		String srcUri = notificationsEndpoint;
		String dstUri = "log:mylog";

		from(srcUri)
				.routeId("updated-remote-site-log")
				.process(new LogUpdatedRemoteSiteProcessor(updatedSiteService))
				.to(dstUri)
					.onCompletion()
					.onCompleteOnly()
					.process( exchange -> {
						CustomMessageListenerContainer.enableAcknowledgement();
						exchange.getMessage().setBody("Site has been updated successfully");
				})
				.end();
	}
}