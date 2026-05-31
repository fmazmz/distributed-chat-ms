package org.fmazmz.bff.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.fmazmz.bff.dto.auth.TokenResponse;
import org.fmazmz.bff.security.ChatAccessTokenCookie;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice(assignableTypes = AuthBffController.class)
public class AuthCookieResponseAdvice implements ResponseBodyAdvice<TokenResponse> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return TokenResponse.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public TokenResponse beforeBodyWrite(
            TokenResponse body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {
        if (body == null || body.accessToken() == null || body.accessToken().isBlank()) {
            return body;
        }
        if (response instanceof ServletServerHttpResponse servletResponse
                && request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletResponse httpResponse = servletResponse.getServletResponse();
            HttpServletRequest httpRequest = servletRequest.getServletRequest();
            long maxAge = body.expiresInSeconds() > 0 ? body.expiresInSeconds() : 3600;
            ChatAccessTokenCookie.write(httpResponse, body.accessToken(), maxAge, httpRequest.isSecure());
        }
        return body;
    }
}
