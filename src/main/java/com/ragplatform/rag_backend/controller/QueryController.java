package com.ragplatform.rag_backend.controller;

import com.ragplatform.rag_backend.dto.QueryRequest;
import com.ragplatform.rag_backend.dto.QueryResponse;
import com.ragplatform.rag_backend.service.QueryService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class QueryController {

    private static final Logger log = LoggerFactory.getLogger(QueryController.class);

    private final QueryService queryService;

    public QueryController(QueryService queryService) {
        this.queryService = queryService;
    }

    @PostMapping("/query")
    public ResponseEntity<QueryResponse> query(@RequestBody QueryRequest request){
        log.info("Received query: {}", request.getQuestion());
        if (request.getQuestion() == null || request.getQuestion().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                    new QueryResponse("Question cannot be empty", null, null, 0)
            );
        }

        QueryResponse response = queryService.query(request);
        return ResponseEntity.ok(response);
    }

}
