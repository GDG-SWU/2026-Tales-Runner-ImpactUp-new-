package com.example.dual_tales.domain.story;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoryRepository extends JpaRepository<Story, Long> {
    //선택한 언어의 공개 동화 최신순 조회
    List<Story> findAllByTargetLangCodeAndIsPublicTrue(String targetLangCode);
    //내 동화 최신순 조회
    List<Story> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}
