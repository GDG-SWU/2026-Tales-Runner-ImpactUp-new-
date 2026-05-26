package com.example.dual_tales.api.story.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

//AI가 최종적으로 만들어서 던져줄 동화 DTO
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class StoryCreateRequestDto {
    private Long draftId;
    private String title;

    @JsonProperty("cover_image_url")
    private String cover_image_url;
    @JsonProperty("target_lang_code")
    private String targetLangCode;
    @JsonProperty("target_age")
    private int targetAge;
    @JsonProperty("page_count")
    private int pageCount;
    @JsonProperty("contents")
    private List<ContentDto> contents;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ContentDto {
        private int sequence;
        @JsonProperty("content_ko")
        private String content_ko;
        @JsonProperty("content_foreign")
        private String content_foreign;
        @JsonProperty("image_url")
        private String image_url;
    }
}
