package org.mz.csaude.dbsyncfeatures.updates.manager.service;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.mz.csaude.dbsyncfeatures.core.manager.utils.Utils;
import org.mz.csaude.dbsyncfeatures.updates.manager.model.ScriptInfo;
import org.mz.csaude.dbsyncfeatures.updates.manager.model.UpdateQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class UpdateQueuePollingProcessor implements Processor {

    private String outPutFolder;
    private static final Logger logger = LoggerFactory.getLogger(UpdateQueuePollingProcessor.class);

    public UpdateQueuePollingProcessor(String outPutFolder) {
        this.outPutFolder = outPutFolder;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        UpdateQueue updateQueue = exchange.getIn().getBody(UpdateQueue.class);


        if (updateQueue.getSitesToUpdatePath() == null && !updateQueue.isUpdateAll()) {
            logger.info("No sites defined to update");
            return;
        }

        // Validate if sitesToUpdate exists or as content

        if (updateQueue.getSitesToUpdatePath() != null) {
            Utils.validateScriptFile(updateQueue.getSitesToUpdatePath());
        }
        // Validate if scriptData exists or as content
        Utils.validateScriptFile(updateQueue.getScriptPath());

        ScriptInfo scriptInfo   = new ScriptInfo();

        String scriptData = Utils.readFileContent(updateQueue.getScriptPath());
        String sitesToUpdate = Utils.readFileContent(updateQueue.getSitesToUpdatePath());
        List<String> sitesToUpdateList = Arrays.asList(sitesToUpdate.split("\\R"));

        scriptInfo.setSitesToUpdate(sitesToUpdateList);
        scriptInfo.setScriptData(scriptData);
        scriptInfo.setUpdateAll(updateQueue.isUpdateAll());


        this.writeToFile(scriptInfo,  this.outPutFolder, getScriptFileName(updateQueue.getScriptPath()));
    }

    private void writeToFile(ScriptInfo scriptInfo, String outPutFolder, String scriptFileName) {
        String path = outPutFolder + "/" + scriptFileName;
        File  file = new File(path);
        Utils.writeObjectToFile(scriptInfo, file);
    }

    private String getScriptFileName(String filePath) {
        Path path = Paths.get(filePath);
        return path.getFileName().toString();
    }

}
