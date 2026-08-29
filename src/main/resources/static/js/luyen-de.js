// =========================================================================
// LUYỆN ĐỀ TOEIC SPEAKING Q7-9 (INTERACTIONS & AI SCORING)
// =========================================================================

let currentExamData = null;
let prepTimerInterval = null;
let prepSecondsLeft = 45;

document.addEventListener('DOMContentLoaded', () => {
    initTabEvents();
    initTemplateButtons();
    initUploadEvents();
    initSampleTestButtons();
    initLiveMeters();
    initAudioButtons();
    initSubmitButton();
});

// -------------------------------------------------------------
// 1. TƯƠNG TÁC TABS & TEMPLATES
// -------------------------------------------------------------
function initTabEvents() {
    const btnAiAuto = document.getElementById('btnAiAutoGenerate');
    if (btnAiAuto) {
        btnAiAuto.addEventListener('click', () => {
            showLoading('AI đang sáng tạo đề thi mới...', 'Tạo bảng thông tin, tình huống cuộc gọi và 3 câu hỏi...');
            fetch('/api/luyen-de/tao-tu-dong', { method: 'POST' })
                .then(res => res.json())
                .then(data => {
                    hideLoading();
                    if (data.error) {
                        alert('Lỗi: ' + data.error);
                        return;
                    }
                    loadExamIntoPractice(data);
                })
                .catch(err => {
                    hideLoading();
                    alert('Lỗi kết nối: ' + err);
                });
        });
    }

    const btnSubmitText = document.getElementById('btnSubmitText');
    if (btnSubmitText) {
        btnSubmitText.addEventListener('click', () => {
            const text = document.getElementById('customTextInput').value.trim();
            if (!text) {
                alert('Vui lòng nhập nội dung bảng thông tin / lịch trình trước!');
                return;
            }
            showLoading('AI đang phân tích văn bản...', 'Tạo câu hỏi chuẩn TOEIC Speaking Q7-9...');
            fetch('/api/luyen-de/tao-tu-van-ban', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ vanBan: text })
            })
            .then(res => res.json())
            .then(data => {
                hideLoading();
                if (data.error) {
                    alert('Lỗi: ' + data.error);
                    return;
                }
                loadExamIntoPractice(data);
            })
            .catch(err => {
                hideLoading();
                alert('Lỗi: ' + err);
            });
        });
    }
}

function initTemplateButtons() {
    const templates = {
        'hoi-nghi': `Annual Technology & AI Summit
Grand Plaza Convention Center, Hall B
Friday, October 15th

09:00 a.m. - 09:45 a.m. | Keynote: Future of Artificial Intelligence | Dr. Kevin Vance
09:45 a.m. - 10:30 a.m. | Speech: Cloud Computing in Business | Sarah Jenkins
10:30 a.m. - 11:30 a.m. | Workshop: Machine Learning for Beginners (Postponed to 2:00 p.m.)
11:30 a.m. - 01:00 p.m. | Lunch Break (Buffet included in registration)
01:00 p.m. - 02:30 p.m. | Panel Discussion: Cyber Security Strategies | Alex Turner & Lisa Wong
02:30 p.m. - 04:00 p.m. | Product Showcase & Networking | All Guest Exhibitors`,

        'tour': `Southeast Island Exploration Itinerary
Departure: Central Pier, Gate 4
Date: Saturday, August 20th

08:00 a.m. | Departure by Express Catamaran
09:30 a.m. - 11:30 a.m. | Coral Reef Snorkeling & Scuba Diving (Instructor: Captain David)
12:00 p.m. - 01:30 p.m. | Seafood BBQ Lunch at Coconut Bay
01:45 p.m. - 03:15 p.m. | Tropical Rain Forest Trekking & Bird Watching
03:30 p.m. - 04:30 p.m. | Souvenir Shopping & Local Craft Village
05:00 p.m. | Return ferry departs back to Central Pier`,

        'cv': `Candidate Profile: Jessica Miller
Position Applied: Senior Marketing Specialist
Contact: jessica.m@email.com | (555) 382-9102

Education:
- Master of Marketing Management, Boston University (2018)
- Bachelor of Business Administration, New York University (2015)

Work Experience:
- Marketing Lead : Apex Digital Solutions (2020 - Present)
  * Managed $2M digital ad budgets, grew organic traffic by 140%
- Social Media Coordinator : Bright Media Group (2018 - 2020)

Skills & Certifications:
- Google Ads & Analytics Certified, SEO/SEM Specialist
- Fluent in English & French`
    };

    document.querySelectorAll('.btn-template').forEach(btn => {
        btn.addEventListener('click', () => {
            const type = btn.getAttribute('data-type');
            if (templates[type]) {
                document.getElementById('customTextInput').value = templates[type];
            }
        });
    });
}

