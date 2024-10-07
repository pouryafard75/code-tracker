package org.codetracker.blame.convertor.model;

import org.codetracker.blame.model.CodeElementWithRepr;
import org.codetracker.blame.model.IBlameTool;

import java.util.LinkedHashMap;
import java.util.Map;

/* Created by pourya on 2024-11-21*/
public class BlameDifferResultWithStats extends BlameDifferResult {
    Map<IBlameTool, Stats> statsMap = new LinkedHashMap<>();
    public BlameDifferResultWithStats(BlameDifferResult blameDifferResult, Map<IBlameTool, Stats> statsMap ) {
        super(blameDifferResult.getTable(), blameDifferResult.getCodeElementWithReprMap(), blameDifferResult.getLegitSize());
        this.statsMap = statsMap;
    }
}
