//package org.codetracker.blame.convertor;
//
//import org.eclipse.jgit.lib.Repository;
//import org.refactoringminer.api.GitService;
//import org.refactoringminer.astDiff.utils.URLHelper;
//import org.refactoringminer.util.GitServiceImpl;
//
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.ObjectOutputStream;
//import java.util.HashMap;
//import java.util.Map;
//
//import static org.codetracker.blame.util.Utils.findParentCommitId;
//import static org.codetracker.blame.util.Utils.getRepository;
//
///* Created by pourya on 2024-10-17*/
////dummy must be deleted
//public class BlameInfoProviderByRM {
//    private static final GitService gitService = new GitServiceImpl();
//    private static final String REPOS_PATH = System.getProperty("user.dir") + "/tmp";
//    public void provide(String[][] dummies) throws Exception {
//        Map<String, String> result = new HashMap<>();
//        for (String[] dummy : dummies) {
//
//            String url = dummy[0];
//            String filePath = dummy[1];
//
//            String commitID = URLHelper.getCommit(url);
//            Repository repo = getRepository(url, gitService, REPOS_PATH);
//
//            while (true) {
//                try {
//                    String correspondingFileInParentCommit = getCorrespondingFileInParentCommit(repo, commitID, filePath);
//                    result.put(commitID, correspondingFileInParentCommit);
//                    commitID = findParentCommitId(repo, commitID);
//                }
//                catch (Exception e){
//                    break;
//                }
//        }
//        writeToFile(result, getMapFileName(url, filePath));
//    }
//}
//
//    private static String getMapFileName(String url, String filePath) {
//        return url.replace("/", "_") + "_" + filePath.replace("/", "_") + ".ser";
//    }
//
//    private static void writeToFile(Map<String, String> result, String name) {
//        try (FileOutputStream fileOut = new FileOutputStream(name);
//             ObjectOutputStream objectOut = new ObjectOutputStream(fileOut)) {
//            objectOut.writeObject(result);
//            System.out.println("Map has been written to file.");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}
