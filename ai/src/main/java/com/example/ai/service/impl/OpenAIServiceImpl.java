package com.example.ai.service.impl;

import com.example.ai.service.OpenAIService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OpenAIServiceImpl implements OpenAIService {

    private final ChatModel chatModel;

    @Override
    public String getAnswer(String question) {
        var promptTemplate = new PromptTemplate(question);
        var chatOptions = OpenAiChatOptions.builder().withTemperature(0.7).withModel("gpt-4o-mini").build();
        var prompt = promptTemplate.create(chatOptions);
        return chatModel.call(prompt).getResult().getOutput().getContent();
    }
}
