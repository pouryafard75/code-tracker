package org.codetracker.blame.fromDiffer;

import org.apache.commons.io.FileUtils;
import org.codetracker.blame.convertor.BlamersEnum;
import org.codetracker.blame.fromDiffer.gitlog.RMDrivenGitLogService;
import org.codetracker.blame.log.GitLogQuery;
import org.codetracker.blame.log.PrevCommitInfo;
import org.codetracker.blame.model.IBlameTool;
import org.codetracker.blame.model.LineBlameResult;
import org.codetracker.blame.util.BlameFormatter;
import org.codetracker.blame.util.FileNotFoundInThePrevCommitException;
import org.codetracker.blame.util.TabularPrint;
import org.codetracker.blame.util.Utils;
import org.eclipse.jgit.lib.Repository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.refactoringminer.api.GitService;
import org.refactoringminer.astDiff.utils.URLHelper;
import org.refactoringminer.util.GitServiceImpl;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

import static org.codetracker.blame.CodeTrackerBlameTest.assertEqualWithFile;
import static org.codetracker.blame.util.Utils.getRepository;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;

/* Created by pourya on 2024-10-14*/
@Isolated
public class DiffToBlameTest {
    private static GitService gitService;
    private static String REPOS_PATH;
    private static String testResourcesPath = "src/test/resources/blame/diffToBlame/";


    // Define a method that provides the URLs


    @BeforeAll
    public static void setup() {
        gitService = new GitServiceImpl();
        REPOS_PATH = System.getProperty("user.dir") + "/tmp";
    }


    @ParameterizedTest
    @MethodSource("myDummyCasesUrl")
    public void NoRenamesTest(String url, String filePath) throws Exception {
        String folderName = "NoRenames";
        IBlameTool blamer =  BlamersEnum.LHBlame;
        String commitId = URLHelper.getCommit(url);
        Repository repository = getRepository(url, gitService, REPOS_PATH);
        List<LineBlameResult> lineBlameResults = blamer.blameFile(repository, commitId, filePath);
        List<String[]> out = new BlameFormatter(Utils.getFileContentByCommit(repository, commitId, filePath)).make(lineBlameResults);
        String expectedFilePath = testResourcesPath + File.separator +
                folderName +
                File.separator + commitId.substring(0, 9) + "_" + filePath.replace("/", "_")
                + ".txt";
        assertEqualWithFile(expectedFilePath, TabularPrint.make(out));
    }

    @ParameterizedTest
    @MethodSource("myDummyCasesUrl")
    public void GitLogServiceTest(String url, String filePath) throws Exception {
        String commitId = URLHelper.getCommit(url);
        Repository repository = getRepository(url, gitService, REPOS_PATH);
        GitLogQuery gitLogQuery = new GitLogQuery(repository, filePath, commitId);
        PrevCommitInfo prevCommitInfo = null;
        try {
            prevCommitInfo = new RMDrivenGitLogService().getPrevCommitInfo(gitLogQuery);
        }
        catch (FileNotFoundInThePrevCommitException e) {
            if (!commitId.equals("c09c3ef57fc20875c6d92cb57e7ee316c13a24b0"))
                Assertions.fail();
            else return;
        }
        String folderName = "ParentResolver";
        String expectedFilePath = testResourcesPath + File.separator +
                folderName +
                File.separator + commitId.substring(0, 9) + "_" + filePath.replace("/", "_");
        assertEqualWithFile(expectedFilePath, prevCommitInfo.getPrevCommitId());
    }
    @ParameterizedTest
    @MethodSource("withRenamesCasesUrl")
    public void WithRenamesTest(String url, String filePath) throws Exception {
        String folderName = "WithRenames";
        IBlameTool blamer =  BlamersEnum.LHBlame;
//        IBlameTool blamer = BlamersEnum.FileTrackerBlameNoSerialization;
        String commitId = URLHelper.getCommit(url);
        Repository repository = getRepository(url, gitService, REPOS_PATH);
        List<LineBlameResult> lineBlameResults = blamer.blameFile(repository, commitId, filePath);
        List<String[]> out = new BlameFormatter(Utils.getFileContentByCommit(repository, commitId, filePath)).make(lineBlameResults);
        String expectedFilePath = testResourcesPath + File.separator +
                folderName +
                File.separator + commitId.substring(0, 9) + "_" + filePath.replace("/", "_")
                + ".txt";

        String result;
        try {
            result = TabularPrint.make(out);
        }
        catch (Exception e) {
            e.printStackTrace();
            fail("Error in making the tabular print");
            return;
        }
        assertEqualWithFile(expectedFilePath, result);

    }

