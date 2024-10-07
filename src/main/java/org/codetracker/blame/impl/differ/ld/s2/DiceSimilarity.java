package org.codetracker.blame.impl.differ.ld.s2;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/* Created by pourya on 2024-10-16*/
public class DiceSimilarity implements SimilarityMetric {

    @Override
    public double calculate(String line1, String line2) {
        Set<String> set1 = new HashSet<>(Arrays.asList(line1.split("\\s+")));
        Set<String> set2 = new HashSet<>(Arrays.asList(line2.split("\\s+")));

        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        return (2.0 * intersection.size()) / (set1.size() + set2.size());
    }
}
