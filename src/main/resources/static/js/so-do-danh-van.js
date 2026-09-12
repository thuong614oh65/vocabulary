/**
 * SƠ ĐỒ TƯ DUY QUY LUẬT ĐÁNH VẦN TIẾNG ANH (PHONICS MINDMAP)
 * Tự động tính toán tọa độ tỏa tròn, vẽ đường cong mũi tên SVG
 * Hỗ trợ tự đọc khi di chuột (Hover-to-read) & Icon hướng dẫn phát âm chi tiết
 */

(function () {
    // State toàn cục
    let dsQuyLuat = window.SERVER_DATA_DS_QUY_LUAT || [];
    let quyLuatHienTai = window.SERVER_DATA_QUY_LUAT_HIEN_TAI || null;
    let currentCategoryKey = window.SERVER_DATA_CAT_HIEN_TAI || "NGUYEN_AM_DAC_BIET";
    let danhSachAmNhomHienTai = [];
    let currentView = "CATEGORY_HUB"; // "CATEGORY_HUB" | "SOUND_LIST" | "MINDMAP"
    let audioHienTai = null;
    let hoverTimeout = null;
    let autoPlayTimer = null;
    let dangAutoPlay = false;
    let cheDoViewGrid = false;

    // Metadata 6 nhóm quy luật đánh vần chuẩn
    const CATEGORY_META = {
        "NGUYEN_AM_DAC_BIET": {
            key: "NGUYEN_AM_DAC_BIET",
            name: "Nguyên âm đặc biệt & Biến âm R",
            icon: "🌟",
            color: "#f59e0b",
            bgLight: "#fef3c7",
            badgeBg: "#fef3c7",
            desc: "Các quy tắc biến âm khi nguyên âm đi liền với r, w hoặc phụ âm đặc biệt (w+or, ar, or, er/ir/ur, al/all, wa/qua)."
        },
        "DUOI_TU_HAU_TO": {
            key: "DUOI_TU_HAU_TO",
            name: "Đuôi từ & Hậu tố thông dụng",
            icon: "🏷️",
            color: "#3b82f6",
            bgLight: "#eff6ff",
            badgeBg: "#dbeafe",
            desc: "Các quy tắc phát âm chuẩn xác cho các đuôi hậu tố như -ise/-ize, -tion, -sion, -ture, -cial/-tial."
        },
        "NGUYEN_AM_DOI": {
            key: "NGUYEN_AM_DOI",
            name: "Nguyên âm đôi & Nguyên âm dài",
            icon: "🔤",
            color: "#10b981",
            bgLight: "#ecfdf5",
            badgeBg: "#d1fae5",
            desc: "Tổng hợp các cặp nguyên âm đôi và nguyên âm dài phổ biến nhất như ea, ee, oo, oa, igh, oy/oi, aw/au."
        },
        "PHU_AM_KEP_CAM": {
            key: "PHU_AM_KEP_CAM",
            name: "Phụ âm kép & Phụ âm câm",
            icon: "🤫",
            color: "#8b5cf6",
            bgLight: "#f5f3ff",
            badgeBg: "#ede9fe",
            desc: "Cách đọc các cặp phụ âm ch, sh, th vô thanh, th hữu thanh, ph, và các phụ âm câm kn-, wr-, wh-, -mb."
        },
        "BIEN_AM_C_G": {
            key: "BIEN_AM_C_G",
            name: "Quy tắc biến âm C & G (Mềm / Cứng)",
            icon: "🔀",
            color: "#ec4899",
            bgLight: "#fdf2f8",
            badgeBg: "#fce7f3",
            desc: "Quy luật biến âm sống còn khi chữ C và G đứng trước e, i, y (Soft C /s/ & Soft G /dʒ/)."
        },
        "ALL": {
            key: "ALL",
            name: "Tất cả quy tắc đánh vần",
            icon: "📚",
            color: "#6366f1",
            bgLight: "#eef2ff",
            badgeBg: "#e0e7ff",
            desc: "Kho lưu trữ đầy đủ toàn bộ 30+ quy luật đánh vần tiếng Anh từ cơ bản đến nâng cao."
        }
    };

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

        if (window.phatAmThanh && vanBan) {
            window.phatAmThanh(vanBan, {
                rate: "+0%",
                onEnd: callback,
                onError: callback
            });
            return;
        }

        const url = audioUrl || ("/audio/phat?text=" + encodeURIComponent(vanBan) + "&rate=+0%");
        const audio = new Audio(url);
        audioHienTai = audio;

        audio.onended = function () {
            audioHienTai = null;
            if (typeof callback === "function") callback();
        };

        audio.onerror = function () {
            audioHienTai = null;
            if (typeof callback === "function") callback();
        };

        audio.play().catch(function () {
            audioHienTai = null;
            if (typeof callback === "function") callback();
        });
    }

    // =========================================================
    // 2. ĐIỀU HƯỚNG 3 CẤP MÀN HÌNH (LEVEL 1 -> LEVEL 2 -> LEVEL 3)
    // =========================================================
    function chuyenManHinh(viewName) {
        currentView = viewName;
        const vHub = document.getElementById("viewCategoryHub");
        const vList = document.getElementById("viewSoundListArena");
        const vMindmap = document.getElementById("viewMindmapArena");

        if (vHub) vHub.style.display = (viewName === "CATEGORY_HUB") ? "block" : "none";
        if (vList) vList.style.display = (viewName === "SOUND_LIST") ? "block" : "none";
        if (vMindmap) vMindmap.style.display = (viewName === "MINDMAP") ? "block" : "none";

        window.scrollTo({ top: 0, behavior: "smooth" });
    }

    window.quayLaiBangDanhMuc = function () {
        dungAudio();
        chuyenManHinh("CATEGORY_HUB");
        if (window.history && window.history.replaceState) {
            window.history.replaceState({}, "", "/quy-luat-danh-van/so-do");
        }
    };

    window.quayLaiDanhSachAm = function () {
        dungAudio();
        chuyenManHinh("SOUND_LIST");
        if (window.history && window.history.replaceState && currentCategoryKey) {
            window.history.replaceState({}, "", "/quy-luat-danh-van/so-do?cat=" + encodeURIComponent(currentCategoryKey));
        }
    };

    window.chonNhomTuBangNgoai = function (catKey) {
        currentCategoryKey = catKey;
        hienThiDanhSachAmTheoNhom(catKey);
        chuyenManHinh("SOUND_LIST");
        if (window.history && window.history.replaceState) {
            window.history.replaceState({}, "", "/quy-luat-danh-van/so-do?cat=" + encodeURIComponent(catKey));
        }
    };

    window.moSoDoTuDanhSach = function (ruleId) {
        chuyenManHinh("MINDMAP");
        taiQuyLuat(ruleId);
        if (window.history && window.history.replaceState) {
            window.history.replaceState({}, "", "/quy-luat-danh-van/so-do?id=" + encodeURIComponent(ruleId));
        }
    };

    window.chuyenAmKeTiep = function (step) {
        if (!quyLuatHienTai || !danhSachAmNhomHienTai || danhSachAmNhomHienTai.length === 0) return;
        const currIdx = danhSachAmNhomHienTai.findIndex(function (r) { return r.id === quyLuatHienTai.id; });
        if (currIdx === -1) return;
        const nextIdx = currIdx + step;
        if (nextIdx >= 0 && nextIdx < danhSachAmNhomHienTai.length) {
            taiQuyLuat(danhSachAmNhomHienTai[nextIdx].id);
        }
    };

    window.moHuongDanAmTheoQuyTac = function (ruleId) {
        const found = dsQuyLuat.find(function (r) { return r.id === ruleId; });
        if (found) {
            quyLuatHienTai = found;
            moHuongDanAmTrungTam();
        } else {
            taiQuyLuat(ruleId);
        }
    };

    // =========================================================
    // 3. RENDER BẢNG DANH MỤC Ở NGOÀI (LEVEL 1 - CATEGORY HUB)
    // =========================================================
    function renderCategoryHub() {
        const grid = document.getElementById("categoryGrid");
        if (!grid) return;
        grid.innerHTML = "";

        Object.keys(CATEGORY_META).forEach(function (key) {
            const cat = CATEGORY_META[key];
            const matchingRules = (key === "ALL")
                ? dsQuyLuat
                : dsQuyLuat.filter(function (r) { return r.phanLoai === key; });

            const count = matchingRules.length;
            const previewRules = matchingRules.slice(0, 5);

            const card = document.createElement("div");
            card.className = "sd-cat-card";
            card.style.setProperty("--cat-accent", cat.color);
            card.style.setProperty("--cat-bg-light", cat.bgLight);
            card.style.setProperty("--cat-badge-bg", cat.badgeBg);
            card.onclick = function () {
                chonNhomTuBangNgoai(key);
            };

            let previewHtml = previewRules.map(function (r) {
                return `<span class="sd-cat-preview-pill"><strong>${r.cumChu}</strong> <small>(${r.docLaIpa})</small></span>`;
            }).join("");

            if (matchingRules.length > 5) {
                previewHtml += `<span class="sd-cat-preview-pill text-muted">+${matchingRules.length - 5} nữa...</span>`;
            }

            card.innerHTML = `
                <div>
                    <div class="sd-cat-card-header">
                        <div class="sd-cat-icon-box">${cat.icon}</div>
                        <div class="sd-cat-title-wrap">
                            <h3 class="sd-cat-title">${cat.name}</h3>
                            <span class="sd-cat-badge">${count} quy tắc</span>
                        </div>
                    </div>
                    <p class="sd-cat-desc">${cat.desc}</p>
                    <div class="sd-cat-preview-sounds">
                        ${previewHtml}
                    </div>
                </div>
                <div class="sd-cat-footer-btn">
                    <span>👉 Mở bảng danh sách các âm (${count} âm)</span>
                    <span>➔</span>
                </div>
            `;

            grid.appendChild(card);
        });
    }

    // =========================================================
    // 4. RENDER BẢNG CÁC ÂM CỦA NHÓM (LEVEL 2 - SOUNDS LIST)
    // =========================================================
    function hienThiDanhSachAmTheoNhom(catKey) {
        const cat = CATEGORY_META[catKey] || CATEGORY_META["ALL"];
        currentCategoryKey = cat.key;

        // Cập nhật Breadcrumb & Banner
        const lblCrumb = document.getElementById("lblBreadcrumbGroup");
        if (lblCrumb) lblCrumb.textContent = `${cat.icon} ${cat.name}`;

        const lblIcon = document.getElementById("lblGroupBannerIcon");
        const lblTitle = document.getElementById("lblGroupBannerTitle");
        const lblCount = document.getElementById("lblGroupBannerCount");
        const lblDesc = document.getElementById("lblGroupBannerDesc");

        if (lblIcon) lblIcon.textContent = cat.icon;
        if (lblTitle) lblTitle.textContent = cat.name;
        if (lblDesc) lblDesc.textContent = cat.desc;

        // Lọc danh sách quy luật
        const rules = (catKey === "ALL")
            ? dsQuyLuat
            : dsQuyLuat.filter(function (r) { return r.phanLoai === catKey; });

        danhSachAmNhomHienTai = rules;
        if (lblCount) lblCount.textContent = `${rules.length} quy tắc`;

        // Render danh sách các thẻ âm
        const soundsGrid = document.getElementById("soundsGrid");
        if (!soundsGrid) return;
        soundsGrid.innerHTML = "";

        if (rules.length === 0) {
            soundsGrid.innerHTML = `<div class="col-12 text-center text-muted py-5">
                Chưa có quy tắc nào trong nhóm này.
            </div>`;
            return;
        }

        rules.forEach(function (ql) {
            const card = document.createElement("div");
            card.className = "sd-sound-card";

            const sampleWords = (ql.danhSachTu || []).slice(0, 5);
            let wordsHtml = sampleWords.map(function (w) {
                return `
                    <span class="sd-sc-word-chip" onclick="event.stopPropagation(); phatAmThanh('${w.tu}', '${w.audioUrl || ''}')" title="Bấm để nghe đọc">
                        <strong>${w.tu}</strong> <small class="text-muted">${w.phienAm || ''}</small>
                    </span>
                `;
            }).join("");

            card.innerHTML = `
                <div>
                    <div class="sd-sc-header">
                        <div class="sd-sc-main-sound">
                            <span class="sd-sc-letter">${ql.cumChu}</span>
                            <span class="sd-sc-ipa">${ql.docLaIpa}</span>
                        </div>
                        <div class="sd-sc-actions">
                            <button type="button" class="btn-sc-audio" onclick="event.stopPropagation(); phatAmThanh('${ql.cumChu}', '${ql.audioUrl || ''}')" title="Nghe âm này">🔊</button>
                            <button type="button" class="btn-sc-guide" onclick="event.stopPropagation(); moHuongDanAmTheoQuyTac('${ql.id}')" title="Xem khẩu hình chi tiết">🗣️</button>
                        </div>
                    </div>
                    <h4 class="sd-sc-title">${ql.tieuDeQuyTac || ql.cumChu}</h4>
                    <p class="sd-sc-desc">${ql.moTaQuyTac || ''}</p>
                    
                    <div class="sd-sc-words-wrap">
                        <div class="sd-sc-words-label">Các từ tiêu biểu:</div>
                        <div class="sd-sc-words-list">
                            ${wordsHtml || '<span class="text-muted small">Đang cập nhật...</span>'}
                        </div>
                    </div>
                </div>

                <button type="button" class="btn-sc-open-mindmap" onclick="moSoDoTuDanhSach('${ql.id}')">
                    🎯 Mở sơ đồ tư duy tỏa tròn ➔
                </button>
            `;

            soundsGrid.appendChild(card);
        });
    }

    // =========================================================
    // 5. KHỞI TẠO KHI TẢI TRANG
    // =========================================================
    document.addEventListener("DOMContentLoaded", function () {
        // Tự động chuyển grid nếu màn hình nhỏ (Mobile)
        if (window.innerWidth < 850) {
            cheDoViewGrid = true;
            const board = document.getElementById("sdMindmapContainer");
            if (board) board.classList.add("view-grid");
        }

        function khoiTaoGiaoDien() {
            renderCategoryHub();

            const moBangNgoai = (window.SERVER_DATA_MO_BANG_NGOAI === true);
            const catHienTai = window.SERVER_DATA_CAT_HIEN_TAI || "";
            const idHienTai = window.SERVER_DATA_ID_HIEN_TAI || "";

            if (moBangNgoai && !catHienTai && !idHienTai) {
                // Mặc định vào Bảng danh mục ở ngoài (Level 1)
                chuyenManHinh("CATEGORY_HUB");
            } else if (catHienTai && !idHienTai) {
                // Vào thẳng danh sách các âm của nhóm (Level 2)
                chonNhomTuBangNgoai(catHienTai);
            } else if (idHienTai || quyLuatHienTai) {
                // Vào thẳng Mindmap của âm đó (Level 3)
                const targetId = idHienTai || (quyLuatHienTai ? quyLuatHienTai.id : "");
                chuyenManHinh("MINDMAP");
                taiQuyLuat(targetId);
            } else {
                chuyenManHinh("CATEGORY_HUB");
            }
        }

        // Tải danh sách quy tắc nếu chưa có
        if (!dsQuyLuat || dsQuyLuat.length === 0) {
            fetch("/api/so-do-danh-van/danh-sach")
                .then(function (r) { return r.json(); })
                .then(function (data) {
                    dsQuyLuat = data;
                    khoiTaoGiaoDien();
                })
                .catch(function () {
                    khoiTaoGiaoDien();
                });
        } else {
            khoiTaoGiaoDien();
        }

        // Tự vẽ lại đường nối SVG khi thay đổi kích thước cửa sổ
        window.addEventListener("resize", function () {
            if (!cheDoViewGrid && currentView === "MINDMAP") {
                veCacDuongMuiTenSvg();
            }
        });
    });

    // =========================================================
    // 6. TẢI VÀ HIỂN THỊ CHI TIẾT 1 QUY TẮC
    // =========================================================
    window.taiQuyLuat = function (id) {
        dungAudio();
        dangAutoPlay = false;
        capNhatNutAutoPlay(false);

        // Kiểm tra trong danh sách cục bộ
        const timThay = dsQuyLuat.find(function (item) { return item.id === id; });
        if (timThay && timThay.danhSachTu && timThay.danhSachTu.length > 0) {
            quyLuatHienTai = timThay;
            if (timThay.phanLoai && currentCategoryKey !== timThay.phanLoai) {
                currentCategoryKey = timThay.phanLoai;
                danhSachAmNhomHienTai = dsQuyLuat.filter(function (r) { return r.phanLoai === timThay.phanLoai; });
            }
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
                if (data.phanLoai && currentCategoryKey !== data.phanLoai) {
                    currentCategoryKey = data.phanLoai;
                    danhSachAmNhomHienTai = dsQuyLuat.filter(function (r) { return r.phanLoai === data.phanLoai; });
                }
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

        // Cập nhật Mindmap Top Navigation
        const catMeta = CATEGORY_META[ql.phanLoai] || CATEGORY_META["ALL"];
        const lblNavCat = document.getElementById("lblNavCatBackName");
        if (lblNavCat) lblNavCat.textContent = catMeta ? catMeta.name : "Nhóm";

        const lblStep = document.getElementById("lblStepSoundInfo");
        if (lblStep) lblStep.textContent = `${ql.cumChu} (${ql.docLaIpa})`;

        if (!danhSachAmNhomHienTai || danhSachAmNhomHienTai.length === 0) {
            danhSachAmNhomHienTai = dsQuyLuat.filter(function (r) { return r.phanLoai === ql.phanLoai; });
        }

        const currIdx = danhSachAmNhomHienTai.findIndex(function (r) { return r.id === ql.id; });
        const btnPrev = document.getElementById("btnPrevSound");
        const btnNext = document.getElementById("btnNextSound");
        if (btnPrev) btnPrev.disabled = (currIdx <= 0);
        if (btnNext) btnNext.disabled = (currIdx === -1 || currIdx >= danhSachAmNhomHienTai.length - 1);

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
    window.locTheoNhom = function (cat) {
        chonNhomTuBangNgoai(cat);
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
        moSoDoTuDanhSach(id);
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
            moSoDoTuDanhSach(found.id);
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

        chuyenManHinh("MINDMAP");
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
                    renderCategoryHub();
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
