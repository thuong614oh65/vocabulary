import asyncio
import edge_tts
from pathlib import Path
import sys


# =========================================================
# CAU HINH PROJECT
# =========================================================

PROJECT_DIR = Path(
    r"D:\Vocabulary\vocabulary"
)


# =========================================================
# THU MUC AUDIO CHU CAI
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
# THU MUC AUDIO TU VUNG
# =========================================================

VOCABULARY_DIR = (
    PROJECT_DIR
    / "src"
    / "main"
    / "resources"
    / "static"
    / "audio"
    / "tu-vung"
)


# =========================================================
# GIONG TIENG ANH
# =========================================================

VOICE = "en-US-AriaNeural"


# =========================================================
# TAO AUDIO
# =========================================================

async def tao_audio(text, output_file):

    output_file = Path(output_file)

    # Tao thu muc neu chua co
    output_file.parent.mkdir(
        parents=True,
        exist_ok=True
    )


    # Neu file da ton tai thi khong tao lai
    if output_file.exists():

        print(
            f"Da ton tai: {output_file.name}"
        )

        return


    print(
        f"Dang tao: {text} -> {output_file.name}"
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
# TAO 26 CHU CAI
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
# TAO TEN FILE MP3 TU TEN TU VUNG
# =========================================================

def tao_ten_file(tu):

    ten_file = (
        tu.lower()
        .strip()
    )

    # Bo cac ky tu khong phu hop voi ten file
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

    # Khoang trang -> dau gach ngang
    ten_file = "-".join(
        ten_file.split()
    )

    return ten_file


# =========================================================
# TAO AUDIO TU VUNG
# =========================================================

async def tao_audio_tu_vung(tu):

    tu = tu.strip()


    if not tu:

        print("Tu rong.")

        return


    ten_file = tao_ten_file(tu)

    output_file = (
        VOCABULARY_DIR
        / f"{ten_file}.mp3"
    )


    await tao_audio(
        tu,
        output_file
    )


# =========================================================
# MAIN
# =========================================================

async def main():

    # Khong co tham so:
    # python tao_audio.py
    #
    # => tao 26 chu cai

    if len(sys.argv) == 1:

        await tao_26_chu_cai()

        return


    # Co tham so:
    # python tao_audio.py apple
    #
    # => tao apple.mp3

    tu = sys.argv[1]

    await tao_audio_tu_vung(tu)


# =========================================================
# CHAY CHUONG TRINH
# =========================================================

if __name__ == "__main__":

    asyncio.run(main())