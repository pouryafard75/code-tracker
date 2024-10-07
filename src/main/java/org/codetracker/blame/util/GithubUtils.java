package org.codetracker.blame.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.github.gumtreediff.utils.Pair;
import org.codetracker.blame.log.GitLogQuery;
import org.eclipse.jgit.api.DiffCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.json.JSONArray;
import org.json.JSONObject;
/* Created by pourya on 2024-09-24*/
public class GithubUtils {
    private static final String GITHUB_API_TOKEN = System.getenv("OAuthToken");
    public static boolean areSameConsideringMerge(Repository repo, String commitId1, String commitId2) {
        Pair<String, String> ownerAndProject = getOwnerAndProject(repo);
        return areSameConsideringMerge(ownerAndProject.first, ownerAndProject.second, commitId1, commitId2);
    }
    public static boolean areSameConsideringMerge(String repo, String project, String commitId1, String commitId2) {
        if (commitId1 == null || commitId2 == null) return false;
        if (commitId1.isEmpty() || commitId2.isEmpty()) return false;
        System.out.println("Checking if " + commitId1 + " and " + commitId2 + " are the same considering merge");
        try {
            JSONObject commit1Info = getCommitInfo(repo, project, commitId1);
            JSONObject commit2Info = getCommitInfo(repo, project , commitId2);
            if (commit1Info == null || commit2Info == null) {
                return false;
            }

            // Get parents for both commits
            JSONArray commit1Parents = commit1Info.getJSONArray("parents");
            JSONArray commit2Parents = commit2Info.getJSONArray("parents");

            //Check which commit is the merge commit
            JSONArray parents;
            String nonMergeCommit;
            if(commit1Parents.length() > 1 && commit2Parents.length() == 1){
                parents = commit1Parents;
                nonMergeCommit = commitId2;
            } else if(commit2Parents.length() > 1 && commit1Parents.length() == 1){
                parents = commit2Parents;
                nonMergeCommit = commitId1;
            } else {
                return false;
            }

            //Check if the commitId of the non-merge commit is in the parents of the merge commit
            for (int i = 0; i < parents.length(); i++) {
                String parent1Sha = parents.getJSONObject(i).getString("sha");
                if (parent1Sha.contains(nonMergeCommit)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false; // Default to false if no relationship found
    }
    private static JSONObject getCommitInfo(String owner, String repo, String commitId) throws Exception {
        String commitURL = java.lang.String.format("https://api.github.com/repos/%s/%s/commits/%s", owner, repo, commitId);
        HttpURLConnection connection = (HttpURLConnection) new URL(commitURL).openConnection();
        connection.setRequestProperty("Authorization", "token " + GITHUB_API_TOKEN);
        connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
        connection.setRequestMethod("GET");
        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return new JSONObject(response.toString());
        } else {
            System.out.println("Failed to fetch commit info, response code: " + responseCode);
        }
        return null;
    }
    public static Pair<String, String> getOwnerAndProject(Repository repository) {
        File repoDir = repository.getDirectory();
        String repoPath = repoDir.getAbsolutePath();
        String[] pathParts = repoPath.split(File.separator);
        String projectName = pathParts[pathParts.length - 2]; // Last segment is the project
        String ownerName = pathParts[pathParts.length - 3]; // Second to last segment is the owner
        return new Pair<>(ownerName, projectName);
    }
    public static List<RevCommit> gitLogRevCommits(Repository repository, String filePath, String startCommitHash) throws GitAPIException, IOException {
        Git git = new Git(repository);
        Iterable<RevCommit> log = git.log()
                .add(repository.resolve(startCommitHash))
                .addPath(filePath)
                .call();
        List<RevCommit> result = new ArrayList<>();
        for (RevCommit rev : log) {
            result.add(rev);
        }
        return result;
    }
    public static List<RevCommit> gitLogRevCommits(GitLogQuery query) throws GitAPIException, IOException {
        return gitLogRevCommits(query.getRepository(), query.getFilePath(), query.getCommitId());
    }

    private static String getPreviousFileName(Git git, RevCommit commit, String filePath) throws Exception {
        // Call followsRename to get the previous file name
        if (followsRename(git, commit, filePath)) {
            return filePath; // Assuming this function has been modified to return the old name
        }
        return null;
    }

    public static boolean followsRename(Git git, RevCommit commit, String filePath) throws Exception {
        // Get parent commits
        RevCommit[] parents = commit.getParents();
        if (parents.length == 0) {
            return false; // No parent, nothing to compare
        }

        // Use DiffCommand to find renames
        DiffCommand diffCommand = git.diff().setOldTree(getTreeParser(git, parents[0]))
                .setNewTree(getTreeParser(git, commit));
        List<DiffEntry> diffEntries = diffCommand.call();

        for (DiffEntry entry : diffEntries) {
            if (entry.getChangeType() == DiffEntry.ChangeType.RENAME) {
                // Check if the renamed file matches the current file path
                if (entry.getNewPath().equals(filePath)) {
                    System.out.println("Previous file name: " + entry.getOldPath());
                    return true; // Found a rename for the file
                }
            }
        }
        return false; // No rename found for the specified file
    }
    private static AbstractTreeIterator getTreeParser(Git git, RevCommit commit) throws IOException {
        try (RevWalk walk = new  RevWalk(git.getRepository())) {
            return new CanonicalTreeParser(null, git.getRepository().newObjectReader(), commit.getTree());
        }
    }
}

