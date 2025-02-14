import google.generativeai as genai
from dotenv import load_dotenv
import os
import json
import glob
from git import Repo
import requests
import logging
from pathlib import Path
from datetime import datetime
import requests


# Setup basic logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(message)s')

# Load environment variables and configure API
load_dotenv()
GITHUB_TOKEN = os.getenv("GIT_TOKEN")
GEMINI_API_KEY = os.getenv("GEMINI_API_KEY")
USERNAME = os.getenv("USERNAME")
genai.configure(api_key=GEMINI_API_KEY)

# Constants
REPO_NAME = "project-1-team-20"
REPO_OWNER = "SE-course-serc"
REPO_URL = f"https://github.com/{REPO_OWNER}/{REPO_NAME}"
REPO_DIR = "./repository"
BRANCH_NAME = "refactored-code"
REPORT_PATH = "code_smells.txt"
BASE_BRANCH = "master"
PR_TITLE = "Automated Refactoring"
PR_DESC = "The LLMs are used to find design smells and refactor automatically using APIs."

def setup_repository():
    """Setup or update the repository"""
    try:
        if os.path.exists(REPO_DIR):
            repo = Repo(REPO_DIR)
            repo.git.reset('--hard')
            repo.git.clean('-fdx')
            repo.remotes.origin.pull()
        else:
            repo = Repo.clone_from(REPO_URL, REPO_DIR)
        return repo
    except Exception as e:
        logging.error(f"Repository setup failed: {e}")
        if os.path.exists(REPO_DIR):
            import shutil
            shutil.rmtree(REPO_DIR)
        return Repo.clone_from(REPO_URL, REPO_DIR)

def get_java_file_pairs():
    """Get pairs of Java files from the same directory"""
    java_files_by_dir = {}
    source_dirs = [
        f"{REPO_DIR}/reader-core/src/main/java",
        f"{REPO_DIR}/reader-web/src/main/java",
        f"{REPO_DIR}/reader-web-common/src/main/java"
    ]
    
    # Collect all Java files grouped by directory
    for source_dir in source_dirs:
        if os.path.exists(source_dir):
            for root, _, files in os.walk(source_dir):
                java_files = [os.path.join(root, f) for f in files if f.endswith('.java')]
                if java_files:
                    java_files.sort()  # Sort files within directory
                    java_files_by_dir[root] = java_files
    
    # Create pairs from files in the same directory
    pairs = []
    single_files = []
    
    for directory, files in java_files_by_dir.items():
        # Process files in pairs
        for i in range(0, len(files) - 1, 2):
            pairs.append((files[i], files[i + 1]))
        
        # Handle last file if odd number of files
        if len(files) % 2 != 0:
            single_files.append(files[-1])
    
    # Log the grouping information
    logging.info(f"Found files in {len(java_files_by_dir)} directories")
    total_files = sum(len(files) for files in java_files_by_dir.values())
    logging.info(f"Total Java files: {total_files}")
    logging.info(f"Created {len(pairs)} pairs")
    if single_files:
        logging.info(f"Single files to analyze individually: {len(single_files)}")
    
    # Return both pairs and single files
    return pairs, single_files

def analyze_code(file1_code, file2_code=""):
    """Analyze Java files for code smells"""
    if file2_code:
        prompt = f"""
        Analyze these two Java files for code and design smells:
        
        File 1:
        ```java
        {file1_code}
        ```
        
        File 2:
        ```java
        {file2_code}
        ```
        """
    else:
        prompt = f"""
        Analyze this Java file for code and design smells:
        
        ```java
        {file1_code}
        ```
        """
    
    prompt += """
    Respond in JSON:
    {
        "smells": [
            {
                "type": "smell_type",
                "category": "code|design",
                "description": "brief description",
                "files": ["file1"|"file2"|"both"],
                "fix": "suggested fix"
            }
        ]
    }
    """
    
    try:
        model = genai.GenerativeModel("gemini-pro")
        response = model.generate_content(prompt)
        text = response.text.strip()
        start = text.find('{')
        end = text.rfind('}') + 1
        if start >= 0 and end > start:
            return json.loads(text[start:end]).get("smells", [])
    except Exception as e:
        logging.error(f"Analysis failed: {e}")
        return []

