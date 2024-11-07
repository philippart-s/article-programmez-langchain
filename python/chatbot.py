from dotenv import load_dotenv
import os

from langchain_mistralai import ChatMistralAI
from langchain_core.prompts import ChatPromptTemplate

# Chargement de la configuration                                              (1)
load_dotenv(dotenv_path='../.env')

# Paramétrage du modèle à utiliser                                            (2)
model = ChatMistralAI(model="Mistral-7B-Instruct-v0.2", 
                        api_key=os.getenv('OVH_AI_ENDPOINTS_ACCESS_TOKEN'),
                        endpoint='https://mistral-7b-instruct-v02.endpoints.kepler.ai.cloud.ovh.net/api/openai_compat/v1', 
                        max_tokens=512)

# Définition des prompts                                                      (3)
prompt = ChatPromptTemplate.from_messages([
  ("system", "You are Nestor, a virtual assistant. Answer to the question."),
  ("human", "{question}"),
])

# Création de la "chaîne"                                                     (4)
chain = prompt | model

# Interrogation du modèle                                                     (5)
response = chain.invoke("What is Programmez magazine?")

print(f"🤖: {response.content}")