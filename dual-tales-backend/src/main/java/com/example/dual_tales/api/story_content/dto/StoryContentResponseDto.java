package com.example.dual_tales.api.story_content.dto;

import com.example.dual_tales.domain.story_content.StoryContent;
import lombok.Getter;

@Getter
public class StoryContentResponseDto {
    private int sequence;
    private String question_ko;
    private String question_foreign;
    private String answer;
    private String content_ko;
    private String content_foreign;
    private String image_url;

    public StoryContentResponseDto(StoryContent content) {
        this.sequence = content.getSequence();
        this.question_ko = content.getQuestion_ko();
        this.question_foreign = content.getQuestion_foreign();
        this.answer = content.getAnswer();
        this.content_ko = content.getContentKo();
        this.content_foreign = content.getContentForeign();
        this.image_url = content.getImageUrl();
    }
}
