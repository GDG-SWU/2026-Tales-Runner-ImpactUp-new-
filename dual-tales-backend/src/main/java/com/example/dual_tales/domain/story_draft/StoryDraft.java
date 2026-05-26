package com.example.dual_tales.domain.story_draft;

import com.example.dual_tales.domain.user.User;
import com.example.dual_tales.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access= AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class StoryDraft extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    private int currentStep; //현재 질문 단계

    @Column(columnDefinition = "TEXT")
    private String history; //지금까지 오간 질문/답변을 JSON으로 저장

    @Column(columnDefinition = "TEXT")
    private String storyState;

    private String targetLangCode;
    private int targetAge;

    public void updateStep(int nextStep, String updatedHistory, String storyState) {
        this.currentStep = nextStep;
        this.history = updatedHistory;
        this.storyState = storyState;
    }
}
