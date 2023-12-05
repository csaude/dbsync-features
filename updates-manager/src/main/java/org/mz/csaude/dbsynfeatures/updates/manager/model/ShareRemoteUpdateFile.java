package org.mz.csaude.dbsynfeatures.updates.manager.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.mz.csaude.dbsynfeatures.core.manager.utils.CommonConverter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;

public class ShareRemoteUpdateFile {
    private byte[] data;

    // It represent the version of the application
    private String fileName;

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public static ShareRemoteUpdateFile init(File dataFile) throws IOException {
        String content = new String(Files.readAllBytes(dataFile.toPath()));

        ShareRemoteUpdateFile shareRemoteUpdateFile = new ShareRemoteUpdateFile();

        shareRemoteUpdateFile.setFileName("updates.sh");

        shareRemoteUpdateFile.setData(new byte[(int) content.length()]);

        FileInputStream attStream = new FileInputStream(dataFile);

        attStream.read(shareRemoteUpdateFile.getData());

        attStream.close();

        shareRemoteUpdateFile.setData(shareRemoteUpdateFile.getData());

        return shareRemoteUpdateFile;
    }

}
