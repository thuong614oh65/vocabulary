package com.thuong.vocabulary.dto.tratu;

import java.util.ArrayList;
import java.util.List;

public class DinhNghiaItem {

    private String partOfSpeech; // noun, verb, adjective, adverb...
    private String definitionEn; // English definition
    private String definitionVi; // Vietnamese translation of definition
    private List<String> examples = new ArrayList<>();
    private List<String> synonyms = new ArrayList<>();

    public DinhNghiaItem() {
    }

    public DinhNghiaItem(String partOfSpeech, String definitionEn, String definitionVi) {
        this.partOfSpeech = partOfSpeech;
        this.definitionEn = definitionEn;
        this.definitionVi = definitionVi;
    }

    public String getPartOfSpeech() {
        return partOfSpeech;
    }

    public void setPartOfSpeech(String partOfSpeech) {
        this.partOfSpeech = partOfSpeech;
    }

    public String getDefinitionEn() {
        return definitionEn;
    }

    public void setDefinitionEn(String definitionEn) {
        this.definitionEn = definitionEn;
    }

    public String getDefinitionVi() {
        return definitionVi;
    }

    public void setDefinitionVi(String definitionVi) {
        this.definitionVi = definitionVi;
    }

    public List<String> getExamples() {
        return examples;
    }

    public void setExamples(List<String> examples) {
        this.examples = examples;
    }

    public List<String> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(List<String> synonyms) {
        this.synonyms = synonyms;
    }
}
