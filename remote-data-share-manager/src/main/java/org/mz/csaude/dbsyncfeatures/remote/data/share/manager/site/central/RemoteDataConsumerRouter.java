package org.mz.csaude.dbsyncfeatures.remote.data.share.manager.site.central;

import java.io.File;
import java.util.Date;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangeProperties;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.util.FileUtil;
import org.mz.csaude.dbsyncfeatures.remote.data.share.manager.RemoteDataShareCommons;
import org.mz.csaude.dbsyncfeatures.remote.data.share.manager.model.RemoteDataInfo;
import org.mz.csaude.dbsyncfeatures.remote.data.share.manager.model.RemoteDataShareInfo;
import org.mz.csaude.dbsyncfeatures.remote.data.share.manager.service.RemoteDataShareInfoService;
import org.mz.csaude.dbsyncfeatures.remote.data.share.manager.utils.CustomMessageListenerContainer;
import org.mz.csaude.dbsyncfeatures.remote.data.share.manager.utils.Utils;
import org.mz.csaude.dbsynfeatures.core.manager.utils.ApplicationProfile;
import org.openmrs.module.epts.etl.controller.ProcessStarter;
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
			
			logger.info("Performing Data Request monitoring actions");
			
			File monitoringFile = new File(commons.getShareMonitorFilePath());
			
			//If the monitoring file exists, mean that there were initialized a process but the process was not finalized yet
			if (monitoringFile.exists()) {
				
				ProcessStarter p = new ProcessStarter(new String[] { commons.getSyncConfigurationFilePath(eptsEtlHomeDir) });
				p.init();
				
				//This mean the process is finished, so lets remove the monitoring file
				if (p.getCurrentController().processIsAlreadyFinished()) {
					logger.info("The share process is finihed...! Removing the monitoring file...");
					
					FileUtil.deleteFile(monitoringFile);
				} else {
					//The process has not finished. Let check if it is running
					
					if (commons.getDataExportProcessStarter() == null) {
						logger.info("The data share was requested but no share process is running. Force staring...");
						
						commons.startExportProcess(Utils.loadObjectFormJSON(RemoteDataShareInfo.class, monitoringFile),
						    logger);
					}
				}
			}
			else {
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


