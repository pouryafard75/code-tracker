package org.codetracker.blame.convertor.impl;

import org.codetracker.blame.model.IBlameTool;
import org.codetracker.blame.model.LineBlameResult;
import org.eclipse.jgit.lib.Repository;

import java.util.*;
import java.util.function.Predicate;

/* Created by pourya on 2024-09-03*/
public class BlameDifferOneWithMany extends BlameDiffer {


    protected final IBlameTool subject;

    public BlameDifferOneWithMany(Set<IBlameTool> blamerFactories, IBlameTool subject, Predicate<String> codeElementFilter) {
        super(blamerFactories, codeElementFilter);
        this.subject = subject;
    }

    @Override
    protected Map<Integer, Map<IBlameTool, String>> process(Repository repository, String commitId, String filePath, Map<Integer, Map<IBlameTool, String>> table) {
        certificate = new LinkedHashMap<>();
        table.entrySet().removeIf(entry -> {
            Map<IBlameTool, String> factories = entry.getValue();
            String subject_value = factories.get(subject);
            Predicate<String> stringPredicate = value -> value.equals(subject_value);

            //I need to record, for each entry, what tools agree with the subject, and store the names as the certificate
            factories.forEach((tool, value) -> {
                if (stringPredicate.test(value)) {
                    certificate.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).add(tool.getToolName());
                }
            });
            return factories.values().stream().filter(stringPredicate).count() > 1;
        });
        return table;
    }

    @Override
    protected boolean verify(Map<IBlameTool, List<LineBlameResult>> results) {
        return true; //TODO:
    }
}
