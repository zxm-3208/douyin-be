package com.example.douyin_commons.constant;

public class RedisConstants {
    public static final String LOGIN_CODE_KEY = "login:code:";
    public static final Long LOGIN_CODE_TTL = 2L;

    public static final String MEDIA_MERGEMD5_KEY = "mediaMerge:md5:";
    public static final Long MEDIA_MERGEMD5_TTL = 60L;
    public static final String COVERMD5_KEY = "cover:md5:";
    public static final Long COVERMD5_TTL = 1440L;

    public static final String MEDIA_CHUNKMD5_KEY = "mediaChunk:md5:";
    public static final Long MEDIA_CHUNKMD5_TTL = 60L;


    // 视频播放列表(各用户推荐栏中的视频列表)
    public static final String MEDIA_URL_KEY = "mediaUrl";
    public static final String COVER_URL_KEY = "coverUrl";
    public static final String PUBLIST_MEDIAID_KEY = "publist:mediaID:";
    public static final Long PUBLIST_MEDIAID_TTL = 7L;       //单位:天

    // 视频播放列表(各用户发布的视频列表的封面url)
    public static final String PUBLIST_USER_COVER_KEY = "publist:userId:cover:";
    public static final Long PUBLIST_USER_COVER_TTL = 7L;
    // 视频播放列表(各用户发布的视频列表的视频url)
    public static final String PUBLIST_USER_MEDIA_KEY = "publist:userId:media:";
    public static final Long PUBLIST_USER_MEDIA_TTL = 7L;

    public static final String LIKE_USER_COVER_KEY = "liked:userId:cover:";
    public static final Long LIKE_USER_COVER_TTL = 7L;
    // 视频播放列表(各用户发布的视频列表的视频url)
    public static final String LIKE_USER_MEDIA_KEY = "liked:userId:media:";
    public static final Long LIKE_USER_MEDIA_TTL = 7L;

    // 视频播放列表(默认发布的视频列表的视频url)
    public static final String PUBLIST_DEFAULT_MEDIA_KEY = "publist:feed:default";
    public static final Long PUBLIST_DEFAULT_MEDIA_TTL = 7L;

    // 存储用户名
    public static final String USER_TOKEN_KEY = "user:token:";
    public static final Long USER_TOKEN_TTL = 48L;

    // 存储视频点赞
    public static final String MEDIA_USER_LIKE_KEY = "liked:media:";

    // 用户视频点赞列表
    public static final String USER_LIKE_MEDIA_LIST_KEY = "liked:user:";

    // 用户关注列表
    public static final String USER_FOLLOW_LIST_KEY = "follow:user:";

}
