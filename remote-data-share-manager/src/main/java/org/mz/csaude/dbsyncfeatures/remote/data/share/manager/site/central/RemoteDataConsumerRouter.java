package org.mz.csaude.dbsyncfeatures.remote.data.share.manager.site.central;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangeProperties;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.mz.csaude.dbsyncfeatures.remote.data.share.manager.RemoteDataShareCommons;
import org.mz.csaude.dbsyncfeatures.remote.data.share.manager.model.RemoteDataInfo;
import org.mz.csaude.dbsyncfeatures.remote.data.share.manager.model.RemoteDataShareInfo;
import org.mz.csaude.dbsyncfeatures.remote.data.share.manager.service.RemoteDataShareInfoService;
import org.mz.csaude.dbsyncfeatures.remote.data.share.manager.utils.CustomMessageListenerContainer;
import org.mz.csaude.dbsynfeatures.core.manager.utils.ApplicationProfile;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.utilities.concurrent.ThreadPoolService;
import org.openmrs.module.epts.etl.utilities.concurrent.TimeCountDown;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile(ApplicationProfile.CENTRAL)
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
		from(srcUri).unmarshal()
			.json(JsonLibrary.Jackson, RemoteDataInfo.class)
		    .log("Message [" + simple(dstUri + "${body.fileName}") + "] was received from " + simple(dstUri + "${body.originAppLocationCode}"))
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
		
		int delay = 1000;
		int period = 1000 * 60 * 1;
		
		from("timer:data-share-monitor?delay=" + delay + "&period=" + period).bean(shareMonitor, "doMonitoring");
	}
}

//@formatter:on
@Component
@Profile(ApplicationProfile.CENTRAL)
class CentralDataShareProcessMonitor {
	
	@Value("${epts-etl.home.dir}")
	private String eptsEtlHomeDir;
	
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
			
			if (UUID.randomUUID() != null) {
				logger.info("Skip start import process");
				
				return;
			}
			
			if (commons.checkIfImportDirectoryHasData()) {
				//Kill current process
				if (commons.getDataExportProcessStarter() != null) {
					ProcessController loadController = commons.getDataExportProcessStarter().getCurrentController();
					
					/*
					 * Prevent multiple stop request
					 */
					if (loadController.stopRequested()) {
						return;
					}
					
					logger.info("Found new folders which are not on loading process!");
					logger.info("Stopping the current loading process...");
					
					loadController.requestStop();
					
					while (!loadController.isStopped()) {
						TimeCountDown.sleep(loadController.getWaitTimeToCheckStatus());
						
						logger.info("Waiting for process to stop!");
					}
					
					ThreadPoolService.getInstance().terminateTread(commons.getDataExportProcessStarter().getLogger(),
					    "data-share-executor", commons.getDataExportProcessStarter());
				} else {
					logger.info("The load process is not running but there are data to load...");
				}
				
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
@Profile(ApplicationProfile.CENTRAL)
class AttachmentReader {
	
	public byte[] readAttachmentContent(RemoteDataInfo data) {
		return data.getData();
	}
}

@Component
@Profile(ApplicationProfile.CENTRAL)
class DestinationGenerator {
	
	public String getDestinationFolder(@ExchangeProperties Map<String, String> properies) {
		String dstUri = properies.get("dstUri");
		
		properies.put("dstUri", "");
		
		return dstUri.isEmpty() ? null : dstUri;
	}
}

@Component
@Profile(ApplicationProfile.CENTRAL)
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
