    // =========================================================
    // ĐỌC TỪ VỰNG
    // =========================================================

    function docTu(tu) {

        if (!tu) {
            return;
        }

        const speech = new SpeechSynthesisUtterance(tu);

        speech.lang = "en-US";

        speech.rate = 0.9;

        speech.pitch = 1;

        window.speechSynthesis.cancel();

        window.speechSynthesis.speak(speech);
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