package org.mz.csaude.dbsyncfeatures.remote.data.share.manager.site.remote;

import java.io.File;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.mz.csaude.dbsyncfeatures.core.manager.utils.ApplicationProfile;
import org.mz.csaude.dbsyncfeatures.remote.data.share.manager.RemoteDataShareCommons;
import org.mz.csaude.dbsyncfeatures.remote.data.share.manager.model.RemoteDataInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({ ApplicationProfile.REMOTE, ApplicationProfile.DATA_SHARE_REMOTE })
public class RemoteDataPublisherRouter extends RouteBuilder {
	
	@Value("${remote.data.share.endpoint}")
	private String artemisEndPoint;
	
	@Autowired
	DataShareLoader dataShareLoader;
	
	@Autowired
	RemoteDataShareCommons commons;
	
	@Override
	public void configure() throws Exception {
		String srcUri = "file:" + commons.getDataShareDirectory()
		        + "?includeExt=json&recursive=true&directoryMustExist=false&sortBy=file:modified;file:name";
		String dstUri = artemisEndPoint;
		
		//@formatter:off
		from(srcUri)
			.routeId("Remote-Data-Publisher")
			.log("Reading the file " + simple("${header.CamelFileAbsolutePath}"))
			.bean(dataShareLoader).marshal()
			.json(JsonLibrary.Jackson, RemoteDataInfo.class).to(dstUri);
	}
}

@Component
@Profile({ ApplicationProfile.REMOTE, ApplicationProfile.DATA_SHARE_REMOTE })
class DataShareLoader {
	
	public RemoteDataInfo loadFile(File file) throws Exception {
		return RemoteDataInfo.init(file);
	}
	
}
