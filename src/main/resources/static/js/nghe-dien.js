// =========================================================
// XỬ LÝ TRANG NGHE ĐIỀN CÂU (DICTATION) - 2 CẤP ĐỘ + GỢI Ý
// =========================================================

document.addEventListener("DOMContentLoaded", function () {

    const formTaoBaiNghe = document.getElementById("formTaoBaiNghe");
    const btnTaoBai = document.getElementById("btnTaoBai");
    const loadingBox = document.getElementById("loadingBox");
    const dataRawCauNgheDien = document.getElementById("dataRawCauNgheDien");
    const dictationList = document.getElementById("dictationList");
    const tongSoCauText = document.getElementById("tongSoCauText");
    const btnChamDiem = document.getElementById("btnChamDiem");
    const btnLamLai = document.getElementById("btnLamLai");
    const resultBox = document.getElementById("resultBox");
    const resultScore = document.getElementById("resultScore");
    const resultComment = document.getElementById("resultComment");
    const resultIcon = document.getElementById("resultIcon");

    // Loading khi bấm tạo bài
    if (formTaoBaiNghe) {
        formTaoBaiNghe.addEventListener("submit", function () {
            if (loadingBox) loadingBox.style.display = "block";
            if (btnTaoBai) {
                btnTaoBai.disabled = true;
                btnTaoBai.innerHTML = "⏳ Đang tạo bài...";
            }
        });
    }

    // Nếu có dữ liệu từ AI -> Parse và render danh sách các câu
    if (dataRawCauNgheDien && dataRawCauNgheDien.textContent.trim()) {
        const rawText = dataRawCauNgheDien.textContent.trim();
        parseVaRenderDanhSachCau(rawText);
    }

    // =========================================================
    // PARSE VÀ RENDER DANH SÁCH CÂU
    // Hỗ trợ format: [CAU:1:English sentence:Nghĩa tiếng Việt]
    // =========================================================
    function parseVaRenderDanhSachCau(text) {
        // Khớp cả [CAU:...], [CÂU:...], [cau:...], [câu:...]
        const regex = /\[(?:CAU|CÂU|cau|câu):(\d+):([^:]+):?([^\]]*)\]/gi;
        let dsCau = [];
        let match;
        let index = 0;

        while ((match = regex.exec(text)) !== null) {
            index++;
            dsCau.push({
                num: match[1] || index,
                english: match[2].trim(),
                meaning: match[3] ? match[3].trim() : ""
            });
        }

        // Fallback nếu format dạng dòng thường:
        if (dsCau.length === 0) {
            const lines = text.split("\n");
            lines.forEach(function (line) {
                const trimmed = line.trim();
                if (!trimmed) return;

                // Thử tách theo dấu : hoặc -
                const colonIdx = trimmed.indexOf(":");
                if (colonIdx > 0 && !trimmed.startsWith("http")) {
                    const left = trimmed.substring(0, colonIdx)
                        .replace(/^\d+[\.\-\)]\s*/, "")
                        .replace(/^\[(?:CAU|CÂU|cau|câu):?\d*\]?\s*/i, "")
                        .trim();
                    const right = trimmed.substring(colonIdx + 1).replace(/\]$/, "").trim();
                    if (left.length > 2) {
                        index++;
                        dsCau.push({
                            num: index,
                            english: left,
                            meaning: right
                        });
                        return;
                    }
                }

                const cleanLine = trimmed.replace(/^\d+[\.\-\)]\s*/, "").trim();
                if (cleanLine.length > 3) {
                    index++;
                    dsCau.push({
                        num: index,
                        english: cleanLine,
                        meaning: ""
                    });
                }
            });
        }

        if (tongSoCauText) {
            tongSoCauText.textContent = dsCau.length;
        }

        if (!dictationList) return;
        dictationList.innerHTML = "";

        dsCau.forEach(function (cau) {
            const card = document.createElement("div");
            card.className = "sentence-card";
            card.id = "card-cau-" + cau.num;
            card.dataset.num = cau.num;
            card.dataset.answer = cau.english;
            card.dataset.meaning = cau.meaning || "";

            card.innerHTML = `
                <div class="sentence-header">
                    <div class="sentence-number">
                        <span>#${cau.num}</span>
                    </div>

                    <div class="audio-controls">
                        <button type="button" class="btn btn-audio btn-audio-normal" onclick="phatAmCau('${cau.english.replace(/'/g, "\\'")}', 1.0)" title="Nghe với tốc độ chuẩn">
                            🔊 Nghe chuẩn (1.0x)
                        </button>
                        <button type="button" class="btn btn-audio btn-audio-slow" onclick="phatAmCau('${cau.english.replace(/'/g, "\\'")}', 0.75)" title="Nghe với tốc độ chậm">
                            🐢 Nghe chậm (0.75x)
                        </button>
                        <button type="button" class="btn btn-hint-toggle" id="btn-hint-${cau.num}" onclick="toggleGoiY(${cau.num})" title="Xem nghĩa tiếng Việt của câu này">
                            💡 Gợi ý nghĩa
                        </button>
                    </div>
                </div>

                <!-- Khung gợi ý nghĩa tiếng Việt -->
                <div class="hint-box" id="hint-box-${cau.num}" style="display: none;">
                    <div class="hint-inner">
                        <span class="hint-badge">💡 Gợi ý nghĩa câu:</span>
                        <span class="hint-text">${cau.meaning ? cau.meaning : '(Đang nghe và viết lại câu theo nghĩa ngữ cảnh)'}</span>
                    </div>
                </div>

                <div class="input-wrapper">
                    <input type="text" 
                           class="input-sentence" 
                           data-num="${cau.num}" 
                           placeholder="Gõ lại câu tiếng Anh bạn nghe được (bấm Enter để sang câu tiếp)..." 
                           autocomplete="off" 
                           spellcheck="false" />
                </div>

                <div class="feedback-box" id="feedback-${cau.num}">
                    <!-- Sẽ hiển thị khi bấm chấm điểm -->
                </div>
            `;

            dictationList.appendChild(card);
        });

        // Xử lý Enter tự động chuyển câu tiếp theo
        const inputs = document.querySelectorAll(".input-sentence");
        inputs.forEach(function (input, i) {
            input.addEventListener("keydown", function (e) {
                if (e.key === "Enter") {
                    e.preventDefault();
                    if (inputs[i + 1]) {
                        inputs[i + 1].focus();
                        // Tự động phát âm câu tiếp theo khi nhảy tới
                        const nextCard = inputs[i + 1].closest(".sentence-card");
                        if (nextCard) {
                            phatAmCau(nextCard.dataset.answer, 1.0);
                        }
                    } else if (btnChamDiem) {
                        btnChamDiem.focus();
                    }
                }
            });
        });

        // Focus vào câu 1 và tự phát âm câu 1
        if (inputs.length > 0) {
            inputs[0].focus();
            if (dsCau.length > 0) {
                setTimeout(function () {
                    phatAmCau(dsCau[0].english, 1.0);
                }, 400);
            }
        }
    }

    // =========================================================
    // CHẤM ĐIỂM TẤT CẢ CÁC CÂU
    // =========================================================
    if (btnChamDiem) {
        btnChamDiem.addEventListener("click", function () {
            chamDiemTatCa();
        });
    }

    function chuanHoaCau(str) {
        if (!str) return "";
        return str
            .toLowerCase()
            .replace(/[.,\/#!$%\^&\*;:{}=\-_`~()?"'’]/g, "")
            .replace(/\s+/g, " ")
            .trim();
    }

    function chamDiemTatCa() {
        const cards = document.querySelectorAll(".sentence-card");
        if (cards.length === 0) return;

        let soCauDung = 0;
        let tongSo = cards.length;

        cards.forEach(function (card) {
            const input = card.querySelector(".input-sentence");
            const feedbackBox = card.querySelector(".feedback-box");
            const rawCorrect = card.dataset.answer || "";
            const rawMeaning = card.dataset.meaning || "";
            const rawUser = input ? (input.value || "").trim() : "";

            const cleanCorrect = chuanHoaCau(rawCorrect);
            const cleanUser = chuanHoaCau(rawUser);

            card.classList.remove("correct", "wrong");
            feedbackBox.style.display = "block";

            if (cleanUser.length > 0 && cleanUser === cleanCorrect) {
                card.classList.add("correct");
                if (input) input.disabled = true;
                feedbackBox.innerHTML = `
                    <div class="feedback-correct">
                        ✅ Chính xác! ${rawMeaning ? `<span class="meaning-correct-hint">— <em>${rawMeaning}</em></span>` : ''}
                    </div>
                `;
                soCauDung++;
            } else {
                card.classList.add("wrong");
                feedbackBox.innerHTML = `
                    <div class="feedback-wrong-details">
                        <div class="correct-text">✨ Câu đúng: <strong>${rawCorrect}</strong></div>
                        ${rawMeaning ? `<div class="meaning-text">📖 Nghĩa tiếng Việt: <strong>${rawMeaning}</strong></div>` : ''}
                        <div class="user-text">✍️ Bạn đã nhập: <span class="text-danger fw-semibold">${rawUser || "(chưa nhập)"}</span></div>
                    </div>
                `;
            }
        });

        // Tổng kết kết quả
        const tiLe = Math.round((soCauDung / tongSo) * 100);
        if (resultBox && resultScore && resultComment) {
            resultBox.style.display = "block";
            resultScore.textContent = `Bạn làm đúng ${soCauDung}/${tongSo} câu (${tiLe}%)`;

            resultBox.classList.remove("good", "average", "poor");

            if (tiLe === 100) {
                resultBox.classList.add("good");
                if (resultIcon) resultIcon.textContent = "🏆";
                resultComment.textContent = "Tuyệt đối hoàn hảo! Khả năng nghe và chép của bạn cực kỳ xuất sắc!";
            } else if (tiLe >= 70) {
                resultBox.classList.add("good");
                if (resultIcon) resultIcon.textContent = "🎉";
                resultComment.textContent = "Rất tốt! Tai nghe tiếng Anh của bạn rất nhạy bén.";
            } else if (tiLe >= 40) {
                resultBox.classList.add("average");
                if (resultIcon) resultIcon.textContent = "👍";
                resultComment.textContent = "Khá tốt! Hãy bấm nghe lại các câu sai để quen với cách phát âm nối âm nhé.";
            } else {
                resultBox.classList.add("poor");
                if (resultIcon) resultIcon.textContent = "💪";
                resultComment.textContent = "Đừng lo lắng! Hãy bấm nút 💡 Gợi ý nghĩa và nghe lại nhiều lần để nhớ câu nhé.";
            }

            resultBox.scrollIntoView({ behavior: "smooth", block: "center" });
        }

        if (btnLamLai) {
            btnLamLai.style.display = "inline-block";
        }
    }

    // =========================================================
    // LÀM LẠI BÀI NGHE
    // =========================================================
    if (btnLamLai) {
        btnLamLai.addEventListener("click", function () {
            const cards = document.querySelectorAll(".sentence-card");
            cards.forEach(function (card) {
                card.classList.remove("correct", "wrong");
                const input = card.querySelector(".input-sentence");
                if (input) {
                    input.value = "";
                    input.disabled = false;
                }
                const feedbackBox = card.querySelector(".feedback-box");
                if (feedbackBox) {
                    feedbackBox.style.display = "none";
                    feedbackBox.innerHTML = "";
                }

                // Reset hint box
                const num = card.dataset.num;
                const hintBox = document.getElementById("hint-box-" + num);
                const btnHint = document.getElementById("btn-hint-" + num);
                if (hintBox) hintBox.style.display = "none";
                if (btnHint) {
                    btnHint.classList.remove("active");
                    btnHint.innerHTML = "💡 Gợi ý nghĩa";
                }
            });

            if (resultBox) resultBox.style.display = "none";
            if (btnLamLai) btnLamLai.style.display = "none";

            const firstInput = document.querySelector(".input-sentence");
            if (firstInput) firstInput.focus();
        });
    }

});

