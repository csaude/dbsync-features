package org.mz.csaude.dbsyncfeatures.updates.manager.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.istack.NotNull;
import org.mz.csaude.dbsyncfeatures.core.manager.entity.LifeCycle;
import org.mz.csaude.dbsyncfeatures.core.manager.utils.ApplicationProfile;
import org.springframework.context.annotation.Profile;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "update_queue")
@Profile(ApplicationProfile.CENTRAL)
public class UpdateQueue extends LifeCycle {

    @NotNull
    @Column(name = "script_path", nullable = false)
    private String scriptPath;

    @NotNull
    @Column(name = "sites_to_update_path", nullable = false)
    private String sitesToUpdatePath;

    @JsonProperty("updateAll")
    @Column(name = "update_all")
    private boolean updateAll;

    @NotNull
    @Column(name = "description", nullable = false)
    private String description;

    @JsonProperty("processed")
    @Column(name = "processed")
    private boolean processed;

    public UpdateQueue() {

    }


    public String getScriptPath() {
        return scriptPath;
    }

    public void setScriptPath(String scriptPath) {
        this.scriptPath = scriptPath;
    }

    public String getSitesToUpdatePath() {
        return sitesToUpdatePath;
    }

    public void setSitesToUpdatePath(String sitesToUpdatePath) {
        this.sitesToUpdatePath = sitesToUpdatePath;
    }

    public boolean isUpdateAll() {
        return updateAll;
    }

    public void setUpdateAll(boolean updateAll) {
        this.updateAll = updateAll;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }
}