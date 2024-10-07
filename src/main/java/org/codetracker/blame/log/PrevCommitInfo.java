package org.codetracker.blame.log;


import java.io.Serializable;

public class PrevCommitInfo implements Serializable {
    String prevCommitId = "";
    String prevFilePath = "";
    private static PrevCommitInfo nullInstance = new PrevCommitInfo();

    public PrevCommitInfo(String prevCommitId, String prevFilePath) {
        this.prevCommitId = prevCommitId;
        this.prevFilePath = prevFilePath;
    }
    public static PrevCommitInfo Null() {
        return nullInstance;
    }

    @Override
    public String toString() {
        return "PrevCommitInfo{" +
                "prevCommitId='" + prevCommitId + '\'' +
                ", prevFilePath='" + prevFilePath + '\'' +
                '}';
    }

    public String getPrevCommitId() {
        return prevCommitId;
    }

    public String getPrevFilePath() {
        return prevFilePath;
    }

    public PrevCommitInfo() {
    }

    public void setPrevCommitId(String prevCommitId) {
        this.prevCommitId = prevCommitId;
    }

    public void setPrevFilePath(String prevFilePath) {
        this.prevFilePath = prevFilePath;
    }
}
