package org.mz.csaude.dbsyncfeatures.remote.data.share.manager.publisher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.util.FileUtil;
import org.apache.commons.io.IOUtils;
import org.mz.csaude.dbsyncfeatures.remote.data.share.manager.model.RemoteDataShareInfo;
import org.mz.csaude.dbsyncfeatures.remote.data.share.manager.utils.ApplicationProfile;
import org.mz.csaude.dbsyncfeatures.remote.data.share.manager.utils.CustomMessageListenerContainer;
import org.mz.csaude.dbsyncfeatures.remote.data.share.manager.utils.DataShareInfoManager;
import org.openmrs.module.epts.etl.controller.ProcessStarter;
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
			.to("direct:init-share-monitor")
        .end();
		
		from("direct:init-share-monitor")
			.bean(shareMonitor);
	}
}

//@formatter:on
@Component
class DataShareMonitor {
	
	/**
	 * Create a sign for monitor route start its job
	 * @param dataShareInfo
	 */
	public void initMonitoring(RemoteDataShareInfo dataShareInfo) {
		try {
			FileUtil.createNewFile(new File(Commons.getShareMonitorFilePath()));
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}

//@formatter:on
@Component
class DataShareStarter implements Processor {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Value("${db-sync.senderId}")
	private String senderId;
	
	@Value("${openmrs.db.host}")
	private String openmrsDbHost;
	
	@Value("${openmrs.db.port}")
	private String openmrsDbPort;
	
	@Value("${openmrs.db.name}")
	private String openmrsDbName;
	
	@Value("${spring.openmrs-datasource.password}")
	private String openmrsDbPassword;
	
	@Value("${spring.openmrs-datasource.username}")
	private String openmrsDbUser;
	
	@Override
	public void process(Exchange exchange) throws Exception {
		RemoteDataShareInfo dataShareInfo = (RemoteDataShareInfo) exchange.getMessage().getBody();
		
		logger.info("Starting data-share....");
		
		//String eptssyncPath="/home/eip/application/eptssync/eptssync-api-1.0.jar";
		//String eptsEtlHome = "/home/eip/application/eptssync/";
		
		String eptsEtlHome = "D:\\PRG\\JEE\\Workspace\\CSaude\\eptssync\\";
		
		File eptsEtlConf = new File(eptsEtlHome + File.separator + "conf" + File.separator + "db-quick-export.json");
		
		FileUtil.copyFile(Commons.getSyncConfigurationFile(), eptsEtlConf);
		
		replaceAllInFile(eptsEtlConf, "origin_app_location_code", senderId);
		replaceAllInFile(eptsEtlConf, "openmrs_db_host", openmrsDbHost);
		replaceAllInFile(eptsEtlConf, "openmrs_db_port", openmrsDbPort);
		replaceAllInFile(eptsEtlConf, "openmrs_db_name", openmrsDbName);
		replaceAllInFile(eptsEtlConf, "openmrs_user_password", openmrsDbPassword);
		replaceAllInFile(eptsEtlConf, "openmrs_user_name", openmrsDbUser);
		replaceAllInFile(eptsEtlConf, "observation_date", "" + dataShareInfo.getRequestDate().getTime());
		
		try {
			ProcessStarter p = new ProcessStarter(new String[] { eptsEtlConf.getAbsolutePath() }, logger);
			p.run();
		}
		catch (IOException e) {}
		
	}
	
	public static void replaceAllInFile(File file, String toFind, String replacement) {
		try {
			Charset charset = StandardCharsets.UTF_8;
			
			String content = IOUtils.toString(new FileInputStream(file), charset);
			content = content.replaceAll(toFind, replacement);
			IOUtils.write(content, new FileOutputStream(file), charset);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}
