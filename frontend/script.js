const codeReader = new ZXing.BrowserQRCodeReader();

async function analyzeQR() {
    const fileInput = document.getElementById('qrImage') || document.getElementById('qr-file');
    const resultSection = document.getElementById('result-section');
    const resultContent = document.getElementById('resultContent');

    if (!fileInput || !fileInput.files || fileInput.files.length === 0) {
        alert('Please select a QR code image first.');
        return;
    }

    if (resultSection) resultSection.classList.remove('hidden');
    if (resultContent) resultContent.innerHTML = '<p>Analyzing QR code...</p>';

    const file = fileInput.files[0];
    const reader = new FileReader();

    reader.onload = async function (e) {
        const img = new Image();
        img.onload = async function () {
            try {
                // ZXing decodes directly from the HTML Image element
                const result = await codeReader.decodeFromImageElement(img);
                const scannedUrl = result.getText();

                // Check localStorage key matching auth section ('currentUser' or fallback 'username')
                const currentUser = localStorage.getItem('currentUser') || localStorage.getItem('username') || 'smit123';

                // Send decoded text & logged-in username to Spring Boot Backend
                const response = await fetch('http://localhost:8080/api/analyze', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ 
                        url: scannedUrl,
                        scannedBy: currentUser
                    })
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

                if (resultContent) {
                    resultContent.innerHTML = `
                        <div class="result-box">
                            <p class="result-item"><strong>Content:</strong> ${scannedUrl}</p>
                            <p class="result-item"><strong>Risk Score:</strong> ${data.score !== undefined ? data.score : (data.riskScore || 0)}/100</p>
                            <p class="result-item"><strong>STATUS:</strong> <span class="badge ${badgeClass}">${(data.status || 'SAFE').toUpperCase()}</span></p>
                            ${warningsHtml}
                        </div>
                    `;
                }

                // Reset file selection
                fileInput.value = '';

                // Refresh history using available handler
                if (typeof loadLogs === 'function') {
                    await loadLogs();
                } else if (typeof fetchHistory === 'function') {
                    await fetchHistory();
                }

            } catch (err) {
                console.error(err);
                if (resultContent) {
                    resultContent.innerHTML = '<p style="color: red;">Could not decode QR code. Please ensure the QR image is clear, not heavily distorted, and cropped close to the code.</p>';
                }
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
        if (!response.ok) return;

        const data = await response.json();

        if (data.length === 0) {
            historyList.innerHTML = '<p style="margin-top: 10px;">No previous scans found.</p>';
            return;
        }

        let html = '';
        data.forEach(item => {
            const score = item.score !== undefined ? item.score : (item.riskScore || 0);
            html += `
                <div class="history-item">
                    <p><strong>Scanned By:</strong> ${item.scannedBy || 'Anonymous'}</p>
                    <p><strong>URL:</strong> ${item.url}</p>
                    <p><strong>Score:</strong> ${score}% | <strong>Status:</strong> ${item.riskLevel || item.status || 'Safe'}</p>
                </div>
            `;
        });

        historyList.innerHTML = html;
    } catch (error) {
        console.error('Error fetching history:', error);
        historyList.innerHTML = '<p style="color:red; margin-top: 10px;">Failed to load scan history.</p>';
    }
}

window.onload = () => {
    if (typeof loadLogs === 'function') {
        loadLogs();
    } else {
        fetchHistory();
    }
};