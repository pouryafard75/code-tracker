package org.codetracker.blame.fromDiffer.gitlog;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.codetracker.blame.log.GitLogQuery;
import org.codetracker.blame.log.PrevCommitInfo;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static org.codetracker.blame.impl.FileTrackerBlameWithSerialization.deserializeCache;
import static org.codetracker.blame.util.Utils.getRepository;

/* Created by pourya on 2024-10-25*/
public class RMDrivenGitLogServiceWithSerialization extends RMDrivenGitLogServiceWithCache
{
    static String SER_PATH = "cacheLog.ser";
    static ObjectMapper objectMapper;
    static {
        objectMapper = new ObjectMapper();
        try {
            cache = deserializeCache(SER_PATH,
                    new TypeReference<Map<String, PrevCommitInfo>>() {});
            custom();
            System.out.println(cache.size());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void custom() {
        String url = "https://github.com/junit-team/junit5/tree/470866bc19cac8717ac77b26efc1fe703bed264a";
        String filePath = "junit5-engine/src/main/java/org/junit/gen5/engine/junit5/descriptor/ClassTestDescriptor.java";
        GitLogQuery query = null;
        try {
            query = new GitLogQuery(getRepository(url), filePath, "470866bc19cac8717ac77b26efc1fe703bed264a");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        RMDrivenGitLogServiceWithCache.cache.put(RMDrivenGitLogServiceWithCache.getKey(query),
                new PrevCommitInfo("57b31220d25f59bb4aa4903dfee4b8ec2be4525e", "junit5-engine/src/main/java/org/junit/gen5/engine/junit5/JUnit5ClassDescriptor.java"));
    }

    public static void saveCacheToFile() throws IOException {
        System.out.println("Saving cache to file " + cache.size());
        objectMapper.writeValue(new File(SER_PATH), cache);
    }
}
