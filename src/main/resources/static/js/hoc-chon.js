
// =========================================================
// QUẢN LÝ ÂM THANH
// =========================================================

let audioHienTai = null;

// Mỗi lần chọn từ mới sẽ tăng số này.
// Dùng để hủy lượt đọc/đánh vần cũ.
let soLanDoc = 0;


// =========================================================
// KHỞI TẠO
// =========================================================
// HÀM CUỘN VÀO CHÍNH GIỮA MÀN HÌNH
// =========================================================
function cuonVaoGiuaManHinh(element) {
    if (!element) return;
    const row = element.closest("tr") || element;

    document.querySelectorAll("tr.hang-dang-hoc").forEach(function (r) {
        r.classList.remove("hang-dang-hoc");
    });

    if (row.tagName === "TR") {
        row.classList.add("hang-dang-hoc");
    }

    row.scrollIntoView({
        behavior: "smooth",
        block: "center",
        inline: "nearest"
    });
}

document.addEventListener("DOMContentLoaded", function () {
    const allInputs = document.querySelectorAll(".cau-tra-loi");

    allInputs.forEach(function (input) {
        // -------------------------------------------------
        // Khi click / focus vào ô nhập
        // -------------------------------------------------
        input.addEventListener("focus", function () {
            cuonVaoGiuaManHinh(this);

            let tu = this.dataset.tu;
            console.log("Chọn từ:", tu);

            if (!tu) {
                return;
            }

            // Tạo lượt đọc mới
            soLanDoc++;
            let maDoc = soLanDoc;

            // Dừng âm thanh cũ
            dungTatCaAmThanh();

            // Bắt đầu đọc từ
            batDauDocTu(tu, maDoc);
        });

        // -------------------------------------------------
        // Máy tính: nhấn ENTER để chấm
        // -------------------------------------------------
        input.addEventListener("keydown", function (e) {
            if (e.key === "Enter") {
                e.preventDefault();
                chamDiem(this);
            }
        });
    });

    // Tự động focus và cuộn hàng đầu tiên vào giữa màn hình khi mới vào trang
    if (allInputs.length > 0) {
        setTimeout(function () {
            allInputs[0].focus();
            cuonVaoGiuaManHinh(allInputs[0]);
        }, 300);
    }
});


// =========================================================
// DỪNG TẤT CẢ ÂM THANH
// =========================================================

function dungTatCaAmThanh() {


    // -------------------------------------------------
    // Dừng SpeechSynthesis
    // -------------------------------------------------

    if (
        window.speechSynthesis
    ) {

        window.speechSynthesis.cancel();

    }


    // -------------------------------------------------
    // Dừng file MP3 hiện tại
    // -------------------------------------------------

    if (audioHienTai) {

        audioHienTai.pause();

        audioHienTai.currentTime = 0;

        audioHienTai = null;

    }

}


// =========================================================
// BẮT ĐẦU ĐỌC TỪ BẰNG MP3
// =========================================================

