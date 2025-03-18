import json
import sys
import subprocess
import os

venv_dir = 'se_p2_env'
venv_python = os.path.join(venv_dir, 'Scripts', 'python.exe') if os.name == 'nt' else os.path.join(venv_dir, 'bin', 'python')

def try_install_with_fallback(package_command):
    """
    Attempt to run installation using venv_python -m pip, 
    fallback to pip and pip3 if it fails.
    """
    try:
        subprocess.run([venv_python, '-m', 'pip'] + package_command, check=True)
    except subprocess.CalledProcessError:
        print("venv pip failed, trying system pip...")
        try:
            subprocess.run(['pip'] + package_command, check=True)
        except subprocess.CalledProcessError:
            print("System pip failed, trying pip3...")
            subprocess.run(['pip3'] + package_command, check=True)

try:
    import spacy
    import numpy as np
    from sklearn.metrics.pairwise import cosine_similarity
except ImportError:
    # Create virtual environment if not present
    if not os.path.exists(venv_dir):
        try:
            print("Creating virtual environment...")
            subprocess.run([sys.executable, '-m', 'venv', venv_dir], check=True)
        except subprocess.CalledProcessError:
            print("Failed to create venv. Installing python3.10-venv and retrying...")
            subprocess.run(['sudo', 'apt', 'install', '-y', 'python3.10-venv'], check=True)
            subprocess.run([sys.executable, '-m', 'venv', venv_dir], check=True)


    # Upgrade pip (try/fallback)
    print("Upgrading pip in the virtual environment (with fallback)...")
    try_install_with_fallback(['install', '--upgrade', 'pip'])

    # Install dependencies (try/fallback)
    print("Installing dependencies (with fallback)...")
    try_install_with_fallback(['install', '-r', 'requirements.txt'])

    # Try importing again
    import spacy
    import numpy as np
    from sklearn.metrics.pairwise import cosine_similarity

print("All dependencies are installed and ready.")


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