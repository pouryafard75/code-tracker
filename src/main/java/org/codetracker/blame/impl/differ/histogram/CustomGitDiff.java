package org.codetracker.blame.impl.differ.histogram;

import java.util.List;
import java.util.Map;


import org.codetracker.blame.impl.differ.ILineDiffer;
import org.eclipse.jgit.diff.*;

import java.util.HashMap;

import static org.codetracker.blame.util.Utils.areStringsIdentical;

/* Created by pourya on 2024-10-15*/

/**
 * This class implements the Git Diff algorithm fully inspired by the JGit library.
 * I could've used the {@link org.eclipse.jgit.diff.RawText} implemented in the JGit library,
 * instead of introducing {@link CustomGitDiff.MyLinesModel}, but I felt this one is much nicer.
 */
public class CustomGitDiff implements ILineDiffer {

    private final DiffAlgorithm diffAlgorithm;
    private final boolean _ignoreWhiteSpace;

    private final SequenceComparator<? super MyLinesModel> comparator = new SequenceComparator<MyLinesModel>() {
        @Override
        public boolean equals(MyLinesModel a, int ai, MyLinesModel b, int bi) {
            String prevContent = a.lines.get(ai);
            String currContent = b.lines.get(bi);
            return areStringsIdentical(_ignoreWhiteSpace, prevContent, currContent);
//            return a.lines.get(ai).equals(b.lines.get(bi));
        }
        @Override
        public int hash(MyLinesModel a, int i) {
            return a.lines.get(i).hashCode();
        }
    };
    public CustomGitDiff(DiffAlgorithm diffAlgorithm, boolean ignoreWhiteSpace) {
        this.diffAlgorithm = diffAlgorithm;
        this._ignoreWhiteSpace = ignoreWhiteSpace;
    }

    @Override
    public Map<Integer, Integer> getDiffMap(List<String> fileContentByCommit, List<String> prevCommitCorrespondingFile)
    {
        return computeHistogramDiff(fileContentByCommit, prevCommitCorrespondingFile, diffAlgorithm);
    }

    public Map<Integer, Integer> computeHistogramDiff(List<String> original, List<String> modified, DiffAlgorithm diffAlgorithm) {
        // Use HistogramDiff algorithm to compute the diff
        EditList editList = diffAlgorithm.diff(
                comparator,
                new MyLinesModel(original),
                new MyLinesModel(modified)
        );
        return converEditListToMap(editList, original.size() - 1, modified.size() - 1);
    }

    // Method to convert EditList to a Map<Integer, Integer>, including non-affected lines
    private static Map<Integer, Integer> converEditListToMap(EditList editList, int originalLineCount, int modifiedLineCount) {
        Map<Integer, Integer> result = new HashMap<>();

        int originalLine = 0;
        int modifiedLine = 0;

        for (Edit edit : editList) {
            // Step 1: Handle non-affected lines before the edit
            while (originalLine < edit.getBeginA() && modifiedLine < edit.getBeginB()) {
                result.put(originalLine, modifiedLine);
                originalLine++;
                modifiedLine++;
            }

            // Step 2: Handle unchanged or modified lines within the edit
            int unchangedOrModifiedLines = Math.min(edit.getEndA() - edit.getBeginA(), edit.getEndB() - edit.getBeginB());
            for (int i = 0; i < unchangedOrModifiedLines; i++) {
                result.put(originalLine, modifiedLine);
                originalLine++;
                modifiedLine++;
            }

            // Step 3: Handle deleted lines in the original list (lines present in original but not in modified)
            while (originalLine < edit.getEndA()) {
//                result.put(originalLine, -1); // Line deleted in modified
                originalLine++;
            }

            // Step 4: Handle added lines in the modified list (lines present in modified but not in original)
            while (modifiedLine < edit.getEndB()) {
                // Optionally track added lines, no corresponding original line
                modifiedLine++;
            }
        }

        // Step 5: Handle non-affected lines after the last edit
        while (originalLine <= originalLineCount && modifiedLine <= modifiedLineCount) {
            result.put(originalLine, modifiedLine);
            originalLine++;
            modifiedLine++;
        }

        return result;
    }

    static class MyLinesModel extends Sequence{
        final List<String> lines;
        public MyLinesModel(List<String> lines) {
            this.lines = lines;
        }
        @Override
        public int size() {
            return (lines == null) ? 0 : lines.size();
        }
    }

}