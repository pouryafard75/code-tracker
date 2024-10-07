package org.codetracker.blame.impl.differ.ld.s3;

import org.codetracker.blame.impl.differ.ld.s2.CosineSimilarity;
import org.codetracker.blame.impl.differ.ld.s2.MyHunk;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* Created by pourya on 2024-10-16*/
public class LineByLineComparator {
    // Step 3: Compare lines within similar hunks
    public static Map<Integer, Integer> performLineByLineComparison(
            Map<MyHunk, MyHunk> similarHunks, double similarityThreshold) {

        Map<Integer, Integer> lineMapping = new HashMap<>();

        for (Map.Entry<MyHunk, MyHunk> entry : similarHunks.entrySet()) {
            MyHunk hunk1 = entry.getKey();
            MyHunk hunk2 = entry.getValue();

            List<String> hunk1Lines = hunk1.lines;
            List<String> hunk2Lines = hunk2.lines;

            for (int i = 0; i < hunk1Lines.size(); i++) {
                for (int j = 0; j < hunk2Lines.size(); j++) {
                    String line1 = hunk1Lines.get(i);
                    String line2 = hunk2Lines.get(j);

                    // Calculate similarity using a line comparison algorithm (e.g., Levenshtein distance)
                    int distance = LevenshteinDistance.calculate(line1, line2);
                    int maxLen = Math.max(line1.length(), line2.length());
                    double similarity = 1.0 - (double) distance / maxLen;

                    // If similarity is above the threshold, map the lines
                    if (similarity >= similarityThreshold) {
                        lineMapping.put(hunk1.start + i, hunk2.start + j);
                    }
                }
            }
        }

        return lineMapping;
    }
}
