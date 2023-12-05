package org.mz.csaude.dbsyncfeatures.remote.data.share.manager;

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
import org.mz.csaude.dbsyncfeatures.remote.data.share.manager.utils.ApplicationProfile;
import org.mz.csaude.dbsyncfeatures.remote.data.share.manager.utils.Utils;
import org.openmrs.module.epts.etl.controller.ProcessStarter;
import org.openmrs.module.epts.etl.controller.conf.SyncConfiguration;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Component
public class RemoteDataShareCommons {
	
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
	
	@Value("${epts-etl.home.dir}")
	private String eptsEtlHomeDir;
	
	@Autowired
	private Environment env;
	
	private boolean configurationFileFullGenerated;
	
	private static String EXPORT_CONF_FILE_TEMPLATE = "/epts-etl/conf/db_quick_export_template_conf.json";
	
	private static String LOAD_CONF_FILE_TEMPLATE = "/epts-etl/conf/db_quick_load_template_conf.json";
	
	private static String EXPORT_CONF_FILE_NAME = "remote_data_share_db_quick_export.json";
	
	private static String LOAD_CONF_FILE_NAME = "remote_data_share_db_quick_load.json";
	
	@JsonIgnore
	public ProcessStarter getDataExportProcessStarter() {
		return dataExportProcessStarter;
	}
	
	public String getSyncConfigurationFilePath(String eptsEtlHomeDir) {
		String profile = env.getActiveProfiles()[0];
		
		String confFileName = "";
		
		if (ApplicationProfile.isRemote(profile)) {
			confFileName = EXPORT_CONF_FILE_NAME;
		} else if (ApplicationProfile.isCentral(profile)) {
			confFileName = LOAD_CONF_FILE_NAME;
		} else
			throw new RuntimeException("The conf file is unkown for profile [" + profile + "]");
		
		return eptsEtlHomeDir + File.separator + "conf" + File.separator + confFileName;
	}
	
	public File getSyncConfigurationFile(String eptsEtlHomeDir) {
		return new File(getSyncConfigurationFilePath(eptsEtlHomeDir));
	}
	
	public File getSyncConfigurationFileTemplate() {
		
		String profile = env.getActiveProfiles()[0];
		
		String confFileTemplate = "";
		
		if (ApplicationProfile.isRemote(profile)) {
			confFileTemplate = EXPORT_CONF_FILE_TEMPLATE;
		} else if (ApplicationProfile.isCentral(profile)) {
			confFileTemplate = LOAD_CONF_FILE_TEMPLATE;
		} else
			throw new RuntimeException("The conf template file is unkown for profile [" + profile + "]");
		
		URL fileUrl = RemoteDataShareCommons.class.getResource(confFileTemplate);
		
		return new File(fileUrl.getFile());
	}
	
	@JsonIgnore
	public String getShareMonitorFilePath() {
		
		if (syncConfig == null) {
			synchronized (stringLock) {
				try {
					
					loadSyncConfigurations(null);
					
					syncConfig = SyncConfiguration.loadFromFile(getSyncConfigurationFile(eptsEtlHomeDir));
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		
		return syncConfig.generateProcessStatusFolder() + File.separator + "remote-data-share" + File.separator + "monitor"
		        + File.separator + "data_share.monitor";
	}
	
	@JsonIgnore
	public File getShareMonitorFile() {
		return new File(getShareMonitorFilePath());
	}
	
	private void loadSyncConfigurations(RemoteDataShareInfo dataShareInfo) {
		
		if (configurationFileFullGenerated) {
			return;
		}
		
		synchronized (stringLock) {
			File eptsEtlConf = this.getSyncConfigurationFile(eptsEtlHomeDir);
			
			try {
				FileUtil.copyFile(this.getSyncConfigurationFileTemplate(), eptsEtlConf);
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
			
			String observation_date = dataShareInfo != null ? "" + dataShareInfo.getRequestDate().getTime() : "null";
			
			replaceAllInFile(eptsEtlConf, "origin_app_location_code", senderId);
			replaceAllInFile(eptsEtlConf, "openmrs_db_host", openmrsDbHost);
			replaceAllInFile(eptsEtlConf, "openmrs_db_port", openmrsDbPort);
			replaceAllInFile(eptsEtlConf, "openmrs_db_name", openmrsDbName);
			replaceAllInFile(eptsEtlConf, "openmrs_user_password", openmrsDbPassword);
			replaceAllInFile(eptsEtlConf, "openmrs_user_name", openmrsDbUser);
			replaceAllInFile(eptsEtlConf, "observation_date", "" + observation_date);
			
			if (dataShareInfo != null) {
				configurationFileFullGenerated = true;
			}
		}
	}
	
	public void startExportProcess(RemoteDataShareInfo dataShareInfo, Logger logger) throws Exception {
		
		logger.info("Starting data-share....");
		
		loadSyncConfigurations(dataShareInfo);
		
		File eptsEtlConf = this.getSyncConfigurationFile(eptsEtlHomeDir);
		
		//Create a sign to start monitoring
		Utils.writeObjectToFile(dataShareInfo, this.getShareMonitorFile());
		
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
