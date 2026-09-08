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

function capNhatActiveBar() {
    document.querySelectorAll(".form-check").forEach(function (bar) {
        const radio = bar.querySelector('input[type="radio"]');
        if (radio && radio.checked) {
            bar.classList.add("active-check");
        } else if (radio) {
            bar.classList.remove("active-check");
        }
    });
}

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
    capNhatActiveBar();
}

function capNhatPhamViBo() {
    if (!phamViTuDen || !tuTu || !denTu) return;
    const isTuDen = phamViTuDen.checked;
    tuTu.disabled = !isTuDen;
    denTu.disabled = !isTuDen;
    capNhatActiveBar();
}

// Cho phép click vào bất kỳ vị trí nào trên thanh (.form-check) để tích chọn
document.querySelectorAll(".form-check").forEach(function (bar) {
    bar.addEventListener("click", function (e) {
        // Nếu click vào ô nhập số, nút bấm hoặc select -> để hoạt động tự nhiên
        if (e.target.tagName === "INPUT" && e.target.type !== "radio") {
            return;
        }
        if (e.target.tagName === "BUTTON" || e.target.tagName === "SELECT") {
            return;
        }
        const radio = bar.querySelector('input[type="radio"]');
        if (radio) {
            if (!radio.checked) {
                radio.checked = true;
                radio.dispatchEvent(new Event("change", { bubbles: true }));
            }
            capNhatActiveBar();
        }
    });
});

if (tuTu) {
    tuTu.addEventListener("focus", function () {
        if (phamViTuDen) {
            phamViTuDen.checked = true;
            capNhatPhamViBo();
        }
    });
    tuTu.addEventListener("click", function (e) {
        e.stopPropagation();
    });
}

if (denTu) {
    denTu.addEventListener("focus", function () {
        if (phamViTuDen) {
            phamViTuDen.checked = true;
            capNhatPhamViBo();
        }
    });
    denTu.addEventListener("click", function (e) {
        e.stopPropagation();
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

if (phamViTatCa) {
    phamViTatCa.onchange = capNhatPhamViBo;
}

if (phamViTuDen) {
    phamViTuDen.onchange = capNhatPhamViBo;
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