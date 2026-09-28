# 🛡️ QR Code & URL Security Scanner

A full-stack web application designed to analyze QR codes and URLs for phishing threats, malicious file payloads, and security risks. Built with a **Spring Boot** Java backend and a **vanilla JavaScript** frontend, deployed across **Render** and **Vercel**.

---

## ✨ Features

- **⚡ Real-Time URL Analysis:** Evaluates URLs against security heuristics including raw IP detection, dangerous file extensions, insecure HTTP protocol, and high-risk phishing keywords.
- **📊 Dynamic Risk Scoring:** Classifies scanned URLs into **Safe**, **Suspicious**, or **Malicious** threat levels with risk percentages ($0\text{--}100\%$).
- **💾 Audit Log History:** Persists all scan activity and user logs using an in-memory database.
- **🔐 User Authentication:** Secure login and registration endpoint management.
- **🌐 Cloud Deployed:** Fully hosted full-stack architecture running online for free.

---

## 🛠️ Tech Stack

### Frontend
- **HTML5 / CSS3 / JavaScript (ES6+)**
- **Vercel** (Frontend Hosting)

### Backend
- **Java 17 / Spring Boot 3**
- **Spring Data JPA & H2 Database**
- **Maven** (Build Tool)
- **Render** (Backend Hosting)

---

## 📐 Threat Detection Heuristics

The scanner backend inspects incoming URLs for key threat signals:

| Flag | Description | Risk Score Penalty |
| :--- | :--- | :--- |
| **Raw IP Host** | Uses direct IP addresses instead of a domain | `+40%` |
| **Executable Payloads** | Direct links to `.exe`, `.scr`, `.bat`, or `.apk` files | `+50%` |
| **Phishing Keywords** | Contains keywords like `login`, `verify`, `paypal`, `malware`, etc. | `+20%` per keyword |
| **Insecure HTTP** | Uses `http://` instead of encrypted `https://` | `+10%` |

---

## 🚀 Getting Started Locally

### Prerequisites
- **JDK 17** or higher
- **Maven**
- **Git**

### 1. Clone the Repository
```bash
git clone [https://github.com/YOUR_USERNAME/QR-Security-Project.git](https://github.com/YOUR_USERNAME/QR-Security-Project.git)
cd QR-Security-Project
