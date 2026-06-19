package com.lowcode.pipeline.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Component
public class OutputHandler {

    private static final Logger log = LoggerFactory.getLogger(OutputHandler.class);


    public void saveResponseToFile(String conversationId, String response) {
        String fileName = "specifications/spec_" + conversationId + ".txt";  // Save as .txt or .json if it's structured
        Path filePath = Paths.get(fileName);

        try {
            Files.createDirectories(filePath.getParent());  // Ensure a 'specifications' directory exists
            Files.writeString(filePath, response, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("Saved AI response to {}", filePath.toAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to save AI response", e);
        }
    }

    public void generatePythonCode(String conversationId, String response) {
        String fileName = "Gen/"+ conversationId + ".py";  // Save as .txt or .json if it's structured
        Path filePath = Paths.get(fileName);

        try {
            Files.createDirectories(filePath.getParent());  // Ensure a 'specifications' directory exists
            Files.writeString(filePath, response, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("Saved AI response to {}", filePath.toAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to save AI response", e);
        }
    }

    /**
     * Read a file from the filesystem rather than via Spring ResourceLoader.
     */
    public String readFile(String location) throws IOException {
        Path filePath = Paths.get(location);
        if (!Files.exists(filePath)) {
            throw new IOException("File not found: " + filePath.toAbsolutePath());
        }
        return Files.readString(filePath, StandardCharsets.UTF_8);
    }
}