package org.codetracker.blame.benchmark;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jgit.lib.Repository;
import org.refactoringminer.api.GitService;
import org.refactoringminer.astDiff.models.ASTDiff;
import org.refactoringminer.astDiff.utils.URLHelper;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.util.GitServiceImpl;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import static org.codetracker.blame.util.Utils.getOwner;
import static org.codetracker.blame.util.Utils.getProject;

/* Created by pourya on 2024-07-14*/
public class Runner {
    private static final GitService gitService = new GitServiceImpl();
    private static final String REPOS_PATH = System.getProperty("user.dir") + "/tmp";
    private static String perfectCasesPath = "/Users/pourya/IdeaProjects/RM-ASTDiff/src/test/resources/astDiff/commits/cases.json";
    private static String blameInfosPath = "blameInfos.json";


    public static Set<ASTDiff> getProjectDiffLocally(String url) throws Exception {
        String REPOS = "/Users/pourya/IdeaProjects/RM-ASTDiff" + "/src/test/resources/oracle/commits";
        String repo = URLHelper.getRepo(url);
        String commit = URLHelper.getCommit(url);

        return new GitHistoryRefactoringMinerImpl().diffAtCommitWithGitHubAPI(repo, commit, new File(REPOS)).getDiffSet();
    }
    static Set<CaseInfo> getCases(String path){
        ObjectMapper mapper = new ObjectMapper();
        Set<CaseInfo> loaded = null;
        try {
            loaded = mapper.readValue(new File(path), new TypeReference<Set<CaseInfo>>(){});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return loaded;
    }
    static Set<BlameCaseInfo> getBlameCases(){
        ObjectMapper mapper = new ObjectMapper();
        Set<BlameCaseInfo> loaded = null;
        try {
            loaded = mapper.readValue(new File(blameInfosPath), new TypeReference<Set<BlameCaseInfo>>(){});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return loaded;
    }


    public static Set<BlameCaseInfo> makeBlameBenchmarkCases() throws Exception {
        Set<CaseInfo> cases = getCases(perfectCasesPath);
        Set<BlameCaseInfo> blameCaseInfos = new LinkedHashSet<>();
        for (CaseInfo info : cases) {
            String url = info.makeURL();
            for (ASTDiff astDiff : getProjectDiffLocally(url)) {
                String filePath = astDiff.getDstPath();
                blameCaseInfos.add(new BlameCaseInfo(url, filePath));
            }
        }
        return blameCaseInfos;
    }
    public static void writeBlameInfos() throws Exception {
        Set<BlameCaseInfo> blameCaseInfos = makeBlameBenchmarkCases();
        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(blameInfosPath), blameCaseInfos);
    }

    public static void main(String[] args) throws Exception {
        run();
    }
    static void run() throws Exception {
        StatsCollector statsCollector = new StatsCollector();
        int  i = 0;
        for (BlameCaseInfo blameCase : getBlameCases()) {
            i ++;
            System.out.println("Processing " + blameCase.url);
            if (i == 4) break;
            process(blameCase.url, blameCase.filePath, statsCollector);
        }

    }
    public static void process(String url, String filePath, StatsCollector statsCollector) throws Exception {
        {
            String commitId = URLHelper.getCommit(url);
            String owner = getOwner(url);
            String project = getProject(url);
            String ownerSlashProject = owner + "/" + project;
            Repository repository = gitService.cloneIfNotExists(REPOS_PATH + "/" + ownerSlashProject, URLHelper.getRepo(url));
            BlameDiffer blameDiffer = new BlameDiffer(repository, commitId, filePath);
            Map<Integer, EnumMap<BlamerFactory, String>> result = blameDiffer.diff();
            statsCollector.process(blameDiffer);
            new CsvWriter(owner, project, commitId, filePath, blameDiffer.getCodeElementMap()).writeToCSV(result);
            statsCollector.writeInfo();
        }
    }


    static class CaseInfo implements Serializable {
        String repo;
        String commit;
        public String makeURL() {
            String infix = (this.repo.contains(".git")) ? "/commit/" : "";

            return this.repo.replace(".git","") + infix + this.commit;
        }

        public CaseInfo() {
        }

        public CaseInfo(String repo, String commit) {
            this.repo = repo;
            this.commit = commit;
        }

        public String getRepo() {
            return repo;
        }

        public void setRepo(String repo) {
            this.repo = repo;
        }

        public String getCommit() {
            return commit;
        }

        public void setCommit(String commit) {
            this.commit = commit;
        }
    }

    static class BlameCaseInfo implements Serializable {
        String url;
        String filePath;

        public BlameCaseInfo() {
        }

        public BlameCaseInfo(String url, String filePath) {
            this.url = url;
            this.filePath = filePath;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            BlameCaseInfo that = (BlameCaseInfo) o;

            if (!Objects.equals(url, that.url)) return false;
            return Objects.equals(filePath, that.filePath);
        }

        @Override
        public int hashCode() {
            int result = url != null ? url.hashCode() : 0;
            result = 31 * result + (filePath != null ? filePath.hashCode() : 0);
            return result;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }
    }
}
