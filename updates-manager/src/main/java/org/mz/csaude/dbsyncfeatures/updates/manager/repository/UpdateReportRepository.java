package org.mz.csaude.dbsyncfeatures.updates.manager.repository;

import org.mz.csaude.dbsyncfeatures.updates.manager.model.UpdateReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UpdateReportRepository extends JpaRepository<UpdateReport, Long> {
}
