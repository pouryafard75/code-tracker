package org.codetracker.blame.impl.differ.sd;

import org.codetracker.blame.impl.differ.ILineDiffer;
import smallChanges.lineNumberMapping.StatementMapper;
import smallChanges.statements.StatementMappingEntry;
import smallChanges.statements.StatementMappingResultSet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* Created by pourya on 2024-10-16*/
public class SDiff implements ILineDiffer {
    private final String srcVersionLeft = "1.5";
    private final String dstVersionLeft = "1.5";
    boolean useTokenEditDistance = false;
    boolean useMinOfTokenOrTextEditDistance = false;
    boolean usePossibleProbable = false;
    double canforaThreshold = 0.4;
    double classMappingThreshold = 0.9;
    double fieldMappingThreshold = 2.5;
    double methodMappingThreshold = 2.0;

    public SDiff() {}

    public SDiff(boolean useTokenEditDistance, boolean useMinOfTokenOrTextEditDistance, boolean usePossibleProbable, double canforaThreshold, double classMappingThreshold, double fieldMappingThreshold, double methodMappingThreshold) {
        this.useTokenEditDistance = useTokenEditDistance;
        this.useMinOfTokenOrTextEditDistance = useMinOfTokenOrTextEditDistance;
        this.usePossibleProbable = usePossibleProbable;
        this.canforaThreshold = canforaThreshold;
        this.classMappingThreshold = classMappingThreshold;
        this.fieldMappingThreshold = fieldMappingThreshold;
        this.methodMappingThreshold = methodMappingThreshold;
    }


    public Map<Integer, Integer> run(String srcFilePath, String dstFilePath) throws IOException {
        StatementMapper mapper = new StatementMapper(srcFilePath, srcVersionLeft, dstFilePath, dstVersionLeft);
        mapper.setUseMinTokenOrTextEditDistance(useMinOfTokenOrTextEditDistance);
        mapper.setCanfora(canforaThreshold);
        mapper.setClassMappingThreshold(classMappingThreshold);
        mapper.setFieldMappingThreshold(fieldMappingThreshold);
        mapper.setMethodMappingThreshold(methodMappingThreshold);
        mapper.setRunTokenEditDistance(useTokenEditDistance);
        StatementMappingResultSet rs = mapper.runStatementMapper();
        mapper.setValidMappings(rs);
        mapper.setProbablePossibleMappings(rs);
        Map<Integer, Integer> result = new HashMap<>();
        for (StatementMappingEntry r : rs) {
            if (r.getLeftStatement().getBeginLine() > 0 && r.getRightStatement().getBeginLine() > 0) {
                result.put(
                        r.getLeftStatement().getBeginLine() - 1,
                        r.getRightStatement().getBeginLine() - 1);
            }
        }
        return result;
    }
    @Override
    public Map<Integer, Integer> getDiffMap(List<String> fileContentByCommit, List<String> prevCommitCorrespondingFile) throws IOException {
        //create tmp files

        Path tempFile = Files.createTempFile("src", ".java");
        String content = String.join("\n", (fileContentByCommit));
        ContentModifier modifier = content1 -> new LambdaBodyModifier().apply(new MethodRefModifier().apply(new GenericStatementModifier().apply(content1)));
        Files.write(tempFile, modifier.apply(content).getBytes());
        Path tempFile2 = Files.createTempFile("dst", ".java");
        String content2 = String.join("\n", prevCommitCorrespondingFile);
        Files.write(tempFile2, modifier.apply(content2).getBytes());
        return run(tempFile.toString(), tempFile2.toString());
    }

}


