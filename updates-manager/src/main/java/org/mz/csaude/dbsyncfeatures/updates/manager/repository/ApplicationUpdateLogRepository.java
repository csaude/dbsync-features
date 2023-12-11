package org.mz.csaude.dbsyncfeatures.updates.manager.repository;

import org.mz.csaude.dbsyncfeatures.updates.manager.model.ApplicationUpdateLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationUpdateLogRepository extends JpaRepository<ApplicationUpdateLog, Long> {
    ApplicationUpdateLog findByCurrentVersion(String fileName);

}
