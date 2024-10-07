package org.codetracker.blame.convertor;

import org.apache.commons.io.FileUtils;
import org.codetracker.blame.fromDiffer.gitlog.RMDrivenGitLogService;
import org.codetracker.blame.fromDiffer.gitlog.RMDrivenGitLogServiceWithCache;
import org.codetracker.blame.log.GitLogQuery;
import org.codetracker.blame.log.PrevCommitInfo;
import org.codetracker.blame.model.IBlameTool;
import org.codetracker.blame.model.LineBlameResult;
import org.codetracker.blame.util.BlameFormatter;
import org.codetracker.blame.util.TabularPrint;
import org.codetracker.blame.util.Utils;
import org.eclipse.jgit.lib.Repository;
import org.refactoringminer.astDiff.utils.URLHelper;

import java.io.File;
import java.util.List;

import static org.codetracker.blame.util.Utils.getRepository;

/* Created by pourya on 2024-10-06*/
public class Example {
    private static String url =
//            "https://github.com/junit-team/junit4/commit/7a3e99635d7ffcc4d730f27835eeaeb082003199";
//            "https://github.com/pouryafard75/TestCases/commit/bc42c5af4e3496f539d24b6d850288407f96d9f4";
//            "https://github.com/pouryafard75/TestCases/commit/457cf89b923a81aac29f701728ea7e88b3cb87b9";
//            "https://github.com/pouryafard75/TestCases/commit/c09c3ef57fc20875c6d92cb57e7ee316c13a24b0";
//            "https://github.com/pouryafard75/TestCases/commit/ce76c035d24fb4c78ac4d21de18fc2329966199a";
//            "https://github.com/spring-projects/spring-framework/commit/b325c74216fd9564a36602158fa1269e2e832874";
            "https://github.com/junit-team/junit5/commit/77cfe71e7f787c59626198e25350545f41e968bd";



        private static String filePath =
//            "src/main/java/org/junit/runners/BlockJUnit4ClassRunner.java";
//            "blameTest/po.java"
//            "spring-webmvc/src/main/java/org/springframework/web/servlet/mvc/method/annotation/AbstractMessageConverterMethodProcessor.java";
            "junit-jupiter-engine/src/main/java/org/junit/jupiter/engine/descriptor/ClassTestDescriptor.java";




    public static void main(String[] args) throws Exception {
        String commitID = URLHelper.getCommit(url);
        Repository repo = getRepository(url);
        writeToolOutput(repo, commitID, filePath, BlamersEnum.FileTrackerBlame);
        writeToolOutput(repo, commitID, filePath, BlamersEnum.HistBlameIW);

//        List<LineBlameResult> lineBlameResults = new LHDiffBlame().blameFile(repo, commitID, filePath);
//        TabularPrint.make(new BlameFormatter(Utils.getFileContentByCommit(repo, commitID, filePath)).make(lineBlameResults));

//        if (true) return;
//        writeToolOutput(repo, commitID, filePath, BlamersEnum.FileTrackerBlame);
//        writeToolOutput(repo, commitID, filePath, getCliBlameTool(true, new String[]{"-M"}));
//        writeToolOutput(repo, commitID, filePath, getCliBlameTool(true, new String[]{"-M100"}));
//        writeToolOutput(repo, commitID, filePath, getCliBlameTool(true, new String[]{"-M200"}));
//        writeToolOutput(repo, commitID, filePath, getCliBlameTool(true, new String[]{"-M400"}));
//        writeToolOutput(repo, commitID, filePath, getCliBlameTool(true, new String[]{"-M1000"}));
//        writeToolOutput(repo, commitID, filePath, getCliBlameTool(true, new String[]{"-C"}));
//        writeToolOutput(repo, commitID, filePath, getCliBlameTool(true, new String[]{"-C100"}));
//        writeToolOutput(repo, commitID, filePath, getCliBlameTool(true, new String[]{"-C200"}));
//        writeToolOutput(repo, commitID, filePath, getCliBlameTool(true, new String[]{"-C400"}));
//        writeToolOutput(repo, commitID, filePath, getCliBlameTool(true, new String[]{"-C1000"}));
//
//        writeToolOutput(repo, commitID, filePath, getCliBlameTool(false, new String[]{"-M"}));
//        writeToolOutput(repo, commitID, filePath, getCliBlameTool(false, new String[]{"-M100"}));
//        writeToolOutput(repo, commitID, filePath, getCliBlameTool(false, new String[]{"-M200"}));
//        writeToolOutput(repo, commitID, filePath, getCliBlameTool(false, new String[]{"-M400"}));
//        writeToolOutput(repo, commitID, filePath, getCliBlameTool(false, new String[]{"-M1000"}));
//        writeToolOutput(repo, commitID, filePath, getCliBlameTool(false, new String[]{"-C"}));
//        writeToolOutput(repo, commitID, filePath, getCliBlameTool(false, new String[]{"-C100"}));
//        writeToolOutput(repo, commitID, filePath, getCliBlameTool(false, new String[]{"-C200"}));
//        writeToolOutput(repo, commitID, filePath, getCliBlameTool(false, new String[]{"-C400"}));
//        writeToolOutput(repo, commitID, filePath, getCliBlameTool(false, new String[]{"-C1000"}));


    }

    public static void writeToolOutput(Repository repository, String commitId, String filePath, IBlameTool blamer) throws Exception {
        List<LineBlameResult> lineBlameResults = blamer.blameFile(repository, commitId, filePath);
        List<String[]> out = new BlameFormatter(Utils.getFileContentByCommit(repository, commitId, filePath)).make(lineBlameResults);
        FileUtils.write(
                new File("exp/" + blamer.getToolName() + ".txt"),
                TabularPrint.make(out));
    }
}