function batDauDocTu(
    tu,
    maDoc
) {

    if (!tu) {
        return;
    }

    console.log(
        "ĐỌC TỪ:",
        tu
    );

    // -------------------------------------------------
    // Tạo tên file MP3
    // -------------------------------------------------

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

    // -------------------------------------------------
    // Dừng tất cả âm thanh cũ
    // -------------------------------------------------

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
            utterance.onend = function () {
                if (maDoc === soLanDoc) {
                    danhVan(tu, maDoc);
                }
            };
            utterance.onerror = function () {
                if (maDoc === soLanDoc) {
                    danhVan(tu, maDoc);
                }
            };
            window.speechSynthesis.speak(utterance);
        } else {
            if (maDoc === soLanDoc) {
                danhVan(tu, maDoc);
            }
        }
    }

    // -------------------------------------------------
    // Tạo MP3 mới
    // -------------------------------------------------

    audioHienTai =
        new Audio(duongDan);

    // -------------------------------------------------
    // Khi bắt đầu đọc
    // -------------------------------------------------

    audioHienTai.onplay =
        function () {
            console.log(
                "BẮT ĐẦU ĐỌC:",
                tu
            );
        };

    // -------------------------------------------------
    // Đọc xong cả từ
    // -------------------------------------------------

    audioHienTai.onended =
        function () {
            console.log(
                "ĐỌC XONG:",
                tu
            );

            // Nếu người dùng đã chuyển sang
            // từ khác thì bỏ lượt này
            if (
                maDoc !== soLanDoc
            ) {
                return;
            }

            // Đọc xong từ mới bắt đầu đánh vần
            danhVan(
                tu,
                maDoc
            );
        };

    // -------------------------------------------------
    // Lỗi MP3 -> Chuyển sang giọng đọc trình duyệt
    // -------------------------------------------------

    audioHienTai.onerror =
        function (e) {
            console.warn(
                "LỖI ĐỌC MP3:",
                duongDan,
                "- Chuyển sang SpeechSynthesis."
            );
            fallbackSpeech();
        };

    // -------------------------------------------------
    // Phát MP3
    // -------------------------------------------------

    audioHienTai
        .play()
        .catch(
            function (error) {
                console.warn(
                    "KHÔNG THỂ PHÁT MP3:",
                    error,
                    "- Chuyển sang SpeechSynthesis."
                );
                fallbackSpeech();
            }
        );

}


// =========================================================
// HÀM docTu() CHO HTML GỌI
// =========================================================
//
// Ví dụ HTML:
//
// onclick="docTu('apple')"
//
// =========================================================

function docTu(tu) {


    if (!tu) {

        return;

    }


    // Tạo lượt đọc mới

    soLanDoc++;


    let maDoc =
        soLanDoc;


    // Dừng tất cả âm thanh cũ

    dungTatCaAmThanh();


    // Đọc từ bằng MP3

    batDauDocTu(
        tu,
        maDoc
    );

}


// =========================================================
// ĐƯA HÀM RA GLOBAL
// Để onclick trong HTML sử dụng được
// =========================================================

window.docTu =
    docTu;

// =========================================================
// ĐÁNH VẦN BẰNG FILE MP3
// =========================================================

async function danhVan(
    tu,
    maDoc
) {


    if (!tu) {

        return;

    }


    // Kiểm tra lượt đọc hiện tại

    if (
        maDoc !== soLanDoc
    ) {

        return;

    }


    console.log(
        "BẮT ĐẦU ĐÁNH VẦN:",
        tu
    );


    // -------------------------------------------------
    // Chuyển từ thành chữ in hoa
    // -------------------------------------------------

    let chuoi =
        tu.toUpperCase();


    // Chỉ lấy A-Z

    let cacChuCai =
        chuoi.match(/[A-Z]/g);


    if (!cacChuCai) {

        return;

    }


    // -------------------------------------------------
    // Đọc từng chữ
    // -------------------------------------------------

    for (
        let i = 0;
        i < cacChuCai.length;
        i++
    ) {


        // Nếu đã chuyển sang từ khác
        // thì dừng ngay

        if (
            maDoc !== soLanDoc
        ) {

            console.log(
                "Đã hủy đánh vần:",
                tu
            );

            return;

        }


        let chuCai =
            cacChuCai[i];


        console.log(
            "CHỮ:",
            chuCai
        );


        await phatChuCai(
            chuCai,
            maDoc
        );


        // Kiểm tra lại sau khi phát

        if (
            maDoc !== soLanDoc
        ) {

            return;

        }

    }


    console.log(
        "ĐÁNH VẦN XONG:",
        tu
    );

}


// =========================================================
// PHÁT 1 FILE MP3 CHỮ CÁI
// =========================================================

