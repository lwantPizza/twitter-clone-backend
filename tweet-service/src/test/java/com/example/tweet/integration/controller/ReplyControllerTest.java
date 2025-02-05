package com.example.tweet.integration.controller;

import com.example.tweet.client.ProfileServiceClient;
import com.example.tweet.client.StorageServiceClient;
import com.example.tweet.integration.IntegrationTestBase;
import com.example.tweet.integration.mocks.ProfileClientMock;
import com.example.tweet.service.MessageSourceService;
import com.example.tweet.service.ReplyService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static com.example.tweet.integration.constants.GlobalConstants.*;
import static com.example.tweet.integration.constants.UrlConstants.REPLIES_URL_WITH_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@RequiredArgsConstructor
@Sql(value = "classpath:/sql/data.sql")
@SuppressWarnings("all")
public class ReplyControllerTest extends IntegrationTestBase {

    private final MockMvc mockMvc;
    private final ReplyService replyService;
    private final MessageSourceService messageSourceService;

    @MockBean
    private final ProfileServiceClient profileServiceClient;

    @MockBean
    private final StorageServiceClient storageServiceClient;

    @BeforeEach
    public void setUp() {
        ProfileClientMock.setupProfileClientResponse(profileServiceClient);
    }

    @Test
    public void replyTest() throws Exception {
        replyAndExpectSuccess(DEFAULT_REPLY_TEXT.getConstant(), 1L, DEFAULT_TWEET_TEXT.getConstant(), 1, 1);
        replyAndExpectSuccess(DEFAULT_REPLY_TEXT.getConstant(), 1L, DEFAULT_TWEET_TEXT.getConstant(), 2, 2);
        replyAndExpectSuccess(DEFAULT_REPLY_TEXT.getConstant(), 3L, DEFAULT_REPLY_TEXT.getConstant(), 1, 3);

        replyAndExpectFailure(
                100L,
                DEFAULT_REPLY_TEXT.getConstant(),
                0,
                3,
                NOT_FOUND,
                "$.message",
                messageSourceService.generateMessage("error.entity.not_found", 100)
        );
        replyAndExpectFailure(
                1L,
                "",
                2,
                3,
                BAD_REQUEST,
                "$.text",
                TEXT_EMPTY_MESSAGE.getConstant()
        );
    }

    private void replyAndExpectSuccess(String replyText, Long parentTweetId, String replyToText, int repliesForTweet, int repliesForUser) throws Exception {
        mockMvc.perform(multipart(
                        HttpMethod.POST,
                        REPLIES_URL_WITH_ID.getConstant().formatted(parentTweetId))
                        .file(createRequest(replyText))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("loggedInUser", EMAIL.getConstant())
                )
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.replyTo.text").value(replyToText),
                        jsonPath("$.replyTo.replies").value(repliesForTweet),
                        jsonPath("$.replyTo.retweets").exists(),
                        jsonPath("$.replyTo.likes").exists(),
                        jsonPath("$.replyTo.views").exists(),
                        jsonPath("$.replyTo.profile").exists(),
                        jsonPath("$.quoteTo").value(IsNull.nullValue()),
                        jsonPath("$.retweetTo").value(IsNull.nullValue()),
                        jsonPath("$.text").value(replyText),
                        jsonPath("$.replies").exists(),
                        jsonPath("$.retweets").exists(),
                        jsonPath("$.likes").exists(),
                        jsonPath("$.views").exists(),
                        jsonPath("$.profile.email").value(EMAIL.getConstant()),
                        jsonPath("$.profile.username").value(USERNAME.getConstant()),
                        jsonPath("$.creationDate").exists(),
                        jsonPath("$.mediaUrls").value(IsNull.nullValue())
                );
        checkNumberOfReplies(parentTweetId, repliesForTweet, repliesForUser);
    }

    private void replyAndExpectFailure(
            Long replyToId,
            String replyText,
            int repliesForTweet,
            int repliesForUser,
            HttpStatus status,
            String jsonPath,
            String message
    ) throws Exception {
        mockMvc.perform(multipart(
                        HttpMethod.POST,
                        REPLIES_URL_WITH_ID.getConstant().formatted(replyToId))
                        .file(createRequest(replyText))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("loggedInUser", EMAIL.getConstant())
                )
                .andExpectAll(
                        status().is(status.value()),
                        jsonPath(jsonPath).value(message)
                );
        checkNumberOfReplies(replyToId, repliesForTweet, repliesForUser);
    }

    private void checkNumberOfReplies(long parentTweetId, int repliesForTweet, int repliesForUser) {
        try {
            assertEquals(repliesForTweet, replyService.findAllRepliesForTweet(parentTweetId).size());
        } catch (EntityNotFoundException ignored) {
        }
        assertEquals(repliesForUser, replyService.findAllRepliesForUser(EMAIL.getConstant(), PageRequest.of(0, 20)).size());
    }
}
