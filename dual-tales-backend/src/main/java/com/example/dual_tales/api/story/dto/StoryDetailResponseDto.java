package com.example.dual_tales.api.story.dto;

import com.example.dual_tales.api.story_content.dto.StoryContentResponseDto;
import com.example.dual_tales.domain.story.Story;
import com.example.dual_tales.domain.story_content.StoryContent;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class StoryDetailResponseDto {
    private Long storyId;
    private String title;
    private List<StoryContentResponseDto> contents; //여러 페이지가 담길 리스트

    public StoryDetailResponseDto(Story story, List<StoryContent> contentList) {
        this.storyId = story.getId();
        this.title = story.getTitle();
        this.contents = contentList.stream()
                .map(StoryContentResponseDto::new)
                .collect(Collectors.toList());
    }
}
