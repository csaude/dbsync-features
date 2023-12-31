package org.mz.csaude.dbsyncfeatures.remote.data.share.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;

import org.apache.commons.io.IOUtils;
import org.mz.csaude.dbsyncfeatures.core.manager.utils.ApplicationProfile;
import org.mz.csaude.dbsyncfeatures.core.manager.utils.Utils;
import org.mz.csaude.dbsyncfeatures.remote.data.share.manager.model.RemoteDataShareInfo;
import org.openmrs.module.epts.etl.controller.ProcessStarter;
import org.openmrs.module.epts.etl.controller.conf.SyncConfiguration;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.concurrent.ThreadPoolService;
import org.openmrs.module.epts.etl.utilities.io.FileUtilities;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Component
public class RemoteDataShareCommons {
	
	static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private static final String stringLock = new String("LOCK_STRING");
	
	private SyncConfiguration syncConfig;
	
	private ProcessStarter dataShareProcessStarter;
	
	@Value("${db-sync.senderId:no-id} ")
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
	
	@Value("${eip.home}")
	private String eipHomeDir;
	
	@Autowired
	private Environment env;
	
	private static String EXPORT_CONF_FILE_TEMPLATE = "epts-etl/conf/db_quick_export_template_conf.json";
	
	private static String LOAD_CONF_FILE_TEMPLATE = "epts-etl/conf/db_quick_load_template_conf.json";
	
	private static String EXPORT_CONF_FILE_NAME = "remote_data_share_db_quick_export.json";
	
	private static String LOAD_CONF_FILE_NAME = "remote_data_share_db_quick_load.json";
	
	@JsonIgnore
	public ProcessStarter getDataExportProcessStarter() {
		return dataShareProcessStarter;
	}
	
	public String getSyncConfigurationFilePath(String eptsEtlHomeDir) {
		String confFileName = "";
		
		if (ApplicationProfile.isRemote(getActiveProfile())) {
			confFileName = EXPORT_CONF_FILE_NAME;
		} else if (ApplicationProfile.isCentral(getActiveProfile())) {
			confFileName = LOAD_CONF_FILE_NAME;
		} else
			throw new RuntimeException("The conf file is unkown for profiles [" + getActiveProfile() + "]");
		
		return eptsEtlHomeDir + File.separator + "conf" + File.separator + confFileName;
	}
	
	public File getSyncConfigurationFile(String eptsEtlHomeDir) {
		return new File(getSyncConfigurationFilePath(eptsEtlHomeDir));
	}
	
