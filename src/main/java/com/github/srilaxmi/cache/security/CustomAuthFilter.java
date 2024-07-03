package com.github.srilaxmi.cache.security;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Component
@Slf4j
public class CustomAuthFilter {

    private final String USER_ID_HEADER = "x-user-id";

    private final CustomReactiveAuthenticationManager customReactiveAuthenticationManager;
    private final CurrentUserAuthenticationToken currentUserAuthenticationToken;

    public WebFilter getFilter() {

        AuthenticationWebFilter authWebFilter = new AuthenticationWebFilter(customReactiveAuthenticationManager);

        authWebFilter.setServerAuthenticationConverter((serverWebExchange) -> {
            return Mono.justOrEmpty(serverWebExchange.getRequest()
                                            .getHeaders()
                                            .getFirst(USER_ID_HEADER))
                    .flatMap(currentUserAuthenticationToken::create);
        });

        return authWebFilter;
    }
}

