package org.codetracker.blame.fromDiffer.gitlog;

import org.codetracker.blame.log.GitLogQuery;
import org.codetracker.blame.log.PrevCommitInfo;
import org.codetracker.blame.util.FileNotFoundInThePrevCommitException;

public interface IPrevCommitFinder {
    PrevCommitInfo getPrevCommitInfo(GitLogQuery query) throws FileNotFoundInThePrevCommitException;
}
