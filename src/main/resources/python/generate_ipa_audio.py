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
VOICE = "en-GB-SoniaNeural"

# 44 IPA PHONEMES - CHUẨN QUỐC TẾ (BRITISH RP)
# QUY TẮC:
# - Âm ngắn: đọc dứt khoát, gọn gàng (rate: +15% đến +20%)
# - Âm dài (:): đọc ngân dài rõ ràng hơn âm ngắn (rate: -25% đến -30%)
# - Nguyên âm đôi: lướt mượt mà giữa 2 âm vị
# - Phụ âm: phát âm thuần túy âm vị đó
PHONEMES = [
    # ── 12 NGUYÊN ÂM ĐƠN ──
    ("i_long",  "iː",  '<phoneme alphabet="ipa" ph="iː">ee</phoneme>',   "-25%"), # i dài: ngân dài
    ("i_short", "ɪ",   '<phoneme alphabet="ipa" ph="ɪ">i</phoneme>',     "+15%"), # i ngắn: dứt khoát
    ("u_short", "ʊ",   '<phoneme alphabet="ipa" ph="ʊ">u</phoneme>',     "+15%"), # u ngắn: dứt khoát
    ("u_long",  "uː",  '<phoneme alphabet="ipa" ph="uː">oo</phoneme>',   "-25%"), # u dài: ngân dài
    ("e",       "e",   '<phoneme alphabet="ipa" ph="e">e</phoneme>',     "-5%"),  # e chuẩn
    ("schwa",   "ə",   '<phoneme alphabet="ipa" ph="ə">er</phoneme>',    "+20%"), # ơ ngắn (schwa): rất nhẹ & ngắn
    ("er_long", "ɜː",  '<phoneme alphabet="ipa" ph="ɜː">ur</phoneme>',   "-25%"), # ơ dài: ngân dài
    ("aw_long", "ɔː",  '<phoneme alphabet="ipa" ph="ɔː">or</phoneme>',   "-25%"), # o dài: ngân dài
    ("ae",      "æ",   '<phoneme alphabet="ipa" ph="æ">a</phoneme>',     "-5%"),  # a bẹt
    ("uh",      "ʌ",   '<phoneme alphabet="ipa" ph="ʌ">u</phoneme>',     "+15%"), # á ngắn: dứt khoát
    ("ah_long", "ɑː",  '<phoneme alphabet="ipa" ph="ɑː">ar</phoneme>',   "-25%"), # a dài: mở rộng ngân dài
    ("o_short", "ɒ",   '<phoneme alphabet="ipa" ph="ɒ">o</phoneme>',     "+15%"), # o ngắn: tròn môi dứt khoát

    # ── 8 NGUYÊN ÂM ĐÔI ──
    ("ia",      "ɪə",  '<phoneme alphabet="ipa" ph="ɪə">ear</phoneme>',  "-10%"),
    ("ei",      "eɪ",  '<phoneme alphabet="ipa" ph="eɪ">ay</phoneme>',   "-10%"),
    ("ua",      "ʊə",  '<phoneme alphabet="ipa" ph="ʊə">ure</phoneme>',  "-10%"),
    ("oi",      "ɔɪ",  '<phoneme alphabet="ipa" ph="ɔɪ">oy</phoneme>',   "-10%"),
    ("ou",      "əʊ",  '<phoneme alphabet="ipa" ph="əʊ">ow</phoneme>',   "-10%"),
    ("ea",      "eə",  '<phoneme alphabet="ipa" ph="eə">air</phoneme>',  "-10%"),
    ("ai",      "aɪ",  '<phoneme alphabet="ipa" ph="aɪ">eye</phoneme>',  "-10%"),
    ("au",      "aʊ",  '<phoneme alphabet="ipa" ph="aʊ">ow</phoneme>',   "-10%"),

    # ── 24 PHỤ ÂM ──
    ("p",       "p",   '<phoneme alphabet="ipa" ph="p">p</phoneme>',     "-5%"),
    ("b",       "b",   '<phoneme alphabet="ipa" ph="b">b</phoneme>',     "-5%"),
    ("t",       "t",   '<phoneme alphabet="ipa" ph="t">t</phoneme>',     "-5%"),
    ("d",       "d",   '<phoneme alphabet="ipa" ph="d">d</phoneme>',     "-5%"),
    ("tsh",     "tʃ",  '<phoneme alphabet="ipa" ph="tʃ">ch</phoneme>',   "-5%"),
    ("dzh",     "dʒ",  '<phoneme alphabet="ipa" ph="dʒ">j</phoneme>',    "-5%"),
    ("k",       "k",   '<phoneme alphabet="ipa" ph="k">k</phoneme>',     "-5%"),
    ("g",       "g",   '<phoneme alphabet="ipa" ph="g">g</phoneme>',     "-5%"),
    ("f",       "f",   '<phoneme alphabet="ipa" ph="f">f</phoneme>',     "-5%"),
    ("v",       "v",   '<phoneme alphabet="ipa" ph="v">v</phoneme>',     "-5%"),
    ("th_v",    "θ",   '<phoneme alphabet="ipa" ph="θ">th</phoneme>',    "-5%"),
    ("th_d",    "ð",   '<phoneme alphabet="ipa" ph="ð">th</phoneme>',    "-5%"),
    ("s",       "s",   '<phoneme alphabet="ipa" ph="s">s</phoneme>',     "-5%"),
    ("z",       "z",   '<phoneme alphabet="ipa" ph="z">z</phoneme>',     "-5%"),
    ("sh",      "ʃ",   '<phoneme alphabet="ipa" ph="ʃ">sh</phoneme>',    "-5%"),
    ("zh",      "ʒ",   '<phoneme alphabet="ipa" ph="ʒ">zh</phoneme>',    "-5%"),
    ("m",       "m",   '<phoneme alphabet="ipa" ph="m">m</phoneme>',     "-5%"),
    ("n",       "n",   '<phoneme alphabet="ipa" ph="n">n</phoneme>',     "-5%"),
    ("ng",      "ŋ",   '<phoneme alphabet="ipa" ph="ŋ">ng</phoneme>',    "-5%"),
    ("h",       "h",   '<phoneme alphabet="ipa" ph="h">h</phoneme>',     "-5%"),
    ("l",       "l",   '<phoneme alphabet="ipa" ph="l">l</phoneme>',     "-5%"),
    ("r",       "r",   '<phoneme alphabet="ipa" ph="r">r</phoneme>',     "-5%"),
    ("w",       "w",   '<phoneme alphabet="ipa" ph="w">w</phoneme>',     "-5%"),
    ("y",       "j",   '<phoneme alphabet="ipa" ph="j">y</phoneme>',     "-5%"),
]

async def gen_file(fname, ipa, ssml, rate):
    out_path = os.path.join(OUTPUT_DIR, fname + ".mp3")
    if os.path.exists(out_path):
        os.remove(out_path)
    try:
        comm = edge_tts.Communicate(ssml, VOICE, rate=rate, volume="+20%")
        await comm.save(out_path)
        size = os.path.getsize(out_path)
        if size > 1000:
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
    print("=" * 55)
    print("TẠO 44 ÂM IPA: ÂM NGẮN GỌN GÀNG - ÂM DÀI (:) NGÂN DÀI")
    print(f"Voice: {VOICE}")
    print(f"Output: {OUTPUT_DIR}")
    print("=" * 55)
    success = 0
    for fname, ipa, ssml, rate in PHONEMES:
        if await gen_file(fname, ipa, ssml, rate):
            success += 1
        await asyncio.sleep(0.08)
    print("=" * 55)
    print(f"Hoàn thành: {success}/{len(PHONEMES)} âm IPA")
    print("=" * 55)

if __name__ == "__main__":
    asyncio.run(main())