package org.codetracker.blame.satd;

import org.apache.commons.io.FileUtils;
import org.codetracker.blame.convertor.BlamersEnum;
import org.codetracker.blame.model.LineBlameResult;
import org.codetracker.blame.util.BlameFormatter;
import org.codetracker.blame.util.TabularPrint;
import org.codetracker.blame.util.Utils;
import org.eclipse.jgit.lib.Repository;

import java.util.List;

/* Created by pourya on 2024-11-25*/
public class RMTest {
    public static void main(String[] args) throws Exception {
        String url  = "https://github.com/apache/ant/commit/";
        String commitId = "2943e6c208b4152e8d3142168c67a3a23509ba2e";
        String filePath = "proposal/myrmidon/src/test/org/apache/myrmidon/components/property/test/DefaultPropertyResolverTestCase.java";


        Repository repository = Utils.getRepository(url);
        List<LineBlameResult> lineBlameResults = BlamersEnum.FileTrackerBlameNoSerialization.blameFile(
                repository,
                commitId,
                filePath
        );
        List<String[]> make = new BlameFormatter(
                Utils.getFileContentByCommit(repository, commitId, filePath)
        ).make(lineBlameResults);
        String out = TabularPrint.make(make);
        FileUtils.write(new java.io.File("out.txt"), out);
    }

}
