package com.example.dual_tales.domain.story_draft;

import com.example.dual_tales.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoryDraftRepository extends JpaRepository<StoryDraft, Long> {
    //특정 사용자의 가장 최근 임시저장본을 가져오기 위한 메서드
    Optional<StoryDraft> findFirstByUserOrderByCreatedAtDesc(User user);
}
