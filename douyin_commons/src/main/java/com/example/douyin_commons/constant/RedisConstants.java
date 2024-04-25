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

    public static final String MEDIA_URL_KEY = "mediaUrl";
    public static final String COVER_URL_KEY = "coverUrl";
    public static final String PUBLIST_USER_KEY = "publist:mediaID:";
    public static final Long PUBLIST_USER_TTL = 7L;       //单位:天
}