// -------------------------------------------------------------
// 2. TẢI ẢNH LÊN (DROPZONE)
// -------------------------------------------------------------
function initUploadEvents() {
    const dropzone = document.getElementById('uploadDropzone');
    const fileInput = document.getElementById('examImageInput');
    const previewContainer = document.getElementById('imagePreviewContainer');
    const previewImg = document.getElementById('imagePreview');
    const btnSubmitUpload = document.getElementById('btnSubmitUpload');

    if (!dropzone || !fileInput) return;

    ['dragenter', 'dragover'].forEach(eventName => {
        dropzone.addEventListener(eventName, (e) => {
            e.preventDefault();
            dropzone.classList.add('dragover');
        });
    });

    ['dragleave', 'drop'].forEach(eventName => {
        dropzone.addEventListener(eventName, (e) => {
            e.preventDefault();
            dropzone.classList.remove('dragover');
        });
    });

    dropzone.addEventListener('drop', (e) => {
        const files = e.dataTransfer.files;
        if (files.length > 0) {
            handleImageFile(files[0]);
        }
    });

    fileInput.addEventListener('change', (e) => {
        if (fileInput.files.length > 0) {
            handleImageFile(fileInput.files[0]);
        }
    });

    function handleImageFile(file) {
        if (!file.type.startsWith('image/')) {
            alert('Vui lòng chọn một file ảnh hợp lệ!');
            return;
        }
        const reader = new FileReader();
        reader.onload = (e) => {
            previewImg.src = e.target.result;
            previewContainer.classList.remove('d-none');
            previewContainer.scrollIntoView({ behavior: 'smooth' });
        };
        reader.readAsDataURL(file);
    }

    if (btnSubmitUpload) {
        btnSubmitUpload.addEventListener('click', () => {
            const file = fileInput.files[0];
            if (!file) {
                alert('Vui lòng chọn ảnh trước!');
                return;
            }
            const formData = new FormData();
            formData.append('file', file);

            showLoading('AI đang nhận diện hình ảnh (OCR)...', 'Trích xuất dữ liệu bảng và tạo câu hỏi TOEIC Q7-9...');
            fetch('/api/luyen-de/tao-tu-anh', {
                method: 'POST',
                body: formData
            })
            .then(res => res.json())
            .then(data => {
                hideLoading();
                if (data.error) {
                    alert('Lỗi: ' + data.error);
                    return;
                }
                loadExamIntoPractice(data);
            })
            .catch(err => {
                hideLoading();
                alert('Lỗi: ' + err);
            });
        });
    }
}

// -------------------------------------------------------------
// 3. 15 ĐỀ MẪU TỪ đề thi.docx
// -------------------------------------------------------------
function initSampleTestButtons() {
    document.querySelectorAll('.sample-test-card').forEach(card => {
        card.addEventListener('click', () => {
            const id = card.getAttribute('data-id');
            loadSampleTest(id);
        });
    });
}

