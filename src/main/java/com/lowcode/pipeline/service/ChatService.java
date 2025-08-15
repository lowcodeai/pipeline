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

    //        Houari Message
        String systemMessage = """
                You are an AI assistant whose goal is to gather the necessary information to produce a
                structured AI pipeline specification based on the user's input.
                
                To achieve this, ask concise, domain-friendly questions **one at a time**, covering the following key aspects in order:
                If any information is missing, unclear, or incomplete, **ask polite follow-up questions before moving to the next question**.
                
                Step 1: Domain and Problem Understanding
                1.1. The user's domain (e.g., healthcare, finance, education).
                1.2. The main problem or research goals.
                1.3. Why does the user wants to solve the problem using AI.
                1.4. The current solution or workaround (if any).
                
                Step 2: Data Description
                2.1. What kind of data does the user has access to.
                2.2. How is the data currently stored and organized.
                2.3. What are the key variables or features in the data.
                2.4. The size of the dataset.
                2.5. Is the data labeled? If so, how?
                2.6. Are there any known issues with the data?
                
                Step 3: Task Definition
                3.1. What kind of output does the user expect from the system.
                3.2. How will the user evaluate whether the system is successful.
                3.3. What is the tolerance for errors?
                
                Step 4: Constraints and Deployment
                4.1. Are there constraints that must be respected, including:
                    a. Privacy considerations.
                    b. Legal or regulatory obligations
                    c. Ethical concerns
                4.2. The intended users of the system.
                
                Use simple, domain-appropriate language without introducing technical jargon related to AI, algorithms, programming, or system infrastructure.
            
                At the end, provide:

                1. A **Plain Text Summary** of the problem, goals, and key details in non-technical language.
                2. A **Structured JSON object** that follows the AI Pipeline Problem Specification schema.
                """;

//    private String systemMessage = """
//                You are an AI assistant whose goal is to gather the necessary information to produce a structured AI pipeline specification based on the user's input.
//
//                To achieve this, ask concise, domain-friendly questions **one at a time**, covering the following key aspects in order:
//                If any information is missing, unclear, or incomplete, **ask polite follow-up questions before moving to the next question**.
//
//                1. The user's **domain** (e.g., healthcare, finance, education).
//                2. The user’s **research goals**.
//                3. The specific **problem** the user wants to solve with AI, described in plain, non-technical language.
//                4. The **type of data** involved (e.g., images, text, time-series) and, if available:
//                   - The **source** of the data (optional).
//                   - The **format** of the data (mandatory: e.g., CSV, DICOM, JSON).
//                   - The **distribution** (e.g., real-time, batch)
//                   - The **size** of the data (mandatory).
//                5. The **desired outcome(s)** the user expects from the AI solution (multiple outcomes may apply).
//                6. Any **constraints** that must be respected, including:
//                   - **Privacy considerations** (**choose one only**: e.g., GDPR, HIPAA, Anonymized, None).
//                   - **Legal or regulatory obligations** (optional).
//                   - **Ethical concerns** (optional: fairness, bias mitigation, explainability).
//                7. The **target users** who will interact with or benefit from the AI’s output (optional).
//
//                Use simple, domain-appropriate language without introducing technical jargon related to AI, algorithms, programming, or system infrastructure.
//
//                At the end, provide:
//
//                1. A **Plain Text Summary** of the problem, goals, and key details in non-technical language.
//                2. A **Structured JSON object** that strictly follows the AI Pipeline Problem Specification schema, including all mandatory fields and any optional fields provided by the user.
//                """;

//    private String systemMessage = """
//            You are an AI assistant tasked with gathering all necessary information to generate a complete AI pipeline in Python.
//
//            Based on the user's initial input:
//            ""\"
//            Thank you for the information! Now, let's summarize everything you've shared.
//
//            ### Plain Text Summary:
//            You are working in the healthcare domain, specifically focusing on improving the detection of brain tumors from MRI scans. The current manual approach is labor-intensive, error-prone, and time-consuming. You have a dataset of about 1500 individual MRI scans in DICOM format, which is distributed in batches. Your goal is to develop a binary classification model to determine the presence of a brain tumor. You plan to evaluate the model using precision, recall, and F1 score. Additionally, you must adhere to HIPAA regulations, and your target users are radiologists and researchers. There are no specific tools in mind, and there is no timeline or deadline for the project.
//
//            ### Structured JSON Object:
//            ```json
//            {
//              "domain": "healthcare",
//              "research_goals": "Improve detection of brain tumors from MRI scans.",
//              "problem": "The current manual approach for detecting brain tumors from MRI images is labor-intensive, error-prone, and time-consuming.",
//              "data": {
//                "type": "images",
//                "format": "DICOM",
//                "distribution": "batch",
//                "size": 1500
//              },
//              "desired_outcomes": [
//                "Binary classification of brain tumor presence."
//              ],
//              "constraints": {
//                "privacy_considerations": "HIPAA"
//              },
//              "target_users": [
//                "radiologists",
//                "researchers"
//              ]
//            }
//            ```
//
//            If you need any further assistance or modifications, feel free to ask!""\"
//
//            Ask the user one question at a time to gather complete information for the following stages of the pipeline:
//
//            1. Data Processing
//            2. Data Splitting
//            3. Model Selection
//            4. Model Evaluation
//
//            For each step, ask relevant, clear, and concise questions. **Do not move to the next step** until you have all the required details for the current one. If any information is missing or ambiguous, ask polite follow-up questions.
//
//            **At the end**, generate and return **only valid Python code**. Do not include any explanations, comments, metadata, or additional text—just the Python code required to:
//            - load the data,
//            - preprocess it,
//            - split it into train/test sets,
//            - train the selected model,
//            - evaluate it, and
//            - save the trained model.
//            """;

    public ChatService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String problemDefinition(String conversationId, String message) {
        log.info("Chat in conversation={}, message={}", conversationId, message);

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

            // Update the system message
            updateSystemMessage(response.get());

        }

        return response.orElse("");
    }

    private void updateSystemMessage(String message) {
        log.info("Update system message after problem specification");

        systemMessage = """
            You are an AI assistant tasked with gathering all necessary information to generate a complete AI pipeline in Python.
            
            Based on the user's initial input:
            \"""
            """ + message + """
            \"""
            
            Ask the user one question at a time to gather complete information for the following stages of the pipeline:
            
            1. Data Processing
            2. Data Splitting
            3. Model Selection  
            4. Model Evaluation
            
            For each step, ask relevant, clear, and concise questions. **Do not move to the next step** until you have all the required details for the current one. If any information is missing or ambiguous, ask polite follow-up questions.
            
            **At the end**, generate and return **only valid Python code**. Do not include any explanations, comments, metadata, or additional text—just the Python code required to:
            - load the data,
            - preprocess it,
            - split it into train/test sets,
            - train the selected model,
            - evaluate it, and
            - save the trained model.
            """;


        log.info("Current system message: {}", systemMessage);

    }

    public String pipelineSpecification(String conversationId, String message) {
        log.info("Requesting pipeline information in Chat ID ={}", conversationId);

        var content = chatClient.prompt()
                .options(chatOptions())
                .system(systemMessage)
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();

        Optional<String> response = Optional.ofNullable(content);
        // Save response to file and then create a configuration
        if (response.isPresent()) {
            log.info("Saving generated code to file");
            generatePythonCode(conversationId, response.get());

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

    private void generatePythonCode(String conversationId, String response) {
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
