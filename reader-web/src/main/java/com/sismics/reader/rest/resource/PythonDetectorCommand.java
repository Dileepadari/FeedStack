package com.sismics.reader.rest.resource;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

public class PythonDetectorCommand implements DetectorCommand {
    @Override
    public String execute(List<String> articleIds, List<String> titles, List<String> descriptions,double threshold) {
        try {
            String articleIdsJson = new Gson().toJson(articleIds);
            String titlesJson = new Gson().toJson(titles);
            String descriptionsJson = new Gson().toJson(descriptions);
            String scriptPath = System.getProperty("user.dir") + "/dupe_detection.py";

            ProcessBuilder pb = new ProcessBuilder("python3", scriptPath, articleIdsJson, titlesJson, descriptionsJson,Double.toString(threshold));
            pb.environment().put("PYTHONIOENCODING", "utf-8");
            pb.redirectErrorStream(true);

            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }

            int exitCode = process.waitFor();
            return (exitCode == 0) ? output.toString() : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
