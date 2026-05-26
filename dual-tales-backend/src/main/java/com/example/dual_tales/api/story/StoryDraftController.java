package com.example.dual_tales.api.story;

import com.example.dual_tales.api.story.dto.StoryAnswerRequest;
import com.example.dual_tales.api.story.dto.StoryDraftCreateRequest;
import com.example.dual_tales.api.story.dto.StoryDraftResponseDto;
import com.example.dual_tales.domain.story_draft.StoryDraft;
import com.example.dual_tales.domain.story_draft.StoryDraftRepository;
import com.example.dual_tales.domain.user.User;
import com.example.dual_tales.service.story.StoryDraftService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stories/draft")
@RequiredArgsConstructor
public class StoryDraftController {
    private final StoryDraftService storyDraftService;

    //1. 동화 제작 시작(POST /api/stories/draft)
    @PostMapping
    public ResponseEntity<StoryDraftResponseDto> startDraft(
            @AuthenticationPrincipal User user,
            @RequestBody StoryDraftCreateRequest dto) {
        StoryDraftResponseDto response = storyDraftService.createDraft(user, dto);
        return ResponseEntity.ok(response);
    }

    //2. 답변 전송 및 진행(PATCH /api/stories/draft/{draftId})
    @PatchMapping("/{draftId}")
    public ResponseEntity<StoryDraftResponseDto> proceedDraft(
            @AuthenticationPrincipal User user,
            @PathVariable Long draftId,
            @RequestBody StoryAnswerRequest dto) {
        StoryDraftResponseDto response = storyDraftService.proceedDraft(user, draftId, dto.getUserAnswer());
        return ResponseEntity.ok(response);
    }
}
