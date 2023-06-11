package com.epam.rd.autotasks.suffixing;

import org.opentest4j.AssertionFailedError;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
            logger.log(Level.SEVERE, "No suffix is configured");
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
        boolean sourceFileExists = Files.exists(Paths.get(filePath));

        if (!sourceFileExists) {
            logger.log(Level.SEVERE, "No such file: " + filePath.replace('\\', '/'));
            return;
        }
        String sourcePath = filePath.replace('\\', '/');
        String destinationPath = destinationFilePath.replace('\\', '/');

        switch (mode.toLowerCase()) {
            case "copy":
                try {
                    Path source = Path.of(sourcePath);
                    Path destination = Path.of(destinationPath);
                    Files.copy(source, destination, StandardCopyOption.COPY_ATTRIBUTES);
                    logger.log(Level.INFO, sourcePath + " -> " + destinationPath);
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Error copying file: " + e.getMessage());
                }
                break;
            case "move":
                try {
                    Path source = Path.of(sourcePath);
                    Path destination = Path.of(destinationPath);
                    Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
                    logger.log(Level.INFO, sourcePath + " => " + destinationPath);
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Error moving file: " + e.getMessage());
                }
                break;
            default:
                logger.log(Level.SEVERE, "Mode is not recognized: " + mode);
                return; // Stop processing if the mode is not recognized
        }


        // Check if the destination file exists
        File destinationFile = new File(destinationFilePath);
        if (!destinationFile.exists()) {
            logger.log(Level.SEVERE, "File " + destinationPath + " must exist");

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
