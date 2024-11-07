///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS dev.langchain4j:langchain4j:0.35.0
//DEPS dev.langchain4j:langchain4j-mistral-ai:0.35.0
//DEPS dev.langchain4j:langchain4j-ovh-ai:0.35.0

import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocument;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.mistralai.MistralAiStreamingChatModel;
import dev.langchain4j.model.ovhai.OvhAiEmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import java.util.List;

public class RAG {

  // Récupération de l'API key pour utiliser AI Endpoints
  private static final String OVHCLOUD_API_KEY = System.getenv(
    "OVH_AI_ENDPOINTS_ACCESS_TOKEN"
  );

  // Utilisation de l'approche AI Service
  interface Assistant {
    @SystemMessage(
      "You are Nestor, a virtual assistant. Answer to the question."
    )
    TokenStream chat(String userMessage);
  }

  public static void main(String[] args) {
    // Chargement du document et découpage en sous parties
    DocumentParser documentParser = new TextDocumentParser();
    Document document = loadDocument("./olympic-results.txt", documentParser);
    DocumentSplitter splitter = DocumentSplitters.recursive(500, 0);

    List<TextSegment> segments = splitter.split(document);

    // Vectorisation des "chunks" avec le modèle d'embedding proposé par AI Endpoints
    EmbeddingModel embeddingModel = OvhAiEmbeddingModel
      .builder()
      .apiKey(OVHCLOUD_API_KEY)
      .build();
    List<Embedding> embeddings = embeddingModel.embedAll(segments).content();

    // Stockage des vecteurs et paramétrage de la recherche vectorielle
    EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
    embeddingStore.addAll(embeddings, segments);
    ContentRetriever contentRetriever = EmbeddingStoreContentRetriever
      .builder()
      .embeddingStore(embeddingStore)
      .embeddingModel(embeddingModel)
      .maxResults(5)
      .minScore(0.9)
      .build();

    // Choix et paramétrage du modèle
    MistralAiStreamingChatModel streamingChatModel = MistralAiStreamingChatModel
      .builder()
      .apiKey(OVHCLOUD_API_KEY)
      .modelName("Mistral-7B-Instruct-v0.2")
      .baseUrl(
        "https://mistral-7b-instruct-v02.endpoints.kepler.ai.cloud.ovh.net/api/openai_compat/v1"
      )
      .maxTokens(512)
      .temperature(0.0)
      .build();

    // Création du chatbot
    Assistant assistant = AiServices
      .builder(Assistant.class)
      .streamingChatLanguageModel(streamingChatModel)
      .contentRetriever(contentRetriever)
      .build();

    // Affichage du résultat avec le RAG activé
    System.out.println(
      "💬: Can you give the number of gold medal for France during the last olympic games?"
    );

    TokenStream tokenStream = assistant.chat(
      "Can you give the number of gold medal for France during the last olympic games?"
    );
    System.out.println("🤖: ");
    tokenStream
      .onNext(System.out::print)
      .onError(Throwable::printStackTrace)
      .start();
  }
}
