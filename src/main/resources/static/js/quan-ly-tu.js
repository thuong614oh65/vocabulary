let audioHienTai = null;
let soLanDoc = 0;

// =========================================================
// DỪNG TẤT CẢ ÂM THANH
// =========================================================
function dungTatCaAmThanh() {
    if (window.speechSynthesis) {
        window.speechSynthesis.cancel();
    }

    if (audioHienTai) {
        audioHienTai.pause();
        audioHienTai.currentTime = 0;
        audioHienTai = null;
    }
}

// =========================================================
// BẮT ĐẦU ĐỌC TỪ BẰNG MP3 (GIỐNG PHẦN HỌC)
// =========================================================
function batDauDocTu(tu, maDoc) {
    if (!tu) {
        return;
    }

    console.log(
        "ĐỌC TỪ:",
        tu
    );

    let tenFile =
        tu
            .toLowerCase()
            .trim()
            .replace(/[\\/:*?"<>|]/g, "")
            .split(/\s+/)
            .join("-");

    let duongDan =
        "/audio/tu-vung/"
        + tenFile
        + ".mp3";

    // Dừng audio cũ
    dungTatCaAmThanh();

    let daFallback = false;
    function fallbackSpeech() {
        if (daFallback) return;
        daFallback = true;
        if (window.speechSynthesis) {
            console.log("Dùng giọng đọc trình duyệt (SpeechSynthesis) cho từ:", tu);
            let utterance = new SpeechSynthesisUtterance(tu);
            utterance.lang = 'en-US';
            utterance.rate = 0.9;
            window.speechSynthesis.speak(utterance);
        }
    }

    // Tạo audio mới
    audioHienTai = new Audio(duongDan);

    audioHienTai.onplay = function () {
        console.log(
            "BẮT ĐẦU ĐỌC:",
            tu
        );
    };

    audioHienTai.onended = function () {
        console.log(
            "ĐỌC XONG:",
            tu
        );
    };

    audioHienTai.onerror = function (e) {
        console.warn(
            "LỖI ĐỌC MP3:",
            duongDan,
            "- Chuyển sang giọng đọc trình duyệt."
        );
        fallbackSpeech();
    };

    audioHienTai
        .play()
        .catch(function (error) {
            console.warn(
                "KHÔNG THỂ PHÁT MP3:",
                error,
                "- Chuyển sang giọng đọc trình duyệt."
            );
            fallbackSpeech();
        });
}

// =========================================================
// HÀM docTu() CHO HTML GỌI
// =========================================================
function docTu(tu) {
    if (!tu) {
        return;
    }

    soLanDoc++;
    let maDoc = soLanDoc;

    batDauDocTu(tu, maDoc);
}

// =========================================================
// MỞ MODAL SỬA TỪ VỰNG
// =========================================================
function moModalSua(id, tiengAnh, tiengViet, phienAm, viDu, boId) {
    document.getElementById("modalTuId").value = id;
    document.getElementById("modalTiengAnh").value = tiengAnh || "";
    document.getElementById("modalTiengViet").value = tiengViet || "";
    document.getElementById("modalPhienAm").value = phienAm || "";
    document.getElementById("modalViDu").value = viDu || "";

    let boSelect = document.getElementById("modalBoId");
    if (boSelect && boId) {
        boSelect.value = boId;
    }

    let editModal = new bootstrap.Modal(document.getElementById("modalSuaTu"));
    editModal.show();
}

// =========================================================
// XÁC NHẬN VÀ XÓA TỪ VỰNG
// =========================================================
function xacNhanXoa(id, tiengAnh) {
    if (confirm("Bạn có chắc chắn muốn xóa từ \"" + tiengAnh + "\" không?")) {
        let formXoa = document.getElementById("formXoaTu");
        if (formXoa) {
            let boIdLoc = document.getElementById("boIdLocHienTai") ? document.getElementById("boIdLocHienTai").value : "";
            formXoa.action = "/quan-ly-tu/xoa/" + id + (boIdLoc ? "?boIdLoc=" + boIdLoc : "");
            formXoa.submit();
        }
    }
}

// =========================================================
// XÁC NHẬN VÀ XÓA TOÀN BỘ BỘ TỪ
// =========================================================
function xacNhanXoaBo(boId, tenBo, soLuongTu) {
    let thongBao = "⚠️ BẠN CÓ CHẮC CHẮN MUỐN XÓA BỘ TỪ: \"" + tenBo + "\" KHÔNG?\n\n"
        + "• Tất cả " + (soLuongTu || 0) + " từ vựng bên trong bộ từ này cũng sẽ bị xóa vĩnh viễn!\n"
        + "• Hành động này không thể hoàn tác!";

    if (confirm(thongBao)) {
        let formXoaBo = document.getElementById("formXoaBo");
        if (formXoaBo) {
            formXoaBo.action = "/quan-ly-tu/xoa-bo/" + boId;
            formXoaBo.submit();
        }
    }
}


// =========================================================
// TÌM KIẾM TỪ KHÓA TRÊN BẢNG
// =========================================================
function timKiemTu() {
    let input = document.getElementById("inputTimKiem");
    let filter = input.value.toLowerCase().trim();
    let rows = document.querySelectorAll("#bangTuVung tbody tr.hang-tu");
    let dem = 0;

    if (filter === "") {
        rows.forEach(function (row) {
            row.style.display = "";
            let tempTd = row.querySelector(".td-bo-tam");
            if (tempTd) {
                tempTd.remove();
            }
            let originalTd = row.querySelector(".td-bo-goc");
            if (originalTd) {
                let oldRowspan = originalTd.getAttribute("data-old-rowspan");
                if (oldRowspan) {
                    originalTd.setAttribute("rowspan", oldRowspan);
                }
                originalTd.style.display = "";
            }
            dem++;
        });
    } else {
        rows.forEach(function (row) {
            let tiengAnh = row.getAttribute("data-tieng-anh") || "";
            let tiengViet = row.getAttribute("data-tieng-viet") || "";
            let tenBo = row.getAttribute("data-ten-bo") || "";

            if (tiengAnh.includes(filter) || tiengViet.includes(filter) || tenBo.includes(filter)) {
                row.style.display = "";
                dem++;

                let originalTd = row.querySelector(".td-bo-goc");
                if (!originalTd) {
                    if (!row.querySelector(".td-bo-tam")) {
                        let tdTam = document.createElement("td");
                        tdTam.className = "text-center fw-bold align-middle bg-light td-bo-tam";
                        tdTam.textContent = row.getAttribute("data-ten-bo-text") || "";
                        let sttTd = row.children[0];
                        if (sttTd) {
                            row.insertBefore(tdTam, sttTd.nextSibling);
                        }
                    }
                } else {
                    if (!originalTd.hasAttribute("data-old-rowspan")) {
                        originalTd.setAttribute("data-old-rowspan", originalTd.getAttribute("rowspan") || "1");
                    }
                    originalTd.setAttribute("rowspan", "1");
                    originalTd.style.display = "";
                }
            } else {
                row.style.display = "none";
            }
        });
    }

    let emptyMessage = document.getElementById("khongCoKetQua");
    if (emptyMessage) {
        emptyMessage.style.display = (dem === 0 && rows.length > 0) ? "block" : "none";
    }

    let demSpan = document.getElementById("soTuHienThi");
    if (demSpan) {
        demSpan.textContent = dem;
    }
}

// =========================================================
// CHỌN BỘ ĐỂ LỌC
// =========================================================
function doiBoLoc() {
    let boSelect = document.getElementById("selectBoLoc");
    if (boSelect) {
        let boId = boSelect.value;
        if (boId) {
            window.location.href = "/quan-ly-tu?boId=" + boId;
        } else {
            window.location.href = "/quan-ly-tu";
        }
    }
}

window.docTu = docTu;
window.moModalSua = moModalSua;
window.xacNhanXoa = xacNhanXoa;
window.xacNhanXoaBo = xacNhanXoaBo;
window.timKiemTu = timKiemTu;
window.doiBoLoc = doiBoLoc;
