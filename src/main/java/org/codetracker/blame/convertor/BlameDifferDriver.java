package org.codetracker.blame.convertor;

import org.codetracker.blame.convertor.impl.BlameDiffer;
import org.codetracker.blame.convertor.impl.BlameDifferOneWithMany;
import org.codetracker.blame.convertor.impl.util.CsvWriter;
import org.codetracker.blame.convertor.impl.util.StatsCollector;
import org.codetracker.blame.convertor.model.BlameCaseInfo;
import org.codetracker.blame.convertor.model.BlameDifferResult;
import org.codetracker.blame.convertor.model.CodeLinePredicate;
import org.codetracker.blame.fromDiffer.gitlog.RMDrivenGitLogServiceWithSerialization;
import org.codetracker.blame.impl.CliGitBlameCustomizable;
import org.codetracker.blame.impl.FileTrackerBlameWithSerialization;
import org.codetracker.blame.log.GitLogQuery;
import org.codetracker.blame.model.IBlame;
import org.codetracker.blame.model.IBlameTool;
import org.codetracker.blame.model.LineBlameResult;
import org.codetracker.blame.util.GithubUtils;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.refactoringminer.api.GitService;
import org.refactoringminer.astDiff.utils.URLHelper;
import org.refactoringminer.util.GitServiceImpl;

import java.util.*;
import java.util.function.Predicate;

import static org.codetracker.blame.util.Utils.*;

