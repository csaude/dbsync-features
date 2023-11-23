package org.mz.csaude.dbsyncfeatures.remote.data.share.manager.publisher;

import java.io.File;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.mz.csaude.dbsyncfeatures.remote.data.share.manager.model.RemoteDataInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

//@Component
//@Profile(ApplicationProfile.PUBLISHER)
public class RemoteDataSharePublisherRouter extends RouteBuilder {
	
	@Value("${remote.data.share.root.folder:}")
	private String remoteDataShareRootFolder;
	
	@Value("${remote.data.share.endpoint}")
	private String notificationsEndpoint;
	
	@Autowired
	DataShareLoader dataShareLoader;
	
	@Override
	public void configure() throws Exception {
		String srcUri = "file:" + remoteDataShareRootFolder + "?includeExt=json&recursive=true&directoryMustExist=true&sortBy=file:modified;file:name";
		String dstUri = notificationsEndpoint;
		from(srcUri)
		.log("Reading the file " + simple("${header.CamelFileAbsolutePath}"))
		.bean(dataShareLoader)
		.marshal()
		.json(JsonLibrary.Jackson, RemoteDataInfo.class)
		.to(dstUri);
	}
}

@Component
class DataShareLoader {
	
	public RemoteDataInfo loadFile(File file) throws Exception {
		return RemoteDataInfo.init(file);
	}
	
}
