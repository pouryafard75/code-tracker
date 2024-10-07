package org.codetracker.blame.impl.differ.sd;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.codetracker.blame.CodeTrackerBlameTest.assertEqualWithFile;


class GenericStatementModifierTest {

    @Test
    void apply() throws IOException {
        String input_file = "generic_code.txt";
        String output_file = "generic_code_result.txt";
        String expectedFolderPath = System.getProperty("user.dir") + "/src/test/resources/blame/diffToBlame/sd/";
        ContentModifier modifier = content1 -> new LambdaBodyModifier().apply(new MethodRefModifier().apply(new GenericStatementModifier().apply(content1)));
        String content = IOUtils.toString(
                new FileInputStream(expectedFolderPath + input_file),
                StandardCharsets.UTF_8
        );
        String modifiedContent = modifier.apply(content);
        assertEqualWithFile(expectedFolderPath + output_file, modifiedContent);
    }
}