import os
import requests
from transformers import T5ForConditionalGeneration, T5Tokenizer
from dotenv import load_dotenv
from typing import Dict, List, Tuple

# Load environment variables
load_dotenv()

# Configuration
HF_API_URL = "https://api-inference.huggingface.co/models/"
HF_MODEL_NAME = "codellama/CodeLlama-7b-hf"
HF_TOKEN = os.getenv("HF_API_TOKEN")
T5_MODEL_NAME = "t5-small"

def load_files_from_directory(directory: str) -> Dict[str, str]:
    """Load code files from directory recursively."""
    file_contents = {}
    for root, _, files in os.walk(directory):
        for file in files:
            if file.endswith(('.java', '.py', '.js')):
                file_path = os.path.join(root, file)
                try:
                    with open(file_path, 'r', encoding='utf-8') as f:
                        file_contents[file_path] = f.read()
                except Exception as e:
                    print(f"Error reading {file_path}: {str(e)}")
    return file_contents

def analyze_with_hf(file_contents: Dict[str, str]) -> Dict[str, Tuple[str, str]]:
    """Analyze using CodeLlama via HF API."""
    responses = {}
    headers = {"Authorization": f"Bearer {HF_TOKEN}"}
    print("Analyzing with CodeLlama...")
    
    for file, content in file_contents.items():
        print(f"Processing {file}...")
        try:
            prompt = (
                "Analyze this code and identify two significant design smells. "
                "For each smell:\n"
                "1. Name the design smell\n"
                "2. Explain why it's present\n"
                "3. Suggest specific refactorings\n"
                f"Code:\n{content}"
            )
            
            response = requests.post(
                HF_API_URL + HF_MODEL_NAME,
                headers=headers,
                json={"inputs": prompt, "parameters": {"max_length": 800}}
            )
            responses[file] = (prompt, response.json()[0]['generated_text'])
        except Exception as e:
            responses[file] = (prompt, f"HF API Error: {str(e)}")
    
    return responses

def analyze_with_t5(file_contents: Dict[str, str]) -> Dict[str, Tuple[str, str]]:
    """Analyze using local T5 model."""
    responses = {}
    try:
        tokenizer = T5Tokenizer.from_pretrained(T5_MODEL_NAME)
        model = T5ForConditionalGeneration.from_pretrained(T5_MODEL_NAME)
    except Exception as e:
        return {file: ("", f"Model load error: {str(e)}") for file in file_contents}
    
    print("Analyzing with T5...")
    for file, content in file_contents.items():
        prompt = ""
        try:
            prompt = (
                "Identify two code design issues in this code. "
                "For each issue:\n"
                "- Name the problem\n"
                "- Explain detection reason\n"
                "- Suggest improvements\n"
                f"Code:\n{content[:1500]}"  # More conservative truncation
            )
            
            inputs = tokenizer.encode(
                prompt,
                return_tensors="pt",
                max_length=512,
                truncation=True
            )
            outputs = model.generate(inputs, max_length=600)
            responses[file] = (prompt, tokenizer.decode(outputs[0], skip_special_tokens=True))
        except Exception as e:
            responses[file] = (prompt, f"T5 Error: {str(e)}")
    
    return responses

def extract_design_smells(text: str) -> Dict[str, List[str]]:
    """Extract design smells and refactorings from response."""
    findings = {
        "smells": [],
        "refactorings": [],
        "explanations": []
    }
    
    current_smell = None
    for line in text.split('\n'):
        line = line.strip()
        if not line:
            continue
        
        # Detect smell names
        if line.lower().startswith(("1.", "2.", "- smell", "- issue")):
            if ":" in line:
                smell_name = line.split(":", 1)[1].strip()
            else:
                smell_name = line.split(".", 1)[-1].strip()
            findings["smells"].append(smell_name)
            current_smell = smell_name
        
        # Capture explanations and refactorings
        elif current_smell:
            if "explain" in line.lower() or "reason" in line.lower():
                findings["explanations"].append(f"{current_smell}: {line}")
            elif "suggest" in line.lower() or "refactor" in line.lower():
                findings["refactorings"].append(f"{current_smell}: {line}")
    
    return findings

def save_comparison_report(hf_results: Dict[str, Tuple[str, str]], t5_results: Dict[str, Tuple[str, str]]):
    """Generate detailed comparison report with full responses."""
    with open("design_smell_comparison.md", "w", encoding="utf-8") as f:
        f.write("# LLM Design Smell Comparison Report\n\n")
        f.write("## Model Descriptions\n")
        f.write("### CodeLlama (Hugging Face)\n")
        f.write("- Code-specific LLM trained on programming languages\n")
        f.write("- 7 billion parameters\n")
        f.write("- Specializes in code analysis and generation\n\n")
        
        f.write("### T5-small (Local)\n")
        f.write("- General-purpose text-to-text model\n")
        f.write("- 60 million parameters\n")
        f.write("- Fine-tuned on various NLP tasks\n\n")
        
        f.write("## Analysis Results\n")
        for file in hf_results:
            hf_prompt, hf_response = hf_results[file]
            t5_prompt, t5_response = t5_results.get(file, ("", "No analysis"))
            
            f.write(f"### File: `{file}`\n")
            
            f.write("#### Prompts Used\n")
            f.write("##### CodeLlama Prompt\n```\n" + hf_prompt + "\n```\n")
            f.write("##### T5 Prompt\n```\n" + t5_prompt + "\n```\n")
            
            f.write("#### CodeLlama Analysis\n")
            f.write("##### Raw Response\n```\n" + hf_response + "\n```\n")
            hf_findings = extract_design_smells(hf_response)
            if hf_findings["smells"]:
                f.write("\n##### Detected Smells\n")
                for smell in hf_findings["smells"][:2]:
                    f.write(f"- {smell}\n")
                f.write("\n##### Suggested Refactorings\n")
                for refac in hf_findings["refactorings"][:2]:
                    f.write(f"- {refac}\n")
            else:
                f.write("\nNo structured design smells detected in response\n")
            
            f.write("\n#### T5 Analysis\n")
            f.write("##### Raw Response\n```\n" + t5_response + "\n```\n")
            t5_findings = extract_design_smells(t5_response)
            if t5_findings["smells"]:
                f.write("\n##### Detected Smells\n")
                for smell in t5_findings["smells"][:2]:
                    f.write(f"- {smell}\n")
                f.write("\n##### Suggested Refactorings\n")
                for refac in t5_findings["refactorings"][:2]:
                    f.write(f"- {refac}\n")
            else:
                f.write("\nNo structured design smells detected in response\n")
            
            f.write("\n---\n")
        
        f.write("## Final Observations\n")
        f.write("- Full responses preserved for manual inspection\n")
        f.write("- Structured analysis attempts to parse key findings\n")
        f.write("- Raw responses show actual model output quality\n")

def main():
    """Main workflow"""
    directory = "./reader-core/src/main/java/com/sismics/reader/core/service"
    
    print("Loading files...")
    files = load_files_from_directory(directory)
    
    print("\nRunning CodeLlama analysis...")
    hf_results = analyze_with_hf(files)
    
    print("\nRunning T5 analysis...")
    t5_results = analyze_with_t5(files)
    
    print("\nGenerating report...")
    save_comparison_report(hf_results, t5_results)
    
    print("\nAnalysis complete! Open design_smell_comparison.md")

if __name__ == "__main__":
    main()