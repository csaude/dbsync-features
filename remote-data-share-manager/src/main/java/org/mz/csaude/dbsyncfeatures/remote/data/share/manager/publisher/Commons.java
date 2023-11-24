package org.mz.csaude.dbsyncfeatures.remote.data.share.manager.publisher;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.openmrs.module.epts.etl.controller.conf.SyncConfiguration;

public class Commons {
	
	private static final String stringLock = new String("LOCK_STRING");
	
	private static SyncConfiguration syncConfig;
	
	public static File getSyncConfigurationFile() {
		URL fileUrl = Commons.class.getResource("/epts-etl/conf/db_quick_export_template_conf.json");
		
		return new File(fileUrl.getFile());
	}
	
	public static SyncConfiguration getSyncConfiguration() {
		if (syncConfig != null) {
			return syncConfig;
		}
		
		synchronized (stringLock) {
			try {
				syncConfig = SyncConfiguration.loadFromFile(getSyncConfigurationFile());
				
				return syncConfig;
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
	}
	
	public static String getShareMonitorFilePath() {
		return getSyncConfiguration().generateProcessStatusFolder() + File.separator + "remote-data-share" + File.separator
		        + "monitor" + File.separator + "data_share.monitor";
	}
	
	public static File getShareMonitorFile() {
		return new File(getShareMonitorFilePath());
	}
}
