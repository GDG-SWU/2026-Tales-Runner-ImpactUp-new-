package com.example.dualtales.network

import com.example.dualtales.network.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("api/users/signup")
    suspend fun signUp(@Body body: UserRequestDto): Response<Long>

    @POST("api/users/login")
    suspend fun login(@Body body: LoginRequestDto): Response<UserResponseDto>

    @POST("api/stories/draft")
    suspend fun startDraft(@Body body: StoryDraftCreateRequest): Response<StoryDraftResponseDto>

    @PATCH("api/stories/draft/{draftId}")
    suspend fun proceedDraft(
        @Path("draftId") draftId: Long,
        @Body body: StoryAnswerRequest
    ): Response<StoryDraftResponseDto>

    @GET("api/stories/my")
    suspend fun getMyStories(): Response<List<StoryResponseDto>>

    @GET("api/stories/{storyId}")
    suspend fun getStoryDetail(@Path("storyId") storyId: Long): Response<StoryDetailResponseDto>

    @GET("api/stories/feed")
    suspend fun getFeed(@Query("langCode") langCode: String): Response<List<StoryResponseDto>>

    @DELETE("api/stories/{storyId}")
    suspend fun deleteStory(@Path("storyId") storyId: Long): Response<Void>

    @PATCH("api/stories/{storyId}/title")
    suspend fun updateTitle(
        @Path("storyId") storyId: Long,
        @Query("newTitle") newTitle: String
    ): Response<Void>

    @PATCH("api/stories/{storyId}/public")
    suspend fun togglePublic(@Path("storyId") storyId: Long): Response<Void>
}