/* Created by pourya on 2024-07-14*/
public class BlameDifferDriver {
    private static final GitService gitService = new GitServiceImpl();
    private static final String REPOS_PATH = System.getProperty("user.dir") + "/tmp";
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
//                    add(BlamersEnum.SBlame);
                    add(BlamersEnum.HistBlame);
                    add(BlamersEnum.MyersBlame);
                    add(BlamersEnum.HistBlameIW);
                    add(BlamersEnum.MyersBlameIW);
                    }};

    public static final Predicate<String> codeElementIgnoreCondition =
            CodeLinePredicate.BLANK_LINE.or(CodeLinePredicate.OPENING_AND_CLOSING_CURLY_BRACKET);
    private static final BlameDiffer blameDiffer = new BlameDifferOneWithMany(blamerFactories,
            BlamersEnum.FileTrackerBlame, codeElementIgnoreCondition);

    private static final String[][] dummies = {
            {"https://github.com/checkstyle/checkstyle/commit/119fd4fb33bef9f5c66fc950396669af842c21a3", "src/main/java/com/puppycrawl/tools/checkstyle/Checker.java"},
            {"https://github.com/javaparser/javaparser/commit/97555053af3025556efe1a168fd7943dac28a2a6", "javaparser-core/src/main/java/com/github/javaparser/printer/lexicalpreservation/Difference.java"},
            {"https://github.com/javaparser/javaparser/commit/97555053af3025556efe1a168fd7943dac28a2a6", "javaparser-symbol-solver-core/src/main/java/com/github/javaparser/symbolsolver/javaparsermodel/contexts/MethodCallExprContext.java"},
            {"https://github.com/spring-projects/spring-framework/commit/b325c74216fd9564a36602158fa1269e2e832874", "spring-webmvc/src/main/java/org/springframework/web/servlet/mvc/method/annotation/AbstractMessageConverterMethodProcessor.java"},
            {"https://github.com/junit-team/junit5/commit/77cfe71e7f787c59626198e25350545f41e968bd", "junit-jupiter-engine/src/main/java/org/junit/jupiter/engine/descriptor/ClassTestDescriptor.java"},
            {"https://github.com/hibernate/hibernate-orm/commit/8bd79b29cfa7b2d539a746dc356d60b66e1e596b", "hibernate-core/src/main/java/org/hibernate/cfg/AnnotationBinder.java"},
            {"https://github.com/eclipse/jgit/commit/bd1a82502680b5de5bf86f6c4470185fd1602386", "org.eclipse.jgit/src/org/eclipse/jgit/internal/storage/pack/PackWriter.java"},
            {"https://github.com/JetBrains/intellij-community/commit/ecb1bb9d4d484ae63ee77f8ad45bdce154db9356", "java/compiler/impl/src/com/intellij/compiler/CompilerManagerImpl.java"},
            {"https://github.com/JetBrains/intellij-community/commit/ecb1bb9d4d484ae63ee77f8ad45bdce154db9356", "java/compiler/impl/src/com/intellij/compiler/actions/CompileDirtyAction.java"},

    };
    public static void main(String[] args) throws Exception {
//        new BlameInfoProviderByRM().provide(dummies);
        String url = "https://github.com/JetBrains/intellij-community/commit/ecb1bb9d4d484ae63ee77f8ad45bdce154db9356";
        String filePath = "java/compiler/impl/src/com/intellij/compiler/actions/CompileDirtyAction.java";

        String commitId = URLHelper.getCommit(url);
        String owner = getOwner(url);
        String project = getProject(url);
        Repository repository = getRepository(url, gitService, REPOS_PATH);
        GitLogQuery gitLogQuery = new GitLogQuery(repository, filePath, commitId);
        List<RevCommit> commits = GithubUtils.gitLogRevCommits(gitLogQuery);
        BlamersEnum.FileTrackerBlameNoSerialization.blameFile(repository, commitId, filePath);
        if (true) return;

        int low = 0;
        int high = 100;
        int step = 50;
        // TODO: We want to record the cases that some configuration reduce the number of disagreement
        //  For these cases, Increase the threshold
        //  1000 ?  try to get character numbers from moved methods directly from RefMiner -?
        //  experiment with https://github.com/junit-team/junit4/commit/7a3e99635d7ffcc4d730f27835eeaeb082003199
//        blamerFactories.addAll(makeCliGitBlames("-M", low, high, step, false));
//        blamerFactories.addAll(makeCliGitBlames("-C", low, high, step, false));
//        blamerFactories.addAll(makeCliGitBlames("-M", low, high, step, true));
//        blamerFactories.addAll(makeCliGitBlames("-C", low, high, step, true));
        run(getDummyBlameCases());
        FileTrackerBlameWithSerialization.saveCacheToFile();
        RMDrivenGitLogServiceWithSerialization.saveCacheToFile();
    }



    public static Set<IBlameTool> makeCliGitBlames(String firstParam, int lowerLimit, int higherLimit, int step, boolean ignoreWhitespace) {
        Set<IBlameTool> iBlameTools = new LinkedHashSet<>();
        int num;
        for (int i = lowerLimit; i < higherLimit; i+=step) {
            num = i;
            String[] moreOptions = {firstParam + num};
            iBlameTools.add(getCliBlameTool(ignoreWhitespace, moreOptions));
        }
        return iBlameTools;
    }

    public static IBlameTool getCliBlameTool(boolean ignoreWhitespace, String[] moreOptions) {
        IBlame cliGitBlameCustomizable = new CliGitBlameCustomizable(ignoreWhitespace, moreOptions);
        IBlameTool customBlameTool = new IBlameTool() {
            @Override
            public List<LineBlameResult> blameFile(Repository repository, String commitId, String filePath) throws Exception {
                return cliGitBlameCustomizable.blameFile(repository, commitId, filePath);
            }

            @Override
            public List<LineBlameResult> blameFile(Repository repository, String commitId, String filePath, int fromLine, int toLine) throws Exception {
                return cliGitBlameCustomizable.blameFile(repository, commitId, filePath, fromLine, toLine);
            }

            @Override
            public String getToolName() {
                return "CliGitBlameCustomizable" + Arrays.toString(moreOptions) + "IgnoreWhitespace" + ignoreWhitespace;
            }
        };
        return customBlameTool;
    }


    private static Set<BlameCaseInfo> getDummyBlameCases() {
        Set<BlameCaseInfo> result = new LinkedHashSet<>();
        for (String[] dummy : dummies) {
            result.add(new BlameCaseInfo(dummy[0], dummy[1]));
        }
        return result;
    }

    static void run(Set<BlameCaseInfo> blameCases) throws Exception {
        StatsCollector statsCollector = new StatsCollector();
        for (BlameCaseInfo blameCase : blameCases) {
            System.out.println("Processing " + blameCase.url);
            process(blameCase.url, blameCase.filePath, statsCollector);
        }

    }
    public static void process(String url, String filePath, StatsCollector statsCollector) throws Exception {
        {
            String commitId = URLHelper.getCommit(url);
            String owner = getOwner(url);
            String project = getProject(url);
            Repository repository = getRepository(url, gitService, REPOS_PATH);
            BlameDifferResult blameDifferResult = blameDiffer.diff(repository, commitId, filePath);
            Map<Integer, Map<IBlameTool, String>> result = blameDifferResult.getTable();
            statsCollector.process(result, blameDifferResult.getLegitSize(), blameDifferResult.getCodeElementWithReprMap());
            CsvWriter csvWriter = new CsvWriter(owner, project, commitId, filePath, blameDifferResult.getCodeElementWithReprMap());
            csvWriter.writeToCSV(result);
            csvWriter.writeCertificateToCSV(blameDiffer.getCertificate());
            statsCollector.writeInfo();
        }
    }
}
