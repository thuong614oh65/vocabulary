#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
generate_ipa_audio.py - Tao 44 file audio IPA chuan British RP
Voice: en-GB-SoniaNeural (Microsoft Edge TTS - chat luong cao nhat)
"""
import asyncio, os, sys
try:
    import edge_tts
except ImportError:
    print("Can not import edge_tts - please: pip install edge-tts")
    sys.exit(1)

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
OUTPUT_DIR = os.path.normpath(os.path.join(SCRIPT_DIR, "..", "static", "audio", "ipa"))
VOICE = "en-GB-SoniaNeural"

PHONEMES = [
    # NGUYEN AM DON (MONOPHTHONGS)
    ("i_long",  "iz",  "i: i: i:",  "sheep"),
    ("i_short", "I",   "I I I",     "ship"),
    ("u_short", "U",   "U U U",     "good"),
    ("u_long",  "uz",  "u: u: u:",  "shoot"),
    ("e",       "e",   "e e e",     "bed"),
    ("schwa",   "@",   "@ @ @",     "teacher"),
    ("er_long", "3z",  "3: 3: 3:",  "bird"),
    ("aw_long", "Oz",  "O: O: O:",  "door"),
    ("ae",      "ae",  "ae ae ae",  "cat"),
    ("uh",      "V",   "V V V",     "up"),
    ("ah_long", "az",  "A: A: A:",  "far"),
    ("o_short", "Q",   "Q Q Q",     "on"),
    # NGUYEN AM DOI (DIPHTHONGS)
    ("ia",      "I@",  "I@ I@ I@",  "here"),
    ("ei",      "eI",  "eI eI eI",  "wait"),
    ("ua",      "U@",  "U@ U@ U@",  "tourist"),
    ("oi",      "OI",  "OI OI OI",  "boy"),
    ("ou",      "@U",  "@U @U @U",  "show"),
    ("ea",      "e@",  "e@ e@ e@",  "hair"),
    ("ai",      "aI",  "aI aI aI",  "my"),
    ("au",      "aU",  "aU aU aU",  "cow"),
    # PHU AM (CONSONANTS)
    ("p",       "p",   "p@ p@ p@",  "pea"),
    ("b",       "b",   "b@ b@ b@",  "boat"),
    ("t",       "t",   "t@ t@ t@",  "tea"),
    ("d",       "d",   "d@ d@ d@",  "dog"),
    ("tsh",     "tS",  "tS@ tS@",   "cheese"),
    ("dzh",     "dZ",  "dZ@ dZ@",   "June"),
    ("k",       "k",   "k@ k@ k@",  "car"),
    ("g",       "g",   "g@ g@ g@",  "go"),
    ("f",       "f",   "f@ f@ f@",  "fly"),
    ("v",       "v",   "v@ v@ v@",  "video"),
    ("th_v",    "T",   "T@ T@ T@",  "think"),
    ("th_d",    "D",   "D@ D@ D@",  "this"),
    ("s",       "s",   "s s s s",   "see"),
    ("z",       "z",   "z z z z",   "zoo"),
    ("sh",      "S",   "S S S",     "shall"),
    ("zh",      "Z",   "Z Z Z",     "television"),
    ("m",       "m",   "m m m m",   "man"),
    ("n",       "n",   "n n n n",   "now"),
    ("ng",      "N",   "N N N",     "sing"),
    ("h",       "h",   "h@ h@ h@",  "hat"),
    ("l",       "l",   "l l l l",   "love"),
    ("r",       "r",   "r@ r@ r@",  "red"),
    ("w",       "w",   "w@ w@ w@",  "wet"),
    ("y",       "j",   "j@ j@ j@",  "yes"),
]

async def gen(fname, ipa, ph, word):
    out = os.path.join(OUTPUT_DIR, fname + ".mp3")
    if os.path.exists(out) and os.path.getsize(out) > 2000:
        print("  [OK] /" + ipa + "/ da co san")
        return True
    try:
        comm = edge_tts.Communicate(ph, VOICE, rate="-40%", volume="+0%")
        await comm.save(out)
        sz = os.path.getsize(out)
        if sz > 2000:
            print("  [XONG] /" + ipa + "/ -> " + fname + ".mp3 (" + str(sz//1024) + "KB)")
            return True
        if os.path.exists(out): os.remove(out)
        print("  [LOI] /" + ipa + "/ file qua nho")
        return False
    except Exception as e:
        print("  [LOI] /" + ipa + "/ - " + str(e)[:60])
        return False

async def main():
    os.makedirs(OUTPUT_DIR, exist_ok=True)
    print("\n=== TAO AUDIO BANG IPA - British RP ===")
    print("Voice: " + VOICE)
    print("Output: " + OUTPUT_DIR)
    print("So am: " + str(len(PHONEMES)))
    print("=" * 40)
    ok = 0
    for fname, ipa, ph, word in PHONEMES:
        if await gen(fname, ipa, ph, word):
            ok += 1
        await asyncio.sleep(0.1)
    print("=" * 40)
    print("Thanh cong: " + str(ok) + "/" + str(len(PHONEMES)))

if __name__ == "__main__":
    asyncio.run(main())
