package org.codetracker.blame.impl.differ.ld.s3;

/* Created by pourya on 2024-10-16*/

public class LevenshteinDistance {

    public static int calculate(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        // Initialize the base cases
        for (int i = 0; i <= a.length(); i++) {
            dp[i][0] = i; // Deleting all characters
        }
        for (int j = 0; j <= b.length(); j++) {
            dp[0][j] = j; // Inserting all characters
        }

        // Fill the dp array
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }

        return dp[a.length()][b.length()];
    }
}

