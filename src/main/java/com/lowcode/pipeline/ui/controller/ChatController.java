package com.lowcode.pipeline.ui.controller;

import com.lowcode.pipeline.ui.request.ChatRequest;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.ChatOptions;
//import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.web.client.RestTemplate;
//import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@RestController
@SessionAttributes("chatMemory")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private RestTemplate restTemplate;

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
                2. A **Structured JSON object** that strictly follows the AI Pipeline Problem Specification schema, including all mandatory fields and any optional fields provided by the user.

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
        if (response.contains("Plain Text Summary")) {
            log.info("Saving AI response to file");
            saveResponseToFile(conversationId, response);

            // Create new request
//            req = new ChatRequest(conversationId, response);
//
//            String configureUrl = "http://localhost:8080/configure";
//
//            try {
//                response = restTemplate.postForEntity(configureUrl, req, String.class).getBody();
//            } catch (RestClientException e) {
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                        .body("Failed to call /chatB: " + e.getMessage());
//            }

        }

        return ResponseEntity.ok(response);
    }

//    @PostMapping("/chat")
//    public ResponseEntity<String> chat(@RequestBody ChatRequest req) {
//        String conversationId = req.getConversationID();
//        String message = req.getMessage();
//        log.info("Chat in conversation={} ", conversationId);
//
//        String configureUrl = "http://localhost:8080/configure";
//
//        try {
//            ResponseEntity<String> response = restTemplate.postForEntity(configureUrl, req, String.class);
//            return ResponseEntity.ok("Configuration: " + response.getBody());
//        } catch (RestClientException e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Failed to call /chatB: " + e.getMessage());
//        }
//    }


