package com.ragplatform.rag_backend.dto;

import java.util.List;

public class QueryResponse {
    private String answer;
    private String question;
    private List<String> sourceChunks; // which chunks were used
    private long latencyMs; // how long it took

    public QueryResponse(String answer, String question, List<String> sourceChunks, long latencyMs) {
        this.answer = answer;
        this.question = question;
        this.sourceChunks = sourceChunks;
        this.latencyMs = latencyMs;
    }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public List<String> getSourceChunks() { return sourceChunks; }
    public void setSourceChunks(List<String> sourceChunks) { this.sourceChunks = sourceChunks; }

    public long getLatencyMs() { return latencyMs; }
    public void setLatencyMs(long latencyMs) { this.latencyMs = latencyMs; }

}
