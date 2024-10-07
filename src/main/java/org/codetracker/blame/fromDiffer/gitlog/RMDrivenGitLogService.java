package org.codetracker.blame.fromDiffer.gitlog;

import gr.uom.java.xmi.UMLModel;
import gr.uom.java.xmi.diff.*;
import org.codetracker.blame.log.GitLogQuery;
import org.codetracker.blame.log.PrevCommitInfo;
import org.codetracker.blame.util.FileNotFoundInThePrevCommitException;
import org.codetracker.blame.util.GithubUtils;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.refactoringminer.api.GitService;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.util.GitServiceImpl;

import java.io.IOException;
import java.util.*;

import static org.codetracker.blame.util.Utils.gitService;
import static org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl.*;

/* Created by pourya on 2024-10-17*/
public class RMDrivenGitLogService extends BaseGitLogService {
    @Override
    public PrevCommitInfo getPrevCommitInfo(GitLogQuery query) throws FileNotFoundInThePrevCommitException {
        try {
            PrevCommitInfo prevCommitInfo = super.getPrevCommitInfo(query);
            return prevCommitInfo;
        }
        catch (FileNotFoundInThePrevCommitException e) {
            String stop;
            if (!commits.isEmpty())
                stop = commits.get(commits.size() - 1).name();
            else
                stop = query.getCommitId();
            String srcPath = getSrcPath(query);
            if (srcPath == null) {
                throw new FileNotFoundInThePrevCommitException();
            }
            RevCommit parentCommit;
            try {
                parentCommit = getParentCommit(query.getRepository(), stop);
            } catch (Exception ex) {
                throw new FileNotFoundInThePrevCommitException();
            }
            return new PrevCommitInfo(parentCommit.getName(), srcPath);
        }
    }

    private static String getSrcPath(GitLogQuery query) {
        String srcPath = null;
        GitService gitService = new GitServiceImpl();
        Repository repository = query.getRepository();
        String commitId = query.getCommitId();
        RevWalk walk = new RevWalk(repository);
        List<MoveSourceFolderRefactoring> moveSourceFolderRefactorings = null;
        try {
            RevCommit currentCommit = walk.parseCommit(repository.resolve(commitId));
            if (currentCommit.getParentCount() > 0) {
                walk.parseCommit(currentCommit.getParent(0));
                Set<String> filePathsBefore = new LinkedHashSet<String>();
                Set<String> filePathsCurrent = new LinkedHashSet<String>();
                Map<String, String> renamedFilesHint = new HashMap<String, String>();
                gitService.fileTreeDiff(repository, currentCommit, filePathsBefore, filePathsCurrent, renamedFilesHint);

                Set<String> repositoryDirectoriesBefore = new LinkedHashSet<String>();
                Set<String> repositoryDirectoriesCurrent = new LinkedHashSet<String>();
                Map<String, String> fileContentsBefore = new LinkedHashMap<String, String>();
                Map<String, String> fileContentsCurrent = new LinkedHashMap<String, String>();
                // If no java files changed, there is no refactoring. Also, if there are
                // only ADD's or only REMOVE's there is no refactoring
                if (!filePathsBefore.isEmpty() && !filePathsCurrent.isEmpty() && currentCommit.getParentCount() > 0) {
                    RevCommit parentCommit = currentCommit.getParent(0);
                    populateFileContents(repository, parentCommit, filePathsBefore, fileContentsBefore, repositoryDirectoriesBefore);
                    populateFileContents(repository, currentCommit, filePathsCurrent, fileContentsCurrent, repositoryDirectoriesCurrent);
                    moveSourceFolderRefactorings = processIdenticalFiles(fileContentsBefore, fileContentsCurrent, renamedFilesHint, true);
                    for (MoveSourceFolderRefactoring refactoring : moveSourceFolderRefactorings) {
                        if (refactoring instanceof MoveSourceFolderRefactoring) {
                            MoveSourceFolderRefactoring moveSourceFolderRefactoring = (MoveSourceFolderRefactoring) refactoring;
                            for (Map.Entry<String, String> stringStringEntry : moveSourceFolderRefactoring.getIdenticalFilePaths().entrySet()) {
                                if (stringStringEntry.getValue().equals(query.getFilePath())) {
                                    srcPath = stringStringEntry.getKey();
                                    return srcPath;
                                }
                            }
                        }
                    }
                    UMLModel parentUMLModel = createModelForASTDiff(fileContentsBefore, repositoryDirectoriesBefore);
                    UMLModel currentUMLModel = createModelForASTDiff(fileContentsCurrent, repositoryDirectoriesCurrent);
                    UMLModelDiff modelDiff = parentUMLModel.diff(currentUMLModel);
                    List<UMLClassBaseDiff> resources = new ArrayList<>();
                    resources.addAll(modelDiff.getClassRenameDiffList());
                    resources.addAll(modelDiff.getClassMoveDiffList());
                    for (UMLClassBaseDiff classDiff : resources) {
//                        System.out.println(classDiff.getNextClass().getLocationInfo().getFilePath());
//                        System.out.println(classDiff.getOriginalClass().getLocationInfo().getFilePath());
//                        System.out.println(query.getFilePath());
//                        System.out.println("--0---");
                        if (classDiff.getNextClass().getLocationInfo().getFilePath().equals(query.getFilePath())) {
                            srcPath = classDiff.getOriginalClass().getLocationInfo().getFilePath();
                            return srcPath;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return srcPath;
    }

    public RevCommit getParentCommit(Repository repository, String commitId) throws Exception {
        // Resolve the commit ID to an actual commit
        ObjectId commitObjectId = repository.resolve(commitId);

        // Initialize RevWalk to traverse the commit history
        try (RevWalk revWalk = new RevWalk(repository)) {
            // Parse the commit from the resolved ObjectId
            RevCommit commit = revWalk.parseCommit(commitObjectId);

            // Get the parent commit (assuming there is one)
            if (commit.getParentCount() > 0) {
                RevCommit parentCommit = revWalk.parseCommit(commit.getParent(0).getId());
                return parentCommit;
            } else {
                System.out.println("The specified commit has no parent.");
                return null;
            }
        }
    }
}

