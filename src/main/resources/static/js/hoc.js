const ngauNhien = document.getElementById("ngauNhien");
const tuSai = document.getElementById("tuSai");
const theoBo = document.getElementById("theoBo");
const chonTu = document.getElementById("chonTu");

const boHoc = document.getElementById("boHoc");

const bangTheoBo =
    document.getElementById("bangTheoBo");

const bangTatCa =
    document.getElementById("bangTatCa");


function capNhat() {

    if (!ngauNhien || !tuSai || !theoBo || !chonTu) {
        return;
    }

    boHoc.disabled =
        !theoBo.checked;

    bangTheoBo.style.display =
        theoBo.checked ? "block" : "none";

    bangTatCa.style.display =
        chonTu.checked ? "block" : "none";

}


if (ngauNhien) {
    ngauNhien.onchange = capNhat;
}

if (tuSai) {
    tuSai.onchange = capNhat;
}

if (theoBo) {
    theoBo.onchange = capNhat;
}

if (chonTu) {
    chonTu.onchange = capNhat;
}


capNhat();


function doiBo() {

    if (boHoc && boHoc.value != "") {

        window.location =
            "/hoc/bo/" + boHoc.value;

    }

}

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
// BẮT ĐẦU ĐỌC TỪ BẰNG MP3
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

    // Tạo audio mới
    audioHienTai =
        new Audio(duongDan);

    audioHienTai.onplay =
        function () {

            console.log(
                "BẮT ĐẦU ĐỌC:",
                tu
            );

        };

    audioHienTai.onended =
        function () {

            console.log(
                "ĐỌC XONG:",
                tu
            );

        };

    audioHienTai.onerror =
        function (e) {

            console.error(
                "LỖI ĐỌC MP3:",
                duongDan,
                e
            );

        };

    audioHienTai
        .play()
        .catch(
            function (error) {

                console.error(
                    "KHÔNG THỂ PHÁT MP3:",
                    error
                );

            }
        );

}


// =========================================================
// HÀM docTu() CHO HTML GỌI
// =========================================================

function docTu(tu) {

    if (!tu) {

        return;

    }

    // Tạo lượt đọc mới

    soLanDoc++;

    let maDoc =
        soLanDoc;

    // Không cần dungTatCaAmThanh() ở đây
    // vì batDauDocTu() đã làm việc đó

    batDauDocTu(
        tu,
        maDoc
    );

}


// =========================================================
// ĐƯA HÀM RA GLOBAL
// =========================================================

window.docTu =
    docTu;