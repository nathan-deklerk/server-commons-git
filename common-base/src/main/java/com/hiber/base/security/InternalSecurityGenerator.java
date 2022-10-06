package com.hiber.base.security;

import java.util.Collection;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

/**
 * Jwt token generator for authentication object.
 */
@RequiredArgsConstructor
public class InternalSecurityGenerator {
	private final JwtAccessTokenConverter tokenConverter;

	private final DefaultTokenServices tokenServices;

	/**
	 * Creates internal jwt access token for logged principal.
	 *
	 * @param authentication Source authentication object.
	 *
	 * @return Internal jwt token.
	 */
	public String createInternalJwtAccessToken(Authentication authentication) {
		Object username = authentication.getPrincipal();
		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
		OAuth2Request oAuth2Request = new OAuth2Request(
				Collections.emptyMap(),
				"internalClient",
				null,
				true,
				Collections.emptySet(),
				null,
				null,
				null,
				null
		);
		AbstractAuthenticationToken authenticationToken = new AbstractAuthenticationToken(authorities) {
			@Override
			public Object getCredentials() {
				return null;
			}

			@Override
			public Object getPrincipal() {
				return username;
			}
		};
		OAuth2Authentication oAuth2Authentication = new OAuth2Authentication(oAuth2Request, authenticationToken);
		OAuth2AccessToken accessToken = tokenServices.createAccessToken(oAuth2Authentication);
		OAuth2AccessToken enhanceAccessToken = tokenConverter.enhance(accessToken, oAuth2Authentication);
		return enhanceAccessToken.getValue();
	}
}
