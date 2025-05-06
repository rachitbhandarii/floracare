package com.floracare.mvp;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class AiController {
    private final AiService aiService;

    @PostMapping
    public ResponseEntity<String> genAI(@RequestBody AiRequest requestBody) {
        String result = aiService.processContent(requestBody);
        return ResponseEntity.ok(result);
    }
}
