package com.example.dual_tales.api.story;

import com.example.dual_tales.api.story.dto.StoryCreateRequestDto;
import com.example.dual_tales.api.story.dto.StoryDetailResponseDto;
import com.example.dual_tales.api.story.dto.StoryResponseDto;
import com.example.dual_tales.domain.user.User;
import com.example.dual_tales.service.story.StoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController //외부 요청을 받아서 JSON으로 응답하는 컨트롤러 어노테이션(@Controller + @ResponseBody)
@RequestMapping("/api/stories")
@RequiredArgsConstructor
@Tag(name="Story", description = "동화 관련 API")
public class StoryController {
    private final StoryService storyService;

    //POST : 동화 생성
    //1. 최종 생성된 동화 저장
    @PostMapping
    @Operation(summary = "동화 생성", description = "AI가 완성한 동화 내용을 DB에 최종 저장합니다.")
    public ResponseEntity<Long> createStory(
            @AuthenticationPrincipal User user, //로그인한 유저 정보
            @RequestBody StoryCreateRequestDto requestDto) {

        Long storyId = storyService.createStory(user, requestDto);
        return ResponseEntity.ok(storyId);
    }

    //GET: 내 동화 목록 조회
    @GetMapping("/my")
    @Operation(summary = "내 동화 목록 조회", description = "내가 작성한 동화 리스트를 최신순으로 조회합니다.")
    public List<StoryResponseDto> getMyStories(@AuthenticationPrincipal User user) {
        return storyService.getMyStories(user.getId());
    }

    //GET: 동화 상세 조회
    @GetMapping("/{storyId}")
    @Operation(summary = "동화 상세 조회", description = "동화의 제목과 모든 페이지 내용을 순서대로 반환합니다.")
    public StoryDetailResponseDto getStoryDetail(@PathVariable Long storyId) {
        return storyService.getStoryDetail(storyId);
    }
    //GET: 언어별 공유 피드 조회
    @GetMapping("/feed")
    @Operation(summary = "언어별 공유 피드 조회", description = "언어코드(KO, EN, FR)를 받아 해당 언어의 공개 동화 목록을 반환")
    public List<StoryResponseDto> getFeedByLanguage(@RequestParam String langCode){
        return storyService.getPublicStoriesByLanguage(langCode);
    }

    //DELETE : 동화 삭제
    @DeleteMapping("/{storyId}")
    @Operation(summary = "동화 삭제", description = "본인이 생성한 동화를 삭제합니다.")
    public ResponseEntity<Void> deleteStory(
            @AuthenticationPrincipal User user,
            @PathVariable Long storyId) {
        storyService.deleteStory(user.getId(), storyId);
        return ResponseEntity.ok().build();
    }

    //PATCH : 동화 제목 수정
    @PatchMapping("/{storyId}/title")
    @Operation(summary = "동화 제목 수정", description = "동화의 제목을 변경합니다.")
    public ResponseEntity<Void> updateTitle(
            @AuthenticationPrincipal User user,
            @PathVariable Long storyId,
            @RequestParam String newTitle) {
        storyService.updateStoryTitle(user.getId(), storyId, newTitle);
        return ResponseEntity.ok().build();
    }

    //PATCH : 공개 여부 전환
    @PatchMapping("/{storyId}/public")
    @Operation(summary = "공개 여부 전환", description = "동화의 공개/비공개 상태를 토글합니다.")
    public ResponseEntity<Void> togglePublic(
            @AuthenticationPrincipal User user,
            @PathVariable Long storyId) {
        storyService.toggleStoryPublic(user.getId(), storyId);
        return ResponseEntity.ok().build();
    }
}