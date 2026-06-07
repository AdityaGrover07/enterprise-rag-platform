package com.ragplatform.rag_backend.dto;

public class QueryRequest {
    private String question;
    private Integer maxChunks = 5; // how many chunks to retrieve

    public QueryRequest() {}

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public Integer getMaxChunks() { return maxChunks; }
    public void setMaxChunks(Integer maxChunks) { this.maxChunks = maxChunks; }

}