function loadSampleTest(id) {
    showLoading(`Đang tải Đề thi mẫu #${id}...`, 'Gemini AI đang phân tích dữ liệu bảng và tạo câu hỏi...');
    fetch(`/api/luyen-de/de-mau/${id}`)
        .then(res => res.json())
        .then(data => {
            hideLoading();
            if (data.error) {
                alert('Lỗi: ' + data.error);
                return;
            }
            loadExamIntoPractice(data);
        })
        .catch(err => {
            hideLoading();
            alert('Lỗi kết nối: ' + err);
        });
}

// -------------------------------------------------------------
// 4. LOAD ĐỀ VÀO KHU VỰC LÀM BÀI
// -------------------------------------------------------------
function loadExamIntoPractice(data) {
    currentExamData = data;

    // Ẩn khu vực chọn đề, hiện khu vực làm bài
    document.getElementById('modeSelectionSection').style.display = 'none';
    document.getElementById('practiceArena').style.display = 'block';
    document.getElementById('resultSection').style.display = 'none';

    // Đặt tiêu đề
    document.getElementById('examTitleDisplay').textContent = data.tieuDe || 'Mẫu thông tin đề thi';

    // Hiển thị Mẫu thông tin (Ảnh hoặc Text)
    const imgDisplay = document.getElementById('examImageDisplay');
    const textDisplay = document.getElementById('examTextDisplay');
    const textSummary = document.getElementById('examTextSummary');

    if (data.loaiNoiDung === 'IMAGE' && data.anhUrl) {
        imgDisplay.src = data.anhUrl;
        imgDisplay.classList.remove('d-none');
        textDisplay.classList.add('d-none');
        
        // Hiển thị chữ trích xuất từ ảnh
        if (data.tomTatNoiDung || data.vanBanThongTin) {
            textSummary.innerHTML = (data.tomTatNoiDung || data.vanBanThongTin);
            textSummary.classList.remove('d-none');
        } else {
            textSummary.classList.add('d-none');
        }
    } else {
        imgDisplay.classList.add('d-none');
        textSummary.classList.add('d-none');
        textDisplay.innerHTML = data.vanBanThongTin || '<p>Không có văn bản.</p>';
        textDisplay.classList.remove('d-none');
    }

    // Tình huống cuộc gọi
    document.getElementById('scenarioText').textContent = data.tinhHuong || 'You are answering an inquiry.';

    // 3 Câu hỏi
    document.getElementById('q1Text').textContent = data.cauHoi1 || 'Question 1';
    document.getElementById('q2Text').textContent = data.cauHoi2 || 'Question 2';
    document.getElementById('q3Text').textContent = data.cauHoi3 || 'Question 3';

    // Reset các ô nhập
    ['ans1Input', 'ans2Input', 'ans3Input'].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.value = '';
    });
    updateLiveMeter(1, '');
    updateLiveMeter(2, '');
    updateLiveMeter(3, '');

    // Reset và bắt đầu đếm 45s đọc đề
    resetPrepTimer();

    // Nút đổi đề khác
    document.getElementById('btnChangeExam').onclick = () => {
        if (confirm('Bạn có muốn quay lại chọn đề khác không?')) {
            document.getElementById('modeSelectionSection').style.display = 'block';
            document.getElementById('practiceArena').style.display = 'none';
            document.getElementById('resultSection').style.display = 'none';
            clearInterval(prepTimerInterval);
        }
    };

    window.scrollTo({ top: 0, behavior: 'smooth' });
}

// -------------------------------------------------------------
// 5. BỘ ĐẾM 45s ĐỌC ĐỀ
// -------------------------------------------------------------
function resetPrepTimer() {
    clearInterval(prepTimerInterval);
    prepSecondsLeft = 45;
    const badge = document.getElementById('prepTimer');
    const btn = document.getElementById('btnStartPrepTimer');
    badge.textContent = '45 giây';
    btn.textContent = 'Bắt đầu đếm';

    btn.onclick = () => {
        if (btn.textContent === 'Tạm dừng') {
            clearInterval(prepTimerInterval);
            btn.textContent = 'Tiếp tục';
            return;
        }
        btn.textContent = 'Tạm dừng';
        prepTimerInterval = setInterval(() => {
            prepSecondsLeft--;
            if (prepSecondsLeft <= 0) {
                clearInterval(prepTimerInterval);
                badge.textContent = 'Hết 45s đọc đề!';
                btn.textContent = 'Bắt đầu lại';
                speakText('Preparation time is now over. Please begin answering Question 1.');
            } else {
                badge.textContent = prepSecondsLeft + ' giây';
            }
        }, 1000);
    };
}

