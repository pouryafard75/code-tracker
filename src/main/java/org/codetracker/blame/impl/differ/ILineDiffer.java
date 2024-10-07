package org.codetracker.blame.impl.differ;

import org.codetracker.blame.util.Utils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ILineDiffer {
    Map<Integer, Integer> getDiffMap(List<String> fileContentByCommit, List<String> prevCommitCorrespondingFile) throws IOException;
    default void display() {}
    default Map<Integer, Integer> getDiffMap(String fileContentByCommit, String prevCommitCorrespondingFile) throws IOException {
        return this.getDiffMap(Utils.contentToLines(fileContentByCommit), Utils.contentToLines(prevCommitCorrespondingFile));
    }
}
