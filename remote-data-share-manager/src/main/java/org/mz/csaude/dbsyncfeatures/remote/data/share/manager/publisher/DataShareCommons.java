package org.mz.csaude.dbsyncfeatures.remote.data.share.manager.publisher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.camel.util.FileUtil;
import org.apache.commons.io.IOUtils;
import org.mz.csaude.dbsyncfeatures.remote.data.share.manager.model.RemoteDataShareInfo;
import org.openmrs.module.epts.etl.controller.ProcessStarter;
import org.openmrs.module.epts.etl.controller.conf.SyncConfiguration;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DataShareCommons {
	
	private static final String stringLock = new String("LOCK_STRING");
	
	private SyncConfiguration syncConfig;
	
	private ProcessStarter dataExportProcessStarter; 
	
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
	
	@Value("${epts-etl.home.dir:}")
	private String eptsEtlHomeDir;	
	
	public  ProcessStarter getDataExportProcessStarter() {
		return dataExportProcessStarter;
	}
	
	public String getSyncConfigurationFilePath(String eptsEtlHomeDir) {
		return eptsEtlHomeDir + File.separator + "conf" + File.separator + "db-quick-export.json";
	}
	
	public File getSyncConfigurationFile(String eptsEtlHomeDir) {
		return new File(getSyncConfigurationFilePath(eptsEtlHomeDir));
	}
	
	public File getSyncConfigurationFileTemplate() {
		URL fileUrl = DataShareCommons.class.getResource("/epts-etl/conf/db_quick_export_template_conf.json");
		
		return new File(fileUrl.getFile());
	}
	
	public SyncConfiguration getSyncConfiguration() {
		if (syncConfig != null) {
			return syncConfig;
		}
		
		synchronized (stringLock) {
			try {
				syncConfig = SyncConfiguration.loadFromFile(getSyncConfigurationFileTemplate());
				
				return syncConfig;
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
	}
	
	public  String getShareMonitorFilePath() {
		return getSyncConfiguration().generateProcessStatusFolder() + File.separator + "remote-data-share" + File.separator
		        + "monitor" + File.separator + "data_share.monitor";
	}
	
	public File getShareMonitorFile() {
		return new File(getShareMonitorFilePath());
	}
	
	public void startExportProcess(RemoteDataShareInfo dataShareInfo, Logger logger) throws Exception {
		
		logger.info("Starting data-share....");
		
		File eptsEtlConf = this.getSyncConfigurationFile(eptsEtlHomeDir);
		
		FileUtil.copyFile(this.getSyncConfigurationFileTemplate(), eptsEtlConf);
		
		replaceAllInFile(eptsEtlConf, "origin_app_location_code", senderId);
		replaceAllInFile(eptsEtlConf, "openmrs_db_host", openmrsDbHost);
		replaceAllInFile(eptsEtlConf, "openmrs_db_port", openmrsDbPort);
		replaceAllInFile(eptsEtlConf, "openmrs_db_name", openmrsDbName);
		replaceAllInFile(eptsEtlConf, "openmrs_user_password", openmrsDbPassword);
		replaceAllInFile(eptsEtlConf, "openmrs_user_name", openmrsDbUser);
		replaceAllInFile(eptsEtlConf, "observation_date", "" + dataShareInfo.getRequestDate().getTime());
		
		try {
			dataExportProcessStarter = new ProcessStarter(new String[] { eptsEtlConf.getAbsolutePath() }, logger);
			dataExportProcessStarter.run();
		}
		catch (IOException e) {}
		
	}
	
	private static void replaceAllInFile(File file, String toFind, String replacement) {
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