// -------------------------------------------------------------
// 6. BỘ ĐẾM SỐ TỪ & THỜI GIAN NÓI REAL-TIME
// -------------------------------------------------------------
function initLiveMeters() {
    [1, 2, 3].forEach(idx => {
        const input = document.getElementById(`ans${idx}Input`);
        if (input) {
            input.addEventListener('input', () => {
                updateLiveMeter(idx, input.value);
            });
        }
    });
}

function updateLiveMeter(idx, text) {
    const words = text.trim() ? text.trim().split(/\s+/).filter(w => w.length > 0) : [];
    const wordCount = words.length;
    // Tốc độ nói trung bình tiếng Anh ~ 2.2 từ / giây (130 wpm)
    const spokenSecs = Math.round(wordCount / 2.2);

    document.getElementById(`wordCount${idx}`).textContent = wordCount;
    document.getElementById(`timeEst${idx}`).textContent = spokenSecs;

    const statusBadge = document.getElementById(`meterStatus${idx}`);
    const isQ3 = (idx === 3);

    if (wordCount === 0) {
        statusBadge.textContent = 'Chưa nhập';
        statusBadge.className = 'meter-status';
        return;
    }

    if (!isQ3) {
        // Câu 1 & 2 (15s quy định, chuẩn 20-35 từ)
        if (wordCount < 12) {
            statusBadge.textContent = '⚠️ Hơi ngắn (<12 từ)';
            statusBadge.className = 'meter-status warning';
        } else if (wordCount <= 38) {
            statusBadge.textContent = '✅ Tốc độ vừa vặn (~10-15s)';
            statusBadge.className = 'meter-status good';
        } else {
            statusBadge.textContent = '⚠️ Quá dài (>38 từ - Nguy cơ bị ngắt lời)';
            statusBadge.className = 'meter-status danger';
        }
    } else {
        // Câu 3 (30s quy định, chuẩn 45-75 từ)
        if (wordCount < 30) {
            statusBadge.textContent = '⚠️ Hơi ngắn cho câu 30s';
            statusBadge.className = 'meter-status warning';
        } else if (wordCount <= 80) {
            statusBadge.textContent = '✅ Dung lượng lý tưởng (~20-28s)';
            statusBadge.className = 'meter-status good';
        } else {
            statusBadge.textContent = '⚠️ Quá dài (>80 từ - Sẽ bị ngắt lời)';
            statusBadge.className = 'meter-status danger';
        }
    }
}

// -------------------------------------------------------------
// 7. PHÁT ÂM THANH CÂU HỎI & TÌNH HUỐNG (TTS WEB SPEECH)
// -------------------------------------------------------------
function initAudioButtons() {
    const btnPlayScenario = document.getElementById('btnPlayScenario');
    if (btnPlayScenario) {
        btnPlayScenario.addEventListener('click', () => {
            if (currentExamData && currentExamData.tinhHuong) {
                speakText(currentExamData.tinhHuong);
            }
        });
    }

    document.querySelectorAll('.btn-play-q').forEach(btn => {
        btn.addEventListener('click', () => {
            const target = btn.getAttribute('data-target');
            if (currentExamData) {
                if (target === 'q1' && currentExamData.cauHoi1) speakText(currentExamData.cauHoi1);
                if (target === 'q2' && currentExamData.cauHoi2) speakText(currentExamData.cauHoi2);
                if (target === 'q3' && currentExamData.cauHoi3) speakText(currentExamData.cauHoi3);
            }
        });
    });
}