function phatChuCai(
    chuCai,
    maDoc
) {

    return new Promise(
        function (resolve) {


            // Nếu đã chuyển từ khác

            if (
                maDoc !== soLanDoc
            ) {

                resolve();

                return;

            }


            // -------------------------------------------------
            // Tên file
            // -------------------------------------------------

            let tenFile =
                chuCai.toLowerCase() +
                ".mp3";


            // -------------------------------------------------
            // Đường dẫn Spring Boot
            // -------------------------------------------------

            let duongDan =
                "/audio/alphabet/" +
                tenFile;


            console.log(
                "PHÁT MP3:",
                duongDan
            );


            // -------------------------------------------------
            // Tạo Audio
            // -------------------------------------------------

            let audio =
                new Audio(duongDan);


            audio.volume =
                1;

            audio.preload =
                "auto";

            audio.playbackRate =
                1.3;

            // Lưu audio hiện tại

            audioHienTai =
                audio;


            // -------------------------------------------------
            // Phát xong
            // -------------------------------------------------

            audio.onended =
                function () {


                    console.log(
                        "XONG:",
                        duongDan
                    );


                    if (
                        audioHienTai === audio
                    ) {

                        audioHienTai =
                            null;

                    }


                    resolve();

                };


            // -------------------------------------------------
            // Lỗi MP3
            // -------------------------------------------------

            audio.onerror =
                function (e) {


                    console.error(
                        "LỖI MP3:",
                        duongDan,
                        e
                    );


                    if (
                        audioHienTai === audio
                    ) {

                        audioHienTai =
                            null;

                    }


                    resolve();

                };


            // -------------------------------------------------
            // Phát
            // -------------------------------------------------

            audio.play()
                .then(
                    function () {

                        console.log(
                            "ĐANG PHÁT:",
                            duongDan
                        );

                    }
                )
                .catch(
                    function (error) {

                        console.error(
                            "KHÔNG PHÁT ĐƯỢC:",
                            duongDan,
                            error
                        );


                        if (
                            audioHienTai === audio
                        ) {

                            audioHienTai =
                                null;

                        }


                        resolve();

                    }
                );

        }
    );

}


// =========================================================
// HÀM CHỜ
// =========================================================

function cho(ms) {

    return new Promise(
        function (resolve) {

            setTimeout(
                resolve,
                ms
            );

        }
    );

}


// =========================================================
// NÚT ✓ TRÊN ĐIỆN THOẠI
// =========================================================

function chamDiemTuNut(button) {


    // Lấy dòng <tr>

    let row =
        button.closest("tr");


    // Tìm ô nhập

    let input =
        row.querySelector(
            ".cau-tra-loi"
        );


    if (!input) {

        return;

    }


    chamDiem(input);

}


// =========================================================
// HÀM CHUẨN HÓA TIẾNG VIỆT & BỎ DẤU
// =========================================================
function boDauTiengViet(str) {
    if (!str) return "";
    return str
        .toString()
        .toLowerCase()
        .replace(/à|á|ạ|ả|ã|â|ầ|ấ|ậ|ẩ|ẫ|ă|ằ|ắ|ặ|ẳ|ẵ/g, "a")
        .replace(/è|é|ẹ|ẻ|ẽ|ê|ề|ế|ệ|ể|ễ/g, "e")
        .replace(/ì|í|ị|ỉ|ĩ/g, "i")
        .replace(/ò|ó|ọ|ỏ|õ|ô|ồ|ố|ộ|ổ|ỗ|ơ|ờ|ớ|ợ|ở|ỡ/g, "o")
        .replace(/ù|ú|ụ|ủ|ũ|ư|ừ|ứ|ự|ử|ữ/g, "u")
        .replace(/ỳ|ý|ỵ|ỷ|ỹ/g, "y")
        .replace(/đ/g, "d")
        .replace(/\u0300|\u0301|\u0303|\u0309|\u0323/g, "")
        .replace(/\u02C6|\u0306|\u031B/g, "")
        .replace(/[^\w\s]/g, " ")
        .replace(/\s+/g, " ")
        .trim();
}

