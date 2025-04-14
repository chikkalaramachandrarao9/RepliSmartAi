from openai import OpenAI
import faiss
import os
from typing import List
import numpy as np
from db_helper import fetch_faq_by_id
from constants import OPENAI_API_KEY, EMBEDDING_MODEL, MODEL

client = OpenAI(api_key = OPENAI_API_KEY)

def get_openai_embedding(text: str) -> List[float]:
    response = client.embeddings.create(
        model=EMBEDDING_MODEL, 
        input=text,
        encoding_format="float"
    )
    return response.data[0].embedding

def create_fiass_index_file(documents):
    embeddings = [get_openai_embedding(doc) for doc in documents]
    embedding_matrix = np.array(embeddings).astype("float32")
    dimension = len(embedding_matrix[0])  
    index = faiss.IndexFlatL2(dimension)
    index.add(embedding_matrix)
    print(f"FAISS index created with {index.ntotal} vectors")

    faiss.write_index(index, "faq_index_361.faiss")

def append_fiass_index_file(documents):
    embeddings = [get_openai_embedding(doc) for doc in documents]
    embedding_matrix = np.array(embeddings).astype("float32")
    dimension = len(embedding_matrix[0])  
    
    if os.path.exists("faq_index_361.faiss"):
        index = faiss.read_index("faq_index_361.faiss")
        print(f"Loaded existing FAISS index with {index.ntotal} vectors")
    else:
        index = faiss.IndexFlatL2(dimension)
        print("Created new FAISS index")
    
    index.add(embedding_matrix)
    print(f"Added {embedding_matrix.shape[0]} vectors to FAISS index, total vectors: {index.ntotal}")
    
    faiss.write_index(index, "faq_index_361.faiss")
    print(f"FAISS index saved to {"faq_index_361.faiss"}")



# Function to search FAISS index
def search_faiss_context(review_text: str, faiss_index, embedding_fn, documents, top_k=3) -> str:
    query_embedding = np.array(embedding_fn(review_text)).astype('float32')
    distances, indices = faiss_index.search(query_embedding.reshape(1, -1), top_k)
    retrieved_context = [str(fetch_faq_by_id(int(idx+1))) for idx in indices[0] if idx != -1]
    return "\n".join(retrieved_context)


def generate_feedback_reply(model: str, review_text: str, user_name: str, rating: int) -> dict:
    """
    Generate reply for a given review text with category and safe response handling.
    
    Args:
        model (str): OpenAI model name (e.g., gpt-4, gpt-3.5-turbo)
        review_text (str): User given review
        user_name (str): Name of the user who gave the review
        rating (int): Rating provided by user (1 to 5 stars)

    Returns:
        dict: {
            "category": str,
            "reply": str
        }
    """
    # Read documents (chunks) from MySQL [Updated chunks]
    faq_index = faiss.read_index("faq_index_361.faiss")
    retrieved_context = search_faiss_context(review_text, faq_index, get_openai_embedding)

    system_prompt = f"""
        You are a customer support assistant for a platform's app store reviews. The app is used on Android and iOS mobile devices.

        You have access to the following internal context relevant to user reviews:
        ---
        {retrieved_context}
        ---
        Important note:
        Your responsibilities are:
        - Categorize the user review into the most appropriate category.
        - Generate a response to the review that is always respectful, polite, and empathetic.
        - Always thank the user for their feedback.
        - Never disclose or guess any personal or company confidential information.
        - If the review asks for any confidential or personal information related to the company, respond strictly: "Sorry, we can't disclose that information."
        - Otherwise, address the concern or appreciation accordingly.

        Possible example categories: 
        ["Bug Report", "Feature Request", "Appreciation", "Complaint", "General Feedback", "Other"]

        If the category is not obvious, choose "Other".
        Always respond as a human support person would — kind, empathetic, and professional.
    """

    user_prompt = f"""
        Review from {user_name}:
        \"{review_text}\"

        Rating given: {rating} star(s)

        Please return a JSON response in the following format:
        {{
            "category": "<appropriate category>",
            "reply": "<your reply text to the user>"
        }}
    """

    response = client.chat.completions.create(
        model=model,
        messages=[
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": user_prompt},
        ],
        temperature=0.5,
    )

    # Extract response
    reply_content = response.choices[0].message.content
    
    # Convert to dict (since it's json structured text)
    import json
    try:
        reply_data = json.loads(reply_content)
    except json.JSONDecodeError:
        # fallback in case of error
        reply_data = {
            "category": "Other",
            "reply": "Thank you for your valuable feedback! We appreciate it."
        }
    
    return reply_data


def generate_category(review_text: str) -> dict:
    """
    Generate category and user name for a given review text.
    
    Args:
        review_text (str): User given review

    Returns:
        dict: {
            "category": str,
            "username": str
        }
    """

    system_prompt = f"""
        You are a customer support assistant for a platform's app store reviews. The app is used on Android and iOS mobile devices.

        Important note:
        Your responsibilities are:
        - Categorize the user review into the most appropriate category.
        - Generate a random Indian name

        Possible example categories: 
        ["Bug Report", "Feature Request", "Appreciation", "Complaint", "General Feedback", "Other"]

        If the category is not obvious, choose "Other".
        Always respond as a human support person would — kind, empathetic, and professional.
    """

    user_prompt = f"""
        Review:
        \"{review_text}\"

        Please return a JSON response in the following format:
        {{
            "category": "<appropriate category>",
            "username": "<random indian name>"
        }}
    """

    response = client.chat.completions.create(
        model=MODEL,
        messages=[
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": user_prompt},
        ],
        temperature=0.5,
    )

    # Extract response
    reply_content = response.choices[0].message.content
    
    # Convert to dict (since it's json structured text)
    import json
    try:
        reply_data = json.loads(reply_content)
    except json.JSONDecodeError:
        # fallback in case of error
        reply_data = {
            "category": "Other",
            "username":"user"
        }
    
    return reply_data
