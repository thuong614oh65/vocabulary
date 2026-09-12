package com.thuong.vocabulary.dto;

import java.util.Map;

public class ToeicPart5QuestionDTO {
    private Integer questionNumber;
    private String questionText;
    private String choiceA;
    private String choiceB;
    private String choiceC;
    private String choiceD;
    private String correctAnswer;
    private String category;
    private String subCategory;
    private String mindmapNode;
    private String vietnameseTranslation;
    private String grammarBreakdown;
    private String explanation;
    private Map<String, String> choicesAnalysis;
    private String quickTip;

    public ToeicPart5QuestionDTO() {
    }

    public Integer getQuestionNumber() {
        return questionNumber;
    }

    public void setQuestionNumber(Integer questionNumber) {
        this.questionNumber = questionNumber;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getChoiceA() {
        return choiceA;
    }

    public void setChoiceA(String choiceA) {
        this.choiceA = choiceA;
    }

    public String getChoiceB() {
        return choiceB;
    }

    public void setChoiceB(String choiceB) {
        this.choiceB = choiceB;
    }

    public String getChoiceC() {
        return choiceC;
    }

    public void setChoiceC(String choiceC) {
        this.choiceC = choiceC;
    }

    public String getChoiceD() {
        return choiceD;
    }

    public void setChoiceD(String choiceD) {
        this.choiceD = choiceD;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    public String getMindmapNode() {
        return mindmapNode;
    }

    public void setMindmapNode(String mindmapNode) {
        this.mindmapNode = mindmapNode;
    }

    public String getVietnameseTranslation() {
        return vietnameseTranslation;
    }

    public void setVietnameseTranslation(String vietnameseTranslation) {
        this.vietnameseTranslation = vietnameseTranslation;
    }

    public String getGrammarBreakdown() {
        return grammarBreakdown;
    }

    public void setGrammarBreakdown(String grammarBreakdown) {
        this.grammarBreakdown = grammarBreakdown;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public Map<String, String> getChoicesAnalysis() {
        return choicesAnalysis;
    }

    public void setChoicesAnalysis(Map<String, String> choicesAnalysis) {
        this.choicesAnalysis = choicesAnalysis;
    }

    public String getQuickTip() {
        return quickTip;
    }

    public void setQuickTip(String quickTip) {
        this.quickTip = quickTip;
    }
}
