const codeReader = new ZXing.BrowserQRCodeReader();

async function analyzeQR() {
    const fileInput = document.getElementById('qrImage');
    const resultSection = document.getElementById('result-section');
    const resultContent = document.getElementById('resultContent');

    if (!fileInput.files || fileInput.files.length === 0) {
        alert('Please select a QR code image first.');
        return;
    }

    resultSection.classList.remove('hidden');
    resultContent.innerHTML = '<p>Analyzing QR code...</p>';

    const file = fileInput.files[0];
    const reader = new FileReader();

    reader.onload = async function (e) {
        const img = new Image();
        img.onload = async function () {
            try {
                // ZXing decodes directly from the HTML Image element
                const result = await codeReader.decodeFromImageElement(img);
                const scannedUrl = result.getText();

                // Send decoded text to Spring Boot Backend
                const response = await fetch('http://localhost:8080/api/analyze', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ url: scannedUrl })
                });

                const data = await response.json();

                let badgeClass = 'badge-safe';
                if (data.status === 'Suspicious') badgeClass = 'badge-suspicious';
                if (data.status === 'Malicious') badgeClass = 'badge-malicious';
                if (data.status === 'Plain Text') badgeClass = 'badge-secondary';

                let warningsHtml = '';
                if (data.reasons && data.reasons.length > 0) {
                    warningsHtml = `<p style="margin-top: 8px;"><strong>Details:</strong></p><ul>` + 
                        data.reasons.map(r => `<li>ℹ️ ${r}</li>`).join('') + `</ul>`;
                }

                resultContent.innerHTML = `
                    <div class="result-box">
                        <p class="result-item"><strong>Content:</strong> ${scannedUrl}</p>
                        <p class="result-item"><strong>Risk Score:</strong> ${data.score}/100</p>
                        <p class="result-item"><strong>STATUS:</strong> <span class="badge ${badgeClass}">${data.status.toUpperCase()}</span></p>
                        ${warningsHtml}
                    </div>
                `;

                fetchHistory();

            } catch (err) {
                console.error(err);
                resultContent.innerHTML = '<p style="color: red;">Could not decode QR code. Please ensure the QR image is clear, not heavily distorted, and cropped close to the code.</p>';
            }
        };
        img.src = e.target.result;
    };

    reader.readAsDataURL(file);
}

async function fetchHistory() {
    const historyList = document.getElementById('historyList');
    if (!historyList) return;

    try {
        const response = await fetch('http://localhost:8080/api/history');
        const data = await response.json();

        if (data.length === 0) {
            historyList.innerHTML = '<p style="margin-top: 10px;">No previous scans found.</p>';
            return;
        }

        let html = '';
        data.forEach(item => {
            html += `
                <div class="history-item">
                    <p><strong>URL:</strong> ${item.url}</p>
                    <p><strong>Score:</strong> ${item.riskScore} | <strong>Status:</strong> ${item.riskLevel}</p>
                </div>
            `;
        });

        historyList.innerHTML = html;
    } catch (error) {
        console.error('Error fetching history:', error);
        historyList.innerHTML = '<p style="color:red; margin-top: 10px;">Failed to load scan history.</p>';
    }
}

window.onload = fetchHistory;
