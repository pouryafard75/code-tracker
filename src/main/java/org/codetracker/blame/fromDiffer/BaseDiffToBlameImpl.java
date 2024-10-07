package org.codetracker.blame.fromDiffer;

import org.codetracker.blame.fromDiffer.gitlog.IPrevCommitFinder;
import org.codetracker.blame.fromDiffer.gitlog.RMDrivenGitLogServiceWithSerialization;
import org.codetracker.blame.impl.differ.ILineDiffer;
import org.codetracker.blame.model.LineBlameResult;
import org.codetracker.blame.util.Utils;
import org.eclipse.jgit.lib.Repository;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.codetracker.blame.util.Utils.*;

/* Created by pourya on 2024-10-09*/
public class BaseDiffToBlameImpl extends AbstractDiffToBlameImpl {
    public BaseDiffToBlameImpl(boolean ignoreWhitespace, ILineDiffer differ, IPrevCommitFinder prevCommitFinder) {
        super(ignoreWhitespace, differ, prevCommitFinder);
    }

    public BaseDiffToBlameImpl(boolean ignoreWhitespace, ILineDiffer differ) {
        super(ignoreWhitespace, differ, new RMDrivenGitLogServiceWithSerialization());
    }

    @Override
    protected void processIntroduction(Repository repository, String commitId, String filePath, List<LineBlameResult> lineBlameResults, String parentCommitId) throws Exception {
        List<String> fileContentByCommit = Utils.getFileContentByCommit(repository, commitId, filePath);
        for (int i = 0; i < fileContentByCommit.size(); i++) {
            lineBlameResults.add(new LineBlameResult(
                    commitId, filePath, filePath,
                    findCommitter(repository, commitId), parentCommitId, findCommitDate(repository, commitId),
                    i + 1, i + 1
            ));
        }
    }

    protected void processTermination(Repository repository, String commitId, String filePath, List<LineBlameResult> lineBlameResults, List<LineBlameResult> todos, String parentCommitId) {
//        if (true) return;
        Iterator<LineBlameResult> iterator = todos.iterator(); // Use an iterator to safely remove items from the list
        while (iterator.hasNext()) {
            LineBlameResult todo = iterator.next();
            lineBlameResults.add(new LineBlameResult(
                    commitId, filePath, todo.getBeforeFilePath(),
                    findCommitter(repository, commitId), parentCommitId, findCommitDate(repository, commitId),
                    todo.getResultLineNumber(), todo.getOriginalLineNumber()
            ));
            iterator.remove();
        }
    }

    protected void processTodos(Repository repository, List<LineBlameResult> lineBlameResults, List<LineBlameResult> todos, Map<Integer, Integer> diffMap, List<String> prevCommitCorrespondingFileContent, List<String> fileContentByCommit, String parentCommitId, String prevCommitCorrespondingFile, String filePath) {
        Iterator<LineBlameResult> iterator = todos.iterator(); // Use an iterator to safely remove items from the list
        while (iterator.hasNext()) {
            LineBlameResult lineBlameResult = iterator.next();
            //find the key which has the value of the resultLineNumber
            Integer prevLine = null;
            for (Integer i : diffMap.keySet()) {
                if (diffMap.get(i).equals(lineBlameResult.getResultLineNumber() - 1)) {
                    prevLine = i;
                    break;
                }
            }

            if (prevLine != null) {
                String prevContent = null;
                prevContent = prevCommitCorrespondingFileContent.get(prevLine);
                String currContent = fileContentByCommit.get(lineBlameResult.getResultLineNumber() - 1);

                // If the content matches, continue tracing back
                if (areStringsIdentical(ignore_whitespace, prevContent, currContent)) {
                    lineBlameResult.setCommitDate(findCommitDate(repository, parentCommitId));
                    lineBlameResult.setCommitter(findCommitter(repository, parentCommitId));
                    lineBlameResult.setResultLineNumber(prevLine + 1);
                    lineBlameResult.setCommitId(parentCommitId);
                    lineBlameResult.setBeforeFilePath(prevCommitCorrespondingFile);
                } else {
                    // Blame this line on the current commit if content has changed
                    lineBlameResults.add(lineBlameResult);
                    lineBlameResult.setBeforeFilePath(filePath);
                    iterator.remove();
                }
            }
            else {
                lineBlameResults.add(lineBlameResult);
                iterator.remove();
            }
        }
    }

    protected void processAddedLines(Repository repository, String commitId, String filePath, List<LineBlameResult> lineBlameResults, List<String> fileContentByCommit, Map<Integer, Integer> diffMap, String parentCommitId) {
        for (int i = 0; i < fileContentByCommit.size(); i++) {
            if (!diffMap.containsValue(i)) {  // If line is not part of the diffMap
                String committer = findCommitter(repository, commitId);
                long commitDate = findCommitDate(repository, commitId);
                LineBlameResult lineBlameResult = new LineBlameResult(
                        commitId, filePath, filePath,
                        committer, parentCommitId, commitDate,
                        i + 1, i + 1
                );
                // Directly blame the parent commit for unchanged lines
                lineBlameResults.add(lineBlameResult);
            }
        }
    }

    protected void processModifiedLines(Repository repository, String commitId, String filePath, List<LineBlameResult> lineBlameResults, List<LineBlameResult> todos, Map<Integer, Integer> diffMap, List<String> prevCommitCorrespondingFileContent, List<String> fileContentByCommit, String parentCommitId) {
        for (Map.Entry<Integer, Integer> lineNumber_lineNumber : diffMap.entrySet()) {
            Integer currLine = lineNumber_lineNumber.getValue();
            Integer prevLine = lineNumber_lineNumber.getKey();
            if (currLine < 0 || prevLine < 0) {
                continue;
            }
            String prevContent = prevCommitCorrespondingFileContent.get(prevLine);
            String currContent = fileContentByCommit.get(currLine);
            // If lines are identical (ignoring whitespace), add to to-do for further tracing
            if (areStringsIdentical(ignore_whitespace, prevContent, currContent)) {
                todos.add(new LineBlameResult(
                        parentCommitId, filePath, filePath,
                        findCommitter(repository, parentCommitId), parentCommitId, findCommitDate(repository, parentCommitId),
                        prevLine + 1, currLine + 1
                ));
            } else {
                lineBlameResults.add(new LineBlameResult(
                        commitId, filePath, filePath,
                        findCommitter(repository, commitId), parentCommitId, findCommitDate(repository, commitId),
                        prevLine + 1, currLine + 1
                ));
            }
        }
    }
}
