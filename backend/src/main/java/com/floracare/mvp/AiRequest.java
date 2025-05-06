package com.floracare.mvp;

import lombok.Data;

@Data
public class AiRequest {
    private String content;
    private String operation;
    private String image;
}
