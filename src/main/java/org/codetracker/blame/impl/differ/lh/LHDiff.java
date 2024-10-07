package org.codetracker.blame.impl.differ.lh;

import org.codetracker.blame.impl.differ.ILineDiffer;

import java.util.HashMap;
import java.util.List;

/* Created by pourya on 2024-10-09*/
public class LHDiff implements ILineDiffer {

    private final com.srlab.matching.LHDiff lhDiff;
    public LHDiff() {
        this.lhDiff = new com.srlab.matching.LHDiff();
    }
    public LHDiff(com.srlab.matching.LHDiff lhDiff) {
        this.lhDiff = lhDiff;
    }

    @Override
    public void display() {
        this.lhDiff.printLine();
    }

    @Override
    public HashMap<Integer, Integer> getDiffMap(List<String> fileContentByCommit, List<String> prevCommitCorrespondingFile) {
        lhDiff.init(fileContentByCommit, prevCommitCorrespondingFile);
        lhDiff.clearMapping();;
        lhDiff.match();
        HashMap<Integer, Integer> result = new HashMap<>();
        lhDiff.getFwdMapping().forEach((key, value) -> {
            value.forEach((innerKey, innerValue) -> result.put(key, innerValue));
        });
        return result;
//        return fixOffsets(lhDiff.getDiffMap());
    }

    private HashMap<Integer, Integer> fixOffsets(@SuppressWarnings("rawtypes") HashMap diffMap) {
        HashMap<Integer, Integer> fixedDiffMap = new HashMap<>();
        for (Object key : diffMap.keySet()) {
            int keyInt = (int) key;
            int valueInt = (int) diffMap.get(key);
            fixedDiffMap.put(keyInt + 1, valueInt + 1); //to fix 0-based indexing
        }
        return fixedDiffMap;
    }
}
