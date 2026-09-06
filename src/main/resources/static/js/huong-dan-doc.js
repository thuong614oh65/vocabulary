/**
 * HƯỚNG DẪN CÁCH ĐỌC CHUẨN (GOOGLE DỊCH & ELSA SPEAK STYLE)
 * Độc lập, an toàn tuyệt đối, không can thiệp code cũ.
 */
(function () {
    let hdAudioHienTai = null;
    let hdTimeoutList = [];
    let hangDangChon = null;
    let duLieuHienTai = null;
    let recognition = null;
    let dangThuAm = false;

    // ---------------------------------------------------------
    // DỪNG TẤT CẢ ÂM THANH TRONG MODAL & TOÀN TRANG
    // ---------------------------------------------------------
    function dungAudioHuongDan() {
        // Dừng âm thanh của trang chính (nếu có)
        if (typeof window.dungTatCaAmThanh === "function") {
            try { window.dungTatCaAmThanh(); } catch (e) {}
        }
        if (typeof window.soLanDoc !== "undefined") {
            window.soLanDoc++;
        }

        // Dừng audio nội bộ của modal
        if (hdAudioHienTai) {
            try {
                hdAudioHienTai.pause();
                hdAudioHienTai.currentTime = 0;
            } catch (e) {}
            hdAudioHienTai = null;
        }

        // Dừng SpeechSynthesis
        if (window.speechSynthesis) {
            try { window.speechSynthesis.cancel(); } catch (e) {}
        }

        // Xóa các timeout đang chờ (cho đánh vần / tách âm)
        hdTimeoutList.forEach(function (t) { clearTimeout(t); });
        hdTimeoutList = [];

        // Gỡ class playing ở các nút
        document.querySelectorAll(".btn-hd-audio").forEach(function (btn) {
            btn.classList.remove("playing");
        });
    }

    // ---------------------------------------------------------
    // MỞ MODAL HƯỚNG DẪN ĐỌC
    // ---------------------------------------------------------
    window.moHuongDanDoc = function (btnElement, tu, phienAm, nghia) {
        if (!tu) return;

        // Lưu dòng bảng hiện tại để khi đóng modal có thể tự focus ô nhập
        if (btnElement) {
            hangDangChon = btnElement.closest("tr");
        }

        dungAudioHuongDan();

        const modal = document.getElementById("modalHuongDanDoc");
        const loadingBox = document.getElementById("hdLoadingBox");
        const contentBox = document.getElementById("hdContentBox");

        if (!modal) return;

        modal.classList.add("show");
        if (loadingBox) loadingBox.style.display = "block";
        if (contentBox) contentBox.style.display = "none";

        // Tự động phát âm 1 lần tốc độ chuẩn ngay khi mở modal (như Google Dịch)
        phatAudioTu(tu, 1.0);

        // Gọi API lấy dữ liệu phân tích ngữ âm
        const params = new URLSearchParams({
            tu: tu,
            phienAm: phienAm || "",
            nghia: nghia || ""
        });

        fetch("/api/huong-dan-doc?" + params.toString())
            .then(function (res) {
                if (!res.ok) throw new Error("Lỗi mạng khi tải hướng dẫn");
                return res.json();
            })
            .then(function (data) {
                duLieuHienTai = data;
                hienThiDuLieuModal(data);
                if (loadingBox) loadingBox.style.display = "none";
                if (contentBox) contentBox.style.display = "flex";
            })
            .catch(function (err) {
                console.warn("[HuongDanDoc] Dùng dữ liệu dự phòng:", err);
                const fallbackData = taoDuLieuDuPhong(tu, phienAm, nghia);
                duLieuHienTai = fallbackData;
                hienThiDuLieuModal(fallbackData);
                if (loadingBox) loadingBox.style.display = "none";
                if (contentBox) contentBox.style.display = "flex";
            });
    };

    // ---------------------------------------------------------
    // ĐÓNG MODAL
    // ---------------------------------------------------------
    window.dongHuongDanDoc = function (tiepTucHoc) {
        dungAudioHuongDan();

        if (recognition && dangThuAm) {
            try { recognition.stop(); } catch (e) {}
            dangThuAm = false;
        }

        const modal = document.getElementById("modalHuongDanDoc");
        if (modal) {
            modal.classList.remove("show");
        }

        // Nếu bấm "Quay lại tiếp tục học tiếp" -> focus và cuộn vào ô nhập
        if (tiepTucHoc && hangDangChon) {
            const input = hangDangChon.querySelector(".cau-tra-loi");
            if (input) {
                setTimeout(function () {
                    input.focus();
                    input.select();
                    if (typeof window.cuonVaoGiuaManHinh === "function") {
                        window.cuonVaoGiuaManHinh(input);
                    }
                }, 100);
            }
        }
    };

    // ---------------------------------------------------------
    // RENDER NỘI DUNG VÀO MODAL
    // ---------------------------------------------------------
    function hienThiDuLieuModal(data) {
        // Từ chính & Nghĩa
        const elTu = document.getElementById("hdTuChinh");
        const elIpa = document.getElementById("hdPhienAmIpa");
        const elVn = document.getElementById("hdPhienAmTiengViet");
        const elNghia = document.getElementById("hdNghia");

        if (elTu) elTu.textContent = data.tu || "";
        if (elIpa) elIpa.textContent = data.phienAm ? (data.phienAm.startsWith("/") ? data.phienAm : "/" + data.phienAm + "/") : "";
        if (elVn) elVn.textContent = "🇻🇳 " + (data.phienAmTiengViet || data.tu);
        if (elNghia) elNghia.textContent = data.nghia ? "(" + data.nghia + ")" : "";

        // Tách âm tiết (Syllables)
        const chipContainer = document.getElementById("hdSyllableChips");
        if (chipContainer) {
            chipContainer.innerHTML = "";
            const amTiet = data.amTiet || [data.tu];
            const amTietIpa = data.amTietIpa || [];
            const amTietBoi = data.amTietBoi || [];
            const amNhan = typeof data.amNhanIndex === "number" ? data.amNhanIndex : -1;

            amTiet.forEach(function (syllable, idx) {
                const chip = document.createElement("div");
                chip.className = "hd-syllable-chip" + (idx === amNhan ? " stressed" : "");
                chip.title = "Bấm để nghe âm tiết: " + syllable;

                let ipaText = amTietIpa[idx] ? "/" + amTietIpa[idx] + "/" : "";
                let boiText = amTietBoi[idx] || "";

                chip.innerHTML = 
                    '<span class="syllable-stress-tag">⭐ Trọng âm</span>' +
                    '<span class="syllable-en">' + syllable + '</span>' +
                    (boiText ? '<span class="syllable-vn">' + boiText + '</span>' : '') +
                    (ipaText ? '<span class="syllable-ipa">' + ipaText + '</span>' : '');

                chip.addEventListener("click", function () {
                    dungAudioHuongDan();
                    docAmTiet(syllable);
                });

                chipContainer.appendChild(chip);
            });
        }

        // Hướng dẫn khẩu hình
        const elKhauHinh = document.getElementById("hdTipKhauHinh");
        if (elKhauHinh) {
            elKhauHinh.textContent = data.khauHinh || "Mở khẩu hình thoải mái, đặt lưỡi tự nhiên và phát âm rõ ràng.";
        }

        // Âm đuôi (Ending sound)
        const boxAmDuoi = document.getElementById("hdCardAmDuoi");
        const elAmDuoi = document.getElementById("hdTipAmDuoi");
        if (data.amDuoi && data.amDuoi.trim()) {
            if (boxAmDuoi) boxAmDuoi.style.display = "block";
            if (elAmDuoi) elAmDuoi.textContent = data.amDuoi;
        } else {
            if (boxAmDuoi) boxAmDuoi.style.display = "none";
        }

        // Lỗi thường gặp
        const elLoi = document.getElementById("hdTipLoi");
        if (elLoi) {
            elLoi.textContent = data.loiThuongGap || "Chú ý nhấn đúng trọng âm và phát âm đầy đủ các âm tiết.";
        }

        // Reset trạng thái thu âm luyện đọc
        const micBtn = document.getElementById("btnMicPractice");
        const micResult = document.getElementById("hdSpeechResult");
        if (micBtn) {
            micBtn.classList.remove("listening");
            micBtn.innerHTML = "🎙️ Bấm để thử phát âm";
        }
        if (micResult) {
            micResult.innerHTML = "";
        }
    }

    // ---------------------------------------------------------
    // PHÁT AUDIO (CHUẨN 1.0x HOẶC CHẬM 0.6x)
    // ---------------------------------------------------------
    window.phatAudioHuongDan = function (tocDo, btnEl) {
        if (!duLieuHienTai || !duLieuHienTai.tu) return;
        dungAudioHuongDan();

        if (btnEl) btnEl.classList.add("playing");

        phatAudioTu(duLieuHienTai.tu, tocDo, function () {
            if (btnEl) btnEl.classList.remove("playing");
        });
    };

    function phatAudioTu(tu, tocDo, onEnd) {
        const tenFile = tu.toLowerCase().replace(/\s+/g, "-") + ".mp3";
        const urlMp3 = "/audio/tu-vung/" + encodeURIComponent(tenFile);

        const audio = new Audio(urlMp3);
        audio.playbackRate = tocDo || 1.0;
        hdAudioHienTai = audio;

        let daChuyenFallback = false;

        function fallbackTTS() {
            if (daChuyenFallback) return;
            daChuyenFallback = true;

            if (window.speechSynthesis) {
                const utterance = new SpeechSynthesisUtterance(tu);
                utterance.lang = "en-US";
                utterance.rate = tocDo || 1.0;
                utterance.onend = function () {
                    if (onEnd) onEnd();
                };
                utterance.onerror = function () {
                    if (onEnd) onEnd();
                };
                window.speechSynthesis.speak(utterance);
            } else {
                if (onEnd) onEnd();
            }
        }

        audio.onended = function () {
            hdAudioHienTai = null;
            if (onEnd) onEnd();
        };

        audio.onerror = function () {
            console.warn("[HuongDanDoc] Không tải được mp3, chuyển sang Web Speech API:", urlMp3);
            fallbackTTS();
        };

        const playPromise = audio.play();
        if (playPromise !== undefined) {
            playPromise.catch(function (err) {
                console.warn("[HuongDanDoc] Audio play error:", err);
                fallbackTTS();
            });
        }
    }

    // ---------------------------------------------------------
    // ĐỌC TỪNG ÂM TIẾT
    // ---------------------------------------------------------
    function docAmTiet(amTiet) {
        if (!amTiet || !window.speechSynthesis) return;
        const utterance = new SpeechSynthesisUtterance(amTiet);
        utterance.lang = "en-US";
        utterance.rate = 0.75;
        window.speechSynthesis.speak(utterance);
    }

    // ---------------------------------------------------------
    // ĐÁNH VẦN TỪNG CHỮ CÁI (SPELLING)
    // ---------------------------------------------------------
    window.danhVanHuongDan = function (btnEl) {
        if (!duLieuHienTai || !duLieuHienTai.tu) return;
        dungAudioHuongDan();

        if (btnEl) btnEl.classList.add("playing");

        const letters = duLieuHienTai.tu.replace(/[^a-zA-Z]/g, "").split("");
        if (letters.length === 0) {
            if (btnEl) btnEl.classList.remove("playing");
            return;
        }

        let idx = 0;
        function docChuTiep() {
            if (idx >= letters.length) {
                if (btnEl) btnEl.classList.remove("playing");
                return;
            }
            const char = letters[idx];
            idx++;

            if (window.speechSynthesis) {
                const utterance = new SpeechSynthesisUtterance(char);
                utterance.lang = "en-US";
                utterance.rate = 0.9;
                utterance.onend = function () {
                    const t = setTimeout(docChuTiep, 300);
                    hdTimeoutList.push(t);
                };
                utterance.onerror = function () {
                    const t = setTimeout(docChuTiep, 300);
                    hdTimeoutList.push(t);
                };
                window.speechSynthesis.speak(utterance);
            } else {
                const t = setTimeout(docChuTiep, 400);
                hdTimeoutList.push(t);
            }
        }

        docChuTiep();
    };

    // ---------------------------------------------------------
    // ĐỌC TÁCH ÂM TIẾT TUẦN TỰ
    // ---------------------------------------------------------
    window.docTachAmHuongDan = function (btnEl) {
        if (!duLieuHienTai || !duLieuHienTai.tu) return;
        dungAudioHuongDan();

        if (btnEl) btnEl.classList.add("playing");

        const amTietList = duLieuHienTai.amTiet && duLieuHienTai.amTiet.length > 0 
            ? duLieuHienTai.amTiet 
            : [duLieuHienTai.tu];

        let idx = 0;
        function docAmTiep() {
            if (idx >= amTietList.length) {
                // Sau khi đọc hết các âm tiết -> đọc lại cả từ
                const t = setTimeout(function () {
                    phatAudioTu(duLieuHienTai.tu, 0.9, function () {
                        if (btnEl) btnEl.classList.remove("playing");
                    });
                }, 400);
                hdTimeoutList.push(t);
                return;
            }

            const syllable = amTietList[idx];
            idx++;

            if (window.speechSynthesis) {
                const utterance = new SpeechSynthesisUtterance(syllable);
                utterance.lang = "en-US";
                utterance.rate = 0.7;
                utterance.onend = function () {
                    const t = setTimeout(docAmTiep, 450);
                    hdTimeoutList.push(t);
                };
                utterance.onerror = function () {
                    const t = setTimeout(docAmTiep, 450);
                    hdTimeoutList.push(t);
                };
                window.speechSynthesis.speak(utterance);
            } else {
                const t = setTimeout(docAmTiep, 600);
                hdTimeoutList.push(t);
            }
        }

        docAmTiep();
    };

    // ---------------------------------------------------------
    // LUYỆN NÓI / NHẬN DIỆN GIỌNG NÓI (WEB SPEECH API)
    // ---------------------------------------------------------
    window.batDauLuyenDoc = function () {
        const micBtn = document.getElementById("btnMicPractice");
        const micResult = document.getElementById("hdSpeechResult");
        if (!micBtn || !micResult || !duLieuHienTai) return;

        const SpeechRec = window.SpeechRecognition || window.webkitSpeechRecognition;
        if (!SpeechRec) {
            micResult.innerHTML = '<span style="color: #ea580c;">Trình duyệt của bạn chưa hỗ trợ nhận diện giọng nói (khuyên dùng Google Chrome).</span>';
            return;
        }

        dungAudioHuongDan();

        if (dangThuAm && recognition) {
            recognition.stop();
            return;
        }

        try {
            recognition = new SpeechRec();
            recognition.lang = "en-US";
            recognition.interimResults = false;
            recognition.maxAlternatives = 3;

            recognition.onstart = function () {
                dangThuAm = true;
                micBtn.classList.add("listening");
                micBtn.innerHTML = "🔴 Đang nghe... Hãy nói to!";
                micResult.innerHTML = '<span style="color: #0284c7;">Đang nghe phát âm của bạn...</span>';
            };

            recognition.onresult = function (event) {
                dangThuAm = false;
                micBtn.classList.remove("listening");
                micBtn.innerHTML = "🎙️ Thử lại lần nữa";

                let recognized = "";
                if (event.results && event.results[0] && event.results[0][0]) {
                    recognized = event.results[0][0].transcript.trim().toLowerCase();
                }

                const target = duLieuHienTai.tu.trim().toLowerCase();
                // Bỏ dấu câu nếu có
                const cleanRecognized = recognized.replace(/[.,\/#!$%\^&\*;:{}=\-_`~()]/g, "");

                if (cleanRecognized.includes(target) || target.includes(cleanRecognized)) {
                    micResult.innerHTML = 
                        '<span style="color: #16a34a; font-weight: 700;">' +
                        '🎉 Xuất sắc! Phát âm chuẩn: "<b>' + recognized + '</b>"' +
                        '</span>';
                } else {
                    micResult.innerHTML = 
                        '<span style="color: #dc2626;">' +
                        'Bạn vừa phát âm: "<b>' + recognized + '</b>". Hãy nghe lại âm chuẩn và thử lại nhé!' +
                        '</span>';
                }
            };

            recognition.onerror = function (e) {
                dangThuAm = false;
                micBtn.classList.remove("listening");
                micBtn.innerHTML = "🎙️ Bấm để thử phát âm";
                if (e.error !== "no-speech") {
                    micResult.innerHTML = '<span style="color: #64748b;">Không nhận được giọng nói. Bấm mic và thử lại nhé!</span>';
                }
            };

            recognition.onend = function () {
                dangThuAm = false;
                micBtn.classList.remove("listening");
                if (micBtn.innerHTML.includes("Đang nghe")) {
                    micBtn.innerHTML = "🎙️ Bấm để thử phát âm";
                }
            };

            recognition.start();
        } catch (err) {
            console.error("[HuongDanDoc] Speech recognition error:", err);
            micBtn.classList.remove("listening");
            micBtn.innerHTML = "🎙️ Bấm để thử phát âm";
        }
    };

    // ---------------------------------------------------------
    // DỮ LIỆU DỰ PHÒNG CHUẨN HÓA TIẾNG VIỆT
    // ---------------------------------------------------------
    function taoDuLieuDuPhong(tu, phienAm, nghia) {
        return {
            tu: tu,
            phienAm: phienAm || "",
            nghia: nghia || "",
            amTiet: [tu],
            amTietIpa: [phienAm || ""],
            amTietBoi: [tu],
            amNhanIndex: 0,
            phienAmTiengViet: tu,
            trongAm: "Nhấn âm 1",
            khauHinh: "Mở miệng tự nhiên, thả lỏng môi và phát âm rõ âm.",
            amDuoi: "",
            loiThuongGap: "Chú ý đọc trọn vẹn từ và nhấn đúng trọng âm.",
            meoGhiNho: ""
        };
    }

    // ---------------------------------------------------------
    // SỰ KIỆN PHÍM & CLICK NGOÀI MODAL
    // ---------------------------------------------------------
    document.addEventListener("DOMContentLoaded", function () {
        const modal = document.getElementById("modalHuongDanDoc");
        if (!modal) return;

        // Click vào nền mờ để đóng modal
        modal.addEventListener("click", function (e) {
            if (e.target === modal) {
                dongHuongDanDoc(false);
            }
        });

        // Nhấn phím ESC để đóng modal
        document.addEventListener("keydown", function (e) {
            if (e.key === "Escape" && modal.classList.contains("show")) {
                dongHuongDanDoc(false);
            }
        });
    });
})();
