package com.thuong.vocabulary.dto;

public class PhonicsPhonemeDTO {

    private String letters;     // Nhóm chữ cái (ví dụ: "sch", "e", "d", "u", "l", "e")
    private String ipa;         // Ký hiệu IPA tương ứng (ví dụ: "sk", "e", "d", "juː", "l", "")
    private String amDoc;       // Âm đọc bồi tiếng Việt
    private boolean silent;     // true nếu là chữ câm (như 'e' cuối, 'k' trong know)
    private boolean vowel;      // true nếu là nguyên âm (hiển thị màu xanh lá như SoundWhy)

    public PhonicsPhonemeDTO() {
    }

    public PhonicsPhonemeDTO(String letters, String ipa, String amDoc, boolean silent, boolean vowel) {
        this.letters = letters;
        this.ipa = ipa;
        this.amDoc = amDoc;
        this.silent = silent;
        this.vowel = vowel;
    }

    public String getLetters() {
        return letters;
    }

    public void setLetters(String letters) {
        this.letters = letters;
    }

    public String getIpa() {
        return ipa;
    }

    public void setIpa(String ipa) {
        this.ipa = ipa;
    }

    public String getAmDoc() {
        return amDoc;
    }

    public void setAmDoc(String amDoc) {
        this.amDoc = amDoc;
    }

    public boolean isSilent() {
        return silent;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    public boolean isVowel() {
        return vowel;
    }

    public void setVowel(boolean vowel) {
        this.vowel = vowel;
    }
}
