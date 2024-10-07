package org.codetracker.blame.satd;

import org.codetracker.blame.util.Utils;
import org.eclipse.jgit.lib.Repository;

import java.util.Map;

public enum SATDEnum implements SATDInstance {
    ANT("Apache", "Ant", "SATDBailiff2-Ant.xlsx",
            Map.of()),
    TOMCAT("Apache", "Tomcat", "SATDBailiff2-Tomcat.xlsx",
            Map.of(
                    "15", "9446b3d3ff6ffb26caee17781b415bb92312e672",
                    "64", "5b44ac56c385cac48f0414b8179df7e6cf939199",
                    "72", "964588a61b974c6694f7c6b2225951f36bb2dc1e")),
    ;

    private final String owner;
    private final String project;
    private final String excelPath;

    private final String res_path = "src/main/resources/";
    private final Map<String, String> changes;

    SATDEnum(String owner, String project, String fileName, Map<String, String> changes) {
        this.owner = owner;
        this.project = project;
        this.excelPath = res_path + fileName;
        this.changes = changes;
    }


    @Override
    public String getName() {
        return this.name();
    }

    @Override
    public Map<String, String> getChanges() {
        return changes;
    }

    @Override
    public Repository getRepo() {
        try {
            return openRepo(owner, project);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Repository openRepo(String owner, String project) throws Exception {
        return Utils.gitService.openRepository(Utils.REPOS_PATH + "/" + owner + "/" + project);
    }

    @Override
    public String getExcelFileName() {
        return excelPath;
    }
}
