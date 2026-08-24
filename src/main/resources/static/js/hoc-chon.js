
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

document.addEventListener("DOMContentLoaded", function () {

    document
        .querySelectorAll(".cau-tra-loi")
        .forEach(function (input) {


            // -------------------------------------------------
            // Khi click / focus vào ô nhập
            // -------------------------------------------------

            input.addEventListener(
                "focus",
                function () {

                    let tu = this.dataset.tu;


                    console.log(
                        "Chọn từ:",
                        tu
                    );


                    if (!tu) {

                        return;

                    }


                    // Tạo lượt đọc mới

                    soLanDoc++;


                    let maDoc =
                        soLanDoc;


                    // Dừng âm thanh cũ

                    dungTatCaAmThanh();


                    // Bắt đầu đọc từ

                    batDauDocTu(
                        tu,
                        maDoc
                    );

                }
            );


            // -------------------------------------------------
            // Máy tính: nhấn ENTER để chấm
            // -------------------------------------------------

            input.addEventListener(
                "keydown",
                function (e) {

                    if (e.key === "Enter") {

                        e.preventDefault();

                        chamDiem(this);

                    }

                }
            );

        });

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

// Bảng ánh xạ số và chữ số tiếng Việt thông dụng
const BANG_SO_TIENG_VIET = {
    "0": ["0", "khong", "so khong"],
    "1": ["1", "mot", "so 1", "so mot", "1st", "nhat", "thu nhat", "thu 1", "dau tien"],
    "2": ["2", "hai", "so 2", "so hai", "2nd", "nhi", "thu hai", "thu 2"],
    "3": ["3", "ba", "so 3", "so ba", "3rd", "tam", "thu ba", "thu 3"],
    "4": ["4", "bon", "tu", "so 4", "so bon", "4th", "thu tu", "thu 4", "thu bon"],
    "5": ["5", "nam", "lam", "so 5", "so nam", "5th", "thu nam", "thu 5", "thu lam"],
    "6": ["6", "sau", "so 6", "so sau", "6th", "thu sau", "thu 6"],
    "7": ["7", "bay", "so 7", "so bay", "7th", "thu bay", "thu 7"],
    "8": ["8", "tam", "so 8", "so tam", "8th", "thu tam", "thu 8"],
    "9": ["9", "chin", "so 9", "so chin", "9th", "thu chin", "thu 9"],
    "10": ["10", "muoi", "so 10", "so muoi", "chuc", "10th", "thu muoi", "thu 10"],
    "11": ["11", "muoi mot", "so 11", "11th", "thu muoi mot", "thu 11"],
    "12": ["12", "muoi hai", "so 12", "12th", "thu muoi hai", "thu 12"],
    "13": ["13", "muoi ba", "so 13", "13th", "thu muoi ba", "thu 13"],
    "14": ["14", "muoi bon", "muoi tu", "so 14", "14th", "thu muoi bon", "thu muoi tu", "thu 14"],
    "15": ["15", "muoi lam", "muoi nam", "so 15", "15th", "thu muoi lam", "thu 15"],
    "16": ["16", "muoi sau", "so 16", "16th", "thu muoi sau", "thu 16"],
    "17": ["17", "muoi bay", "so 17", "17th", "thu muoi bay", "thu 17"],
    "18": ["18", "muoi tam", "so 18", "18th", "thu muoi tam", "thu 18"],
    "19": ["19", "muoi chin", "so 19", "19th", "thu muoi chin", "thu 19"],
    "20": ["20", "hai muoi", "so 20", "20th", "thu hai muoi", "thu 20"],
    "21": ["21", "hai muoi mot", "so 21", "21st", "thu hai muoi mot", "thu 21"],
    "22": ["22", "hai muoi hai", "so 22", "22nd", "thu hai muoi hai", "thu 22"],
    "23": ["23", "hai muoi ba", "so 23", "23rd", "thu hai muoi ba", "thu 23"],
    "24": ["24", "hai muoi bon", "hai muoi tu", "so 24", "24th", "thu hai muoi bon", "thu 24"],
    "25": ["25", "hai muoi lam", "hai muoi nam", "so 25", "25th", "thu hai muoi lam", "thu 25"],
    "26": ["26", "hai muoi sau", "so 26", "26th", "thu hai muoi sau", "thu 26"],
    "27": ["27", "hai muoi bay", "so 27", "27th", "thu hai muoi bay", "thu 27"],
    "28": ["28", "hai muoi tam", "so 28", "28th", "thu hai muoi tam", "thu 28"],
    "29": ["29", "hai muoi chin", "so 29", "29th", "thu hai muoi chin", "thu 29"],
    "30": ["30", "ba muoi", "so 30", "30th", "thu ba muoi", "thu 30"],
    "40": ["40", "bon muoi", "so 40", "40th", "thu bon muoi", "thu 40"],
    "50": ["50", "nam muoi", "so 50", "50th", "thu nam muoi", "thu 50"],
    "60": ["60", "sau muoi", "so 60", "60th", "thu sau muoi", "thu 60"],
    "70": ["70", "bay muoi", "so 70", "70th", "thu bay muoi", "thu 70"],
    "80": ["80", "tam muoi", "so 80", "80th", "thu tam muoi", "thu 80"],
    "90": ["90", "chin muoi", "so 90", "90th", "thu chin muoi", "thu 90"],
    "100": ["100", "mot tram", "tram", "so 100", "100th", "thu mot tram", "thu tram", "thu 100"],
    "1000": ["1000", "1 000", "1.000", "1,000", "mot nghin", "mot ngan", "nghin", "ngan", "1000th", "thu mot nghin", "thu 1000"],
    "1000000": ["1000000", "1 000 000", "1.000.000", "1,000,000", "mot trieu", "trieu", "1000000th", "thu mot trieu", "thu 1000000"],
    "1000000000": ["1000000000", "1 000 000 000", "1.000.000.000", "1,000,000,000", "mot ty", "mot ti", "ty", "ti", "1000000000th", "thu mot ty", "thu 1000000000"]
};

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

// Kiểm tra đáp án tiếng Việt (chấm dễ, số/chữ tương đương, không dấu vẫn đúng, đúng 1 nghĩa là đúng)
function kiemTraTiengVietDung(nhapRaw, dapAnRaw) {
    if (!nhapRaw || !nhapRaw.trim()) return false;
    if (!dapAnRaw || !dapAnRaw.trim()) return false;

    const nhapChuan = boDauTiengViet(nhapRaw);
    if (!nhapChuan) return false;

    const danhSachNghia = tachDanhSachNghiaTiengViet(dapAnRaw);

    for (let nghia of danhSachNghia) {
        const nghiaChuan = boDauTiengViet(nghia);

        // 1. So khớp trực tiếp (có dấu hoặc không dấu)
        if (nhapChuan === nghiaChuan) {
            return true;
        }

        // 2. So khớp số <-> chữ
        for (let key in BANG_SO_TIENG_VIET) {
            let words = BANG_SO_TIENG_VIET[key];
            if (words.includes(nhapChuan) && (words.includes(nghiaChuan) || nghiaChuan === key)) {
                return true;
            }
            if (words.includes(nghiaChuan) && (words.includes(nhapChuan) || nhapChuan === key)) {
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