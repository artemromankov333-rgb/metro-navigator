package com.metro;

import com.metro.gui.MetroGUI;
import com.metro.utils.LoggerUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;

/**
 * Main class of Metro Navigator application.
 */
public class Main {

    private static final Logger logger = LogManager.getLogger(Main.class);

    /**
     * Application entry point.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        try {
            logger.info("Starting Metro Navigator application");

            SwingUtilities.invokeLater(() -> {
                try {
                    new MetroGUI();
                    logger.info("GUI successfully initialized");
                } catch (Exception e) {
                    logger.error("Error creating GUI: {}", e.getMessage(), e);
                    JOptionPane.showMessageDialog(
                            null,
                            "Failed to initialize application GUI:\n" + e.getMessage(),
                            "Initialization Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            });

            logger.info("Application startup completed");

        } catch (Exception e) {
            logger.error("Critical application error: {}", e.getMessage(), e);
            System.err.println("Critical error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}