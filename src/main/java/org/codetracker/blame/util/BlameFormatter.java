package org.codetracker.blame.util;

import org.codetracker.api.History.HistoryInfo;
import org.codetracker.blame.model.LineBlameResult;
import org.codetracker.element.BaseCodeElement;

import java.text.SimpleDateFormat;
import java.util.*;

/* Created by pourya on 2024-07-02*/
public class BlameFormatter {
    private final String NOT_FOUND_PLACEHOLDER;
    private final List<String> lines;
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
    private int fromLine;
    private boolean _disableUnnecessaryParts = true;

    public void set_disableUnnecessaryParts(boolean _disableUnnecessaryParts) {
        this._disableUnnecessaryParts = _disableUnnecessaryParts;
    }

    public BlameFormatter(List<String> lines) {
    	this("", lines);
    }
    public BlameFormatter(List<String> lines, boolean _disableUnnecessaryParts) {
        this("", lines);
        this._disableUnnecessaryParts = _disableUnnecessaryParts;
    }

    public BlameFormatter(List<String> lines, int fromLine) {
    	this("", lines);
    	this.fromLine = fromLine;
    }



    public BlameFormatter(String NOT_FOUND_PLACEHOLDER, List<String> lines) {
        this.NOT_FOUND_PLACEHOLDER = NOT_FOUND_PLACEHOLDER;
        this.lines = lines;
        this.simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT0:00"));
    }

    public List<String[]> make(List<LineBlameResult> lineBlameResults) {
        //TODO: this one will produce very wrong results in case of not having enough items in the list
        // Must be checked with the originalLineNumber of the lineBlameResult to produce better input

        List<String[]> result = new java.util.ArrayList<>();
        for (int lineNumber = 1; lineNumber <= lines.size(); lineNumber++) {
            LineBlameResult lineBlameResult = null;
            if (lineBlameResults != null) {
                int finalLineNumber = lineNumber;
                Optional<LineBlameResult> first = lineBlameResults.stream().filter(lbr -> lbr.getOriginalLineNumber() == finalLineNumber).findFirst();
                if (first.isPresent()) {
                    lineBlameResult = first.get();
                }
            }
            result.add(make(lineBlameResult, fromLine > 0 ? fromLine + lineNumber - 1 : lineNumber, lines.get(lineNumber - 1)));
        }
        return result;
    }

    public List<String[]> make(Map<Integer, HistoryInfo<? extends BaseCodeElement>> blameInfo) {
        List<String[]> result = new java.util.ArrayList<>();
        for (int lineNumber = 1; lineNumber <= lines.size(); lineNumber++) {
            HistoryInfo<? extends BaseCodeElement> historyInfo = blameInfo.get(lineNumber);
            LineBlameResult lineBlameResult = null;
            if (historyInfo != null) {
                lineBlameResult = LineBlameResult.of(historyInfo, lineNumber);
            }
            result.add(make(lineBlameResult, fromLine > 0 ? fromLine + lineNumber - 1 : lineNumber, lines.get(lineNumber - 1)));
        }
        return result;
    }

    public String[] make(LineBlameResult lineBlameResult, int lineNumber, String content) {
        String shortCommitId = NOT_FOUND_PLACEHOLDER;
        String committer = NOT_FOUND_PLACEHOLDER;
        String commitDate = NOT_FOUND_PLACEHOLDER;
        String beforeFilePath = NOT_FOUND_PLACEHOLDER;
        String resultLineNumber = String.valueOf(-1);
        if (lineBlameResult != null && !content.trim().isBlank()) {
            shortCommitId = lineBlameResult.getShortCommitId();
            beforeFilePath = lineBlameResult.getBeforeFilePath();
            committer = "(" + lineBlameResult.getCommitter();
            commitDate = lineBlameResult.getCommitDate() == 0 ? "" : simpleDateFormat.format(new Date(lineBlameResult.getCommitDate() * 1000L));
        }
        if (lineBlameResult != null) {
            resultLineNumber = String.valueOf(lineBlameResult.getResultLineNumber());
        }
        if (_disableUnnecessaryParts)
        {
            resultLineNumber = "";
            committer= "";
            commitDate = "";
            beforeFilePath = "";
        }


        return new String[]
                {
                        shortCommitId,
                        resultLineNumber,
                        beforeFilePath,
                        committer,
                        commitDate,
                        lineNumber + ")",
                        content
                };
    }

}
