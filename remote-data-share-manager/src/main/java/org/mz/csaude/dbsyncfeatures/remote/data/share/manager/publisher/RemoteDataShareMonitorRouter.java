package org.mz.csaude.dbsyncfeatures.remote.data.share.manager.publisher;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.mz.csaude.dbsyncfeatures.remote.data.share.manager.model.RemoteDataInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;


/**
 * Monitors the data-share process.
 * 
 * When it detects that the process is not running and the previous process was not finished, it force the start
 * 
 */

//@Component
//@Profile(ApplicationProfile.PUBLISHER)
public class RemoteDataShareMonitorRouter extends RouteBuilder {
	
	@Value("${remote.data.share.root.folder:}")
	private String remoteDataShareRootFolder;
	
	@Value("${remote.data.share.endpoint}")
	private String notificationsEndpoint;
	
	@Autowired
	DataShareLoader dataShareLoader;
	
	@Override
	public void configure() throws Exception {
		String monitorFolder = Commons.getShareMonitorFile().getParent();
		
		
		String srcUri = "file:" + monitorFolder + "?fileName=" + Commons.getShareMonitorFile().getName();
		
		String dstUri = notificationsEndpoint;
		from(srcUri)
		.log("Detected the sign for data-share-monitoring [" + simple("${header.CamelFileAbsolutePath}") + "]")
		.bean(dataShareLoader)
		.marshal()
		.json(JsonLibrary.Jackson, RemoteDataInfo.class)
		.to(dstUri);
	}
}

