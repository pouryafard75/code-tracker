package org.codetracker.blame.benchmark;

import org.codetracker.api.CodeElement;
import org.codetracker.blame.model.LineBlameResult;
import org.codetracker.blame.util.Utils;
import org.codetracker.util.CodeElementLocator;
import org.eclipse.jgit.lib.Repository;

import java.util.*;
import java.util.function.Predicate;

/* Created by pourya on 2024-07-14*/
public class BlameDiffer {

    private final EnumSet<BlamerFactory> blamerFactories = EnumSet.of(BlamerFactory.GitBlame, BlamerFactory.CodeTrackerBlame);
    private final EnumMap<BlamerFactory, List<LineBlameResult>> blameResults;
    private final List<String> content;
    private final Predicate<Integer> emptyLinesCondition;
    private final Repository repository;
    private final String commitId;
    private final String filePath;
    private final Map<Integer, CodeElement> codeElementMap = new LinkedHashMap<>();
    private Map<Integer, EnumMap<BlamerFactory, String>> table;
    private int legitSize;

    public BlameDiffer(Repository repository, String commitId, String filePath) throws Exception {
        this.repository = repository;
        this.commitId = commitId;
        this.filePath = filePath;
        this.blameResults = runBlamers(repository, commitId, filePath);
        this.content = Utils.getFileContentByCommit(repository, commitId, filePath);
        this.emptyLinesCondition = lineNumber -> content.get(lineNumber-1).trim().isEmpty();
        verify(blameResults);
        diff();

    }

    private EnumMap<BlamerFactory, List<LineBlameResult>> runBlamers(Repository repository, String commitId, String filePath) throws Exception {
        EnumMap<BlamerFactory, List<LineBlameResult>> results = new EnumMap<>(BlamerFactory.class);
        for (BlamerFactory blamerFactory : blamerFactories) {
            List<LineBlameResult> lineBlameResults = blamerFactory.getBlamer().blameFile
                    (repository, commitId, filePath);
            results.put(blamerFactory, lineBlameResults);
        }
        return results;
    }

    private static void verify(EnumMap<BlamerFactory, List<LineBlameResult>> blameResults) {
        if (blameResults.size() != 2)
            throw new RuntimeException("BlameDiffer only works with two blamers");
    }

    public Map<Integer, EnumMap<BlamerFactory, String>> diff() {
        if (table != null) return table;
        BenchmarkRecordManager<Integer, String> benchmarkRecordManager =
                new LineNumberToCommitIDRecordManager(blameResults);
        table = benchmarkRecordManager.getRegistry();
        table.entrySet().removeIf(entry -> emptyLinesCondition.test(entry.getKey()));
        legitSize = table.size();
        table.entrySet().removeIf(entry -> entry.getValue().values().stream().distinct().count() == 1);

        makeCodeElementMap(table.keySet());
        return table;
    }

    void makeCodeElementMap(Set<Integer> lineNumbers){
        for (Integer lineNumber : lineNumbers) {
            try {
                codeElementMap.put(lineNumber,
                        new CodeElementLocator(repository, commitId, filePath, lineNumber ).locate());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Map<Integer, CodeElement> getCodeElementMap() {
        return codeElementMap;
    }
    public int getLegitSize() {return legitSize;}
}
