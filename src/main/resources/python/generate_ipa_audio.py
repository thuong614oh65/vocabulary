import asyncio
import os
import sys

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding='utf-8')

try:
    import edge_tts
except ImportError:
    print("edge_tts not found"); sys.exit(1)

OUTPUT_DIR = os.path.normpath(os.path.join(os.path.dirname(__file__), "..", "static", "audio", "ipa"))
TARGET_OUTPUT_DIR = os.path.normpath(os.path.join(os.path.dirname(__file__), "..", "..", "..", "target", "classes", "static", "audio", "ipa"))
VOICE = "en-GB-SoniaNeural"

# 44 IPA PHONEMES - PHÁT ÂM THUẦN TÚY 1 LẦN DUY NHẤT, KHÔNG ĐỌC TỪ VÍ DỤ
PHONEMES = [
    # ── 12 NGUYÊN ÂM ĐƠN ──
    ("i_long",  "iː",  "ee",   "-25%"), # i dài: ngân dài
    ("i_short", "ɪ",   "ih",   "+20%"), # i ngắn: dứt khoát
    ("u_short", "ʊ",   "uuh",  "+20%"), # u ngắn: dứt khoát
    ("u_long",  "uː",  "ooo",  "-25%"), # u dài: ngân dài
    ("e",       "e",   "eh",   "+0%"),  # e chuẩn
    ("schwa",   "ə",   "uh",   "+25%"), # ơ ngắn (schwa): rất nhẹ & ngắn
    ("er_long", "ɜː",  "ur",   "-25%"), # ơ dài: ngân dài
    ("aw_long", "ɔː",  "aw",   "-25%"), # o dài: ngân dài
    ("ae",      "æ",   "a",    "+0%"),  # a bẹt
    ("uh",      "ʌ",   "uh",   "+15%"), # á ngắn: dứt khoát
    ("ah_long", "ɑː",  "aah",  "-25%"), # a dài: mở rộng ngân dài
    ("o_short", "ɒ",   "o",    "+20%"), # o ngắn: tròn môi dứt khoát

    # ── 8 NGUYÊN ÂM ĐÔI ──
    ("ia",      "ɪə",  "eer",  "-15%"),
    ("ei",      "eɪ",  "ay",   "-15%"),
    ("ua",      "ʊə",  "oor",  "-15%"),
    ("oi",      "ɔɪ",  "oy",   "-15%"),
    ("ou",      "əʊ",  "oh",   "-15%"),
    ("ea",      "eə",  "air",  "-15%"),
    ("ai",      "aɪ",  "eye",  "-15%"),
    ("au",      "aʊ",  "ow",   "-15%"),

    # ── 24 PHỤ ÂM ──
    ("p",       "p",   "p",    "+0%"),
    ("b",       "b",   "b",    "+0%"),
    ("t",       "t",   "t",    "+0%"),
    ("d",       "d",   "d",    "+0%"),
    ("tsh",     "tʃ",  "ch",   "+0%"),
    ("dzh",     "dʒ",  "j",    "+0%"),
    ("k",       "k",   "k",    "+0%"),
    ("g",       "g",   "g",    "+0%"),
    ("f",       "f",   "f",    "+0%"),
    ("v",       "v",   "v",    "+0%"),
    ("th_v",    "θ",   "th",   "+0%"),
    ("th_d",    "ð",   "the",  "+0%"),
    ("s",       "s",   "s",    "+0%"),
    ("z",       "z",   "z",    "+0%"),
    ("sh",      "ʃ",   "sh",   "+0%"),
    ("zh",      "ʒ",   "zh",   "+0%"),
    ("m",       "m",   "m",    "+0%"),
    ("n",       "n",   "n",    "+0%"),
    ("ng",      "ŋ",   "ng",   "+0%"),
    ("h",       "h",   "h",    "+0%"),
    ("l",       "l",   "l",    "+0%"),
    ("r",       "r",   "r",    "+0%"),
    ("w",       "w",   "w",    "+0%"),
    ("y",       "j",   "y",    "+0%"),
]

async def gen_file(fname, ipa, text, rate):
    out_path = os.path.join(OUTPUT_DIR, fname + ".mp3")
    if os.path.exists(out_path):
        os.remove(out_path)
    try:
        comm = edge_tts.Communicate(text, VOICE, rate=rate, volume="+50%")
        await comm.save(out_path)
        size = os.path.getsize(out_path)
        if size > 1000:
            # Sao chép sang target/classes nếu có
            if os.path.exists(TARGET_OUTPUT_DIR):
                import shutil
                shutil.copy2(out_path, os.path.join(TARGET_OUTPUT_DIR, fname + ".mp3"))
            print(f"  [OK] /{ipa}/ -> {fname}.mp3 ({size} bytes)")
            return True
        else:
            print(f"  [FAIL] /{ipa}/ file size too small ({size} bytes)")
            return False
    except Exception as e:
        print(f"  [ERR] /{ipa}/: {e}")
        return False

async def main():
    os.makedirs(OUTPUT_DIR, exist_ok=True)
    if os.path.exists(os.path.dirname(TARGET_OUTPUT_DIR)):
        os.makedirs(TARGET_OUTPUT_DIR, exist_ok=True)
    print("=" * 55)
    print("TẠO 44 ÂM IPA: CHỈ ĐỌC ÂM THUẦN TÚY 1 LẦN, TO RÕ CHUẨN")
    print(f"Voice: {VOICE}")
    print(f"Output: {OUTPUT_DIR}")
    print("=" * 55)
    success = 0
    for fname, ipa, text, rate in PHONEMES:
        if await gen_file(fname, ipa, text, rate):
            success += 1
        await asyncio.sleep(0.08)
    print("=" * 55)
    print(f"Hoàn thành: {success}/{len(PHONEMES)} âm IPA")
    print("=" * 55)

if __name__ == "__main__":
    asyncio.run(main())