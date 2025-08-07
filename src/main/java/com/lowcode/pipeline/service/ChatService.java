package com.lowcode.pipeline.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

@Service
public class ChatService {

    private final ChatClient chatClient;

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    public ChatService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String chat(String conversationId, String message) {
        log.info("Chat in conversation={}, message={}", conversationId, message);

//        Houari Message
//        String systemMessage = """
//                  I'm a researcher in software engineering and AI and I was contacted by a researcher asking me to help
//                  him define an AI pipeline to solve a problem. This researcher is good in his domain and data analysis,
//                  but he is not familiar with computer science and AI. So help me asking the right questions to have a
//                  precise specification of his problem.
//
//                  Please ask the question and I will ask him to respond. At the end I need a structured description of
//                  the problem. Let's do it interactively. Ask one question at a time
//                  """;

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
                2. A **Structured JSON object** that strictly follows the AI Pipeline Problem Specification schema, including all mandatory fields and any optional fields provided by the user.

                If any information is missing, unclear, or incomplete, **ask polite follow-up questions before generating the final specification**.
                """;



        var content = chatClient.prompt()
                .options(chatOptions())
                .system(systemMessage)
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();

        Optional<String> response = Optional.ofNullable(content);

        // Save response to file and then create a configuration
        if (response.isPresent() && response.get().contains("Plain Text Summary")) {
            log.info("Saving AI response to file");
            saveResponseToFile(conversationId, response.get());

            // Configure a feature model
//            response = configure(conversationId, response);

        }

        return response.orElse("");
    }

    public String configure(String conversationId, String message) {
        log.info("Feature Model Configuration in conversation={} ", conversationId);

        String systemMessage = """
                You are an AI assistant tasked with creating a valid configuration of a feature model for an AI pipeline,
                using a structured specification provided by the user, create a valid configuration of the feature model
                below. Ensure that the configuration satisfies all constraints and feature relationships defined in the
                feature model. You can ask me additional questions or clarification if needed to create a valid configuration.

                # Feature Model:
                namespace AI_Pipeline

                features
                	AI_Pipeline {extended__ true, abstract true}
                		mandatory
                			Analysis_Type
                				alternative
                					Classification
                					Prediction
                					Anomaly_Detection
                					Clustering
                			Input_Data
                				mandatory
                					Type
                						or
                							Text
                							Image
                								alternative
                									Natural_Images
                									Medical_Scans
                							Audio
                							Video
                							AV_Type
                								alternative
                									Speech
                									Music
                					Distribution
                						alternative
                							Balanced
                							Unbalanced
                					Format
                						alternative
                							Structured
                							Unstructured
                				optional
                					Size
                						optional
                							Dozens
                							Hundreds
                							Thousands
                					Data_Privacy
                						or
                							Anonymized
                							Pseudonymized
                							PII_Removed
                			Desired_Output
                				alternative
                					Binary_Classification
                					Multi_Class_Classification

                constraints
                	Classification => Image | Text
                	Clustering => !Speech
                	Anomaly_Detection => Structured
                	Audio | Video => AV_Type

                """;

        var content = chatClient.prompt()
                .options(chatOptions())
                .system(systemMessage)
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();

        return Optional.ofNullable(content).orElse("No response");
    }

    private static ChatOptions chatOptions() {
        return ChatOptions.builder().temperature(0.3).build();

    }

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
    private void saveResponseToFile(String conversationId, String response) {
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
}
