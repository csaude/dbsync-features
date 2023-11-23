package org.mz.csaude.dbsyncfeatures.remote.data.share.manager.publisher;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.mz.csaude.dbsyncfeatures.remote.data.share.manager.model.RemoteDataShareInfo;
import org.mz.csaude.dbsyncfeatures.remote.data.share.manager.utils.ApplicationProfile;
import org.mz.csaude.dbsyncfeatures.remote.data.share.manager.utils.DataShareInfoManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile(ApplicationProfile.PUBLISHER)
public class DataShareRequestListenerRouter extends RouteBuilder {
	
	@Value("${remote.data.share.request.endpoint}")
	private String artemisEndPoint;
	
	@Value("${db-sync.senderId}")
	private String senderId;
	
	@Autowired
	private DataShareInfoManager dataShareInfoManager;
	
	@Autowired
	private DataShareStarter shareStarter;
	
	@Override
	public void configure() throws Exception {
		dataShareInfoManager.setCurrentOriginLocation(senderId);
		
		//@formatter:off
		from(artemisEndPoint)
			.log("Received message from active MQ ${body}")
			.unmarshal()
			.json(JsonLibrary.Jackson, RemoteDataShareInfo.class)
			.setProperty("requestedForCurrentSite", method(dataShareInfoManager, "checkOrigin(${body})"))
			.choice()
	        	.when().simple("${exchangeProperty.requestedForCurrentSite}")
	        	  	.log("Received request for currente site[" + senderId + "]")
	        	  	.process(shareStarter)
	        	.otherwise()
	          		.log("Ignoring request as it is for a site [${body.originAppLocationCode}]")
	        .end()
	        .onCompletion()
			.onCompleteOnly()
			.choice()
				.when().simple("${exchangeProperty.requestedForCurrentSite}")
        			.log("Skip commit on test")
					//.process( exchange -> {CustomMessageListenerContainer.enableAcknowledgement();})
        		.otherwise()
        			.log("Not removing message from ActiveMQ as it was not consumed")
        	.end(); 		
	}
}

@Component
class DataShareStarter implements Processor{
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Override
	public void process(Exchange exchange) throws Exception {
		logger.info("Starting data-share....");
	}
	
}