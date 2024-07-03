package com.github.srilaxmi.cache.security;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomReactiveAuthorizationManager implements ReactiveAuthorizationManager<AuthorizationContext> {

    @Override
    public Mono<AuthorizationDecision> check(Mono<Authentication> authentication, AuthorizationContext ac) {

        return authentication.flatMap(auth -> {
            if (!Objects.nonNull(auth.getPrincipal()))
                return Mono.just(new AuthorizationDecision(false));

            return Mono.just(new AuthorizationDecision(true));

        });
    }
}

