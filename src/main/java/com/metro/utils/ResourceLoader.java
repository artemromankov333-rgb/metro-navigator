package com.metro.utils;

import com.metro.gui.MetroGUI;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ResourceLoader {

    private static final String RESOURCES_BASE_PATH = "main/resources/";

    public static Image loadImage(String fileName) {
        try {
            InputStream is = getResourceStream(fileName);
            if (is != null) {
                return ImageIO.read(is);
            }
            System.err.println("Resource not found: " + fileName);
            return null;
        } catch (IOException e) {
            System.err.println("Error loading image " + fileName + ": " + e.getMessage());
            return null;
        }
    }

    public static List<String> readTextFile(String fileName) {
        List<String> lines = new ArrayList<>();

        try {
            InputStream is = getResourceStream(fileName);
            if (is != null) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        lines.add(line);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return lines;
    }

    public static List<MetroGUI.Station> loadStations() {
        List<MetroGUI.Station> stations = new ArrayList<>();
        List<String> lines = readTextFile("stations.txt");

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            String[] parts = line.split(";");
            if (parts.length >= 4) {
                try {
                    String name = parts[0];
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);
                    String lineName = parts[3];
                    stations.add(new MetroGUI.Station(name, x, y, lineName));
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing station line: " + line);
                }
            }
        }

        return stations;
    }

    private static InputStream getResourceStream(String fileName) {
        InputStream is = ResourceLoader.class.getClassLoader()
                .getResourceAsStream(fileName);

        if (is == null) {
            is = ResourceLoader.class.getClassLoader()
                    .getResourceAsStream("resources/" + fileName);
        }

        if (is == null) {
            is = ResourceLoader.class.getClassLoader()
                    .getResourceAsStream("main/resources/" + fileName);
        }

        if (is == null) {
            try {
                File file = new File(fileName);
                if (file.exists()) {
                    is = new FileInputStream(file);
                }
            } catch (IOException e) {
                // Ignore
            }
        }

        return is;
    }
}