def refactor_code(file1_code, file2_code="", smells=None):
    """Refactor Java files based on identified smells"""
    if not smells:
        return file1_code, file2_code
        
    if file2_code:
        prompt = f"""
        Refactor these Java files (Java 8) to fix these issues:
        {json.dumps(smells, indent=2)}
        
        File 1:
        ```java
        {file1_code}
        ```
        
        File 2:
        ```java
        {file2_code}
        ```
        """
    else:
        prompt = f"""
        Refactor this Java file (Java 8) to fix these issues:
        {json.dumps(smells, indent=2)}
        
        ```java
        {file1_code}
        ```
        """
    
    prompt += """
    Rules:
    You are a Java expert working on "java version 8". Refactor this Java code to fix these issues while following these STRICT rules:
    1. MUST Ensure that the refactoring process is robust, preserving the functionality of the code while enhancing its design.
    2. The output MUST be valid Java code that compiles
    3. MUST keep the exact same package declaration
    4. MUST keep all original imports
    5. MUST keep the same class name and visibility
    6. MUST maintain all public method signatures
    7. MUST include all class-level annotations
    8. MUST maintain proper opening/closing braces
    9. DO NOT include any markdown formatting or code block markers
    
    Respond with both files separated by: ====FILE_DELIMITER==== (for two files)
    or just the refactored code (for single file)
    """
    
    try:
        model = genai.GenerativeModel("gemini-pro")
        response = model.generate_content(prompt)
        code = response.text.strip()
        
        if file2_code:
            if "====FILE_DELIMITER====" in code:
                file1_new, file2_new = code.split("====FILE_DELIMITER====")
                return file1_new.strip(), file2_new.strip()
            return file1_code, file2_code
        else:
            return code.strip(), ""
    except Exception as e:
        logging.error(f"Refactoring failed: {e}")
        return file1_code, file2_code


def get_existing_pull_request():
    """Check if a pull request already exists for the branch."""
    url = f"https://api.github.com/repos/{REPO_OWNER}/{REPO_NAME}/pulls"
    headers = {
        "Authorization": f"token {GITHUB_TOKEN}",
        "Accept": "application/vnd.github.v3+json"
    }
    payload = {
        "title": PR_TITLE,
        "body": PR_DESC,
        "head": BRANCH_NAME,
        "base": BASE_BRANCH
    }
    response = requests.get(url,json=payload, headers=headers)

    # Debugging: Print the response status and content
    print(f"Status Code: {response.status_code}")

    if response.status_code == 200:
        prs = response.json()
        if prs:  # Check if the list is not empty
            # Return the most recent PR (first in the list)
            return prs[0]
    return None



def close_pull_request(pr_number):
    """Close an existing pull request."""
    url = f"https://api.github.com/repos/{REPO_OWNER}/{REPO_NAME}/pulls/{pr_number}"
    headers = {
        "Authorization": f"token {GITHUB_TOKEN}",
        "Accept": "application/vnd.github.v3+json"
    }
    payload = {
        "state": "closed"
    }
    response = requests.patch(url, json=payload, headers=headers)

    if response.status_code == 200:
        print(f"Closed existing pull request #{pr_number}")
    else:
        print(f"Error closing PR #{pr_number}: {response.json()}")


def create_pull_request():
    """Creates a GitHub Pull Request, deleting an existing one if necessary."""
    # Check for an existing pull request
    existing_pr = get_existing_pull_request()

    if existing_pr:
        print(f"Existing pull request found: #{existing_pr['number']}")
        close_pull_request(existing_pr['number'])

    # Create a new pull request
    url = f"https://api.github.com/repos/{REPO_OWNER}/{REPO_NAME}/pulls"
    headers = {
        "Authorization": f"token {GITHUB_TOKEN}",
        "Accept": "application/vnd.github.v3+json"
    }
    payload = {
        "title": PR_TITLE,
        "body": PR_DESC,
        "head": BRANCH_NAME,
        "base": BASE_BRANCH
    }
    response = requests.post(url, json=payload, headers=headers)

    if response.status_code == 201:
        print("Pull request created successfully!")
        print(response.json()["html_url"])
    elif response.status_code == 422 and "already exists" in response.json().get("message", ""):
        # Handle the case where the PR still exists (e.g., race condition)
        print("Pull request already exists. Attempting to close and recreate...")
        existing_pr = get_existing_pull_request()
        if existing_pr:
            close_pull_request(existing_pr['number'])
            # Retry creating the PR
            response = requests.post(url, json=payload, headers=headers)
            if response.status_code == 201:
                print("Pull request created successfully after retry!")
                print(response.json()["html_url"])
            else:
                print(f"Error creating PR after retry: {response.json()}")
    else:
        print(f"Error creating PR: {response.json()}")


