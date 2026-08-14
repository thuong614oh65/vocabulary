// =====================================================
// DỊCH ĐOẠN VĂN
// XỬ LÝ TỪ TRONG ĐOẠN VĂN
// =====================================================

document.addEventListener("DOMContentLoaded", function () {

    // =================================================
    // LẤY ĐOẠN VĂN
    // =================================================

    const khuVucDoanVan =
        document.getElementById("noiDungDoanVan");

    if (!khuVucDoanVan) {
        return;
    }


    const doanVan =
        khuVucDoanVan.dataset.doanVan;

    if (!doanVan) {
        return;
    }


   // =================================================
   // TẠO MAP TỪ VỰNG CSDL
   // =================================================

   const tuVungMap = new Map();

   if (
       typeof tuVungTrongCSDL !== "undefined" &&
       Array.isArray(tuVungTrongCSDL)
   ) {

       tuVungTrongCSDL.forEach(function (tu) {

           if (!tu || !tu.tiengAnh) {
               return;
           }

           const tuTiengAnh =
               tu.tiengAnh
                   .trim()
                   .toLowerCase();

           tuVungMap.set(
               tuTiengAnh,
               {
                   tiengAnh: tu.tiengAnh,
                   tiengViet: tu.tiengViet || ""
               }
           );

       });

   }

    // =================================================
    // CHIA ĐOẠN VĂN THÀNH TỪ
    // =================================================

    const cacPhan =
        doanVan.split(/(\s+)/);


    const fragment =
        document.createDocumentFragment();


    cacPhan.forEach(function (phan) {

        // ---------------------------------------------
        // Khoảng trắng
        // ---------------------------------------------

        if (/^\s+$/.test(phan)) {

            fragment.appendChild(
                document.createTextNode(phan)
            );

            return;
        }


        // ---------------------------------------------
        // Lấy từ bỏ dấu câu
        // ---------------------------------------------

        const ketQua =
            phan.match(
                /^([^a-zA-ZÀ-ỹ0-9]*)(.*?)([^a-zA-ZÀ-ỹ0-9]*)$/
            );


        if (!ketQua) {

            fragment.appendChild(
                document.createTextNode(phan)
            );

            return;
        }


        const dau =
            ketQua[1];

        const tu =
            ketQua[2];

        const cuoi =
            ketQua[3];


        // Nếu không có phần chữ
        if (!tu) {

            fragment.appendChild(
                document.createTextNode(phan)
            );

            return;
        }


        // ---------------------------------------------
        // Tìm trong CSDL
        // ---------------------------------------------

        const tuChuanHoa =
            tu.toLowerCase();


        const tuTrongCSDL =
            tuVungMap.get(tuChuanHoa);


        // ---------------------------------------------
        // Dấu câu phía trước
        // ---------------------------------------------

        if (dau) {

            fragment.appendChild(
                document.createTextNode(dau)
            );

        }


        // ---------------------------------------------
        // Tạo span cho từ
        // ---------------------------------------------

        const span =
            document.createElement("span");


        span.classList.add(
            "tu-trong-doan-van"
        );


        span.textContent =
            tu;


        // ---------------------------------------------
        // Từ có trong CSDL
        // ---------------------------------------------

        if (tuTrongCSDL) {

            span.classList.add(
                "tu-dang-hoc"
            );


            span.dataset.tiengViet =
                tuTrongCSDL.tiengViet;


            span.dataset.coTrongCSDL =
                "true";

        } else {

            // -----------------------------------------
            // Từ chưa có trong CSDL
            // -----------------------------------------

            span.dataset.coTrongCSDL =
                "false";

        }


        // ---------------------------------------------
        // Dấu câu phía sau
        // ---------------------------------------------

        fragment.appendChild(span);


        if (cuoi) {

            fragment.appendChild(
                document.createTextNode(cuoi)
            );

        }

    });


    // =================================================
    // ĐƯA KẾT QUẢ VÀO HTML
    // =================================================

    khuVucDoanVan.innerHTML = "";

    khuVucDoanVan.appendChild(
        fragment
    );


    // =================================================
    // XỬ LÝ CLICK VÀO TỪ
    // =================================================

    khuVucDoanVan
        .addEventListener(
            "click",
            function (event) {

                const tu =
                    event.target.closest(
                        ".tu-trong-doan-van"
                    );


                if (!tu) {
                    return;
                }


                hienThiThongTinTu(tu);

            }
        );

});


// =====================================================
// HIỂN THỊ THÔNG TIN TỪ
// =====================================================

