package org.mz.csaude.dbsyncfeatures.updates.manager.model;

import com.sun.istack.NotNull;
import org.mz.csaude.dbsyncfeatures.core.manager.entity.LifeCycle;
import org.mz.csaude.dbsyncfeatures.core.manager.utils.ApplicationProfile;
import org.springframework.context.annotation.Profile;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "updated_site")
@Profile(ApplicationProfile.CENTRAL)
public class UpdatedSite extends LifeCycle {
    @NotNull
    @Column(name = "site_id", nullable = false)
    private String siteId;
    @NotNull
    @Column(name = "version", nullable = false)
    private String version;

    @Column(name="script_name")
    private String scriptName;

    @Column(name="log")
    private String log;

    @Column(name= "executed")
    private boolean executed;

    @Column(name="received_date")
    private Date receivedDate;

    public UpdatedSite(){

    }

    public UpdatedSite(String siteId, String version){
        this.siteId = siteId;
        this.version = version;
        this.setCreatedAt(new Date());
        this.setActive(Boolean.TRUE);
    }
    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getScriptName() {
        return scriptName;
    }

    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public boolean isExecuted() {
        return executed;
    }

    public void setExecuted(boolean executed) {
        this.executed = executed;
    }

    public Date getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(Date receivedDate) {
        this.receivedDate = receivedDate;
    }
}
