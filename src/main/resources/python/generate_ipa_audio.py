import asyncio
import os
import sys

# Support utf-8 stdout on Windows console
if sys.platform == "win32":
    sys.stdout.reconfigure(encoding='utf-8')

try:
    import edge_tts
except ImportError:
    print("edge_tts not found"); sys.exit(1)

OUTPUT_DIR = os.path.normpath(os.path.join(os.path.dirname(__file__), "..", "static", "audio", "ipa"))
VOICE = "en-GB-SoniaNeural"

# 44 IPA Phonemes - Chuan Received Pronunciation (British RP - Adrian Underhill Chart)
# Moi am doc dung 1 lan duy nhat, chuan xac 100% am vi quoc te
PHONEMES = [
    # --- 12 NGUYÊN ÂM ĐƠN (MONOPHTHONGS) ---
    ("i_long",  "iː",  '<phoneme alphabet="ipa" ph="iː">ee</phoneme>',   "-10%"),
    ("i_short", "ɪ",   '<phoneme alphabet="ipa" ph="ɪ">i</phoneme>',     "-10%"),
    ("u_short", "ʊ",   '<phoneme alphabet="ipa" ph="ʊ">u</phoneme>',     "-10%"),
    ("u_long",  "uː",  '<phoneme alphabet="ipa" ph="uː">oo</phoneme>',   "-10%"),
    ("e",       "e",   '<phoneme alphabet="ipa" ph="e">e</phoneme>',     "-10%"),
    ("schwa",   "ə",   '<phoneme alphabet="ipa" ph="ə">er</phoneme>',    "-10%"),
    ("er_long", "ɜː",  '<phoneme alphabet="ipa" ph="ɜː">ur</phoneme>',   "-10%"),
    ("aw_long", "ɔː",  '<phoneme alphabet="ipa" ph="ɔː">or</phoneme>',   "-10%"),
    ("ae",      "æ",   '<phoneme alphabet="ipa" ph="æ">a</phoneme>',     "-10%"),
    ("uh",      "ʌ",   '<phoneme alphabet="ipa" ph="ʌ">u</phoneme>',     "-10%"),
    ("ah_long", "ɑː",  '<phoneme alphabet="ipa" ph="ɑː">ar</phoneme>',   "-10%"),
    ("o_short", "ɒ",   '<phoneme alphabet="ipa" ph="ɒ">o</phoneme>',     "-10%"),

    # --- 8 NGUYÊN ÂM ĐÔI (DIPHTHONGS) ---
    ("ia",      "ɪə",  '<phoneme alphabet="ipa" ph="ɪə">ear</phoneme>',  "-10%"),
    ("ei",      "eɪ",  '<phoneme alphabet="ipa" ph="eɪ">ay</phoneme>',   "-10%"),
    ("ua",      "ʊə",  '<phoneme alphabet="ipa" ph="ʊə">ure</phoneme>',  "-10%"),
    ("oi",      "ɔɪ",  '<phoneme alphabet="ipa" ph="ɔɪ">oy</phoneme>',   "-10%"),
    ("ou",      "əʊ",  '<phoneme alphabet="ipa" ph="əʊ">ow</phoneme>',   "-10%"),
    ("ea",      "eə",  '<phoneme alphabet="ipa" ph="eə">air</phoneme>',  "-10%"),
    ("ai",      "aɪ",  '<phoneme alphabet="ipa" ph="aɪ">eye</phoneme>',  "-10%"),
    ("au",      "aʊ",  '<phoneme alphabet="ipa" ph="aʊ">ow</phoneme>',   "-10%"),

    # --- 24 PHỤ ÂM (CONSONANTS) ---
    ("p",       "p",   '<phoneme alphabet="ipa" ph="p">p</phoneme>',     "-10%"),
    ("b",       "b",   '<phoneme alphabet="ipa" ph="b">b</phoneme>',     "-10%"),
    ("t",       "t",   '<phoneme alphabet="ipa" ph="t">t</phoneme>',     "-10%"),
    ("d",       "d",   '<phoneme alphabet="ipa" ph="d">d</phoneme>',     "-10%"),
    ("tsh",     "tʃ",  '<phoneme alphabet="ipa" ph="tʃ">ch</phoneme>',   "-10%"),
    ("dzh",     "dʒ",  '<phoneme alphabet="ipa" ph="dʒ">j</phoneme>',    "-10%"),
    ("k",       "k",   '<phoneme alphabet="ipa" ph="k">k</phoneme>',     "-10%"),
    ("g",       "g",   '<phoneme alphabet="ipa" ph="g">g</phoneme>',     "-10%"),
    ("f",       "f",   '<phoneme alphabet="ipa" ph="f">f</phoneme>',     "-10%"),
    ("v",       "v",   '<phoneme alphabet="ipa" ph="v">v</phoneme>',     "-10%"),
    ("th_v",    "θ",   '<phoneme alphabet="ipa" ph="θ">th</phoneme>',    "-10%"),
    ("th_d",    "ð",   '<phoneme alphabet="ipa" ph="ð">th</phoneme>',    "-10%"),
    ("s",       "s",   '<phoneme alphabet="ipa" ph="s">s</phoneme>',     "-10%"),
    ("z",       "z",   '<phoneme alphabet="ipa" ph="z">z</phoneme>',     "-10%"),
    ("sh",      "ʃ",   '<phoneme alphabet="ipa" ph="ʃ">sh</phoneme>',    "-10%"),
    ("zh",      "ʒ",   '<phoneme alphabet="ipa" ph="ʒ">zh</phoneme>',    "-10%"),
    ("m",       "m",   '<phoneme alphabet="ipa" ph="m">m</phoneme>',     "-10%"),
    ("n",       "n",   '<phoneme alphabet="ipa" ph="n">n</phoneme>',     "-10%"),
    ("ng",      "ŋ",   '<phoneme alphabet="ipa" ph="ŋ">ng</phoneme>',    "-10%"),
    ("h",       "h",   '<phoneme alphabet="ipa" ph="h">h</phoneme>',     "-10%"),
    ("l",       "l",   '<phoneme alphabet="ipa" ph="l">l</phoneme>',     "-10%"),
    ("r",       "r",   '<phoneme alphabet="ipa" ph="r">r</phoneme>',     "-10%"),
    ("w",       "w",   '<phoneme alphabet="ipa" ph="w">w</phoneme>',     "-10%"),
    ("y",       "j",   '<phoneme alphabet="ipa" ph="j">y</phoneme>',     "-10%"),
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
    print("=" * 50)
    print("TAO 44 FILE AUDIO IPA CHUAN QUOC TE (DOC 1 LAN)")
    print(f"Voice: {VOICE}")
    print(f"Output: {OUTPUT_DIR}")
    print("=" * 50)
    success = 0
    for fname, ipa, ssml, rate in PHONEMES:
        if await gen_file(fname, ipa, ssml, rate):
            success += 1
        await asyncio.sleep(0.08)
    print("=" * 50)
    print(f"Hoan thanh: {success}/{len(PHONEMES)} am IPA")
    print("=" * 50)

if __name__ == "__main__":
    asyncio.run(main())