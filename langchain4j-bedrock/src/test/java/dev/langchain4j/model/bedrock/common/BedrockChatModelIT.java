package dev.langchain4j.model.bedrock.common;

import dev.langchain4j.model.bedrock.BedrockAnthropicMessageChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.common.ChatLanguageModelIT;

import java.util.List;

import static dev.langchain4j.model.bedrock.BedrockAnthropicMessageChatModel.Types.AnthropicClaude3SonnetV1;

class BedrockChatModelIT extends ChatLanguageModelIT {

    @Override
    protected List<ChatLanguageModel> models() {
        return List.of(
                BedrockAnthropicMessageChatModel.builder()
                        .model(AnthropicClaude3SonnetV1.getValue())
                        .build()
                // TODO add more models
        );
    }
}
