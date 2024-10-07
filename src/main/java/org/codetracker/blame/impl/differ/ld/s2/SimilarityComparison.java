package org.codetracker.blame.impl.differ.ld.s2;

import java.util.*;

public class SimilarityComparison {
    // Compare hunks between file1 and file2 based on the chosen similarity metric
    public static Map<MyHunk, MyHunk> mapSimilarHunks(List<MyHunk> hunks1, List<MyHunk> hunks2, SimilarityMetric metric, double threshold) {
        Map<MyHunk, MyHunk> hunkMapping = new HashMap<>();
        Set<MyHunk> usedHunks2 = new HashSet<>(); // Keep track of used hunks from file2

        for (MyHunk hunk1 : hunks1) {
            MyHunk bestMatch = null;
            double bestSimilarity = -1.0;

            for (MyHunk hunk2 : hunks2) {
                if (usedHunks2.contains(hunk2)) {
                    continue; // Skip already used hunks from file2
                }

                String text1 = String.join(" ", hunk1.lines);
                String text2 = String.join(" ", hunk2.lines);

                // Calculate similarity between hunks
                double similarity = metric.calculate(text1, text2);

                // Select the most similar hunk above a certain threshold
                if (similarity > bestSimilarity) {
                    bestSimilarity = similarity;
                    bestMatch = hunk2;
                }
            }

            if (bestMatch != null && bestSimilarity > threshold) {
                // Map hunk1 to the best match and mark it as used
                hunkMapping.put(hunk1, bestMatch);
                usedHunks2.add(bestMatch);
            }
        }

        return hunkMapping;
    }
}
