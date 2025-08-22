package com.lowcode.pipeline.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Component
public class GenHandler {

    private static final Logger log = LoggerFactory.getLogger(GenHandler.class);


    /**
     * Saves the AI-generated response to a file named after the given conversation ID.
     *
     * <p>The method creates a file in the {@code specifications/} directory with a filename
     * formatted as {@code spec_<conversationId>.txt}. If the directory does not exist, it
     * is created. The file is overwritten if it already exists.</p>
     *
     * <p>This is useful for persisting AI responses tied to a conversation, such as
     * pipeline specifications or other structured outputs.</p>
     *
     * @param conversationId the unique identifier for the conversation, used in the filename
     * @param response       the content to be saved to the file
     */
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
        String fileName = "Gen/"+ conversationId.substring(0, 7) + ".py";  // Save as .txt or .json if it's structured
        Path filePath = Paths.get(fileName);

        try {
            Files.createDirectories(filePath.getParent());  // Ensure a 'specifications' directory exists
            Files.writeString(filePath, response, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("Saved AI response to {}", filePath.toAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to save AI response", e);
        }
    }
}
