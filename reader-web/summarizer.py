import sys
import os
from groq import Groq
from dotenv import load_dotenv

# Initialize Groq Client
load_dotenv()
groq_api_key = os.getenv('GROQ_API')
client = Groq(api_key=groq_api_key)

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
        You are tasked with generating a **comprehensive daily report** based on the provided information. The report should be **detailed, well-structured, and easy to read**, ensuring all critical updates are captured effectively.

        ### **Guidelines for Structuring the Report:**
        - **Thorough Coverage:** Include all key updates instead of a brief summary.
        - **Logical Organization:** Group related updates together for coherence.
        - **Readable Format:** Use bullet points, subheadings, or structured paragraphs for clarity.
        - **Essential Information:** Ensure the report conveys all necessary details.
        - **Appropriate Length:** Aim for **200–400 words**, depending on the diversity and volume of information.

        Now, generate a structured and detailed daily report based on the following text:  

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