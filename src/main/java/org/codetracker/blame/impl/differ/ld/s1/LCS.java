package org.codetracker.blame.impl.differ.ld.s1;

/* Created by pourya on 2024-10-16*/
import java.util.*;

public class LCS {
    // Method to find the LCS between two lists of lines and return a map of line numbers
    public static Map<Integer, Integer> findLCS(List<String> file1Lines, List<String> file2Lines) {
        int m = file1Lines.size();
        int n = file2Lines.size();

        // DP table for LCS
        int[][] lcsTable = new int[m + 1][n + 1];

        // Fill the LCS table
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (file1Lines.get(i - 1).equals(file2Lines.get(j - 1))) {
                    lcsTable[i][j] = lcsTable[i - 1][j - 1] + 1;
                } else {
                    lcsTable[i][j] = Math.max(lcsTable[i - 1][j], lcsTable[i][j - 1]);
                }
            }
        }

        // Backtrack to find the matching lines and create a map
        Map<Integer, Integer> lineMapping = new HashMap<>();
        int i = m, j = n;

        while (i > 0 && j > 0) {
            if (file1Lines.get(i - 1).equals(file2Lines.get(j - 1))) {
                lineMapping.put(i - 1, j - 1);  // Matching line numbers (adjusting for 0-indexing)
                i--;
                j--;
            } else if (lcsTable[i - 1][j] >= lcsTable[i][j - 1]) {
                i--;
            } else {
                j--;
            }
        }

        return lineMapping; // Returns the map with corresponding line numbers
    }
}
