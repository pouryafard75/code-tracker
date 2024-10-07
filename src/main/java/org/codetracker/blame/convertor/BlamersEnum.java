package org.codetracker.blame.convertor;

import org.codetracker.blame.fromDiffer.BaseDiffToBlameImpl;
import org.codetracker.blame.impl.*;
import org.codetracker.blame.impl.differ.histogram.CustomGitDiff;
import org.codetracker.blame.impl.differ.ld.LDiff;
import org.codetracker.blame.impl.differ.lh.LHDiff;
import org.codetracker.blame.impl.differ.sd.SDiff;
import org.codetracker.blame.model.IBlame;
import org.codetracker.blame.model.IBlameTool;
import org.codetracker.blame.model.LineBlameResult;
import org.eclipse.jgit.diff.DiffAlgorithm;
import org.eclipse.jgit.lib.Repository;

import java.util.List;

public enum BlamersEnum implements IBlameTool {
    JGitBlameWithFollow(new JGitBlame()),
    JGitBlameHistogramWithFollow(new JGitBlame(DiffAlgorithm.getAlgorithm(DiffAlgorithm.SupportedAlgorithm.HISTOGRAM))),
    CliGitBlameIgnoringWhiteSpace(new CliGitBlameCustomizable(true)),
    CliGitBlameDefault(new CliGitBlameCustomizable(false)),
    CliGitBlameMoveAware(new CliGitBlameCustomizable(false, new String[]{"-M"})),
    CliGitBlameMoveAwareIgnoringWhiteSpace(new CliGitBlameCustomizable(true, new String[]{"-M"})),
    CliGitBlameCopyAware(new CliGitBlameCustomizable(false, new String[]{"-C"})),
    CliGitBlameCopyAwareIgnoringWhiteSpace(new CliGitBlameCustomizable(true, new String[]{"-C"})),
//    CodeTrackerBlame(new CodeTrackerBlame()), //Too slow
    FileTrackerBlame(new FileTrackerBlameWithSerialization()),
    FileTrackerBlameNoSerialization(new FileTrackerBlame()),

    LHBlame(new BaseDiffToBlameImpl(true, new LHDiff())),
    LBlame(new BaseDiffToBlameImpl(true, new LDiff())),
    SBlame(new BaseDiffToBlameImpl(true, new SDiff())),
    HistBlame(new BaseDiffToBlameImpl(false, new CustomGitDiff(DiffAlgorithm.getAlgorithm(DiffAlgorithm.SupportedAlgorithm.HISTOGRAM), false))),
    HistBlameIW(new BaseDiffToBlameImpl(true, new CustomGitDiff(DiffAlgorithm.getAlgorithm(DiffAlgorithm.SupportedAlgorithm.HISTOGRAM), true))),
    MyersBlame(new BaseDiffToBlameImpl(true, new CustomGitDiff(DiffAlgorithm.getAlgorithm(DiffAlgorithm.SupportedAlgorithm.MYERS), false))),
    MyersBlameIW(new BaseDiffToBlameImpl(true, new CustomGitDiff(DiffAlgorithm.getAlgorithm(DiffAlgorithm.SupportedAlgorithm.MYERS), true))),

    //TODO: https://github.com/UCLA-SEAL/LSDiff


    ;
    private final IBlame blamer;

    BlamersEnum(IBlame iBlame) {
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
