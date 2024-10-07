package org.codetracker.blame.impl.differ.ld.s2;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/* Created by pourya on 2024-10-16*/
public class JaccardSimilarity implements SimilarityMetric {

    @Override
    public double calculate(String line1, String line2) {
        Set<String> set1 = new HashSet<>(Arrays.asList(line1.split("\\s+")));
        Set<String> set2 = new HashSet<>(Arrays.asList(line2.split("\\s+")));

        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        return (double) intersection.size() / union.size();
    }
}
