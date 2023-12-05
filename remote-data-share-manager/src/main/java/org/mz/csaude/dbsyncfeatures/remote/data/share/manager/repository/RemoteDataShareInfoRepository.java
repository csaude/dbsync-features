package org.mz.csaude.dbsyncfeatures.remote.data.share.manager.repository;

import java.util.List;

import org.mz.csaude.dbsyncfeatures.remote.data.share.manager.model.RemoteDataShareInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface RemoteDataShareInfoRepository extends JpaRepository<RemoteDataShareInfo, Integer>{
	List<RemoteDataShareInfo> findByOriginAppLocationCode(String originAppLocationCode);
}