    @ParameterizedTest
    @MethodSource("renameCaseMultipleBlamers")
    public void CheckStyleTest(String url, String filePath, IBlameTool blamer) throws Exception {
        String folderName = blamer.getToolName();
        String commitId = URLHelper.getCommit(url);
        Repository repository = getRepository(url, gitService, REPOS_PATH);
        List<LineBlameResult> lineBlameResults = blamer.blameFile(repository, commitId, filePath);
        List<String[]> out = new BlameFormatter(Utils.getFileContentByCommit(repository, commitId, filePath)).make(lineBlameResults);
        String expectedFilePath = testResourcesPath + File.separator +
                folderName +
                File.separator + commitId.substring(0, 9) + "_" + filePath.replace("/", "_")
                + ".txt";
//        assertEqualWithFile(expectedFilePath, TabularPrint.make(out));
        FileUtils.write(new File(expectedFilePath), TabularPrint.make(out));
    }



    public static Stream<Arguments> myDummyCasesUrl() {
        return Stream.of(
                Arguments.of("https://github.com/pouryafard75/TestCases/commit/457cf89b923a81aac29f701728ea7e88b3cb87b9", "blameTest/po.java"),
                Arguments.of("https://github.com/pouryafard75/TestCases/commit/bc42c5af4e3496f539d24b6d850288407f96d9f4", "blameTest/po.java"),
                Arguments.of("https://github.com/pouryafard75/TestCases/commit/0c741ab9da8decc3097cd7e7805f54fd17cfd137", "blameTest/po.java"),
                Arguments.of("https://github.com/pouryafard75/TestCases/commit/bce1e231a602191a158835b449fdcc58d9469704", "blameTest/po.java"),
                Arguments.of("https://github.com/pouryafard75/TestCases/commit/11f11ce0685d167fcb87bd1183a63d803f4e2b1c", "blameTest/po.java"),
                Arguments.of("https://github.com/pouryafard75/TestCases/commit/c09c3ef57fc20875c6d92cb57e7ee316c13a24b0", "blameTest/po.java")
        );
    }

    public static Stream<Arguments> withRenamesCasesUrl() {
        return Stream.of(
                Arguments.of("https://github.com/pouryafard75/TestCases/commit/2c9b22f59b4456de880dd69e550963d44670928e", "blameTest/po3.java"),
                Arguments.of("https://github.com/pouryafard75/TestCases/commit/64e3039e9ffee93883cf0b5ea8395da440548ea0", "blameTest/po3.java"),
                Arguments.of("https://github.com/pouryafard75/TestCases/commit/614f3faa1bdfa2b21db7a17c866ef7f9fcd18323", "blameTest/po3.java"),
                Arguments.of("https://github.com/pouryafard75/TestCases/commit/0bc2c3d0a75f04519b84d2579f51224cc8b6f485", "blameTest/po2.java"),
                Arguments.of("https://github.com/pouryafard75/TestCases/commit/75b35f6422a6b52ff47383ffeedaca4947487546", "blameTest/po2.java"),
                Arguments.of("https://github.com/pouryafard75/TestCases/commit/ce76c035d24fb4c78ac4d21de18fc2329966199a", "blameTest/po.java"),
                Arguments.of("https://github.com/pouryafard75/TestCases/commit/457cf89b923a81aac29f701728ea7e88b3cb87b9", "blameTest/po.java")
        );
    }

    public static Stream<Arguments> renameCaseMultipleBlamers(){
        return Stream.of(
                Arguments.of("https://github.com/checkstyle/checkstyle/commit/119fd4fb33bef9f5c66fc950396669af842c21a3",
                        "src/main/java/com/puppycrawl/tools/checkstyle/Checker.java",
                        BlamersEnum.FileTrackerBlameNoSerialization
                ),
                Arguments.of("https://github.com/checkstyle/checkstyle/commit/119fd4fb33bef9f5c66fc950396669af842c21a3",
                        "src/main/java/com/puppycrawl/tools/checkstyle/Checker.java",
                        BlamersEnum.MyersBlameIW
                )
        );
    }

}
