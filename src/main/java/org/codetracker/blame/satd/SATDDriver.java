package org.codetracker.blame.satd;

import org.apache.commons.io.FileUtils;
import org.codetracker.blame.convertor.BlamersEnum;
import org.codetracker.blame.model.IBlameTool;
import org.codetracker.blame.model.LineBlameResult;
import org.codetracker.blame.util.Utils;
import org.eclipse.jgit.lib.Repository;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.codetracker.blame.satd.ExcelUtils.*;

/* Created by pourya on 2024-11-21*/
public class SATDDriver {

    public static void main(String[] args) {
        SATDInstance satd_project = SATDEnum.TOMCAT;
        List<SATDBlameInstance> instances;
        Map<SATDBlameInstance, Map<String, String>> all = new LinkedHashMap<>();
        try {
            instances = getInstances(satd_project, readXlsxToMap(satd_project.getExcelFileName()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int counter = 0;
        for (SATDBlameInstance instance : instances) {
            counter += 1;
            String commitId = instance.commitId;
            String filePath = instance.filePath;
            int lineNumber = instance.lineNumber;
            List<LineBlameResult> lineBlameResults = null;
            Repository repo = satd_project.getRepo();
            all.put(instance, new LinkedHashMap<>());
            all.get(instance).put("Expected", instance.expectedCommitId.substring(0,9));
            for (IBlameTool blamer : blamerFactories) {
                System.out.println("Running blamer: " + blamer.getToolName() + ", " + counter + "/" + instance.excelRow);
                String blamer_result = "";
                try {
                    lineBlameResults = blamer.blameFile(
                            repo,
                            commitId,
                            filePath,
                            lineNumber, lineNumber
                    );
                    if (lineBlameResults.size() != 1)
                        blamer_result = "";
                    else {
                        LineBlameResult lineBlameResult = lineBlameResults.get(0);
                        blamer_result = lineBlameResult.getCommitId();
                    }
                } catch (Exception e) {
                    blamer_result = "";
                }
                all.get(instance).put(blamer.getToolName(), blamer_result.isEmpty() ? "" : blamer_result.substring(0,9));
            }
//            if (counter == 5) break;
        }
        writeToFile(all, satd_project.getName() + ".csv");
    }

    private static void writeToFile(Map<SATDBlameInstance, Map<String, String>> all, String filePath) {
        StringBuilder sb = new StringBuilder();
        if (all == null || all.isEmpty()) throw new RuntimeException("Empty result");
        List<String> entries = new ArrayList<>(all.values().iterator().next().keySet());
        sb.append("Repo,DeletedCommit,DeletedFile,DeletedLine");
        for (String entry : entries) {
            sb.append(",").append(entry);
        }
        sb.append("\n");
        for (Map.Entry<SATDBlameInstance, Map<String, String>> entry : all.entrySet()) {
            SATDBlameInstance key = entry.getKey();
            sb.append(key.name).append(",").append(key.commitId).append(",").append(key.filePath).append(",").append(key.lineNumber).append(",");
            int i = 0;
            for (String s : entries) {
                sb.append(entry.getValue().get(s))
                //if it's the last one, do not append ","
                .append(i == entries.size() - 1 ? "" : ",");
                i++;
            }
            sb.append("\n");
        }

        try {
            FileUtils.writeStringToFile(new File(filePath), sb.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private static List<SATDBlameInstance> getInstances(SATDInstance satd_project, List<Map<String, String>> rows) {
        List<SATDBlameInstance> instances = new ArrayList<>();
        int index = 1;
        for (Map<String, String> row : rows) {
            index += 1;
            if (!row.get(COLUMN_LABEL).equals("TP")) continue;
            row.put("index", String.valueOf(index));
            if (row.get(COLUMN_UPDATED_IN_COMMITS).isEmpty() && !row.get(COLUMN_DELETED_IN_COMMIT).isEmpty()) {
                instances.add(new SATDBlameInstance(
                        satd_project.getRepo(),
                        Utils.findParentCommitId(satd_project.getRepo(),row.get(COLUMN_DELETED_IN_COMMIT)),
                        row.get(COLUMN_DELETED_IN_FILE),
                        (int) Double.parseDouble(row.get(COLUMN_DELETED_IN_LINE)),
                        satd_project.getName(),
                        index,
                        row.get(COLUMN_CREATED_IN_COMMIT),
                        row));
            }
            else if (!row.get(COLUMN_UPDATED_IN_COMMITS).isEmpty()) {
                String created_commit = row.get(COLUMN_CREATED_IN_COMMIT);
                String created_file = row.get(COLUMN_CREATED_IN_FILE);
                String[] update_commits = row.get(COLUMN_UPDATED_IN_COMMITS).replace("[","").replace("]","").replace("'", "").split(",");
                String[] update_lines = row.get(COLUMN_UPDATED_IN_LINES).replace("[","").replace("]","").replace("'", "").split(",");
                for (int i = 0; i < update_commits.length; i++) {
                    String commit = update_commits[i].trim();
                    String line = update_lines[i].trim();
                    if (i == 0)
                        instances.add(new SATDBlameInstance(
                                satd_project.getRepo(),
                                commit,
                                created_file,
                                Integer.parseInt(line),
                                satd_project.getName(),
                                index,
                                created_commit,
                                row
                        ));
                    else
                    {
                        instances.add(new SATDBlameInstance(
                                satd_project.getRepo(),
                                commit,
                                created_file,
                                Integer.parseInt(line),
                                satd_project.getName(),
                                index,
                                update_commits[i-1].trim(),
                                row
                        ));
                    }
                }
                if (!row.get(COLUMN_DELETED_IN_COMMIT).isEmpty()) {
                    String last_update_commit = update_commits[update_commits.length - 1].trim();
                    instances.add(new SATDBlameInstance(
                            satd_project.getRepo(),
                            Utils.findParentCommitId(satd_project.getRepo(),row.get(COLUMN_DELETED_IN_COMMIT)),
                            row.get(COLUMN_DELETED_IN_FILE),
                            (int) Double.parseDouble(row.get(COLUMN_DELETED_IN_LINE)),
                            satd_project.getName(),
                            index,
                            last_update_commit,
                            row
                    ));
                }
            }
        }
        return instances;
    }

    private static Set<IBlameTool> blamerFactories =
            new LinkedHashSet<IBlameTool>()
            {{
                add(BlamersEnum.JGitBlameWithFollow);
                add(BlamersEnum.JGitBlameHistogramWithFollow);
                add(BlamersEnum.CliGitBlameIgnoringWhiteSpace);
                add(BlamersEnum.CliGitBlameDefault);
                add(BlamersEnum.CliGitBlameMoveAware);
                add(BlamersEnum.CliGitBlameMoveAwareIgnoringWhiteSpace);
                add(BlamersEnum.CliGitBlameCopyAware);
                add(BlamersEnum.CliGitBlameCopyAwareIgnoringWhiteSpace);
                add(BlamersEnum.FileTrackerBlame);
                add(BlamersEnum.LHBlame);
                add(BlamersEnum.LBlame);
//                add(BlamersEnum.SBlame);
                add(BlamersEnum.HistBlame);
                add(BlamersEnum.MyersBlame);
                add(BlamersEnum.HistBlameIW);
                add(BlamersEnum.MyersBlameIW);
            }};
}
