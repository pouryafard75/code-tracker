package org.codetracker.blame.impl.differ.ld.s2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MyHunk {
    public int start;
    public int end;
    public List<String> lines;

    public MyHunk(int start, int end, List<String> lines) {
        this.start = start;
        this.end = end;
        this.lines = lines;
    }

    @Override
    public String toString() {
        return "Hunk{" + "start=" + start + ", end=" + end + ", lines=" + lines + '}';
    }


    // Helper function to create hunks from file lines, excluding unchanged lines
    public static List<MyHunk> createHunks(List<String> fileLines, Map<Integer, Integer> unchangedLines) {
        List<MyHunk> hunks = new ArrayList<>();
        int start = -1;

        for (int i = 0; i < fileLines.size(); i++) {
            if (!unchangedLines.containsKey(i)) {
                // Start of a new hunk
                if (start == -1) {
                    start = i;
                }
            } else {
                // End of the current hunk
                if (start != -1) {
                    hunks.add(new MyHunk(start, i - 1, fileLines.subList(start, i)));
                    start = -1; // Reset
                }
            }
        }
        // Add the last hunk if it extends to the end of the file
        if (start != -1) {
            hunks.add(new MyHunk(start, fileLines.size() - 1, fileLines.subList(start, fileLines.size())));
        }
        return hunks;
    }
}
