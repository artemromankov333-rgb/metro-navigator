package com.metro.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PathResult Tests")
class PathResultTest {

    private static final List<String> TEST_PATH = Arrays.asList("Station A", "Station B", "Station C");
    private static final int TEST_TIME = 15;
    private static final String TEST_ERROR = "Path search error";

    @Test
    @DisplayName("Successful result - creation and property checks")
    void successfulResult() {
        PathResult result = new PathResult(TEST_PATH, TEST_TIME);

        assertAll(
                () -> assertTrue(result.isSuccess()),
                () -> assertFalse(result.hasError()),
                () -> assertEquals(TEST_TIME, result.getTotalTime()),
                () -> assertEquals(TEST_PATH.size(), result.getStationCount()),
                () -> assertNull(result.getErrorMessage()),
                () -> assertEquals(TEST_PATH, result.getPath())
        );
    }

    @Test
    @DisplayName("Error result")
    void errorResult() {
        PathResult result = new PathResult(TEST_ERROR);

        assertAll(
                () -> assertFalse(result.isSuccess()),
                () -> assertTrue(result.hasError()),
                () -> assertEquals(0, result.getTotalTime()),
                () -> assertEquals(0, result.getStationCount()),
                () -> assertEquals(TEST_ERROR, result.getErrorMessage()),
                () -> assertTrue(result.getPath().isEmpty())
        );
    }

    @Test
    @DisplayName("Factory methods")
    void factoryMethods() {
        PathResult success = PathResult.success(TEST_PATH, TEST_TIME);
        PathResult error = PathResult.error(TEST_ERROR);

        assertAll(
                () -> assertTrue(success.isSuccess()),
                () -> assertTrue(error.hasError())
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("Invalid error message")
    void invalidErrorMessage(String errorMessage) {
        assertThrows(IllegalArgumentException.class, () -> new PathResult(errorMessage));
    }

    @Test
    @DisplayName("Negative time")
    void negativeTime() {
        assertThrows(IllegalArgumentException.class, () -> new PathResult(TEST_PATH, -5));
    }

    @Test
    @DisplayName("Formatted result - success")
    void formattedResultSuccess() {
        PathResult result = new PathResult(TEST_PATH, TEST_TIME);
        String formatted = result.getFormattedResult();

        assertAll(
                () -> assertNotNull(formatted),
                () -> assertTrue(formatted.contains("Path found")),
                () -> assertTrue(formatted.contains(String.valueOf(TEST_TIME)))
        );
    }

    @Test
    @DisplayName("Formatted result - error")
    void formattedResultError() {
        PathResult result = new PathResult(TEST_ERROR);
        String formatted = result.getFormattedResult();

        assertNotNull(formatted);
        assertTrue(formatted.contains(TEST_ERROR));
    }

    @Test
    @DisplayName("Equality and hashCode")
    void equalityAndHashCode() {
        PathResult result1 = new PathResult(TEST_PATH, TEST_TIME);
        PathResult result2 = new PathResult(TEST_PATH, TEST_TIME);
        PathResult result3 = new PathResult(Arrays.asList("Station A", "Station B"), 20);

        assertAll(
                () -> assertEquals(result1, result2),
                () -> assertNotEquals(result1, result3),
                () -> assertEquals(result1.hashCode(), result2.hashCode())
        );
    }

    @Test
    @DisplayName("Path immutability")
    void pathImmutability() {
        List<String> mutablePath = new java.util.ArrayList<>(TEST_PATH);
        PathResult result = new PathResult(mutablePath, TEST_TIME);

        mutablePath.add("New station");

        assertNotEquals(mutablePath.size(), result.getStationCount());
        assertThrows(UnsupportedOperationException.class, () -> result.getPath().add("Another station"));
    }

    @Test
    @DisplayName("Error from exception")
    void errorFromException() {
        Exception exception = new RuntimeException("Test error");
        PathResult result = PathResult.error(exception);

        assertTrue(result.hasError());
        assertTrue(result.getErrorMessage().contains("Test error"));
    }

    @Test
    @DisplayName("ToString contains important information")
    void toStringContainsInfo() {
        PathResult success = new PathResult(TEST_PATH, TEST_TIME);
        PathResult error = new PathResult(TEST_ERROR);

        assertAll(
                () -> assertTrue(success.toString().contains("PathResult")),
                () -> assertTrue(success.toString().contains("success=true")),
                () -> assertTrue(error.toString().contains(TEST_ERROR))
        );
    }

    @Test
    @DisplayName("Empty path with error")
    void emptyPathWithError() {
        PathResult result = new PathResult(Collections.emptyList(), 0, TEST_ERROR);

        assertAll(
                () -> assertTrue(result.hasError()),
                () -> assertEquals(TEST_ERROR, result.getErrorMessage()),
                () -> assertTrue(result.getPath().isEmpty())
        );
    }
}