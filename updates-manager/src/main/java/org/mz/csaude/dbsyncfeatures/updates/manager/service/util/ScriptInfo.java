package org.mz.csaude.dbsyncfeatures.updates.manager.service.util;

import java.util.List;

public class ScriptInfo {
    private String scriptData;

    private List<String> sitesToUpdate;

    private boolean updateAll;

    public String getScriptData() {
        return scriptData;
    }

    public void setScriptData(String scriptData) {
        this.scriptData = scriptData;
    }

    public List<String> getSitesToUpdate() {
        return sitesToUpdate;
    }

    public void setSitesToUpdate(List<String> sitesToUpdate) {
        this.sitesToUpdate = sitesToUpdate;
    }

    public boolean isUpdateAll() {
        return updateAll;
    }

    public void setUpdateAll(boolean updateAll) {
        this.updateAll = updateAll;
    }
}
