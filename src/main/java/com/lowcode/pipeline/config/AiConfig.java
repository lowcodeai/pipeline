package com.lowcode.pipeline.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    MessageWindowChatMemory chatMemory() {

        return MessageWindowChatMemory.builder().maxMessages(200).build();
    }

    @Bean
    ChatClient chatClient(OpenAiChatModel model, MessageWindowChatMemory memory) {
        return ChatClient.builder(model)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(memory).build())
                .build();
    }

    @Bean
    ChatOptions chatOptions(@Value("${ai.temperature:0.3}") double temp) {
        return ChatOptions.builder()
                .temperature(0.4)
                .build();
    }
}
