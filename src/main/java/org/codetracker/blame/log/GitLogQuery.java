package org.codetracker.blame.log;

import org.eclipse.jgit.lib.Repository;

import java.util.Objects;

public class GitLogQuery {
    Repository repository;
    String filePath;
    String commitId;

    public GitLogQuery(Repository repository, String filePath, String commitId) {
        this.repository = repository;
        this.filePath = filePath;
        this.commitId = commitId;
    }

    public Repository getRepository() {
        return repository;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GitLogQuery that = (GitLogQuery) o;
        return Objects.equals(repository, that.repository) && Objects.equals(filePath, that.filePath) && Objects.equals(commitId, that.commitId);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(repository);
        result = 31 * result + Objects.hashCode(filePath);
        result = 31 * result + Objects.hashCode(commitId);
        return result;
    }
}