// =====================================================
// HIỂN THỊ THÔNG TIN TỪ
// =====================================================

function hienThiThongTinTu(tu) {

    // =================================================
    // XÓA POPUP CŨ
    // =================================================

    const popupCu =
        document.querySelector(
            ".popup-tu-vung"
        );

    if (popupCu) {
        popupCu.remove();
    }


    // =================================================
    // LẤY THÔNG TIN TỪ
    // =================================================

    const tenTu =
        tu.textContent.trim();

    const coTrongCSDL =
        tu.dataset.coTrongCSDL === "true";

    const nghia =
        tu.dataset.tiengViet || "";


    // =================================================
    // TẠO POPUP
    // =================================================

    const popup =
        document.createElement("div");

    popup.className =
        "popup-tu-vung";


    // =================================================
    // TÊN TỪ
    // =================================================

    const tieuDe =
        document.createElement("div");

    tieuDe.className =
        "popup-tu";

    tieuDe.textContent =
        tenTu;

    popup.appendChild(
        tieuDe
    );


    // =================================================
    // TỪ CÓ TRONG CSDL
    // =================================================

    if (coTrongCSDL) {

        const nhan =
            document.createElement("div");

        nhan.className =
            "popup-nhan";

        nhan.textContent =
            "⭐ Từ đang học";

        popup.appendChild(
            nhan
        );


        // ---------------------------------------------
        // Hiển thị nghĩa từ CSDL
        // ---------------------------------------------

        if (nghia) {

            const nghiaElement =
                document.createElement("div");

            nghiaElement.className =
                "popup-nghia";

            nghiaElement.textContent =
                "📖 " + nghia;

            popup.appendChild(
                nghiaElement
            );

        }


    // =================================================
    // TỪ CHƯA CÓ TRONG CSDL
    // =================================================

    } else {

        const nhan =
            document.createElement("div");

        nhan.className =
            "popup-nhan";

        nhan.textContent =
            "📚 Từ chưa có trong CSDL";

        popup.appendChild(
            nhan
        );


        // ---------------------------------------------
        // Hiển thị trạng thái đang lấy nghĩa
        // ---------------------------------------------

        const thongBao =
            document.createElement("div");

        thongBao.className =
            "popup-nghia";

        thongBao.textContent =
            "⏳ Đang lấy nghĩa...";

        popup.appendChild(
            thongBao
        );


        // =================================================
        // GỌI BACKEND LẤY NGHĨA
        // =================================================

        fetch(
            "/dich-doan-van/nghia?tu=" +
            encodeURIComponent(tenTu)
        )

            // ---------------------------------------------
            // Nhận response
            // ---------------------------------------------

            .then(function (response) {

                if (!response.ok) {

                    throw new Error(
                        "HTTP " +
                        response.status
                    );

                }

                return response.json();

            })


            // ---------------------------------------------
            // Nhận JSON
            // ---------------------------------------------

            .then(function (data) {

                console.log(
                    "Kết quả lấy nghĩa:",
                    data
                );


                if (
                    data &&
                    data.nghia &&
                    data.nghia.trim() !== ""
                ) {

                    thongBao.textContent =
                        "📖 " +
                        data.nghia;

                } else {

                    thongBao.textContent =
                        "📖 Không tìm thấy nghĩa.";

                }

            })


            // ---------------------------------------------
            // Xử lý lỗi
            // ---------------------------------------------

            .catch(function (error) {

                console.error(
                    "Lỗi lấy nghĩa:",
                    error
                );

                thongBao.textContent =
                    "❌ Không thể lấy nghĩa.";

            });

    }


    // =================================================
    // ĐƯA POPUP VÀO HTML
    // =================================================

    document.body.appendChild(
        popup
    );


    // =================================================
    // VỊ TRÍ POPUP
    // =================================================

    const rect =
        tu.getBoundingClientRect();

    popup.style.position =
        "fixed";

    popup.style.left =
        Math.min(
            rect.left,
            window.innerWidth - 280
        ) + "px";

    popup.style.top =
        (rect.bottom + 10) + "px";


    // =================================================
    // CLICK RA NGOÀI → ĐÓNG POPUP
    // =================================================

    setTimeout(function () {

        document.addEventListener(
            "click",
            function dongPopup(event) {

                if (
                    !popup.contains(event.target) &&
                    !tu.contains(event.target)
                ) {

                    popup.remove();

                    document.removeEventListener(
                        "click",
                        dongPopup
                    );

                }

            }
        );

    }, 0);

}