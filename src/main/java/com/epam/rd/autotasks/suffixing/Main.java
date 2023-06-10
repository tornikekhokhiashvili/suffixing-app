package com.epam.rd.autotasks.suffixing;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        if (args.length != 1) {
            logger.log(Level.SEVERE, "Please provide the path to a config file as an argument.");
            return;
        }

        String configFile = args[0];

        try (FileInputStream input = new FileInputStream(configFile)) {
            Properties properties = new Properties();
            properties.load(input);
            processConfig(properties);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error reading the config file: " + configFile, e);
        }
    }

    private static void processConfig(Properties properties) {
        String mode = properties.getProperty("mode");
        String suffix = properties.getProperty("suffix");
        String files = properties.getProperty("files");

        if (mode == null || mode.isEmpty()) {
            logger.log(Level.SEVERE, "No mode is specified in the config.");
            return;
        }

        if (suffix == null || suffix.isEmpty()) {
            logger.log(Level.SEVERE, "No suffix is specified in the config.");
            return;
        }

        if (files == null || files.isEmpty()) {
            logger.log(Level.WARNING, "No files are configured to be copied/moved.");
            return;
        }

        String[] filePaths = files.split(":");

        for (String filePath : filePaths) {
            processFile(filePath.trim(), mode, suffix);
        }
    }

    private static void processFile(String filePath, String mode, String suffix) {
        String destinationFilePath = addSuffixToFile(filePath, suffix);

        if (destinationFilePath == null) {
            logger.log(Level.SEVERE, "No such file: " + filePath.replace('\\', '/'));
            return;
        }

        switch (mode.toLowerCase()) {
            case "copy":
                logger.log(Level.INFO, filePath.replace('\\', '/') + " -> " + destinationFilePath.replace('\\', '/'));
                // Copy the file to the destinationFilePath
                break;
            case "move":
                logger.log(Level.INFO, filePath.replace('\\', '/') + " => " + destinationFilePath.replace('\\', '/'));
                // Move the file to the destinationFilePath
                break;
            default:
                logger.log(Level.SEVERE, "Mode is not recognized: " + mode);
        }
    }
    private static String addSuffixToFile(String filePath, String suffix) {
        Path path = Paths.get(filePath);
        String fileName = path.getFileName().toString();
        String extension = "";

        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = fileName.substring(dotIndex);
            fileName = fileName.substring(0, dotIndex);
        }

        String newFileName = fileName + suffix + extension;
        Path newPath = path.resolveSibling(newFileName);

        return newPath.toString();
    }

}
