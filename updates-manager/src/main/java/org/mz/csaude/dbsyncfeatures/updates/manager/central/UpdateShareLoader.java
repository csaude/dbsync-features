package org.mz.csaude.dbsyncfeatures.updates.manager.central;

import org.mz.csaude.dbsyncfeatures.core.manager.utils.ApplicationProfile;
import org.mz.csaude.dbsyncfeatures.updates.manager.model.ShareRemoteUpdateFile;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@Profile(ApplicationProfile.CENTRAL)
public class UpdateShareLoader {
    public ShareRemoteUpdateFile loadFile(File file) throws Exception {
        return ShareRemoteUpdateFile.init(file);
    }
}
