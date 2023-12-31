package org.mz.csaude.dbsyncfeatures.remote.data.share.manager.site.remote;

import java.io.File;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.util.FileUtil;
import org.mz.csaude.dbsyncfeatures.core.manager.artemis.CustomMessageListenerContainer;
import org.mz.csaude.dbsyncfeatures.core.manager.utils.ApplicationProfile;
import org.mz.csaude.dbsyncfeatures.core.manager.utils.Utils;
import org.mz.csaude.dbsyncfeatures.remote.data.share.manager.RemoteDataShareCommons;
import org.mz.csaude.dbsyncfeatures.remote.data.share.manager.model.RemoteDataShareInfo;
import org.mz.csaude.dbsyncfeatures.remote.data.share.manager.utils.DataShareInfoManager;
import org.openmrs.module.epts.etl.controller.ProcessStarter;
import org.openmrs.module.epts.etl.dbquickexport.controller.DbQuickExportFinalizer;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({ ApplicationProfile.REMOTE, ApplicationProfile.DATA_SHARE_REMOTE })
public class RemoteDataRequestListenerRouter extends RouteBuilder {
	
	@Value("${remote.data.share.request.endpoint}")
	private String artemisEndPoint;
	
	@Value("${db-sync.senderId}")
	private String senderId;
	
	@Autowired
	private DataShareInfoManager dataShareInfoManager;
	
	@Autowired
	private DataShareStarter shareStarter;
	
	@Autowired
	private RemoteDataShareProcessMonitor shareMonitor;
	
	@Override
	public void configure() throws Exception {
		dataShareInfoManager.setCurrentOriginLocation(senderId);
		
		//@formatter:off
		from(artemisEndPoint)
			.routeId("Remote-Data-Request-Listener")
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
			.process(exchange -> {
									 	CustomMessageListenerContainer.enableAcknowledgement();
								  }
				)
        .end();
		
		int delay = 1000*60*1;
		int period = 1000*60*15;
		
		from("timer:data-share-monitor?delay=" + delay + "&period=" + period)
			.routeId("Data-Share-Monitor-In-Remote-Site")
			.bean(shareMonitor, "doMonitoring");
	}
}

//@formatter:on
@Component
@Profile({ ApplicationProfile.REMOTE, ApplicationProfile.DATA_SHARE_REMOTE })
class RemoteDataShareProcessMonitor {
	
	@Value("${eip.home}")
	private String eipHomeDir;
	
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
			
			logger.info("Performing Data Request monitoring actions");
			
			File monitoringFile = new File(commons.getShareMonitorFilePath());
			
			//If the monitoring file exists, mean that there were initialized a process but the process was not finalized yet
			if (monitoringFile.exists()) {
				
				ProcessStarter p = new ProcessStarter(new String[] { commons.getSyncConfigurationFilePath(eipHomeDir) });
				p.init();
				
				//This mean the process is finished, so lets remove the monitoring file
				if (p.getCurrentController().processIsAlreadyFinished()) {
					
					//Check if all files were published.
					
					if (!commons.checkIfImportDirectoryHasData()) {
						logger.info("All data were published. Finalizing the data share process");
						
						DbQuickExportFinalizer finalizer = new DbQuickExportFinalizer(p.getCurrentController());

						finalizer.performeFinalizationTasks();
						
						logger.info("Removing the monitoring file...");
						
						FileUtil.deleteFile(monitoringFile);
					}else {
						logger.info("The generation of share file is finished but the data is not full published! Waiting....");
					}
				} else {
					//The process has not finished. Let check if it is running
					
					if (commons.getDataExportProcessStarter() == null) {
						logger.info("The data share was requested but no share process is running. Force staring...");
						
						commons.startDataShareProcess(Utils.loadObjectFormJSON(RemoteDataShareInfo.class, monitoringFile),
						    logger);
					}
				}
			} else {
				logger.info("No monitoring file exists! Nothing to do!");
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

//@formatter:on
@Component
@Profile({ ApplicationProfile.REMOTE, ApplicationProfile.DATA_SHARE_REMOTE })
class DataShareStarter implements Processor {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	RemoteDataShareCommons commons;
	
	@Override
	public void process(Exchange exchange) throws Exception {
		RemoteDataShareInfo dataShareInfo = (RemoteDataShareInfo) exchange.getMessage().getBody();
		
		commons.startDataShareProcess(dataShareInfo, logger);
	}
}
