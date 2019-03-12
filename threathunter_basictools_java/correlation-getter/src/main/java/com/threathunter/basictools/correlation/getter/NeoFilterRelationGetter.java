package com.threathunter.basictools.correlation.getter;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * created by www.threathunter.cn
 */
public class NeoFilterRelationGetter {
    private final RelationshipType ipMobileRelation = () -> "connect";
    private final Map<String, Node> cacheMap = new HashMap<>();
    private final TraversalDescription td;
    private final GraphDatabaseService graphDb;
    private final int depth;


    public NeoFilterRelationGetter(String dbPath, int depth) {
        graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(dbPath);
        td = graphDb.traversalDescription().breadthFirst().relationships(ipMobileRelation, Direction.OUTGOING)
                .evaluator(Evaluators.toDepth(3)).evaluator(Evaluators.excludeStartPosition());
        this.depth = depth;
    }

    private String findNeighborsOfNode(Node node) {
        ResourceIterator<Path> iter = td.traverse(node).iterator();
        Map<Integer, Set<String>> neighbors = new HashMap<>();

        while (iter.hasNext()) {
            Path p = iter.next();
            neighbors.computeIfAbsent(p.length(), (Integer depth) -> new HashSet<>()).add((String)p.endNode().getProperty("name"));
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= depth; i++) {
            Set<String> nodes = neighbors.getOrDefault(i, new HashSet<>());
            sb.append(String.join(",", nodes));
            sb.append("|");
        }
        return sb.toString();
    }

    public void getNodesNeighbors(String output) throws IOException {
        BufferedWriter bw = null;
        if (output != null && !output.isEmpty()) {
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output)));
        } else {
            bw = new BufferedWriter(new OutputStreamWriter(System.out));
        }
        Transaction tx = null;
        try  {
            tx = graphDb.beginTx();
            int count = 0;
            for (Node n : cacheMap.values()) {
                bw.write((String)n.getProperty("name"));
                bw.write("|");
                bw.write(findNeighborsOfNode(n));
                bw.newLine();

                count++;
                if (count % 10000 == 0) {
                    tx.success();
                    tx.finish();
                    tx = graphDb.beginTx();
                    bw.flush();
                }
            }
        } catch (Exception e) {
        } finally {
            if (tx != null) {
                tx.success();
            }
            if (bw != null) {
                bw.flush();
                bw.close();
            }
        }
    }

    public void addRelationToNeo(String filePath) throws IOException{
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"));
        String line = br.readLine();

        Transaction tx = null;
        try {
            tx = graphDb.beginTx();
            int count = 0;
            while (line != null) {
                count++;
                try {
                    String[] ipMobile = line.split("\\s+");
                    if (ipMobile.length < 2) {
                        line = br.readLine();
                        continue;
                    }
                    String ip = ipMobile[1].trim();
                    String mobile = ipMobile[0].trim();

                    if (ip.isEmpty() || mobile.isEmpty()) {
                        continue;
                    }

                    Node ipNode = cacheMap.get(ip);
                    if (ipNode == null) {
                        ipNode = graphDb.createNode();
                        ipNode.setProperty("name", ip);
                        cacheMap.put(ip, ipNode);
                    }
                    Node mobileNode = cacheMap.get(mobile);
                    if (mobileNode == null) {
                        mobileNode = graphDb.createNode();
                        mobileNode.setProperty("name", mobile);
                        cacheMap.put(mobile, mobileNode);
                    }

                    ipNode.createRelationshipTo(mobileNode, ipMobileRelation);
                    mobileNode.createRelationshipTo(ipNode, ipMobileRelation);

                } catch (Exception e) {
                }
                line = br.readLine();
                if (count % 10000 == 0) {
                    tx.success();
                    tx.finish();
                    tx = graphDb.beginTx();
                }
            }
        } catch (Exception e) {
            ;
        } finally {
            if (tx != null) {
                tx.success();
                tx.close();
            }
            br.close();
        }
    }

}
