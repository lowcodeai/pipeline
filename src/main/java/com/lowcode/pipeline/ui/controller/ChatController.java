package com.lowcode.pipeline.ui.controller;

import com.lowcode.pipeline.ui.request.ChatRequest;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@RestController
//@RequestMapping("/chat")
@SessionAttributes("chatMemory")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);
    private final ChatClient chatClient;

    /**
     * Constructs a new {@code ChatController} with the specified {@link ChatClient.Builder}
     * and {@link ChatMemory} instance.
     *
     * <p>This constructor initializes the {@code chatClient} using the provided builder,
     * setting a default advisor based on the given chat memory context. The advisor enables
     * conversation continuity by tracking chat history and memory.
     * By default, Spring AI will wire up a ChatMemoryRepository → MessageWindowChatMemory </p>
     *
     * @param builder     the builder for creating a {@code ChatClient} instance
     * @param chatMemory  the memory context used to retain conversation state across messages
     */
   public ChatController(ChatClient.Builder builder, ChatMemory chatMemory) {
       this.chatClient = builder
               .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
               .build();
   }

    private static ChatOptions chatOptions() {
        return ChatOptions.builder().temperature(0.3).build();

    }

    @PostMapping("/chat")
    public ResponseEntity<String> chat(@RequestBody ChatRequest req) {
        String conversationId = req.getConversationID();
        String message = req.getMessage();
        log.info("Chat in conversation={}, message={}", conversationId, message);

        String systemMessage = """
                You are an AI assistant whose goal is to gather the necessary information to produce a structured AI pipeline specification based on the user's input.
                
                To achieve this, ask concise, domain-friendly questions **one at a time**, covering the following key aspects in order:
                
                1. The user's **domain** (e.g., healthcare, finance, education).
                2. The user’s **research goals**.
                3. The specific **problem** the user wants to solve with AI, described in plain, non-technical language.
                4. The **type of data** involved (e.g., images, text, time-series) and, if available:
                   - The **source** of the data (optional).
                   - The **format** of the data (mandatory: e.g., CSV, DICOM, JSON).
                   - The **distribution** (e.g., real-time, batch) and **size** of the data (mandatory).
                5. The **desired outcome(s)** the user expects from the AI solution (multiple outcomes may apply).
                6. Any **constraints** that must be respected, including:
                   - **Privacy considerations** (**choose one only**: GDPR, HIPAA, Anonymized, None).
                   - **Legal or regulatory obligations** (optional).
                   - **Ethical concerns** (optional: fairness, bias mitigation, explainability).
                   - **Accuracy or performance requirements** (optional).
                   - **Resource or infrastructure limitations** (optional).
                7. The **target users** who will interact with or benefit from the AI’s output (optional).
                8. The **data privacy approach** the user plans to adopt, if relevant (optional: e.g., anonymization, federated learning, secure storage).
                
                Use simple, domain-appropriate language without introducing technical jargon related to AI, algorithms, programming, or system infrastructure.
                
                Each response must:
                - Be **complete, self-contained, and clearly phrased**, without trailing or unfinished sentences.
                - Maintain a **concise, polite, and professional tone** focused on gathering information efficiently.
                
                Once you have collected sufficient information, provide:
                
                1. A **Plain Text Summary** of the problem, goals, and key details in non-technical language.
                2. A **structured JSON object** that strictly follows the AI Pipeline Problem Specification schema, including all mandatory fields and any optional fields provided by the user.
                
                If any information is missing, unclear, or incomplete, **ask polite follow-up questions before generating the final specification**.
                """;
        var response = chatClient.prompt()
                .options(chatOptions())
                .system(systemMessage)
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();

        // Save response to file
//        if (response.contains("AI Pipeline Specification Summary")) {
//            log.info("Saving AI response to file");
//            saveResponseToFile(conversationId, response);
//        }

        log.info("Saving AI response to file");
        saveResponseToFile(conversationId, response);
        return ResponseEntity.ok(response);
    }

    private void saveResponseToFile(String conversationId, String response) {
        String fileName = "specifications/spec_" + conversationId + ".txt";  // Save as .txt or .json if it's structured
        Path filePath = Paths.get(fileName);

        try {
            Files.createDirectories(filePath.getParent());  // Ensure 'specifications' directory exists
            Files.writeString(filePath, response, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("Saved AI response to {}", filePath.toAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to save AI response", e);
        }
    }
}
