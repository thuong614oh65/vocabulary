/**
 * HƯỚNG DẪN CÁCH ĐỌC CHUẨN (GOOGLE DỊCH & ELSA SPEAK STYLE)
 * Quản lý âm thanh tập trung: Đảm bảo toàn trang CHỈ CÓ 1 ÂM THANH DUY NHẤT.
 * Bật bất kỳ âm thanh nào mới sẽ lập tức ngắt âm thanh cũ đang phát.
 */
(function () {
    let hdAudioHienTai = null;
    let hdTimeoutList = [];
    let hangDangChon = null;
    let duLieuHienTai = null;
    let recognition = null;
    let dangThuAm = false;

    // =========================================================
    // 1. BỘ ĐIỀU PHỐI ÂM THANH TOÀN TRANG (MASTER AUDIO CONTROLLER)
    // Bắt và quản lý mọi âm thanh HTML5 Audio và SpeechSynthesis
    // =========================================================
    let audioDangPhatToanCuc = null;

    // Hook HTMLMediaElement.prototype.play (bắt tất cả new Audio().play() trên toàn trang)
    const playGoc = HTMLMediaElement.prototype.play;
    HTMLMediaElement.prototype.play = function () {
        // 1. Dừng SpeechSynthesis nếu đang phát
        if (window.speechSynthesis && (window.speechSynthesis.speaking || window.speechSynthesis.pending)) {
            try { window.speechSynthesis.cancel(); } catch (e) {}
        }

        // 2. Dừng bất kỳ Audio nào khác đang phát trước đó
        if (audioDangPhatToanCuc && audioDangPhatToanCuc !== this) {
            try {
                audioDangPhatToanCuc.pause();
                audioDangPhatToanCuc.currentTime = 0;
            } catch (e) {}
        }

        // 3. Đánh dấu audio hiện tại
        audioDangPhatToanCuc = this;

        const self = this;
        const xoaAudio = function () {
            if (audioDangPhatToanCuc === self) {
                audioDangPhatToanCuc = null;
            }
        };
        this.addEventListener("ended", xoaAudio, { once: true });
        this.addEventListener("pause", xoaAudio, { once: true });

        return playGoc.apply(this, arguments);
    };

    // Hook window.speechSynthesis.speak (bắt tất cả giọng đọc trình duyệt)
    if (window.speechSynthesis) {
        const speakGoc = window.speechSynthesis.speak.bind(window.speechSynthesis);
        window.speechSynthesis.speak = function (utterance) {
            // 1. Dừng bất kỳ Audio nào đang phát
            if (audioDangPhatToanCuc) {
                try {
                    audioDangPhatToanCuc.pause();
                    audioDangPhatToanCuc.currentTime = 0;
                } catch (e) {}
                audioDangPhatToanCuc = null;
            }

            // 2. Dừng lượt SpeechSynthesis trước đó
            try { window.speechSynthesis.cancel(); } catch (e) {}

            return speakGoc(utterance);
        };
    }

    // =========================================================
    // 2. DỪNG TẤT CẢ ÂM THANH TOÀN BỘ TRANG VÀ MODAL
    // =========================================================
    function dungAudioHuongDan() {
        // 1. Hủy lượt đọc & đánh vần bên hoc-chon.js (nếu có)
        if (typeof window.huyDocHocChon === "function") {
            try { window.huyDocHocChon(); } catch (e) {}
        } else if (typeof window.dungTatCaAmThanh === "function") {
            try { window.dungTatCaAmThanh(); } catch (e) {}
        }

        // 2. Dừng audio toàn cục
        if (audioDangPhatToanCuc) {
            try {
                audioDangPhatToanCuc.pause();
                audioDangPhatToanCuc.currentTime = 0;
            } catch (e) {}
                audioDangPhatToanCuc = null;
        }

        // 3. Dừng audio nội bộ của modal
        if (hdAudioHienTai) {
            try {
                hdAudioHienTai.pause();
                hdAudioHienTai.currentTime = 0;
            } catch (e) {}
            hdAudioHienTai = null;
        }

        // 4. Dừng SpeechSynthesis
        if (window.speechSynthesis) {
            try { window.speechSynthesis.cancel(); } catch (e) {}
        }

        // 5. Xóa các timeout đang chờ (cho đánh vần / tách âm)
        hdTimeoutList.forEach(function (t) { clearTimeout(t); });
        hdTimeoutList = [];

        // 6. Gỡ class playing ở các nút
        document.querySelectorAll(".btn-hd-audio").forEach(function (btn) {
            btn.classList.remove("playing");
        });

        // 7. Gỡ class active-playing ở các thẻ âm tiết
        document.querySelectorAll(".hd-syllable-chip").forEach(function (chip) {
            chip.classList.remove("active-playing");
        });
    }

    // Đưa ra global để hoc-chon.js có thể gọi ngắt khi cần
    window.dungAudioHuongDan = dungAudioHuongDan;

    // =========================================================
    // 3. TỰ ĐỘNG KHỞI TẠO VÀ MỞ MODAL HƯỚNG DẪN ĐỌC
    // =========================================================
    function ensureModalExists() {
        let modal = document.getElementById("modalHuongDanDoc");
        if (!modal) {
            const modalDiv = document.createElement("div");
            modalDiv.id = "modalHuongDanDoc";
            modalDiv.className = "modal-hd-overlay";
            modalDiv.innerHTML =
                '<div class="modal-hd-container">' +
                    '<div class="modal-hd-header">' +
                        '<h5 class="modal-hd-title">🗣️ Hướng dẫn cách đọc chuẩn</h5>' +
                        '<button type="button" class="modal-hd-close" onclick="dongHuongDanDoc(false)" title="Đóng">&times;</button>' +
                    '</div>' +
                    '<div class="modal-hd-body">' +
                        '<div id="hdLoadingBox" class="modal-hd-loading">' +
                            '<div class="modal-hd-spinner"></div>' +
                            '<div>Đang phân tích phát âm chuẩn...</div>' +
                        '</div>' +
                        '<div id="hdContentBox" style="display: none; flex-direction: column; gap: 16px;">' +
                            '<div class="hd-word-hero">' +
                                '<div class="hd-word" id="hdTuChinh">...</div>' +
                                '<div><span class="hd-phonetic-ipa" id="hdPhienAmIpa"></span></div>' +
                                '<div class="hd-visual-strip" id="hdVisualStrip" title="Bấm vào để nghe phát âm chuẩn cả từ">' +
                                    '<span class="hd-vs-word" id="hdVsWord"></span>' +
                                    '<span class="hd-vs-dash">–</span>' +
                                    '<span class="hd-vs-respell" id="hdVsRespell"></span>' +
                                    '<span class="hd-vs-dash">–</span>' +
                                    '<span class="hd-vs-meaning" id="hdVsMeaning"></span>' +
                                    '<span class="hd-vs-audio-icon">🔊</span>' +
                                '</div>' +
                                '<div><span class="hd-vietnamese-respell" id="hdPhienAmTiengViet"></span></div>' +
                                '<div class="hd-meaning" id="hdNghia"></div>' +
                            '</div>' +
                            '<div class="hd-audio-bar">' +
                                '<button type="button" class="btn-hd-audio btn-primary-audio" onclick="phatAudioHuongDan(1.0, this)" title="Nghe với tốc độ người bản xứ bình thường">🔊 Chuẩn (1.0x)</button>' +
                                '<button type="button" class="btn-hd-audio" onclick="phatAudioHuongDan(0.6, this)" title="Nghe chậm rõ từng âm như Google Dịch">🐢 Chậm (0.6x)</button>' +
                                '<button type="button" class="btn-hd-audio" onclick="docTachAmHuongDan(this)" title="Đọc từng âm tiết rồi đọc cả từ">🎶 Tách âm tiết</button>' +
                                '<button type="button" class="btn-hd-audio" onclick="danhVanHuongDan(this)" title="Đánh vần từng chữ cái tiếng Anh">🔡 Đánh vần</button>' +
                                '<button type="button" class="btn-hd-audio btn-hd-mindmap" style="background:#fef2f2; color:#dc2626; border-color:#fca5a5;" onclick="moSoDoDanhVanChoTuHienTai()" title="Xem sơ đồ tư duy quy luật đánh vần của từ này">🧠 Sơ đồ đánh vần</button>' +
                            '</div>' +
                            '<div class="hd-syllables-box">' +
                                '<div class="hd-section-title"><span>🎯</span> Các âm tiết (Bấm từng âm để nghe):</div>' +
                                '<div class="hd-syllable-chips" id="hdSyllableChips"></div>' +
                                '<p class="hd-syllable-hint">💡 Nhấp chuột vào từng âm tiết ở trên để luyện nghe riêng âm đó</p>' +
                            '</div>' +
                            '<div class="hd-speech-box">' +
                                '<button type="button" id="btnMicPractice" class="btn-mic-practice" onclick="batDauLuyenDoc()">🎙️ Bấm để thử phát âm</button>' +
                                '<div id="hdSpeechResult" class="hd-speech-result"></div>' +
                            '</div>' +
                            '<div class="hd-tips-grid">' +
                                '<div class="hd-tip-card mouth">' +
                                    '<div class="hd-tip-title">👄 Khẩu hình & vị trí lưỡi:</div>' +
                                    '<div id="hdTipKhauHinh">Mở miệng vừa phải, thả lỏng môi...</div>' +
                                '</div>' +
                                '<div class="hd-tip-card ending" id="hdCardAmDuoi" style="display: none;">' +
                                    '<div class="hd-tip-title">🔔 Chú ý âm đuôi (Ending sound):</div>' +
                                    '<div id="hdTipAmDuoi">...</div>' +
                                '</div>' +
                                '<div class="hd-tip-card warning">' +
                                    '<div class="hd-tip-title">⚠️ Lỗi người Việt hay gặp:</div>' +
                                    '<div id="hdTipLoi">...</div>' +
                                '</div>' +
                            '</div>' +
                        '</div>' +
                    '</div>' +
                    '<div class="modal-hd-footer">' +
                        '<button type="button" class="btn-hd-close-secondary" onclick="dongHuongDanDoc(false)">Đóng</button>' +
                        '<button type="button" class="btn-hd-back-study" id="btnHdBackStudy" onclick="dongHuongDanDoc(true)" title="Đóng modal và con trỏ tự động quay lại ô nhập để học tiếp">← Quay lại tiếp tục học tiếp</button>' +
                    '</div>' +
                '</div>';
            document.body.appendChild(modalDiv);
            modal = modalDiv;

            modal.addEventListener("click", function (e) {
                if (e.target === modal) {
                    dongHuongDanDoc(false);
                }
            });
        }
        return modal;
    }

    window.moHuongDanDoc = function (btnElement, tu, phienAm, nghia) {
        if (!tu) return;

        // Lưu dòng bảng hiện tại để khi đóng modal có thể tự focus ô nhập
        hangDangChon = null;
        if (btnElement) {
            hangDangChon = btnElement.closest("tr");
        }

        // Dừng mọi âm thanh đang phát trước đó
        dungAudioHuongDan();

        const modal = ensureModalExists();
        const loadingBox = document.getElementById("hdLoadingBox");
        const contentBox = document.getElementById("hdContentBox");

        if (!modal) return;

        // Ẩn nút "Quay lại tiếp tục học" nếu không có ô nhập câu trả lời trên trang
        const btnBack = document.getElementById("btnHdBackStudy");
        if (btnBack) {
            btnBack.style.display = (hangDangChon && hangDangChon.querySelector(".cau-tra-loi")) ? "inline-flex" : "none";
        }

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

    // =========================================================
    // 4. ĐÓNG MODAL
    // =========================================================
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

    window.moSoDoDanhVanChoTuHienTai = function () {
        if (duLieuHienTai && duLieuHienTai.tu) {
            dungAudioHuongDan();
            window.open("/so-do-danh-van?tu=" + encodeURIComponent(duLieuHienTai.tu), "_blank");
        }
    };

    // =========================================================
    // ĐỊNH DẠNG ÂM BỒI TIẾNG VIỆT & HIGHLIGHT ÂM ĐUÔI THEO ẢNH MẪU
    // =========================================================
    function dinhDangAmBoiHtml(str) {
        if (!str) return "";
        let s = String(str);
        // Nhận diện và bọc huy hiệu âm đuôi trực quan: -x, -z, -th, -đ, -ch, -dzh, -t, -k, -s
        s = s.replace(/-\s*(x|z|th|đ|ch|dzh|t|k|s)(?=[^a-zA-Z\d\u00C0-\u1EF9]|$)/gi, function(match, ending) {
            const lower = ending.toLowerCase();
            let cls = "ending-" + (lower === "đ" ? "d" : lower);
            return '<span class="hd-ending-tag ' + cls + '">-' + ending + '</span>';
        });
        return s;
    }

    // =========================================================
    // 5. RENDER NỘI DUNG VÀO MODAL
    // =========================================================
    function hienThiDuLieuModal(data) {
        // Từ chính & Nghĩa
        const elTu = document.getElementById("hdTuChinh");
        const elIpa = document.getElementById("hdPhienAmIpa");
        const elVn = document.getElementById("hdPhienAmTiengViet");
        const elNghia = document.getElementById("hdNghia");

        const elVsStrip = document.getElementById("hdVisualStrip");
        const elVsWord = document.getElementById("hdVsWord");
        const elVsRespell = document.getElementById("hdVsRespell");
        const elVsMeaning = document.getElementById("hdVsMeaning");

        const rawVn = data.phienAmTiengViet || data.tu || "";
        const formattedVnHtml = dinhDangAmBoiHtml(rawVn);
        const nghiaClean = data.nghia ? data.nghia.replace(/^\(|\)$/g, "").trim() : "";

        if (elTu) elTu.textContent = data.tu || "";
        if (elIpa) elIpa.textContent = data.phienAm ? (data.phienAm.startsWith("/") ? data.phienAm : "/" + data.phienAm + "/") : "";
        if (elVn) elVn.innerHTML = '🇻🇳 <span class="hd-vn-prefix">Âm Việt:</span> ' + formattedVnHtml;
        if (elNghia) elNghia.textContent = data.nghia ? "(" + data.nghia + ")" : "";

        // Card trực quan chuẩn theo ảnh mẫu (Word – Âm bồi – Nghĩa 🔊)
        if (elVsStrip) {
            elVsStrip.style.display = "inline-flex";
            if (elVsWord) elVsWord.textContent = data.tu || "";
            if (elVsRespell) elVsRespell.innerHTML = formattedVnHtml;
            if (elVsMeaning) elVsMeaning.textContent = nghiaClean || (data.tu || "");
            elVsStrip.onclick = function () {
                phatAudioHuongDan(1.0);
            };
            // Ẩn 2 dòng phụ trùng lặp để modal gọn gàng, thẻ âm tiết luôn hiển thị trọn vẹn
            if (elVn && elVn.parentElement) elVn.parentElement.style.display = "none";
            if (elNghia) elNghia.style.display = "none";
        } else {
            if (elVn && elVn.parentElement) elVn.parentElement.style.display = "block";
            if (elNghia) elNghia.style.display = "block";
        }

        // Tách âm tiết (Syllables)
        const chipContainer = document.getElementById("hdSyllableChips");
        if (chipContainer) {
            chipContainer.innerHTML = "";
            const amTiet = data.amTiet || [data.tu];
            const amTietIpa = data.amTietIpa || [];
            const amTietBoi = data.amTietBoi || [];
            const amTietDoc = data.amTietDoc || [];
            const amNhan = typeof data.amNhanIndex === "number" ? data.amNhanIndex : -1;

            amTiet.forEach(function (rawSyllable, idx) {
                // Làm sạch: chỉ hiển thị phần tiếng Anh sạch trên đầu thẻ
                const syllable = rawSyllable.replace(/\s*\([^)]*\)/g, "").trim();
                const chip = document.createElement("div");
                chip.className = "hd-syllable-chip" + (idx === amNhan ? " stressed" : "");
                chip.title = "Bấm để nghe âm tiết: " + syllable;

                let ipaText = amTietIpa[idx] ? "/" + amTietIpa[idx] + "/" : "";
                let boiText = amTietBoi[idx] || "";
                let boiHtml = dinhDangAmBoiHtml(boiText);
                let docText = (amTietDoc && amTietDoc[idx]) ? amTietDoc[idx] : syllable;

                chip.innerHTML = 
                    '<span class="syllable-stress-tag">⭐ Trọng âm</span>' +
                    '<span class="syllable-en">' + syllable + '</span>' +
                    (boiText ? '<span class="syllable-vn">' + boiHtml + '</span>' : '') +
                    (ipaText ? '<span class="syllable-ipa">' + ipaText + '</span>' : '');

                chip.addEventListener("click", function () {
                    // Dừng ngay mọi âm thanh khác trước khi đọc âm tiết này
                    dungAudioHuongDan();
                    chip.classList.add("active-playing");
                    docAmTiet(syllable, docText, function () {
                        chip.classList.remove("active-playing");
                    });
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

    // =========================================================
    // 6. PHÁT AUDIO (CHUẨN 1.0x HOẶC CHẬM 0.6x)
    // =========================================================
    window.phatAudioHuongDan = function (tocDo, btnEl) {
        if (!duLieuHienTai || !duLieuHienTai.tu) return;
        
        // Dừng tất cả âm thanh đang phát trước đó
        dungAudioHuongDan();

        if (btnEl) btnEl.classList.add("playing");

        phatAudioTu(duLieuHienTai.tu, tocDo, function () {
            if (btnEl) btnEl.classList.remove("playing");
        });
    };

    function phatAudioTu(tu, tocDo, onEnd) {
        const isSlow = tocDo && tocDo < 0.9;
        const rateParam = isSlow ? "-28%" : "+0%";

        if (window.phatAmThanh) {
            window.phatAmThanh(tu, {
                rate: rateParam,
                onEnd: onEnd,
                onError: onEnd
            });
            return;
        }

        const urlPhat = "/audio/phat?text=" + encodeURIComponent(tu) + "&rate=" + encodeURIComponent(rateParam);
        const audio = new Audio(urlPhat);
        hdAudioHienTai = audio;

        audio.onended = function () {
            hdAudioHienTai = null;
            if (onEnd) onEnd();
        };

        audio.onerror = function () {
            hdAudioHienTai = null;
            if (onEnd) onEnd();
        };

        const playPromise = audio.play();
        if (playPromise !== undefined) {
            playPromise.catch(function () {
                hdAudioHienTai = null;
                if (onEnd) onEnd();
            });
        }
    }

    // =========================================================
    // 7. ĐỌC TỪNG ÂM TIẾT CHUẨN XÁC THEO TỪ CHÍNH (NEURAL TTS)
    // =========================================================
    const SYLLABLE_PHONETIC_MAP = {
        "co": "caw", "com": "cawm", "me": "muh", "dy": "dee", "ty": "tee",
        "ly": "lee", "ny": "nee", "ry": "ree", "sy": "see", "cy": "see",
        "gy": "jee", "al": "ull", "el": "ell", "le": "ull", "ble": "bull",
        "ple": "pull", "tle": "tull", "dle": "dull", "cle": "cull",
        "fle": "full", "tion": "shun", "sion": "zhun", "ture": "chur",
        "ous": "us", "ful": "full", "ment": "muhnt", "ness": "ness",
        "for": "fer", "ta": "tuh", "ca": "kuh", "ga": "guh"
    };

    function docAmTiet(syllable, docText, onEnd) {
        dungAudioHuongDan();

        let toSpeak = docText;
        if (!toSpeak || toSpeak.trim() === "") {
            const lowerSyl = (syllable || "").toLowerCase().trim();
            toSpeak = SYLLABLE_PHONETIC_MAP[lowerSyl] || syllable;
        }

        // Ưu tiên phát qua Microsoft Edge Neural TTS cho âm chuẩn xác và tự nhiên
        const ttsUrl = "/audio/tts?text=" + encodeURIComponent(toSpeak) + "&rate=-10%";
        const audio = new Audio(ttsUrl);
        hdAudioHienTai = audio;

        let fallbackDone = false;
        function doFallback() {
            if (fallbackDone) return;
            fallbackDone = true;
            if (window.speechSynthesis) {
                const utterance = new SpeechSynthesisUtterance(toSpeak);
                utterance.lang = "en-US";
                utterance.rate = 0.8;
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
            doFallback();
        };

        const p = audio.play();
        if (p !== undefined) {
            p.catch(function () {
                doFallback();
            });
        }
    }

    // =========================================================
    // 8. ĐÁNH VẦN TỪNG CHỮ CÁI (SPELLING BẢN XỨ CHUẨN 100%)
    // Dùng bộ 26 file MP3 phát âm chữ cái bản xứ (/audio/alphabet/{a-z}.mp3)
    // =========================================================
    function phatAudioChuCai(char, onEnd) {
        const c = (char || "").toLowerCase();
        if (c >= 'a' && c <= 'z') {
            const audio = new Audio("/audio/alphabet/" + c + ".mp3");
            hdAudioHienTai = audio;
            let done = false;
            function finish() {
                if (done) return;
                done = true;
                hdAudioHienTai = null;
                if (onEnd) onEnd();
            }
            audio.onended = finish;
            audio.onerror = function () {
                phatSpeechChuCai(c, finish);
            };
            const p = audio.play();
            if (p !== undefined) {
                p.catch(function () {
                    phatSpeechChuCai(c, finish);
                });
            }
        } else {
            phatSpeechChuCai(c, onEnd);
        }
    }

    function phatSpeechChuCai(char, onEnd) {
        const text = (char || "").toUpperCase();
        if (window.phatAmThanh) {
            window.phatAmThanh(text, { onEnd: onEnd, onError: onEnd });
        } else {
            const audio = new Audio("/audio/phat?text=" + encodeURIComponent(text) + "&rate=+0%");
            hdAudioHienTai = audio;
            audio.onended = function () { hdAudioHienTai = null; if (onEnd) onEnd(); };
            audio.onerror = function () { hdAudioHienTai = null; if (onEnd) onEnd(); };
            audio.play().catch(function () { hdAudioHienTai = null; if (onEnd) onEnd(); });
        }
    }

    window.danhVanHuongDan = function (btnEl) {
        if (!duLieuHienTai || !duLieuHienTai.tu) return;

        // Dừng tất cả âm thanh trước đó
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
                // Đánh vần xong -> đọc lại cả từ hoàn chỉnh tốc độ chuẩn!
                const t = setTimeout(function () {
                    phatAudioTu(duLieuHienTai.tu, 1.0, function () {
                        if (btnEl) btnEl.classList.remove("playing");
                    });
                }, 400);
                hdTimeoutList.push(t);
                return;
            }
            const char = letters[idx];
            idx++;

            phatAudioChuCai(char, function () {
                const t = setTimeout(docChuTiep, 200);
                hdTimeoutList.push(t);
            });
        }

        docChuTiep();
    };

    // =========================================================
    // 9. ĐỌC TÁCH TỪNG ÂM TIẾT RỒI ĐỌC CẢ TỪ HOÀN CHỈNH
    // =========================================================
    window.docTachAmHuongDan = function (btnEl) {
        if (!duLieuHienTai || !duLieuHienTai.tu) return;

        // Dừng tất cả âm thanh trước đó
        dungAudioHuongDan();

        if (btnEl) btnEl.classList.add("playing");

        const amTietList = duLieuHienTai.amTiet && duLieuHienTai.amTiet.length > 0 
            ? duLieuHienTai.amTiet 
            : [duLieuHienTai.tu];
        const amTietDocList = duLieuHienTai.amTietDoc || [];

        const chips = document.querySelectorAll(".hd-syllable-chip");

        let idx = 0;
        function docAmTiep() {
            if (idx >= amTietList.length) {
                chips.forEach(function (c) { c.classList.remove("active-playing"); });

                // Sau khi đọc xong các âm tiết -> đọc hoàn chỉnh lại cả từ theo phát âm chuẩn!
                const t = setTimeout(function () {
                    phatAudioTu(duLieuHienTai.tu, 1.0, function () {
                        if (btnEl) btnEl.classList.remove("playing");
                    });
                }, 500);
                hdTimeoutList.push(t);
                return;
            }

            const currentIdx = idx;
            const syllable = amTietList[currentIdx].replace(/\s*\([^)]*\)/g, "").trim();
            const speakText = (amTietDocList[currentIdx]) ? amTietDocList[currentIdx] : syllable;
            idx++;

            // Highlight trực quan âm tiết đang được phát
            chips.forEach(function (c, i) {
                if (i === currentIdx) c.classList.add("active-playing");
                else c.classList.remove("active-playing");
            });

            docAmTiet(syllable, speakText, function () {
                const t = setTimeout(docAmTiep, 450);
                hdTimeoutList.push(t);
            });
        }

        docAmTiep();
    };

    // =========================================================
    // 10. LUYỆN NÓI / NHẬN DIỆN GIỌNG NÓI (WEB SPEECH API)
    // =========================================================
    window.batDauLuyenDoc = function () {
        const micBtn = document.getElementById("btnMicPractice");
        const micResult = document.getElementById("hdSpeechResult");
        if (!micBtn || !micResult || !duLieuHienTai) return;

        const SpeechRec = window.SpeechRecognition || window.webkitSpeechRecognition;
        if (!SpeechRec) {
            micResult.innerHTML = '<span style="color: #ea580c;">Trình duyệt của bạn chưa hỗ trợ nhận diện giọng nói (khuyên dùng Google Chrome).</span>';
            return;
        }

        // Tắt toàn bộ âm thanh khi người dùng chuẩn bị phát âm
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

    // =========================================================
    // 11. DỮ LIỆU DỰ PHÒNG CHUẨN HÓA TIẾNG VIỆT
    // =========================================================
    function taoDuLieuDuPhong(tu, phienAm, nghia) {
        return {
            tu: tu,
            phienAm: phienAm || "",
            nghia: nghia || "",
            amTiet: [tu],
            amTietIpa: [phienAm || ""],
            amTietBoi: [tu],
            amTietDoc: [tu],
            amNhanIndex: 0,
            phienAmTiengViet: tu,
            trongAm: "Nhấn âm 1",
            khauHinh: "Mở miệng tự nhiên, thả lỏng môi và phát âm rõ âm.",
            amDuoi: "",
            loiThuongGap: "Chú ý đọc trọn vẹn từ và nhấn đúng trọng âm.",
            meoGhiNho: ""
        };
    }

    // =========================================================
    // 12. SỰ KIỆN PHÍM & CLICK NGOÀI MODAL
    // =========================================================
    document.addEventListener("keydown", function (e) {
        const modal = document.getElementById("modalHuongDanDoc");
        if (e.key === "Escape" && modal && modal.classList.contains("show")) {
            dongHuongDanDoc(false);
        }
    });
})();
