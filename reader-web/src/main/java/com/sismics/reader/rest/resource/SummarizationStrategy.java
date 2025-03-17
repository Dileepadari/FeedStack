package com.sismics.reader.rest.resource;

import java.io.BufferedReader;
import java.io.InputStreamReader;


interface SummarizationStrategy {
    String summarize(String text, double weight);
    default String callPythonSummarizer(String text, double weight, int promptType) {
        try {
            String scriptPath = System.getProperty("user.dir") + "/summarizer.py";
            ProcessBuilder pb = new ProcessBuilder("python3", scriptPath, text.replace("\"", "\\\""), 
                    Double.toString(weight), Integer.toString(promptType));
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            process.waitFor();
            return "Summary - \n" + output.toString().trim();
        } catch (Exception e) {
            return "Error generating summary: " + e.getMessage();
        }
    }
    
}


