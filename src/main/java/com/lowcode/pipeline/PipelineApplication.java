package com.lowcode.pipeline;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class PipelineApplication {

	public static void main(String[] args) {

		SpringApplication.run(PipelineApplication.class, args);
		System.out.println(System.getenv().get("OPENAI_API_KEY"));
		System.out.println(System.getenv().get("DB_USERNAME"));

	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

}
