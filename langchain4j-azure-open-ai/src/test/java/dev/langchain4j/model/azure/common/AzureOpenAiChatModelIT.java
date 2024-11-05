package dev.langchain4j.model.azure.common;

import dev.langchain4j.model.azure.AzureOpenAiChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.common.AbstractChatLanguageModelIT;

import java.util.List;

class AzureOpenAiChatModelIT extends AbstractChatLanguageModelIT {

    @Override
    protected List<ChatLanguageModel> models() {
        return List.of(
                AzureOpenAiChatModel.builder()
                        .endpoint(System.getenv("AZURE_OPENAI_ENDPOINT"))
                        .apiKey(System.getenv("AZURE_OPENAI_KEY"))
                        .deploymentName("gpt-4o-mini")
                        .build()
        );
    }

    protected boolean assertFinishReason() {
        return false; // TODO fix
    }
}