// Bảng regex chuyển đổi số chữ sang chữ số trong toàn bộ cụm từ (ví dụ: "tháng bảy" -> "thang 7", "thứ hai" -> "thu 2")
const DANH_SACH_SO_CHU_REGEX = [
    [/\b(mot\s+ty|mot\s+ti|ty|ti)\b/g, '1000000000'],
    [/\b(mot\s+trieu|trieu)\b/g, '1000000'],
    [/\b(mot\s+nghin|mot\s+ngan|nghin|ngan)\b/g, '1000'],
    [/\b(mot\s+tram|tram)\b/g, '100'],
    [/\b(chin\s+muoi)\b/g, '90'],
    [/\b(tam\s+muoi)\b/g, '80'],
    [/\b(bay\s+muoi|bay\s+muoi)\b/g, '70'],
    [/\b(sau\s+muoi)\b/g, '60'],
    [/\b(nam\s+muoi)\b/g, '50'],
    [/\b(bon\s+muoi)\b/g, '40'],
    [/\b(ba\s+muoi)\b/g, '30'],
    [/\b(hai\s+muoi\s+chin)\b/g, '29'],
    [/\b(hai\s+muoi\s+tam)\b/g, '28'],
    [/\b(hai\s+muoi\s+bay|hai\s+muoi\s+bay)\b/g, '27'],
    [/\b(hai\s+muoi\s+sau)\b/g, '26'],
    [/\b(hai\s+muoi\s+lam|hai\s+muoi\s+nam)\b/g, '25'],
    [/\b(hai\s+muoi\s+bon|hai\s+muoi\s+tu)\b/g, '24'],
    [/\b(hai\s+muoi\s+ba)\b/g, '23'],
    [/\b(hai\s+muoi\s+hai)\b/g, '22'],
    [/\b(hai\s+muoi\s+mot|hai\s+muoi\s+mot)\b/g, '21'],
    [/\b(hai\s+muoi)\b/g, '20'],
    [/\b(muoi\s+chin)\b/g, '19'],
    [/\b(muoi\s+tam)\b/g, '18'],
    [/\b(muoi\s+bay|muoi\s+bay)\b/g, '17'],
    [/\b(muoi\s+sau)\b/g, '16'],
    [/\b(muoi\s+lam|muoi\s+nam)\b/g, '15'],
    [/\b(muoi\s+bon|muoi\s+tu)\b/g, '14'],
    [/\b(muoi\s+ba)\b/g, '13'],
    [/\b(muoi\s+hai|thang\s+chap)\b/g, '12'],
    [/\b(muoi\s+mot)\b/g, '11'],
    [/\b(muoi|chuc)\b/g, '10'],
    [/\b(chin)\b/g, '9'],
    [/\b(tam)\b/g, '8'],
    [/\b(bay|bay)\b/g, '7'],
    [/\b(sau)\b/g, '6'],
    [/\b(nam|lam)\b/g, '5'],
    [/\b(bon|tu)\b/g, '4'],
    [/\b(ba)\b/g, '3'],
    [/\b(hai|nhi)\b/g, '2'],
    [/\b(mot|mot|gieng|nhat)\b/g, '1'],
    [/\b(khong)\b/g, '0']
];

function chuanHoaSoTrongChuoi(s) {
    if (!s) return "";
    let norm = boDauTiengViet(s);
    for (let [regex, rep] of DANH_SACH_SO_CHU_REGEX) {
        norm = norm.replace(regex, rep);
    }
    return norm.replace(/\s+/g, " ").trim();
}

// Tách đáp án tiếng Việt thành các nghĩa riêng lẻ (theo dấu phẩy, chấm phẩy, gạch chéo, ngoặc đơn)
function tachDanhSachNghiaTiengViet(dapAnRaw) {
    if (!dapAnRaw) return [];
    let parts = dapAnRaw.split(/[,;/|\n\r]+/);
    let danhSach = [];
    
    parts.forEach(p => {
        let trimmed = p.trim();
        if (!trimmed) return;
        danhSach.push(trimmed);

        // Nếu có ngoặc đơn e.g. "xe hơi (ô tô)" -> thêm cả "xe hơi" và "ô tô"
        let matches = trimmed.match(/\(([^)]+)\)/g);
        if (matches) {
            matches.forEach(m => {
                let inside = m.replace(/[()]/g, "").trim();
                if (inside) danhSach.push(inside);
            });
            let outside = trimmed.replace(/\([^)]*\)/g, "").trim();
            if (outside && outside !== trimmed) {
                danhSach.push(outside);
            }
        }
    });

    return Array.from(new Set(danhSach.filter(s => s.length > 0)));
}

