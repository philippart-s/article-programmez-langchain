///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS dev.langchain4j:langchain4j:0.35.0
//DEPS dev.langchain4j:langchain4j-mistral-ai:0.35.0

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.mistralai.MistralAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;

public class chatbot {
    // Utilisation de l'approche AI Service
    interface Assistant {
        @SystemMessage("You are Nestor, a virtual assistant. Answer to the question.")
        TokenStream chat(String message);
    }

    public static void main(String[] args) {
        // Choix et paramétrage du modèle
        MistralAiStreamingChatModel streamingChatModel = MistralAiStreamingChatModel.builder()
                .apiKey(System.getenv("OVH_AI_ENDPOINTS_ACCESS_TOKEN"))
                .modelName("Mistral-7B-Instruct-v0.2")
                .baseUrl(
                        "https://mistral-7b-instruct-v02.endpoints.kepler.ai.cloud.ovh.net/api/openai_compat/v1")
                .maxTokens(512)
                .build();

        // Utilisation du stockage de l'historique
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);

        // Création du chatbot
        Assistant assistant = AiServices.builder(Assistant.class)
                .streamingChatLanguageModel(streamingChatModel)
                .chatMemory(chatMemory)
                .build();

        // Affichage du résultat avec test de la mémoire
        System.out.println("💬: My name is Stéphane.");
        TokenStream tokenStream = assistant.chat("My name is Stéphane.");
        System.out.print("🤖: ");
        tokenStream
                .onNext(System.out::print)
                .onComplete(token -> {
                    System.out.println("💬: Do you remember what is my name?");
                    System.out.print("🤖: ");
                    assistant.chat("Do you remember what is my name?")
                            .onNext(System.out::print)
                            .onError(Throwable::printStackTrace).start();
                })
                .onError(Throwable::printStackTrace).start();
    }
}