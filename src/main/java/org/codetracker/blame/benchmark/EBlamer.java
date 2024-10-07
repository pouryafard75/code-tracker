package org.codetracker.blame.benchmark;

import org.codetracker.blame.impl.*;
import org.codetracker.blame.model.IBlame;
import org.codetracker.blame.model.IBlameTool;
import org.codetracker.blame.model.LineBlameResult;
import org.eclipse.jgit.diff.DiffAlgorithm;
import org.eclipse.jgit.lib.Repository;

import java.util.List;

public enum EBlamer implements IBlameTool {
    JGitBlameWithFollow(new JGitBlame()),
    JGitBlameHistogramWithFollow(new JGitBlame(DiffAlgorithm.getAlgorithm(DiffAlgorithm.SupportedAlgorithm.HISTOGRAM))),
    CliGitBlameIgnoringWhiteSpace(new CliGitBlameCustomizable(true)),
    CliGitBlameDefault(new CliGitBlameCustomizable(false)),
    CliGitBlameMoveAware(new CliGitBlameCustomizable(false, new String[]{"-M"})),
    CliGitBlameMoveAwareIgnoringWhiteSpace(new CliGitBlameCustomizable(true, new String[]{"-M"})),
    CliGitBlameCopyAware(new CliGitBlameCustomizable(false, new String[]{"-C"})),
    CliGitBlameCopyAwareIgnoringWhiteSpace(new CliGitBlameCustomizable(true, new String[]{"-C"})),
//    CodeTrackerBlame(new CodeTrackerBlame()),
    FileTrackerBlame(new FileTrackerBlameWithSerialization());
    private final IBlame blamer;

    EBlamer(IBlame iBlame) {
        this.blamer = iBlame;
    }

    @Override
    public List<LineBlameResult> blameFile(Repository repository, String commitId, String filePath) throws Exception {
        return blamer.blameFile(repository, commitId, filePath);
    }

    @Override
    public List<LineBlameResult> blameFile(Repository repository, String commitId, String filePath, int fromLine, int toLine) throws Exception {
        return blamer.blameFile(repository, commitId, filePath, fromLine, toLine);
    }

    @Override
    public String getToolName() {
        return name();
    }
}
