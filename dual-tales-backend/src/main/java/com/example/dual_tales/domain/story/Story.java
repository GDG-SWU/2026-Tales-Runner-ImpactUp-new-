package com.example.dual_tales.domain.story;

import com.example.dual_tales.domain.story_content.StoryContent;
import com.example.dual_tales.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Story {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 여러 개의 Story가 한명의 유저에게 속함(N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") //외래키 연결
    private User user; //JPA 방식(아이디만 가져오는것이 아니라, user 속 모든 정보를 들고있게 함)

    private String title;

    @Column(name="cover_image", length = 1000)
    private String cover_image;
    private String targetLangCode; //모국어 코드(ex: en, vi 등)
    private String status;
    private int targetAge;

    private Integer page_count;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    //mappedBY = "story" : StoryContent 엔티티에 있는 'story' 필드에 의해 관리된다
    @OneToMany(mappedBy = "story", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequence ASC")
    private List<StoryContent> content = new ArrayList<>();

    @Column(nullable = false)
    private boolean isPublic = false;
}
