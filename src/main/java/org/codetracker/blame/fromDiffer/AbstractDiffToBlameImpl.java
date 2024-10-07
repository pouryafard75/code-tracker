package org.codetracker.blame.fromDiffer;


import org.codetracker.blame.fromDiffer.gitlog.IPrevCommitFinder;
import org.codetracker.blame.impl.differ.ILineDiffer;
import org.codetracker.blame.log.GitLogQuery;
import org.codetracker.blame.log.PrevCommitInfo;
import org.codetracker.blame.model.IBlame;
import org.codetracker.blame.model.LineBlameResult;
import org.codetracker.blame.util.FileNotFoundInThePrevCommitException;
import org.codetracker.blame.util.Utils;
import org.eclipse.jgit.lib.Repository;

import java.io.IOException;
import java.util.*;

import static org.codetracker.blame.util.Utils.*;

/* Created by pourya on 2024-10-09*/
public abstract class AbstractDiffToBlameImpl implements IBlame {
    final boolean ignore_whitespace;
    final ILineDiffer differ;
    final IPrevCommitFinder prevCommitFinder;
    protected AbstractDiffToBlameImpl(boolean ignoreWhitespace, ILineDiffer differ, IPrevCommitFinder prevCommitFinder){
        this.ignore_whitespace = ignoreWhitespace;
        this.differ = differ;
        this.prevCommitFinder = prevCommitFinder;
    }
    @Override
    public List<LineBlameResult> blameFile(Repository repository, String commitId, String filePath) throws Exception {
        return blameFile(differ, repository, commitId, filePath, null, new ArrayList<>(), commitId);
    }

    protected List<LineBlameResult> blameFile(ILineDiffer differ, Repository repository, String commitId, String filePath, List<LineBlameResult> lineBlameResults, List<LineBlameResult> todos, String startCommit) throws Exception {
        boolean firstInvo = false;
        if (lineBlameResults == null) {
            firstInvo = true;
            lineBlameResults = new ArrayList<>();
        }
        if (!firstInvo && todos.isEmpty()) return lineBlameResults;  // If no to-do items, return immediately


        List<String> fileContentByCommit;
        List<String> prevCommitCorrespondingFileContent;
        String prevCommitId = null;
        String prevCommitCorrespondingFile;
        try{
            PrevCommitInfo prevCommitInfo = prevCommitFinder.getPrevCommitInfo(new GitLogQuery(repository, filePath, commitId));
//            System.out.println(prevCommitInfo);
            prevCommitId = prevCommitInfo.getPrevCommitId();
            fileContentByCommit = Utils.getFileContentByCommit(repository, commitId, filePath);
            if (prevCommitId == null || prevCommitId.isEmpty()) throw new FileNotFoundInThePrevCommitException();
            if (prevCommitId.equals(commitId)) throw new FileNotFoundInThePrevCommitException();
            prevCommitCorrespondingFile = prevCommitInfo.getPrevFilePath();
            prevCommitCorrespondingFileContent = getFileContentByCommit(repository, prevCommitId, prevCommitCorrespondingFile);
        }
        catch (FileNotFoundInThePrevCommitException e)
        {
            //Step 4: it means all the todos must be coming from the commit itself as an introduction
            if (firstInvo)
                processIntroduction(repository, commitId, filePath, lineBlameResults, prevCommitId);
            processTermination(repository, commitId, filePath, lineBlameResults, todos, prevCommitId);
            return lineBlameResults;
        }
        Map<Integer, Integer> diffMap;
        try {
            diffMap = differ.getDiffMap(prevCommitCorrespondingFileContent, fileContentByCommit);
        }
        catch (Exception e)
        {
            System.out.println("Error in getting diff map: " + e.getMessage());
            return lineBlameResults;
        }
        if (firstInvo) {
            // Step 1: Process lines present in diffMap (modified lines)
            processModifiedLines(repository, commitId, filePath, lineBlameResults, todos, diffMap, prevCommitCorrespondingFileContent, fileContentByCommit, prevCommitId);

            // Step 2: Process lines NOT in diffMap (added lines)
            processAddedLines(repository, commitId, filePath, lineBlameResults, fileContentByCommit, diffMap, prevCommitId);
        }

        else if (!todos.isEmpty()){
            // Step 3: Process to-do items and trace their history further back
            processTodos(repository, lineBlameResults, todos, diffMap, prevCommitCorrespondingFileContent, fileContentByCommit, prevCommitId, prevCommitCorrespondingFile, filePath);
        }
        //Recursive pass: trace lines in the to-do list further back
        blameFile(differ, repository, prevCommitId, prevCommitCorrespondingFile, lineBlameResults, todos, startCommit);


        return lineBlameResults;

    }

    protected abstract void processIntroduction(Repository repository, String commitId, String filePath, List<LineBlameResult> lineBlameResults, String parentCommitId) throws Exception;

    protected abstract void processTermination(Repository repository, String commitId, String filePath, List<LineBlameResult> lineBlameResults, List<LineBlameResult> todos, String parentCommitId);

    protected abstract void processModifiedLines(Repository repository, String commitId, String filePath, List<LineBlameResult> lineBlameResults, List<LineBlameResult> todos, Map<Integer, Integer> diffMap, List<String> prevCommitCorrespondingFileContent, List<String> fileContentByCommit, String parentCommitId);

    protected abstract void processAddedLines(Repository repository, String commitId, String filePath, List<LineBlameResult> lineBlameResults, List<String> fileContentByCommit, Map<Integer, Integer> diffMap, String parentCommitId);

    protected abstract void processTodos(Repository repository, List<LineBlameResult> lineBlameResults, List<LineBlameResult> todos, Map<Integer, Integer> diffMap, List<String> prevCommitCorrespondingFileContent, List<String> fileContentByCommit, String parentCommitId, String prevCommitCorrespondingFile, String filePath);
}
