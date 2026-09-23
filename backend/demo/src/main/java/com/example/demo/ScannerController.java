                                        package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ScannerController {

    @Autowired
    private ScanHistoryRepository scanHistoryRepository;

    @PostMapping("/analyze")
    public Map<String, Object> analyzeUrl(@RequestBody Map<String, String> request) {
        String input = request.get("url");
        Map<String, Object> response = new HashMap<>();
        List<String> reasons = new ArrayList<>();

        if (input == null || input.trim().isEmpty()) {
            response.put("score", 0);
            response.put("status", "Invalid");
            response.put("color", "gray");
            reasons.add("Empty input provided.");
            response.put("reasons", reasons);
            return response;
        }

        // Basic URL check: must start with http:// or https:// or contain a valid domain pattern
        boolean isUrl = input.toLowerCase().startsWith("http://") || 
                        input.toLowerCase().startsWith("https://") || 
                        input.contains(".");

        if (!isUrl) {
            // Handle Plain Text QR Codes
            response.put("score", 0);
            response.put("status", "Plain Text");
            response.put("color", "blue");
            reasons.add("This QR code contains plain text, not a web URL.");
            response.put("reasons", reasons);

            ScanHistory historyRecord = new ScanHistory(input, 0, "Plain Text", "Text QR Code");
            scanHistoryRepository.save(historyRecord);

            return response;
        }

        // URL Security Analysis
        int score = 0;

        // 1. HTTPS Check
        if (!input.toLowerCase().startsWith("https://")) {
            score += 20;
            reasons.add("Missing HTTPS (+20)");
        }

        // 2. Suspicious Keywords
        String lower = input.toLowerCase();
        if (lower.contains("login") || lower.contains("verify") || lower.contains("bank") || lower.contains("update")) {
            score += 25;
            reasons.add("Contains sensitive/phishing keywords (+25)");
        }

        // 3. IP Address instead of Domain
        if (input.matches(".*\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}.*")) {
            score += 30;
            reasons.add("Uses raw IP address instead of domain (+30)");
        }

        // Determine Risk Status
        String status = "Safe";
        String color = "green";

        if (score >= 60) {
            status = "Malicious";
            color = "red";
        } else if (score >= 30) {
            status = "Suspicious";
            color = "orange";
        }

        // Save Record
        ScanHistory historyRecord = new ScanHistory(
            input,
            score,
            status,
            String.join(", ", reasons)
        );
        scanHistoryRepository.save(historyRecord);

        response.put("score", score);
        response.put("status", status);
        response.put("color", color);
        response.put("reasons", reasons);
        return response;
    }

    @GetMapping("/history")
    public List<ScanHistory> getHistory() {
        return scanHistoryRepository.findAll();
    }
}