//    @PostMapping("/configure")
//    public ResponseEntity<String> configure(@RequestBody ChatRequest req) {
//        String conversationId = req.getConversationID();
//        String message = req.getMessage();
//        log.info("Feature Model Configuration in conversation={} ", conversationId);
//
//        String systemMessage = """
//                You are an AI assistant tasked with generating a valid configuration of a feature model for an AI pipeline, using a structured specification provided by the user.
//
//                Your primary goal is to:
//
//                Derive a valid configuration that satisfies all the constraints and feature relationships defined in the model.
//
//                Request clarification if the pipeline specification is incomplete, ambiguous, or would otherwise result in an invalid configuration.
//
//                Use simple, domain-appropriate language. Avoid technical jargon related to AI, programming, or system infrastructure. Keep your tone concise, polite, and professional.
//
//                Response Guidelines:
//
//                Your output must be self-contained and clearly written, without unfinished sentences.
//
//                Ask clear and polite follow-up questions if necessary information is missing.
//
//                Only generate a final feature model configuration after all required information has been gathered.
//
//                Ensure the configuration respects all mandatory, optional, alternative, and ‘or’ feature relationships, as well as any cross-feature constraints.
//
//                Once complete, output the final valid configuration using the structure defined by the feature model.
//
//                # Feature Model:
//                namespace AI_Pipeline
//
//                features
//                	AI_Pipeline {extended__ true, abstract true}
//                		mandatory
//                			Analysis_Type
//                				alternative
//                					Classification
//                					Prediction
//                					Anomaly_Detection
//                					Clustering
//                			Input_Data
//                				mandatory
//                					Type
//                						or
//                							Text
//                							Image
//                								alternative
//                									Natural_Images
//                									Medical_Scans
//                							Audio
//                							Video
//                							AV_Type
//                								alternative
//                									Speech
//                									Music
//                					Distribution
//                						alternative
//                							Balanced
//                							Unbalanced
//                					Format
//                						alternative
//                							Structured
//                							Unstructured
//                				optional
//                					Size
//                						optional
//                							Dozens
//                							Hundreds
//                							Thousands
//                					Data_Privacy
//                						or
//                							Anonymized
//                							Pseudonymized
//                							PII_Removed
//                			Desired_Output
//                				alternative
//                					Binary_Classification
//                					Multi_Class_Classification
//
//                constraints
//                	Classification => Image | Text
//                	Clustering => !Speech
//                	Anomaly_Detection => Structured
//                	Audio | Video => AV_Type
//
//                """;
////        var promptMessage = """
////                Create a a configuration that satisfies all constraints and feature relationships defined in the feature model below.
////
////                # Feature Model:
////                namespace AI_Pipeline
////
////                features
////                	AI_Pipeline {extended__ true, abstract true}
////                		mandatory
////                			Analysis_Type
////                				alternative
////                					Classification
////                					Prediction
////                					Anomaly_Detection
////                					Clustering
////                			Input_Data
////                				mandatory
////                					Type
////                						or
////                							Text
////                							Image
////                								alternative
////                									Natural_Images
////                									Medical_Scans
////                							Audio
////                							Video
////                							AV_Type
////                								alternative
////                									Speech
////                									Music
////                					Distribution
////                						alternative
////                							Balanced
////                							Unbalanced
////                					Format
////                						alternative
////                							Structured
////                							Unstructured
////                				optional
////                					Size
////                						optional
////                							Dozens
////                							Hundreds
////                							Thousands
////                					Data_Privacy
////                						or
////                							Anonymized
////                							Pseudonymized
////                							PII_Removed
////                			Desired_Output
////                				alternative
////                					Binary_Classification
////                					Multi_Class_Classification
////
////                constraints
////                	Classification => Image | Text
////                	Clustering => !Speech
////                	Anomaly_Detection => Structured
////                	Audio | Video => AV_Type
////
////                Example 1:
////                Create a configuration of the feature model.
////                YAML Response 1:
////                ```
////                AI_Pipeline:
////                  Analysis_Type: Classification
////                  Input_Data:
////                    Type:
////                      Image: Medical Scans
////                    Distribution: Balanced
////                    Format: Structured
////                  Desired_Output: Binary Classification
////                ```
////                This is a valid configiuration because it satisfies all constraints and feature relationships defined
////                in the feature model.
////
////                Example 2:
////                Create a configuration of the feature model.
////                YAML Response 2:
////                ```
////                AI_Pipeline:
////                  Analysis_Type: Classification
////                  Input_Data:
////                    Type:
////                      Image: Medical Scans
////                    Distribution: Balanced
////                    Format: Structured
////                    Size: Hundreds
////                    Data_Privacy:
////                      - Anonymized
////                      - PII_Removed
////                  Desired_Output: Binary Classification
////                ```
////                This is a valid configiuration because it satisfies all constraints and feature relationships defined
////                in the feature model, and includes optional features.
////
////                Example 3:
////                Create a configuration of the feature model.
////                YAML Response 3:
////                ```
////                AI_Pipeline:
////                  Analysis_Type: Classification
////                  Input_Data:
////                    Type:
////                      Image: Medical Scans
////                    Format: Structured
////                    Size: Hundreds
////                    Data_Privacy:
////                      - Anonymized
////                  Desired_Output: Binary Classification
////                ```
////                This is an invalid configuration because it does not select the required feature "Distribution"
////
////                Example 4:
////                Create a configuration of the feature model.
////                YAML Response 4:
////                ```
////                AI_Pipeline:
////                  Analysis_Type:
////                    - Classification
////                    - Prediction
////                  Input_Data:
////                    Type:
////                      Image: Medical Scans
////                    Distribution: Balanced
////                    Format: Structured
////                    Size: Hundreds
////                    Data_Privacy:
////                      - Anonymized
////                  Desired_Output: Binary Classification
////                ```
////                This is an invalid configuration because it does not select exactly one of the "Analysis_Type"
////
////                Example 5:
////                Create a configuration of the feature model.
////                YAML Response 5:
////                ```
////                AI_Pipeline:
////                  Analysis_Type: Classification
////                  Input_Data:
////                    Type: Audio
////                    Distribution: Balanced
////                    Format: Structured
////                    Size: Hundreds
////                    Data_Privacy:
////                      - Anonymized
////                  Desired_Output: Binary Classification
////                ```
////                This is an invalid configuration because selecting "Classification" excludes "Audio"
////
////                Example 6:
////                Create a configuration of the feature model.
////                YAML Response 6:
////                ```
////                AI_Pipeline:
////                  Analysis_Type: Clustering
////                  Input_Data:
////                    Type: Audio
////                    AV_Type: Speech
////                    Distribution: Balanced
////                    Format: Structured
////                    Size: Hundreds
////                    Data_Privacy:
////                      - Anonymized
////                      - PII_Removed
////                  Desired_Output: Binary Classification
////                ```
////                This is an invalid configuration because selecting "Clustering" excludes "Speech"
////
////                Example 7:
////                Create a configuration of the feature model.
////                YAML Response 7:
////                ```
////                AI_Pipeline:
////                  Analysis_Type: Anomaly Detection
////                  Input_Data:
////                    Type:
////                      Image: Medical Scans
////                    Distribution: Balanced
////                    Format: Unstructured
////                    Size: Hundreds
////                    Data_Privacy:
////                      - Anonymized
////                      - PII_Removed
////                  Desired_Output: Binary Classification
////                ```
////                This is an invalid configuration because selecting "Anomaly Detection" excludes "Unstructured"
////                """;
//        var promptMessage = """
//                Create a a configuration that satisfies all constraints and feature relationships defined in the feature model below.
//
//                # Feature Model:
//                namespace AI_Pipeline
//
//                features
//                	AI_Pipeline {extended__ true, abstract true}
//                		mandatory
//                			Analysis_Type
//                				alternative
//                					Classification
//                					Prediction
//                					Anomaly_Detection
//                					Clustering
//                			Input_Data
//                				mandatory
//                					Type
//                						or
//                							Text
//                							Image
//                								alternative
//                									Natural_Images
//                									Medical_Scans
//                							Audio
//                							Video
//                							AV_Type
//                								alternative
//                									Speech
//                									Music
//                					Distribution
//                						alternative
//                							Balanced
//                							Unbalanced
//                					Format
//                						alternative
//                							Structured
//                							Unstructured
//                				optional
//                					Size
//                						optional
//                							Dozens
//                							Hundreds
//                							Thousands
//                					Data_Privacy
//                						or
//                							Anonymized
//                							Pseudonymized
//                							PII_Removed
//                			Desired_Output
//                				alternative
//                					Binary_Classification
//                					Multi_Class_Classification
//
//                constraints
//                	Classification => Image | Text
//                	Clustering => !Speech
//                	Anomaly_Detection => Structured
//                	Audio | Video => AV_Type
//
//                Example Response 1:
//                ```
//                AI_Pipeline:
//                  Analysis_Type: Classification
//                  Input_Data:
//                    Type:
//                      Image: Medical Scans
//                    Distribution: Balanced
//                    Format: Structured
//                  Desired_Output: Binary Classification
//                ```
//
//                Example Response 2:
//                ```
//                AI_Pipeline:
//                  Analysis_Type: Classification
//                  Input_Data:
//                    Type:
//                      Image: Medical Scans
//                    Distribution: Balanced
//                    Format: Structured
//                    Size: Hundreds
//                    Data_Privacy:
//                      - Anonymized
//                      - PII_Removed
//                  Desired_Output: Binary Classification
//                ```
//                """;
//
//        var response = chatClient.prompt(promptMessage)
//                .options(chatOptions())
//                .system(systemMessage)
//                .user(message)
//                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
//                .call()
//                .content();
//
//        return ResponseEntity.ok(response);
//    }

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
            Files.createDirectories(filePath.getParent());  // Ensure 'specifications' directory exists
            Files.writeString(filePath, response, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("Saved AI response to {}", filePath.toAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to save AI response", e);
        }
    }
}
