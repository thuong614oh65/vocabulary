// =========================================================
// KHỞI TẠO
// =========================================================

document.addEventListener("DOMContentLoaded", function () {

    document
        .querySelectorAll(".cau-tra-loi")
        .forEach(function (input) {

            // -------------------------------------------------
            // Nhấn ENTER để chấm
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
// NÚT ✓ TRÊN ĐIỆN THOẠI
// =========================================================

function chamDiemTuNut(button) {

    // Lấy dòng <tr>
    let row = button.closest("tr");

    if (!row) {
        return;
    }


    // Tìm ô nhập
    let input =
        row.querySelector(".cau-tra-loi");

    if (!input) {
        return;
    }


    // Chấm điểm
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
    // Lấy câu trả lời
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
    // Chuyển sang ô tiếp theo
    // -------------------------------------------------

    setTimeout(function () {

        let inputs =
            document.querySelectorAll(
                ".cau-tra-loi"
            );


        let viTri =
            Array.from(inputs)
                .indexOf(input);


        if (inputs[viTri + 1]) {

            inputs[
                viTri + 1
            ].focus();

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