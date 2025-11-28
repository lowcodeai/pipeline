package com.lowcode.pipeline.service;

import com.lowcode.pipeline.util.OutputHandler;
import com.lowcode.pipeline.util.SystemMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
public class ChatService {

    private final ChatClient chatClient;
    private SystemMessage systemMessage;
    private OutputHandler outputHandler;
    private ChatOptions chatOptions;
    private int counter = 0;
    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    public ChatService(ChatClient chatClient, SystemMessage systemMessage, OutputHandler outputHandler, ChatOptions chatOptions) {
        this.chatClient = chatClient;
        this.systemMessage = systemMessage;
        this.outputHandler = outputHandler;
        this.chatOptions = chatOptions;
    }

    public String problemDefinition(String conversationId, String message) {
        log.info("Chat in conversation={}, message={}", conversationId, message);

        var content = chatClient.prompt()
                .options(chatOptions)
                .system(systemMessage.getProblemSystemMessage())
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();

        Optional<String> response = Optional.ofNullable(content);

        // Save response to file and then create a configuration
        if (response.isPresent() && response.get().contains("Plain Text Summary")) {
            log.info("Saving a problem definition to file");
            outputHandler.saveResponseToFile("problem_" + conversationId, response.get());
        }

        return response.orElse("");
    }


    public String computeSpecification(String conversationId, String message) {
        log.info("Requesting information for Compute and Tooling Specification ={}", conversationId);

        String problemDefn = "";
        try {
            problemDefn = outputHandler.readFile("specifications/problem_definition.txt");
        } catch (IOException e) {
            log.error("Failed to read problem definition file", e);
            return "Failed to read problem definition file";
        }

        log.info("Problem Definition ={}", problemDefn);

        String userM = """
                Semi‑Structured Problem Definition:
                {context}
                
                Begin by asking the first single question.
                """.replace("{context}", problemDefn);

        userM = counter == 0 ? userM : message;
        counter++;
        log.info("Current User Message ={}", userM);

        final String userMessage = userM;


        var content = chatClient.prompt()
                .options(chatOptions)
                .system(systemMessage.getComputeSystemMessage())
                .user(userMessage)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();

        Optional<String> response = Optional.ofNullable(content);

        if (response.isPresent() && response.get().contains("Compute Environment Specification")) {
            log.info("Saving compute and tooling specification to file");
            outputHandler.saveResponseToFile("compute_" + conversationId, response.get());
        }
        return response.orElse("");
    }

    public String pipelineGeneration(String conversationId, String message) {
        log.info("Requesting pipeline information in Chat ID ={}, message: {}", conversationId, message);

        String problemDefn = "";
        String compute = "";
        try {
            problemDefn = outputHandler.readFile("specifications/problem_definition.txt");
        } catch (IOException e) {
            log.error("Failed to read problem definition file", e);
            return "Failed to read problem definition file";
        }

        try {
            compute = outputHandler.readFile("specifications/compute_specification.txt");
        } catch (IOException e) {
            log.error("Failed to read compute specification file", e);
            return "Failed to read compute specification file";
        }


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


        var content = chatClient.prompt()
                .options(chatOptions)
                .system(systemMessage.getPipelineSystemMessage())
                .user(userM)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();

        Optional<String> response = Optional.ofNullable(content);
        // Save response to file and then create a configuration
        if (response.isPresent() && response.get().contains("Alternative AI Pipelines")) {
            log.info("Saving generated pipelines to file");
            outputHandler.saveResponseToFile("pipeline_" + conversationId, response.get());

        }
        return response.orElse("");
    }

    public String codeGeneration(String conversationId, String message) throws IOException {
        log.info("Generating code in Chat ID ={}, message: {}", conversationId, message);

        String problemDefn = outputHandler.readFile("specifications/problem_definition.txt");

        String compute = outputHandler.readFile("specifications/compute_specification.txt");

        String pipeline = outputHandler.readFile("specifications/pipeline_generation.txt");

        String userM1 = """
                Here is a jason description of a research problem that can be solved by an AI.
                {problem}
                
                And here is the available computing environment:
                {compute}
                
                And here is the proposed pipeline:
                {pipeline}
                
                """;

        String userM = message;

        if (counter == 0) {
            userM = userM1
                    .replace("{problem}", problemDefn)
                    .replace("{compute}", compute)
                    .replace("{pipeline}", pipeline);
        }
        counter++;




//        You will be given a semi-structured problem description, compute-environment specification, and a
//        pipeline specification.

        var content = chatClient.prompt()
                .options(chatOptions)
                .system(systemMessage.getCodeSystemMessage())
                .user(userM)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();



        Optional<String> response = Optional.ofNullable(content);
        // Save response to file and then create a configuration
        if (response.isPresent() && response.get().contains("AI Implementation Code")) {
            log.info("Saving generated code to file");
            outputHandler.saveResponseToFile("code" + conversationId, response.get());

        }
        return response.orElse("");
    }

}
