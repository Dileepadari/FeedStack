import sys
import os
from groq import Groq

# Initialize Groq Client
client = Groq(api_key="GROQ_API_KEY_REMOVED")

def summarize_text(text,weight,prompt_type=1):
    max_weight = 1
    if prompt_type == 1:
        prompt = f"""
        Summarize the following text concisely while maintaining clarity and readability. 
        The weight of the text is {weight}, with a maximum weight of {max_weight}.
        
        - If the weight is high, provide a well-structured, detailed summary that highlights key insights, important points, and relevant context.
        - If the weight is low, ensure the summary remains brief yet informative, avoiding unnecessary complexity.
        - Make the summary naturally readable for users without explicitly mentioning the weight.

        Text: {text}
    """

    else :
        prompt = f"""
        You are generating a **daily report** from the provided information. The goal is to ensure the report is **detailed yet structured**, capturing all major updates while maintaining clarity.

        **Guidelines:**
        - **Cover all key updates** rather than providing a minimal summary.
        - **Group related updates together** for better readability.
        - **Use bullet points or structured paragraphs** to make it easy to scan.
        - **Ensure a balanced level of detail**—not too brief, but not overly technical.

        Now, generate a well-structured daily report based on this text:  

        {text}
    """

    try:
        response = client.chat.completions.create(
            messages=[
                {"role": "system", "content": "You are an expert summarizer."},
                {"role": "user", "content": prompt}
            ],
            model="llama-3.1-8b-instant"
        )
        return response.choices[0].message.content
    except Exception as e:
        print(f"Error: {str(e)}", file=sys.stderr)
        return f"Failed to summarize: {str(e)}"


if __name__ == "__main__":
    # Retrieve arguments passed from Java
    if len(sys.argv) > 1:
        text = sys.argv[1]
        weight = float(sys.argv[2])
        prompt_type = int(sys.argv[3])
        summary = summarize_text(text,weight)
        print(summary)
    else:
        print("No text provided for summarization")