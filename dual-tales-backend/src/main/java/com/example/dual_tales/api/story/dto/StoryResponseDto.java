package com.example.dual_tales.api.story.dto;

import com.example.dual_tales.domain.story.Story;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class StoryResponseDto {
    private Long id;
    private String title;
    private String coverImageUrl;
    private String targetLangCode;
    private int targetAge;
    private int page_count;
    private LocalDateTime createdAt;
    private String nickname;

    public StoryResponseDto(Story story) {
        this.id = story.getId();
        this.title = story.getTitle();
        this.coverImageUrl = story.getCover_image();
        this.targetLangCode = story.getTargetLangCode();
        this.targetAge = story.getTargetAge();
        this.page_count = story.getPage_count();
        this.createdAt = story.getCreatedAt();
        this.nickname = story.getUser().getNickname();

    }
}
