package com.example.demo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "scan_history")
public class ScanHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String url;
    private int riskScore;
    private String riskLevel;
    private String warnings;
    private LocalDateTime scanDate = LocalDateTime.now();

    public ScanHistory() {}

    public ScanHistory(String url, int riskScore, String riskLevel, String warnings) {
        this.url = url;
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.warnings = warnings;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public String getWarnings() { return warnings; }
    public void setWarnings(String warnings) { this.warnings = warnings; }
    public LocalDateTime getScanDate() { return scanDate; }
}