def main():
    try:
        # Setup repository
        repo = setup_repository()
        
        # Get file pairs and single files to analyze
        file_pairs, single_files = get_java_file_pairs()
        total_pairs = len(file_pairs)
        total_singles = len(single_files)
        report_lines = []
        files_analyzed = set()
        
        # Process pairs first
        logging.info("Processing file pairs...")
        for index, (file1_path, file2_path) in enumerate(file_pairs, 1):
            try:
                logging.info(f"Processing pair {index}/{total_pairs}")
                logging.info(f"Analyzing:\n- {file1_path}\n- {file2_path}")
                
                # Read files
                with open(file1_path, 'r', encoding='utf-8') as f:
                    file1_code = f.read()
                with open(file2_path, 'r', encoding='utf-8') as f:
                    file2_code = f.read()
                
                # Skip small files
                if len(file1_code.split('\n')) <= 100 or len(file2_code.split('\n')) <= 100:
                    continue
                
                # Analyze and refactor
                smells = analyze_code(file1_code, file2_code)
                if smells:
                    file1_new, file2_new = refactor_code(file1_code, file2_code, smells)
                    
                    # Write refactored code
                    with open(file1_path, 'w', encoding='utf-8') as f:
                        f.write(file1_new)
                    with open(file2_path, 'w', encoding='utf-8') as f:
                        f.write(file2_new)
                    
                    report_lines.append(
                        f"Pair {index}/{total_pairs}:\n"
                        f"Files:\n- {file1_path}\n- {file2_path}\n"
                        f"Smells Found:\n{json.dumps(smells, indent=2)}\n\n"
                    )
                
                files_analyzed.add(file1_path)
                files_analyzed.add(file2_path)
            
            except Exception as e:
                logging.error(f"Error processing pair {index}/{total_pairs}: {e}")
        
        # Process single files
        if single_files:
            logging.info("\nProcessing single files...")
            for index, file_path in enumerate(single_files, 1):
                try:
                    logging.info(f"Processing single file {index}/{total_singles}: {file_path}")
                    
                    with open(file_path, 'r', encoding='utf-8') as f:
                        file_code = f.read()
                    
                    # Skip small files
                    if len(file_code.split('\n')) <= 100:
                        continue
                    
                    # Analyze single file
                    smells = analyze_code(file_code)
                    if smells:
                        file_new, _ = refactor_code(file_code, smells=smells)
                        
                        with open(file_path, 'w', encoding='utf-8') as f:
                            f.write(file_new)
                        
                        report_lines.append(
                            f"Single File {index}/{total_singles}:\n"
                            f"File: {file_path}\n"
                            f"Smells Found:\n{json.dumps(smells, indent=2)}\n\n"
                        )
                    
                    files_analyzed.add(file_path)
                
                except Exception as e:
                    logging.error(f"Error processing single file {file_path}: {e}")
        
        # Log analysis summary
        all_java_files = set(f for pair in file_pairs for f in pair).union(set(single_files))
        unanalyzed_files = all_java_files - files_analyzed
        
        logging.info(f"\nAnalysis Summary:")
        logging.info(f"Total Java files found: {len(all_java_files)}")
        logging.info(f"Files analyzed: {len(files_analyzed)}")
        logging.info(f"Files skipped or errored: {len(unanalyzed_files)}")
        
        if unanalyzed_files:
            logging.warning("Files not analyzed:")
            for file in unanalyzed_files:
                logging.warning(f"- {file}")
        
        # Save report with summary
        with open(os.path.join(REPO_DIR, REPORT_PATH), 'w') as f:
            f.write(f"Analysis Summary:\n")
            f.write(f"Total Java files: {len(all_java_files)}\n")
            f.write(f"Files analyzed: {len(files_analyzed)}\n")
            f.write(f"Files skipped or errored: {len(unanalyzed_files)}\n\n")
            f.write("Detailed Analysis:\n")
            f.write("".join(report_lines))
        
        # Create pull request if changes were made
        if report_lines:
            repo.git.add(A=True)
            commit_message = (
                f"Automated code refactoring\n\n"
                f"Analyzed {len(files_analyzed)} files\n"
                f"Found and fixed issues in {len(report_lines)} file pairs/singles"
            )
            repo.index.commit(commit_message)
            repo.git.add(A=True)
            repo.git.push('--force', 'origin', BRANCH_NAME)  # Use force push
            create_pull_request()
            logging.info("Changes committed and pull request created")
        else:
            logging.info("No changes were made, skipping pull request")
        
    except Exception as e:
        logging.error(f"Process failed: {e}")
        raise

if __name__ == "__main__":
    main()