function speakText(text) {
    if (!('speechSynthesis' in window)) {
        alert('Trình duyệt của bạn không hỗ trợ Text-to-Speech.');
        return;
    }
    window.speechSynthesis.cancel();
    const cleanText = text.replace(/\(.*?\)/g, '').trim();
    const utterance = new SpeechSynthesisUtterance(cleanText);
    utterance.lang = 'en-US';
    utterance.rate = 0.95; // Tốc độ tự nhiên bản xứ
    window.speechSynthesis.speak(utterance);
}

// -------------------------------------------------------------
// 8. NỘP BÀI & CHẤM ĐIỂM BẰNG GEMINI AI
// -------------------------------------------------------------
function initSubmitButton() {
    const btnSubmit = document.getElementById('btnSubmitAnswers');
    if (!btnSubmit) return;

    btnSubmit.addEventListener('click', () => {
        const a1 = document.getElementById('ans1Input').value.trim();
        const a2 = document.getElementById('ans2Input').value.trim();
        const a3 = document.getElementById('ans3Input').value.trim();

        if (!a1 && !a2 && !a3) {
            alert('Vui lòng nhập câu trả lời cho ít nhất một câu trước khi chấm điểm!');
            return;
        }

        const payload = {
            tieuDe: currentExamData.tieuDe || 'Đề thi TOEIC Speaking',
            loaiNoiDung: currentExamData.loaiNoiDung || 'IMAGE',
            thongTinDeBai: currentExamData.vanBanThongTin || currentExamData.tieuDe,
            tinhHuong: currentExamData.tinhHuong || '',
            cauHoi1: currentExamData.cauHoi1 || '',
            cauTraLoi1: a1,
            cauHoi2: currentExamData.cauHoi2 || '',
            cauTraLoi2: a2,
            cauHoi3: currentExamData.cauHoi3 || '',
            cauTraLoi3: a3
        };

        showLoading('Gemini AI đang chấm điểm bài thi...', 'Đánh giá độ chính xác thông tin, tính toán thời gian nói thực tế và kiểm tra ngữ pháp...');

        fetch('/api/luyen-de/cham-diem', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        })
        .then(res => res.json())
        .then(data => {
            hideLoading();
            if (data.error) {
                alert('Lỗi chấm điểm: ' + data.error);
                return;
            }
            renderEvaluationResults(data);
        })
        .catch(err => {
            hideLoading();
            alert('Lỗi kết nối: ' + err);
        });
    });
}

