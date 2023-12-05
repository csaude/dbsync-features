package org.mz.csaude.dbsyncfeatures.remote.data.share.manager.site.remote;

import java.io.File;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.mz.csaude.dbsyncfeatures.remote.data.share.manager.model.RemoteDataInfo;
import org.mz.csaude.dbsynfeatures.core.manager.utils.ApplicationProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile(ApplicationProfile.REMOTE)
public class RemoteDataPublisherRouter extends RouteBuilder {
	
	@Value("${remote.data.share.root.folder}")
	private String remoteDataShareRootFolder;
	
	@Value("${remote.data.share.endpoint}")
	private String artemisEndPoint;
	
	@Autowired
	DataShareLoader dataShareLoader;
	
	@Override
	public void configure() throws Exception {
		String srcUri = "file:" + remoteDataShareRootFolder + "?includeExt=json&recursive=true&directoryMustExist=false&sortBy=file:modified;file:name";
		String dstUri = artemisEndPoint;
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
