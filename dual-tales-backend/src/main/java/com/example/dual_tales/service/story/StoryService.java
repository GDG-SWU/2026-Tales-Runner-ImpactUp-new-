package com.example.dual_tales.service.story;

import com.example.dual_tales.api.story.dto.StoryCreateRequestDto;
import com.example.dual_tales.api.story.dto.StoryDetailResponseDto;
import com.example.dual_tales.api.story.dto.StoryResponseDto;
import com.example.dual_tales.domain.story.Story;
import com.example.dual_tales.domain.story.StoryRepository;
import com.example.dual_tales.domain.story_content.StoryContent;
import com.example.dual_tales.domain.story_content.StoryContentRepository;
import com.example.dual_tales.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor //자동 생성자 주입 어노테이션 (의존성 주입 중 하나)
public class StoryService {
    private final StoryRepository storyRepository;
    private final StoryContentRepository storyContentRepository;

    //동화 생성(최종 답변 저장시 호출)
    @Transactional
    public Long createStory(User user, StoryCreateRequestDto requestDto) {
        //데이터 무결성 검사
        if(requestDto.getPageCount() != requestDto.getContents().size()) {
            throw new IllegalArgumentException("설정된 페이지 수와 실제 데이터 개수가 일치하지 않습니다.");
        }
        //1. 부모엔티티(Story) 생성 및 저장
        Story story = Story.builder()
                .user(user)
                .title(requestDto.getTitle())
                .targetLangCode(requestDto.getTargetLangCode())
                .targetAge(requestDto.getTargetAge())
                .page_count(requestDto.getPageCount())
                .status("COMPLETED")
                .isPublic(false)
                .build();

        Story savedStory = storyRepository.save(story);

        //2. 자식 엔티티(StoryContent) 리스트 생성 및 저장
        List<StoryContent> contents = requestDto.getContents().stream()
                .map(dto ->StoryContent.builder()
                        .story(savedStory)
                        .sequence(dto.getSequence())
                        .contentKo(dto.getContent_ko())
                        .contentForeign(dto.getContent_foreign())
                        .imageUrl(dto.getImage_url())
                        .build())
                .toList();

        storyContentRepository.saveAll(contents);

        return savedStory.getId();
    }

    //동화 삭제
    @Transactional
    public void deleteStory(Long userId, Long storyId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(()->new IllegalArgumentException("해당 동화를 찾을 수 없습니다."));

        //본인 확인 로직
        if(!story.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("자신의 동화만 삭제 가능합니다.");
        }

        storyRepository.delete(story);
    }

    //동화 제목 수정
    @Transactional
    public void updateStoryTitle(Long userId, Long storyId, String newTitle) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(()->new IllegalArgumentException("해당 동화를 찾을 수 없습니다."));

        if(!story.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("자신의 동화만 수정 가능합니다.");
        }

        story.setTitle(newTitle);
    }

    //동화 공개/비공개 상태 전환
    @Transactional
    public void toggleStoryPublic(Long userId, Long storyId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(()->new IllegalArgumentException("해당 동화를 찾을 수 없습니다."));

        if(!story.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("자신의 동화만 수정 가능합니다.");
        }

        story.setPublic(!story.isPublic()); //현재 상태 반전
    }

    //내 동화 목록 조회
    @Transactional(readOnly = true)
    public List<StoryResponseDto> getMyStories(Long userId) {
        return storyRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(StoryResponseDto::new)
                .collect(Collectors.toList());
    }

    //동화 상세 조회
    @Transactional(readOnly = true)
    public StoryDetailResponseDto getStoryDetail(Long storyId) {
        //1. 동화 기본 정보 조회
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new IllegalArgumentException("해당 동화를 찾을 수 없습니다."));
        //2. 동화의 페이지 내용들을 순서대로(sequence) 조회
        return new StoryDetailResponseDto(story, story.getContent());
    }

    @Transactional(readOnly = true)
    public List<StoryResponseDto> getPublicStoriesByLanguage(String langCode) {
        //공개된 동화 중 특정 언어 최신순으로 가져와서 DTO 반환
        return storyRepository.findAllByTargetLangCodeAndIsPublicTrue(langCode)
                .stream()
                .map(StoryResponseDto::new)
                .collect(Collectors.toList());
    }
}
