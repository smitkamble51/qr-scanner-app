package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Allows request from your frontend
public class ScannerController {

    @Autowired
    private ScanHistoryRepository scanHistoryRepository;

    @PostMapping("/analyze")
    public ScanResponse analyzeUrl(@RequestBody ScanRequest request) {
        // Read the scannedBy field sent from the frontend
        String user = (request.getScannedBy() != null && !request.getScannedBy().isEmpty()) 
                      ? request.getScannedBy() 
                      : "Anonymous";

        // Calculate risk score and status (simplified example logic)
        int score = 0;
        String status = "Safe";
        
        if (request.getUrl().startsWith("http://")) {
            score = 20;
        }

        // Save scan history with the scannedBy user
        ScanHistory log = new ScanHistory();
        log.setScannedBy(user);
        log.setUrl(request.getUrl());
        log.setRiskLevel(status + " (" + score + "%)");
        log.setRiskScore(score);
        
        scanHistoryRepository.save(log);

        return new ScanResponse(status, score, List.of("Analyzed URL successfully"));
    }

    @GetMapping("/history")
    public List<ScanHistory> getHistory() {
        return scanHistoryRepository.findAll();
    }
}

// DTO class to parse incoming JSON payload
class ScanRequest {
    private String url;
    private String scannedBy;

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getScannedBy() { return scannedBy; }
    public void setScannedBy(String scannedBy) { this.scannedBy = scannedBy; }
}

// DTO class for sending analysis response
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