// Loại bỏ từ chỉ loại / mạo từ tiếng Việt (ví dụ: quả táo -> táo, con mèo -> mèo, cái bàn -> bàn...)
const TU_CHI_LOAI_TIENG_VIET = [
    "qua", "trai", "con", "cai", "chiec", "nguoi", "cay", "bong", "cu", "hat",
    "vi", "su", "viec", "tinh", "cuoc", "mot", "nhung", "cac", "loai", "giong",
    "thu", "bup", "doa", "buc", "to", "cuon", "quyen", "hop", "goi", "can", "toa", "dan", "bay"
];

function loaiBoTuChiLoai(str) {
    if (!str) return "";
    let words = str.trim().split(/\s+/);
    while (words.length > 1 && TU_CHI_LOAI_TIENG_VIET.includes(words[0])) {
        words.shift();
    }
    return words.join(" ");
}

// Tính khoảng cách Levenshtein kiểm tra sai lệch nhỏ
function tinhKhoangCachLevenshtein(s1, s2) {
    if (!s1 || !s2) return Math.max(s1 ? s1.length : 0, s2 ? s2.length : 0);
    const m = s1.length, n = s2.length;
    const dp = Array.from({ length: m + 1 }, () => Array(n + 1).fill(0));
    for (let i = 0; i <= m; i++) dp[i][0] = i;
    for (let j = 0; j <= n; j++) dp[0][j] = j;

    for (let i = 1; i <= m; i++) {
        for (let j = 1; j <= n; j++) {
            if (s1[i - 1] === s2[j - 1]) {
                dp[i][j] = dp[i - 1][j - 1];
            } else {
                dp[i][j] = Math.min(
                    dp[i - 1][j] + 1,     // deletion
                    dp[i][j - 1] + 1,     // insertion
                    dp[i - 1][j - 1] + 1  // substitution
                );
            }
        }
    }
    return dp[m][n];
}

// Kiểm tra đáp án tiếng Việt (chấm dễ, số/chữ tương đương trong cả cụm từ như "tháng 7" <-> "tháng bảy", không dấu, gần đúng, bỏ từ chỉ loại như quả táo -> táo)
function kiemTraTiengVietDung(nhapRaw, dapAnRaw) {
    if (!nhapRaw || !nhapRaw.trim()) return false;
    if (!dapAnRaw || !dapAnRaw.trim()) return false;

    const danhSachNghia = tachDanhSachNghiaTiengViet(dapAnRaw);

    const nhapChuan = boDauTiengViet(nhapRaw);
    const nhapCanon = chuanHoaSoTrongChuoi(nhapRaw);
    const nhapNoCls = loaiBoTuChiLoai(nhapCanon);

    for (let nghia of danhSachNghia) {
        const nghiaChuan = boDauTiengViet(nghia);
        const nghiaCanon = chuanHoaSoTrongChuoi(nghia);
        const nghiaNoCls = loaiBoTuChiLoai(nghiaCanon);

        // 1. So khớp trực tiếp (bỏ dấu hoặc chuẩn hóa số: tháng 7 <-> tháng bảy)
        if (nhapChuan === nghiaChuan || nhapCanon === nghiaCanon || nhapNoCls === nghiaNoCls) {
            return true;
        }
        if (nhapCanon === nghiaNoCls || nhapNoCls === nghiaCanon) {
            return true;
        }

        // 2. So khớp loại bỏ từ chỉ loại (ví dụ: "quả táo" <-> "táo", "con mèo" <-> "mèo", "chiếc xe" <-> "xe")
        const nhapGoc = loaiBoTuChiLoai(nhapChuan);
        const nghiaGoc = loaiBoTuChiLoai(nghiaChuan);

        if (nhapGoc === nghiaGoc || nhapChuan === nghiaGoc || nhapGoc === nghiaChuan) {
            return true;
        }

        // 3. So khớp bao hàm từ khóa / từ cốt lõi (ví dụ: "táo" trong "quả táo", "chạy" trong "chạy bộ", "học" trong "học tập")
        const wordsNhap = nhapNoCls.split(/\s+/).filter(w => w.length > 0);
        const wordsNghia = nghiaNoCls.split(/\s+/).filter(w => w.length > 0);

        // Nếu người dùng nhập tập con các từ trong nghĩa hoặc ngược lại
        const isNhapSubset = wordsNhap.length > 0 && wordsNhap.every(w => wordsNghia.includes(w));
        const isNghiaSubset = wordsNghia.length > 0 && wordsNghia.every(w => wordsNhap.includes(w));
        if (isNhapSubset || isNghiaSubset) {
            return true;
        }

        // 4. Nếu một chuỗi là chuỗi con của chuỗi kia (độ dài >= 2 ký tự)
        if (nhapNoCls.length >= 2 && (nghiaNoCls.includes(nhapNoCls) || nhapNoCls.includes(nghiaNoCls))) {
            return true;
        }
        if (nhapGoc.length >= 2 && (nghiaGoc.includes(nhapGoc) || nhapGoc.includes(nghiaGoc))) {
            return true;
        }

        // 5. Kiểm tra lỗi gõ phím nhỏ (Fuzzy matching Levenshtein)
        const maxLen = Math.max(nhapGoc.length, nghiaGoc.length);
        if (maxLen >= 4) {
            const dist = tinhKhoangCachLevenshtein(nhapGoc, nghiaGoc);
            if (dist <= 1) {
                return true;
            }
            if (maxLen >= 8 && dist <= 2) {
                return true;
            }
        }
    }

    return false;
}

