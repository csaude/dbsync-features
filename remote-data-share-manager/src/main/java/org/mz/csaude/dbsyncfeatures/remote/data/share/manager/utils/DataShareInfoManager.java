package org.mz.csaude.dbsyncfeatures.remote.data.share.manager.utils;

import java.util.Date;

import org.mz.csaude.dbsyncfeatures.remote.data.share.manager.model.RemoteDataShareInfo;
import org.mz.csaude.dbsyncfeatures.remote.data.share.manager.service.RemoteDataShareInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataShareInfoManager {
	private String currentOriginLocation;
	
	
	@Autowired
	private RemoteDataShareInfoService shareInfoService;
	
	public void updateRemoteDataShareInfo(RemoteDataShareInfo data) {
		shareInfoService.persist(data);
	}
	
	public void setRequestDateToNow(RemoteDataShareInfo data) {
		data.setRequestDate(new Date());
	}
	
	public boolean checkOrigin(RemoteDataShareInfo data) {
		return data.getOriginAppLocationCode().equalsIgnoreCase(this.currentOriginLocation);
	}
	
	public void setCurrentOriginLocation(String currentOriginLocation) {
		this.currentOriginLocation = currentOriginLocation;
	}
}
