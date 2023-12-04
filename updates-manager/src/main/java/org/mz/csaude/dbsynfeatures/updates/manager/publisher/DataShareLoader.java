package org.mz.csaude.dbsynfeatures.updates.manager.publisher;

import org.mz.csaude.dbsynfeatures.updates.manager.model.ShareRemoteUpdateFile;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class DataShareLoader {
    public ShareRemoteUpdateFile loadFile(File file) throws Exception {
        return ShareRemoteUpdateFile.init(file);
    }
}
