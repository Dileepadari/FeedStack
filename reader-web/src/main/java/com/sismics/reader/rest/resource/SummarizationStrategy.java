package com.sismics.reader.rest.resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

interface SummarizationStrategy {
    Logger log = LoggerFactory.getLogger(SummarizationStrategy.class);

    /** A summariser that cannot answer must not stall the request thread. */
    int TIMEOUT_SECONDS = 60;

    /** Shown to the reader when no summary could be produced. */
    String UNAVAILABLE = "Summary unavailable.";

    String summarize(String text, double weight);

    /**
     * Runs summarizer.py and returns what it printed on stdout.
     *
     * Two things here are deliberate, because both used to be wrong.
     *
     * stderr is kept separate rather than merged into stdout. It used to be
     * merged and the whole stream returned as the summary, so a Python
     * traceback, a pip transcript and the absolute path of the script on the
     * server were all rendered into the article a reader was looking at. Only
     * stdout is the summary; stderr goes to the log.
     *
     * The exit code is checked. A failed run returns a fixed message rather
     * than whatever the process happened to emit, so nothing about the server
     * reaches the browser.
     */
    default String callPythonSummarizer(String text, double weight, int promptType) {
        Process process = null;
        try {
            String scriptPath = System.getProperty("user.dir") + "/summarizer.py";
            ProcessBuilder pb = new ProcessBuilder("python3", scriptPath, text,
                    Double.toString(weight), Integer.toString(promptType));
            process = pb.start();

            String stdout = drain(process.getInputStream());
            String stderr = drain(process.getErrorStream());

            if (!process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                log.warn("Summariser timed out after {}s", TIMEOUT_SECONDS);
                return UNAVAILABLE;
            }

            if (process.exitValue() != 0 || stdout.trim().isEmpty()) {
                log.warn("Summariser exited {}: {}", process.exitValue(), stderr.trim());
                return UNAVAILABLE;
            }

            return "Summary - \n" + stdout.trim();
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.warn("Could not run the summariser", e);
            return UNAVAILABLE;
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }

    /** Static, not private: this targets Java 8, where interfaces cannot have
     *  private methods. */
    static String drain(InputStream stream) throws IOException {
        StringBuilder out = new StringBuilder();
        try (BufferedReader reader =
                new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line).append('\n');
            }
        }
        return out.toString();
    }
}
