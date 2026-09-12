/**
 * SƠ ĐỒ TƯ DUY QUY LUẬT ĐÁNH VẦN TIẾNG ANH (PHONICS MINDMAP)
 * Tự động tính toán tọa độ tỏa tròn, vẽ đường cong mũi tên SVG
 * Hỗ trợ tự đọc khi di chuột (Hover-to-read) & Icon hướng dẫn phát âm chi tiết
 */

(function () {
    // State toàn cục
    let dsQuyLuat = window.SERVER_DATA_DS_QUY_LUAT || [];
    let quyLuatHienTai = window.SERVER_DATA_QUY_LUAT_HIEN_TAI || null;
    let audioHienTai = null;
    let hoverTimeout = null;
    let autoPlayTimer = null;
    let dangAutoPlay = false;
    let cheDoViewGrid = false;

    // =========================================================
    // 1. QUẢN LÝ ÂM THANH DUY NHẤT (AUDIO CONTROLLER)
    // =========================================================
    function dungAudio() {
        if (hoverTimeout) {
            clearTimeout(hoverTimeout);
            hoverTimeout = null;
        }
        if (autoPlayTimer) {
            clearTimeout(autoPlayTimer);
            autoPlayTimer = null;
        }
        if (audioHienTai) {
            try {
                audioHienTai.pause();
                audioHienTai.currentTime = 0;
            } catch (e) {}
            audioHienTai = null;
        }
        if (window.speechSynthesis) {
            try { window.speechSynthesis.cancel(); } catch (e) {}
        }
        document.querySelectorAll(".sd-word-card.playing").forEach(function (c) {
            c.classList.remove("playing");
        });
    }

    function phatAmThanh(vanBan, audioUrl, callback) {
        dungAudio();
        if (!vanBan && !audioUrl) return;

        // Ưu tiên Audio URL từ backend
        const url = audioUrl || ("/audio/tts?text=" + encodeURIComponent(vanBan) + "&rate=+0%");
        const audio = new Audio(url);
        audioHienTai = audio;

        audio.onended = function () {
            audioHienTai = null;
            if (typeof callback === "function") callback();
        };

        audio.onerror = function () {
            // Fallback SpeechSynthesis nếu trình duyệt chặn audio
            if (window.speechSynthesis) {
                const utter = new SpeechSynthesisUtterance(vanBan);
                utter.lang = "en-US";
                utter.rate = 0.9;
                utter.onend = function () {
                    if (typeof callback === "function") callback();
                };
                window.speechSynthesis.speak(utter);
            }
        };

        audio.play().catch(function () {
            if (typeof callback === "function") callback();
        });
    }

    // =========================================================
    // 2. KHỞI TẠO KHI TẢI TRANG
    // =========================================================
    document.addEventListener("DOMContentLoaded", function () {
        // Tự động chuyển grid nếu màn hình nhỏ (Mobile)
        if (window.innerWidth < 850) {
            cheDoViewGrid = true;
            const board = document.getElementById("sdMindmapContainer");
            if (board) board.classList.add("view-grid");
        }

        // Tải danh sách quy tắc nếu chưa có
        if (!dsQuyLuat || dsQuyLuat.length === 0) {
            fetch("/api/so-do-danh-van/danh-sach")
                .then(function (r) { return r.json(); })
                .then(function (data) {
                    dsQuyLuat = data;
                    renderRulePills(dsQuyLuat);
                    if (!quyLuatHienTai && dsQuyLuat.length > 0) {
                        taiQuyLuat(dsQuyLuat[0].id);
                    }
                })
                .catch(function () {});
        } else {
            renderRulePills(dsQuyLuat);
            if (quyLuatHienTai) {
                renderSoDoMindmap(quyLuatHienTai);
            } else if (dsQuyLuat.length > 0) {
                taiQuyLuat(dsQuyLuat[0].id);
            }
        }

        // Tự vẽ lại đường nối SVG khi thay đổi kích thước cửa sổ
        window.addEventListener("resize", function () {
            if (!cheDoViewGrid) {
                veCacDuongMuiTenSvg();
            }
        });
    });

    // =========================================================
    // 3. RENDER DANH SÁCH PILLS CHỌN QUY TẮC
    // =========================================================
    function renderRulePills(danhSach) {
        const container = document.getElementById("sdRulePillsContainer");
        if (!container) return;

        container.innerHTML = "";
        const idHienTai = quyLuatHienTai ? quyLuatHienTai.id : "";

        danhSach.forEach(function (ql) {
            const btn = document.createElement("button");
            btn.type = "button";
            btn.className = "btn-sd-pill" + (ql.id === idHienTai ? " active" : "");
            btn.setAttribute("data-id", ql.id);
            btn.setAttribute("data-cat", ql.phanLoai);
            btn.innerHTML = `<span>${ql.icon || '⚡'}</span> <span>${ql.cumChu} <small>(${ql.docLaIpa})</small></span>`;

            btn.onclick = function () {
                taiQuyLuat(ql.id);
            };

            container.appendChild(btn);
        });
    }

    // =========================================================
    // 4. TẢI VÀ HIỂN THỊ CHI TIẾT 1 QUY TẮC
    // =========================================================
    window.taiQuyLuat = function (id) {
        dungAudio();
        dangAutoPlay = false;
        capNhatNutAutoPlay(false);

        // Đổi active pill
        document.querySelectorAll(".btn-sd-pill").forEach(function (p) {
            p.classList.toggle("active", p.getAttribute("data-id") === id);
        });

        // Kiểm tra trong danh sách cục bộ
        const timThay = dsQuyLuat.find(function (item) { return item.id === id; });
        if (timThay && timThay.danhSachTu && timThay.danhSachTu.length > 0) {
            quyLuatHienTai = timThay;
            renderSoDoMindmap(timThay);
            return;
        }

        // Tải từ API
        hienThiLoading(true, "Đang tải dữ liệu quy luật...");
        fetch("/api/so-do-danh-van/chi-tiet?id=" + encodeURIComponent(id))
            .then(function (r) {
                if (!r.ok) throw new Error("Không tìm thấy quy tắc");
                return r.json();
            })
            .then(function (data) {
                hienThiLoading(false);
                quyLuatHienTai = data;
                renderSoDoMindmap(data);
            })
            .catch(function (err) {
                hienThiLoading(false);
                console.error("[SoDoDanhVan] Lỗi tải quy tắc:", err);
            });
    };

    // =========================================================
    // 5. RENDER SƠ ĐỒ MINDMAP (CANVAS & VỆ TINH TỎA TRÒN)
    // =========================================================
    function renderSoDoMindmap(ql) {
        if (!ql) return;

        // Cập nhật Banner
        const elCat = document.getElementById("lblBannerCategory");
        const elTitle = document.getElementById("lblBannerTitle");
        const elDesc = document.getElementById("lblBannerDesc");

        if (elCat) elCat.textContent = ql.tenPhanLoai || "Quy luật đánh vần";
        if (elTitle) elTitle.textContent = ql.tieuDeQuyTac || ql.cumChu;
        if (elDesc) elDesc.textContent = ql.moTaQuyTac || "";

        // Cập nhật Node Trung tâm (Âm ở giữa)
        const elCenterLetter = document.getElementById("lblCenterLetter");
        const elCenterIpa = document.getElementById("lblCenterIpa");

        if (elCenterLetter) elCenterLetter.textContent = ql.cumChu;
        if (elCenterIpa) elCenterIpa.textContent = "đọc là " + ql.docLaIpa;

        // Render các thẻ từ vệ tinh xung quanh
        const satellitesWrap = document.getElementById("satellitesWrapper");
        if (!satellitesWrap) return;
        satellitesWrap.innerHTML = "";

        const dsTu = ql.danhSachTu || [];
        const total = dsTu.length;

        dsTu.forEach(function (t, idx) {
            const card = document.createElement("div");
            card.className = "sd-word-card";
            card.setAttribute("data-tu", t.tu);
            card.setAttribute("data-audio", t.audioUrl || ("/audio/tts?text=" + encodeURIComponent(t.tu)));

            // Tạo chữ có bôi đỏ phần quy tắc
            const wordHtml = taoChuHighlight(t.tu, t.phanHighlight);
            const ipaHtml = taoIpaHighlight(t.phienAm, t.phanIpaHighlight);

            card.innerHTML = `
                <div class="sd-card-icon">${t.icon || '💡'}</div>
                <div class="sd-card-word">${wordHtml}</div>
                <div class="sd-card-ipa">${ipaHtml}</div>
                <div class="sd-card-meaning">${t.nghia || ''}</div>
                <div class="sd-card-actions">
                    <button type="button" class="btn-card-audio" title="Nghe phát âm" onclick="event.stopPropagation(); clickPhatAmTu('${t.tu}', '${t.audioUrl || ''}', this.closest('.sd-word-card'))">🔊</button>
                    <button type="button" class="btn-card-guide" title="Xem hướng dẫn đọc chi tiết" onclick="event.stopPropagation(); moHuongDanTu('${t.tu}', '${t.phienAm || ''}', '${t.nghia || ''}', this)">🗣️</button>
                </div>
            `;

            // Hover tự động đọc
            card.addEventListener("mouseenter", function () {
                hoverPhatAmTu(t.tu, t.audioUrl || '', card);
            });

            // Click phát âm
            card.addEventListener("click", function () {
                clickPhatAmTu(t.tu, t.audioUrl || '', card);
            });

            satellitesWrap.appendChild(card);
        });

        // Bố trí tọa độ vệ tinh và vẽ mũi tên SVG (nếu không ở chế độ view-grid)
        setTimeout(function () {
            if (!cheDoViewGrid) {
                sapXepVeTinhToaTron();
                veCacDuongMuiTenSvg();
            }
        }, 50);
    }

    // Highlight phần chữ theo quy tắc màu đỏ đậm
    function taoChuHighlight(tu, phanHl) {
        if (!tu) return "";
        if (!phanHl) return tu;

        const idx = tu.toLowerCase().indexOf(phanHl.toLowerCase());
        if (idx === -1) return tu;

        const before = tu.substring(0, idx);
        const match = tu.substring(idx, idx + phanHl.length);
        const after = tu.substring(idx + phanHl.length);

        return `${before}<span class="hl-red">${match}</span>${after}`;
    }

    // Highlight âm IPA theo quy tắc màu đỏ
    function taoIpaHighlight(ipa, phanIpaHl) {
        if (!ipa) return "";
        if (!phanIpaHl) return ipa;

        const cleanIpa = ipa.replace(/[/\[\]]/g, "");
        const idx = cleanIpa.indexOf(phanIpaHl);
        if (idx === -1) return ipa;

        const before = cleanIpa.substring(0, idx);
        const match = cleanIpa.substring(idx, idx + phanIpaHl.length);
        const after = cleanIpa.substring(idx + phanIpaHl.length);

        return `/${before}<span class="hl-red">${match}</span>${after}/`;
    }

    // =========================================================
    // 6. SẮP XẾP VỆ TINH THEO HÌNH ELIP TỎA TRÒN (RADIAL LAYOUT)
    // =========================================================
    function sapXepVeTinhToaTron() {
        const board = document.getElementById("sdMindmapContainer");
        const cards = document.querySelectorAll(".sd-satellites-wrapper .sd-word-card");
        if (!board || cards.length === 0) return;

        const boardWidth = board.clientWidth;
        const boardHeight = board.clientHeight;

        const centerX = boardWidth / 2;
        const centerY = boardHeight / 2;

        // Bán kính elip tùy theo kích thước màn hình
        const radiusX = Math.min(360, Math.max(260, boardWidth * 0.36));
        const radiusY = Math.min(240, Math.max(180, boardHeight * 0.36));

        const count = cards.length;
        // Bắt đầu từ góc -90 độ (đỉnh trên cùng 12 giờ) và quay theo chiều kim đồng hồ
        const angleStep = (2 * Math.PI) / count;
        const startAngle = -Math.PI / 2;

        cards.forEach(function (card, i) {
            const angle = startAngle + i * angleStep;
            const x = centerX + radiusX * Math.cos(angle) - (card.offsetWidth / 2);
            const y = centerY + radiusY * Math.sin(angle) - (card.offsetHeight / 2);

            card.style.left = Math.round(x) + "px";
            card.style.top = Math.round(y) + "px";
        });
    }

    // =========================================================
    // 7. VẼ CÁC MŨI TÊN UỐN LƯỢN SVG NỐI TỪ TÂM RA CÁC TỪ
    // =========================================================
    function veCacDuongMuiTenSvg() {
        const svg = document.getElementById("svgMindmapLines");
        const board = document.getElementById("sdMindmapContainer");
        const centerNode = document.getElementById("nodeCenterHub");
        const cards = document.querySelectorAll(".sd-satellites-wrapper .sd-word-card");

        if (!svg || !board || !centerNode || cards.length === 0) return;

        const boardRect = board.getBoundingClientRect();
        const centerRect = centerNode.getBoundingClientRect();

        const cX = centerRect.left - boardRect.left + (centerRect.width / 2);
        const cY = centerRect.top - boardRect.top + (centerRect.height / 2);

        // Thiết lập kích thước SVG
        svg.setAttribute("width", boardRect.width);
        svg.setAttribute("height", boardRect.height);

        let pathsHtml = `
            <defs>
                <marker id="arrowHead" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto">
                    <polygon points="0 0, 9 4.5, 0 9" fill="#dc2626" />
                </marker>
            </defs>
        `;

        cards.forEach(function (card) {
            const cardRect = card.getBoundingClientRect();
            const targetX = cardRect.left - boardRect.left + (cardRect.width / 2);
            const targetY = cardRect.top - boardRect.top + (cardRect.height / 2);

            // Điểm bắt đầu từ mép ngoài của Node trung tâm
            const dx = targetX - cX;
            const dy = targetY - cY;
            const dist = Math.sqrt(dx * dx + dy * dy);
            if (dist === 0) return;

            const startX = cX + (dx / dist) * 75;
            const startY = cY + (dy / dist) * 55;

            // Điểm kết thúc ở mép thẻ từ
            const endX = targetX - (dx / dist) * (cardRect.width * 0.45);
            const endY = targetY - (dy / dist) * (cardRect.height * 0.45);

            // Điểm điều khiển uốn lượn cong tự nhiên (Quadratic Bezier)
            const midX = (startX + endX) / 2;
            const midY = (startY + endY) / 2;
            const curvature = 18;
            const ctrlX = midX - (dy / dist) * curvature;
            const ctrlY = midY + (dx / dist) * curvature;

            pathsHtml += `
                <path d="M ${startX.toFixed(1)} ${startY.toFixed(1)} Q ${ctrlX.toFixed(1)} ${ctrlY.toFixed(1)} ${endX.toFixed(1)} ${endY.toFixed(1)}"
                      stroke="#ef4444"
                      stroke-width="2.2"
                      stroke-dasharray="4 2"
                      fill="none"
                      marker-end="url(#arrowHead)"
                      opacity="0.85" />
            `;
        });

        svg.innerHTML = pathsHtml;
    }

    // =========================================================
    // 8. TỰ ĐỘNG ĐỌC KHI HOVER & CLICK PHÁT ÂM
    // =========================================================
    function kiemTraCoChoPhepAutoRead() {
        const chk = document.getElementById("chkAutoHoverRead");
        return chk ? chk.checked : true;
    }

    window.hoverPhatAmNodeTrungTam = function () {
        if (!kiemTraCoChoPhepAutoRead() || dangAutoPlay) return;
        if (hoverTimeout) clearTimeout(hoverTimeout);
        hoverTimeout = setTimeout(function () {
            clickPhatAmNodeTrungTam();
        }, 150);
    };

    window.clickPhatAmNodeTrungTam = function () {
        if (!quyLuatHienTai) return;
        const textToRead = quyLuatHienTai.amDoc || quyLuatHienTai.cumChu;
        phatAmThanh(textToRead, null);
    };

    function hoverPhatAmTu(tu, audioUrl, cardEl) {
        if (!kiemTraCoChoPhepAutoRead() || dangAutoPlay) return;
        if (hoverTimeout) clearTimeout(hoverTimeout);
        hoverTimeout = setTimeout(function () {
            clickPhatAmTu(tu, audioUrl, cardEl);
        }, 150);
    }

    function clickPhatAmTu(tu, audioUrl, cardEl) {
        if (cardEl) {
            document.querySelectorAll(".sd-word-card.playing").forEach(function (c) { c.classList.remove("playing"); });
            cardEl.classList.add("playing");
        }
        phatAmThanh(tu, audioUrl, function () {
            if (cardEl) cardEl.classList.remove("playing");
        });
    }

    // Mở modal hướng dẫn đọc chi tiết cho từng từ
    window.moHuongDanTu = function (tu, phienAm, nghia, btnEl) {
        dungAudio();
        if (typeof window.moHuongDanDoc === "function") {
            window.moHuongDanDoc(btnEl, tu, phienAm, nghia);
        } else {
            alert(`Từ: ${tu}\nPhiên âm: ${phienAm}\nNghĩa: ${nghia}`);
        }
    };

    // =========================================================
    // 9. ĐỌC TOÀN BỘ SƠ ĐỒ (AUTOPLAY ALL WORDS)
    // =========================================================
    window.docToanBoSoDo = function () {
        if (!quyLuatHienTai) return;

        if (dangAutoPlay) {
            dungAudio();
            dangAutoPlay = false;
            capNhatNutAutoPlay(false);
            return;
        }

        dangAutoPlay = true;
        capNhatNutAutoPlay(true);

        const cards = Array.from(document.querySelectorAll(".sd-satellites-wrapper .sd-word-card"));
        let step = -1; // -1: Đọc âm trung tâm trước, sau đó 0..n: đọc các từ

        function chayBuocTiepTheo() {
            if (!dangAutoPlay) return;

            step++;
            if (step >= cards.length) {
                dangAutoPlay = false;
                capNhatNutAutoPlay(false);
                return;
            }

            if (step === 0) {
                // Đọc âm trung tâm trước
                const centerNode = document.getElementById("nodeCenterHub");
                if (centerNode) centerNode.classList.add("pulse-active");
                const text = quyLuatHienTai.amDoc || quyLuatHienTai.cumChu;
                phatAmThanh(text, null, function () {
                    if (centerNode) centerNode.classList.remove("pulse-active");
                    autoPlayTimer = setTimeout(chayBuocTiepTheo, 600);
                });
                return;
            }

            // Đọc các từ vệ tinh
            const card = cards[step];
            const tu = card.getAttribute("data-tu");
            const audioUrl = card.getAttribute("data-audio");

            document.querySelectorAll(".sd-word-card.playing").forEach(function (c) { c.classList.remove("playing"); });
            card.classList.add("playing");

            phatAmThanh(tu, audioUrl, function () {
                card.classList.remove("playing");
                autoPlayTimer = setTimeout(chayBuocTiepTheo, 700);
            });
        }

        chayBuocTiepTheo();
    };

    function capNhatNutAutoPlay(isPlaying) {
        const btn = document.getElementById("btnAutoPlayAll");
        if (!btn) return;
        if (isPlaying) {
            btn.innerHTML = "⏹️ Dừng đọc sơ đồ";
            btn.classList.add("btn-danger");
            btn.classList.remove("btn-sd-autoplay");
        } else {
            btn.innerHTML = "▶️ Đọc toàn bộ sơ đồ";
            btn.classList.add("btn-sd-autoplay");
            btn.classList.remove("btn-danger");
        }
    }

    // =========================================================
    // 10. MODAL HƯỚNG DẪN ÂM TRUNG TÂM
    // =========================================================
    window.moHuongDanAmTrungTam = function () {
        if (!quyLuatHienTai) return;
        dungAudio();

        const modal = document.getElementById("modalAmTrungTam");
        const title = document.getElementById("lblModalCenterTitle");
        const bigSymbol = document.getElementById("lblModalBigSymbol");
        const ruleDetail = document.getElementById("lblModalRuleDetail");
        const mouthDetail = document.getElementById("lblModalMouthDetail");
        const wordsList = document.getElementById("lblModalSampleWords");

        if (title) title.textContent = `Quy tắc "${quyLuatHienTai.cumChu}" đọc là ${quyLuatHienTai.docLaIpa}`;
        if (bigSymbol) bigSymbol.textContent = quyLuatHienTai.docLaIpa;
        if (ruleDetail) ruleDetail.textContent = quyLuatHienTai.moTaQuyTac || "";
        if (mouthDetail) mouthDetail.textContent = quyLuatHienTai.huongDanPhatAm || "Mở khẩu hình tự nhiên, phát âm rõ ràng từ cuống họng.";

        if (wordsList) {
            wordsList.innerHTML = "";
            (quyLuatHienTai.danhSachTu || []).forEach(function (w) {
                const span = document.createElement("span");
                span.className = "sd-modal-chip-word";
                span.innerHTML = `<strong>${w.tu}</strong> <small class="text-muted">${w.phienAm}</small> - ${w.nghia}`;
                span.onclick = function () {
                    phatAmThanh(w.tu, w.audioUrl);
                };
                wordsList.appendChild(span);
            });
        }

        if (modal) modal.style.display = "flex";
    };

    window.dongModalAmTrungTam = function () {
        dungAudio();
        const modal = document.getElementById("modalAmTrungTam");
        if (modal) modal.style.display = "none";
    };

    // =========================================================
    // 11. BỘ LỌC THEO NHÓM & CHUYỂN VIEW
    // =========================================================
    window.locTheoNhom = function (cat, btnEl) {
        document.querySelectorAll(".sd-category-tabs .btn-sd-tab").forEach(function (b) {
            b.classList.remove("active");
        });
        if (btnEl) btnEl.classList.add("active");

        const pills = document.querySelectorAll("#sdRulePillsContainer .btn-sd-pill");
        let firstMatchId = null;

        pills.forEach(function (pill) {
            const pillCat = pill.getAttribute("data-cat");
            if (cat === "ALL" || pillCat === cat) {
                pill.style.display = "inline-flex";
                if (!firstMatchId) firstMatchId = pill.getAttribute("data-id");
            } else {
                pill.style.display = "none";
            }
        });

        // Nếu quy luật hiện tại không thuộc nhóm mới, chuyển sang cái đầu tiên của nhóm
        if (firstMatchId && quyLuatHienTai && quyLuatHienTai.phanLoai !== cat && cat !== "ALL") {
            taiQuyLuat(firstMatchId);
        }
    };

    window.chuyenDoiCheDoHienThi = function () {
        cheDoViewGrid = !cheDoViewGrid;
        const board = document.getElementById("sdMindmapContainer");
        if (!board) return;

        if (cheDoViewGrid) {
            board.classList.add("view-grid");
        } else {
            board.classList.remove("view-grid");
            sapXepVeTinhToaTron();
            veCacDuongMuiTenSvg();
        }
    };

    // =========================================================
    // 12. TÌM KIẾM THÔNG MINH & GỌI AI VẼ SƠ ĐỒ MỚI
    // =========================================================
    window.xuLyTimKiem = function (val) {
        const drop = document.getElementById("searchSuggestions");
        if (!drop) return;

        if (!val || val.trim().length === 0) {
            drop.style.display = "none";
            return;
        }

        const q = val.trim().toLowerCase();
        const ketQua = [];

        dsQuyLuat.forEach(function (ql) {
            let match = false;
            let tuMatch = "";

            if (ql.cumChu.toLowerCase().includes(q) || ql.tieuDeQuyTac.toLowerCase().includes(q)) {
                match = true;
            } else {
                for (let t of ql.danhSachTu) {
                    if (t.tu.toLowerCase().includes(q) || t.nghia.toLowerCase().includes(q)) {
                        match = true;
                        tuMatch = t.tu;
                        break;
                    }
                }
            }

            if (match) {
                ketQua.push({ ql: ql, tuMatch: tuMatch });
            }
        });

        if (ketQua.length === 0) {
            drop.innerHTML = `<div class="p-3 text-center text-muted">
                Không có sẵn trong kho. <br>Bấm <b>"🤖 AI vẽ sơ đồ"</b> để Gemini tạo sơ đồ cho "${val}"!
            </div>`;
            drop.style.display = "block";
            return;
        }

        let html = "";
        ketQua.slice(0, 6).forEach(function (item) {
            html += `
                <div class="sd-search-item" onclick="chonGoiYTimKiem('${item.ql.id}')">
                    <div>
                        <strong>${item.ql.icon || '⚡'} ${item.ql.cumChu}</strong>
                        <small class="text-danger fw-bold">(${item.ql.docLaIpa})</small>
                        <div class="small text-muted">${item.ql.tieuDeQuyTac}</div>
                    </div>
                    ${item.tuMatch ? `<span class="badge bg-light text-dark border">${item.tuMatch}</span>` : ''}
                </div>
            `;
        });

        drop.innerHTML = html;
        drop.style.display = "block";
    };

    window.chonGoiYTimKiem = function (id) {
        const drop = document.getElementById("searchSuggestions");
        if (drop) drop.style.display = "none";
        taiQuyLuat(id);
    };

    window.timKiemVaVeSoDo = function () {
        const txt = document.getElementById("txtSearchRule");
        const val = txt ? txt.value.trim() : "";
        if (!val) return;

        const drop = document.getElementById("searchSuggestions");
        if (drop) drop.style.display = "none";

        // Thử tìm trong kho có sẵn
        const q = val.toLowerCase();
        const found = dsQuyLuat.find(function (ql) {
            if (ql.cumChu.toLowerCase() === q || ql.id.toLowerCase() === q) return true;
            return ql.danhSachTu.some(function (t) { return t.tu.toLowerCase() === q; });
        });

        if (found) {
            taiQuyLuat(found.id);
        } else {
            goiAiVeSoDo();
        }
    };

    window.goiAiVeSoDo = function () {
        const txt = document.getElementById("txtSearchRule");
        const val = txt ? txt.value.trim() : "";
        if (!val) {
            alert("Vui lòng nhập từ hoặc âm cần vẽ sơ đồ tư duy!");
            if (txt) txt.focus();
            return;
        }

        hienThiLoading(true, `🤖 AI Gemini đang phân tích quy luật đánh vần cho "${val}" và vẽ sơ đồ tư duy...`);

        fetch("/api/so-do-danh-van/ai-generate?q=" + encodeURIComponent(val))
            .then(function (r) {
                if (!r.ok) throw new Error("Không thể tạo sơ đồ với AI");
                return r.json();
            })
            .then(function (data) {
                hienThiLoading(false);
                quyLuatHienTai = data;

                // Thêm vào danh sách nếu chưa có
                if (!dsQuyLuat.some(function (x) { return x.id === data.id; })) {
                    dsQuyLuat.unshift(data);
                    renderRulePills(dsQuyLuat);
                }

                renderSoDoMindmap(data);
            })
            .catch(function (err) {
                hienThiLoading(false);
                alert("Lỗi khi gọi AI tạo sơ đồ: " + err.message);
            });
    };

    function hienThiLoading(isLoading, text) {
        const loadBox = document.getElementById("sdLoadingBox");
        const board = document.getElementById("sdMindmapContainer");
        const lbl = document.getElementById("lblLoadingText");

        if (lbl && text) lbl.textContent = text;
        if (loadBox) loadBox.style.display = isLoading ? "block" : "none";
        if (board) board.style.display = isLoading ? "none" : "flex";
    }

})();
