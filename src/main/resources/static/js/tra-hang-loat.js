    // =========================================================
    // ĐỌC TỪ VỰNG
    // =========================================================

    // =========================================================
    // ĐỌC TỪ VỰNG BẰNG FILE MP3
    // =========================================================

    let amThanhTuVung = null;


    function taoTenFileAudio(tu) {

        let tenFile =
            tu
                .toLowerCase()
                .trim()
                .replace(/[\\/:*?"<>|]/g, "");

        tenFile =
            tenFile
                .split(/\s+/)
                .join("-");

        return tenFile;
    }


    // =========================================================
    // TỰ ĐỘNG ẨN THÔNG BÁO
    // =========================================================

    document.addEventListener("DOMContentLoaded", function () {

        const alerts = document.querySelectorAll(".alert");

        alerts.forEach(function (alert) {

            setTimeout(function () {

                alert.style.opacity = "0";

                alert.style.transition = "opacity 0.5s ease";

                setTimeout(function () {

                    alert.remove();

                }, 500);

            }, 3000);

        });

    });