// =========================================================
// CHẤM ĐIỂM TIẾNG VIỆT (HOC-CHON)
// =========================================================
function chamDiem(input) {
    if (input.disabled) {
        return;
    }

    let dapAnRaw = input.dataset.dapAn || "";
    if (!dapAnRaw) {
        return;
    }

    let nhap = input.value.trim();
    let dung = kiemTraTiengVietDung(nhap, dapAnRaw);

    const containerKetQua = input.parentElement.querySelector(".ket-qua");

    if (dung) {
        input.classList.add("input-dung");
        input.classList.remove("input-sai");

        if (containerKetQua) {
            containerKetQua.innerHTML = `
                <div class="ket-qua-box kq-dung">
                    <span class="fw-bold text-success">✅ Chính xác!</span>
                </div>
            `;
        }

        fetch("/hoc/dung/" + input.dataset.id, { method: "POST" });
    } else {
        input.classList.add("input-sai");
        input.classList.remove("input-dung");

        if (containerKetQua) {
            containerKetQua.innerHTML = `
                <div class="ket-qua-box kq-sai">
                    <div class="mb-1">
                        <small class="text-muted">Bạn nhập:</small>
                        <span class="ms-1 fw-bold text-danger">${nhap || '(chưa nhập)'}</span>
                    </div>
                    <div>
                        <small class="text-muted">Đáp án đúng:</small>
                        <span class="dap-an-chuan-badge">${dapAnRaw}</span>
                    </div>
                </div>
            `;
        }

        fetch("/hoc/sai/" + input.dataset.id, { method: "POST" });
    }

    input.disabled = true;

    // Tìm ô tiếp theo
    setTimeout(function () {
        let inputs = document.querySelectorAll(".cau-tra-loi");
        let viTri = Array.from(inputs).indexOf(input);
        if (inputs[viTri + 1]) {
            inputs[viTri + 1].focus();
            cuonVaoGiuaManHinh(inputs[viTri + 1]);
        }
    }, 800);
}


// =========================================================
// TIẾP LƯỢT
// =========================================================

function tiepLuot() {


    // Dừng âm thanh

    soLanDoc++;


    dungTatCaAmThanh();


    window.location =
        "/hoc/tiep";

}


// =========================================================
// DỪNG HỌC
// =========================================================

function dungHoc() {


    // Dừng âm thanh

    soLanDoc++;


    dungTatCaAmThanh();


    window.location =
        "/hoc/dung";

}