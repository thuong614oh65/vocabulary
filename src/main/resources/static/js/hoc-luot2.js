// =========================================================
// KHỞI TẠO
// =========================================================

document.addEventListener("DOMContentLoaded", function () {

    document
        .querySelectorAll(".cau-tra-loi")
        .forEach(function (input) {


            // -------------------------------------------------
            // Khi click/focus vào ô nhập
            // -------------------------------------------------

            input.addEventListener(
                "focus",
                function () {

                    let tu = this.dataset.tu;

                    if (!tu) {
                        return;
                    }


                    // Đọc cả từ trước
                    docTu(tu);


                    // Sau khi đọc từ xong mới đánh vần
                    setTimeout(function () {

                        danhVan(tu);

                    }, 1200);

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
// ĐỌC TỪ TIẾNG ANH
// =========================================================

function docTu(tu) {

    if (!tu || tu.trim() === "") {
        return;
    }


    tu = tu.trim();


    // Dừng TTS cũ
    if ("speechSynthesis" in window) {

        speechSynthesis.cancel();

    }


    // -------------------------------------------------
    // Audio phát âm tiếng Anh
    // -------------------------------------------------

    let audio =
        new Audio(
            "https://api.dictionaryapi.dev/media/pronunciations/en/"
            + encodeURIComponent(tu)
            + "-us.mp3"
        );


    audio.volume = 1.0;


    // -------------------------------------------------
    // Nếu audio phát được
    // -------------------------------------------------

    audio.play()
        .catch(function () {

            // -------------------------------------------------
            // Không có audio -> fallback TTS
            // -------------------------------------------------

            docTuBangTTS(tu);

        });

}


// =========================================================
// FALLBACK ĐỌC TỪ BẰNG TTS
// =========================================================

function docTuBangTTS(tu) {

    if (!("speechSynthesis" in window)) {
        return;
    }


    speechSynthesis.cancel();


    let noiDung =
        new SpeechSynthesisUtterance(tu);


    noiDung.lang = "en-US";

    noiDung.rate = 0.8;

    noiDung.pitch = 1.0;

    noiDung.volume = 1.0;


    speechSynthesis.speak(noiDung);

}


// =========================================================
// ĐÁNH VẦN TỪ
// =========================================================

async function danhVan(tu) {

    if (!tu || tu.trim() === "") {
        return;
    }


    tu = tu.trim().toUpperCase();


    // Dừng TTS cũ
    if ("speechSynthesis" in window) {

        speechSynthesis.cancel();

    }


    // -------------------------------------------------
    // Đọc từng ký tự
    // -------------------------------------------------

    for (let i = 0; i < tu.length; i++) {

        let kyTu = tu[i];


        // Bỏ qua khoảng trắng
        if (kyTu === " ") {

            continue;

        }


        // Đọc chữ cái
        await docChuCai(kyTu);


        // Khoảng nghỉ giữa các chữ
        await cho(400);

    }

}


// =========================================================
// ĐỌC 1 CHỮ CÁI
// =========================================================

function docChuCai(chu) {

    return new Promise(function (resolve) {


        // -------------------------------------------------
        // URL audio chữ cái
        // -------------------------------------------------

        let audio =
            new Audio(
                "https://api.dictionaryapi.dev/media/pronunciations/en/"
                + encodeURIComponent(
                    chu.toLowerCase()
                )
                + "-us.mp3"
            );


        audio.volume = 1.0;


        // -------------------------------------------------
        // Audio đọc xong
        // -------------------------------------------------

        audio.onended = function () {

            resolve();

        };


        // -------------------------------------------------
        // Audio lỗi
        // -------------------------------------------------

        audio.onerror = function () {

            docChuCaiBangTTS(
                chu,
                resolve
            );

        };


        // -------------------------------------------------
        // Phát audio
        // -------------------------------------------------

        audio.play()
            .catch(function () {

                docChuCaiBangTTS(
                    chu,
                    resolve
                );

            });

    });

}


// =========================================================
// FALLBACK ĐỌC CHỮ CÁI BẰNG TTS
// =========================================================

function docChuCaiBangTTS(
    chu,
    resolve
) {

    // -------------------------------------------------
    // Thiết bị không hỗ trợ TTS
    // -------------------------------------------------

    if (!("speechSynthesis" in window)) {

        resolve();

        return;

    }


    speechSynthesis.cancel();


    let noiDung =
        new SpeechSynthesisUtterance(chu);


    noiDung.lang = "en-US";

    noiDung.rate = 0.8;

    noiDung.pitch = 1.0;

    noiDung.volume = 1.0;


    noiDung.onend = function () {

        resolve();

    };


    noiDung.onerror = function () {

        resolve();

    };


    speechSynthesis.speak(noiDung);

}


// =========================================================
// CHỜ
// =========================================================

function cho(ms) {

    return new Promise(function (resolve) {

        setTimeout(
            resolve,
            ms
        );

    });

}


// =========================================================
// NÚT ✓ TRÊN ĐIỆN THOẠI
// =========================================================

function chamDiemTuNut(button) {


    // -------------------------------------------------
    // Lấy dòng <tr> chứa nút
    // -------------------------------------------------

    let row =
        button.closest("tr");


    if (!row) {
        return;
    }


    // -------------------------------------------------
    // Tìm ô nhập trong dòng
    // -------------------------------------------------

    let input =
        row.querySelector(".cau-tra-loi");


    if (!input) {
        return;
    }


    // -------------------------------------------------
    // Không chấm lại nếu đã chấm
    // -------------------------------------------------

    if (input.disabled) {
        return;
    }


    // -------------------------------------------------
    // Chấm điểm
    // -------------------------------------------------

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


    // -------------------------------------------------
    // Hỗ trợ nhiều đáp án cách nhau bằng ;
// -------------------------------------------------

    let danhSach =
        dapAn.split(";");


    // -------------------------------------------------
    // Lấy câu trả lời người dùng
    // -------------------------------------------------

    let nhap =
        input.value
            .trim()
            .toLowerCase();


    let dung = false;


    // -------------------------------------------------
    // Kiểm tra đáp án
    // -------------------------------------------------

    for (let da of danhSach) {

        if (nhap === da.trim()) {

            dung = true;

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
            "/hoc/dung/" + input.dataset.id,
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
            "/hoc/sai/" + input.dataset.id,
            {
                method: "POST"
            }
        );

    }


    // -------------------------------------------------
    // KHÓA ô nhập
    // -------------------------------------------------

    input.disabled = true;


    // -------------------------------------------------
    // Tìm ô nhập tiếp theo
    // -------------------------------------------------

    setTimeout(function () {


        let inputs =
            document.querySelectorAll(
                ".cau-tra-loi"
            );


        let viTri =
            Array.from(inputs)
                .indexOf(input);


        // -------------------------------------------------
        // Focus ô tiếp theo
        // -------------------------------------------------

        if (inputs[viTri + 1]) {

            inputs[viTri + 1].focus();

        }


    }, 500);

}


// =========================================================
// TIẾP LƯỢT
// =========================================================

function tiepLuot() {

    window.location =
        "/hoc/tiep";

}


// =========================================================
// DỪNG HỌC
// =========================================================

function dungHoc() {

    window.location =
        "/hoc/dung";

}