	@JsonIgnore
	public String getShareMonitorFilePath() {
		
		if (syncConfig == null) {
			synchronized (stringLock) {
				try {
					
					loadSyncConfigurations(null);
					
					syncConfig = SyncConfiguration.loadFromFile(getSyncConfigurationFile(eipHomeDir));
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		
		return syncConfig.generateProcessStatusFolder() + File.separator + "remote-data-share" + File.separator + "monitor"
		        + File.separator + "data_share.monitor";
	}
	
	public void tryToCreateImportDataFolders(Logger logger) {
		String basePath = getDataShareDirectoryPath();
		
		logger.info("Check import source directories");
		
		for (String tableName : syncConfig.parteTableConfigurationsToString()) {
			File dir = new File(basePath + File.separator + tableName);
			
			if (!dir.exists()) {
				logger.info("The dir for table " + tableName + " does not exists! Creating it....");
				dir.mkdirs();
				logger.info("The dir for table " + tableName + " was created at: " + dir.getAbsolutePath());
			}
		}
	}
	
	@JsonIgnore
	public boolean checkIfImportDirectoryHasData() {
		for (File file : getDataShareDirectory().listFiles()) {
			if (file.isDirectory()) {
				File[] filesInDir = file.listFiles();
				
				if (filesInDir.length > 0) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	@JsonIgnore
	public File getDataShareDirectory() {
		return new File(getDataShareDirectoryPath());
	}
	
	@JsonIgnore
	public String getDataShareDirectoryPath() {
		
		if (syncConfig == null) {
			synchronized (stringLock) {
				try {
					
					loadSyncConfigurations(null);
					
					syncConfig = SyncConfiguration.loadFromFile(getSyncConfigurationFile(eipHomeDir));
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		
		String shareDirectory = "";
		shareDirectory += syncConfig.getSyncRootDirectory() + File.separator;
		
		if (ApplicationProfile.isCentral(getActiveProfile())) {
			shareDirectory += "import" + File.separator;
			shareDirectory += syncConfig.getOperations().get(0).getChild().getSourceFolders().get(0);
		} else if (ApplicationProfile.isRemote(getActiveProfile())) {
			shareDirectory += "_" + syncConfig.getOriginAppLocationCode().toLowerCase();
			shareDirectory += FileUtilities.getPathSeparator();
			shareDirectory += "export";
		}
		
		return shareDirectory;
	}
	
	@JsonIgnore
	public File getShareMonitorFile() {
		return new File(getShareMonitorFilePath());
	}
	
	private void loadSyncConfigurations(RemoteDataShareInfo dataShareInfo) {
		
		synchronized (stringLock) {
			File eptsEtlConf = this.getSyncConfigurationFile(eipHomeDir);
			
			InputStream inputStream = null;
			OutputStream outputStream = null;
			
			try {
				String confFileTemplate = "";
				
				if (ApplicationProfile.isRemote(getActiveProfile())) {
					confFileTemplate = EXPORT_CONF_FILE_TEMPLATE;
				} else if (ApplicationProfile.isCentral(getActiveProfile())) {
					confFileTemplate = LOAD_CONF_FILE_TEMPLATE;
				} else
					throw new RuntimeException("The conf template file is unkown for profiles [" + getActiveProfile() + "]");
				
				eptsEtlConf.getParentFile().mkdirs();
				
				inputStream = RemoteDataShareCommons.class.getClassLoader().getResourceAsStream(confFileTemplate);
				
				
				
				outputStream = new FileOutputStream(eptsEtlConf.getAbsolutePath());
				
				IOUtils.copy(inputStream, outputStream);
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
			finally {
				if (inputStream != null) {
					try {
						inputStream.close();
					}
					catch (IOException e) {}
				}
				
				if (outputStream != null) {
					try {
						outputStream.close();
					}
					catch (IOException e) {}
				}
			}
			
			String observation_date = dataShareInfo != null ? "" + dataShareInfo.getRequestDate().getTime() : "null";
			
			replaceAllInFile(eptsEtlConf, "origin_app_location_code", senderId);
			replaceAllInFile(eptsEtlConf, "openmrs_db_host", openmrsDbHost);
			replaceAllInFile(eptsEtlConf, "openmrs_db_port", openmrsDbPort);
			replaceAllInFile(eptsEtlConf, "openmrs_db_name", openmrsDbName);
			replaceAllInFile(eptsEtlConf, "openmrs_user_password", Matcher.quoteReplacement(openmrsDbPassword));
			replaceAllInFile(eptsEtlConf, "openmrs_user_name", openmrsDbUser);
			replaceAllInFile(eptsEtlConf, utilities.quote("observation_date"), "" + observation_date);
		}
	}
	
	public String[] getActiveProfile() {
		return this.env.getActiveProfiles();
	}
	
	public void startDataShareProcess(RemoteDataShareInfo dataShareInfo, Logger logger) throws Exception {
		
		String process = "";
		
		if (ApplicationProfile.isCentral(getActiveProfile())) {
			process = "Loading";
		} else if (ApplicationProfile.isRemote(getActiveProfile())) {
			process = "Exporting";
		} else
			throw new RuntimeException("Unsupported profile [" + getActiveProfile() + "]");
		
		logger.info("Starting " + process + " process on data-share");
		
		loadSyncConfigurations(dataShareInfo);
		
		File eptsEtlConf = this.getSyncConfigurationFile(eipHomeDir);
		
		if (ApplicationProfile.isCentral(getActiveProfile())) {
			String srcFolderList = "";
			
			for (File file : getDataShareDirectory().listFiles()) {
				if (file.isDirectory()) {
					
					if (srcFolderList.isEmpty()) {
						srcFolderList = utilities.quote(file.getName());
					} else {
						srcFolderList = String.join(",", srcFolderList, utilities.quote(file.getName()));
					}
				}
			}
		} else {
			//Create a sign to start monitoring
			Utils.writeObjectToFile(dataShareInfo, this.getShareMonitorFile());
		}
		
		this.dataShareProcessStarter = new ProcessStarter(new String[] { eptsEtlConf.getAbsolutePath() }, logger);
		
		ThreadPoolService.getInstance().createNewThreadPoolExecutor("data-share-executor")
		        .execute(this.dataShareProcessStarter);
	}
	
	private static void replaceAllInFile(File file, String toFind, String replacement) {
		
		OutputStream dstFile = null;
		InputStream srcFile = null;
		
		try {
			Charset charset = StandardCharsets.UTF_8;
			
			srcFile = new FileInputStream(file);
			
			String content = IOUtils.toString(srcFile, charset);
			content = content.replaceAll(toFind, replacement);
			
			dstFile = new FileOutputStream(file);
			
			IOUtils.write(content, dstFile, charset);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		finally {
			try {
				if (dstFile != null)
					dstFile.close();
			}
			catch (IOException e) {}
			try {
				if (srcFile != null)
					srcFile.close();
			}
			catch (IOException e) {}
		}
	}
}
