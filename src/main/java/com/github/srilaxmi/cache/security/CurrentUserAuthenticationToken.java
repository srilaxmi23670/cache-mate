package com.github.srilaxmi.cache.security;


import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class CurrentUserAuthenticationToken {

    public Mono<Authentication> create(String userId) {

        PreAuthenticatedAuthenticationToken token = new PreAuthenticatedAuthenticationToken(userId, userId);
        token.setAuthenticated(true);

        CustomContext context = new CustomContext();
        context.setUserId(userId);
        token.setDetails(context);

        return Mono.just(token);
    }
}

