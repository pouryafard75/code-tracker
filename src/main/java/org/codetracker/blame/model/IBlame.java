package org.codetracker.blame.model;

import org.eclipse.jgit.lib.Repository;

import java.util.ArrayList;
import java.util.List;

/* Created by pourya on 2024-06-26*/
public interface IBlame {
    List<LineBlameResult> blameFile(Repository repository, String commitId, String filePath) throws Exception;
    default List<LineBlameResult> blameFile(Repository repository, String commitId, String filePath, int fromLine, int toLine) throws Exception {
        List<LineBlameResult> lineBlameResults = this.blameFile(repository, commitId, filePath);
        List<LineBlameResult> result = new ArrayList<>();
        for (LineBlameResult lineBlameResult : lineBlameResults) {
            if (fromLine <= lineBlameResult.getOriginalLineNumber() && lineBlameResult.getOriginalLineNumber() <= toLine) {
                result.add(lineBlameResult);
            }
        }
        return result;
    }
}
