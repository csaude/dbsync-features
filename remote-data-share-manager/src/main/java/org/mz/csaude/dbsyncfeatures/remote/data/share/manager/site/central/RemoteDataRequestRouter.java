package org.mz.csaude.dbsyncfeatures.remote.data.share.manager.site.central;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.mz.csaude.dbsyncfeatures.core.manager.utils.ApplicationProfile;
import org.mz.csaude.dbsyncfeatures.remote.data.share.manager.model.RemoteDataShareInfo;
import org.mz.csaude.dbsyncfeatures.remote.data.share.manager.utils.DataShareInfoManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({ApplicationProfile.CENTRAL, ApplicationProfile.DATA_SHARE_CENTRAL})
public class RemoteDataRequestRouter extends RouteBuilder {
	
	@Value("${remote.data.share.request.endpoint}")
	private String artemisEndPoint;
	
	@Autowired
	private DataShareInfoManager dataShareInfoManager;
	
	@Override
	public void configure() throws Exception {
		String shareRequestSourceEndPoint = "scheduler:share-request-reader?initialDelay=" + 1000*15 +"&delay=" + 1000*60000*15;

		//@formatter:off
		from(shareRequestSourceEndPoint)
			.routeId("Remote-Data-Request")
			.log("Looking for data-share requests...")
			.setProperty("doLoop", simple("true"))
	        .loopDoWhile(simple("${exchangeProperty.doLoop}"))
	        .to("jpa:RemoteDataShareInfo?query=SELECT r FROM RemoteDataShareInfo r Where requestDate is null&maximumResults=1000")
	        .choice()
	        	.when().simple("${body.size()} == 0")
	        	  	.log("No site found for request data")
	        	  	.setProperty("doLoop", simple("false"))
	        	.endChoice()
				.otherwise()	
					.log("Found ${body.size()} site(s) for request data")
	        		.split(body())
	        		.log("Processing request for ${body.originAppLocationCode} site")
	        		.bean(dataShareInfoManager, "setRequestDateToNow")
	        		.log("Marshalling Message")
	        		.marshal()
	        		.json(JsonLibrary.Jackson, RemoteDataShareInfo.class)
	        		.log("Sending Message to ActiveMQ")
	        		.to(artemisEndPoint)
	        		.log("Message sent do activeMQ")
	        		.unmarshal()
	        		.json(JsonLibrary.Jackson, RemoteDataShareInfo.class)
	        		.bean(dataShareInfoManager, "updateRemoteDataShareInfo")
	        	 .endChoice()
	        .end()
	    .end();
	}
	
}