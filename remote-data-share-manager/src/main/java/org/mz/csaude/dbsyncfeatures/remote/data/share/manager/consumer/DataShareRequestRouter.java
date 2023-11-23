package org.mz.csaude.dbsyncfeatures.remote.data.share.manager.consumer;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.mz.csaude.dbsyncfeatures.remote.data.share.manager.model.RemoteDataShareInfo;
import org.mz.csaude.dbsyncfeatures.remote.data.share.manager.utils.ApplicationProfile;
import org.mz.csaude.dbsyncfeatures.remote.data.share.manager.utils.DataShareInfoManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile(ApplicationProfile.CONSUMER)
public class DataShareRequestRouter extends RouteBuilder {
	
	@Value("${remote.data.share.request.endpoint}")
	private String artemisEndPoint;
	
	@Autowired
	private DataShareInfoManager dataShareInfoManager;
	
	@Override
	public void configure() throws Exception {
		int mins = 1;
		
		String shareRequestSourceEndPoint = "scheduler:share-request-reader?initialDelay=" + mins*1000+"&delay=" + mins*60000;
		
		//@formatter:off
		from(shareRequestSourceEndPoint)
			.log("Looking for data-share requests...")
			.setProperty("doLoop", simple("true"))
	        .loopDoWhile(simple("${exchangeProperty.doLoop}"))
	        .to("jpa:RemoteDataShareInfo?query=SELECT r FROM RemoteDataShareInfo r Where requestDate is null&maximumResults=1000")
	        .choice()
	        	.when().simple("${body.size()} == 0")
	        	  	.log("No site found for request data")
	        	  	.setProperty("doLoop", simple("false"))
				.otherwise()	
					.log("Found ${body.size()} site(s) for request data")
	        		.split(body())
	        		.log("Processing request for ${body.originAppLocationCode} site")
	        		.bean(dataShareInfoManager, "setRequestDateToNow")
	        		.marshal()
	        		.json(JsonLibrary.Jackson, RemoteDataShareInfo.class)
	        		.to(artemisEndPoint)
	        		.unmarshal()
	        		.json(JsonLibrary.Jackson, RemoteDataShareInfo.class)
	        		.bean(dataShareInfoManager, "updateRemoteDataShareInfo")
	        	 .endChoice()
	        .end();
	}
	
}