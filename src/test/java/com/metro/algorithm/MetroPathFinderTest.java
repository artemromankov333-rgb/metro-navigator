package com.metro.algorithm;

import com.metro.model.PathResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MetroPathFinder Tests")
class MetroPathFinderTest {

    @TempDir
    Path tempDir;
    private MetroPathFinder pathFinder;

    @BeforeEach
    void setUp() throws Exception {
        // Create test matrix file
        File testFile = tempDir.resolve("test_metro.txt").toFile();
        createTestMatrixFile(testFile);
        pathFinder = new MetroPathFinder(testFile.getPath());
    }

    private void createTestMatrixFile(File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(",A,B,C\n");
            writer.write("A,0,2,9\n");
            writer.write("B,2,0,3\n");
            writer.write("C,9,3,0\n");
        }
    }

    @Test
    @DisplayName("Find existing path")
    void findExistingPath() {
        PathResult result = pathFinder.findShortestPath("A", "B");

        assertAll(
                () -> assertTrue(result.isSuccess()),
                () -> assertEquals(2, result.getTotalTime()),
                () -> assertEquals(2, result.getStationCount())
        );
    }

    @Test
    @DisplayName("Find path with transfer")
    void findPathWithTransfer() {
        PathResult result = pathFinder.findShortestPath("A", "C");

        assertAll(
                () -> assertTrue(result.isSuccess()),
                () -> assertEquals(5, result.getTotalTime()), // A→B(2) + B→C(3)
                () -> assertEquals(3, result.getStationCount())
        );
    }

    @Test
    @DisplayName("Station not found")
    void stationNotFound() {
        PathResult result = pathFinder.findShortestPath("A", "Nonexistent");

        assertTrue(result.hasError());
        assertTrue(result.getErrorMessage().contains("Station not found"));
    }

    @Test
    @DisplayName("Get all stations")
    void getAllStations() {
        List<String> stations = pathFinder.getAllStations();

        assertAll(
                () -> assertNotNull(stations),
                () -> assertFalse(stations.isEmpty()),
                () -> assertTrue(stations.contains("A")),
                () -> assertTrue(stations.contains("B")),
                () -> assertTrue(stations.contains("C"))
        );
    }

    @Test
    @DisplayName("Path from station to itself")
    void pathToSameStation() {
        PathResult result = pathFinder.findShortestPath("A", "A");

        assertAll(
                () -> assertTrue(result.isSuccess()),
                () -> assertEquals(0, result.getTotalTime()),
                () -> assertEquals(1, result.getStationCount()),
                () -> assertEquals("A", result.getPath().get(0))
        );
    }

    @Test
    @DisplayName("Non-existent matrix file")
    void nonExistentMatrixFile() {
        assertThrows(Exception.class, () -> {
            new MetroPathFinder("nonexistent_file.txt");
        });
    }

    @Test
    @DisplayName("Check path validity")
    void pathValidity() {
        PathResult result = pathFinder.findShortestPath("A", "C");
        List<String> path = result.getPath();

        assertAll(
                () -> assertNotNull(path),
                () -> assertEquals("A", path.get(0)),
                () -> assertEquals("C", path.get(path.size() - 1)),
                () -> assertTrue(path.contains("B"))
        );
    }
}