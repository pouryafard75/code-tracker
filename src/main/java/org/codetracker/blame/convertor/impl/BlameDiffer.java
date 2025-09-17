package org.codetracker.blame.convertor.impl;

import org.codetracker.api.CodeElement;
import org.codetracker.blame.convertor.impl.record.LineNumberToCommitIDRecordManager;
import org.codetracker.blame.convertor.impl.util.BlameWriter;
import org.codetracker.blame.convertor.model.BlameDifferResult;
import org.codetracker.blame.model.CodeElementWithRepr;
import org.codetracker.blame.model.IBlameTool;
import org.codetracker.blame.model.LineBlameResult;
import org.codetracker.blame.util.Utils;
import org.codetracker.util.CodeElementLocator;
import org.eclipse.jgit.lib.Repository;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/* Created by pourya on 2024-07-14*/
public class BlameDiffer {

    protected final Set<IBlameTool> blamerFactories;
    protected LineNumberToCommitIDRecordManager benchmarkRecordManager;
//    protected BiPredicate<Integer, List<String>> emptyLinesCondition = (lineNumber, content) -> content.get(lineNumber-1).trim().isEmpty();
    protected final Predicate<String> ignoreCondition;
    protected Map<Integer, List<String>> certificate; // jic
    private boolean _dump = false;

    public Map<Integer, List<String>> getCertificate() {
        return certificate;
    }

    public void set_dump(boolean _dump) {
        this._dump = _dump;
    }

    public BlameDiffer(Set<IBlameTool> blamerFactories, Predicate<String> ignoreCondition){
        this.blamerFactories = blamerFactories;
        this.ignoreCondition = ignoreCondition;
    }

    private Map<IBlameTool, List<LineBlameResult>> runBlamers(Repository repository, String commitId, String filePath) throws Exception {
        Map<IBlameTool, List<LineBlameResult>> results = new LinkedHashMap<>();
        for (IBlameTool blamerFactory : blamerFactories) {
            System.out.println("Running " + blamerFactory);
            List<LineBlameResult> lineBlameResults = blamerFactory.blameFile
                    (repository, commitId, filePath);
            results.put(blamerFactory, lineBlameResults);
            System.out.println("Finished " + blamerFactory);
        }
        return results;
    }
    private Map<IBlameTool, List<LineBlameResult>> runBlamers(Repository repository, String commitId, String filePath, int fromLine, int toLine) throws Exception {
        Map<IBlameTool, List<LineBlameResult>> results = new LinkedHashMap<>();
        for (IBlameTool blamerFactory : blamerFactories) {
            System.out.println("Running " + blamerFactory);
            List<LineBlameResult> lineBlameResults = blamerFactory.blameFile
                    (repository, commitId, filePath, fromLine, toLine);
            results.put(blamerFactory, lineBlameResults);
            System.out.println("Finished " + blamerFactory);
        }
        return results;
    }

    protected boolean verify(Map<IBlameTool, List<LineBlameResult>> results) {
        if (results.size() != 2)
            throw new RuntimeException("BlameDiffer only works with two blamers");
        return true;
    }
    public final BlameDifferResult diff(Repository repository, String commitId, String filePath) throws Exception {
        Map<Integer, Map<IBlameTool, String>> table = generateTable(prepareResults(repository, commitId, filePath), repository, commitId, filePath);
        int legitSize = table.size();
        table =  process(repository, commitId, filePath, table);
        return getBlameDifferResult(repository, commitId, filePath, table, legitSize);
    }

    public Map<Integer, Map<IBlameTool, String>> generateTable(Map<IBlameTool, List<LineBlameResult>> repository, Repository repository1, String commitId, String filePath) throws Exception {
        Map<IBlameTool, List<LineBlameResult>> blameResults = repository;
        List<String> content = Utils.getFileContentByCommit(repository1, commitId, filePath);
        if (_dump) new BlameWriter(repository1, commitId, filePath).dump(blameResults, content);
        benchmarkRecordManager = new LineNumberToCommitIDRecordManager();
        benchmarkRecordManager.diff(blameResults);

        Map<Integer, Map<IBlameTool, String>> table = benchmarkRecordManager.getRegistry();
        table.entrySet().removeIf(entry -> ignoreCondition.test(content.get(entry.getKey() - 1)));
        return table;
    }

    public final BlameDifferResult diff(Repository repository, String commitId, String filePath, int fromLine, int toLine) throws Exception {
        Map<Integer, Map<IBlameTool, String>> table = generateTable(prepareResults(repository, commitId, filePath, fromLine, toLine), repository, commitId, filePath);
        int legitSize = table.size();
        table =  process(repository, commitId, filePath, table);
        return getBlameDifferResult(repository, commitId, filePath, table, legitSize);
    }

    public BlameDifferResult getBlameDifferResult(Repository repository, String commitId, String filePath, Map<Integer, Map<IBlameTool, String>> table, int legitSize) {
        return new BlameDifferResult(table, makeCodeElementMap(table.keySet(), repository, commitId, filePath), legitSize);
    }

    protected Map<Integer, Map<IBlameTool, String>> process(Repository repository, String commitId, String filePath, Map<Integer, Map<IBlameTool, String>> table) {
        table.entrySet().removeIf(entry -> entry.getValue().values().stream().distinct().count() == 1);
        //TODO: For this one we dont consider the merge commits;
        return table;
    }

    public Map<IBlameTool, List<LineBlameResult>> prepareResults(Repository repository, String commitId, String filePath) throws Exception {
        Map<IBlameTool, List<LineBlameResult>> blameResults = runBlamers(repository, commitId, filePath);
        verify(blameResults);
        return blameResults;
    }
    private Map<IBlameTool, List<LineBlameResult>> prepareResults(Repository repository, String commitId, String filePath, int fromLine, int toLine) throws Exception {
        Map<IBlameTool, List<LineBlameResult>> blameResults = runBlamers(repository, commitId, filePath, fromLine, toLine);
        verify(blameResults);
        return blameResults;
    }

    protected Map<Integer, CodeElementWithRepr> makeCodeElementMap(Set<Integer> lineNumbers, Repository repository, String commitId, String filePath){
//        System.out.println("Making code element map");
        Map<Integer, CodeElementWithRepr> codeElementMap = new LinkedHashMap<>();
        for (Integer lineNumber : lineNumbers) {
            try {
                CodeElement locate = new CodeElementLocator(repository, commitId, filePath, lineNumber).locate();
                codeElementMap.put(lineNumber, new CodeElementWithRepr(locate, Utils.getFileContentByCommit(repository, commitId, filePath).get(lineNumber-1)));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
//        System.out.println("Finished making code element map");
        return codeElementMap;
    }
}


