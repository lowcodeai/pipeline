package com.lowcode.pipeline.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SystemMessage {

    private static final Logger log = LoggerFactory.getLogger(SystemMessage.class);

//    private String systemMessage = """
//                You are an AI assistant whose goal is to gather the necessary information to produce a
//                structured AI pipeline specification based on the user's input.
//
//                To achieve this, ask concise, domain-friendly questions **one at a time**, covering the following key aspects in order:
//                If any information is missing, unclear, or incomplete, **ask polite follow-up questions before moving to the next question**.
//
//                Step 1: Domain and Problem Understanding
//                1.1. The user's domain (e.g., healthcare, finance, education).
//                1.2. The main problem or research goals.
//                1.3. Why does the user wants to solve the problem using AI.
//                1.4. The current solution or workaround (if any).
//
//                Step 2: Data Description
//                2.1. What kind of data does the user has access to.
//                2.2. The format of the data (e.g., CSV, DICOM, JSON).
//                2.3. The distribution of the data (e.g., real-time, batch).
//                2.4. How is the data currently stored and organized.
//                2.5. What are the key variables or features in the data.
//                2.6. The size of the dataset.
//                2.7. Is the data labeled? If so, how?
//                2.8. Are there any known issues with the data?
//
//                Step 3: Task Definition
//                3.1. What kind of output does the user expect from the system.
//                3.2. How will the user evaluate whether the system is successful.
//                3.3. What is the tolerance for errors?
//
//                Step 4: Constraints and Deployment
//                4.1. Are there constraints that must be respected, including:
//                    a. Privacy considerations.
//                    b. Legal or regulatory obligations
//                    c. Ethical concerns
//                4.2. The intended users of the system.
//
//                Use simple, domain-appropriate language without introducing technical jargon related to AI, algorithms, programming, or system infrastructure.
//
//                At the end, provide:
//
//                1. A **Plain Text Summary** of the problem, goals, and key details in non-technical language.
//                2. A **Structured JSON object** that follows the AI Pipeline Problem Specification schema.
//                """;

    // Testing
    private String userMessage = """
            Semi-Structured Problem Definition:
            
            ### Plain Text Summary
                You are working in the healthcare domain, specifically focusing on the detection of brain tumors using MRI images. Your goal is to develop a binary classification system that can accurately identify the presence or absence of brain tumors. You believe that AI can enhance the accuracy and efficiency of this process. You currently have a labeled dataset of 1,500 MRI images in DICOM format, which includes information on tumor size and abnormal growth. The data is stored in a research repository and has no known issues. You plan to evaluate the system's performance using accuracy, precision, recall, and F1 score, with a maximum error tolerance of 10%. It is crucial to preserve data privacy and comply with government regulations and healthcare guidelines. The intended users of the system are radiologists and researchers.
            
                        ### Structured JSON Object
            ```json
                {
                    "domain": "healthcare",
                        "problem": {
                    "goals": "Develop a binary classification system to detect brain tumors in MRI images.",
                            "reason_for_AI": "To enhance accuracy and efficiency in identifying brain tumors."
                },
                    "current_solution": "N/A",
                        "data_description": {
                    "data_type": "MRI images",
                            "data_format": "DICOM",
                            "data_distribution": "batch",
                            "data_storage": "research repository",
                            "key_variables": ["tumor size", "abnormal growth"],
                    "dataset_size": 1500,
                            "labeled": true,
                            "labeling_method": "indicating presence or absence of brain tumor",
                            "known_issues": "none"
                },
                    "task_definition": {
                    "expected_output": "binary classification indicating presence or absence of brain tumor",
                            "evaluation_metrics": ["accuracy", "precision", "recall", "F1 score"],
                    "error_tolerance": "maximum of 10%"
                },
                    "constraints": {
                    "privacy": "Data privacy must be preserved.",
                            "legal_regulations": "Adhere to government regulations and healthcare guidelines.",
                            "ethical_concerns": "N/A"
                },
                    "intended_users": ["radiologists", "researchers"]
                }
            """;
    private String systemMessage = """
            You are an AI assistant that asks exactly ONE clear, concrete question at a time to gather the COMPUTE and
            TOOLING requirements for an AI pipeline. Your audience is a domain expert, who is NOT a Computer Science or
            AI specialist.
            
            Goals:
            - Elicit the minimum necessary details to design compute and tooling: hardware, OS/driver/CUDA stack, Python/ML libraries, data formats & preprocessing, caching vs on-the-fly I/O, training precision, batch sizing, loaders, experiment logging, checkpoints, and reproducibility vs throughput.
            - Adapt choices to user constraints (GPU/CPU/RAM/storage, local vs cluster/cloud).
            - Be concise, use plain language, and avoid jargon unless explained.
            - After each user reply, ask the NEXT best single question. Do not ask multiple questions at once.
            - Do NOT include any metadata, role tags, or code fences in your questions unless the user explicitly asks for code.
            - When you have enough info, produce a final “Compute & Tooling Spec” in valid JSON, then stop.
            
            Context (semi-structured problem follows). Use it to tailor your questions.
            """;

    public void updateSystemMessageForCompute(String message) {
        log.info("Update system message for compute and tooling");

        systemMessage = """
            A researcher who is not an expert in computer science and AI has defined his research problem in a
            semi-structured format, shown below. You are an AI assistant tasked with asking the right questions about
            compute and tooling to build the AI pipeline.
            """ + message + """
            """;

    }

    public void updateSystemMessageForFullPipeline(String message) {
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

    public String getSystemMessage() {
        return systemMessage;
    }

    public String getUserMessage() {
        return userMessage;
    }
}
