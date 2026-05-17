package org.fmazmz.bff.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

/**
 * HttpOnly cookie used for browser WebSocket handshakes (browsers cannot set {@code Authorization} on WS).
 * REST continues to use {@code Authorization: Bearer} from the SPA; both are set on login/register.
 */
public final class ChatAccessTokenCookie {

    public static final String NAME = "chat_access_token";

    private ChatAccessTokenCookie() {}

    public static void write(HttpServletResponse response, String accessToken, long maxAgeSeconds, boolean secure) {
        ResponseCookie cookie = ResponseCookie.from(NAME, accessToken)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofSeconds(maxAgeSeconds))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public static void clear(HttpServletResponse response, boolean secure) {
        ResponseCookie cookie = ResponseCookie.from(NAME, "")
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public static String readRawToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (NAME.equals(cookie.getName())) {
                String value = cookie.getValue();
                return value != null && !value.isBlank() ? value.trim() : null;
            }
        }
        return null;
    }

    public static String readRawTokenFromHandshake(org.springframework.http.HttpHeaders headers) {
        String cookieHeader = headers.getFirst(HttpHeaders.COOKIE);
        if (cookieHeader == null || cookieHeader.isBlank()) {
            return null;
        }
        for (String part : cookieHeader.split(";")) {
            String trimmed = part.trim();
            if (trimmed.startsWith(NAME + "=")) {
                String value = trimmed.substring((NAME + "=").length()).trim();
                return value.isBlank() ? null : value;
            }
        }
        return null;
    }
}
