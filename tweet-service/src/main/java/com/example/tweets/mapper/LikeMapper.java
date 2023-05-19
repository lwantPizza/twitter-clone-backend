package com.example.tweets.mapper;

import com.example.tweets.client.ProfileServiceClient;
import com.example.tweets.entity.Like;
import com.example.tweets.entity.Tweet;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LikeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parentTweet", expression = "java(parentTweet)")
    @Mapping(target = "profileId", expression = "java(profileServiceClient.getProfileIdByLoggedInUser(loggedInUser))")
    Like toEntity (
            Tweet parentTweet,
            @Context ProfileServiceClient profileServiceClient,
            @Context String loggedInUser
    );
}
