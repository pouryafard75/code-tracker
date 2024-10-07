package org.codetracker.blame.impl.differ.ld;

import org.codetracker.blame.impl.differ.ILineDiffer;
import org.codetracker.blame.impl.differ.ld.s1.LCS;
import org.codetracker.blame.impl.differ.ld.s2.CosineSimilarity;
import org.codetracker.blame.impl.differ.ld.s2.MyHunk;
import org.codetracker.blame.impl.differ.ld.s2.SimilarityComparison;
import org.codetracker.blame.impl.differ.ld.s2.SimilarityMetric;
import org.codetracker.blame.impl.differ.ld.s3.LevenshteinDistance;
import org.codetracker.blame.impl.differ.ld.s3.LineByLineComparator;

import java.util.*;

public class LDiff implements ILineDiffer {

    // Set a threshold for similarity
    private final double LT;
    private final double HT;
    private final SimilarityMetric metric;
    public LDiff() {
        LT = 0.8; HT = 0.8; metric = new CosineSimilarity();
    }
    public LDiff(double LT, double HT, SimilarityMetric metric) {
        this.LT = LT;
        this.HT = HT;
        this.metric = metric;
    }
    @Override
    public Map<Integer, Integer> getDiffMap(List<String> fileContentByCommit, List<String> prevCommitCorrespondingFile) {
        // Step 1: Find the LCS (unchanged lines)
        Map<Integer, Integer> unchangedLines = LCS.findLCS(fileContentByCommit, prevCommitCorrespondingFile);
//        System.out.println("Unchanged Lines (LCS): " + unchangedLines);

        // Step 2: Identify hunks not classified as unchanged and compare them using a similarity metric
        List<MyHunk> hunks1 = MyHunk.createHunks(fileContentByCommit, unchangedLines);
        List<MyHunk> hunks2 = MyHunk.createHunks(prevCommitCorrespondingFile, unchangedLines);

        Map<MyHunk, MyHunk> similarityMapping = SimilarityComparison.mapSimilarHunks(hunks1, hunks2, metric, HT);
//        System.out.println("Similar Changed Hunks (using " + metric.getClass().getSimpleName() + "): " + similarityMapping);

        // Step 3: Perform line-by-line comparison for similar hunk pairs
        Map<Integer, Integer> changedLinesMapping = LineByLineComparator.performLineByLineComparison(similarityMapping, LT);
//        System.out.println("Changed Lines Mapping: " + changedLinesMapping);

        // Combine Step 1 and Step 3 into one unified map
        Map<Integer, Integer> unifiedMapping = new HashMap<>(unchangedLines); // Start with unchanged lines
        unifiedMapping.putAll(changedLinesMapping); // Add the mapped similar lines
//        System.out.println("Final Unified Line Mapping: " + unifiedMapping);

        return unifiedMapping;
    }
}
