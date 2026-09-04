"""
Article summariser, invoked by SummarizationStrategy in the web app.

    python3 summarizer.py "<text>" <weight> <prompt_type>

Prints the summary on stdout. Any failure prints a short reason on stderr and
exits non-zero; nothing this writes is shown to a reader, so it does not need
to be user-facing prose.

This used to create a virtualenv, run `pip install -r requirements.txt` and, if
that failed, `sudo apt install python3.10-venv`, all on the first request that
asked for a summary. That is not a thing a web request should do: it fetches and
installs code from the network at request time, and the sudo path meant a
request could install system packages. Dependencies are the operator's job now.
Install them once:

    pip install -r requirements.txt
"""

import os
import sys

try:
    from groq import Groq
    from dotenv import load_dotenv
except ImportError as exc:
    sys.stderr.write(
        "summarizer.py: missing dependency ({}). "
        "Run: pip install -r requirements.txt\n".format(exc.name)
    )
    raise SystemExit(2)

# Initialize Groq Client
load_dotenv('tokens.env')
groq_api_key = os.getenv('GROQ_API')

if not groq_api_key:
    sys.stderr.write(
        "summarizer.py: GROQ_API is not set. "
        "Copy tokens.env.example to tokens.env and fill it in.\n"
    )
    raise SystemExit(3)

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
        # stderr and a non-zero exit, never stdout: whatever lands on stdout is
        # treated by the caller as the summary itself.
        sys.stderr.write("summarizer.py: {}\n".format(e))
        raise SystemExit(4)


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