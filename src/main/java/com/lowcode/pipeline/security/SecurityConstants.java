package com.lowcode.pipeline.security;

public class SecurityConstants {

//    TODO: Move these values to yaml file
    public static final String SECRET = "change-me-in-production";
    public static final long EXPIRATION_TIME = 864_000_000; // 10 days
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final String SIGN_UP_URL = "/user/register";
}