let audioDangPhat = null;

// =========================================================
// PHÁT ÂM CÂU (EDGE NEURAL TTS MP3 STREAM - KHÔNG LƯU ĐĨA)
// =========================================================
function phatAmCau(cau, tocDo) {
    if (!cau) return;

    // Dừng tất cả âm thanh đang phát
    if (window.speechSynthesis) {
        window.speechSynthesis.cancel();
    }
    if (audioDangPhat) {
        audioDangPhat.pause();
        audioDangPhat.currentTime = 0;
        audioDangPhat = null;
    }

    const speed = tocDo || 1.0;
    console.log("ĐỌC CÂU:", cau, "(Tốc độ:", speed + "x)");

    // Tốc độ: chuẩn (1.0x) -> +0%, chậm (0.75x) -> -25%
    let rateStr = "+0%";
    if (speed < 0.9) {
        rateStr = "-25%";
    }

    let daFallback = false;
    function fallbackSpeech() {
        if (daFallback) return;
        daFallback = true;
        if (window.speechSynthesis) {
            console.log("Dùng giọng đọc trình duyệt (SpeechSynthesis) cho câu:", cau);
            let utterance = new SpeechSynthesisUtterance(cau);
            utterance.lang = "en-US";
            utterance.rate = speed;
            window.speechSynthesis.speak(utterance);
        }
    }

    fetch("/audio/tts", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            text: cau,
            rate: rateStr
        })
    })
    .then(function (response) {
        if (!response.ok) {
            throw new Error("Lỗi HTTP " + response.status);
        }
        return response.blob();
    })
    .then(function (blob) {
        const audioUrl = URL.createObjectURL(blob);
        audioDangPhat = new Audio(audioUrl);

        audioDangPhat.onplay = function () {
            console.log("BẮT ĐẦU ĐỌC CÂU (MP3 Edge Neural TTS):", cau);
        };

        audioDangPhat.onended = function () {
            console.log("ĐỌC XONG CÂU:", cau);
            URL.revokeObjectURL(audioUrl); // Giải phóng bộ nhớ RAM ngay khi đọc xong
            audioDangPhat = null;
        };

        audioDangPhat.onerror = function (err) {
            console.warn("LỖI PHÁT MP3 CÂU:", err, "- Chuyển sang giọng đọc trình duyệt.");
            URL.revokeObjectURL(audioUrl);
            fallbackSpeech();
        };

        audioDangPhat.play().catch(function (playErr) {
            console.warn("KHÔNG THỂ PHÁT MP3:", playErr, "- Chuyển sang giọng đọc trình duyệt.");
            URL.revokeObjectURL(audioUrl);
            fallbackSpeech();
        });
    })
    .catch(function (err) {
        console.warn("LỖI GỌI API /audio/tts:", err.message, "- Chuyển sang giọng đọc trình duyệt.");
        fallbackSpeech();
    });
}

// =========================================================
// BẬT / TẮT GỢI Ý NGHĨA TIẾNG VIỆT
// =========================================================
function toggleGoiY(num) {
    const hintBox = document.getElementById("hint-box-" + num);
    const btnHint = document.getElementById("btn-hint-" + num);
    if (!hintBox) return;

    const isHidden = (hintBox.style.display === "none" || !hintBox.style.display || getComputedStyle(hintBox).display === "none");
    if (isHidden) {
        hintBox.style.display = "block";
        if (btnHint) {
            btnHint.classList.add("active");
            btnHint.innerHTML = "🙈 Ẩn gợi ý";
        }
    } else {
        hintBox.style.display = "none";
        if (btnHint) {
            btnHint.classList.remove("active");
            btnHint.innerHTML = "💡 Gợi ý nghĩa";
        }
    }
}

window.phatAmCau = phatAmCau;
window.toggleGoiY = toggleGoiY;
