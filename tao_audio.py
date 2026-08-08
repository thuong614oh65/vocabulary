import asyncio
import edge_tts
from pathlib import Path


# Thư mục chứa audio của Spring Boot
OUTPUT_DIR = Path(
    r"D:\Vocabulary\vocabulary\src\main\resources\static\audio\alphabet"
)

# Giọng tiếng Anh
VOICE = "en-US-AriaNeural"

# 26 chữ cái
LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"


async def create_audio(letter):
    output_file = OUTPUT_DIR / f"{letter.lower()}.mp3"

    print(f"Đang tạo: {output_file.name}")

    communicate = edge_tts.Communicate(
        text=f"{letter}.",
        voice=VOICE
    )

    await communicate.save(str(output_file))

    print(
        f"Đã tạo: {output_file.name} "
        f"({output_file.stat().st_size} bytes)"
    )


async def main():
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

    print("====================================")
    print("      TẠO AUDIO 26 CHỮ CÁI")
    print("====================================")
    print()

    for letter in LETTERS:
        try:
            await create_audio(letter)
        except Exception as e:
            print(f"LỖI khi tạo {letter}: {e}")

    print()
    print("====================================")
    print("HOÀN TẤT")
    print("====================================")
    print()
    print(f"Audio được lưu tại:")
    print(OUTPUT_DIR)


if __name__ == "__main__":
    asyncio.run(main())