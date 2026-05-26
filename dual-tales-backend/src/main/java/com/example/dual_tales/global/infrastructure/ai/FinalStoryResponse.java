package com.example.dual_tales.global.infrastructure.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FinalStoryResponse {

    private String title;

    @JsonProperty("cover_image_url")
    private String coverImageUrl;

    @JsonProperty("target_lang_code")
    private String targetLangCode;

    @JsonProperty("target_age")
    private int targetAge;

    @JsonProperty("page_count")
    private int pageCount;

    private List<PageContent> pages;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageContent {
        private int sequence;

        @JsonProperty("content_ko")
        private String contentKo;

        @JsonProperty("content_foreign")
        private String contentForeign;

        @JsonProperty("image_url")
        private String imageUrl;
    }
}
