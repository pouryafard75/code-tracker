package org.codetracker.blame.impl.differ.ld.s2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/* Created by pourya on 2024-10-16*/
public class CosineSimilarity implements SimilarityMetric {

    @Override
    public double calculate(String line1, String line2) {
        Map<String, Integer> freq1 = getTermFrequency(line1);
        Map<String, Integer> freq2 = getTermFrequency(line2);

        Set<String> terms = new HashSet<>(freq1.keySet());
        terms.addAll(freq2.keySet());

        double dotProduct = 0.0;
        double magnitude1 = 0.0;
        double magnitude2 = 0.0;

        for (String term : terms) {
            int tf1 = freq1.getOrDefault(term, 0);
            int tf2 = freq2.getOrDefault(term, 0);

            dotProduct += tf1 * tf2;
            magnitude1 += Math.pow(tf1, 2);
            magnitude2 += Math.pow(tf2, 2);
        }

        if (magnitude1 == 0 || magnitude2 == 0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(magnitude1) * Math.sqrt(magnitude2));
    }

    private Map<String, Integer> getTermFrequency(String line) {
        Map<String, Integer> termFrequency = new HashMap<>();
        String[] words = line.split("\\s+");

        for (String word : words) {
            termFrequency.put(word, termFrequency.getOrDefault(word, 0) + 1);
        }

        return termFrequency;
    }
}
