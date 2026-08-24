const ngauNhien = document.getElementById("ngauNhien");
const tuSai = document.getElementById("tuSai");
const theoBo = document.getElementById("theoBo");
const chonTu = document.getElementById("chonTu");

const boHoc = document.getElementById("boHoc");

const bangTheoBo =
    document.getElementById("bangTheoBo");

const bangTatCa =
    document.getElementById("bangTatCa");


const tuyChonPhamViBo = document.getElementById("tuyChonPhamViBo");
const phamViTatCa = document.getElementById("phamViTatCa");
const phamViTuDen = document.getElementById("phamViTuDen");
const tuTu = document.getElementById("tuTu");
const denTu = document.getElementById("denTu");

function capNhat() {
    if (!ngauNhien || !tuSai || !theoBo || !chonTu) {
        return;
    }

    boHoc.disabled = !theoBo.checked;

    const coChonBo = theoBo.checked && boHoc.value !== "";

    if (tuyChonPhamViBo) {
        tuyChonPhamViBo.style.display = coChonBo ? "block" : "none";
    }

    bangTheoBo.style.display = coChonBo ? "block" : "none";
    bangTatCa.style.display = chonTu.checked ? "block" : "none";

    capNhatPhamViBo();
}

function capNhatPhamViBo() {
    if (!phamViTuDen || !tuTu || !denTu) return;
    const isTuDen = phamViTuDen.checked;
    tuTu.disabled = !isTuDen;
    denTu.disabled = !isTuDen;
}

if (tuTu) {
    tuTu.addEventListener("focus", function () {
        if (phamViTuDen) {
            phamViTuDen.checked = true;
            capNhatPhamViBo();
        }
    });
}

if (denTu) {
    denTu.addEventListener("focus", function () {
        if (phamViTuDen) {
            phamViTuDen.checked = true;
            capNhatPhamViBo();
        }
    });
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
        window.location = "/hoc/bo/" + boHoc.value;
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
            console.warn(
                "LỖI ĐỌC MP3:",
                duongDan,
                "- Chuyển sang giọng đọc trình duyệt."
            );
            fallbackSpeech();
        };

    audioHienTai
        .play()
        .catch(
            function (error) {
                console.warn(
                    "KHÔNG THỂ PHÁT MP3:",
                    error,
                    "- Chuyển sang giọng đọc trình duyệt."
                );
                fallbackSpeech();
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