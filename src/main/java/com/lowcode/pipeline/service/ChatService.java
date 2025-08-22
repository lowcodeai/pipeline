package com.lowcode.pipeline.service;

import com.lowcode.pipeline.util.GenHandler;
import com.lowcode.pipeline.util.SystemMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ChatService {

    private final ChatClient chatClient;

    private SystemMessage systemMessage;

    private GenHandler genHandler;

    private int counter = 0;

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    public ChatService(ChatClient chatClient, SystemMessage systemMessage, GenHandler genHandler) {

        this.chatClient = chatClient;
        this.systemMessage = systemMessage;
        this.genHandler = genHandler;
    }

    public String problemDefinition(String conversationId, String message) {
        log.info("Chat in conversation={}, message={}", conversationId, message);

        var content = chatClient.prompt()
                .options(chatOptions())
                .system(systemMessage.getSystemMessage())
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();

        Optional<String> response = Optional.ofNullable(content);

        // Save response to file and then create a configuration
        if (response.isPresent() && response.get().contains("Plain Text Summary")) {
            log.info("Saving AI response to file");
            genHandler.saveResponseToFile("problem" + conversationId.substring(0, 5), response.get());

            // Update the system message
            systemMessage.updateSystemMessageForCompute(response.get());

        }

        return response.orElse("");
    }


    public String computeSpecification(String conversationId, String message) {
        log.info("Requesting information for Compute and Tooling Specification ={}", conversationId);

//        String systemM = """
//                You are an AI assistant tasked with gathering ONLY the COMPUTE and RUN-TIME PATHS information for an
//                AI pipeline based on the user's input. The user is not a Computer Science or AI specialist, and you are
//                encouraged to provide contextual information that guides the user's responses to your questions.
//
//                To achieve this, ask concise, domain-friendly questions **one at a time**, covering the following key aspects in order:
//                If any information is missing, unclear, or incomplete, **ask polite follow-up questions before moving to the next question**.
//
//                Step 1: Hardware
//                - Type of hardware
//                -
//
//                Interaction rules:
//                - Be concise, avoid jargon unless explained in plain language, and focus on basic information that have impact on the pipeline code.
//                - After each user reply, ask the NEXT best single question. Never ask multiple questions at once.
//                - Use the provided context to specialize your questions; do not restate it verbatim.
//                - When you have enough information the hardware and data location details, generate a complete Compute
//                Specification in JSON format, then stop.
//
//
//                Context (semi‑structured problem follows). Use its details to specialize your questions instead of asking generic ones.
//
//                """;

        String systemM = """
                You are an AI assistant tasked with gathering COMPUTE HARDWARE and PATH details needed for an AI/ML pipeline.
                The user is not a Computer Science or AI specialist, and you are encouraged to provide contextual information 
                that guides the user's input. 
                
                To achieve this, ask concise, domain-friendly questions **one at a time**, covering the following key aspects in order:
                If any information is missing, unclear, or incomplete, **ask polite follow-up questions before moving to the next question**.
                If the user enters "Not Applicable", mark the corresponding field as unknown, and move on to the next question.
                
                Start by asking what type of compute environment the user has (e.g., Are you working on a local workstation, on-prem cluster, or in the cloud?):
                - local workstation,
                - on-prem/HPC cluster, or
                - cloud instance/service. 
                
                Based on their answer, adapt your follow-up questions to gather: 
                - GPU: model and VRAM in GB 
                - CPU: model and number of cores (or vCPUs) 
                - RAM: total system memory in GB 
                - Storage: mount/path or service (local disk, network, or cloud), type (nvme, ssd, hdd, network, cloud), free space if known, and notes \s
                - Paths: 
                  - project_root (where code/configs live) 
                  - data_train, data_val, data_test (where training/validation/test data are located) 
                  - labels_location (if labels are separate) 
                  - outputs_root (base folder for outputs) 
                  - checkpoints_dir (where to save model checkpoints) 
                  - logs_dir (where to save training logs) 
                  
                After the logs directory question, **STOP** asking questions and generate valid JSON containing exactly the target fields, and STOP. No extra text before or after the JSON.
                
                Context (semi‑structured problem follows). Use its details to specialize your questions instead of asking generic ones.
                
                """;

        String problemDefn = """
                Thank you for confirming that there are no additional requirements. To summarize our discussion: ### Plain Text Summary You are working in the healthcare domain, specifically focusing on detecting brain tumors through MRI scans. Your goal is to develop an AI system that can classify MRI images as either showing the presence or absence of a tumor. You aim to use AI for this task to improve diagnostic accuracy and efficiency for radiologists and researchers. You have a dataset of 1,500 labeled MRI scans, where the labels indicate whether a tumor is present. The data is well-organized, with no known issues, and you plan to evaluate the AI system's performance using accuracy, precision, recall, and F1 score. Additionally, you must ensure that the data is anonymized and compliant with healthcare regulations. There are no specific deployment preferences. ### Structured JSON Object
                json
                {
                  "domain": "healthcare",
                  "problem": {
                    "goals": "Detect brain tumors in MRI scans using AI.",
                    "reason_for_ai": "To improve diagnostic accuracy and efficiency.",
                    "current_solution": "N/A"
                  },
                  "data": {
                    "type": "MRI scans",
                    "storage": "Research repository",
                    "key_variables": ["tumor presence"],
                    "size": 1500,
                    "labeled": true,
                    "label_description": "Indicates presence or absence of a tumor.",
                    "known_issues": "None"
                  },
                  "task": {
                    "expected_output": "Binary classification (tumor present or absent).",
                    "evaluation_metrics": ["accuracy", "precision", "recall", "F1 score"],
                    "error_tolerance": "N/A"
                  },
                  "constraints": {
                    "privacy": "Data must be anonymized.",
                    "legal_regulations": "Must comply with government and healthcare regulations.",
                    "ethical_concerns": "N/A"
                  },
                  "intended_users": ["radiologists", "researchers"],
                  "deployment_preferences": "No preference."
                }
                """;

        String userM = """
                Semi‑Structured Problem Definition:
                {context}
                
                Begin by asking the first single question.
                """;

        userM = counter == 0 ? userM : message;
        counter++;
        log.info("Current User Message ={}", userM);

        final String userMessage = userM;



//        var content = chatClient.prompt()
//                .options(chatOptions())
//                .system(systemMessage.getSystemMessage())
//                .user(systemMessage.getUserMessage())
//                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
//                .call()
//                .content();

        var content = chatClient.prompt()
                .options(chatOptions())
                .system(systemM)
                .user(u -> u.text(userMessage).param("context", problemDefn))
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();

        Optional<String> response = Optional.ofNullable(content);

        if (response.isPresent() && response.get().contains("Compute and Tooling Specification")) {
            log.info("Saving compute and tooling specification to file");
            genHandler.saveResponseToFile("compute_" + conversationId.substring(0, 5), response.get());

            // Update the system message
            systemMessage.updateSystemMessageForCompute(response.get());

        }
        return response.orElse("");
    }

    public String pipelineSpecification(String conversationId, String message) {
        log.info("Requesting pipeline information in Chat ID ={}", conversationId);

        var content = chatClient.prompt()
                .options(chatOptions())
                .system(systemMessage.getSystemMessage())
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();

        Optional<String> response = Optional.ofNullable(content);
        // Save response to file and then create a configuration
        if (response.isPresent()) {
            log.info("Saving generated code to file");
            genHandler.generatePythonCode("pipeline" + conversationId.substring(0, 5), response.get());

        }
        return response.orElse("");
    }

    private static ChatOptions chatOptions() {
        return ChatOptions.builder().temperature(0.4).build();

    }
}
