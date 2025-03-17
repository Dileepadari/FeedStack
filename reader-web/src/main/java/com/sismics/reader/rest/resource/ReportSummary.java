package com.sismics.reader.rest.resource;

public class ReportSummary implements SummarizationStrategy{
    @Override
    public String summarize(String text, double weight) {
        try {
            return this.callPythonSummarizer(text, weight, 2);
        } catch (Exception e) {
            return "Error generating summary: " + e.getMessage();
        }
    }
}
