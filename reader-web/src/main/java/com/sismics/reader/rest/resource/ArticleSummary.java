package com.sismics.reader.rest.resource;


public class ArticleSummary implements SummarizationStrategy {
    @Override
    public String summarize(String text, double weight) {
        try {
            return this.callPythonSummarizer(text, weight, 1);
        } catch (Exception e) {
            return "Error generating summary: " + e.getMessage();
        }
    }
}
