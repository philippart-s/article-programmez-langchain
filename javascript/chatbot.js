import { ChatMistralAI } from "@langchain/mistralai";
import { ChatPromptTemplate } from "@langchain/core/prompts";
import { setTimeout } from "timers/promises";
import {config} from 'dotenv';

// chargement de la configuration
config({path: '../.env'});

// Paramétrage du modèle à utiliser 
const model = new ChatMistralAI({                                           // (1)     
  modelName: "Mistral-7B-Instruct-v0.2",
  model: "Mistral-7B-Instruct-v0.2",
  apiKey: process.env.OVH_AI_ENDPOINTS_ACCESS_TOKEN,
  endpoint: "https://mistral-7b-instruct-v02.endpoints.kepler.ai.cloud.ovh.net/api/openai_compat",
  maxTokens: 512
});

// Définition des prompts
const promptTemplate = ChatPromptTemplate.fromMessages([                    // (2)
  ["system", "You are Nestor, a virtual assistant. Answer to the question."],
  ["human", "{question}"]
]);

// Création de la "chaîne"
const chain = promptTemplate.pipe(model);                                   // (3)

// Interrogation du modèle 
const stream = await chain.stream({ question: "What is Programmez magazine?" });       // (4)
console.log("Nestor 🤖: ");

for await (const chunk of stream) {                                         // (5)
  // Timeout pour simuler la réponse humaine
  await setTimeout(50);
  process.stdout.write(chunk.content);
}