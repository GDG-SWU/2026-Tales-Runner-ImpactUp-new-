package com.example.dual_tales.domain.story_content;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoryContentRepository extends JpaRepository<StoryContent, Long> {
}
