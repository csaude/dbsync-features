package org.mz.csaude.dbsyncfeatures.remote.data.share.manager.site.central;

import java.util.Date;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangeProperties;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.mz.csaude.dbsyncfeatures.core.manager.artemis.CustomMessageListenerContainer;
import org.mz.csaude.dbsyncfeatures.core.manager.utils.ApplicationProfile;
import org.mz.csaude.dbsyncfeatures.remote.data.share.manager.RemoteDataShareCommons;
import org.mz.csaude.dbsyncfeatures.remote.data.share.manager.model.RemoteDataInfo;
import org.mz.csaude.dbsyncfeatures.remote.data.share.manager.model.RemoteDataShareInfo;
import org.mz.csaude.dbsyncfeatures.remote.data.share.manager.service.RemoteDataShareInfoService;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({ApplicationProfile.CENTRAL, ApplicationProfile.DATA_SHARE_CENTRAL})
public class RemoteDataConsumerRouter extends RouteBuilder {
	
	@Value("${remote.data.share.endpoint}")
	private String artemisEndPoint;
	
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
	
	@Autowired
	private CentralDataShareProcessMonitor shareMonitor;
	
	@Autowired
	private RemoteDataShareCommons commons;
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Override
	public void configure() {
		String srcUri = artemisEndPoint;
		String dstUri = "file:" + commons.getDataShareDirectory();
		
		commons.tryToCreateImportDataFolders(logger);
		
		//@formatter:off
		from(srcUri)
			.routeId("Remote-Data-Consumer")
			.unmarshal()
			.json(JsonLibrary.Jackson, RemoteDataInfo.class)
		    .log("Message [" + simple("${body.fileName}") + "] was received from " + simple("${body.originAppLocationCode}"))
		    .choice()
		    	.when(simple("${body.empty}"))
		        	.log("Receiveid finish signal for ${body.originAppLocationCode} site. Finishing...").process(shareFinalizer)
		        .endChoice()
		        .otherwise()
		        	.setProperty("dstUri", simple(dstUri + "${body.destinationRelativePath}"))
		        	.setProperty("fileName", simple("${body.fileName}"))
		        	.bean(reader)
		        	.log("Writing file to: " + simple("${exchangeProperty.dstUri}"))
		        	.setHeader("CamelFileName", simple("${exchangeProperty.fileName}"))
		        	.dynamicRouter(method(destinationGenerator))
		        .endChoice()
		    .end()
		    .onCompletion()
		    	.onCompleteOnly()
		        	.process(exchange -> {
		        		CustomMessageListenerContainer.enableAcknowledgement();
		        	});
		
		int delay = 1000*15;
		int period = 1000 * 60 * 10;
		
		from("timer:data-share-monitor?delay=" + delay + "&period=" + period)
		.routeId("Data-Share-Monitor-In-Central-Site")
		.bean(shareMonitor, "doMonitoring");
	}
}

//@formatter:on
@Component
@Profile({ApplicationProfile.CENTRAL, ApplicationProfile.DATA_SHARE_CENTRAL})
class CentralDataShareProcessMonitor {
	
	@Autowired
	private RemoteDataShareCommons commons;
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	/**
	 * Create a sign for monitor route start its job
	 * 
	 * @param dataShareInfo
	 */
	public void doMonitoring() {
		try {
			logger.info("Performing Data Load monitoring actions");
			
			if (commons.getDataExportProcessStarter() != null) {
				logger.info("The Import process is running! Nothing to do!");
			} else if (commons.checkIfImportDirectoryHasData()) {
				logger.info("The load process is not running but there are data to load...");
				
				commons.startDataShareProcess(null, logger);
			} else {
				logger.info("There is no data to load. Sleeping...");
			}
		}
		catch (DBException e) {
			throw new RuntimeException(e);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}

@Component
@Profile({ApplicationProfile.CENTRAL, ApplicationProfile.DATA_SHARE_CENTRAL})
class AttachmentReader {
	
	public byte[] readAttachmentContent(RemoteDataInfo data) {
		return data.getData();
	}
}

@Component
@Profile({ApplicationProfile.CENTRAL, ApplicationProfile.DATA_SHARE_CENTRAL})
class DestinationGenerator {
	
	public String getDestinationFolder(@ExchangeProperties Map<String, String> properies) {
		String dstUri = properies.get("dstUri");
		
		properies.put("dstUri", "");
		
		return dstUri.isEmpty() ? null : dstUri;
	}
}

@Component
@Profile({ApplicationProfile.CENTRAL, ApplicationProfile.DATA_SHARE_CENTRAL})
class SiteDataShareFinalizer implements Processor {
	
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
