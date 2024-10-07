package org.codetracker.blame.satd;

import org.eclipse.jgit.lib.Repository;

import java.util.Map;

/* Created by pourya on 2024-11-25*/
public class SATDBlameInstance {
    public Repository repo;
    public String name;
    public String commitId;
    public String filePath;
    public int lineNumber;
    public int excelRow;
    public Map<String, String> row;
    public String expectedCommitId;

    public SATDBlameInstance(Repository repo, String commitId, String filePath, int lineNumber, String name, int excelRow, String expected, Map<String, String> row) {
        this.repo = repo;
        this.commitId = commitId;
        this.filePath = filePath;
        this.name = name;
        this.lineNumber = lineNumber;
        this.excelRow = excelRow;
        this.expectedCommitId = expected;
        this.row = row;

    }
}
