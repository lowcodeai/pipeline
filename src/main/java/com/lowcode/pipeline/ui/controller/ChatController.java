package com.lowcode.pipeline.ui.controller;

import com.lowcode.pipeline.service.ChatService;
import com.lowcode.pipeline.ui.request.ChatRequest;
import com.lowcode.pipeline.ui.request.ChatType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;


@RestController
@SessionAttributes("chatMemory")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chat")
    public ResponseEntity<String> chat(@RequestBody ChatRequest req) throws IOException {

        String conversationId = req.getConversationID();
        String message = req.getMessage();
        ChatType chatType = req.getType();

        var response = switch (chatType) {
            case PROBLEM_DEFINITION -> ResponseEntity.ok(chatService.problemDefinition(conversationId, message));
            case COMPUTE_SPECIFICATION -> ResponseEntity.ok(chatService.computeSpecification(conversationId, message));
            case PIPELINE_SPECIFICATION -> ResponseEntity.ok(chatService.pipelineGeneration(conversationId, message));
            case CODE_GENERATION -> ResponseEntity.ok(chatService.codeGeneration(conversationId, message));
            case REFINE_CODE_1 -> ResponseEntity.ok(chatService.refineCode1(conversationId, message));
            case REFINE_CODE_2 -> ResponseEntity.ok(chatService.refineCode2(conversationId, message));
            default -> ResponseEntity.badRequest().body("Invalid chat type");
        };
//
//        if (chatType == ChatType.PROBLEM_DEFINITION) {
//            return ResponseEntity.ok(chatService.problemDefinition(conversationId, message));
//        } else if (chatType == ChatType.PIPELINE_SPECIFICATION) {
//            return ResponseEntity.ok(chatService.pipelineSpecification(conversationId, message));
//        } else {
//            return ResponseEntity.badRequest().body("Invalid chat type");
//        }

        return response;


    }
}
