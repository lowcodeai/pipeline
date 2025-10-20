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
            genHandler.saveResponseToFile("paper_problem" + conversationId.substring(0, 5), response.get());

            // Update the system message
            systemMessage.updateSystemMessageForCompute(response.get());

        }

        return response.orElse("");
    }


    public String computeSpecification(String conversationId, String message) {
        log.info("Requesting information for Compute and Tooling Specification ={}", conversationId);



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
                You are an AI Architect Expert helping a researcher define their computing environment for training an AI model.
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
                
                1. The experience level of the researcher in computing infrastucture.
                2. Preferred location for the training.
                
                Based on their answers, adapt your follow-up questions to gather:
                
                3. The compute options (e.g., local workstation, on-prem cluster, or in the cloud) the researcher has.
                4. Whether the compute option has a GPU.
                5. Available storage space for the dataset.
                6. Training frequency (one-off, weekly, monthly, continuous)
                7. Budget for computing costs.
                
                After asking questions about the budget, **STOP** asking questions and generate a **Compute Environment Specification**,
                and its implications in terms of cost, availability, accessibility, privileges, model performance, and training time.
                
                Also, propose alternative options for the compute environment, and their implications.

                Context (semi‑structured problem follows). Use its details to specialize your questions instead of asking generic ones.

                """;

        String systemTP = """
                You are an AI Architect Expert that guides users in defining their computing environment for AI pipeline
                generation. Act as an expert facilitator who adapts language to the user’s expertise level.
                
                Your task:
                Keep questions simple, one at a time.
                Avoid technical jargon for beginners.
                
                Step 1 (Entry Point):
                Ask if the user already has a computing environment.
                    If YES, go to Step 2a (Known Environment)
                    If NO, go to Step 2b (New Environment)
                
                Step 2a (Known Environment):
                - The compute options (e.g., local workstation, on-prem cluster, or in the cloud) the researcher has.
                - Whether the compute option has a GPU.
                - Available storage space for the dataset.
                - Constraints:
                    - Queue/wait times
                    - Quotas or usage limits
                - Usage Model:
                    - Training frequency (one-off, weekly, monthly, continuous)
                    - Inference needs (batch, real-time)
                
                Step 2a (New Environment):
                - Assess experience level
                - Budget preference
                    - One-time (buy hardware) vs. recurring (cloud subscription).
                    - Max budget per month / per project.
                - Workload pattern
                    - Small dataset vs large-scale (millions/streaming).
                    - Batch only vs real-time inference.
                - Priorities
                    - Rank: lowest cost / fastest results / easiest maintenance.
                
                At the end, generate a **Compute Environment Specification**, and its implications in terms of cost,
                availability, accessibility, privileges, model performance, and training time.
                
                Also, propose alternative options for the compute environment, and their implications.

                Context (semi‑structured problem follows). Use its details to specialize your questions instead of asking
                generic ones.
                
                let's think step by step.

                """;

        String systemMT = """
                Role: You are an assistant helping medical researchers choose computing options for their AI research.
                
                Your Goals:
                
                First identify the user’s experience level and their preferred computing location.
                
                Then check whether they already have a computing environment or not.
                
                If they already have one, ask only basic questions about how they use it and their budget.
                
                If they don’t have one, ask simple questions about budget, ease of use, and preferences.
                
                At the end, provide 2–3 computing options with a short trade-off summary (cost, ease of use, and flexibility).
                
                Interaction Rules
                
                Keep questions simple, non-technical, and one at a time.
                
                Use plain language (avoid jargon).
                
                Adapt based on answers—don’t ask unnecessary follow-ups.
                
                Always end with a short set of tailored recommendations and trade-offs.
                """;

//        After the logs directory question, **STOP** asking questions and generate valid JSON containing exactly
////      the target fields, and STOP. No extra text before or after the JSON.

        String problemDefn = """
                ### Summary of Your Project:
                You are working on designing and implementing an automatic brain tumor detection system in the medical science domain. As a beginner in AI, you aim to utilize AI for fast, accurate, and automated detection of brain tumors using a dataset of 1,500 labeled MRI images in DICOM format. The images are organized in a folder and labeled as either "tumor present" or "tumor not present." You plan to evaluate the system's performance using metrics such as accuracy, precision, recall, F1 score, and root mean square error, with a tolerance for misclassification of 7 to 10%. Privacy considerations are important, as the dataset must be protected. The intended users of the system are medical professionals and researchers, and you do not have a specific timeline for the project.
                
                ### Structured JSON Object:
                ```json
                {
                  "project": {
                    "domain": "Medical Science",
                    "experience_level": "Beginner",
                    "goals": "Design and implement an automatic brain tumor detection system.",
                    "reason_for_using_AI": "AI enables fast, accurate, and automated detection of brain tumors.",
                    "current_solution": "Classical machine learning approaches.",
                    "data": {
                      "type": "MRI images",
                      "format": "DICOM",
                      "distribution": "Historical collection",
                      "storage": "Stored in a folder",
                      "size": 1500,
                      "labeled": true,
                      "labeling_method": "Tumor present or not present",
                      "known_issues": "No issues"
                    },
                    "task": {
                      "expected_output": "Classify images as tumor present or not present.",
                      "evaluation_metrics": [
                        "accuracy",
                        "precision",
                        "recall",
                        "F1 score",
                        "root mean square error"
                      ],
                      "error_tolerance": "7 to 10%"
                    },
                    "constraints": {
                      "privacy": "Protect the privacy of the dataset.",
                      "legal_regulatory": "None specified",
                      "ethical_concerns": "None specified"
                    },
                    "intended_users": [
                      "Medical professionals",
                      "Researchers"
                    ],
                    "timeline": "No specific deadline"
                  }
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



        String problemDefn = """
                ### Summary of Your Project:
                You are working on designing and implementing an automatic brain tumor detection system in the medical science domain. As a beginner in AI, you aim to utilize AI for fast, accurate, and automated detection of brain tumors using a dataset of 1,500 labeled MRI images in DICOM format. The images are organized in a folder and labeled as either "tumor present" or "tumor not present." You plan to evaluate the system's performance using metrics such as accuracy, precision, recall, F1 score, and root mean square error, with a tolerance for misclassification of 7 to 10%. Privacy considerations are important, as the dataset must be protected. The intended users of the system are medical professionals and researchers, and you do not have a specific timeline for the project.
                
                ### Structured JSON Object:
                ```json
                {
                  "project": {
                    "domain": "Medical Science",
                    "experience_level": "Beginner",
                    "goals": "Design and implement an automatic brain tumor detection system.",
                    "reason_for_using_AI": "AI enables fast, accurate, and automated detection of brain tumors.",
                    "current_solution": "Classical machine learning approaches.",
                    "data": {
                      "type": "MRI images",
                      "format": "DICOM",
                      "distribution": "Historical collection",
                      "storage": "Stored in a folder",
                      "size": 1500,
                      "labeled": true,
                      "labeling_method": "Tumor present or not present",
                      "known_issues": "No issues"
                    },
                    "task": {
                      "expected_output": "Classify images as tumor present or not present.",
                      "evaluation_metrics": [
                        "accuracy",
                        "precision",
                        "recall",
                        "F1 score",
                        "root mean square error"
                      ],
                      "error_tolerance": "7 to 10%"
                    },
                    "constraints": {
                      "privacy": "Protect the privacy of the dataset.",
                      "legal_regulatory": "None specified",
                      "ethical_concerns": "None specified"
                    },
                    "intended_users": [
                      "Medical professionals",
                      "Researchers"
                    ],
                    "timeline": "No specific deadline"
                  }
                }
                
                """;

        String compute = """
                ### Compute Environment Specification:
                1. **Experience Level**: Beginner
                2. **Preferred Location for Training**: Dedicated server
                3. **Compute Option**: Dedicated server with GPU
                4. **Available Storage Space**: About 1 TB
                5. **Training Frequency**: Just once
                6. **Budget for Computing Costs**: About $150
                """;

        String userM1 = """
                Here is a jason description of a research problem that can be solved by an AI.
                {problem}
                
                And here is the available computing environment:
                {compute}
                
                Do you need more information to propose alternative pipelines?
                if yes, ask for those information, one question at a time.
                """;


        String userM = message;

        if (counter == 0) {
            userM = userM1.replace("{problem}", problemDefn).replace("{compute}", compute);
        }
        counter++;

        String systemM = """
                You are an AI specialist whose task is to propose alternative AI pipelines that align with a problem
                definition, as well as a computing environment. Append each proposed pipeline with its pros and cons,
                and note that the proposed specification will be consumed by a separate tool to generate implementation
                code.
                
                Let's think step by step.
                """;

        var content = chatClient.prompt()
                .options(chatOptions)
                .system(systemM)
                .user(userM)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();

        Optional<String> response = Optional.ofNullable(content);
        // Save response to file and then create a configuration
        if (response.isPresent()) {
            log.info("Saving generated pipeline to file");
            genHandler.saveResponseToFile("paper_pipeline" + conversationId.substring(0, 5), response.get());

        }
        return response.orElse("");
    }

    public String codeGeneration(String conversationId, String message) {
        log.info("Generating code in Chat ID ={}, message: {}", conversationId, message);

        String problemDefn = """
                ### Summary of Your Project:
                You are working on designing and implementing an automatic brain tumor detection system in the medical science domain. As a beginner in AI, you aim to utilize AI for fast, accurate, and automated detection of brain tumors using a dataset of 1,500 labeled MRI images in DICOM format. The images are organized in a folder and labeled as either "tumor present" or "tumor not present." You plan to evaluate the system's performance using metrics such as accuracy, precision, recall, F1 score, and root mean square error, with a tolerance for misclassification of 7 to 10%. Privacy considerations are important, as the dataset must be protected. The intended users of the system are medical professionals and researchers, and you do not have a specific timeline for the project.
                
                ### Structured JSON Object:
                ```json
                {
                  "project": {
                    "domain": "Medical Science",
                    "experience_level": "Beginner",
                    "goals": "Design and implement an automatic brain tumor detection system.",
                    "reason_for_using_AI": "AI enables fast, accurate, and automated detection of brain tumors.",
                    "current_solution": "Classical machine learning approaches.",
                    "data": {
                      "type": "MRI images",
                      "format": "DICOM",
                      "distribution": "Historical collection",
                      "storage": "Stored in a folder",
                      "size": 1500,
                      "labeled": true,
                      "labeling_method": "Tumor present or not present",
                      "known_issues": "No issues"
                    },
                    "task": {
                      "expected_output": "Classify images as tumor present or not present.",
                      "evaluation_metrics": [
                        "accuracy",
                        "precision",
                        "recall",
                        "F1 score",
                        "root mean square error"
                      ],
                      "error_tolerance": "7 to 10%"
                    },
                    "constraints": {
                      "privacy": "Protect the privacy of the dataset.",
                      "legal_regulatory": "None specified",
                      "ethical_concerns": "None specified"
                    },
                    "intended_users": [
                      "Medical professionals",
                      "Researchers"
                    ],
                    "timeline": "No specific deadline"
                  }
                }
                
                """;

        String compute = """
                ### Compute Environment Specification:
                1. **Experience Level**: Beginner
                2. **Preferred Location for Training**: Dedicated server
                3. **Compute Option**: Dedicated server with GPU
                4. **Available Storage Space**: About 1 TB
                5. **Training Frequency**: Just once
                6. **Budget for Computing Costs**: About $150
                """;

        String pipepine = """
                Transfer Learning with Pre-trained CNN
                
                **Description:**
                This pipeline leverages a pre-trained CNN model (e.g., VGG16, ResNet50) to improve performance by fine-tuning it on your specific dataset.
                
                **Steps:**
                1. **Data Preprocessing:**
                   - Load DICOM images and convert them to a suitable format.
                   - Normalize pixel values.
                   - Split the dataset into training (80%) and validation (20%) sets.
                   - Use data augmentation techniques (e.g., rotation, flipping) to increase dataset diversity.
                
                2. **Model Architecture:**
                   - Load a pre-trained model (e.g., VGG16) without the top layers.
                   - Add a new fully connected layer with 128 neurons and ReLU activation.
                   - Add a dropout layer for regularization.
                   - Add an output layer with 1 neuron and sigmoid activation.
                
                3. **Compilation:**
                   - Use binary cross-entropy as the loss function.
                   - Optimize using Adam optimizer.
                
                4. **Training:**
                   - Train the model for a specified number of epochs (e.g., 20) with early stopping based on validation loss.
                
                5. **Evaluation:**
                   - Evaluate the model using accuracy, precision, recall, F1 score, and RMSE.
                
                """;

        String userM1 = """
                Here is a jason description of a research problem that can be solved by an AI.
                {problem}
                
                And here is the available computing environment:
                {compute}
                
                And here is the proposed pipeline:
                {pipeline}
                
                """;

//        Lets generate the implementation code for the pipeline. Do you need more information?


        String userM = message;

        if (counter == 0) {
            userM = userM1
                    .replace("{problem}", problemDefn)
                    .replace("{compute}", compute)
                    .replace("{pipeline}", pipepine);
        }
        counter++;

        String systemM = """
                You are an expert in code generation tasked with generating pipeline code based on a problem definition,
                compute environment specification, and pipeline specification.
                
                Your tasks:
                - First, evaluate whether the information provided is sufficient to generate the implementation code.
                  If not, ask targeted clarification questions, one at a time, tailored to the user’s expertise level.
                - Once sufficient information is available, generate the implementation code.
                
                Let's think step by step.
                """;


//        You will be given a semi-structured problem description, compute-environment specification, and a
//        pipeline specification.

        var content = chatClient.prompt()
                .options(chatOptions)
                .system(systemM)
                .user(userM)
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
