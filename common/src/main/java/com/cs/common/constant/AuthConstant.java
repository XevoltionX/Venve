package com.cs.common.constant;

public class AuthConstant {

    public static final String HEADER_TOKEN = "Authorization";
    public static final String PREFIX_BEARER = "Bearer ";
    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USER_NAME = "X-User-Name";
    public static final String HEADER_USER_ROLE = "X-User-Role";

    public static final String[] WHITE_LIST = {
            "/api/auth/login",
            "/api/auth/register",
            "/swagger-ui",
            "/webjars",
            "/v3/api-docs",
            "/auth/v3/api-docs",
            "/venue/v3/api-docs",
            "/booking/v3/api-docs",
            "/api/monitor",
            "/actuator"
    };
}
