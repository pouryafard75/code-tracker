package org.codetracker.blame.convertor.impl.util;

import org.apache.commons.io.FileUtils;
import org.codetracker.blame.model.IBlameTool;
import org.codetracker.blame.model.LineBlameResult;
import org.codetracker.blame.util.BlameFormatter;
import org.codetracker.blame.util.TabularPrint;
import org.eclipse.jgit.lib.Repository;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/* Created by pourya on 2024-10-17*/
public class BlameWriter extends AbstractWriter {

    public BlameWriter(Repository repository, String commitId, String filePath) {
        super(repository, commitId, filePath);
    }

    public void dump(Map<IBlameTool, List<LineBlameResult>> blameResults, List<String> content) throws IOException {
        for (Map.Entry<IBlameTool, List<LineBlameResult>> entry : blameResults.entrySet()) {
            List<LineBlameResult> value = entry.getValue();
            BlameFormatter formatter = new BlameFormatter(content, true);
            String res = null;
            try {
                res = TabularPrint.make(formatter.make(value));
            }
            catch (RuntimeException e){
                System.out.println("Error in formatting blame results for " + entry.getKey().getToolName());
                e.printStackTrace();
                continue;
            }
            File file = makeFile(entry.getKey().getToolName() + ".txt");
            FileUtils.write(file, res);
        }
    }

}
