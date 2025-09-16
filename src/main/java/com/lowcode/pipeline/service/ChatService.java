package com.lowcode.pipeline.service;

import com.lowcode.pipeline.util.GenHandler;
import com.lowcode.pipeline.util.SystemMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ChatService {

    private final ChatClient chatClient;

    private SystemMessage systemMessage;

    private GenHandler genHandler;

    private ChatOptions chatOptions;

    private int counter = 0;

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    public ChatService(ChatClient chatClient, SystemMessage systemMessage, GenHandler genHandler, ChatOptions chatOptions) {
        this.chatClient = chatClient;
        this.systemMessage = systemMessage;
        this.genHandler = genHandler;
        this.chatOptions = chatOptions;
    }

    public String problemDefinition(String conversationId, String message) {
        log.info("Chat in conversation={}, message={}", conversationId, message);

        var content = chatClient.prompt()
                .options(chatOptions)
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

//        String systemM = """
//                You are an AI assistant tasked with gathering COMPUTE ENVIRONMENT information for an AI/ML pipeline. Ask
//                concise, domain-friendly questions **one at a time**, focuing on the basic and layman's information
//                from the user, and propose different options and their implications.
//
//                Note that the user does not have any background in Computer Science or AI specialist, and you are encouraged to
//                provide contextual information that guides the user's input.
//
//                To achieve this, ask concise, domain-friendly questions **one at a time**, covering the following key aspects in order:
//                If any information is missing, unclear, or incomplete, **ask polite follow-up questions before moving to the next question**.
//                If the user enters "Not Applicable", mark the corresponding field as unknown, and move on to the next question.
//
//                Start by asking what type of compute environment the user has (e.g., Are you working on a local workstation, on-prem cluster, or in the cloud?):
//                - local workstation,
//                - on-prem/HPC cluster, or
//                - cloud instance/service.
//
//                Based on their answer, adapt your follow-up questions to gather:
//                - GPU: model and VRAM in GB
//                - CPU: model and number of cores (or vCPUs)
//                - RAM: total system memory in GB
//                - Storage: mount/path or service (local disk, network, or cloud), type (nvme, ssd, hdd, network, cloud), free space if known, and notes \s
//                - Paths:
//                  - project_root (where code/configs live)
//                  - data_train, data_val, data_test (where training/validation/test data are located)
//                  - labels_location (if labels are separate)
//                  - outputs_root (base folder for outputs)
//                  - checkpoints_dir (where to save model checkpoints)
//                  - logs_dir (where to save training logs)
////
////                After the logs directory question, **STOP** asking questions and generate valid JSON containing exactly
////                the target fields, and STOP. No extra text before or after the JSON.
//
//                After the logs directory question, **STOP** asking questions and provide:.
//                1. A **Plain Text Summary** of the compute, infrastructure, and paths.
//                2. A **Structured JSON object** containing exactly the target fields.
//
//                Context (semi‑structured problem follows). Use its details to specialize your questions instead of asking generic ones.
//
//                """;

//        String systemM = """
//                You are an AI assistant tasked with gathering COMPUTE ENVIRONMENT information for an AI/ML pipeline. Ask
//                concise, domain-friendly questions **one after the other**, focuing on the basic and layman's information
//                from the user, and propose different options and their implications. The output of this questionnaire will
//                be fed to another tool that will generate the pipeline code. Hence, don't ask questions about programming
//                 language, preprocessing, AI/Ml framework, etc.
//
//                Note that the user does not have any background in Computer Science or AI specialist, and you are encouraged to
//                provide contextual information that guides the user's input.
//
//                After you have sufficient information for the AI pipeline, **STOP** asking questions and generate
//                pipeline options, as well as their implications.
//
//                Context (semi‑structured problem follows). Use its details to specialize your questions instead of asking generic ones.
//
//                """;


        String systemM = """
                Prompt Message
                
                You are an AI assistant helping a researcher define their computing environment for training an AI model.
                Ask domain-friendly questions **one after the other**, to the user, and propose different pipeline options and
                their implications. The output of this questionnaire will be fed to another tool that will generate the
                pipeline code. Hence, don't ask questions about programming language, preprocessing, AI/Ml framework, etc.
                
                The researcher has no technical background in AI or Computer Science, so you must ask questions in simple,
                everyday language with clear multiple-choice options.
                
                Your task:
                Ask the researcher one question at a time.
                Wait for their response before moving to the next question.
                Use short, plain wording.
                Provide contextual information that guides the user's input.
                Cover the following topics in order:
                
                1. Where the training will happen.
                
                Based on their answer, adapt your follow-up questions to gather:
                
                2. The compute option(s).
                3. Whether the compute option has a GPU.
                4. Available storage space for the dataset.
                5. How much waiting time they are comfortable with for training.
                
                After asking questions about the budget, **STOP** asking questions and generate a **Compute Environment Specification**,
                and its implications in terms of cost, availability, accessibility, privileges, model performance, and training time.

                Context (semi‑structured problem follows). Use its details to specialize your questions instead of asking generic ones.

                """;

        String systemMT = """
                You are an AI assistant helping a researcher (with no background in AI or computer science) describe
                their computing environment for training an AI model on MRI images.
                
                Your role and behavior:
                Ask one question at a time and adapt wording to the researcher’s level.
                Use simple everyday examples.
                Always provide easy answer options (multiple-choice style), but also allow free-form answers.
                If the researcher doesn’t know, reassure them (“That’s fine, we can work around it”) and move on.
                Keep technical terms to a minimum, and translate them into plain language if they come up.
                At the end, generate a **Compute Environment Specification**, in plain English and briefly explain how
                their situation might affect: 
                - Training speed
                - Accuracy of the model
                - Cost
                
                Information you need to collect:
                1. Where the training will happen.
                2. The type of computing..
                3. Whether special graphics cards (GPUs) are available.
                4. How much storage space is available for the data.
                5. How much waiting time is acceptable for training.
                6. Whether they have a budget for computing costs.
                """;

//        After the logs directory question, **STOP** asking questions and generate valid JSON containing exactly
////      the target fields, and STOP. No extra text before or after the JSON.

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
//                .options(chatOptions)
//                .system(systemMessage.getSystemMessage())
//                .user(systemMessage.getUserMessage())
//                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
//                .call()
//                .content();

        var content = chatClient.prompt()
                .options(chatOptions)
                .system(systemM)
                .user(u -> u.text(userMessage).param("context", problemDefn))
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();

        Optional<String> response = Optional.ofNullable(content);

        if (response.isPresent() && response.get().contains("Compute Environment Specification")) {
            log.info("Saving compute and tooling specification to file");
            genHandler.saveResponseToFile("compute_" + conversationId.substring(0, 5), response.get());

            // Update the system message
            systemMessage.updateSystemMessageForCompute(response.get());

        }
        return response.orElse("");
    }

    public String pipelineSpecification(String conversationId, String message) {
        log.info("Requesting pipeline information in Chat ID ={}, message: {}", conversationId, message);

        String innerSystemMessage = """
                Role & Goal
                You are an AI assistant that produces a conceptual, step-by-step AI/ML pipeline specification. 
                Your specification will be consumed by a separate tool to generate code. You must make the technical 
                decisions yourself, aligning them with the user’s Problem Specification and Compute/Infrastructure 
                Specification provided in semi-structured form.
                
                Interaction Rules
                1. Use non-technical language when you must ask for missing critical inputs.
                2. Minimize questions: only ask when a decision is impossible or risky without the answer. Otherwise, 
                proceed with clearly stated assumptions and sensible defaults.
                3. Do not output executable code. Produce a specification that is complete enough for code generation.
                
                Outputs (two artifacts)
                1. Human-oriented Outline: a clear, step-by-step narrative.
                2. Machine-readable PipelineSpec: a single JSON object (valid JSON) matching the schema below.
                
                Required Sections & Decision Policy
                Make all choices to balance problem needs and compute constraints. Prefer standard, well-supported 
                components. If multiple options are viable, pick one and log the rationale.
                
                1. Problem Summary & Assumptions
                2. Data Specification
                3. Paths
                4. Preprocessing & Feature Engineering
                5. Model Selection
                6. Training Configuration
                7. Evaluation Plan
                
                Context (semi‑structured problem and compute specifications follows). Use these details to specialize 
                your pipeline generation..
                
                
                """;

        String computeSpec = """
                ### Plain Text Summary
                - **Compute Environment**: On-premises
                - **CPU**: 16 cores
                - **RAM**: 128 GB
                - **Storage**: 1 TB SSD
                - **Outputs Root**: output/root
                - **Data Paths**: Not specified (data_train, data_val, data_test, labels_location)
                - **Checkpoints Directory**: Not specified
                - **Logs Directory**: Not specified
                
                ### Structured JSON
                ```json
                {
                  "compute": {
                    "cpu": {
                      "cores": 16
                    },
                    "ram": 128,
                    "storage": {
                      "type": "SSD",
                      "size": "1TB"
                    }
                  },
                  "paths": {
                    "project_root": "Not Applicable",
                    "data_train": "Not Applicable",
                    "data_val": "Not Applicable",
                    "data_test": "Not Applicable",
                    "labels_location": "Not Applicable",
                    "outputs_root": "output/root",
                    "checkpoints_dir": "Not Applicable",
                    "logs_dir": "Not Applicable"
                  }
                }
                ```
                """;

//        String computeSpec = """
//                ### Plain Text Summary
//                You are working on a local workstation for your AI project focused on detecting brain tumors in MRI scans. Your workstation is equipped with an NVIDIA RTX 4090 GPU with 24 GB of VRAM and an AMD Ryzen 9 7950X CPU with 16 cores. You have a total of 128 GB of RAM. Your storage is an HDD mounted at `/data`, with 1200 GB of free space available. The paths for your project and data are as follows:
//                - Project Root: `/home/user/brain-tumor-ai`
//                - Training Data: `/data/brain_mri/train`
//                - Validation Data: `/data/brain_mri/val`
//                - Test Data: `/data/brain_mri/test`
//                - Labels Location: `/data/brain_mri/labels`
//                - Outputs Root: `/home/user/brain_mri_outputs`
//                - Checkpoints Directory: `/home/user/brain_mri_outputs/checkpoints`
//                - Logs Directory: `/home/user/brain_mri_outputs/logs`
//
//                ### Structured JSON Object
//                ```json
//                {
//                  "compute": {
//                    "environment": "local workstation",
//                    "gpu": {
//                      "model": "NVIDIA RTX 4090",
//                      "vram": 24
//                    },
//                    "cpu": {
//                      "model": "AMD Ryzen 9 7950X",
//                      "cores": 16
//                    },
//                    "ram": 128
//                  },
//                  "infrastructure": {
//                    "storage": {
//                      "type": "HDD",
//                      "mount": "/data",
//                      "free_space": 1200
//                    }
//                  },
//                  "paths": {
//                    "project_root": "/home/user/brain-tumor-ai",
//                    "data_train": "/data/brain_mri/train",
//                    "data_val": "/data/brain_mri/val",
//                    "data_test": "/data/brain_mri/test",
//                    "labels_location": "/data/brain_mri/labels",
//                    "outputs_root": "/home/user/brain_mri_outputs",
//                    "checkpoints_dir": "/home/user/brain_mri_outputs/checkpoints",
//                    "logs_dir": "/home/user/brain_mri_outputs/logs"
//                  }
//                }
//                ```
//                """;

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
                Semi‑Structured Problem and Compute Definition:
                {context}
                
                Begin by generating the pipeline or asking the first first if any information is missing, and you cannot 
                make a decision without it..
                """;

        var content = chatClient.prompt()
                .options(chatOptions)
                .system(innerSystemMessage)
                .user(u -> u.text(userM).param("context", problemDefn + computeSpec))
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();

        Optional<String> response = Optional.ofNullable(content);
        // Save response to file and then create a configuration
        if (response.isPresent()) {
            log.info("Saving generated pipeline to file");
            genHandler.saveResponseToFile("pipeline" + conversationId.substring(0, 5), response.get());

        }
        return response.orElse("");
    }

}
