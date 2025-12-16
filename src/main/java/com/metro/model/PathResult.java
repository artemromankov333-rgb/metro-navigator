package com.metro.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Model class representing the result of metro path search.
 * Contains information about found route, travel time and possible errors.
 *
 * @author Metro Navigator Team
 * @version 1.0.0
 */
public final class PathResult {

    private final List<String> path;
    private final int totalTime;
    private final String errorMessage;
    private final boolean success;

    /**
     * Constructor for successful path search result.
     *
     * @param path list of stations forming the path
     * @param totalTime total travel time in minutes
     * @throws IllegalArgumentException if path is null/empty or time is negative
     */
    public PathResult(List<String> path, int totalTime) {
        this(path, totalTime, null);
    }

    /**
     * Constructor for error result.
     *
     * @param errorMessage error message
     * @throws IllegalArgumentException if error message is null or empty
     */
    public PathResult(String errorMessage) {
        this(Collections.emptyList(), 0, errorMessage);
    }

    /**
     * Full constructor for path search result.
     *
     * @param path list of station path (can be null or empty for error)
     * @param totalTime total travel time in minutes
     * @param errorMessage error message (null if no error)
     * @throws IllegalArgumentException if both path and errorMessage are null
     */
    public PathResult(List<String> path, int totalTime, String errorMessage) {
        validateParameters(path, totalTime, errorMessage);

        this.path = (path != null) ? Collections.unmodifiableList(new ArrayList<>(path))
                : Collections.emptyList();
        this.totalTime = totalTime;
        this.errorMessage = errorMessage;
        this.success = (errorMessage == null);
    }

    /**
     * Validates constructor parameters.
     */
    private void validateParameters(List<String> path, int totalTime, String errorMessage) {
        if (path == null && errorMessage == null) {
            throw new IllegalArgumentException(
                    "At least one parameter (path or errorMessage) must be non-null"
            );
        }

        if (totalTime < 0) {
            throw new IllegalArgumentException("Travel time cannot be negative");
        }

        if (errorMessage != null && errorMessage.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Error message cannot be empty"
            );
        }
    }

    /**
     * Returns path as list of stations.
     * For successful result returns unmodifiable list of stations.
     * For error result returns empty unmodifiable list.
     *
     * @return list of station path
     */
    public List<String> getPath() {
        return path;
    }

    /**
     * Returns total travel time in minutes.
     * For error result returns 0.
     *
     * @return travel time in minutes
     */
    public int getTotalTime() {
        return totalTime;
    }

    /**
     * Returns error message.
     * For successful result returns null.
     *
     * @return error message or null
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Checks if path search was successful.
     *
     * @return true if path successfully found, false in case of error
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Checks if error occurred during path search.
     *
     * @return true if error occurred, false in case of success
     */
    public boolean hasError() {
        return !success;
    }

    /**
     * Returns number of stations in path.
     * For error result returns 0.
     *
     * @return number of stations in path
     */
    public int getStationCount() {
        return path.size();
    }

    /**
     * Returns textual representation of path.
     * For successful result returns string with station enumeration.
     * For error result returns error message.
     *
     * @return textual representation of result
     */
    public String getFormattedResult() {
        if (hasError()) {
            return "Error: " + errorMessage;
        }

        StringBuilder builder = new StringBuilder();
        builder.append("Path found!\n");
        builder.append("Total time: ").append(totalTime).append(" minutes\n");
        builder.append("Number of stations: ").append(getStationCount()).append("\n");
        builder.append("Route:\n");

        for (int i = 0; i < path.size(); i++) {
            builder.append(String.format("%2d. %s\n", i + 1, path.get(i)));
        }

        return builder.toString();
    }

    /**
     * Returns short path description.
     *
     * @return string in format "from → to (X minutes, Y stations)"
     */
    public String getShortDescription() {
        if (hasError()) {
            return "Path search error";
        }

        if (path.isEmpty()) {
            return "Empty path";
        }

        String start = path.get(0);
        String end = path.get(path.size() - 1);

        return String.format("%s → %s (%d min, %d stations)",
                start, end, totalTime, getStationCount());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        PathResult that = (PathResult) obj;

        return totalTime == that.totalTime &&
                success == that.success &&
                Objects.equals(path, that.path) &&
                Objects.equals(errorMessage, that.errorMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, totalTime, errorMessage, success);
    }

    @Override
    public String toString() {
        return "PathResult{" +
                "path=" + path +
                ", totalTime=" + totalTime +
                ", errorMessage='" + errorMessage + '\'' +
                ", success=" + success +
                '}';
    }

    /**
     * Creates successful path search result.
     *
     * @param path list of station path
     * @param totalTime total travel time
     * @return PathResult object with successful result
     */
    public static PathResult success(List<String> path, int totalTime) {
        return new PathResult(path, totalTime);
    }

    /**
     * Creates error result.
     *
     * @param errorMessage error message
     * @return PathResult object with error
     */
    public static PathResult error(String errorMessage) {
        return new PathResult(errorMessage);
    }

    /**
     * Creates error result based on exception.
     *
     * @param exception exception that caused error
     * @return PathResult object with error
     */
    public static PathResult error(Exception exception) {
        return new PathResult("Error: " + exception.getMessage());
    }
}