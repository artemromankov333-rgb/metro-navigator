package com.metro.algorithm;

import com.metro.model.PathResult;
import com.metro.utils.LoggerUtil;
import com.metro.utils.ResourceLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MetroPathFinder {

    private static final Logger logger = LogManager.getLogger(MetroPathFinder.class);
    private static final int INF = Integer.MAX_VALUE / 2;

    private List<String> stations;
    private int[][] adjacencyMatrix;

    public MetroPathFinder(String fileName) throws Exception {
        loadMatrixFromResource(fileName);
        logger.debug("MetroPathFinder initialized with {} stations", stations.size());
    }

    private void loadMatrixFromResource(String fileName) throws Exception {
        List<String> lines = ResourceLoader.readTextFile(fileName);

        if (lines.isEmpty()) {
            throw new Exception("Matrix file is empty or not found: " + fileName);
        }

        String firstLine = lines.get(0).trim();
        String[] header = firstLine.split(",");

        if (header.length < 2) {
            throw new Exception("Invalid matrix header format");
        }

        stations = new ArrayList<>(
                Arrays.asList(header).subList(1, header.length)
        );

        int size = stations.size();
        adjacencyMatrix = new int[size][size];

        for (int i = 0; i < size; i++) {
            if (i + 1 >= lines.size()) {
                throw new Exception("Not enough rows in matrix");
            }

            String line = lines.get(i + 1).trim();
            if (line.isEmpty()) {
                throw new Exception("Empty row in matrix (row " + (i + 2) + ")");
            }

            String[] values = line.split(",");

            if (values.length != size + 1) {
                throw new Exception(
                        "Error in row " + (i + 2) +
                                ": expected " + (size + 1) +
                                " values, got " + values.length
                );
            }

            for (int j = 1; j < values.length; j++) {
                try {
                    int weight = Integer.parseInt(values[j].trim());
                    adjacencyMatrix[i][j - 1] = (weight == 9) ? INF : weight;
                } catch (NumberFormatException e) {
                    adjacencyMatrix[i][j - 1] = INF;
                }
            }
        }

        for (int i = 0; i < size; i++) {
            adjacencyMatrix[i][i] = 0;
        }

        logger.info("Successfully loaded metro matrix with {} stations", size);
    }

    public PathResult findShortestPath(String startStation, String endStation) {
        logger.debug("Searching path from '{}' to '{}'", startStation, endStation);

        int startIndex = stations.indexOf(startStation);
        int endIndex = stations.indexOf(endStation);

        if (startIndex == -1 || endIndex == -1) {
            logger.warn("Station not found: {} or {}", startStation, endStation);
            return new PathResult("Station not found: " +
                    (startIndex == -1 ? startStation : endStation));
        }

        int size = stations.size();
        int[] dist = new int[size];
        int[] prev = new int[size];
        boolean[] visited = new boolean[size];

        Arrays.fill(dist, INF);
        Arrays.fill(prev, -1);
        dist[startIndex] = 0;

        for (int i = 0; i < size; i++) {
            int minDist = INF;
            int u = -1;

            for (int j = 0; j < size; j++) {
                if (!visited[j] && dist[j] < minDist) {
                    minDist = dist[j];
                    u = j;
                }
            }

            if (u == -1 || u == endIndex) break;

            visited[u] = true;

            for (int v = 0; v < size; v++) {
                if (!visited[v] && adjacencyMatrix[u][v] != INF) {
                    int alt = dist[u] + adjacencyMatrix[u][v];
                    if (alt < dist[v]) {
                        dist[v] = alt;
                        prev[v] = u;
                    }
                }
            }
        }

        if (dist[endIndex] == INF) {
            logger.warn("No path found from '{}' to '{}'", startStation, endStation);
            return new PathResult("Path not found between " + startStation + " and " + endStation);
        }

        List<String> path = new ArrayList<>();
        int current = endIndex;

        while (current != -1) {
            path.add(0, stations.get(current));
            current = prev[current];
        }

        logger.info("Path found: {} stations, {} minutes", path.size(), dist[endIndex]);
        return new PathResult(path, dist[endIndex], null);
    }

    public List<String> getAllStations() {
        return new ArrayList<>(stations);
    }
}