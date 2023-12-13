package org.mz.csaude.dbsyncfeatures.remote.data.share.manager.service;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.mz.csaude.dbsyncfeatures.remote.data.share.manager.model.RemoteDataShareInfo;
import org.mz.csaude.dbsyncfeatures.remote.data.share.manager.repository.RemoteDataShareInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RemoteDataShareInfoService {
	
	@Autowired
	private RemoteDataShareInfoRepository remoteDataShareInfoRepository;
	
	public void persist(RemoteDataShareInfo data) {
		List<RemoteDataShareInfo> existingData = remoteDataShareInfoRepository
		        .findByOriginAppLocationCode(data.getOriginAppLocationCode());
		
		if (CollectionUtils.isEmpty(existingData)) {
			remoteDataShareInfoRepository.save(data);
		} else {
			for (RemoteDataShareInfo r : existingData) {
				//There is an assumption that for any origin location only one entry can have empty days
				//And this record is the last registered
				if (r.getRequestDate() == null || r.getImportFinishDate() == null) {
					if (r.getRequestDate() == null) {
						r.setRequestDate(data.getRequestDate());
					}
					
					if (r.getImportFinishDate() == null) {
						r.setImportFinishDate(data.getImportFinishDate());
					}
					
					data.setId(r.getId());
					
					remoteDataShareInfoRepository.save(data);
					
					break;
				}
			}
		}
	}
}