// -------------------------------------------------------------
// 9. HIỂN THỊ KẾT QUẢ CHẤM ĐIỂM
// -------------------------------------------------------------
function renderEvaluationResults(res) {
    document.getElementById('practiceArena').style.display = 'none';
    const resultSec = document.getElementById('resultSection');
    resultSec.style.display = 'block';

    // Tổng điểm & Xếp loại
    document.getElementById('totalScoreNum').textContent = res.tongDiem != null ? res.tongDiem : '-';
    document.getElementById('xepLoaiBadge').textContent = (res.xepLoai || 'Hoàn thành').toUpperCase();
    document.getElementById('nhanXetTongQuanText').textContent = res.nhanXetTongQuan || 'Bạn đã hoàn thành bài thi TOEIC Speaking Q7-9.';

    const container = document.getElementById('questionResultsContainer');
    container.innerHTML = '';

    const questions = [
        { qText: currentExamData.cauHoi1, ans: document.getElementById('ans1Input').value.trim(), time: 15 },
        { qText: currentExamData.cauHoi2, ans: document.getElementById('ans2Input').value.trim(), time: 15 },
        { qText: currentExamData.cauHoi3, ans: document.getElementById('ans3Input').value.trim(), time: 30 }
    ];

    if (res.danhSachCauHoi && res.danhSachCauHoi.length > 0) {
        res.danhSachCauHoi.forEach((item, idx) => {
            const qInfo = questions[idx] || { qText: `Câu ${idx+1}`, ans: '', time: 15 };
            const card = document.createElement('div');
            card.className = 'result-q-card';

            const scoreBadgeColor = item.diem === 3 ? 'bg-success' : (item.diem === 2 ? 'bg-primary' : 'bg-danger');

            card.innerHTML = `
                <div class="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
                    <h4 class="h5 fw-bold text-slate-800 mb-0">Câu ${item.soThuTu || (idx+1)} (${qInfo.time} giây)</h4>
                    <div class="d-flex gap-2 align-items-center">
                        <span class="badge ${scoreBadgeColor} px-3 py-2 rounded-pill fw-bold fs-6">
                            ⭐ Điểm: ${item.diem != null ? item.diem : 0} / 3
                        </span>
                    </div>
                </div>

                <div class="p-3 bg-light rounded-3 mb-3 border">
                    <div class="fw-bold text-slate-700 mb-1">❓ Câu hỏi:</div>
                    <div class="text-slate-800 fw-semibold">${qInfo.qText}</div>
                </div>

                <div class="p-3 rounded-3 mb-3 border ${qInfo.ans ? 'bg-white' : 'bg-light'}">
                    <div class="fw-bold text-slate-700 mb-1">✍️ Câu trả lời của bạn:</div>
                    <div class="text-slate-800">${qInfo.ans ? qInfo.ans : '<em class="text-muted">Không trả lời</em>'}</div>
                    <div class="mt-2 small text-muted">
                        📊 Dung lượng: <strong>${item.soTu || 0} từ</strong> • ⏱️ Thời gian nói ước tính: <strong>~${item.thoiGianNoiUocTinh || 0}s</strong>
                    </div>
                </div>

                <div class="row g-3 mb-3">
                    <div class="col-md-6">
                        <div class="p-3 bg-light rounded-3 h-100 border">
                            <div class="fw-bold text-slate-700 mb-1">📋 Đánh giá thông tin:</div>
                            <div class="text-slate-800 small">${item.danhGiaThongTin || 'Chính xác'}</div>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <div class="p-3 bg-light rounded-3 h-100 border">
                            <div class="fw-bold text-slate-700 mb-1">⏱️ Đánh giá tốc độ & thời gian nói:</div>
                            <div class="text-slate-800 small">${item.danhGiaThoiGian || 'Vừa vặn'}</div>
                        </div>
                    </div>
                </div>

                ${item.nhanXetChiTiet ? `
                    <div class="p-3 bg-light rounded-3 mb-3 border">
                        <div class="fw-bold text-slate-700 mb-1">🔍 Nhận xét chi tiết (Ngữ pháp / Giới từ / Giọng điệu):</div>
                        <div class="text-slate-800 small" style="white-space: pre-line;">${item.nhanXetChiTiet}</div>
                    </div>
                ` : ''}

                <!-- Model Spoken Answer -->
                <div class="model-answer-box">
                    <div class="d-flex justify-content-between align-items-center mb-2">
                        <span class="fw-bold text-success">🌟 Câu trả lời mẫu chuẩn bản xứ (Đạt điểm tối đa):</span>
                        <button class="btn btn-sm btn-outline-success rounded-pill px-3" onclick="speakText('${(item.cauTraLoiMau || '').replace(/'/g, "\\'")}')">
                            🔊 Nghe đọc mẫu
                        </button>
                    </div>
                    <div class="fw-semibold text-slate-900 mb-1">${item.cauTraLoiMau || ''}</div>
                    ${item.dichTiengVietMau ? `<div class="small text-muted fst-italic">Dịch nghĩa: ${item.dichTiengVietMau}</div>` : ''}
                </div>
            `;
            container.appendChild(card);
        });
    }

    resultSec.scrollIntoView({ behavior: 'smooth' });
}

