//package org.codetracker.blame.convertor;
//
//import org.codetracker.blame.convertor.impl.BlameDiffer;
//import org.codetracker.blame.convertor.model.BlameDifferResult;
//import org.codetracker.blame.convertor.model.BlameDifferResultWithStats;
//import org.codetracker.blame.convertor.model.Stats;
//import org.codetracker.blame.exp.SATDInstance;
//import org.codetracker.blame.model.IBlameTool;
//import org.codetracker.blame.model.LineBlameResult;
//import org.eclipse.jgit.lib.Repository;
//
//import java.io.IOException;
//import java.util.*;
//import java.util.function.Predicate;
//
//
///* Created by pourya on 2024-11-21*/
//public class BlameDifferWithGroundTruth extends BlameDiffer{
//
//    public BlameDifferWithGroundTruth(Set<IBlameTool> blamerFactories, SATDInstance groundTruth, Predicate<String> codeElementFilter) {
//        super(blamerFactories, codeElementFilter);
//        this.satd = groundTruth;
//    }
//
//    @Override
//    protected Map<Integer, Map<IBlameTool, String>> process(Repository repository, String commitId, String filePath, Map<Integer, Map<IBlameTool, String>> table) {
//        return table;
//    }
//
//    public BlameDifferResult getBlameDifferResult(Repository repository, String commitId, String filePath, Map<Integer, Map<IBlameTool, String>> table, int legitSize) {
//        Map<IBlameTool, Stats> statsMap = new LinkedHashMap<>();
//        BlameDifferResult blameDifferResult = super.getBlameDifferResult(repository, commitId, filePath, table, legitSize);
//        for (Map.Entry<Integer, Map<IBlameTool, String>> integerMapEntry : blameDifferResult.getTable().entrySet()) {
//            Integer ln = integerMapEntry.getKey();
//            Map<IBlameTool, String> toolStringMap = integerMapEntry.getValue();
//            for (Map.Entry<IBlameTool, String> toolStringEntry : toolStringMap.entrySet()) {
//                IBlameTool tool = toolStringEntry.getKey();
//                String blame = toolStringEntry.getValue();
//                Stats stats = statsMap.computeIfAbsent(tool, k -> new Stats());
//                if (groundTruth.getBlame(ln).equals(blame)) {
//                    stats.tp();
//                } else {
//                    stats.fp();
//                }
//            }
//        }
//        BlameDifferResultWithStats resultWithStats = new BlameDifferResultWithStats(
//                blameDifferResult,
//
//                );
//
//    }
//
//    @Override
//    protected boolean verify(Map<IBlameTool, List<LineBlameResult>> results) {
//        return super.verify(results);
//    }
//
//    public void getGroundTruth() throws IOException {
////        List<Map<String, String>> rows = readXlsxToMap(satd.getExcelFileName());
////        rows.
//    }
//}
