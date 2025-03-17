import spacy
import numpy as np
from sklearn.metrics.pairwise import cosine_similarity
import json
import os
import sys
import subprocess

# Ensure spaCy is installed
try:
    import spacy
except ImportError:
    subprocess.run(["python", "-m", "pip", "install", "spacy"], check=True)
    import spacy

# Ensure the model is installed
try:
    nlp = spacy.load("en_core_web_sm")
except OSError:
    subprocess.run(["python", "-m", "spacy", "download", "en_core_web_sm"], check=True)
    nlp = spacy.load("en_core_web_sm")

def extract_named_entities(text):
    """Extract named entities (ORG, PERSON, GPE, etc.) from text."""
    doc = nlp(text)
    return set([ent.text.lower() for ent in doc.ents])  # Lowercased for consistency

def get_spacy_embedding(text):
    """Get an embedding using spaCy word vectors."""
    doc = nlp(text)
    return doc.vector  # Returns a fixed-size vector

def compute_similarity(title1, desc1, title2, desc2):
    """Compare two articles based on NER and semantic embeddings."""
    
    # 1. Extract named entities
    entities1 = extract_named_entities(title1 + " " + desc1)
    entities2 = extract_named_entities(title2 + " " + desc2)
    
    # Compute entity overlap
    entity_score = len(entities1.intersection(entities2)) / max(len(entities1), len(entities2), 1)

    # 2. Generate embeddings
    embedding1 = get_spacy_embedding(title1 + " " + desc1)
    embedding2 = get_spacy_embedding(title2 + " " + desc2)
    
    # Compute cosine similarity
    semantic_score = cosine_similarity([embedding1], [embedding2])[0][0]
    
    # 3. Combine scores (weighted sum)
    final_score = 0.4 * entity_score + 0.6 * semantic_score  # Adjust weights as needed
    return final_score

def find_duplicate_articles(article_ids, titles, descriptions, threshold=0.7):
    """Return a list of duplicate article ID pairs based on similarity score."""
    
    duplicate_pairs = []
    n = len(titles)
    
    for i in range(n):
        for j in range(i + 1, n):  # Avoid redundant comparisons
            similarity = compute_similarity(titles[i], descriptions[i], titles[j], descriptions[j])
            
            if similarity > threshold:
                duplicate_pairs.append((article_ids[i], article_ids[j]))  # Store duplicate pairs
    
    return duplicate_pairs

if __name__ == "__main__":
    if len(sys.argv) != 5:
        print(json.dumps({"error": "Usage: python script.py '<article_ids_json>' '<titles_json>' '<descriptions_json>'"}))
        sys.exit(1)

    try:
        # Deserialize JSON input from Java ProcessBuilder
        article_ids = json.loads(sys.argv[1])
        titles = json.loads(sys.argv[2])
        descriptions = json.loads(sys.argv[3])
        threshold = float(sys.argv[4])

        # Ensure valid input
        if not (isinstance(article_ids, list) and isinstance(titles, list) and isinstance(descriptions, list)):
            raise ValueError("All inputs must be lists.")
        if not (len(article_ids) == len(titles) == len(descriptions)):
            raise ValueError("Mismatched lengths of input lists.")
 
        # Run duplicate detection
        duplicates = find_duplicate_articles(article_ids, titles, descriptions,threshold)

        # Print output as JSON (Java will read this)
        print(json.dumps({"duplicates": duplicates}))

    except Exception as e:
        print(json.dumps({"error": str(e)}))
        sys.exit(1)