
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
// CHẤM ĐIỂM
// =========================================================

function chamDiem(input) {


    // -------------------------------------------------
    // Nếu đã chấm rồi thì không chấm lại
    // -------------------------------------------------

    if (input.disabled) {

        return;

    }


    // -------------------------------------------------
    // Lấy đáp án
    // -------------------------------------------------

    let dapAn =
        input.dataset.dapAn;


    if (!dapAn) {

        return;

    }


    dapAn =
        dapAn.toLowerCase();


    // Cho phép nhiều đáp án:
    // hello;hi

    let danhSach =
        dapAn.split(";");


    // -------------------------------------------------
    // Lấy câu trả lời người dùng
    // -------------------------------------------------

    let nhap =
        input.value
            .trim()
            .toLowerCase();


    let dung =
        false;


    // -------------------------------------------------
    // Kiểm tra đáp án
    // -------------------------------------------------

    for (
        let da of danhSach
    ) {

        if (
            nhap === da.trim()
        ) {

            dung =
                true;

            break;

        }

    }


    // -------------------------------------------------
    // ĐÚNG
    // -------------------------------------------------

    if (dung) {


        input.style.background =
            "lightgreen";


        fetch(
            "/hoc/dung/" +
            input.dataset.id,
            {
                method: "POST"
            }
        );

    }


    // -------------------------------------------------
    // SAI
    // -------------------------------------------------

    else {


        input.style.background =
            "pink";


        fetch(
            "/hoc/sai/" +
            input.dataset.id,
            {
                method: "POST"
            }
        );

    }


    // -------------------------------------------------
    // KHÓA Ô NHẬP
    // -------------------------------------------------

    input.disabled =
        true;


    // -------------------------------------------------
    // Tìm từ tiếp theo
    // -------------------------------------------------

    setTimeout(
        function () {


            let inputs =
                document.querySelectorAll(
                    ".cau-tra-loi"
                );


            let viTri =
                Array.from(inputs)
                    .indexOf(input);


            if (
                inputs[viTri + 1]
            ) {


                inputs[
                    viTri + 1
                ].focus();


            }


        },
        200
    );

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