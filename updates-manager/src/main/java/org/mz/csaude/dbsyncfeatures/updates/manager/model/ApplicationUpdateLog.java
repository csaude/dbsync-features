package org.mz.csaude.dbsyncfeatures.updates.manager.model;

import com.sun.istack.NotNull;
import org.mz.csaude.dbsyncfeatures.core.manager.entity.LifeCycle;
import org.mz.csaude.dbsyncfeatures.core.manager.utils.ApplicationProfile;
import org.springframework.context.annotation.Profile;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "application_update_log")
@Profile(ApplicationProfile.REMOTE)
public class ApplicationUpdateLog extends LifeCycle {

    @NotNull
    @Column(name = "site_id", nullable = false, unique = true)
    private String siteId;
    @NotNull
    @Column(name = "current_version", nullable = false, unique = true)
    private String currentVersion;

    public String getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(String currentVersion) {
        this.currentVersion = currentVersion;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }
}
