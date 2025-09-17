package org.codetracker.blame.convertor.impl;

import org.codetracker.blame.model.IBlameTool;
import org.eclipse.jgit.lib.Repository;

import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class NoBlameDiffer extends BlameDifferOneWithMany {
    public NoBlameDiffer(Set<IBlameTool> blamerFactories, Predicate<String> codeElementFilter) {
        super(blamerFactories, null, codeElementFilter);
    }

    @Override
    protected Map<Integer, Map<IBlameTool, String>> process(Repository repository, String commitId, String filePath, Map<Integer, Map<IBlameTool, String>> table) {
        return table;
    }
}
