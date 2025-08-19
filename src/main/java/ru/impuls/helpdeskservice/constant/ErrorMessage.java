package ru.impuls.helpdeskservice.constant;

public class ErrorMessage {
    public static final String GROUP_ALREADY_EXISTS = "Group already exists";

    public static final String GROUP_NOT_FOUND = "Group not found";

    public static final String SUBGROUP_ALREADY_EXISTS = "Subgroup already exists";

    public static final String SUBGROUP_NOT_FOUND = "Subgroup not found";

    public static final String USER_NOT_FOUND = "User not found";

    public static final String USER_ALREADY_EXISTS = "User already exists";

    public static final String ROLE_NOT_FOUND = "Role not found";

    public static final String PRIVATE_KEY_FILE_NOT_FOUND = "Private key file not found";

    public static final String PUBLIC_KEY_FILE_NOT_FOUND = "Public key file not found";

    public static final String JWT_SIGNING_ERROR = "JWT signing error";

    public static final String JWT_GENERATION_ERROR = "JWT generation error";

    public static final String TOKEN_EXPIRED = "Token expired";

    public static final String INVALID_JWT_TOKEN = "invalid_token";

    public static final String INVALID_JWT_SIGNATURE = "Invalid JWT signature";

    public static final String MISSING_JWT_ROLES = "Missing JWT roles";

    public static final String INVALID_LOGIN_OR_PASSWORD = "Invalid login or password";

    public static final String USER_BLOCKED = "User blocked";

    public static final String REFRESH_NOT_FOUND_IN_COOKIE = "Refresh not found in cookie";

    public static final String ONLY_ACCESS_TOKENS_ALLOWED = "Only access tokens allowed";

    public static final String UNAUTHORIZED = "Unauthorized: Please log in to access this resource.";

    public static final String USER_NOT_AUTHENTICATED = "User not authenticated";

    public static final String CHAT_IS_CLOSED = "Chat is closed";

    public static final String CHAT_IS_WORKED_BY_ANOTHER_USER = "Chat is worked by another user";

    public static final String CHAT_NOT_FOUND = "Chat not found";
}