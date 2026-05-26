package com.example.dual_tales.api.story.dto;

import com.example.dual_tales.domain.story_draft.StoryDraft;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class StoryDraftResponseDto {
    private Long draftId;
    private String question_ko;
    private String question_foreign;
    private int currentStep;
    private boolean isFinal;

    public static StoryDraftResponseDto from(StoryDraft draft,String qKo, String qForeign, boolean isFinal) {
        return StoryDraftResponseDto.builder()
                .draftId(draft.getId())
                .question_ko(qKo)
                .question_foreign(qForeign)
                .currentStep(draft.getCurrentStep())
                .isFinal(isFinal)
                .build();
    }
}
