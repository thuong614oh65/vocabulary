import asyncio
import edge_tts
from pathlib import Path
import sys


# =========================================================
# PROJECT DIR
# =========================================================

PROJECT_DIR = Path(__file__).resolve().parent


# =========================================================
# AUDIO CHỮ CÁI
# =========================================================

ALPHABET_DIR = (
    PROJECT_DIR
    / "src"
    / "main"
    / "resources"
    / "static"
    / "audio"
    / "alphabet"
)


# =========================================================
# GIỌNG TIẾNG ANH
# =========================================================

VOICE = "en-US-AriaNeural"


# =========================================================
# TẠO AUDIO
# =========================================================

async def tao_audio(text, output_file):

    output_file = Path(output_file)

    # Tạo thư mục nếu chưa có
    output_file.parent.mkdir(
        parents=True,
        exist_ok=True
    )

    # Nếu file đã tồn tại
    if output_file.exists():

        print(
            f"Da ton tai: {output_file}"
        )

        return

    print(
        f"Dang tao: {text} -> {output_file}"
    )

    communicate = edge_tts.Communicate(
        text=text,
        voice=VOICE,
        rate="+12%"
    )

    await communicate.save(
        str(output_file)
    )

    print(
        f"Da tao: {output_file.name} "
        f"({output_file.stat().st_size} bytes)"
    )


# =========================================================
# TẠO 26 CHỮ CÁI
# =========================================================

async def tao_26_chu_cai():

    ALPHABET_DIR.mkdir(
        parents=True,
        exist_ok=True
    )

    LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

    print("====================================")
    print("      TAO AUDIO 26 CHU CAI")
    print("====================================")
    print()

    for letter in LETTERS:

        output_file = (
            ALPHABET_DIR
            / f"{letter.lower()}.mp3"
        )

        try:

            await tao_audio(
                letter,
                output_file
            )

        except Exception as e:

            print(
                f"LOI khi tao {letter}: {e}"
            )

    print()
    print("====================================")
    print("HOAN TAT 26 CHU CAI")
    print("====================================")
    print()


# =========================================================
# TẠO TÊN FILE AN TOÀN
# =========================================================

def tao_ten_file(tu):

    ten_file = (
        tu.lower()
        .strip()
    )

    # Bỏ ký tự không phù hợp
    ten_file = (
        ten_file
        .replace("\\", "")
        .replace("/", "")
        .replace(":", "")
        .replace("*", "")
        .replace("?", "")
        .replace('"', "")
        .replace("<", "")
        .replace(">", "")
        .replace("|", "")
    )

    # Khoảng trắng -> -
    ten_file = "-".join(
        ten_file.split()
    )

    return ten_file


# =========================================================
# TẠO AUDIO TỪ VỰNG
# =========================================================

async def tao_audio_tu_vung(
    tu,
    output_dir
):

    tu = tu.strip()

    if not tu:

        print("Tu rong.")

        return

    # Tạo tên file
    ten_file = tao_ten_file(tu)

    # Thư mục Java truyền vào
    output_dir = Path(output_dir)

    # File MP3 cuối cùng
    output_file = (
        output_dir
        / f"{ten_file}.mp3"
    )

    await tao_audio(
        tu,
        output_file
    )


# =========================================================
# STREAM AUDIO TRỰC TIẾP (KHÔNG LƯU VÀO ĐĨA)
# =========================================================

async def stream_audio(text, rate_str="+0%"):
    text = text.strip()
    if not text:
        return

    communicate = edge_tts.Communicate(
        text=text,
        voice=VOICE,
        rate=rate_str
    )

    async for chunk in communicate.stream():
        if chunk["type"] == "audio":
            sys.stdout.buffer.write(chunk["data"])

    sys.stdout.buffer.flush()


# =========================================================
# MAIN
# =========================================================

async def main():

    # =====================================================
    # KHÔNG CÓ THAM SỐ
    #
    # python tao_audio.py
    #
    # => tạo 26 chữ cái
    # =====================================================

    if len(sys.argv) == 1:

        await tao_26_chu_cai()

        return


    # =====================================================
    # CHẾ ĐỘ STREAM AUDIO (KHÔNG LƯU VÀO ĐĨA)
    #
    # python tao_audio.py --stream "This is a sentence." "+0%"
    # =====================================================

    if sys.argv[1] == "--stream":
        text = sys.argv[2] if len(sys.argv) >= 3 else ""
        rate = sys.argv[3] if len(sys.argv) >= 4 else "+0%"
        await stream_audio(text, rate)
        return


    # =====================================================
    # TẠO AUDIO TỪ VỰNG (LƯU VÀO FILE)
    #
    # python tao_audio.py apple "D:\...\audio-data\tu-vung"
    # =====================================================

    tu = sys.argv[1]


    # Nếu có thư mục đích
    if len(sys.argv) >= 3:

        output_dir = sys.argv[2]

    else:

        # Fallback:
        # nếu chạy Python trực tiếp
        # thì lưu vào thư mục cũ

        output_dir = (
            PROJECT_DIR
            / "src"
            / "main"
            / "resources"
            / "static"
            / "audio"
            / "tu-vung"
        )


    await tao_audio_tu_vung(
        tu,
        output_dir
    )


# =========================================================
# CHẠY
# =========================================================

if __name__ == "__main__":

    asyncio.run(main())