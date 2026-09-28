package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class ScannerController {

    @Autowired
    private ScanHistoryRepository historyRepository;

    @PostMapping("/analyze")
    public ScanResponse analyzeUrl(@RequestBody ScanRequest request) {
        String rawUrl = request.getUrl();
        String url = (rawUrl != null) ? rawUrl.trim().toLowerCase() : "";
        
        int score = 0;
        List<String> reasons = new ArrayList<>();

        // 1. Check for Direct IP Address host (e.g., http://185.220.101.5)
        if (url.matches(".*://\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}.*")) {
            score += 40;
            reasons.add("Uses raw IP address instead of domain name.");
        }

        // 2. Check for Executable / Harmful file extensions
        if (url.endsWith(".exe") || url.endsWith(".scr") || url.endsWith(".bat") || url.contains(".exe?") || url.contains(".apk")) {
            score += 50;
            reasons.add("Direct link to an executable file download.");
        }

        // 3. Check for Phishing / Security Keywords
        String[] suspiciousKeywords = {"login", "verify", "account", "paypal", "bank", "update", "security", "warning", "alert", "malware", "paypal-security"};
        for (String kw : suspiciousKeywords) {
            if (url.contains(kw)) {
                score += 20;
                reasons.add("Contains high-risk security/phishing keyword: " + kw);
            }
        }

        // 4. Check for HTTP vs HTTPS
        if (url.startsWith("http://")) {
            score += 10;
            reasons.add("Insecure HTTP protocol used.");
        }

        // Cap the score at 100
        if (score > 100) score = 100;

        // Determine Risk Status
        String status;
        if (score >= 70) {
            status = "Malicious";
        } else if (score >= 40) {
            status = "Suspicious";
        } else {
            status = "Safe";
        }

        // Save entry to H2 Database
        ScanHistory history = new ScanHistory();
        history.setUrl(rawUrl);
        history.setScannedBy(request.getScannedBy());
        history.setRiskScore(score);
        history.setRiskLevel(status);
        historyRepository.save(history);

        return new ScanResponse(status, score, reasons);
    }

    @GetMapping("/history")
    public List<ScanHistory> getHistory() {
        return historyRepository.findAll();
    }
}

// Request and Response DTO Classes
class ScanRequest {
    private String url;
    private String scannedBy;

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getScannedBy() { return scannedBy; }
    public void setScannedBy(String scannedBy) { this.scannedBy = scannedBy; }
}

class ScanResponse {
    private String status;
    private int score;
    private List<String> reasons;

    public ScanResponse(String status, int score, List<String> reasons) {
        this.status = status;
        this.score = score;
        this.reasons = reasons;
    }

    public String getStatus() { return status; }
    public int getScore() { return score; }
    public List<String> getReasons() { return reasons; }
}