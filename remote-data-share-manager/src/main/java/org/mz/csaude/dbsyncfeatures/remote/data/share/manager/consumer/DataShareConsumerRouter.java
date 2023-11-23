package org.mz.csaude.dbsyncfeatures.remote.data.share.manager.consumer;

import java.util.Date;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangeProperties;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.mz.csaude.dbsyncfeatures.remote.data.share.manager.model.RemoteDataInfo;
import org.mz.csaude.dbsyncfeatures.remote.data.share.manager.model.RemoteDataShareInfo;
import org.mz.csaude.dbsyncfeatures.remote.data.share.manager.service.RemoteDataShareInfoService;
import org.mz.csaude.dbsyncfeatures.remote.data.share.manager.utils.CustomMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

//@Component
//@Profile(ApplicationProfile.CONSUMER)
public class DataShareConsumerRouter extends RouteBuilder {
	
	@Value("${remote.data.share.endpoint}")
	private String artemisEndPoint;

	@Value("${remote.data.share.root.folder}")
	private String remoteDataShareRootFolder;
	
	@Value("${smtp.host.name}")
	private String host;

	@Value("${smtp.host.port}")
	private int port;

	@Value("${smtp.auth.user}")
	private String username;

	@Value("${smtp.auth.pass}")
	private String password;

	@Autowired
	private AttachmentReader reader;

	@Autowired
	private DestinationGenerator destinationGenerator;
	
	@Autowired
	private SiteDataShareFinalizer shareFinalizer;
	
	@Override
	public void configure() {
		String srcUri = artemisEndPoint;
	  	String dstUri = "file:"+ remoteDataShareRootFolder;		
		
		from(srcUri)
			.unmarshal()
			.json(JsonLibrary.Jackson, RemoteDataInfo.class)
			.log("Message [" + simple(dstUri + "${body.fileName}") + "] was received from " + simple(dstUri + "${body.originAppLocationCode}"))
				.choice()
						.when(simple("${body.empty}"))
							.log("Receiveid finish signal for ${body.originAppLocationCode} site. Finishing...")
							.process(shareFinalizer)
						.endChoice()
						.otherwise()
							.setProperty("dstUri", simple(dstUri + "${body.destinationRelativePath}"))
							.setProperty("fileName", simple("${body.fileName}"))
							.bean(reader)
							.log("Writing file to: "+ simple("${exchangeProperty.dstUri}"))
							.setHeader("CamelFileName", simple("${exchangeProperty.fileName}"))
							.dynamicRouter(method(destinationGenerator))
						.endChoice()
					.end()
				.onCompletion()
				.onCompleteOnly()
				.process( exchange -> {
					CustomMessageListenerContainer.enableAcknowledgement();
				});
	}
}


@Component
class AttachmentReader{
	public byte[] readAttachmentContent(RemoteDataInfo data) {
		return data.getData();
	}
}

@Component
class DestinationGenerator{
	public String getDestinationFolder(@ExchangeProperties Map<String, String> properies) {
		String dstUri = properies.get("dstUri");
		
		properies.put("dstUri", "");
		
		return dstUri.isEmpty() ? null : dstUri;
	}
}


@Component
class SiteDataShareFinalizer implements Processor{
	
	@Autowired
	private RemoteDataShareInfoService dataShareService;

	@Override
	public void process(Exchange exchange) throws Exception {
		RemoteDataInfo remoteData = (RemoteDataInfo) exchange.getMessage().getBody();
	
		RemoteDataShareInfo dataShareInfo = new RemoteDataShareInfo();
		dataShareInfo.setImportFinishDate(new Date());
		dataShareInfo.setOriginAppLocationCode(remoteData.getOriginAppLocationCode());
		
		dataShareService.persist(dataShareInfo);
	}
}


