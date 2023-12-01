package org.mz.csaude.dbsyncfeatures.remote.data.share.manager.publisher;

import java.io.File;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.util.FileUtil;
import org.mz.csaude.dbsyncfeatures.remote.data.share.manager.model.RemoteDataShareInfo;
import org.mz.csaude.dbsyncfeatures.remote.data.share.manager.model.Utils;
import org.mz.csaude.dbsyncfeatures.remote.data.share.manager.utils.ApplicationProfile;
import org.mz.csaude.dbsyncfeatures.remote.data.share.manager.utils.CustomMessageListenerContainer;
import org.mz.csaude.dbsyncfeatures.remote.data.share.manager.utils.DataShareInfoManager;
import org.openmrs.module.epts.etl.controller.ProcessStarter;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
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
	
	@Autowired
	private DataShareMonitor shareMonitor;
	
	@Value("${epts-etl.home.dir:}")
	private String eptsEtlHomeDir;
	
	@Autowired
	private DataShareCommons commons;
	
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
			.process(exchange -> {CustomMessageListenerContainer.enableAcknowledgement();})
			.to("file:"+commons.getShareMonitorFilePath())
        .end();
		
		int delay = 1000*60;
		int period = 1000*60*5;
		
		from("timer:data-share-monitor?delay=" + delay + "&period=" + period)
			.log(artemisEndPoint)
			.bean(shareMonitor, "doMonitoring");
	}
}

//@formatter:on
@Component
class DataShareMonitor {
	
	@Value("${epts-etl.home.dir:}")
	private String eptsEtlHomeDir;
	
	@Autowired
	private DataShareCommons commons;
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	/**
	 * Create a sign for monitor route start its job
	 * 
	 * @param dataShareInfo
	 */
	public void doMonitoring() {
		try {
			File monitoringFile = new File(commons.getShareMonitorFilePath());
			
			//If the monitoring file exists, mean that there were initialized a process but the process was not finalized yet
			if (monitoringFile.exists()) {
				
				ProcessStarter p = new ProcessStarter(new String[] { commons.getSyncConfigurationFilePath(eptsEtlHomeDir) });
				p.init();
				
				//This mean the process is finished, so lets remove the monitoring file
				if (p.getCurrentController().processIsAlreadyFinished()) {
					FileUtil.deleteFile(monitoringFile);
				} else {
					//The process has not finished. Let check if it is running
					
					if (commons.getDataExportProcessStarter() == null) {
						commons.startExportProcess(Utils.loadObjectFormJSON(null, eptsEtlHomeDir), logger);
					}
				}
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
class DataShareStarter implements Processor {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	DataShareCommons commons;
	
	@Override
	public void process(Exchange exchange) throws Exception {
		RemoteDataShareInfo dataShareInfo = (RemoteDataShareInfo) exchange.getMessage().getBody();
		
		commons.startExportProcess(dataShareInfo, logger);
	}
}
