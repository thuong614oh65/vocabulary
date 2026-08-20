// =========================================================
// XỬ LÝ TRANG NGHE ĐIỀN CÂU (DICTATION)
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
            if (btnTaoBai) btnTaoBai.disabled = true;
        });
    }

    // Nếu có dữ liệu từ AI -> Parse và render danh sách 15 câu
    if (dataRawCauNgheDien && dataRawCauNgheDien.textContent.trim()) {
        const rawText = dataRawCauNgheDien.textContent.trim();
        parseVaRenderDanhSachCau(rawText);
    }

    // =========================================================
    // PARSE VÀ RENDER DANH SÁCH CÂU
    // Format mẫu: [CÂU:1:English sentence:Nghĩa tiếng Việt]
    // =========================================================
    function parseVaRenderDanhSachCau(text) {
        const regex = /\[CÂU:(\d+):([^:]+):?([^\]]*)\]/g;
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
                const cleanLine = line.replace(/^\d+[\.\-\)]\s*/, "").trim();
                if (cleanLine.length > 5) {
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

            card.innerHTML = `
                <div class="sentence-header">
                    <div class="sentence-number">
                        <span>#${cau.num}</span>
                    </div>

                    <div class="audio-controls">
                        <button type="button" class="btn btn-audio btn-audio-normal" onclick="phatAmCau('${cau.english.replace(/'/g, "\\'")}', 1.0)">
                            🔊 Nghe chuẩn (1.0x)
                        </button>
                        <button type="button" class="btn btn-audio btn-audio-slow" onclick="phatAmCau('${cau.english.replace(/'/g, "\\'")}', 0.75)">
                            🐢 Nghe chậm (0.75x)
                        </button>
                        ${cau.meaning ? `<button type="button" class="btn btn-hint-toggle" onclick="toggleGoiY(${cau.num})">💡 Gợi ý</button>` : ''}
                    </div>
                </div>

                ${cau.meaning ? `<div class="hint-box" id="hint-box-${cau.num}">📖 <strong>Nghĩa:</strong> ${cau.meaning}</div>` : ''}

                <div class="input-wrapper">
                    <input type="text" 
                           class="input-sentence" 
                           data-num="${cau.num}" 
                           placeholder="Gõ lại câu bạn nghe được..." 
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
                }, 500);
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
            .replace(/[.,\/#!$%\^&\*;:{}=\-_`~()?"']/g, "")
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
            const rawUser = input.value || "";

            const cleanCorrect = chuanHoaCau(rawCorrect);
            const cleanUser = chuanHoaCau(rawUser);

            card.classList.remove("correct", "wrong");
            feedbackBox.style.display = "block";

            if (cleanUser.length > 0 && cleanUser === cleanCorrect) {
                card.classList.add("correct");
                input.disabled = true;
                feedbackBox.innerHTML = `
                    <div class="feedback-correct">
                        ✅ Chính xác!
                    </div>
                `;
                soCauDung++;
            } else {
                card.classList.add("wrong");
                feedbackBox.innerHTML = `
                    <div class="feedback-wrong-details">
                        <div class="correct-text">✨ Câu đúng: <strong>${rawCorrect}</strong></div>
                        <div class="meaning-text">Câu bạn nhập: <span class="text-danger">${rawUser || "(chưa nhập)"}</span></div>
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
                resultComment.textContent = "Đừng lo lắng! Hãy bấm nghe lại câu mẫu và chép lại để nâng cao kỹ năng nghe.";
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
            });

            if (resultBox) resultBox.style.display = "none";
            if (btnLamLai) btnLamLai.style.display = "none";

            const firstInput = document.querySelector(".input-sentence");
            if (firstInput) firstInput.focus();
        });
    }

});

// =========================================================
// PHÁT ÂM CÂU BẰNG WEB SPEECH API
// =========================================================
function phatAmCau(cau, tocDo) {
    if (!cau) return;

    if (window.speechSynthesis) {
        window.speechSynthesis.cancel();
        let utterance = new SpeechSynthesisUtterance(cau);
        utterance.lang = "en-US";
        utterance.rate = tocDo || 1.0;
        window.speechSynthesis.speak(utterance);
    }
}

// Ẩn / hiện gợi ý nghĩa tiếng Việt
function toggleGoiY(num) {
    const hintBox = document.getElementById("hint-box-" + num);
    if (hintBox) {
        hintBox.style.display = (hintBox.style.display === "none" || hintBox.style.display === "") ? "block" : "none";
    }
}

window.phatAmCau = phatAmCau;
window.toggleGoiY = toggleGoiY;
