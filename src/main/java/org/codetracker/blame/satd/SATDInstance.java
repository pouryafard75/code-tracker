package org.codetracker.blame.satd;

import org.eclipse.jgit.lib.Repository;

import java.util.Map;

public interface SATDInstance  {
    Repository getRepo();
    String getExcelFileName();
    String getName();
    Map<String, String> getChanges();
}
