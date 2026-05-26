package com.example.dual_tales.domain.story_content;

import com.example.dual_tales.domain.story.Story;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoryContent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id")
    private Story story;

    private int sequence; //페이지 번호

    @Column(columnDefinition = "TEXT")
    private String question_ko;

    @Column(columnDefinition = "TEXT")
    private String question_foreign;

    @Column(columnDefinition = "TEXT")
    private String answer;

    @Column(name = "content_ko", columnDefinition = "TEXT")
    private String contentKo; //한국어 문장

    @Column(name = "content_foreign", columnDefinition = "TEXT")
    private String contentForeign; //외국어 문장

    @Column(name = "image_url", length=1000)
    private String imageUrl; //삽화 경로
}
