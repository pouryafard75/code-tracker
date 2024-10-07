package org.codetracker.blame.fromDiffer.gitlog;
import org.codetracker.blame.log.GitLogQuery;
import org.codetracker.blame.log.PrevCommitInfo;
import org.codetracker.blame.util.FileNotFoundInThePrevCommitException;
import org.codetracker.blame.util.GithubUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.util.List;

/* Created by pourya on 2024-10-17*/
//No follow option
public class BaseGitLogService implements IPrevCommitFinder {
    protected List<RevCommit> commits;
    @Override
    public PrevCommitInfo getPrevCommitInfo(GitLogQuery query) throws FileNotFoundInThePrevCommitException {
        updateLog(query);
        if (commits == null)
            throw new FileNotFoundInThePrevCommitException();
        if (commits.isEmpty())
            throw new FileNotFoundInThePrevCommitException();
        RevCommit result = commits.get(0);
        if (result.getName().equals(query.getCommitId()))
        {
            if (commits.size() > 1)
                result = commits.get(1);
            else
                throw new FileNotFoundInThePrevCommitException();
        }
        return new PrevCommitInfo(result.getName(), query.getFilePath());
    }
    protected void updateLog(GitLogQuery query) {
        try {
            commits = GithubUtils.gitLogRevCommits(query);
        } catch (GitAPIException | IOException e) {
            e.printStackTrace();
        }
    }
}