// -------------------------------------------------------------
// LOADING HELPERS
// -------------------------------------------------------------
function showLoading(title, subtitle) {
    document.getElementById('loadingTitle').textContent = title || 'Đang xử lý...';
    document.getElementById('loadingSubtitle').textContent = subtitle || 'Vui lòng chờ giây lát';
    document.getElementById('loadingOverlay').style.display = 'flex';
}

function hideLoading() {
    document.getElementById('loadingOverlay').style.display = 'none';
}

// -------------------------------------------------------------
// XỬ LÝ BÔI ĐEN DỊCH NGHĨA (TOOLTIP TRANSLATION)
// -------------------------------------------------------------
document.addEventListener('DOMContentLoaded', () => {
    const tooltip = document.getElementById('translateTooltip');
    const btnTranslate = document.getElementById('btnTranslateTooltip');
    const resultBox = document.getElementById('translateResultBox');
    const loading = document.getElementById('translateLoading');
    const content = document.getElementById('translateContent');
    let selectedText = "";

    // Lắng nghe sự kiện bôi đen (mouseup)
    document.addEventListener('mouseup', (e) => {
        if (!tooltip) return;
        // Nếu click vào bên trong tooltip thì không làm gì cả
        if (tooltip.contains(e.target)) return;

        // Bỏ qua nếu đang click vào các input, textarea để tránh xung đột
        if (e.target.tagName === 'TEXTAREA' || e.target.tagName === 'INPUT') {
            tooltip.classList.add('d-none');
            resultBox.classList.add('d-none');
            selectedText = "";
            return;
        }

        // Lấy chữ được bôi đen
        let text = window.getSelection().toString().trim();
        if (text.length > 0 && text.length < 500) { // Giới hạn không dịch đoạn quá dài
            selectedText = text;
            
            // Tính toán vị trí hiển thị tooltip (ngay dưới đoạn bôi đen)
            const range = window.getSelection().getRangeAt(0);
            const rect = range.getBoundingClientRect();
            
            // Căn tooltip nằm ngay dưới văn bản được bôi đen
            tooltip.style.left = `${Math.max(10, rect.left + window.scrollX + (rect.width/2) - 30)}px`;
            tooltip.style.top = `${rect.bottom + window.scrollY + 10}px`;
            
            // Reset trạng thái tooltip
            resultBox.classList.add('d-none');
            btnTranslate.classList.remove('d-none');
            tooltip.classList.remove('d-none');
        } else {
            // Ẩn tooltip nếu click ra ngoài hoặc không có text
            tooltip.classList.add('d-none');
            resultBox.classList.add('d-none');
            selectedText = "";
        }
    });

    // Khi bấm nút Dịch
    if (btnTranslate) {
        btnTranslate.addEventListener('click', () => {
            if (!selectedText) return;
            
            btnTranslate.classList.add('d-none');
            resultBox.classList.remove('d-none');
            loading.classList.remove('d-none');
            content.innerHTML = "";

            fetch('/api/tra-tu/dich', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    text: selectedText,
                    mode: 'AUTO'
                })
            })
            .then(res => res.json())
            .then(data => {
                loading.classList.add('d-none');
                if (data.thanhCong && data.banDich) {
                    // Hiển thị kết quả dịch
                    let html = `<div class="fw-bold text-primary mb-1">${data.tuGoc || selectedText}</div>`;
                    if (data.phienAm) html += `<div class="text-muted small mb-1">${data.phienAm}</div>`;
                    html += `<div class="fw-semibold">${data.banDich}</div>`;
                    
                    if (data.giaiThich) {
                        html += `<div class="mt-2 text-muted small" style="white-space: pre-line;">${data.giaiThich}</div>`;
                    }
                    content.innerHTML = html;
                } else {
                    content.innerHTML = `<div class="text-danger">${data.thongBaoLoi || 'Không thể dịch đoạn văn này.'}</div>`;
                }
            })
            .catch(err => {
                loading.classList.add('d-none');
                content.innerHTML = `<div class="text-danger">Lỗi kết nối.</div>`;
            });
        });
    }
});
