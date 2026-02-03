package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StockNews implements Serializable {
    private String category;
    private Long datetime; // Unix timestamp
    private String headline;
    private Long id;
    private String image;
    private String related;
    private String source;
    private String summary;
    private String url;
}