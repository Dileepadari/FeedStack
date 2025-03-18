# Task 6 - LLM Features

## 6a - Daily Report

### 1. Tasks Implemented

#### Article Summarization

- The system fetches articles from the database and applies AI-based summarization using an external Python script (`summarizer.py`).<br>

- The `ArticleSummary` and `ReportSummary` classes handle the summarization logic.

- The summarization is influenced by a weighting mechanism based on the category hierarchy.


#### User-Specific Summaries
- The system generates personalized daily reports for users based on their subscribed feeds.
- The daily report is generated for `10` most recent subscribed articles.
- It organizes the latest articles into a structured format for better readability.
- If no new articles exist, the system ensures an empty response instead of returning errors.



#### Weighted Summarization
- Articles from a top-level category receive a higher weight (1.0).
- Articles from subcategories receive a lower weight (0.7) to emphasize higher-level content.
- Higher the weight, the llm's response would be more elaborative.
- The determineCategoryWeight() method (in `ArticleSummaryResource` and `GenerateResource`) applies this logic by analyzing a user’s feed subscriptions.


### 2. Design Pattern Used
#### Strategy Pattern
The Strategy Pattern is used to separate the summarization logic from the main application logic.

Rationale : 
- Encapsulation of Summarization Logic: <br>
The summarization process is abstracted into the SummarizationStrategy interface.
Concrete implementations (ArticleSummary and ReportSummary) handle different summarization tasks.

- Flexibility & Extensibility : <br>
New summarization techniques (e.g., different AI models, external APIs) can be introduced by simply creating a new implementation of SummarizationStrategy.
The system can dynamically switch between different summarization methods (e.g., short summaries for articles, detailed summaries for reports).


Implementation : 

```
@startuml

interface SummarizationStrategy {
    + summarize(text: String, weight: double): String
    + callPythonSummarizer(text: String, weight: double, promptType: int): String
}

class ArticleSummary {
    + summarize(text: String, weight: double): String
}

class ReportSummary {
    + summarize(text: String, weight: double): String
}

class ArticleSummaryResource {
    - summarizationStrategy: SummarizationStrategy
    + get(unread: boolean, limit: Integer, afterArticle: String): Response
}

class GenerateResource {
    - articlesummary: SummarizationStrategy
    - reportsummary: SummarizationStrategy
    + get(unread: boolean, limit: Integer, afterArticle: String): Response
}

SummarizationStrategy <|.. ArticleSummary
SummarizationStrategy <|.. ReportSummary

ArticleSummaryResource --> SummarizationStrategy
GenerateResource --> SummarizationStrategy

@enduml

```


## 6b - Duplicate Detection

### 1. Tasks Implemented
####  Article Similarity Detection
- The system identifies duplicate articles based on Named Entity Recognition (NER) and semantic similarity.
- It uses spaCy for extracting named entities (e.g., organizations, people, and locations).
- Cosine similarity is applied to article embeddings for semantic comparison.
- The final similarity score is computed using a weighted sum:
    - 40% weight to named entity overlap.
    - 60% weight to semantic similarity.

#### Duplicate Article Filtering
- If two articles exceed a similarity threshold (default 0.8), they are marked as duplicates.
- The system filters out duplicate articles, ensuring users receive only unique content.



### 2. Design Pattern Used
#### Command Pattern 
- Encapsulates duplicate detection logic in a separate command object (DetectorCommand).
- Allows dynamic execution of commands (e.g., using PythonDetectorCommand for Python-based detection).
- Decouples request sender (DetectorResource) from the actual detection logic, making the system more flexible.

````
@startuml

interface DetectorCommand {
    + execute(articleIds: List<String>, titles: List<String>, descriptions: List<String>, threshold: double): String
}

class PythonDetectorCommand {
    + execute(articleIds: List<String>, titles: List<String>, descriptions: List<String>, threshold: double): String
}

class DetectorResource {
    - detectorCommand: DetectorCommand
    + get(unread: boolean, limit: Integer, afterArticle: String): Response
}

DetectorCommand <|.. PythonDetectorCommand
DetectorResource --> DetectorCommand

@enduml

````