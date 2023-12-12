package org.mz.csaude.dbsyncfeatures.updates.manager.publisher;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.mz.csaude.dbsyncfeatures.updates.manager.model.ShareRemoteUpdateFile;
import org.mz.csaude.dbsyncfeatures.core.manager.utils.ApplicationProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile(ApplicationProfile.CENTRAL)
public class UpdateShareFileRouter extends RouteBuilder {

	@Value("${share.update.root.folder}")
	private String updateRootFolder;

	@Value("${artemis.dbsync.central.updates.endpoint}")
	private String shareUpdatesEndpoint;

	@Autowired
	private UpdateShareLoader dataShareLoader;

	@Override
	public void configure() {

		String srcUri = "file:" + updateRootFolder + "?fileName=updates.sh";
		String dstUri = shareUpdatesEndpoint;
		from(srcUri)
				.log("Reading the file " + simple("${header.CamelFileNameOnly}"))
				.bean(dataShareLoader,"loadFile")
				.marshal()
				.json(JsonLibrary.Jackson, ShareRemoteUpdateFile.class)
				.to(dstUri);
	}
}


