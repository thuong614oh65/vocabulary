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
    // XỬ LÝ PHÁT ÂM VÀ DỪNG ĐOẠN VĂN (EDGE NEURAL TTS MP3 STREAM)
    // =================================================
    const btnNgheDoanVan = document.getElementById("btnNgheDoanVan");
    const btnDungNgheDoanVan = document.getElementById("btnDungNgheDoanVan");
    let audioDoanVanDich = null;

    function dungDocDoanVanDich() {
        if (window.speechSynthesis) {
            window.speechSynthesis.cancel();
        }
        if (audioDoanVanDich) {
            audioDoanVanDich.pause();
            audioDoanVanDich.currentTime = 0;
            if (audioDoanVanDich.src && audioDoanVanDich.src.startsWith("blob:")) {
                URL.revokeObjectURL(audioDoanVanDich.src);
            }
            audioDoanVanDich = null;
        }
        if (btnDungNgheDoanVan) {
            btnDungNgheDoanVan.style.display = "none";
        }
        console.log("ĐÃ DỪNG ĐỌC ĐOẠN VĂN DỊCH.");
    }

    if (btnDungNgheDoanVan) {
        btnDungNgheDoanVan.addEventListener("click", function () {
            dungDocDoanVanDich();
        });
    }

    if (btnNgheDoanVan) {
        btnNgheDoanVan.addEventListener("click", function () {
            if (!doanVan) return;

            dungDocDoanVanDich();

            console.log("ĐỌC ĐOẠN VĂN DỊCH:", doanVan);

            if (btnDungNgheDoanVan) {
                btnDungNgheDoanVan.style.display = "inline-block";
            }

            let daFallback = false;
            function fallbackSpeech() {
                if (daFallback) return;
                daFallback = true;
                if (window.speechSynthesis) {
                    console.log("Dùng giọng đọc trình duyệt (SpeechSynthesis) cho đoạn văn dịch.");
                    let utterance = new SpeechSynthesisUtterance(doanVan);
                    utterance.lang = "en-US";
                    utterance.rate = 0.85;
                    utterance.onend = function () {
                        if (btnDungNgheDoanVan) btnDungNgheDoanVan.style.display = "none";
                    };
                    utterance.onerror = function () {
                        if (btnDungNgheDoanVan) btnDungNgheDoanVan.style.display = "none";
                    };
                    window.speechSynthesis.speak(utterance);
                }
            }

            fetch("/audio/tts", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    text: doanVan,
                    rate: "-5%"
                })
            })
            .then(function (response) {
                if (!response.ok) {
                    throw new Error("Lỗi HTTP " + response.status);
                }
                return response.blob();
            })
            .then(function (blob) {
                const audioUrl = URL.createObjectURL(blob);
                audioDoanVanDich = new Audio(audioUrl);

                audioDoanVanDich.onplay = function () {
                    console.log("BẮT ĐẦU ĐỌC ĐOẠN VĂN DỊCH (MP3 Edge Neural TTS)...");
                    if (btnDungNgheDoanVan) btnDungNgheDoanVan.style.display = "inline-block";
                };

                audioDoanVanDich.onended = function () {
                    console.log("ĐỌC XONG ĐOẠN VĂN DỊCH.");
                    URL.revokeObjectURL(audioUrl); // Giải phóng bộ nhớ RAM
                    audioDoanVanDich = null;
                    if (btnDungNgheDoanVan) btnDungNgheDoanVan.style.display = "none";
                };

                audioDoanVanDich.onerror = function (err) {
                    console.warn("LỖI PHÁT MP3 ĐOẠN VĂN DỊCH:", err, "- Chuyển sang giọng đọc trình duyệt.");
                    URL.revokeObjectURL(audioUrl);
                    fallbackSpeech();
                };

                audioDoanVanDich.play().catch(function (playErr) {
                    console.warn("KHÔNG THỂ PHÁT MP3 ĐOẠN VĂN DỊCH:", playErr, "- Chuyển sang giọng đọc trình duyệt.");
                    URL.revokeObjectURL(audioUrl);
                    fallbackSpeech();
                });
            })
            .catch(function (err) {
                console.warn("LỖI GỌI API /audio/tts:", err.message, "- Chuyển sang giọng đọc trình duyệt.");
                fallbackSpeech();
            });
        });
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