package org.codetracker.blame.fromDiffer.gitlog;

import org.codetracker.blame.impl.FileTrackerBlameWithCache;
import org.codetracker.blame.log.GitLogQuery;
import org.codetracker.blame.log.PrevCommitInfo;
import org.codetracker.blame.util.FileNotFoundInThePrevCommitException;

import java.util.LinkedHashMap;
import java.util.Map;


/* Created by pourya on 2024-10-24*/
public class RMDrivenGitLogServiceWithCache extends RMDrivenGitLogService {
    public static Map<String, PrevCommitInfo> cache = new LinkedHashMap<>();
    @Override
    public PrevCommitInfo getPrevCommitInfo(GitLogQuery query) throws FileNotFoundInThePrevCommitException {
        String cacheKey = getKey(query);
        if (cache.containsKey(cacheKey)) {
            PrevCommitInfo prevCommitInfo = cache.get(cacheKey);
            if (prevCommitInfo.equals(PrevCommitInfo.Null())) {
                throw new FileNotFoundInThePrevCommitException();
            }
            return prevCommitInfo;
        }
//        System.out.println("Cache miss for " + cacheKey);
        PrevCommitInfo prevCommitInfo;
        try {
            prevCommitInfo = super.getPrevCommitInfo(query);
            cache.put(cacheKey, prevCommitInfo);
        }
        catch (FileNotFoundInThePrevCommitException e) {
            cache.put(cacheKey, PrevCommitInfo.Null());
            throw e;
        }
        return prevCommitInfo;
    }

    public static String getKey(GitLogQuery query) {
        return FileTrackerBlameWithCache.getKey(query.getRepository(), query.getCommitId(), query.getFilePath());
    }
}



//def make_graph_from_file(file_path):
//with open(file_path, 'r') as file:
//lines = file.readlines()
//nodes = {}
//edges = []
//
//        for line in lines:
//data = line.strip().split(':')
//node_id, pos, *connections = data
//        x, y = map(int, pos.split(','))
//
//nodes[int(node_id)] = {'pos': (x, y)}
//        edges.extend((int(node_id), int(neighbor)) for neighbor in connections[0].split(','))
//graph = nx.Graph()
//    graph.add_nodes_from(nodes.items())
//        graph.add_edges_from(edges)
//    return graph
//
//def write_to_csv(csv_file, data, startPoint, openWith = 'w'):
//        # Writing to CSV
//csv_file += ".csv"
//with open(csv_file, openWith, newline='') as file:
//writer = csv.writer(file)
//        # Writing the header
//        writer.writerow(['size', "Informed",  'history'])
//
//        # Writing each row in the list of lists
//        for row in data:
//        writer.writerow([len(row), startPoint, row])