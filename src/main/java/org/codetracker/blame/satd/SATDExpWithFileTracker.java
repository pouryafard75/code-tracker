package org.codetracker.blame.satd;

import org.codetracker.blame.convertor.BlamersEnum;
import org.codetracker.blame.model.LineBlameResult;
import org.codetracker.blame.util.Utils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.codetracker.blame.satd.ExcelUtils.*;

/* Created by pourya on 2024-11-21*/
public class SATDExpWithFileTracker {

    public static void main(String[] args) {
        SATDInstance satd_project = SATDEnum.ANT;
        List<Map<String, String>> rows = null;
        try {
            rows = readXlsxToMap(satd_project.getExcelFileName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        int counter = 0;
        int rowIndex = 1;
        int match = 0;
        int miss = 0;
        for (Map<String, String> row : rows) {
            rowIndex += 1;
            if (row.get(COLUMN_DELETED_IN_COMMIT).isEmpty()) continue;
            counter++;
            String deleted_commit = row.get(COLUMN_DELETED_IN_COMMIT);
            String deleted_file = row.get(COLUMN_DELETED_IN_FILE);
            int deleted_line = (int) Double.parseDouble(row.get(COLUMN_DELETED_IN_LINE));
            try {
                List<LineBlameResult> lineBlameResults = null;
                try {
                    lineBlameResults = BlamersEnum.FileTrackerBlame.blameFile(
                            satd_project.getRepo(),
                            Utils.findParentCommitId(satd_project.getRepo(), deleted_commit),
                            deleted_file,
                            deleted_line, deleted_line
                    );
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                if (lineBlameResults.size() != 1)
                    throw new RuntimeException("Expected 1 line blame result, but got " + lineBlameResults.size());
                LineBlameResult lineBlameResult = lineBlameResults.get(0);
                String commitId = lineBlameResult.getCommitId();
                String expectedCommitId = row.get(COLUMN_CREATED_IN_COMMIT);

                if (!commitId.equals(expectedCommitId)) {
                    miss++;
                    System.out.println("Mismatch @ " + rowIndex + ": " + commitId + " != " + expectedCommitId);
                    if (commitId.equals("")) {
                        System.out.println(satd_project.getRepo());
                        System.out.println(Utils.findParentCommitId(satd_project.getRepo(), deleted_commit));
                        System.out.println(deleted_file);
                        System.out.println(deleted_line);
                    }
                } else {
                    match++;
                    System.out.println("Match @ " + rowIndex + ": " + commitId + " == " + expectedCommitId);
                }
            } catch (Exception e) {
                System.out.println(satd_project.getRepo());
                System.out.println(Utils.findParentCommitId(satd_project.getRepo(), deleted_commit));
                System.out.println(deleted_file);
                System.out.println(deleted_line);
                System.out.println("Error @ " + rowIndex + ": " + e.getMessage());
            }
        }
        System.out.println("Total: " + counter + ", Match: " + match + ", Miss: " + miss);
    }
}

